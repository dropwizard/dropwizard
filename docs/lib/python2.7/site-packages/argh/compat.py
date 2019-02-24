# originally inspired by "six" by Benjamin Peterson

import inspect
import sys


if sys.version_info < (3,0):
    text_type = unicode
    binary_type = str

    import StringIO
    StringIO = BytesIO = StringIO.StringIO
else:
    text_type = str
    binary_type = bytes

    import io
    StringIO = io.StringIO
    BytesIO = io.BytesIO


def getargspec_permissive(func):
    """
    An `inspect.getargspec` with a relaxed sanity check to support Cython.

    Motivation:

        A Cython-compiled function is *not* an instance of Python's
        types.FunctionType.  That is the sanity check the standard Py2
        library uses in `inspect.getargspec()`.  So, an exception is raised
        when calling `argh.dispatch_command(cythonCompiledFunc)`.  However,
        the CyFunctions do have perfectly usable `.func_code` and
        `.func_defaults` which is all `inspect.getargspec` needs.

        This function just copies `inspect.getargspec()` from the standard
        library but relaxes the test to a more duck-typing one of having
        both `.func_code` and `.func_defaults` attributes.
    """
    if inspect.ismethod(func):
        func = func.im_func

    # Py2 Stdlib uses isfunction(func) which is too strict for Cython-compiled
    # functions though such have perfectly usable func_code, func_defaults.
    if not (hasattr(func, "func_code") and hasattr(func, "func_defaults")):
        raise TypeError('{!r} missing func_code or func_defaults'.format(func))

    args, varargs, varkw = inspect.getargs(func.func_code)
    return inspect.ArgSpec(args, varargs, varkw, func.func_defaults)


if sys.version_info < (3,0):
    getargspec = getargspec_permissive
else:
    # in Python 3 the basic getargspec doesn't support keyword-only arguments
    # and annotations and raises ValueError if they are discovered
    getargspec = inspect.getfullargspec


class _PrimitiveOrderedDict(dict):
    """
    A poor man's OrderedDict replacement for compatibility with Python 2.6.
    Implements only the basic features.  May easily break if non-overloaded
    methods are used.
    """
    def __init__(self, *args, **kwargs):
        super(_PrimitiveOrderedDict, self).__init__(*args, **kwargs)
        self._seq = []

    def __setitem__(self, key, value):
        super(_PrimitiveOrderedDict, self).__setitem__(key, value)
        if key not in self._seq:
            self._seq.append(key)

    def __delitem__(self, key):
        super(_PrimitiveOrderedDict, self).__delitem__(key)
        idx = self._seq.index(key)
        del self._seq[idx]

    def __iter__(self):
        return iter(self._seq)

    def keys(self):
        return list(self)

    def values(self):
        return [self[k] for k in self]


try:
    from collections import OrderedDict
except ImportError:
    OrderedDict = _PrimitiveOrderedDict
