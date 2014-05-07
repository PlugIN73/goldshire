(ns goldshire.redis
  (:require [taoensso.carmine :as car :refer (wcar)])
  (:require [taoensso.carmine.message-queue :as car-mq]))

(def server-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})
(defmacro wcar* [& body] `(car/wcar server-conn ~@body))

(defn queue-worker
  []
  (car-mq/worker server-conn "code"
   {:handler (fn [{:keys [message attempt]}]
               (println "Received" message)
               {:status :success})}))

(defn enqueue
  [queue, msg]
  (wcar* (car-mq/enqueue queue msg)))

