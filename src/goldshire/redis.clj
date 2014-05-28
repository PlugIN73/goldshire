(ns goldshire.redis
  (:require [taoensso.carmine :as car :refer (wcar)])
  (:require [taoensso.carmine.message-queue :as car-mq]))

(def server-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})
(defmacro wcar* [& body] `(car/wcar server-conn ~@body))

(defn get-worker
  [callback]
  (callback (wcar* (car/rpop "code"))))

(defn queue-worker
  [callback]
  (car-mq/worker server-conn "code"
   {:handler (fn [{:keys [message attempt]}]
               (println "Received" message)
               (callback "ruby" message)
               {:status :success})}))

(defn enqueue
  [queue, msg]
  (wcar* (car-mq/enqueue queue msg)))

