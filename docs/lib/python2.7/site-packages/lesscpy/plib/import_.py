# -*- coding: utf8 -*-
"""
.. module:: lesscpy.plib.property
    :synopsis: Import node.

    Copyright (c)
    See LICENSE for details.
.. moduleauthor:: Johann T. Mariusson <jtm@robot.is>
"""
from .node import Node


class Import(Node):

    """Represents CSS property declaration.
    """

    def parse(self, scope):
        """Parse node
        args:
            scope (Scope): current scope
        raises:
            SyntaxError
        returns:
            parsed
        """
        if not self.parsed:
            self.parsed = ''.join(self.process(self.tokens, scope))
        return self.parsed

    def fmt(self, fills):
        return ''

    def copy(self):
        """ Return a full copy of self
        Returns:
            Import object
        """
        return Import([t for t in self.tokens], 0)
