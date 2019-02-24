from sphinx_autobuild import SphinxBuilder


class TestWatcher(object):
    pass


class TestBuilder(SphinxBuilder):
    def __init__(self, ignored=None, regex_ignored=None):
        super(TestBuilder, self).__init__('', [], ignored, regex_ignored)
        self.built = []

    def __call__(self, src_path):
        watcher = TestWatcher()
        super(TestBuilder, self).__call__(watcher, src_path)
        return hasattr(watcher, '_action_file')

    def build(self, path):
        self.built.append(path)


def test_no_ignore():
    builder = TestBuilder()
    assert builder('test.rst')


def test_fnmatch():
    builder = TestBuilder(ignored=[
        'test.rst',
        'test-?.rst',
        'test'
    ])
    assert builder('test1.rst')
    assert not builder('test.rst')
    assert not builder('test-2.rst')
    assert not builder('test/test.rst')
    assert builder('test1/test.rst')


def test_regex():
    builder = TestBuilder(regex_ignored=[
        r'__pycache__/.*\.py',
    ])
    assert builder('test.py')
    assert builder('__pycache__/test.rst')
    assert not builder('__pycache__/test.py')
