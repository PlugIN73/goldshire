(defproject goldshire "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.apache.commons/commons-daemon "1.0.9"]
                 [docker "0.2.0"]
                 [org.clojure/data.json "0.2.4"]
                 [com.taoensso/carmine "2.6.0"]]
  :main goldshire.core
  :aot :all)
