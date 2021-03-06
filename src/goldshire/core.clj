(ns goldshire.core
  (:require [goldshire.docker :as docker-client])
  (:require [goldshire.redis :as redis-helper])
  (:require [docker.core :as docker])
  (:require [docker.image :as image])
  (:require [docker.container :as container])
  (:require [clojure.data.json :as json])
  (:import [org.apache.commons.daemon Daemon DaemonContext])
  (:use [clojure.java.shell :only [sh]])
  (:gen-class
    :implements [org.apache.commons.daemon.Daemon]))

(defn send-result
  [result, file-content, id, callback_url]
  (let [responce-body (json/write-str (array-map :result result,
                                                 :file-content file-content,
                                                 :id id,
                                                 :callback_url callback_url))]
    (redis-helper/set-worker responce-body)))

(defn ruby-eval
  "eval ruby expression on docker"
  [params]
  (let [box (container/create docker-client/client {:Hostname "127.0.0.1",
                                                    :Memory "10m",
                                                    :Image "paintedfox/ruby"
                                                    :Cmd ["ruby", "-e", (get params "code")]})
        callback_url (get params "callback_url")
        id (get params "id")]

    (container/start docker-client/client (:Id box) )
    (send-result (slurp (container/attach docker-client/client (:Id box) :logs true :stdout true :stderr true :stream true))
                 (get params "code")
                 id
                 callback_url)))

(defn c-eval
  "eval c++ expression on docker"
  [params]
  (let [file-name (clojure.string/join "" [(get params "id"),
                                           ".cpp"])
        result-name (get params "id")
        callback_url (get params "callback_url")
        id (get params "id")]
    (sh "sh" "-c" (clojure.string/join " "
                                       ["echo '",
                                        (get params "code"),
                                        "'>",
                                        file-name]))
    (let [f-compile (sh "gcc" file-name "-o" result-name)]
      (if (= (:exit f-compile) 0)
            ((sh "sh" "-c" (clojure.string/join " "
                                                ["chmod +x"
                                                 result-name]))
             (let [run (sh "sh" "-c" (clojure.string/join ""
                                                          ["./"
                                                           result-name]))]
               (if (:err run)
                 (send-result (:err run)
                              (:out (sh "cat" result-name))
                              id
                              callback_url)
                 (send-result (:out run)
                              (:out (sh "cat" result-name))
                              id
                              callback_url))))
               (send-result (:err f-compile)
                          (:out (sh "cat" result-name))
                          id
                          callback_url )))))

(defn get-code
  "parse params and return code field"
  [params]
  (json/read-str params))

(defn code-eval
  "handle eval code"
  [params]
  (if params
    (let [parsed-params (get-code params)]
      (cond
        (= (get parsed-params "lang") "ruby") (ruby-eval parsed-params)
        (= (get parsed-params "lang") "c++") (c-eval parsed-params)))
    (println "waiting")))

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

