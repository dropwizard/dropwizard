.. image:: https://travis-ci.org/agronholm/pythonfutures.svg?branch=master
  :target: https://travis-ci.org/agronholm/pythonfutures
  :alt: Build Status

This is a backport of the `concurrent.futures`_ standard library module to Python 2.

It should not be installed on Python 3, although there should be no harm in doing so, as the
standard library takes precedence over third party libraries.

To conditionally require this library only on Python 2, you can do this in your ``setup.py``:

.. code-block:: python

    setup(
        ...
        extras_require={
            ':python_version == "2.7"': ['futures']
        }
    )

Or, using the newer syntax:

.. code-block:: python

    setup(
        ...
        install_requires={
            'futures; python_version == "2.7"'
        }
    )

.. warning:: The ``ProcessPoolExecutor`` class has known (unfixable) problems on Python 2 and
  should not be relied on for mission critical work.

.. _concurrent.futures: https://docs.python.org/library/concurrent.futures.html


