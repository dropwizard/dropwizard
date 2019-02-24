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
Shell completion
~~~~~~~~~~~~~~~~

Command and argument completion is a great way to reduce the number of
keystrokes and improve user experience.

To display suggestions when you press :kbd:`tab`, a shell must obtain choices
from your program.  It calls the program in a specific environment and expects
it to return a list of relevant choices.

`Argparse` does not support completion out of the box.  However, there are
3rd-party apps that do the job, such as argcomplete_ and
python-selfcompletion_.

`Argh` supports only argcomplete_ which doesn't require subclassing
the parser and monkey-patches it instead.  Combining `Argh`
with python-selfcompletion_ isn't much harder though: simply use
`SelfCompletingArgumentParser` instead of vanilla `ArgumentParser`.

See installation details and gotchas in the documentation of the 3rd-party app
you've chosen for the completion backend.

`Argh` automatically enables completion if argcomplete_ is available
(see :attr:`COMPLETION_ENABLED`).  If completion is undesirable in given app by
design, it can be turned off by setting ``completion=False``
in :func:`argh.dispatching.dispatch`.

Note that you don't *have* to add completion via `Argh`; it doesn't matter
whether you let it do it for you or use the underlying API.

.. _argcomplete: https://github.com/kislyuk/argcomplete
.. _python-selfcompletion: https://github.com/dbarnett/python-selfcompletion

Argument-level completion
-------------------------

Argcomplete_ supports custom "completers".  The documentation suggests adding
the completer as an attribute of the argument parser action::

    parser.add_argument("--env-var1").completer = EnvironCompleter

However, this doesn't fit the normal `Argh`-assisted workflow.
It is recommended to use the :func:`~argh.decorators.arg` decorator::

    @arg('--env-var1', completer=EnvironCompleter)
    def func(...):
        ...

"""
import logging
import os


COMPLETION_ENABLED = False
"""
Dynamically set to `True` on load if argcomplete_ was successfully imported.
"""

try:
    import argcomplete
except ImportError:
    pass
else:
    COMPLETION_ENABLED = True


__all__ = ['autocomplete', 'COMPLETION_ENABLED']


logger = logging.getLogger(__package__)


def autocomplete(parser):
    """
    Adds support for shell completion via argcomplete_ by patching given
    `argparse.ArgumentParser` (sub)class.

    If completion is not enabled, logs a debug-level message.
    """
    if COMPLETION_ENABLED:
        argcomplete.autocomplete(parser)
    elif 'bash' in os.getenv('SHELL', ''):
        logger.debug('Bash completion not available. Install argcomplete.')
