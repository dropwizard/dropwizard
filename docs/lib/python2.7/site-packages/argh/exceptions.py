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
Exceptions
~~~~~~~~~~
"""
class AssemblingError(Exception):
    """
    Raised if the parser could not be configured due to malformed
    or conflicting command declarations.
    """


class DispatchingError(Exception):
    """
    Raised if the dispatching could not be completed due to misconfiguration
    which could not be determined on an earlier stage.
    """


class CommandError(Exception):
    """
    Intended to be raised from within a command.  The dispatcher wraps this
    exception by default and prints its message without traceback.

    Useful for print-and-exit tasks when you expect a failure and don't want
    to startle the ordinary user by the cryptic output.

    Consider the following example::

        def foo(args):
            try:
                ...
            except KeyError as e:
                print(u'Could not fetch item: {0}'.format(e))
                return

    It is exactly the same as::

        def bar(args):
            try:
                ...
            except KeyError as e:
                raise CommandError(u'Could not fetch item: {0}'.format(e))

    This exception can be safely used in both print-style and yield-style
    commands (see :doc:`tutorial`).
    """
