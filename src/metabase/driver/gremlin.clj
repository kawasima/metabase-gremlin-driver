(ns metabase.driver.gremlin
  (:refer-clojure :exclude [second])
  (:require [metabase.driver :as driver]
            [metabase.driver.common :as driver.common]
            [metabase.util
             [date :as du]]
            [metabase.query-processor.store :as qp.store]
            [metabase.driver.gremlin
             [query-processor :as qp]
             [util :refer [with-gremlin-client]]]))

(driver/register! :gremlin)

(defmethod driver/display-name :gremlin [_]
  "Gremlin")

(defmethod driver/supports? [:gremlin :basic-aggregations] [_ _] false)

(defmethod driver/can-connect? :gremlin [_ details]
  true)

(defmethod driver/process-query-in-context :gremlin [_ qp]
  (fn [{:as query}]
    (with-gremlin-client [_ (qp.store/database)]
      (qp query))))

(defmethod driver/mbql->native :gremlin [_ query]
  (println "mbql-query: " query)
  (qp/mbql->native query))

(defmethod driver/execute-query :gremlin [_ {{query :query} :native, :as native-query}]
  (println "native-query:" native-query)
  (try
    (qp/gremlin-query-results query)
    (catch Exception e
      (.printStackTrace e)
      (throw e)))
  )
