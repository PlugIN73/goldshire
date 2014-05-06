(ns goldshire.redis
  (:require [taoensso.carmine :as car :refer (wcar)]))

(def server1-conn {:pool {<opts>} :spec {<opts>}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(wcar* (car/ping)
       (car/set "foo" "bar")
       (car/get "foo"))

