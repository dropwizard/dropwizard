# -*- coding: utf8 -*-
"""
.. module:: lesscpy.plib.statement
    :synopsis: Statement node.

    Copyright (c)
    See LICENSE for details.
.. moduleauthor:: Johann T. Mariusson <jtm@robot.is>
"""
from .node import Node
from lesscpy.lessc import utility


class Statement(Node):

    """Represents CSS statement (@import, @charset...)
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
        self.parsed = list(utility.flatten(self.tokens))
        if self.parsed[0] == '@import':
            if len(self.parsed) > 4:
                # Media @import
                self.parsed.insert(3, ' ')
        return self

    def fmt(self, fills):
        """ Format node
        args:
            fills (dict): replacements
        returns:
            str
        """
        return ''.join(self.parsed) + fills['eb']
