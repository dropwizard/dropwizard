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
Helpers
~~~~~~~
"""
import argparse

from argh.completion import autocomplete
from argh.assembling import add_commands, set_default_command
from argh.dispatching import PARSER_FORMATTER, ArghNamespace, dispatch


__all__ = ['ArghParser']


class ArghParser(argparse.ArgumentParser):
    """
    A subclass of :class:`ArgumentParser` with support for and a couple 
    of convenience methods.

    All methods are but wrappers for stand-alone functions
    :func:`~argh.assembling.add_commands`,
    :func:`~argh.completion.autocomplete` and
    :func:`~argh.dispatching.dispatch`.

    Uses :attr:`~argh.dispatching.PARSER_FORMATTER`.
    """
    def __init__(self, *args, **kwargs):
        kwargs.setdefault('formatter_class', PARSER_FORMATTER)
        super(ArghParser, self).__init__(*args, **kwargs)

    def set_default_command(self, *args, **kwargs):
        "Wrapper for :func:`~argh.assembling.set_default_command`."
        return set_default_command(self, *args, **kwargs)

    def add_commands(self, *args, **kwargs):
        "Wrapper for :func:`~argh.assembling.add_commands`."
        return add_commands(self, *args, **kwargs)

    def autocomplete(self):
        "Wrapper for :func:`~argh.completion.autocomplete`."
        return autocomplete(self)

    def dispatch(self, *args, **kwargs):
        "Wrapper for :func:`~argh.dispatching.dispatch`."
        return dispatch(self, *args, **kwargs)

    def parse_args(self, args=None, namespace=None):
        """
        Wrapper for :meth:`argparse.ArgumentParser.parse_args`.  If `namespace`
        is not defined, :class:`argh.dispatching.ArghNamespace` is used.
        This is required for functions to be properly used as commands.
        """
        namespace = namespace or ArghNamespace()
        return super(ArghParser, self).parse_args(args, namespace)
