# -*- coding: utf-8 -*-
"""
    livereload.handlers
    ~~~~~~~~~~~~~~~~~~~

    HTTP and WebSocket handlers for livereload.

    :copyright: (c) 2013 - 2015 by Hsiaoming Yang
    :license: BSD, see LICENSE for more details.
"""

import os
import time
import logging
from pkg_resources import resource_string
from tornado import web
from tornado import ioloop
from tornado import escape
from tornado.websocket import WebSocketHandler
from tornado.util import ObjectDict

logger = logging.getLogger('livereload')


class LiveReloadHandler(WebSocketHandler):
    waiters = set()
    watcher = None
    live_css = None
    _last_reload_time = None

    def allow_draft76(self):
        return True

    def check_origin(self, origin):
        return True

    def on_close(self):
        if self in LiveReloadHandler.waiters:
            LiveReloadHandler.waiters.remove(self)

    def send_message(self, message):
        if isinstance(message, dict):
            message = escape.json_encode(message)

        try:
            self.write_message(message)
        except:
            logger.error('Error sending message', exc_info=True)

    @classmethod
    def start_tasks(cls):
        if cls._last_reload_time:
            return

        if not cls.watcher._tasks:
            logger.info('Watch current working directory')
            cls.watcher.watch(os.getcwd())

        cls._last_reload_time = time.time()
        logger.info('Start watching changes')
        if not cls.watcher.start(cls.poll_tasks):
            logger.info('Start detecting changes')
            ioloop.PeriodicCallback(cls.poll_tasks, 800).start()

    @classmethod
    def poll_tasks(cls):
        filepath, delay = cls.watcher.examine()
        if not filepath or delay == 'forever' or not cls.waiters:
            return
        reload_time = 3

        if delay:
            reload_time = max(3 - delay, 1)
        if filepath == '__livereload__':
            reload_time = 0

        if time.time() - cls._last_reload_time < reload_time:
            # if you changed lot of files in one time
            # it will refresh too many times
            logger.info('Ignore: %s', filepath)
            return
        if delay:
            loop = ioloop.IOLoop.current()
            loop.call_later(delay, cls.reload_waiters)
        else:
            cls.reload_waiters()

    @classmethod
    def reload_waiters(cls, path=None):
        logger.info(
            'Reload %s waiters: %s',
            len(cls.waiters),
            cls.watcher.filepath,
        )

        if path is None:
            path = cls.watcher.filepath or '*'

        msg = {
            'command': 'reload',
            'path': path,
            'liveCSS': cls.live_css,
            'liveImg': True,
        }

        cls._last_reload_time = time.time()
        for waiter in cls.waiters:
            try:
                waiter.write_message(msg)
            except:
                logger.error('Error sending message', exc_info=True)
                cls.waiters.remove(waiter)

    def on_message(self, message):
        """Handshake with livereload.js

        1. client send 'hello'
        2. server reply 'hello'
        3. client send 'info'
        """
        message = ObjectDict(escape.json_decode(message))
        if message.command == 'hello':
            handshake = {
                'command': 'hello',
                'protocols': [
                    'http://livereload.com/protocols/official-7',
                ],
                'serverName': 'livereload-tornado',
            }
            self.send_message(handshake)

        if message.command == 'info' and 'url' in message:
            logger.info('Browser Connected: %s' % message.url)
            LiveReloadHandler.waiters.add(self)


class LiveReloadJSHandler(web.RequestHandler):

    def get(self):
        self.set_header('Content-Type', 'application/javascript')
        self.write(resource_string(__name__, 'vendors/livereload.js'))


class ForceReloadHandler(web.RequestHandler):
    def get(self):
        path = self.get_argument('path', default=None) or '*'
        LiveReloadHandler.reload_waiters(path)
        self.write('ok')


class StaticFileHandler(web.StaticFileHandler):
    def should_return_304(self):
        return False
