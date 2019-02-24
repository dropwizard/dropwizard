"""
    Lexer for LESSCSS.

    http://www.dabeaz.com/ply/ply.html
    http://www.w3.org/TR/CSS21/grammar.html#scanner
    http://lesscss.org/#docs

    Copyright (c)
    See LICENSE for details.
    <jtm@robot.is>
"""
import re
import ply.lex as lex
from six import string_types

from lesscpy.lib import dom
from lesscpy.lib import css
from lesscpy.lib import reserved


class LessLexer:
    states = (
        ('parn', 'inclusive'),
        ('escapequotes', 'inclusive'),
        ('escapeapostrophe', 'inclusive'),
        ('istringquotes', 'inclusive'),
        ('istringapostrophe', 'inclusive'),
        ('iselector', 'inclusive'),
        ('mediaquery', 'inclusive'),
        ('import', 'inclusive'),
    )
    literals = '<>=%!/*-+&'
    tokens = [
        'css_ident',
        'css_dom',
        'css_class',
        'css_id',
        'css_property',
        'css_vendor_property',
        'css_comment',
        'css_string',
        'css_color',
        'css_filter',
        'css_number',
        'css_important',
        'css_vendor_hack',
        'css_uri',
        'css_ms_filter',
        'css_keyframe_selector',

        'css_media_type',
        'css_media_feature',

        't_and',
        't_not',
        't_only',

        'less_variable',
        'less_comment',
        'less_open_format',
        'less_when',
        'less_and',
        'less_not',

        't_ws',
        't_popen',
        't_pclose',
        't_semicolon',
        't_tilde',
        't_colon',
        't_comma',

        't_eopen',
        't_eclose',

        't_isopen',
        't_isclose',

        't_bopen',
        't_bclose'
    ]
    tokens += list(set(reserved.tokens.values()))
    # Tokens with significant following whitespace
    significant_ws = set([
        'css_class',
        'css_id',
        'css_dom',
        'css_property',
        'css_vendor_property',
        'css_ident',
        'css_number',
        'css_color',
        'css_media_type',
        'css_filter',
        'less_variable',
        't_and',
        't_not',
        't_only',
        '&',
    ])
    significant_ws.update(reserved.tokens.values())

    def __init__(self):
        self.build(reflags=re.UNICODE | re.IGNORECASE)
        self.last = None
        self.next_ = None
        self.pretok = True

    def t_css_filter(self, t):
        (r'\[[^\]]*\]'
         '|(not|lang|nth-[a-z\-]+)\(.+\)'
         '|and[ \t]\([^><=\{]+\)')
        return t

    def t_css_ms_filter(self, t):
        r'(?:progid:|DX\.)[^;\(]*'
        return t

    def t_t_bopen(self, t):
        r'\{'
        t.lexer.in_property_decl = False
        return t

    def t_t_bclose(self, t):
        r'\}'
        return t

    def t_t_colon(self, t):
        r':'
        return t

    def t_t_comma(self, t):
        r','
        t.lexer.in_property_decl = False
        return t

    def t_css_number(self, t):
        r'-?(\d*\.\d+|\d+)(s|%|in|ex|[ecm]m|p[txc]|deg|g?rad|ms?|k?hz|dpi|dpcm|dppx)?'
        return t

    def t_css_ident(self, t):
        (r'([\-\.\#]?'
         '([_a-z]'
         '|[\200-\377]'
         '|\\\[0-9a-f]{1,6}'
         '|\\\[^\s\r\n0-9a-f])'
         '([_a-z0-9\-]'
         '|[\200-\377]'
         '|\\\[0-9a-f]{1,6}'
         '|\\\[^\s\r\n0-9a-f])*)'
         '|\.')
        v = t.value.strip()
        c = v[0]
        if c == '.':
            # In some cases, only the '.' can be marked as CSS class.
            #
            # Example: .@{name}
            #
            t.type = 'css_class'
            if t.lexer.lexstate != "iselector":
                # Selector-chaining case (a.b.c), we are already in state 'iselector'
                t.lexer.push_state("iselector")
        elif c == '#':
            t.type = 'css_id'
            if len(v) in [4, 7]:
                try:
                    int(v[1:], 16)
                    t.type = 'css_color'
                except ValueError:
                    pass
        elif v == 'when':
            t.type = 'less_when'
        elif v == 'and':
            t.type = 'less_and'
        elif v == 'not':
            t.type = 'less_not'
        elif v in ('from', 'to'):
            t.type = 'css_keyframe_selector'
        elif v in css.propertys:
            t.type = 'css_property'
            t.lexer.in_property_decl = True
        elif (v in dom.elements or v.lower() in dom.elements) and not t.lexer.in_property_decl:
            # DOM elements can't be part of property declarations, avoids ambiguity between 'rect' DOM
            # element and rect() CSS function.
            t.type = 'css_dom'
        elif c == '-':
            t.type = 'css_vendor_property'
            t.lexer.in_property_decl = True
        t.value = v
        return t

    def t_iselector_less_variable(self, t):
        r'@\{[^@\}]+\}'
        return t

    def t_iselector_t_eclose(self, t):
        r'"|\''
        # Can only happen if iselector state is on top of estring state.
        #
        # Example: @item: ~".col-xs-@{index}";
        #
        t.lexer.pop_state()
        return t

    def t_iselector_css_filter(self, t):
        (r'\[[^\]]*\]'
         '|(not|lang|nth-[a-z\-]+)\(.+\)'
         '|and[ \t]\([^><\{]+\)')
        # TODO/FIXME(saschpe): Only needs to be redifined in state 'iselector' so that
        # the following css_class doesn't catch everything.
        return t

    def t_iselector_css_class(self, t):
        r'[_a-z0-9\-]+'
        # The first part of CSS class was tokenized by t_css_ident() already.
        # Here we gather up the any LESS variable.
        #
        # Example: .span_@{num}_small
        #
        return t

    def t_iselector_t_ws(self, t):
        r'[ \t\f\v]+'
        #
        # Example: .span_@{num}
        #
        t.lexer.pop_state()
        t.value = ' '
        return t

    def t_iselector_t_bopen(self, t):
        r'\{'
        t.lexer.pop_state()
        return t

    def t_iselector_t_colon(self, t):
        r':'
        t.lexer.pop_state()
        return t

    def t_mediaquery_t_not(self, t):
        r'not'
        return t

    def t_mediaquery_t_only(self, t):
        r'only'
        return t

    def t_mediaquery_t_and(self, t):
        r'and'
        return t

    def t_mediaquery_t_popen(self, t):
        r'\('
        # Redefine global t_popen to avoid pushing state 'parn'
        return t

    @lex.TOKEN('|'.join(css.media_types))
    def t_mediaquery_css_media_type(self, t):
        return t

    @lex.TOKEN('|'.join(css.media_features))
    def t_mediaquery_css_media_feature(self, t):
        return t

    def t_mediaquery_t_bopen(self, t):
        r'\{'
        t.lexer.pop_state()
        return t

    def t_mediaquery_t_semicolon(self, t):
        r';'
        # This can happen only as part of a CSS import statement. The
        # "mediaquery" state is reused there. Ordinary media queries always
        # end at '{', i.e. when a block is opened.
        t.lexer.pop_state()  # state mediaquery
        # We have to pop the 'import' state here because we already ate the
        # t_semicolon and won't trigger t_import_t_semicolon.
        t.lexer.pop_state()  # state import
        return t

    @lex.TOKEN('|'.join(css.media_types))
    def t_import_css_media_type(self, t):
        # Example: @import url("bar.css") handheld and (max-width: 500px);
        # Alternatively, we could use a lookahead "if not ';'" after the URL
        # part of the @import statement...
        t.lexer.push_state("mediaquery")
        return t

    def t_import_t_semicolon(self, t):
        r';'
        t.lexer.pop_state()
        return t

    def t_less_variable(self, t):
        r'@@?[\w-]+|@\{[^@\}]+\}'
        v = t.value.lower()
        if v in reserved.tokens:
            t.type = reserved.tokens[v]
            if t.type == "css_media":
                t.lexer.push_state("mediaquery")
            elif t.type == "css_import":
                t.lexer.push_state("import")
        return t

    def t_css_color(self, t):
        r'\#[0-9]([0-9a-f]{5}|[0-9a-f]{2})'
        return t

    def t_parn_css_uri(self, t):
        (r'data:[^\)]+'
         '|(([a-z]+://)?'
         '('
         '(/?[\.a-z:]+[\w\.:]*[\\/][\\/]?)+'
         '|([a-z][\w\.\-]+(\.[a-z0-9]+))'
         '(\#[a-z]+)?)'
         ')+')
        return t

    def t_parn_css_ident(self, t):
        (r'(([_a-z]'
         '|[\200-\377]'
         '|\\\[0-9a-f]{1,6}'
         '|\\\[^\r\n\s0-9a-f])'
         '([_a-z0-9\-]|[\200-\377]'
         '|\\\[0-9a-f]{1,6}'
         '|\\\[^\r\n\s0-9a-f])*)')
        return t

    def t_newline(self, t):
        r'[\n\r]+'
        t.lexer.lineno += t.value.count('\n')

    def t_css_comment(self, t):
        r'(/\*(.|\n|\r)*?\*/)'
        t.lexer.lineno += t.value.count('\n')
        pass

    def t_less_comment(self, t):
        r'//.*'
        pass

    def t_css_important(self, t):
        r'!\s*important'
        t.value = '!important'
        return t

    def t_t_ws(self, t):
        r'[ \t\f\v]+'
        t.value = ' '
        return t

    def t_t_popen(self, t):
        r'\('
        t.lexer.push_state('parn')
        return t

    def t_less_open_format(self, t):
        r'%\('
        t.lexer.push_state('parn')
        return t

    def t_parn_t_pclose(self, t):
        r'\)'
        t.lexer.pop_state()
        return t

    def t_t_pclose(self, t):
        r'\)'
        return t

    def t_t_semicolon(self, t):
        r';'
        t.lexer.in_property_decl = False
        return t

    def t_t_eopen(self, t):
        r'~"|~\''
        if t.value[1] == '"':
            t.lexer.push_state('escapequotes')
        elif t.value[1] == '\'':
            t.lexer.push_state('escapeapostrophe')
        return t

    def t_t_tilde(self, t):
        r'~'
        return t

    def t_escapequotes_less_variable(self, t):
        r'@\{[^@"\}]+\}'
        return t

    def t_escapeapostrophe_less_variable(self, t):
        r'@\{[^@\'\}]+\}'
        return t

    def t_escapequotes_t_eclose(self, t):
        r'"'
        t.lexer.pop_state()
        return t

    def t_escapeapostrophe_t_eclose(self, t):
        r'\''
        t.lexer.pop_state()
        return t

    def t_css_string(self, t):
        r'"[^"@]*"|\'[^\'@]*\''
        t.lexer.lineno += t.value.count('\n')
        return t

    def t_t_isopen(self, t):
        r'"|\''
        if t.value[0] == '"':
            t.lexer.push_state('istringquotes')
        elif t.value[0] == '\'':
            t.lexer.push_state('istringapostrophe')
        return t

    def t_istringquotes_less_variable(self, t):
        r'@\{[^@"\}]+\}'
        return t

    def t_istringapostrophe_less_variable(self, t):
        r'@\{[^@\'\}]+\}'
        return t

    def t_istringapostrophe_css_string(self, t):
        r'[^\'@]+'
        t.lexer.lineno += t.value.count('\n')
        return t

    def t_istringquotes_css_string(self, t):
        r'[^"@]+'
        t.lexer.lineno += t.value.count('\n')
        return t

    def t_istringquotes_t_isclose(self, t):
        r'"'
        t.lexer.pop_state()
        return t

    def t_istringapostrophe_t_isclose(self, t):
        r'\''
        t.lexer.pop_state()
        return t

    # Error handling rule
    def t_error(self, t):
        raise SyntaxError("Illegal character '%s' line %d" %
                          (t.value[0], t.lexer.lineno))
        t.lexer.skip(1)

    # Build the lexer
    def build(self, **kwargs):
        self.lexer = lex.lex(module=self, **kwargs)
        # State-tracking variable, see http://www.dabeaz.com/ply/ply.html#ply_nn18
        self.lexer.in_property_decl = False

    def file(self, filename):
        """
        Lex file.
        """
        with open(filename) as f:
            self.lexer.input(f.read())
        return self

    def input(self, file):
        """
        Load lexer with content from `file` which can be a path or a file
        like object.
        """
        if isinstance(file, string_types):
            with open(file) as f:
                self.lexer.input(f.read())
        else:
            self.lexer.input(file.read())

    def token(self):
        """
        Token function. Contains 2 hacks:
            1.  Injects ';' into blocks where the last property
                leaves out the ;
            2.  Strips out whitespace from nonsignificant locations
                to ease parsing.
        """
        if self.next_:
            t = self.next_
            self.next_ = None
            return t
        while True:
            t = self.lexer.token()
            if not t:
                return t
            if t.type == 't_ws' and (
                self.pretok or (self.last
                                and self.last.type not in self.significant_ws)):
                continue
            self.pretok = False
            if t.type == 't_bclose' and self.last and self.last.type not in ['t_bopen', 't_bclose'] and self.last.type != 't_semicolon' \
                    and not (hasattr(t, 'lexer') and (t.lexer.lexstate == 'escapequotes' or t.lexer.lexstate == 'escapeapostrophe')):
                self.next_ = t
                tok = lex.LexToken()
                tok.type = 't_semicolon'
                tok.value = ';'
                tok.lineno = t.lineno
                tok.lexpos = t.lexpos
                self.last = tok
                self.lexer.in_property_decl = False
                return tok
            self.last = t
            break
        return t
