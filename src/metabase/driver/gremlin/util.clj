(ns metabase.driver.gremlin.util
  (:import [org.apache.tinkerpop.gremlin.driver Cluster Client]
           [org.apache.tinkerpop.gremlin.driver.ser GraphSONMessageSerializerV2d0]))

(def ^:dynamic ^org.apache.tinkerpop.gremlin.driver.Client *gremlin-client* nil)

(defn -with-gremlin-client
  [f database]
  (let [builder (-> (Cluster/build)
                    (.port            (-> (get-in database [:details :port] "8182") bigint int))
                    (.addContactPoint (get-in database [:details :host]))
                    (.enableSsl       (not= (get-in database [:details :protocol] "http") "http"))
                    (.serializer      (GraphSONMessageSerializerV2d0.)))
        username (get-in database [:details :username])
        password (get-in database [:details :password])]
    (when (and username password)
      (.credentials builder username password))
    (when-let [client (.connect (.create builder))]
      (try
        (binding [*gremlin-client* client]
          (f *gremlin-client*))
        (finally (.close client))))))

(defmacro with-gremlin-client
  "Open a new Gremlin client"
  [[binding database] & body]
  `(let [f# (fn [~binding]
             ~@body)]
    (if *gremlin-client*
      (f# *gremlin-client*)
      (-with-gremlin-client f# ~database))))
