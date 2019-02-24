# -*- coding: utf8 -*-
"""
.. module:: lesscpy.lessc.utility
    :synopsis: various utility functions

    Copyright (c)
    See LICENSE for details.
.. moduleauthor:: Johann T. Mariusson <jtm@robot.is>
"""

from __future__ import print_function

import collections
import itertools
import math
import re
import sys
from six import string_types

def flatten(lst):
    """Flatten list.
    Args:
        lst (list): List to flatten
    Returns:
        generator
    """
    for elm in lst:
        if isinstance(elm, collections.Iterable) and not isinstance(elm, string_types):
            for sub in flatten(elm):
                yield sub
        else:
            yield elm


def pairwise(lst):
    """ yield item i and item i+1 in lst. e.g.
        (lst[0], lst[1]), (lst[1], lst[2]), ..., (lst[-1], None)
    Args:
        lst (list): List to process
    Returns:
        list
    """
    if not lst:
        return
    length = len(lst)
    for i in range(length - 1):
        yield lst[i], lst[i + 1]
    yield lst[-1], None


def rename(blocks, scope, stype):
    """ Rename all sub-blocks moved under another
        block. (mixins)
    Args:
        lst (list): block list
        scope (object): Scope object
    """
    for p in blocks:
        if isinstance(p, stype):
            p.tokens[0].parse(scope)
            if p.tokens[1]:
                scope.push()
                scope.current = p.tokens[0]
                rename(p.tokens[1], scope, stype)
                scope.pop()


def blocksearch(block, name):
    """ Recursive search for name in block (inner blocks)
    Args:
        name (str): search term
    Returns:
        Block OR False
    """
    if hasattr(block, 'tokens'):
        for b in block.tokens[1]:
            b = (b if hasattr(b, 'raw') and b.raw() == name
                 else blocksearch(b, name))
            if b:
                return b
    return False


def reverse_guard(lst):
    """ Reverse guard expression. not
        (@a > 5) ->  (@a =< 5)
    Args:
        lst (list): Expression
    returns:
        list
    """
    rev = {
        '<': '>=',
        '>': '=<',
        '>=': '<',
        '=<': '>'
    }
    return [rev[l] if l in rev else l for l in lst]


def debug_print(lst, lvl=0):
    """ Print scope tree
    args:
        lst (list): parse result
        lvl (int): current nesting level
    """
    pad = ''.join(['\t.'] * lvl)
    t = type(lst)
    if t is list:
        for p in lst:
            debug_print(p, lvl)
    elif hasattr(lst, 'tokens'):
        print(pad, t)
        debug_print(list(flatten(lst.tokens)), lvl + 1)


def destring(value):
    """ Strip quotes from string
    args:
        value (str)
    returns:
        str
    """
    return value.strip('"\'')


def analyze_number(var, err=''):
    """ Analyse number for type and split from unit
        1px -> (q, 'px')
    args:
        var (str): number string
    kwargs:
        err (str): Error message
    raises:
        SyntaxError
    returns:
        tuple
    """
    n, u = split_unit(var)
    if not isinstance(var, string_types):
        return (var, u)
    if is_color(var):
        return (var, 'color')
    if is_int(n):
        n = int(n)
    elif is_float(n):
        n = float(n)
    else:
        raise SyntaxError('%s ´%s´' % (err, var))
    return (n, u)


def with_unit(number, unit=None):
    """ Return number with unit
    args:
        number (mixed): Number
        unit (str): Unit
    returns:
        str
    """
    if isinstance(number, tuple):
        number, unit = number
    if number == 0:
        return '0'
    if unit:
        number = str(number)
        if number.startswith('.'):
            number = '0' + number
        return "%s%s" % (number, unit)
    return number if isinstance(number, string_types) else str(number)


def is_color(value):
    """ Is string CSS color
    args:
        value (str): string
    returns:
        bool
    """
    if not value or not isinstance(value, string_types):
        return False
    if value[0] == '#' and len(value) in [4, 5, 7, 9]:
        try:
            int(value[1:], 16)
            return True
        except ValueError:
            pass
    return False


def is_variable(value):
    """ Check if string is LESS variable
    args:
        value (str): string
    returns:
        bool
    """
    if isinstance(value, string_types):
        return (value.startswith('@') or value.startswith('-@'))
    elif isinstance(value, tuple):
        value = ''.join(value)
        return (value.startswith('@') or value.startswith('-@'))
    return False


def is_int(value):
    """ Is value integer
    args:
        value (str): string
    returns:
        bool
    """
    try:
        int(str(value))
        return True
    except (ValueError, TypeError):
        pass
    return False


def is_float(value):
    """ Is value float
    args:
        value (str): string
    returns:
        bool
    """
    if not is_int(value):
        try:
            float(str(value))
            return True
        except (ValueError, TypeError):
            pass
    return False


def split_unit(value):
    """ Split a number from its unit
        1px -> (q, 'px')
    Args:
        value (str): input
    returns:
        tuple
    """
    r = re.search('^(\-?[\d\.]+)(.*)$', str(value))
    return r.groups() if r else ('', '')


def away_from_zero_round(value, ndigits=0):
    """Round half-way away from zero.

    Python2's round() method.
    """
    if sys.version_info[0] >= 3:
        p = 10 ** ndigits
        return float(math.floor((value * p) + math.copysign(0.5, value))) / p
    else:
        return round(value, ndigits)


def convergent_round(value, ndigits=0):
    """Convergent rounding.

    Round to neareas even, similar to Python3's round() method.
    """
    if sys.version_info[0] < 3:
        if value < 0.0:
            return -convergent_round(-value)

        epsilon = 0.0000001
        integral_part, _ = divmod(value, 1)

        if abs(value - (integral_part + 0.5)) < epsilon:
            if integral_part % 2.0 < epsilon:
                return integral_part
            else:
                nearest_even = integral_part + 0.5
                return math.ceil(nearest_even)
    return round(value, ndigits)

def pc_or_float(s):
    """ Utility function to process strings that contain either percentiles or floats
    args:
        str: s
    returns:
       float
    """
    if isinstance(s, string_types) and '%' in s:
        return float(s.strip('%')) / 100.0
    return float(s)

def permutations_with_replacement(iterable, r=None):
    """Return successive r length permutations of elements in the iterable.

    Similar to itertools.permutation but withouth repeated values filtering.
    """
    pool = tuple(iterable)
    n = len(pool)
    r = n if r is None else r
    for indices in itertools.product(range(n), repeat=r):
        yield list(pool[i] for i in indices)
