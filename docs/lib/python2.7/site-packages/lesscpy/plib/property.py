# -*- coding: utf8 -*-
"""
.. module:: lesscpy.plib.property
    :synopsis: Property node.

    Copyright (c)
    See LICENSE for details.
.. moduleauthor:: Johann T. Mariusson <jtm@robot.is>
"""
import re
from .node import Node


class Property(Node):

    """Represents CSS property declaration.
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
        if not self.parsed:
            if len(self.tokens) > 2:
                property, style, _ = self.tokens
                self.important = True
            else:
                property, style = self.tokens
                self.important = False
            self.property = ''.join(property)
            self.parsed = []
            if style:
                style = self.preprocess(style)
                self.parsed = self.process(style, scope)
        return self

    def preprocess(self, style):
        """Hackish preprocessing from font shorthand tags.
        Skips expression parse on certain tags.
        args:
            style (list): .
        returns:
            list
        """
        if self.property == 'font':
            style = [''.join(u.expression())
                     if hasattr(u, 'expression')
                     else u
                     for u in style]
        else:
            style = [(u, ' ')
                     if hasattr(u, 'expression')
                     else u
                     for u in style]
        return style

    def fmt(self, fills):
        """ Format node
        args:
            fills (dict): replacements
        returns:
            str
        """
        f = "%(tab)s%(property)s:%(ws)s%(style)s%(important)s;%(nl)s"
        imp = ' !important' if self.important else ''
        if fills['nl']:
            self.parsed = [',%s' % fills['ws']
                           if p == ','
                           else p
                           for p in self.parsed]
        style = ''.join([p.fmt(fills)
                         if hasattr(p, 'fmt')
                         else str(p)
                         for p in self.parsed])
        # IE cannot handle no space after url()
        style = re.sub("(url\([^\)]*\))([^\s,])", "\\1 \\2", style)
        fills.update({
            'property': self.property,
            'style': style.strip(),
            'important': imp
        })
        return f % fills

    def copy(self):
        """ Return a full copy of self
        Returns:
            Property object
        """
        return Property([t for t in self.tokens], 0)
