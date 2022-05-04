import os
import re

from sphinx.directives.code import *
from source.utils import maven_version


def get_version():
    version = os.environ.get('DROPWIZARD_VERSION', '').strip()
    version = version or maven_version('../pom.xml')
    version_regex = re.match(r"(?P<major>\d+)\.(?P<minor>\d+).*", version)
    return "%s.%s" % (version_regex.group('major'), version_regex.group('minor'))


def get_dropwizard_link(path, from_line, to_line):
    version = get_version()
    return '`Source file <https://github.com/dropwizard/dropwizard/tree/release/{}.x/docs/source/{}#L{}-L{}>`__'\
        .format(version, path.lstrip('/'), from_line, to_line)


class DropwizardLiteralInclude(LiteralInclude):
    def run(self):
        lineno_match = 'lineno-match' in self.options
        show_linenos = lineno_match or 'linenos' in self.options
        if 'caption' in self.options:
            caption = self.options['caption'] + ' '
            del self.options['caption']
        else:
            caption = ''
        self.options['lineno-match'] = True
        result = LiteralInclude.run(self)[0]
        result.attributes['linenos'] = show_linenos
        starting_line = result.attributes['highlight_args']['linenostart']
        lines = len(result.children[0].splitlines())
        if not lineno_match:
            result.attributes['highlight_args']['linenostart'] = 1
        link = get_dropwizard_link(self.arguments[0], starting_line, starting_line + lines - 1)
        caption += link
        return [container_wrapper(self, result, caption)]


def setup(app):
    app.add_directive("dropwizard_literalinclude", DropwizardLiteralInclude)

    return {
        'version': '0.1',
        'parallel_read_safe': True,
        'parallel_write_safe': False,
    }
