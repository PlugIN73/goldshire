# goldshire

A Clojure library designed to eval code on High-level programming languages. (Work In Progress)

## Usage

### Build daemon

    $ lein uberjar


### Run daemon

    $ sudo docker -H 127.0.0.1:4243 -H unix:///var/run/docker.sock -d &
    $ sudo jsvc  -home /usr/lib/jvm/java-7-oracle \
                 -cp "$(pwd)/target/goldshire-0.1.0-SNAPSHOT-standalone.jar" \
                 -outfile "$(pwd)/out.txt" goldshire.core
    $ tail -f out.txt

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
