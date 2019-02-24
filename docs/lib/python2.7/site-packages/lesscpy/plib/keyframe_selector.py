# -*- coding: utf8 -*-
"""
.. module:: lesscpy.plib.keyframe_selector
    :synopsis: Keyframe selector node.

    Copyright (c)
    See LICENSE for details.
"""

from .node import Node


class KeyframeSelector(Node):
    """Keyframe selector node. Represents the keyframe selector in an animation
    sequence. Keyframes can be identified by the keywords "from" or "to", or by
    percentage.

    http://www.w3.org/TR/css3-animations/#keyframes
    """

    def parse(self, scope):
        """Parse node.
        args:
            scope (Scope): Current scope
        raises:
            SyntaxError
        returns:
            self
        """
        self.keyframe, = [e[0] if isinstance(e, tuple) else e
                          for e in self.tokens if str(e).strip()]
        self.subparse = False
        return self

    def copy(self):
        """ Return copy of self
        Returns:
            KeyframeSelector object
        """
        return KeyframeSelector(self.tokens, 0)

    def fmt(self, fills):
        """Format identifier
        args:
            fills (dict): replacements
        returns:
            str (CSS)
        """
        return self.keyframe
