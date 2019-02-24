# -*- coding: utf8 -*-
"""
.. module:: lesscpy.plib.expression
    :synopsis: Expression node.

    Copyright (c)
    See LICENSE for details.
.. moduleauthor:: Johann T. Mariusson <jtm@robot.is>
"""

import operator

from .node import Node
from lesscpy.lessc import utility
from lesscpy.lessc import color


class Expression(Node):

    """Expression node. Parses all expression except
    color expressions (handled in the color class)
    and unary negation (handled in the NegatedExpression class).
    """

    def parse(self, scope):
        """ Parse Node
        args:
            scope (Scope): Scope object
        raises:
            SyntaxError
        returns:
            str
        """
        assert(len(self.tokens) == 3)
        expr = self.process(self.tokens, scope)
        A, O, B = [e[0]
                   if isinstance(e, tuple)
                   else e
                   for e in expr
                   if str(e).strip()]
        try:
            a, ua = utility.analyze_number(A, 'Illegal element in expression')
            b, ub = utility.analyze_number(B, 'Illegal element in expression')
        except SyntaxError:
            return ' '.join([str(A), str(O), str(B)])
        if(a is False or b is False):
            return ' '.join([str(A), str(O), str(B)])
        if ua == 'color' or ub == 'color':
            return color.Color().process((A, O, B))
        if a == 0 and O == '/':
            # NOTE(saschpe): The ugliest but valid CSS since sliced bread: 'font: 0/1 a;'
            return ''.join([str(A), str(O), str(B), ' '])
        out = self.operate(a, b, O)
        if isinstance(out, bool):
            return out
        return self.with_units(out, ua, ub)

    def with_units(self, val, ua, ub):
        """Return value with unit.
        args:
            val (mixed): result
            ua (str): 1st unit
            ub (str): 2nd unit
        raises:
            SyntaxError
        returns:
            str
        """
        if not val:
            return str(val)
        if ua or ub:
            if ua and ub:
                if ua == ub:
                    return str(val) + ua
                else:
                    # Nodejs version does not seem to mind mismatched
                    # units within expressions. So we choose the first
                    # as they do
                    # raise SyntaxError("Error in expression %s != %s" % (ua, ub))
                    return str(val) + ua
            elif ua:
                return str(val) + ua
            elif ub:
                return str(val) + ub
        return repr(val)

    def operate(self, vala, valb, oper):
        """Perform operation
        args:
            vala (mixed): 1st value
            valb (mixed): 2nd value
            oper (str): operation
        returns:
            mixed
        """
        operation = {
            '+': operator.add,
            '-': operator.sub,
            '*': operator.mul,
            '/': operator.truediv,
            '=': operator.eq,
            '>': operator.gt,
            '<': operator.lt,
            '>=': operator.ge,
            '=<': operator.le,
        }.get(oper)
        if operation is None:
            raise SyntaxError("Unknown operation %s" % oper)
        ret = operation(vala, valb)
        if oper in '+-*/' and int(ret) == ret:
            ret = int(ret)
        return ret

    def expression(self):
        """Return str representation of expression
        returns:
            str
        """
        return utility.flatten(self.tokens)
