# -*- coding: utf8 -*-
"""
.. module:: lesscpy.plib.mixin
    :synopsis: Mixin node.

    Copyright (c)
    See LICENSE for details.
.. moduleauthor:: Johann T. Mariusson <jtm@robot.is>
"""
import sys
import copy
import itertools
from .node import Node
from .block import Block
from .expression import Expression
from .variable import Variable
from lesscpy.lessc import utility


class Mixin(Node):

    """ Mixin Node. Represents callable mixin types.
    """

    def parse(self, scope):
        """Parse node
        args:
            scope (Scope): current scope
        raises:
            SyntaxError
        returns:
            self
        """
        self.name, args, self.guards = self.tokens[0]
        self.args = [a for a in utility.flatten(args) if a]
        self.body = Block([None, self.tokens[1]], 0)
        self.vars = list(utility.flatten([list(v.values())
                                          for v in [s['__variables__']
                                                    for s in scope]]))
        return self

    def raw(self):
        """Raw mixin name
        returns:
            str
        """
        return self.name.raw()

    def parse_args(self, args, scope):
        """Parse arguments to mixin. Add them to scope
        as variables. Sets upp special variable @arguments
        as well.
        args:
            args (list): arguments
            scope (Scope): current scope
        raises:
            SyntaxError
        """
        arguments = list(zip(args, [' '] * len(args))) if args and args[0] else None
        zl = itertools.zip_longest if sys.version_info[
            0] == 3 else itertools.izip_longest
        if self.args:
            parsed = [v if hasattr(v, 'parse') else v
                      for v in copy.copy(self.args)]
            args = args if isinstance(args, list) else [args]
            vars = [self._parse_arg(var, arg, scope)
                    for arg, var in zl([a for a in args], parsed)]
            for var in vars:
                if var:
                    var.parse(scope)
            if not arguments:
                arguments = [v.value for v in vars if v]
        if not arguments:
            arguments = ''
        Variable(['@arguments', None, arguments]).parse(scope)

    def _parse_arg(self, var, arg, scope):
        """ Parse a single argument to mixin.
        args:
            var (Variable object): variable
            arg (mixed): argument
            scope (Scope object): current scope
        returns:
            Variable object or None
        """
        if isinstance(var, Variable):
            # kwarg
            if arg:
                if utility.is_variable(arg[0]):
                    tmp = scope.variables(arg[0])
                    if not tmp:
                        return None
                    val = tmp.value
                else:
                    val = arg
                var = Variable(var.tokens[:-1] + [val])
        else:
            # arg
            if utility.is_variable(var):
                if arg is None:
                    raise SyntaxError('Missing argument to mixin')
                elif utility.is_variable(arg[0]):
                    tmp = scope.variables(arg[0])
                    if not tmp:
                        return None
                    val = tmp.value
                else:
                    val = arg
                var = Variable([var, None, val])
            else:
                return None
        return var

    def parse_guards(self, scope):
        """Parse guards on mixin.
        args:
            scope (Scope): current scope
        raises:
            SyntaxError
        returns:
            bool (passes guards)
        """
        if self.guards:
            cor = True if ',' in self.guards else False
            for g in self.guards:
                if isinstance(g, list):
                    res = (g[0].parse(scope)
                           if len(g) == 1
                           else Expression(g).parse(scope))
                    if cor:
                        if res:
                            return True
                    elif not res:
                        return False
        return True

    def call(self, scope, args=[]):
        """Call mixin. Parses a copy of the mixins body
        in the current scope and returns it.
        args:
            scope (Scope): current scope
            args (list): arguments
        raises:
            SyntaxError
        returns:
            list or False
        """
        ret = False
        if args:
            args = [[a.parse(scope)
                    if isinstance(a, Expression)
                    else a for a in arg]
                    if arg else arg
                    for arg in args]
        try:
            self.parse_args(args, scope)
        except SyntaxError:
            pass
        else:
            if self.parse_guards(scope):
                body = self.body.copy()
                ret = body.tokens[1]
                if ret:
                    utility.rename(ret, scope, Block)
        return ret
