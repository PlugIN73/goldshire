(ns goldshire.core
  (:require [goldshire.docker :as docker-client])
  (:require [goldshire.redis :as redis-helper])
  (:import [org.apache.commons.daemon Daemon DaemonContext])
  (:gen-class
    :implements [org.apache.commons.daemon.Daemon]))

(defn ruby-eval
  "eval ruby expression on docker"
  [cmd]
  docker-client/start-ruby-container
  (println (slurp docker-client/attach-stdout)))

(defn code-eval
  "handle eval code"
  [params]
  (println
    (clojure.string/join
       (nth
         (clojure.string/split (nth (clojure.string/split params #",\"") 0) #"\":")
         1)
     " ")))
  ;(cond
    ;(= lang "ruby") (ruby-eval cmd)))

(def state (atom {}))

(defn init [args]
  (swap! state assoc :running true))

(defn start []
  (while (:running @state)
    (println "tick")
    (println (redis-helper/get-worker code-eval))
    (println "tick")
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

