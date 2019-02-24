# -*- coding: utf-8 -*-
from __future__ import absolute_import, with_statement
import os
try:
    from ConfigParser import ConfigParser, DEFAULTSECT
except ImportError: # python3
    from configparser import ConfigParser, DEFAULTSECT

from .api import select_random
from .exceptions import PortForException

DEFAULT_CONFIG_PATH = '/etc/port-for.conf'

class PortStore(object):
    def __init__(self, config_filename=DEFAULT_CONFIG_PATH):
        self._config = config_filename

    def bind_port(self, app, port=None):
        if '=' in app or ':' in app:
            raise Exception('invalid app name: "%s"' % app)

        if port is not None:
            port = str(port)

        parser = self._get_parser()

        # this app already use some port; return it
        if parser.has_option(DEFAULTSECT, app):
            actual_port = parser.get(DEFAULTSECT, app)
            if port is not None and port != actual_port:
                msg = "Can't bind to port %s: %s is already associated with port %s" % (port, app, actual_port)
                raise PortForException(msg)
            return int(actual_port)

        # port is already used by an another app
        app_by_port = dict((v, k) for k, v in parser.items(DEFAULTSECT))
        bound_port_numbers = map(int, app_by_port.keys())

        if port is None:
            port = str(select_random(exclude_ports=bound_port_numbers))

        if port in app_by_port:
            binding_app = app_by_port[port]
            if binding_app != app:
                raise PortForException('Port %s is already used by %s!' % (port, binding_app))

        # new app & new port
        parser.set(DEFAULTSECT, app, port)
        self._save(parser)

        return int(port)

    def unbind_port(self, app):
        parser = self._get_parser()
        parser.remove_option(DEFAULTSECT, app)
        self._save(parser)

    def bound_ports(self):
        return [(app, int(port)) for app, port in self._get_parser().items(DEFAULTSECT)]

    def _ensure_config_exists(self):
        if not os.path.exists(self._config):
            with open(self._config, 'wb'):
                pass

    def _get_parser(self):
        self._ensure_config_exists()
        parser = ConfigParser()
        parser.read(self._config)
        return parser

    def _save(self, parser):
        with open(self._config, 'wt') as f:
            parser.write(f)

