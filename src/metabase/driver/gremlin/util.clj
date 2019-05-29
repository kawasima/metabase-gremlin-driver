(ns metabase.driver.gremlin.util
  (:import [org.apache.tinkerpop.gremlin.driver Cluster Client]
           [org.apache.tinkerpop.gremlin.driver.ser GraphSONMessageSerializerV2d0]))

(def ^:dynamic ^org.apache.tinkerpop.gremlin.driver.Client *gremlin-client* nil)

(defn -with-gremlin-client
  [f database]
  (let [cluster (-> (Cluster/build)
                    (.port            (get database :port 8182))
                    (.addContactPoint (get database :host))
                    (.enableSsl       (get database :ssl? false))
                    (.serializer      (GraphSONMessageSerializerV2d0.))
;                    (.credentials username password)
                    (.create))
        client (.connect cluster)]
    (try
      (binding [*gremlin-client* client]
        (f *gremlin-client*))
      (finally (.close client)))))

(defmacro with-gremlin-client
  "Open a new Gremlin client"
  [[binding database] & body]
  `(let [f# (fn [~binding]
             ~@body)]
    (if *gremlin-client*
      (f# *gremlin-client*)
      (-with-gremlin-client f# ~database))))
