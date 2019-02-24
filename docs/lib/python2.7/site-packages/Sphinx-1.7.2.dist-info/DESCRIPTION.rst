========
 Sphinx
========

.. image:: https://img.shields.io/pypi/v/sphinx.svg
   :target: https://pypi.python.org/pypi/Sphinx
   :alt: Package on PyPi

.. image:: https://readthedocs.org/projects/sphinx/badge/
   :target: http://www.sphinx-doc.org/
   :alt: Documentation Status

.. image:: https://travis-ci.org/sphinx-doc/sphinx.svg?branch=master
   :target: https://travis-ci.org/sphinx-doc/sphinx
   :alt: Build Status (Travis CI)

.. image:: https://ci.appveyor.com/api/projects/status/github/sphinx-doc/sphinx?branch=master&svg=true
   :target: https://ci.appveyor.com/project/sphinxdoc/sphinx
   :alt: Build Status (AppVeyor)

.. image:: https://circleci.com/gh/sphinx-doc/sphinx.svg?style=shield
   :target: https://circleci.com/gh/sphinx-doc/sphinx
   :alt: Build Status (CircleCI)

.. image:: https://codecov.io/gh/sphinx-doc/sphinx/branch/master/graph/badge.svg
   :target: https://codecov.io/gh/sphinx-doc/sphinx
   :alt: Code Coverage Status (Codecov)

Sphinx is a tool that makes it easy to create intelligent and beautiful
documentation for Python projects (or other documents consisting of multiple
reStructuredText sources), written by Georg Brandl.  It was originally created
for the new Python documentation, and has excellent facilities for Python
project documentation, but C/C++ is supported as well, and more languages are
planned.

Sphinx uses reStructuredText as its markup language, and many of its strengths
come from the power and straightforwardness of reStructuredText and its parsing
and translating suite, the Docutils.

Among its features are the following:

* Output formats: HTML (including derivative formats such as HTML Help, Epub
  and Qt Help), plain text, manual pages and LaTeX or direct PDF output
  using rst2pdf
* Extensive cross-references: semantic markup and automatic links
  for functions, classes, glossary terms and similar pieces of information
* Hierarchical structure: easy definition of a document tree, with automatic
  links to siblings, parents and children
* Automatic indices: general index as well as a module index
* Code handling: automatic highlighting using the Pygments highlighter
* Flexible HTML output using the Jinja 2 templating engine
* Various extensions are available, e.g. for automatic testing of snippets
  and inclusion of appropriately formatted docstrings
* Setuptools integration

For more information, refer to the `the documentation`__.

.. __: http://www.sphinx-doc.org/

Installation
============

Sphinx is published on `PyPI`__ and can be installed from there::

   pip install -U sphinx

We also publish beta releases::

   pip install -U --pre sphinx

If you wish to install `Sphinx` for development purposes, refer to `the
contributors guide`__.

__ https://pypi.python.org/pypi/Sphinx
__ http://www.sphinx-doc.org/en/master/devguide.html

Documentation
=============

Documentation is available from `sphinx-doc.org`__.

__ http://www.sphinx-doc.org/

Testing
=======

Continuous testing is provided by `Travis`__ (for unit tests and style checks
on Linux), `AppVeyor`__ (for unit tests on Windows), and `CircleCI`__ (for
large processes like TeX compilation).

For information on running tests locally, refer to `the contributors guide`__.

__ https://travis-ci.org/sphinx-doc/sphinx
__ https://ci.appveyor.com/project/sphinxdoc/sphinx
__ https://circleci.com/gh/sphinx-doc/sphinx
__ http://www.sphinx-doc.org/en/master/devguide.html

Contributing
============

Refer to `the contributors guide`__.

__ http://www.sphinx-doc.org/en/master/devguide.html

Release signatures
==================

Releases are signed with following keys:

* `498D6B9E <https://pgp.mit.edu/pks/lookup?op=vindex&search=0x102C2C17498D6B9E>`_
* `5EBA0E07 <https://pgp.mit.edu/pks/lookup?op=vindex&search=0x1425F8CE5EBA0E07>`_


