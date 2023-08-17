(ns midas.backend.main.core
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [ring.util.response :refer [resource-response]]))

(defn handler [request]
  (let [path (:uri request)]
    (if (= path "/")
      (resource-response "index.html" {:root "public"})
      (resource-response path {:root "public"}))))

(defn -main [& args]
  (jetty/run-jetty handler {:port 3000}))
