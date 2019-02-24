# -*- coding: utf-8 -*-
"""
    livereload.server
    ~~~~~~~~~~~~~~~~~

    WSGI app server for livereload.

    :copyright: (c) 2013 - 2015 by Hsiaoming Yang
    :license: BSD, see LICENSE for more details.
"""

import os
import time
import shlex
import logging
import threading
import webbrowser
from subprocess import Popen, PIPE

from tornado.wsgi import WSGIContainer
from tornado.ioloop import IOLoop
from tornado import web
from tornado import escape
from tornado import httputil
from tornado.log import LogFormatter
from .handlers import LiveReloadHandler, LiveReloadJSHandler
from .handlers import ForceReloadHandler, StaticFileHandler
from .watcher import get_watcher_class
from six import string_types, PY3

logger = logging.getLogger('livereload')

HEAD_END = b'</head>'


def shell(cmd, output=None, mode='w', cwd=None, shell=False):
    """Execute a shell command.

    You can add a shell command::

        server.watch(
            'style.less', shell('lessc style.less', output='style.css')
        )

    :param cmd: a shell command, string or list
    :param output: output stdout to the given file
    :param mode: only works with output, mode ``w`` means write,
                 mode ``a`` means append
    :param cwd: set working directory before command is executed.
    :param shell: if true, on Unix the executable argument specifies a
                  replacement shell for the default ``/bin/sh``.
    """
    if not output:
        output = os.devnull
    else:
        folder = os.path.dirname(output)
        if folder and not os.path.isdir(folder):
            os.makedirs(folder)

    if not isinstance(cmd, (list, tuple)) and not shell:
        cmd = shlex.split(cmd)

    def run_shell():
        try:
            p = Popen(cmd, stdin=PIPE, stdout=PIPE, stderr=PIPE, cwd=cwd,
                      shell=shell)
        except OSError as e:
            logger.error(e)
            if e.errno == os.errno.ENOENT:  # file (command) not found
                logger.error("maybe you haven't installed %s", cmd[0])
            return e
        stdout, stderr = p.communicate()
        if stderr:
            logger.error(stderr)
            return stderr
        #: stdout is bytes, decode for python3
        if PY3:
            stdout = stdout.decode()
        with open(output, mode) as f:
            f.write(stdout)

    return run_shell


class LiveScriptInjector(web.OutputTransform):
    def __init__(self, request):
        super(LiveScriptInjector, self).__init__(request)

    def transform_first_chunk(self, status_code, headers, chunk, finishing):
        if HEAD_END in chunk:
            chunk = chunk.replace(HEAD_END, self.script + HEAD_END)
            if 'Content-Length' in headers:
                length = int(headers['Content-Length']) + len(self.script)
                headers['Content-Length'] = str(length)
        return status_code, headers, chunk


class LiveScriptContainer(WSGIContainer):
    def __init__(self, wsgi_app, script=''):
        self.wsgi_app = wsgi_app
        self.script = script

    def __call__(self, request):
        data = {}
        response = []

        def start_response(status, response_headers, exc_info=None):
            data["status"] = status
            data["headers"] = response_headers
            return response.append

        app_response = self.wsgi_app(
            WSGIContainer.environ(request), start_response)
        try:
            response.extend(app_response)
            body = b"".join(response)
        finally:
            if hasattr(app_response, "close"):
                app_response.close()
        if not data:
            raise Exception("WSGI app did not call start_response")

        status_code, reason = data["status"].split(' ', 1)
        status_code = int(status_code)
        headers = data["headers"]
        header_set = set(k.lower() for (k, v) in headers)
        body = escape.utf8(body)

        if HEAD_END in body:
            body = body.replace(HEAD_END, self.script + HEAD_END)

        if status_code != 304:
            if "content-type" not in header_set:
                headers.append(("Content-Type", "application/octet-stream; charset=UTF-8"))
            if "content-length" not in header_set:
                headers.append(("Content-Length", str(len(body))))

        if "server" not in header_set:
            headers.append(("Server", "LiveServer"))

        start_line = httputil.ResponseStartLine(
            "HTTP/1.1", status_code, reason
        )
        header_obj = httputil.HTTPHeaders()
        for key, value in headers:
            if key.lower() == 'content-length':
                value = str(len(body))
            header_obj.add(key, value)
        request.connection.write_headers(start_line, header_obj, chunk=body)
        request.connection.finish()
        self._log(status_code, request)


class Server(object):
    """Livereload server interface.

    Initialize a server and watch file changes::

        server = Server(wsgi_app)
        server.serve()

    :param app: a wsgi application instance
    :param watcher: A Watcher instance, you don't have to initialize
                    it by yourself. Under Linux, you will want to install
                    pyinotify and use INotifyWatcher() to avoid wasted
                    CPU usage.
    """
    def __init__(self, app=None, watcher=None):
        self.root = None

        self.app = app
        if not watcher:
            watcher_cls = get_watcher_class()
            watcher = watcher_cls()
        self.watcher = watcher

    def watch(self, filepath, func=None, delay=None, ignore=None):
        """Add the given filepath for watcher list.

        Once you have intialized a server, watch file changes before
        serve the server::

            server.watch('static/*.stylus', 'make static')
            def alert():
                print('foo')
            server.watch('foo.txt', alert)
            server.serve()

        :param filepath: files to be watched, it can be a filepath,
                         a directory, or a glob pattern
        :param func: the function to be called, it can be a string of
                     shell command, or any callable object without
                     parameters
        :param delay: Delay sending the reload message. Use 'forever' to
                      not send it. This is useful to compile sass files to
                      css, but reload on changed css files then only.
        :param ignore: A function return True to ignore a certain pattern of
                       filepath.
        """
        if isinstance(func, string_types):
            func = shell(func)

        self.watcher.watch(filepath, func, delay, ignore=ignore)

    def application(self, port, host, liveport=None, debug=None, live_css=True):
        LiveReloadHandler.watcher = self.watcher
        LiveReloadHandler.live_css = live_css
        if liveport is None:
            liveport = port
        if debug is None and self.app:
            debug = True

        live_handlers = [
            (r'/livereload', LiveReloadHandler),
            (r'/forcereload', ForceReloadHandler),
            (r'/livereload.js', LiveReloadJSHandler)
        ]

        # The livereload.js snippet.
        # Uses JavaScript to dynamically inject the client's hostname.
        # This allows for serving on 0.0.0.0.
        live_script = escape.utf8((
            '<script type="text/javascript">'
            'document.write("<script src=''http://"'
            ' + window.location.hostname + ":{port}/livereload.js''>'
            ' </"+"script>");'
            '</script>'
        ).format(port=liveport))

        web_handlers = self.get_web_handlers(live_script)

        class ConfiguredTransform(LiveScriptInjector):
            script = live_script

        if liveport == port:
            handlers = live_handlers + web_handlers
            app = web.Application(
                handlers=handlers,
                debug=debug,
                transforms=[ConfiguredTransform]
            )
            app.listen(port, address=host)
        else:
            app = web.Application(
                handlers=web_handlers,
                debug=debug,
                transforms=[ConfiguredTransform]
            )
            app.listen(port, address=host)
            live = web.Application(handlers=live_handlers, debug=False)
            live.listen(liveport, address=host)

    def get_web_handlers(self, script):
        if self.app:
            fallback = LiveScriptContainer(self.app, script)
            return [(r'.*', web.FallbackHandler, {'fallback': fallback})]
        return [
            (r'/(.*)', StaticFileHandler, {
                'path': self.root or '.',
                'default_filename': 'index.html',
            }),
        ]

    def serve(self, port=5500, liveport=None, host=None, root=None, debug=None,
              open_url=False, restart_delay=2, open_url_delay=None, live_css=True):
        """Start serve the server with the given port.

        :param port: serve on this port, default is 5500
        :param liveport: live reload on this port
        :param host: serve on this hostname, default is 127.0.0.1
        :param root: serve static on this root directory
        :param debug: set debug mode, which autoreloads the app on code changes
                      via Tornado (and causes polling). Defaults to True when
                      ``self.app`` is set, otherwise False.
        :param open_url_delay: open webbrowser after the delay seconds
        :param live_css: whether to use live css or force reload on css. Defaults to True
        """
        host = host or '127.0.0.1'
        if root is not None:
            self.root = root

        self._setup_logging()
        logger.info('Serving on http://%s:%s' % (host, port))

        self.application(port, host, liveport=liveport, debug=debug, live_css=live_css)

        # Async open web browser after 5 sec timeout
        if open_url or open_url_delay:
            if open_url:
                logger.warn('Use `open_url_delay` instead of `open_url`')
            sleep = open_url_delay or 5

            def opener():
                time.sleep(sleep)
                webbrowser.open('http://%s:%s' % (host, port))
            threading.Thread(target=opener).start()

        try:
            self.watcher._changes.append(('__livereload__', restart_delay))
            LiveReloadHandler.start_tasks()
            IOLoop.instance().start()
        except KeyboardInterrupt:
            logger.info('Shutting down...')

    def _setup_logging(self):
        logger.setLevel(logging.INFO)

        channel = logging.StreamHandler()
        channel.setFormatter(LogFormatter())
        logger.addHandler(channel)

        # need a tornado logging handler to prevent IOLoop._setup_logging
        logging.getLogger('tornado').addHandler(channel)
