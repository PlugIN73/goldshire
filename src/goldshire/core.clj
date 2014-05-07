(ns goldshire.core
  (:require [goldshire.docker :as docker-client])
  (:require [goldshire.redis :as redis-helper])
  (:import [org.apache.commons.daemon Daemon DaemonContext])
  (:gen-class
    :implements [org.apache.commons.daemon.Daemon]))

(def state (atom {}))

(defn init [args]
  (swap! state assoc :running true))

(defn start []
  (redis-helper/queue-worker)
  (while (:running @state)
    (println "tick")
    (redis-helper/enqueue "code" "puts 1 + 1")
    (Thread/sleep 2000)))

(defn stop []
  (swap! state assoc :running false))


(defn -init [this ^DaemonContext context]
  (init (.getArguments context)))

(defn -start [this]
  (future (start)))

(defn -stop [this]
  (stop))

(defn -main [& args]
  (init args)
  (start))

(defn ruby-eval
  "eval ruby expression on docker"
  [cmd]
  docker-client/start-ruby-container
  (slurp docker-client/attach-stdout))
