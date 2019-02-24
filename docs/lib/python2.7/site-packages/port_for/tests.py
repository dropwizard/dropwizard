# -*- coding: utf-8 -*-
from __future__ import absolute_import
import unittest
import tempfile
import mock
import socket
import os
import port_for
from port_for.utils import ranges_to_set

def test_common_ports():
    assert port_for.is_available(80) == False
    assert port_for.is_available(11211) == False

def test_good_port_ranges():
    ranges = [
        (10, 15), # too short
        (100, 200), # good
        (220, 245), # a bit short
        (300, 330), # good
        (440, 495), # also good
    ]

    ports = ranges_to_set(ranges)
    good_ranges = port_for.good_port_ranges(ports, 20, 3)
    assert good_ranges == [(103, 197), (443, 492), (303, 327)], good_ranges

def test_something_works():
    assert len(port_for.good_port_ranges()) > 10
    assert len(port_for.available_good_ports()) > 1000

def test_binding():
    # low ports are not available
    assert port_for.port_is_used(10) == True

def test_binding_high():
    s = socket.socket()
    s.bind(("", 0))
    port = s.getsockname()[1]
    assert port_for.port_is_used(port) == True
    s.close()
    assert port_for.port_is_used(port) == False


class SelectPortTest(unittest.TestCase):

    @mock.patch('port_for.api.port_is_used')
    def test_all_used(self, port_is_used):
        port_is_used.return_value = True
        self.assertRaises(port_for.PortForException, port_for.select_random)

    @mock.patch('port_for.api.port_is_used')
    def test_random_port(self, port_is_used):
        ports = set([1, 2, 3])
        used = {1: True, 2:False, 3:True}
        port_is_used.side_effect = lambda port: used[port]

        for x in range(100):
            self.assertEqual(port_for.select_random(ports), 2)

class StoreTest(unittest.TestCase):

    def setUp(self):
        fd, self.fname = tempfile.mkstemp()
        self.store = port_for.PortStore(self.fname)

    def tearDown(self):
        os.remove(self.fname)

    def test_store(self):
        assert self.store.bound_ports() == []

        port = self.store.bind_port('foo')
        self.assertTrue(port)
        self.assertEqual(self.store.bound_ports(), [('foo', port)])
        self.assertEqual(port, self.store.bind_port('foo'))

        port2 = self.store.bind_port('aar')
        self.assertNotEqual(port, port2)
        self.assertEqual(self.store.bound_ports(), [('foo', port), ('aar', port2)])

        self.store.unbind_port('aar')
        self.assertEqual(self.store.bound_ports(), [('foo', port)])

    def test_rebind(self):
        # try to rebind an used port for an another app
        port = self.store.bind_port('foo')
        self.assertRaises(port_for.PortForException, self.store.bind_port, 'baz', port)

    def test_change_port(self):
        # changing app ports is not supported.
        port = self.store.bind_port('foo')
        another_port = port_for.select_random()
        assert port != another_port
        self.assertRaises(port_for.PortForException, self.store.bind_port, 'foo', another_port)

    def test_bind_unavailable(self):
        # it is possible to explicitly bind currently unavailable port
        port = self.store.bind_port('foo', 80)
        self.assertEqual(port, 80)
        self.assertEqual(self.store.bound_ports(), [('foo', 80)])

    def test_bind_non_auto(self):
        # it is possible to pass a port
        port = port_for.select_random()
        res_port = self.store.bind_port('foo', port)
        self.assertEqual(res_port, port)
