(ns midas.frontend.main.core
  (:require [reagent.dom :as rdom]))

;; define your app data so that it doesn't get over-written on reload

(defn hello-world []
  [:div
   [:h1 "Shadow-lein Fullstack Template"]])

(defn start []
  (rdom/render [hello-world]
               (. js/document (getElementById "app"))))

(defn ^:export init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (start))

(defn stop []
  ;; stop is called before any code is reloaded
  ;; this is controlled by :before-load in the config
  (js/console.log "stop"))

(init)
