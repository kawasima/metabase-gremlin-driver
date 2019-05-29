(ns tinkerpop.graph-server
  (:require [duct.logger :as logger]
            [integrant.core :as ig])
  (:import [org.apache.tinkerpop.gremlin.server GremlinServer Settings]))

(defmethod ig/init-key :tinkerpop/graph-server [_ {:keys [logger] :as opts}]
  (let [logger (atom logger)
        options (dissoc opts :logger)
        server (GremlinServer. (Settings.))]
    (.start server)
    (logger/log @logger :report ::starting-server)
    {:logger logger
     :server server}))

(defmethod ig/halt-key! :tinkerpop/graph-server [_ {:keys [logger server]}]
  (logger/log @logger :report ::stopping-server)
  (.stop server))
