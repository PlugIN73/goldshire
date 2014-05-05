(ns goldshire.core
  (:require [goldshire.docker :as docker-client]))


(defn ruby-eval
  "eval ruby expression on docker"
  [cmd]
  docker-client/start-ruby-container
  (slurp docker-client/attach-stdout))
