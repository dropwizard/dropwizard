#!/usr/bin/env python
# -*- coding: utf-8 -*-
# path.py: Path functions.
#
# Copyright (C) 2010 Yesudeep Mangalapilly <yesudeep@gmail.com>
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.

"""
:module: pathtools.path
:synopsis: Directory walking, listing, and path sanitizing functions.
:author: Yesudeep Mangalapilly <yesudeep@gmail.com>

Functions
---------
.. autofunction:: get_dir_walker
.. autofunction:: walk
.. autofunction:: listdir
.. autofunction:: list_directories
.. autofunction:: list_files
.. autofunction:: absolute_path
.. autofunction:: real_absolute_path
.. autofunction:: parent_dir_path
"""

import os.path
import os.path
from functools import partial


__all__ = [
    'get_dir_walker',
    'walk',
    'listdir',
    'list_directories',
    'list_files',
    'absolute_path',
    'real_absolute_path',
    'parent_dir_path',
]


def get_dir_walker(recursive, topdown=True, followlinks=False):
    """
    Returns a recursive or a non-recursive directory walker.

    :param recursive:
        ``True`` produces a recursive walker; ``False`` produces a non-recursive
        walker.
    :returns:
        A walker function.
    """
    if recursive:
        walk = partial(os.walk, topdown=topdown, followlinks=followlinks)
    else:
        def walk(path, topdown=topdown, followlinks=followlinks):
            try:
                yield next(os.walk(path, topdown=topdown, followlinks=followlinks))
            except NameError:
                yield os.walk(path, topdown=topdown, followlinks=followlinks).next() #IGNORE:E1101
    return walk


def walk(dir_pathname, recursive=True, topdown=True, followlinks=False):
    """
    Walks a directory tree optionally recursively. Works exactly like
    :func:`os.walk` only adding the `recursive` argument.

    :param dir_pathname:
        The directory to traverse.
    :param recursive:
        ``True`` for walking recursively through the directory tree;
        ``False`` otherwise.
    :param topdown:
        Please see the documentation for :func:`os.walk`
    :param followlinks:
        Please see the documentation for :func:`os.walk`
    """
    walk_func = get_dir_walker(recursive, topdown, followlinks)
    for root, dirnames, filenames in walk_func(dir_pathname):
        yield (root, dirnames, filenames)


def listdir(dir_pathname,
            recursive=True,
            topdown=True,
            followlinks=False):
    """
    Enlists all items using their absolute paths in a directory, optionally
    recursively.

    :param dir_pathname:
        The directory to traverse.
    :param recursive:
        ``True`` for walking recursively through the directory tree;
        ``False`` otherwise.
    :param topdown:
        Please see the documentation for :func:`os.walk`
    :param followlinks:
        Please see the documentation for :func:`os.walk`
    """
    for root, dirnames, filenames\
    in walk(dir_pathname, recursive, topdown, followlinks):
        for dirname in dirnames:
            yield absolute_path(os.path.join(root, dirname))
        for filename in filenames:
            yield absolute_path(os.path.join(root, filename))


def list_directories(dir_pathname,
                     recursive=True,
                     topdown=True,
                     followlinks=False):
    """
    Enlists all the directories using their absolute paths within the specified
    directory, optionally recursively.

    :param dir_pathname:
        The directory to traverse.
    :param recursive:
        ``True`` for walking recursively through the directory tree;
        ``False`` otherwise.
    :param topdown:
        Please see the documentation for :func:`os.walk`
    :param followlinks:
        Please see the documentation for :func:`os.walk`
    """
    for root, dirnames, filenames\
    in walk(dir_pathname, recursive, topdown, followlinks):
        for dirname in dirnames:
            yield absolute_path(os.path.join(root, dirname))


def list_files(dir_pathname,
               recursive=True,
               topdown=True,
               followlinks=False):
    """
    Enlists all the files using their absolute paths within the specified
    directory, optionally recursively.

    :param dir_pathname:
        The directory to traverse.
    :param recursive:
        ``True`` for walking recursively through the directory tree;
        ``False`` otherwise.
    :param topdown:
        Please see the documentation for :func:`os.walk`
    :param followlinks:
        Please see the documentation for :func:`os.walk`
    """
    for root, dirnames, filenames\
    in walk(dir_pathname, recursive, topdown, followlinks):
        for filename in filenames:
            yield absolute_path(os.path.join(root, filename))


def absolute_path(path):
    """
    Returns the absolute path for the given path and normalizes the path.

    :param path:
        Path for which the absolute normalized path will be found.
    :returns:
        Absolute normalized path.
    """
    return os.path.abspath(os.path.normpath(path))


def real_absolute_path(path):
    """
    Returns the real absolute normalized path for the given path.

    :param path:
        Path for which the real absolute normalized path will be found.
    :returns:
        Real absolute normalized path.
    """
    return os.path.realpath(absolute_path(path))


def parent_dir_path(path):
    """
    Returns the parent directory path.

    :param path:
        Path for which the parent directory will be obtained.
    :returns:
        Parent directory path.
    """
    return absolute_path(os.path.dirname(path))
