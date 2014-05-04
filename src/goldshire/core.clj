(ns goldshire.core
  (:require [docker.core :as docker]))

(def client  (make-client "10.0.1.2:4243"))
(docker/version client)
(docker/info client)

(:require [docker.image :as image])

(defn ruby-eval
  "eval ruby expression on docker"
  [exp]
  2)

