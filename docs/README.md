# Dropwizard documentation

## Building locally

Create and enter the Python virtual environment:

    # virtualenv .
    # source ./bin/activate

Install [Sphinx](http://sphinx-doc.org) and all required dependencies:

    # pip install -r requirements.txt

Build the static documentation and open it in your browser:

    # make html
    # open target/html/index.html

Build the documentation and automatically build them on any change:

    # make livehtml
    # open http://127.0.0.1:8000/
