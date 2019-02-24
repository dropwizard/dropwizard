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
Dispatching
~~~~~~~~~~~
"""
import argparse
import sys
from types import GeneratorType

from argh import compat, io
from argh.constants import (
    ATTR_WRAPPED_EXCEPTIONS,
    ATTR_WRAPPED_EXCEPTIONS_PROCESSOR,
    ATTR_EXPECTS_NAMESPACE_OBJECT,
    PARSER_FORMATTER,
    DEST_FUNCTION,
)
from argh.completion import autocomplete
from argh.assembling import add_commands, set_default_command
from argh.exceptions import DispatchingError, CommandError
from argh.utils import get_arg_spec


__all__ = ['dispatch', 'dispatch_command', 'dispatch_commands',
           'PARSER_FORMATTER', 'EntryPoint']


class ArghNamespace(argparse.Namespace):
    """
    A namespace object which collects the stack of functions (the
    :attr:`~argh.constants.DEST_FUNCTION` arguments passed to it via
    parser's defaults).
    """
    def __init__(self, *args, **kw):
        super(ArghNamespace, self).__init__(*args, **kw)
        self._functions_stack = []

    def __setattr__(self, k, v):
        if k == DEST_FUNCTION:
            # don't register the function under DEST_FUNCTION name.
            # If `ArgumentParser.parse_known_args()` sees that we already have
            # such attribute, it skips it.  However, it goes from the topmost
            # parser to subparsers.  We need the function mapped to the
            # subparser.  So we fool the `ArgumentParser` and pretend that we
            # didn't get a DEST_FUNCTION attribute; however, in fact we collect
            # all its values in a stack.  The last item in the stack would be
            # the function mapped to the innermost parser — the one we need.
            self._functions_stack.append(v)
        else:
            super(ArghNamespace, self).__setattr__(k, v)

    def get_function(self):
        return self._functions_stack[-1]


def dispatch(parser, argv=None, add_help_command=True,
             completion=True, pre_call=None,
             output_file=sys.stdout, errors_file=sys.stderr,
             raw_output=False, namespace=None,
             skip_unknown_args=False):
    """
    Parses given list of arguments using given parser, calls the relevant
    function and prints the result.

    The target function should expect one positional argument: the
    :class:`argparse.Namespace` object. However, if the function is decorated with
    :func:`~argh.decorators.plain_signature`, the positional and named
    arguments from the namespace object are passed to the function instead
    of the object itself.

    :param parser:

        the ArgumentParser instance.

    :param argv:

        a list of strings representing the arguments. If `None`, ``sys.argv``
        is used instead. Default is `None`.

    :param add_help_command:

        if `True`, converts first positional argument "help" to a keyword
        argument so that ``help foo`` becomes ``foo --help`` and displays usage
        information for "foo". Default is `True`.

    :param output_file:

        A file-like object for output. If `None`, the resulting lines are
        collected and returned as a string. Default is ``sys.stdout``.

    :param errors_file:

        Same as `output_file` but for ``sys.stderr``.

    :param raw_output:

        If `True`, results are written to the output file raw, without adding
        whitespaces or newlines between yielded strings. Default is `False`.

    :param completion:

        If `True`, shell tab completion is enabled. Default is `True`. (You
        will also need to install it.)  See :mod:`argh.completion`.

    :param skip_unknown_args:

        If `True`, unknown arguments do not cause an error
        (`ArgumentParser.parse_known_args` is used).

    :param namespace:

        An `argparse.Namespace`-like object.  By default an
        :class:`ArghNamespace` object is used.  Please note that support for
        combined default and nested functions may be broken if a different
        type of object is forced.

    By default the exceptions are not wrapped and will propagate. The only
    exception that is always wrapped is :class:`~argh.exceptions.CommandError`
    which is interpreted as an expected event so the traceback is hidden.
    You can also mark arbitrary exceptions as "wrappable" by using the
    :func:`~argh.decorators.wrap_errors` decorator.
    """
    if completion:
        autocomplete(parser)

    if argv is None:
        argv = sys.argv[1:]

    if add_help_command:
        if argv and argv[0] == 'help':
            argv.pop(0)
            argv.append('--help')

    if skip_unknown_args:
        parse_args = parser.parse_known_args
    else:
        parse_args = parser.parse_args

    if not namespace:
        namespace = ArghNamespace()

    # this will raise SystemExit if parsing fails
    namespace_obj = parse_args(argv, namespace=namespace)

    function = _get_function_from_namespace_obj(namespace_obj)

    if function:
        lines = _execute_command(function, namespace_obj, errors_file,
                                 pre_call=pre_call)
    else:
        # no commands declared, can't dispatch; display help message
        lines = [parser.format_usage()]

    if output_file is None:
        # user wants a string; we create an internal temporary file-like object
        # and will return its contents as a string
        if sys.version_info < (3,0):
            f = compat.BytesIO()
        else:
            f = compat.StringIO()
    else:
        # normally this is stdout; can be any file
        f = output_file

    for line in lines:
        # print the line as soon as it is generated to ensure that it is
        # displayed to the user before anything else happens, e.g.
        # raw_input() is called

        io.dump(line, f)
        if not raw_output:
            # in most cases user wants one message per line
            io.dump('\n', f)

    if output_file is None:
        # user wanted a string; return contents of our temporary file-like obj
        f.seek(0)
        return f.read()


def _get_function_from_namespace_obj(namespace_obj):
    if isinstance(namespace_obj, ArghNamespace):
        # our special namespace object keeps the stack of assigned functions
        try:
            function = namespace_obj.get_function()
        except (AttributeError, IndexError):
            return None
    else:
        # a custom (probably vanilla) namespace object keeps the last assigned
        # function; this may be wrong but at least something may work
        if not hasattr(namespace_obj, DEST_FUNCTION):
            return None
        function = getattr(namespace_obj, DEST_FUNCTION)

    if not function or not hasattr(function, '__call__'):
        return None

    return function


def _execute_command(function, namespace_obj, errors_file, pre_call=None):
    """
    Assumes that `function` is a callable.  Tries different approaches
    to call it (with `namespace_obj` or with ordinary signature).
    Yields the results line by line.

    If :class:`~argh.exceptions.CommandError` is raised, its message is
    appended to the results (i.e. yielded by the generator as a string).
    All other exceptions propagate unless marked as wrappable
    by :func:`wrap_errors`.
    """
    if pre_call:  # XXX undocumented because I'm unsure if it's OK
        # Actually used in real projects:
        # * https://google.com/search?q=argh+dispatch+pre_call
        # * https://github.com/neithere/argh/issues/63
        pre_call(namespace_obj)

    # the function is nested to catch certain exceptions (see below)
    def _call():
        # Actually call the function
        if getattr(function, ATTR_EXPECTS_NAMESPACE_OBJECT, False):
            result = function(namespace_obj)
        else:
            # namespace -> dictionary
            _flat_key = lambda key: key.replace('-', '_')
            all_input = dict((_flat_key(k), v)
                             for k,v in vars(namespace_obj).items())

            # filter the namespace variables so that only those expected
            # by the actual function will pass

            spec = get_arg_spec(function)

            positional = [all_input[k] for k in spec.args]
            kwonly = getattr(spec, 'kwonlyargs', [])
            keywords = dict((k, all_input[k]) for k in kwonly)

            # *args
            if spec.varargs:
                positional += getattr(namespace_obj, spec.varargs)

            # **kwargs
            varkw = getattr(spec, 'varkw', getattr(spec, 'keywords', []))
            if varkw:
                not_kwargs = [DEST_FUNCTION] + spec.args + [spec.varargs] + kwonly
                for k in vars(namespace_obj):
                    if k.startswith('_') or k in not_kwargs:
                        continue
                    keywords[k] = getattr(namespace_obj, k)

            result = function(*positional, **keywords)

        # Yield the results
        if isinstance(result, (GeneratorType, list, tuple)):
            # yield each line ASAP, convert CommandError message to a line
            for line in result:
                yield line
        else:
            # yield non-empty non-iterable result as a single line
            if result is not None:
                yield result

    wrappable_exceptions = [CommandError]
    wrappable_exceptions += getattr(function, ATTR_WRAPPED_EXCEPTIONS, [])

    try:
        result = _call()
        for line in result:
            yield line
    except tuple(wrappable_exceptions) as e:
        processor = getattr(function, ATTR_WRAPPED_EXCEPTIONS_PROCESSOR,
                            lambda e: '{0.__class__.__name__}: {0}'.format(e))

        errors_file.write(compat.text_type(processor(e)))
        errors_file.write('\n')


def dispatch_command(function, *args, **kwargs):
    """
    A wrapper for :func:`dispatch` that creates a one-command parser.
    Uses :attr:`PARSER_FORMATTER`.

    This::

        dispatch_command(foo)

    ...is a shortcut for::

        parser = ArgumentParser()
        set_default_command(parser, foo)
        dispatch(parser)

    This function can be also used as a decorator.
    """
    parser = argparse.ArgumentParser(formatter_class=PARSER_FORMATTER)
    set_default_command(parser, function)
    dispatch(parser, *args, **kwargs)


def dispatch_commands(functions, *args, **kwargs):
    """
    A wrapper for :func:`dispatch` that creates a parser, adds commands to
    the parser and dispatches them.
    Uses :attr:`PARSER_FORMATTER`.

    This::

        dispatch_commands([foo, bar])

    ...is a shortcut for::

        parser = ArgumentParser()
        add_commands(parser, [foo, bar])
        dispatch(parser)

    """
    parser = argparse.ArgumentParser(formatter_class=PARSER_FORMATTER)
    add_commands(parser, functions)
    dispatch(parser, *args, **kwargs)


class EntryPoint(object):
    """
    An object to which functions can be attached and then dispatched.

    When called with an argument, the argument (a function) is registered
    at this entry point as a command.

    When called without an argument, dispatching is triggered with all
    previously registered commands.

    Usage::

        from argh import EntryPoint

        app = EntryPoint('main', dict(description='This is a cool app'))

        @app
        def ls():
            for i in range(10):
                print i

        @app
        def greet():
            print 'hello'

        if __name__ == '__main__':
            app()

    """
    def __init__(self, name=None, parser_kwargs=None):
        self.name = name or 'unnamed'
        self.commands = []
        self.parser_kwargs = parser_kwargs or {}

    def __call__(self, f=None):
        if f:
            self._register_command(f)
            return f

        return self._dispatch()

    def _register_command(self, f):
        self.commands.append(f)

    def _dispatch(self):
        if not self.commands:
            raise DispatchingError('no commands for entry point "{0}"'
                                   .format(self.name))

        parser = argparse.ArgumentParser(**self.parser_kwargs)
        add_commands(parser, self.commands)
        dispatch(parser)
