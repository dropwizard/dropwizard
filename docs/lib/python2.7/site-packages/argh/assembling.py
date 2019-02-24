# coding: utf-8
#
#  Copyright © 2010—2014 Andrey Mikhaylenko and contributors
#
#  This file is part of Argh.
#
#  Argh is free software under terms of the GNU Lesser
#  General Public License version 3 (LGPLv3) as published by the Free
#  Software Foundation. See the file README.rst for copying conditions.
#
"""
Assembling
~~~~~~~~~~

Functions and classes to properly assemble your commands in a parser.
"""
import argparse
import sys
import warnings

from argh.completion import COMPLETION_ENABLED
from argh.compat import OrderedDict
from argh.constants import (
    ATTR_ALIASES,
    ATTR_ARGS,
    ATTR_NAME,
    ATTR_EXPECTS_NAMESPACE_OBJECT,
    PARSER_FORMATTER,
    DEFAULT_ARGUMENT_TEMPLATE,
    DEST_FUNCTION,
)
from argh.utils import get_subparsers, get_arg_spec
from argh.exceptions import AssemblingError


__all__ = [
    'SUPPORTS_ALIASES',
    'set_default_command',
    'add_commands',
    'add_subcommands',
]


def _check_support_aliases():
    p = argparse.ArgumentParser()
    s = p.add_subparsers()
    try:
        s.add_parser('x', aliases=[])
    except TypeError:
        return False
    else:
        return True


SUPPORTS_ALIASES = _check_support_aliases()
"""
Calculated on load. If `True`, current version of argparse supports
alternative command names (can be set via :func:`~argh.decorators.aliases`).
"""


def _get_args_from_signature(function):
    if getattr(function, ATTR_EXPECTS_NAMESPACE_OBJECT, False):
        return

    spec = get_arg_spec(function)

    defaults = dict(zip(*[reversed(x) for x in (spec.args,
                                                spec.defaults or [])]))
    defaults.update(getattr(spec, 'kwonlydefaults', None) or {})

    kwonly = getattr(spec, 'kwonlyargs', [])

    if sys.version_info < (3,0):
        annotations = {}
    else:
        annotations = dict((k,v) for k,v in function.__annotations__.items()
                           if isinstance(v, str))

    # define the list of conflicting option strings
    # (short forms, i.e. single-character ones)
    chars = [a[0] for a in spec.args + kwonly]
    char_counts = dict((char, chars.count(char)) for char in set(chars))
    conflicting_opts = tuple(char for char in char_counts
                             if 1 < char_counts[char])

    for name in spec.args + kwonly:
        flags = []    # name_or_flags
        akwargs = {}  # keyword arguments for add_argument()

        if name in annotations:
            # help message:  func(a : "b")  ->  add_argument("a", help="b")
            akwargs.update(help=annotations.get(name))

        if name in defaults or name in kwonly:
            if name in defaults:
                akwargs.update(default=defaults.get(name))
            else:
                akwargs.update(required=True)
            flags = ('-{0}'.format(name[0]), '--{0}'.format(name))
            if name.startswith(conflicting_opts):
                # remove short name
                flags = flags[1:]

        else:
            # positional argument
            flags = (name,)

        # cmd(foo_bar)  ->  add_argument('foo-bar')
        flags = tuple(x.replace('_', '-') for x in flags)

        yield dict(option_strings=flags, **akwargs)

    if spec.varargs:
        # *args
        yield dict(option_strings=[spec.varargs], nargs='*')


def _guess(kwargs):
    """
    Adds types, actions, etc. to given argument specification.
    For example, ``default=3`` implies ``type=int``.

    :param arg: a :class:`argh.utils.Arg` instance
    """
    guessed = {}

    # Parser actions that accept argument 'type'
    TYPE_AWARE_ACTIONS = 'store', 'append'

    # guess type/action from default value
    value = kwargs.get('default')
    if value is not None:
        if isinstance(value, bool):
            if kwargs.get('action') is None:
                # infer action from default value
                guessed['action'] = 'store_false' if value else 'store_true'
        elif kwargs.get('type') is None:
            # infer type from default value
            # (make sure that action handler supports this keyword)
            if kwargs.get('action', 'store') in TYPE_AWARE_ACTIONS:
                guessed['type'] = type(value)

    # guess type from choices (first item)
    if kwargs.get('choices') and 'type' not in list(guessed) + list(kwargs):
        guessed['type'] = type(kwargs['choices'][0])

    return dict(kwargs, **guessed)


def _is_positional(args, prefix_chars='-'):
    assert args
    if 1 < len(args) or args[0][0].startswith(tuple(prefix_chars)):
        return False
    else:
        return True


def _get_parser_param_kwargs(parser, argspec):
    argspec = argspec.copy()    # parser methods modify source data
    args = argspec['option_strings']

    if _is_positional(args, prefix_chars=parser.prefix_chars):
        get_kwargs = parser._get_positional_kwargs
    else:
        get_kwargs = parser._get_optional_kwargs

    kwargs = get_kwargs(*args, **argspec)

    kwargs['dest'] = kwargs['dest'].replace('-', '_')

    return kwargs


def _get_dest(parser, argspec):
    kwargs = _get_parser_param_kwargs(parser, argspec)
    return kwargs['dest']


def _require_support_for_default_command_with_subparsers():
    if sys.version_info < (3,4):
        raise AssemblingError(
            'Argparse library bundled with this version of Python '
            'does not support combining a default command with nested ones.')


def set_default_command(parser, function):
    """
    Sets default command (i.e. a function) for given parser.

    If `parser.description` is empty and the function has a docstring,
    it is used as the description.

    .. note::

       An attempt to set default command to a parser which already has
       subparsers (e.g. added with :func:`~argh.assembling.add_commands`)
       results in a `AssemblingError`.

    .. note::

       If there are both explicitly declared arguments (e.g. via
       :func:`~argh.decorators.arg`) and ones inferred from the function
       signature (e.g. via :func:`~argh.decorators.command`), declared ones
       will be merged into inferred ones. If an argument does not conform
       function signature, `AssemblingError` is raised.

    .. note::

       If the parser was created with ``add_help=True`` (which is by default),
       option name ``-h`` is silently removed from any argument.

    """
    if parser._subparsers:
        _require_support_for_default_command_with_subparsers()

    spec = get_arg_spec(function)

    declared_args = getattr(function, ATTR_ARGS, [])
    inferred_args = list(_get_args_from_signature(function))

    if inferred_args and declared_args:
        # We've got a mixture of declared and inferred arguments

        # a mapping of "dest" strings to argument declarations.
        #
        # * a "dest" string is a normalized form of argument name, i.e.:
        #
        #     '-f', '--foo' → 'foo'
        #     'foo-bar'     → 'foo_bar'
        #
        # * argument declaration is a dictionary representing an argument;
        #   it is obtained either from _get_args_from_signature() or from
        #   an @arg decorator (as is).
        #
        dests = OrderedDict()

        for argspec in inferred_args:
            dest = _get_parser_param_kwargs(parser, argspec)['dest']
            dests[dest] = argspec

        for declared_kw in declared_args:
            # an argument is declared via decorator
            dest = _get_dest(parser, declared_kw)
            if dest in dests:
                # the argument is already known from function signature
                #
                # now make sure that this declared arg conforms to the function
                # signature and therefore only refines an inferred arg:
                #
                #      @arg('my-foo')    maps to  func(my_foo)
                #      @arg('--my-bar')  maps to  func(my_bar=...)

                # either both arguments are positional or both are optional
                decl_positional = _is_positional(declared_kw['option_strings'])
                infr_positional = _is_positional(dests[dest]['option_strings'])
                if decl_positional != infr_positional:
                    kinds = {True: 'positional', False: 'optional'}
                    raise AssemblingError(
                        '{func}: argument "{dest}" declared as {kind_i} '
                        '(in function signature) and {kind_d} (via decorator)'
                        .format(
                            func=function.__name__,
                            dest=dest,
                            kind_i=kinds[infr_positional],
                            kind_d=kinds[decl_positional],
                        ))

                # merge explicit argument declaration into the inferred one
                # (e.g. `help=...`)
                dests[dest].update(**declared_kw)
            else:
                # the argument is not in function signature
                varkw = getattr(spec, 'varkw', getattr(spec, 'keywords', []))
                if varkw:
                    # function accepts **kwargs; the argument goes into it
                    dests[dest] = declared_kw
                else:
                    # there's no way we can map the argument declaration
                    # to function signature
                    xs = (dests[x]['option_strings'] for x in dests)
                    raise AssemblingError(
                        '{func}: argument {flags} does not fit '
                        'function signature: {sig}'.format(
                            flags=', '.join(declared_kw['option_strings']),
                            func=function.__name__,
                            sig=', '.join('/'.join(x) for x in xs)))

        # pack the modified data back into a list
        inferred_args = dests.values()

    command_args = inferred_args or declared_args

    # add types, actions, etc. (e.g. default=3 implies type=int)
    command_args = [_guess(x) for x in command_args]

    for draft in command_args:
        draft = draft.copy()
        if 'help' not in draft:
            draft.update(help=DEFAULT_ARGUMENT_TEMPLATE)
        dest_or_opt_strings = draft.pop('option_strings')
        if parser.add_help and '-h' in dest_or_opt_strings:
            dest_or_opt_strings = [x for x in dest_or_opt_strings if x != '-h']
        completer = draft.pop('completer', None)
        try:
            action = parser.add_argument(*dest_or_opt_strings, **draft)
            if COMPLETION_ENABLED and completer:
                action.completer = completer
        except Exception as e:
            raise type(e)('{func}: cannot add arg {args}: {msg}'.format(
                args='/'.join(dest_or_opt_strings), func=function.__name__, msg=e))

    if function.__doc__ and not parser.description:
        parser.description = function.__doc__
    parser.set_defaults(**{
        DEST_FUNCTION: function,
    })


def add_commands(parser, functions, namespace=None, namespace_kwargs=None,
                 func_kwargs=None,
                 # deprecated args:
                 title=None, description=None, help=None):
    """
    Adds given functions as commands to given parser.

    :param parser:

        an :class:`argparse.ArgumentParser` instance.

    :param functions:

        a list of functions. A subparser is created for each of them.
        If the function is decorated with :func:`~argh.decorators.arg`, the
        arguments are passed to :class:`argparse.ArgumentParser.add_argument`.
        See also :func:`~argh.dispatching.dispatch` for requirements
        concerning function signatures. The command name is inferred from the
        function name. Note that the underscores in the name are replaced with
        hyphens, i.e. function name "foo_bar" becomes command name "foo-bar".

    :param namespace:

        an optional string representing the group of commands. For example, if
        a command named "hello" is added without the namespace, it will be
        available as "prog.py hello"; if the namespace if specified as "greet",
        then the command will be accessible as "prog.py greet hello". The
        namespace itself is not callable, so "prog.py greet" will fail and only
        display a help message.

    :param func_kwargs:

        a `dict` of keyword arguments to be passed to each nested ArgumentParser
        instance created per command (i.e. per function).  Members of this
        dictionary have the highest priority, so a function's docstring is
        overridden by a `help` in `func_kwargs` (if present).

    :param namespace_kwargs:

        a `dict` of keyword arguments to be passed to the nested ArgumentParser
        instance under given `namespace`.

    Deprecated params that should be moved into `namespace_kwargs`:

    :param title:

        passed to :meth:`argparse.ArgumentParser.add_subparsers` as `title`.

        .. deprecated:: 0.26.0

           Please use `namespace_kwargs` instead.

    :param description:

        passed to :meth:`argparse.ArgumentParser.add_subparsers` as
        `description`.

        .. deprecated:: 0.26.0

           Please use `namespace_kwargs` instead.

    :param help:

        passed to :meth:`argparse.ArgumentParser.add_subparsers` as `help`.

        .. deprecated:: 0.26.0

           Please use `namespace_kwargs` instead.

    .. note::

        This function modifies the parser object. Generally side effects are
        bad practice but we don't seem to have any choice as ArgumentParser is
        pretty opaque.
        You may prefer :class:`~argh.helpers.ArghParser.add_commands` for a bit
        more predictable API.

    .. note::

       An attempt to add commands to a parser which already has a default
       function (e.g. added with :func:`~argh.assembling.set_default_command`)
       results in `AssemblingError`.

    """
    # FIXME "namespace" is a correct name but it clashes with the "namespace"
    # that represents arguments (argparse.Namespace and our ArghNamespace).
    # We should rename the argument here.

    if DEST_FUNCTION in parser._defaults:
        _require_support_for_default_command_with_subparsers()

    namespace_kwargs = namespace_kwargs or {}

    # FIXME remove this by 1.0
    #
    if title:
        warnings.warn('argument `title` is deprecated in add_commands(),'
                      ' use `parser_kwargs` instead', DeprecationWarning)
        namespace_kwargs['description'] = title
    if help:
        warnings.warn('argument `help` is deprecated in add_commands(),'
                      ' use `parser_kwargs` instead', DeprecationWarning)
        namespace_kwargs['help'] = help
    if description:
        warnings.warn('argument `description` is deprecated in add_commands(),'
                      ' use `parser_kwargs` instead', DeprecationWarning)
        namespace_kwargs['description'] = description
    #
    # /

    subparsers_action = get_subparsers(parser, create=True)

    if namespace:
        # Make a nested parser and init a deeper _SubParsersAction under it.

        # Create a named group of commands.  It will be listed along with
        # root-level commands in ``app.py --help``; in that context its `title`
        # can be used as a short description on the right side of its name.
        # Normally `title` is shown above the list of commands
        # in ``app.py my-namespace --help``.
        subsubparser_kw = {
            'help': namespace_kwargs.get('title'),
        }
        subsubparser = subparsers_action.add_parser(namespace, **subsubparser_kw)
        subparsers_action = subsubparser.add_subparsers(**namespace_kwargs)
    else:
        assert not namespace_kwargs, ('`parser_kwargs` only makes sense '
                                      'with `namespace`.')

    for func in functions:
        cmd_name, func_parser_kwargs = _extract_command_meta_from_func(func)

        # override any computed kwargs by manually supplied ones
        if func_kwargs:
            func_parser_kwargs.update(func_kwargs)

        # create and set up the parser for this command
        command_parser = subparsers_action.add_parser(cmd_name, **func_parser_kwargs)
        set_default_command(command_parser, func)


def _extract_command_meta_from_func(func):
    # use explicitly defined name; if none, use function name (a_b → a-b)
    cmd_name = getattr(func, ATTR_NAME,
                       func.__name__.replace('_','-'))

    func_parser_kwargs = {

        # add command help from function's docstring
        'help': func.__doc__,

        # set default formatter
        'formatter_class': PARSER_FORMATTER,

    }

    # try adding aliases for command name
    if SUPPORTS_ALIASES:
        func_parser_kwargs['aliases'] = getattr(func, ATTR_ALIASES, [])

    return cmd_name, func_parser_kwargs


def add_subcommands(parser, namespace, functions, **namespace_kwargs):
    """
    A wrapper for :func:`add_commands`.

    These examples are equivalent::

        add_commands(parser, [get, put], namespace='db',
                     namespace_kwargs={
                         'title': 'database commands',
                         'help': 'CRUD for our silly database'
                     })

        add_subcommands(parser, 'db', [get, put],
                        title='database commands',
                        help='CRUD for our silly database')

    """
    add_commands(parser, functions, namespace=namespace,
                 namespace_kwargs=namespace_kwargs)
