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
import argparse

__all__ = (
    'ATTR_NAME', 'ATTR_ALIASES', 'ATTR_ARGS', 'ATTR_WRAPPED_EXCEPTIONS',
    'ATTR_WRAPPED_EXCEPTIONS_PROCESSOR', 'ATTR_EXPECTS_NAMESPACE_OBJECT',
    'PARSER_FORMATTER', 'DEFAULT_ARGUMENT_TEMPLATE', 'DEST_FUNCTION',
)


#
# Names of function attributes where Argh stores command behaviour
#

#: explicit command name (differing from function name)
ATTR_NAME = 'argh_name'

#: alternative command names
ATTR_ALIASES = 'argh_aliases'

#: declared arguments
ATTR_ARGS = 'argh_args'

#: list of exception classes that should be wrapped and printed as results
ATTR_WRAPPED_EXCEPTIONS = 'argh_wrap_errors'

#: a function to preprocess the exception object when it is wrapped
ATTR_WRAPPED_EXCEPTIONS_PROCESSOR = 'argh_wrap_errors_processor'

#: forcing argparse.Namespace object instead of signature introspection
ATTR_EXPECTS_NAMESPACE_OBJECT = 'argh_expects_namespace_object'

#
# Dest names in parser defaults
#

#: dest name for a function mapped to given endpoint (goes to Namespace obj)
DEST_FUNCTION = 'function'

#
# Other library-wide stuff
#

class CustomFormatter(argparse.ArgumentDefaultsHelpFormatter,
                      argparse.RawDescriptionHelpFormatter):
    def _expand_help(self, action):
        """
        This method is copied verbatim from ArgumentDefaultsHelpFormatter with
        a couple of lines added just before the end.  Reason: we need to
        `repr()` default values instead of simply inserting them as is.
        This helps notice, for example, an empty string as the default value;
        moreover, it prevents breaking argparse due to logical quirks inside
        of its formatters.

        Ideally this could be achieved by simply defining
        :attr:`DEFAULT_ARGUMENT_TEMPLATE` as ``{default!r}`` but unfortunately
        argparse only supports the old printf syntax.
        """
        params = dict(vars(action), prog=self._prog)
        for name in list(params):
            if params[name] is argparse.SUPPRESS:
                del params[name]
        for name in list(params):
            if hasattr(params[name], '__name__'):
                params[name] = params[name].__name__
        if params.get('choices') is not None:
            choices_str = ', '.join([str(c) for c in params['choices']])
            params['choices'] = choices_str

        # XXX this is added in Argh vs. argparse.ArgumentDefaultsHelpFormatter
        #     (avoiding empty strings, otherwise Argparse would die with
        #     an IndexError in _format_action)
        #
        if 'default' in params:
            if params['default'] is None:
                params['default'] = '-'
            else:
                params['default'] = repr(params['default'])
        #
        # /

        return self._get_help_string(action) % params


#: Default formatter to be used in implicitly instantiated ArgumentParser.
PARSER_FORMATTER = CustomFormatter


DEFAULT_ARGUMENT_TEMPLATE = '%(default)s'
"""
Default template of argument help message (see issue #64).
The template ``%(default)s`` is used by `argparse` to display the argument's
default value.
"""
