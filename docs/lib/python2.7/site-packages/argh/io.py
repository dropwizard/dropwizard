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
Output Processing
~~~~~~~~~~~~~~~~~
"""
import locale
import sys

from argh import compat


__all__ = ['dump', 'encode_output', 'safe_input']


def _input(prompt):
    # this function can be mocked up in tests
    if sys.version_info < (3,0):
        return raw_input(prompt)
    else:
        return input(prompt)


def safe_input(prompt):
    """
    Prompts user for input. Correctly handles prompt message encoding.
    """

    if sys.version_info < (3,0):
        if isinstance(prompt, compat.text_type):
            # Python 2.x: unicode →  bytes
            encoding = locale.getpreferredencoding() or 'utf-8'
            prompt = prompt.encode(encoding)
    else:
        if not isinstance(prompt, compat.text_type):
            # Python 3.x: bytes →  unicode
            prompt = prompt.decode()

    return _input(prompt)


def encode_output(value, output_file):
    """
    Encodes given value so it can be written to given file object.

    Value may be Unicode, binary string or any other data type.

    The exact behaviour depends on the Python version:

    Python 3.x

        `sys.stdout` is a `_io.TextIOWrapper` instance that accepts `str`
        (unicode) and breaks on `bytes`.

        It is OK to simply assume that everything is Unicode unless special
        handling is introduced in the client code.

        Thus, no additional processing is performed.

    Python 2.x

        `sys.stdout` is a file-like object that accepts `str` (bytes)
        and breaks when `unicode` is passed to `sys.stdout.write()`.

        We can expect both Unicode and bytes. They need to be encoded so as
        to match the file object encoding.

        The output is binary if the object doesn't explicitly require Unicode.

    """
    if sys.version_info > (3,0):
        # Python 3:  whatever → unicode
        return compat.text_type(value)
    else:
        # Python 2:  handle special cases
        stream_encoding = getattr(output_file, 'encoding', None)
        if stream_encoding:
            if stream_encoding.upper() == 'UTF-8':
                return compat.text_type(value)
            else:
                return value.encode(stream_encoding, 'ignore')
        else:
            # no explicit encoding requirements; force binary
            if isinstance(value, compat.text_type):
                # unicode → binary
                return value.encode('utf-8')
            else:
                return str(value)


def dump(raw_data, output_file):
    """
    Writes given line to given output file.
    See :func:`encode_output` for details.
    """
    data = encode_output(raw_data, output_file)
    output_file.write(data)
