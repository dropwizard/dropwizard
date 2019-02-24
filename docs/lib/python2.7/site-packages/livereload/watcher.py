# -*- coding: utf-8 -*-
"""
    livereload.watcher
    ~~~~~~~~~~~~~~~~~~

    A file watch management for LiveReload Server.

    :copyright: (c) 2013 - 2015 by Hsiaoming Yang
    :license: BSD, see LICENSE for more details.
"""

import os
import glob
import time
try:
    import pyinotify
except ImportError:
    pyinotify = None


class Watcher(object):
    """A file watcher registery."""
    def __init__(self):
        self._tasks = {}
        self._mtimes = {}

        # setting changes
        self._changes = []

        # filepath that is changed
        self.filepath = None
        self._start = time.time()

    def ignore(self, filename):
        """Ignore a given filename or not."""
        _, ext = os.path.splitext(filename)
        return ext in ['.pyc', '.pyo', '.o', '.swp']

    def watch(self, path, func=None, delay=0, ignore=None):
        """Add a task to watcher.

        :param path: a filepath or directory path or glob pattern
        :param func: the function to be executed when file changed
        :param delay: Delay sending the reload message. Use 'forever' to
                      not send it. This is useful to compile sass files to
                      css, but reload on changed css files then only.
        :param ignore: A function return True to ignore a certain pattern of
                       filepath.
        """
        self._tasks[path] = {
            'func': func,
            'delay': delay,
            'ignore': ignore,
        }

    def start(self, callback):
        """Start the watcher running, calling callback when changes are
        observed. If this returns False, regular polling will be used."""
        return False

    def examine(self):
        """Check if there are changes, if true, run the given task."""
        if self._changes:
            return self._changes.pop()

        # clean filepath
        self.filepath = None
        delays = set()
        for path in self._tasks:
            item = self._tasks[path]
            if self.is_changed(path, item['ignore']):
                func = item['func']
                func and func()
                delay = item['delay']
                if delay and isinstance(delay, float):
                    delays.add(delay)

        if delays:
            delay = max(delays)
        else:
            delay = None
        return self.filepath, delay

    def is_changed(self, path, ignore=None):
        if os.path.isfile(path):
            return self.is_file_changed(path, ignore)
        elif os.path.isdir(path):
            return self.is_folder_changed(path, ignore)
        return self.is_glob_changed(path, ignore)

    def is_file_changed(self, path, ignore=None):
        if not os.path.isfile(path):
            return False

        if self.ignore(path):
            return False

        if ignore and ignore(path):
            return False

        mtime = os.path.getmtime(path)

        if path not in self._mtimes:
            self._mtimes[path] = mtime
            self.filepath = path
            return mtime > self._start

        if self._mtimes[path] != mtime:
            self._mtimes[path] = mtime
            self.filepath = path
            return True

        self._mtimes[path] = mtime
        return False

    def is_folder_changed(self, path, ignore=None):
        for root, dirs, files in os.walk(path, followlinks=True):
            if '.git' in dirs:
                dirs.remove('.git')
            if '.hg' in dirs:
                dirs.remove('.hg')
            if '.svn' in dirs:
                dirs.remove('.svn')
            if '.cvs' in dirs:
                dirs.remove('.cvs')

            for f in files:
                if self.is_file_changed(os.path.join(root, f), ignore):
                    return True
        return False

    def is_glob_changed(self, path, ignore=None):
        for f in glob.glob(path):
            if self.is_file_changed(f, ignore):
                return True
        return False


class INotifyWatcher(Watcher):
    def __init__(self):
        Watcher.__init__(self)

        self.wm = pyinotify.WatchManager()
        self.notifier = None
        self.callback = None

    def watch(self, path, func=None, delay=None, ignore=None):
        flag = pyinotify.IN_CREATE | pyinotify.IN_DELETE | pyinotify.IN_MODIFY
        self.wm.add_watch(path, flag, rec=True, do_glob=True, auto_add=True)
        Watcher.watch(self, path, func, delay, ignore)

    def inotify_event(self, event):
        self.callback()

    def start(self, callback):
        if not self.notifier:
            self.callback = callback

            from tornado import ioloop
            self.notifier = pyinotify.TornadoAsyncNotifier(
                self.wm, ioloop.IOLoop.instance(),
                default_proc_fun=self.inotify_event
            )
            callback()
        return True


def get_watcher_class():
    if pyinotify is None or not hasattr(pyinotify, 'TornadoAsyncNotifier'):
        return Watcher
    return INotifyWatcher
