# -*- coding: utf8 -*-
"""
.. module:: lesscpy.plib.block
    :synopsis: Block parse node.

    Copyright (c)
    See LICENSE for details.
.. moduleauthor:: Johann T. Mariusson <jtm@robot.is>
"""
from .node import Node
from lesscpy.lessc import utility
from lesscpy.plib.identifier import Identifier


class Block(Node):

    """ Block node. Represents one parse-block.
    Can contain property nodes or other block nodes.
    identifier {
        propertys
        inner blocks
    }
    """

    def parse(self, scope):
        """Parse block node.
        args:
            scope (Scope): Current scope
        raises:
            SyntaxError
        returns:
            self
        """
        if not self.parsed:
            scope.push()
            self.name, inner = self.tokens
            scope.current = self.name
            scope.real.append(self.name)
            if not self.name.parsed:
                self.name.parse(scope)
            if not inner:
                inner = []
            inner = list(utility.flatten([p.parse(scope) for p in inner if p]))
            self.parsed = []
            self.inner = []
            if not hasattr(self, "inner_media_queries"):
                self.inner_media_queries = []
            for p in inner:
                if p is not None:
                    if isinstance(p, Block):
                        if (len(scope) == 2 and p.tokens[1] is not None):
                            p_is_mediaquery = p.name.tokens[0] == '@media'
                            # Inner block @media ... { ... } is a nested media
                            # query. But double-nested media queries have to be
                            # removed and marked as well. While parsing ".foo",
                            # both nested "@media print" and double-nested
                            # "@media all" will be handled as we have to
                            # re-arrange the scope and block layout quite a bit:
                            #
                            #   .foo {
                            #       @media print {
                            #           color: blue;
                            #           @media screen { font-size: 12em; }
                            #       }
                            #   }
                            #
                            # Expected result:
                            #
                            #   @media print {
                            #       .foo { color: blue; }
                            #   }
                            #   @media print and screen {
                            #       .foo { font-size: 12 em; }
                            #   }
                            append_list = []
                            reparse_p = False
                            for child in p.tokens[1]:
                                if isinstance(child, Block) and child.name.raw().startswith("@media"):
                                    # Remove child from the nested media query, it will be re-added to
                                    # the parent with 'merged' media query (see above example).
                                    p.tokens[1].remove(child)
                                    if p_is_mediaquery:  # Media query inside a & block
                                        # Double-nested media query found. We remove it from 'p' and add
                                        # it to this block with a new 'name'.
                                        reparse_p = True
                                        part_a = p.name.tokens[2:][0][0][0]
                                        part_b = child.name.tokens[2:][0][0]
                                        new_ident_tokens = ['@media', ' ', [part_a, (' ', 'and', ' '), part_b]]
                                        # Parse child again with new @media $BLA {} part
                                        child.tokens[0] = Identifier(new_ident_tokens)
                                        child.parsed = None
                                        child = child.parse(scope)
                                    else:
                                        child.block_name = p.name
                                    append_list.append(child)
                                if reparse_p:
                                    p.parsed = None
                                    p = p.parse(scope)
                            if not p_is_mediaquery and not append_list:
                                self.inner.append(p)
                            else:
                                append_list.insert(0, p)  # This media query should occur before it's children
                                for media_query in append_list:
                                    self.inner_media_queries.append(media_query)
                            # NOTE(saschpe): The code is not recursive but we hope that people
                            # wont use triple-nested media queries.
                        else:
                            self.inner.append(p)
                    else:
                        self.parsed.append(p)
            if self.inner_media_queries:
                # Nested media queries, we have to remove self from scope and
                # push all nested @media ... {} blocks.
                scope.remove_block(self, index=-2)
                for mb in self.inner_media_queries:
                    # New inner block with current name and media block contents
                    if hasattr(mb, 'block_name'):
                        cb_name = mb.block_name
                    else:
                        cb_name = self.tokens[0]
                    cb = Block([cb_name, mb.tokens[1]]).parse(scope)
                    # Replace inner block contents with new block
                    new_mb = Block([mb.tokens[0], [cb]]).parse(scope)
                    self.inner.append(new_mb)
                    scope.add_block(new_mb)
            scope.real.pop()
            scope.pop()
        return self

    def raw(self, clean=False):
        """Raw block name
        args:
            clean (bool): clean name
        returns:
            str
        """
        try:
            return self.tokens[0].raw(clean)
        except (AttributeError, TypeError):
            pass

    def fmt(self, fills):
        """Format block (CSS)
        args:
            fills (dict): Fill elements
        returns:
            str (CSS)
        """
        f = "%(identifier)s%(ws)s{%(nl)s%(proplist)s}%(eb)s"
        out = []
        name = self.name.fmt(fills)
        if self.parsed and any(p for p in self.parsed if str(type(p)) != "<class 'lesscpy.plib.variable.Variable'>"):
            fills.update({
                'identifier': name,
                'proplist': ''.join([p.fmt(fills) for p in self.parsed if p]),
            })
            out.append(f % fills)
        if hasattr(self, 'inner'):
            if self.name.subparse and len(self.inner) > 0:  # @media
                inner = ''.join([p.fmt(fills) for p in self.inner])
                inner = inner.replace(fills['nl'],
                                      fills['nl'] + fills['tab']).rstrip(fills['tab'])
                if not fills['nl']:
                    inner = inner.strip()
                fills.update({
                    'identifier': name,
                    'proplist': fills['tab'] + inner
                })
                out.append(f % fills)
            else:
                out.append(''.join([p.fmt(fills) for p in self.inner]))
        return ''.join(out)

    def copy(self):
        """ Return a full copy of self
        returns: Block object
        """
        name, inner = self.tokens
        if inner:
            inner = [u.copy() if u else u
                     for u in inner]
        if name:
            name = name.copy()
        return Block([name, inner], 0)

    def copy_inner(self, scope):
        """Copy block contents (properties, inner blocks).
        Renames inner block from current scope.
        Used for mixins.
        args:
            scope (Scope): Current scope
        returns:
            list (block contents)
        """
        if self.tokens[1]:
            tokens = [u.copy() if u else u
                      for u in self.tokens[1]]
            out = [p for p in tokens if p]
            utility.rename(out, scope, Block)
            return out
        return None
