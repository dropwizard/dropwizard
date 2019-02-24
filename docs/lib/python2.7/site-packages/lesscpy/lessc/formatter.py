# -*- coding: utf8 -*-
"""
.. module:: lesscpy.lessc.formatter
    :synopsis: CSS Formatter class.

    Copyright (c)
    See LICENSE for details.
.. moduleauthor:: Johann T. Mariusson <jtm@robot.is>
"""


class Formatter(object):

    def __init__(self, args):
        self.args = args

    def format(self, parse):
        """
        """
        if not parse.result:
            return ''
        eb = '\n'
        if self.args.xminify:
            eb = ''
            self.args.minify = True
        self.items = {}
        if self.args.minify:
            self.items.update({
                'nl': '',
                'tab': '',
                'ws': '',
                'eb': eb
            })
        else:
            tab = '\t' if self.args.tabs else ' ' * int(self.args.spaces)
            self.items.update({
                'nl': '\n',
                'tab': tab,
                'ws': ' ',
                'eb': eb
            })
        self.out = [u.fmt(self.items)
                    for u in parse.result
                    if u]
        return ''.join(self.out).strip()
