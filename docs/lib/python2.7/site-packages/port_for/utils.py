# -*- coding: utf-8 -*-
from __future__ import absolute_import
import itertools

def ranges_to_set(lst):
    """
    Converts a list of ranges to a set of numbers::

    >>> ranges = [(1,3), (5,6)]
    >>> sorted(list(ranges_to_set(ranges)))
    [1, 2, 3, 5, 6]

    """
    return set(itertools.chain(*(range(x[0], x[1]+1) for x in lst)))

def to_ranges(lst):
    """
    Converts a list of numbers to a list of ranges::

    >>> numbers = [1,2,3,5,6]
    >>> list(to_ranges(numbers))
    [(1, 3), (5, 6)]

    """
    for a, b in itertools.groupby(enumerate(lst), lambda t: t[1] - t[0]):
        b = list(b)
        yield b[0][1], b[-1][1]

