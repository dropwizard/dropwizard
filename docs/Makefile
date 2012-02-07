build:
	mook

upload: build
	rsync -avz target/ codahale.com:/home/codahale/dropwizard.codahale.com/
