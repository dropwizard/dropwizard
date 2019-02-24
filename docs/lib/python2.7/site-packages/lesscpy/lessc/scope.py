"""
.. module:: lesscpy.lessc.scope
    :synopsis: Scope class.


    Copyright (c)
    See LICENSE for details.
.. moduleauthor:: Johann T. Mariusson <jtm@robot.is>
"""
import six

from . import utility


class Scope(list):

    """ Scope class. A stack implementation.
    """

    def __init__(self, init=False):
        """Scope
        Args:
            init (bool): Initiate scope
        """
        super(Scope, self).__init__()
        self._mixins = {}
        if init:
            self.push()
        self.deferred = False
        self.real = []

    def push(self):
        """Push level on scope
        """
        self.append({
            '__variables__': {},
            '__blocks__': [],
            '__names__': [],
            '__current__': None
        })

    @property
    def current(self):
        return self[-1]['__current__']

    @current.setter
    def current(self, value):
        self[-1]['__current__'] = value

    @property
    def scopename(self):
        """Current scope name as list
        Returns:
            list
        """
        return [r['__current__']
                for r in self
                if r['__current__']]

    def add_block(self, block):
        """Add block element to scope
        Args:
            block (Block): Block object
        """
        self[-1]['__blocks__'].append(block)
        self[-1]['__names__'].append(block.raw())

    def remove_block(self, block, index="-1"):
        """Remove block element from scope
        Args:
            block (Block): Block object
        """
        self[index]["__blocks__"].remove(block)
        self[index]["__names__"].remove(block.raw())

    def add_mixin(self, mixin):
        """Add mixin to scope
        Args:
            mixin (Mixin): Mixin object
        """
        raw = mixin.tokens[0][0].raw()
        if raw in self._mixins:
            self._mixins[raw].append(mixin)
        else:
            self._mixins[raw] = [mixin]

    def add_variable(self, variable):
        """Add variable to scope
        Args:
            variable (Variable): Variable object
        """
        self[-1]['__variables__'][variable.name] = variable

    def variables(self, name):
        """Search for variable by name. Searches scope top down
        Args:
            name (string): Search term
        Returns:
            Variable object OR False
        """
        if isinstance(name, tuple):
            name = name[0]
        if name.startswith('@{'):
            name = '@' + name[2:-1]
        i = len(self)
        while i >= 0:
            i -= 1
            if name in self[i]['__variables__']:
                return self[i]['__variables__'][name]
        return False

    def mixins(self, name):
        """ Search mixins for name.
        Allow '>' to be ignored. '.a .b()' == '.a > .b()'
        Args:
            name (string): Search term
        Returns:
            Mixin object list OR False
        """
        m = self._smixins(name)
        if m:
            return m
        return self._smixins(name.replace('?>?', ' '))

    def _smixins(self, name):
        """Inner wrapper to search for mixins by name.
        """
        return (self._mixins[name]
                if name in self._mixins
                else False)

    def blocks(self, name):
        """
        Search for defined blocks recursively.
        Allow '>' to be ignored. '.a .b' == '.a > .b'
        Args:
            name (string): Search term
        Returns:
            Block object OR False
        """
        b = self._blocks(name)
        if b:
            return b
        return self._blocks(name.replace('?>?', ' '))

    def _blocks(self, name):
        """Inner wrapper to search for blocks by name.
        """
        i = len(self)
        while i >= 0:
            i -= 1
            if name in self[i]['__names__']:
                for b in self[i]['__blocks__']:
                    r = b.raw()
                    if r and r == name:
                        return b
            else:
                for b in self[i]['__blocks__']:
                    r = b.raw()
                    if r and name.startswith(r):
                        b = utility.blocksearch(b, name)
                        if b:
                            return b
        return False

    def update(self, scope, at=0):
        """Update scope. Add another scope to this one.
        Args:
            scope (Scope): Scope object
        Kwargs:
            at (int): Level to update
        """
        if hasattr(scope, '_mixins') and not at:
            self._mixins.update(scope._mixins)
        self[at]['__variables__'].update(scope[at]['__variables__'])
        self[at]['__blocks__'].extend(scope[at]['__blocks__'])
        self[at]['__names__'].extend(scope[at]['__names__'])

    def swap(self, name):
        """ Swap variable name for variable value
        Args:
            name (str): Variable name
        Returns:
            Variable value (Mixed)
        """
        if name.startswith('@@'):
            var = self.variables(name[1:])
            if var is False:
                raise SyntaxError('Unknown variable %s' % name)
            name = '@' + utility.destring(var.value[0])
            var = self.variables(name)
            if var is False:
                raise SyntaxError('Unknown variable %s' % name)
        elif name.startswith('@{'):
            var = self.variables('@' + name[2:-1])
            if var is False:
                raise SyntaxError('Unknown escaped variable %s' % name)
            if isinstance(var.value[0], six.string_types):
                var.value[0] = utility.destring(var.value[0])
        else:
            var = self.variables(name)
            if var is False:
                raise SyntaxError('Unknown variable %s' % name)
        return var.value
