(ns goldshire.docker
  (:require [docker.core :as docker])
  (:require [docker.image :as image])
  (:require [docker.container :as container]))

(def client  (docker/make-client "127.0.0.1:4243"))

(def box
  (container/create client {:Hostname "127.0.0.1",
                            :Memory "10m",
                            :Image "paintedfox/ruby",
                            :Cmd ["ruby", "-e", "puts 1 + 1"] }))
(def start-ruby-container
  (container/start client (:Id box)))

(def attach-stdout
  (container/attach client (:Id box) :logs true :stdout true :stderr true :stream true))
