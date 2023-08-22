(ns midas.frontend.components.itemFeed.core
  (:require [re-frame.core :as rf]))

(defn feed [items]
  [:div
   [:table
    [:thead
     [:tr
      [:th "Date"]
      [:th "Amount"]
      [:th "Description"]
      [:th "Tags"]]]
    [:tbody
     (for [item items]
       ^{:key (str (:id item))}
       [:tr
        [:td (:date item)]
        [:td (:amount item)]
        [:td (:description item)]
        [:td (clojure.string/join ", " (:tags item))]])]]])