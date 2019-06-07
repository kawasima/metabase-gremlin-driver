(ns metabase.driver.gremlin.query-processor
  (:require [metabase.query-processor
             [interface :as i]
             [store :as qp.store]]
            [metabase.driver.gremlin.util :refer [*gremlin-client*]])
  (:import [org.apache.tinkerpop.gremlin.structure Vertex Edge]))

(def ^:dynamic ^:private *query* nil)
(defn mbql->native
  "Process and run an MBQL query."
  [{{source-table-id :source-table} :query, :as query}]
  (let [{source-table-name :name} (qp.store/table source-table-id)]
    (binding [*query* query]
      {})))

(defn- element->map [vertex]
  (->> (.properties vertex (into-array String nil))
       iterator-seq
       (reduce (fn [m prop](assoc m (.key prop) (.value prop))) {})))

(defn- map->row [m columns]
  (for [col columns]
    (get m col)))

(defn- unnested-seq [s]
  (if (and (seq? s) (= (count s) 1) (seq? (first s)))
    (first s)
    s))

(defn- element-map->result-map [m]
  (->> (.entrySet (.get m "properties"))
       (map (fn [e] [(.getKey e) (->> (.getValue e)
                                      (map #(.get % "value"))
                                      (clojure.string/join ","))]))
       (reduce #(assoc %1 (first %2) (second %2)) {})))

(defmulti parse-map (fn [m] (.get m "type")))

(defmethod parse-map "vertex" [m]
  (element-map->result-map m))

(defmethod parse-map "edge" [m]
  (element-map->result-map m))

(defmethod parse-map :default [m]
  (for [k (.keySet m)]
    {"item" k, "count" (.get m k)}))

(defn gremlin-query-results [query]
  (let [result-seq (-> (.submit *gremlin-client* query)
                       .iterator
                       iterator-seq)
        _ (println result-seq)
        results (->> result-seq
                     (map #(condp instance? (.getObject %)
                             String        {"item" (.getString %)}
                             Integer       {"item" (.getInt %)}
                             Short         {"item" (.getShort %)}
                             Long          {"item" (.getLong %)}
                             Double        {"item" (.getDouble %)}
                             Float         {"item" (.getFloat %)}
                             Boolean       {"item" (.getBoolean %)}
                             Edge          (element->map (.getEdge %))
                             Vertex        (element->map (.getVertex %))
                             java.util.Map (parse-map (.getObject %))
                             (.getObject %)))
                     unnested-seq
                     vec)

        columns (->> results
                     (map keys)
                     (reduce #(apply conj %1 %2) #{})
                     seq)

        rows (->> results
                  (map #(if (map? %) (map->row % columns) %)))]
    {:rows rows
     :columns columns}
    ))
