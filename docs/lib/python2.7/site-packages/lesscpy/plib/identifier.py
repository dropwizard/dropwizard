# -*- coding: utf8 -*-
"""
.. module:: lesscpy.plib.identifier
    :synopsis: Identifier node.

    Copyright (c)
    See LICENSE for details.
.. moduleauthor:: Johann T. Mariusson <jtm@robot.is>
"""
import re
from .node import Node
from lesscpy.lessc import utility
from lesscpy.lib import reserved


class Identifier(Node):

    """Identifier node. Represents block identifier.
    """

    def parse(self, scope):
        """Parse node. Block identifiers are stored as
        strings with spaces replaced with ?
        args:
            scope (Scope): Current scope
        raises:
            SyntaxError
        returns:
            self
        """
        names = []
        name = []
        self._subp = (
            '@media', '@keyframes',
            '@-moz-keyframes', '@-webkit-keyframes',
            '@-ms-keyframes'
        )
        if self.tokens and hasattr(self.tokens, 'parse'):
            self.tokens = list(utility.flatten([id.split() + [',']
                                                for id in self.tokens.parse(scope).split(',')]))
            self.tokens.pop()
        if self.tokens and any(hasattr(t, 'parse') for t in self.tokens):
            tmp_tokens = []
            for t in self.tokens:
                if hasattr(t, 'parse'):
                    tmp_tokens.append(t.parse(scope))
                else:
                    tmp_tokens.append(t)
            self.tokens = list(utility.flatten(tmp_tokens))
        if self.tokens and self.tokens[0] in self._subp:
            name = list(utility.flatten(self.tokens))
            self.subparse = True
        else:
            self.subparse = False
            for n in utility.flatten(self.tokens):
                if n == '*':
                    name.append('* ')
                elif n in '>+~':
                    if name and name[-1] == ' ':
                        name.pop()
                    name.append('?%s?' % n)
                elif n == ',':
                    names.append(name)
                    name = []
                else:
                    name.append(n)
        names.append(name)
        parsed = self.root(scope, names) if scope else names

        # Interpolated selectors need another step, we have to replace variables. Avoid reserved words though
        #
        # Example:  '.@{var}'       results in [['.', '@{var}']]
        # But:      '@media print'  results in [['@media', ' ', 'print']]
        #
        def replace_variables(tokens, scope):
            return [scope.swap(t)
                    if (utility.is_variable(t) and not t in reserved.tokens)
                    else t
                    for t in tokens]
        parsed = [list(utility.flatten(replace_variables(part, scope))) for part in parsed]

        self.parsed = [[i for i, j in utility.pairwise(part)
                        if i != ' ' or (j and '?' not in j)]
                       for part in parsed]
        return self

    def root(self, scope, names):
        """Find root of identifier, from scope
        args:
            scope (Scope): current scope
            names (list): identifier name list (, separated identifiers)
        returns:
            list
        """
        parent = scope.scopename
        if parent:
            parent = parent[-1]
            if parent.parsed:
                parsed_names = []
                for name in names:
                    ampersand_count = name.count('&')
                    if ampersand_count:
                        filtered_parts = []
                        for part in parent.parsed:
                            if part and part[0] not in self._subp:
                                filtered_parts.append(part)
                        permutations = list(utility.permutations_with_replacement(filtered_parts, ampersand_count))
                        for permutation in permutations:
                            parsed = []
                            for name_part in name:
                                if name_part == "&":
                                    parent_part = permutation.pop(0)
                                    if parsed and parsed[-1].endswith(']'):
                                        parsed.extend(' ')
                                    if parent_part[-1] == ' ':
                                        parent_part.pop()
                                    parsed.extend(parent_part)
                                else:
                                    parsed.append(name_part)
                            parsed_names.append(parsed)
                    else:
                        # NOTE(saschpe): Maybe this code can be expressed with permutations too?
                        for part in parent.parsed:
                            if part and part[0] not in self._subp:
                                parsed = []
                                if name[0] == "@media":
                                    parsed.extend(name)
                                else:
                                    parsed.extend(part)
                                    if part[-1] != ' ':
                                        parsed.append(' ')
                                    parsed.extend(name)
                                parsed_names.append(parsed)
                            else:
                                parsed_names.append(name)
                return parsed_names
        return names

    def raw(self, clean=False):
        """Raw identifier.
        args:
            clean (bool): clean name
        returns:
            str
        """
        if clean:
            return ''.join(''.join(p) for p in self.parsed).replace('?', ' ')
        return '%'.join('%'.join(p) for p in self.parsed).strip().strip('%')

    def copy(self):
        """ Return copy of self
        Returns:
            Identifier object
        """
        tokens = ([t for t in self.tokens]
                  if isinstance(self.tokens, list)
                  else self.tokens)
        return Identifier(tokens, 0)

    def fmt(self, fills):
        """Format identifier
        args:
            fills (dict): replacements
        returns:
            str (CSS)
        """
        name = ',$$'.join(''.join(p).strip()
                          for p in self.parsed)
        name = re.sub('\?(.)\?', '%(ws)s\\1%(ws)s', name) % fills
        return name.replace('$$', fills['nl']).replace('  ', ' ')
