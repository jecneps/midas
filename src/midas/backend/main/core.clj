(ns midas.backend.main.core
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [reitit.core :as r]
            [reitit.ring :as ring]
            [ring.util.response :refer [resource-response]]
            [midas.database.core :as db]
            [midas.router.core :refer [routes]]))

(defn my-expand [registry]
  (fn [data opts]
    ;; (println "data: " data)
    ;; (println "opts: " opts)
    ;; (let [name (if (keyword? data) data (:name data))]
    ;;   (if))
    (if (keyword? data)
      (if-let [d (registry data)]
        (assoc d :name data)
        (r/expand data opts))
      (if-let [m (registry (:name data))]
        (merge data (r/expand m opts))
        (r/expand data opts)))
    ;; (if (keyword? data)
    ;;   (some-> data
    ;;           registry
    ;;           (r/expand opts)
    ;;           (assoc :name data))

    ;;   (r/expand data opts))
    ))

(defn read-line-items [_] (println "get-line-items handler") {:status 200, :body (prn-str (db/read-line-items))})
(defn read-tags [_] {:status 200 :body (prn-str (db/read-tags))})
(defn pages [_] (println "pages handler") (resource-response "index.html" {:root "public"}))
(defn default-handler [request] (println "default handler: " (:uri request)) (resource-response (:uri request) {:root "public"}))

(def router (ring/router routes {:expand (my-expand {:api/read-line-items {:get read-line-items}
                                                     :api/read-tags {:get read-tags}
                                                     :pages/feed {:get pages}
                                                     :pages/data-input {:get pages}
                                                     :pages/home {:get pages}})}))

(println (r/routes router))

(def app
  (ring/ring-handler
   router
   default-handler))

(defn -main [& args]
  (jetty/run-jetty app {:port 3000}))
