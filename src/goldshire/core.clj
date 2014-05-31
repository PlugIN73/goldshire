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



(defn get-code
  "parse params and return code field"
  [params]
  (str
    (nth
      (clojure.string/split
        (nth
          (clojure.string/split
            (nth
              (clojure.string/split (nth (clojure.string/split params #",\"") 0) #"\":")
              1)
            #"\"")
          3)
        #"\\")
      0)))

(defn code-eval
  "handle eval code"
  [params]
  (if params
    (ruby-eval (get-code params))
    (println "waiting")))
;" ")))
;(cond
;(= lang "ruby") (ruby-eval cmd)))
;(ruby-eval "puts 1 + 1"))

(def state (atom {}))

(defn init [args]
  (swap! state assoc :running true))

(defn start []
  (while (:running @state)
    (println "tick")
    (redis-helper/get-worker code-eval)
    (println "tick")
    (Thread/sleep 1000)))

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

