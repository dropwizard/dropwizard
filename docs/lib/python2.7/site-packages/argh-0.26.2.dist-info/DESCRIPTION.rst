Argh: The Natural CLI
=====================

.. image:: https://img.shields.io/coveralls/neithere/argh.svg
    :target: https://coveralls.io/r/neithere/argh

.. image:: https://img.shields.io/travis/neithere/argh.svg
    :target: https://travis-ci.org/neithere/argh

.. image:: https://img.shields.io/pypi/format/argh.svg
    :target: https://pypi.python.org/pypi/argh

.. image:: https://img.shields.io/pypi/status/argh.svg
    :target: https://pypi.python.org/pypi/argh

.. image:: https://img.shields.io/pypi/v/argh.svg
    :target: https://pypi.python.org/pypi/argh

.. image:: https://img.shields.io/pypi/pyversions/argh.svg
    :target: https://pypi.python.org/pypi/argh

.. image:: https://img.shields.io/pypi/dd/argh.svg
    :target: https://pypi.python.org/pypi/argh

.. image:: https://readthedocs.org/projects/argh/badge/?version=stable
    :target: http://argh.readthedocs.org/en/stable/

.. image:: https://readthedocs.org/projects/argh/badge/?version=latest
    :target: http://argh.readthedocs.org/en/latest/

Building a command-line interface?  Found yourself uttering "argh!" while
struggling with the API of `argparse`?  Don't like the complexity but need
the power?

.. epigraph::

    Everything should be made as simple as possible, but no simpler.

    -- Albert Einstein (probably)

`Argh` is a smart wrapper for `argparse`.  `Argparse` is a very powerful tool;
`Argh` just makes it easy to use.

In a nutshell
-------------

`Argh`-powered applications are *simple* but *flexible*:

:Modular:
    Declaration of commands can be decoupled from assembling and dispatching;

:Pythonic:
    Commands are declared naturally, no complex API calls in most cases;

:Reusable:
    Commands are plain functions, can be used directly outside of CLI context;

:Layered:
    The complexity of code raises with requirements;

:Transparent:
    The full power of argparse is available whenever needed;

:Namespaced:
    Nested commands are a piece of cake, no messing with subparsers (though
    they are of course used under the hood);

:Term-Friendly:
    Command output is processed with respect to stream encoding;

:Unobtrusive:
    `Argh` can dispatch a subset of pure-`argparse` code, and pure-`argparse`
    code can update and dispatch a parser assembled with `Argh`;

:DRY:
    The amount of boilerplate code is minimal; among other things, `Argh` will:

    * infer command name from function name;
    * infer arguments from function signature;
    * infer argument type from the default value;
    * infer argument action from the default value (for booleans);
    * add an alias root command ``help`` for the ``--help`` argument.

:NIH free:
    `Argh` supports *completion*, *progress bars* and everything else by being
    friendly to excellent 3rd-party libraries.  No need to reinvent the wheel.

Sounds good?  Check the tutorial!

Relation to argparse
--------------------

`Argh` is fully compatible with `argparse`.  You can mix `Argh`-agnostic and
`Argh`-aware code.  Just keep in mind that the dispatcher does some extra work
that a custom dispatcher may not do.

Installation
------------

Using pip::

    $ pip install argh

Arch Linux (AUR)::

    $ yaourt python-argh

Examples
--------

A very simple application with one command:

.. code-block:: python

    import argh

    def main():
        return 'Hello world'

    argh.dispatch_command(main)

Run it:

.. code-block:: bash

    $ ./app.py
    Hello world

A potentially modular application with multiple commands:

.. code-block:: python

    import argh

    # declaring:

    def echo(text):
        "Returns given word as is."
        return text

    def greet(name, greeting='Hello'):
        "Greets the user with given name. The greeting is customizable."
        return greeting + ', ' + name

    # assembling:

    parser = argh.ArghParser()
    parser.add_commands([echo, greet])

    # dispatching:

    if __name__ == '__main__':
        parser.dispatch()

Of course it works:

.. code-block:: bash

    $ ./app.py greet Andy
    Hello, Andy

    $ ./app.py greet Andy -g Arrrgh
    Arrrgh, Andy

Here's the auto-generated help for this application (note how the docstrings
are reused)::

    $ ./app.py help

    usage: app.py {echo,greet} ...

    positional arguments:
        echo        Returns given word as is.
        greet       Greets the user with given name. The greeting is customizable.

...and for a specific command (an ordinary function signature is converted
to CLI arguments)::

    $ ./app.py help greet

    usage: app.py greet [-g GREETING] name

    Greets the user with given name. The greeting is customizable.

    positional arguments:
      name

    optional arguments:
      -g GREETING, --greeting GREETING   'Hello'

(The help messages have been simplified a bit for brevity.)

`Argh` easily maps plain Python functions to CLI.  Sometimes this is not
enough; in these cases the powerful API of `argparse` is also available:

.. code-block:: python

    @arg('text', default='hello world', nargs='+', help='The message')
    def echo(text):
        print text

The approaches can be safely combined even up to this level:

.. code-block:: python

    # adding help to `foo` which is in the function signature:
    @arg('foo', help='blah')
    # these are not in the signature so they go to **kwargs:
    @arg('baz')
    @arg('-q', '--quux')
    # the function itself:
    def cmd(foo, bar=1, *args, **kwargs):
        yield foo
        yield bar
        yield ', '.join(args)
        yield kwargs['baz']
        yield kwargs['quux']

Links
-----

* `Project home page`_ (GitHub)
* `Documentation`_ (Read the Docs)
* `Package distribution`_ (PyPI)
* Questions, requests, bug reports, etc.:

  * `Issue tracker`_ (GitHub)
  * `Mailing list`_ (subscribe to get important announcements)
  * Direct e-mail (neithere at gmail com)

.. _project home page: http://github.com/neithere/argh/
.. _documentation: http://argh.readthedocs.org
.. _package distribution: http://pypi.python.org/pypi/argh
.. _issue tracker: http://github.com/neithere/argh/issues/
.. _mailing list: http://groups.google.com/group/argh-users

Author
------

Developed by Andrey Mikhaylenko since 2010.

See file `AUTHORS` for a complete list of contributors to this library.

Support
-------

The fastest way to improve this project is to submit tested and documented
patches or detailed bug reports.

Otherwise you can "flattr" me: |FlattrLink|_

.. _FlattrLink: https://flattr.com/submit/auto?user_id=neithere&url=http%3A%2F%2Fpypi.python.org%2Fpypi%2Fargh
.. |FlattrLink| image:: https://api.flattr.com/button/flattr-badge-large.png
   :alt: Flattr the Argh project

Licensing
---------

Argh is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Argh is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Argh.  If not, see <http://gnu.org/licenses/>.


