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
Utilities
~~~~~~~~~
"""
import argparse
import inspect

from argh import compat


def get_subparsers(parser, create=False):
    """
    Returns the :class:`argparse._SubParsersAction` instance for given
    :class:`ArgumentParser` instance as would have been returned by
    :meth:`ArgumentParser.add_subparsers`. The problem with the latter is that
    it only works once and raises an exception on the second attempt, and the
    public API seems to lack a method to get *existing* subparsers.

    :param create:
        If `True`, creates the subparser if it does not exist. Default if
        `False`.

    """
    # note that ArgumentParser._subparsers is *not* what is returned by
    # ArgumentParser.add_subparsers().
    if parser._subparsers:
        actions = [a for a in parser._actions
                   if isinstance(a, argparse._SubParsersAction)]
        assert len(actions) == 1
        return actions[0]
    else:
        if create:
            return parser.add_subparsers()


def get_arg_spec(function):
    """
    Returns argument specification for given function.  Omits special
    arguments of instance methods (`self`) and static methods (usually `cls`
    or something like this).
    """
    spec = compat.getargspec(function)
    if inspect.ismethod(function):
        spec = spec._replace(args=spec.args[1:])
    return spec
