from copy import copy
import sys
import re


class DocoptLanguageError(Exception):

    """Error in construction of usage-message by developer."""


class DocoptExit(SystemExit):

    """Exit in case user invoked program with incorrect arguments."""

    usage = ''

    def __init__(self, message=''):
        SystemExit.__init__(self, (message + '\n' + self.usage).strip())


class Pattern(object):

    def __init__(self, *children):
        self.children = list(children)

    def __eq__(self, other):
        return repr(self) == repr(other)

    def __hash__(self):
        return hash(repr(self))

    def __repr__(self):
        return '%s(%s)' % (self.__class__.__name__,
                           ', '.join(repr(a) for a in self.children))

    @property
    def flat(self):
        if not hasattr(self, 'children'):
            return [self]
        return sum([c.flat for c in self.children], [])

    def fix(self):
        self.fix_identities()
        self.fix_list_arguments()
        return self

    def fix_identities(self, uniq=None):
        """Make pattern-tree tips point to same object if they are equal."""
        if not hasattr(self, 'children'):
            return self
        uniq = list(set(self.flat)) if uniq == None else uniq
        for i, c in enumerate(self.children):
            if not hasattr(c, 'children'):
                assert c in uniq
                self.children[i] = uniq[uniq.index(c)]
            else:
                c.fix_identities(uniq)

    def fix_list_arguments(self):
        """Find arguments that should accumulate values and fix them."""
        either = [list(c.children) for c in self.either.children]
        for case in either:
            case = [c for c in case if case.count(c) > 1]
            for a in [e for e in case if type(e) == Argument]:
                a.value = []
        return self

    @property
    def either(self):
        """Transform pattern into an equivalent, with only top-level Either."""
        # Currently the pattern will not be equivalent, but more "narrow",
        # although good enough to reason about list arguments.
        if not hasattr(self, 'children'):
            return Either(Required(self))
        else:
            ret = []
            groups = [[self]]
            while groups:
                children = groups.pop(0)
                types = [type(c) for c in children]
                if Either in types:
                    either = [c for c in children if type(c) is Either][0]
                    children.pop(children.index(either))
                    for c in either.children:
                        groups.append([c] + children)
                elif Required in types:
                    required = [c for c in children if type(c) is Required][0]
                    children.pop(children.index(required))
                    groups.append(list(required.children) + children)
                elif Optional in types:
                    optional = [c for c in children if type(c) is Optional][0]
                    children.pop(children.index(optional))
                    groups.append(list(optional.children) + children)
                elif OneOrMore in types:
                    oneormore = [c for c in children if type(c) is OneOrMore][0]
                    children.pop(children.index(oneormore))
                    groups.append(list(oneormore.children) * 2 + children)
                else:
                    ret.append(children)
            return Either(*[Required(*e) for e in ret])


class Argument(Pattern):

    def __init__(self, name, value=None):
        self.name = name
        self.value = value

    def match(self, left, collected=None):
        collected = [] if collected is None else collected
        args = [l for l in left if type(l) is Argument]
        if not len(args):
            return False, left, collected
        left.remove(args[0])
        if type(self.value) is not list:
            return True, left, collected + [Argument(self.name, args[0].value)]
        same_name = [a for a in collected
                     if type(a) is Argument and a.name == self.name]
        if len(same_name):
            same_name[0].value += [args[0].value]
            return True, left, collected
        else:
            return True, left, collected + [Argument(self.name,
                                                     [args[0].value])]

    def __repr__(self):
        return 'Argument(%r, %r)' % (self.name, self.value)


class Command(Pattern):

    def __init__(self, name, value=False):
        self.name = name
        self.value = value

    def match(self, left, collected=None):
        collected = [] if collected is None else collected
        args = [l for l in left if type(l) is Argument]
        if not len(args) or args[0].value != self.name:
            return False, left, collected
        left.remove(args[0])
        return True, left, collected + [Command(self.name, True)]

    def __repr__(self):
        return 'Command(%r, %r)' % (self.name, self.value)


class Option(Pattern):

    def __init__(self, short=None, long=None, argcount=0, value=False):
        assert argcount in (0, 1)
        self.short, self.long = short, long
        self.argcount, self.value = argcount, value
        self.value = None if value == False and argcount else value  # HACK

    @classmethod
    def parse(class_, option_description):
        short, long, argcount, value = None, None, 0, False
        options, _, description = option_description.strip().partition('  ')
        options = options.replace(',', ' ').replace('=', ' ')
        for s in options.split():
            if s.startswith('--'):
                long = s
            elif s.startswith('-'):
                short = s
            else:
                argcount = 1
        if argcount:
            matched = re.findall('\[default: (.*)\]', description, flags=re.I)
            value = matched[0] if matched else None
        return class_(short, long, argcount, value)

    def match(self, left, collected=None):
        collected = [] if collected is None else collected
        left_ = []
        for l in left:
            # if this is so greedy, how to handle OneOrMore then?
            if not (type(l) is Option and
                    (self.short, self.long) == (l.short, l.long)):
                left_.append(l)
        return (left != left_), left_, collected

    @property
    def name(self):
        return self.long or self.short

    def __repr__(self):
        return 'Option(%r, %r, %r, %r)' % (self.short, self.long,
                                           self.argcount, self.value)


class AnyOptions(Pattern):

    def match(self, left, collected=None):
        collected = [] if collected is None else collected
        left_ = [l for l in left if not type(l) == Option]
        return (left != left_), left_, collected


class Required(Pattern):

    def match(self, left, collected=None):
        collected = [] if collected is None else collected
        l = copy(left)
        c = copy(collected)
        for p in self.children:
            matched, l, c = p.match(l, c)
            if not matched:
                return False, left, collected
        return True, l, c


class Optional(Pattern):

    def match(self, left, collected=None):
        collected = [] if collected is None else collected
        left = copy(left)
        for p in self.children:
            m, left, collected = p.match(left, collected)
        return True, left, collected


class OneOrMore(Pattern):

    def match(self, left, collected=None):
        assert len(self.children) == 1
        collected = [] if collected is None else collected
        l = copy(left)
        c = copy(collected)
        l_ = None
        matched = True
        times = 0
        while matched:
            # could it be that something didn't match but changed l or c?
            matched, l, c = self.children[0].match(l, c)
            times += 1 if matched else 0
            if l_ == l:
                break
            l_ = copy(l)
        if times >= 1:
            return True, l, c
        return False, left, collected


class Either(Pattern):

    def match(self, left, collected=None):
        collected = [] if collected is None else collected
        outcomes = []
        for p in self.children:
            matched, _, _ = outcome = p.match(copy(left), copy(collected))
            if matched:
                outcomes.append(outcome)
        if outcomes:
            return min(outcomes, key=lambda outcome: len(outcome[1]))
        return False, left, collected


class TokenStream(list):

    def __init__(self, source, error):
        self += source.split() if type(source) is str else source
        self.error = error

    def move(self):
        return self.pop(0) if len(self) else None

    def current(self):
        return self[0] if len(self) else None


def parse_long(tokens, options):
    raw, eq, value = tokens.move().partition('=')
    value = None if eq == value == '' else value
    opt = [o for o in options if o.long and o.long.startswith(raw)]
    if len(opt) < 1:
        if tokens.error is DocoptExit:
            raise tokens.error('%s is not recognized' % raw)
        else:
            o = Option(None, raw, (1 if eq == '=' else 0))
            options.append(o)
            return [o]
    if len(opt) > 1:
        raise tokens.error('%s is not a unique prefix: %s?' %
                         (raw, ', '.join('%s' % o.long for o in opt)))
    opt = copy(opt[0])
    if opt.argcount == 1:
        if value is None:
            if tokens.current() is None:
                raise tokens.error('%s requires argument' % opt.name)
            value = tokens.move()
    elif value is not None:
        raise tokens.error('%s must not have an argument' % opt.name)
    opt.value = value or True
    return [opt]


def parse_shorts(tokens, options):
    raw = tokens.move()[1:]
    parsed = []
    while raw != '':
        opt = [o for o in options
               if o.short and o.short.lstrip('-').startswith(raw[0])]
        if len(opt) > 1:
            raise tokens.error('-%s is specified ambiguously %d times' %
                              (raw[0], len(opt)))
        if len(opt) < 1:
            if tokens.error is DocoptExit:
                raise tokens.error('-%s is not recognized' % raw[0])
            else:
                o = Option('-' + raw[0], None)
                options.append(o)
                parsed.append(o)
                raw = raw[1:]
                continue
        opt = copy(opt[0])
        raw = raw[1:]
        if opt.argcount == 0:
            value = True
        else:
            if raw == '':
                if tokens.current() is None:
                    raise tokens.error('-%s requires argument' % opt.short[0])
                raw = tokens.move()
            value, raw = raw, ''
        opt.value = value
        parsed.append(opt)
    return parsed


def parse_pattern(source, options):
    tokens = TokenStream(re.sub(r'([\[\]\(\)\|]|\.\.\.)', r' \1 ', source),
                         DocoptLanguageError)
    result = parse_expr(tokens, options)
    if tokens.current() is not None:
        raise tokens.error('unexpected ending: %r' % ' '.join(tokens))
    return Required(*result)


def parse_expr(tokens, options):
    """expr ::= seq ( '|' seq )* ;"""
    seq = parse_seq(tokens, options)
    if tokens.current() != '|':
        return seq
    result = [Required(*seq)] if len(seq) > 1 else seq
    while tokens.current() == '|':
        tokens.move()
        seq = parse_seq(tokens, options)
        result += [Required(*seq)] if len(seq) > 1 else seq
    return [Either(*result)] if len(result) > 1 else result


def parse_seq(tokens, options):
    """seq ::= ( atom [ '...' ] )* ;"""
    result = []
    while tokens.current() not in [None, ']', ')', '|']:
        atom = parse_atom(tokens, options)
        if tokens.current() == '...':
            atom = [OneOrMore(*atom)]
            tokens.move()
        result += atom
    return result


def parse_atom(tokens, options):
    """atom ::= '(' expr ')' | '[' expr ']' | 'options'
             | long | shorts | argument | command ;
    """
    token = tokens.current()
    result = []
    if token == '(':
        tokens.move()
        result = [Required(*parse_expr(tokens, options))]
        if tokens.move() != ')':
            raise tokens.error("Unmatched '('")
        return result
    elif token == '[':
        tokens.move()
        result = [Optional(*parse_expr(tokens, options))]
        if tokens.move() != ']':
            raise tokens.error("Unmatched '['")
        return result
    elif token == 'options':
        tokens.move()
        return [AnyOptions()]
    elif token.startswith('--') and token != '--':
        return parse_long(tokens, options)
    elif token.startswith('-') and token not in ('-', '--'):
        return parse_shorts(tokens, options)
    elif token.startswith('<') and token.endswith('>') or token.isupper():
        return [Argument(tokens.move())]
    else:
        return [Command(tokens.move())]


def parse_args(source, options):
    tokens = TokenStream(source, DocoptExit)
    options = copy(options)
    parsed = []
    while tokens.current() is not None:
        if tokens.current() == '--':
            return parsed + [Argument(None, v) for v in tokens]
        elif tokens.current().startswith('--'):
            parsed += parse_long(tokens, options)
        elif tokens.current().startswith('-') and tokens.current() != '-':
            parsed += parse_shorts(tokens, options)
        else:
            parsed.append(Argument(None, tokens.move()))
    return parsed


def parse_doc_options(doc):
    return [Option.parse('-' + s) for s in re.split('^ *-|\n *-', doc)[1:]]


def printable_usage(doc):
    usage_split = re.split(r'([Uu][Ss][Aa][Gg][Ee]:)', doc)
    if len(usage_split) < 3:
        raise DocoptLanguageError('"usage:" (case-insensitive) not found.')
    if len(usage_split) > 3:
        raise DocoptLanguageError('More than one "usage:" (case-insensitive).')
    return re.split(r'\n\s*\n', ''.join(usage_split[1:]))[0].strip()


def formal_usage(printable_usage):
    pu = printable_usage.split()[1:]  # split and drop "usage:"
    return ' '.join('|' if s == pu[0] else s for s in pu[1:])


def extras(help, version, options, doc):
    if help and any((o.name in ('-h', '--help')) and o.value for o in options):
        print(doc.strip())
        exit()
    if version and any(o.name == '--version' and o.value for o in options):
        print(version)
        exit()


class Dict(dict):
    def __repr__(self):
        return '{%s}' % ',\n '.join('%r: %r' % i for i in sorted(self.items()))


def docopt(doc, argv=sys.argv[1:], help=True, version=None):
    DocoptExit.usage = docopt.usage = usage = printable_usage(doc)
    pot_options = parse_doc_options(doc)
    formal_pattern = parse_pattern(formal_usage(usage), options=pot_options)
    argv = parse_args(argv, options=pot_options)
    extras(help, version, argv, doc)
    matched, left, arguments = formal_pattern.fix().match(argv)
    if matched and left == []:  # better message if left?
        options = [o for o in argv if type(o) is Option]
        pot_arguments = [a for a in formal_pattern.flat
                         if type(a) in [Argument, Command]]
        return Dict((a.name, a.value) for a in
                    (pot_options + options + pot_arguments + arguments))
    raise DocoptExit()
