__version_info__ = ('0', '13', '0')
__version__ = '.'.join(__version_info__)


def compile(file, minify=False, xminify=False, tabs=False, spaces=True):
    from .lessc import parser
    from .lessc import formatter

    class Opt(object):
        def __init__(self):
            self.minify = minify
            self.xminify = xminify
            self.tabs = tabs
            self.spaces = spaces

    p = parser.LessParser(fail_with_exc=True)
    opt = Opt()
    p.parse(file=file)
    f = formatter.Formatter(opt)
    return f.format(p)

