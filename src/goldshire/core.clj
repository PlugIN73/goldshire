(ns goldshire.core
  (:require [goldshire.docker :as docker-client])
  (:require [goldshire.redis :as redis-helper])
  (:require [docker.core :as docker])
  (:require [docker.image :as image])
  (:require [docker.container :as container])
  (:import [org.apache.commons.daemon Daemon DaemonContext])
  (:gen-class
    :implements [org.apache.commons.daemon.Daemon]))

(defn send-result
  [result]
  (redis-helper/set-worker result))

(defn ruby-eval
  "eval ruby expression on docker"
  [cmd]
  (let [box (container/create docker-client/client {:Hostname "127.0.0.1",
                                                    :Memory "10m",
                                                    :Image "paintedfox/ruby"
                                                    :Cmd ["ruby", "-e", cmd]})]

    (container/start docker-client/client (:Id box) )
    (send-result (slurp (container/attach docker-client/client (:Id box) :logs true :stdout true :stderr true :stream true)))))

(defn get-code
  "parse params and return code field"
  [params]
  (let [code (nth
              (clojure.string/split
                (nth (clojure.string/split params #",\"") 0)
                #"\":")
              1)]
    (clojure.core/subs code 1 (- (clojure.core/count code) 1))))

(defn code-eval
  "handle eval code"
  [params]
  (if params
    (ruby-eval (get-code params))
    (println "waiting"))
  (println "served"))

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

