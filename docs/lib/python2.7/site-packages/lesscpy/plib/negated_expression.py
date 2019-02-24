# -*- coding: utf8 -*-
"""
.. module:: lesscpy.plib.negated_expression
    :synopsis: Node for unary negated expressions.

    Copyright (c)
    See LICENSE for details.
"""

import six

from .node import Node


class NegatedExpression(Node):

    """Expressions preceded by unary negation."""

    def parse(self, scope):
        val, = self.process(self.tokens, scope)
        if isinstance(val, six.string_types):
            return '-' + val
        return -val
