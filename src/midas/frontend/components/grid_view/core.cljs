(ns midas.frontend.components.grid-view.core
  (:require [re-frame.core :as rf]))

(def TEST_COLUMNS ["Description" "Amount" "Date" "Tags"])



(defn item-rows [items]
  (for [item items]

    ^{:key (str (:id item))}
    [:tr
     [:td (:description item)]
     [:td (:amount item)]
     [:td (:date item)]
     [:td (clojure.string/join ", " (:tags item))]]))

(defn item-row [item]
  ^{:key (str (:id item))}
  [:tr
   [:td (:description item)]
   [:td (:amount item)]
   [:td (:date item)]
   [:td (clojure.string/join ", " (:tags item))]])

(defn spacer-row [spacer]
  [:tr {:style {:background-color "lightgray"}}
   [:td {:colspan 4}]])

(defn group-header-row [group-header]
  ^{:key (str group-header)}
  [:tr {:style {:font-weight "bold" :background-color (if (:collapsed? group-header) "red" "green")}
        :on-click #(rf/dispatch [:toggle-group-header (:id group-header)])}
   [:td (str (:name group-header) " " (:g-val group-header))]
   [:td {:colspan 3}]])



(defn Grid-view [data]
  [:div
   [:table
    [:thead
     [:tr
      (for [column TEST_COLUMNS]
        ^{:key column}
        [:th column])]]
    [:tbody
     (map (fn [row]
            (println "row=" row)
            (case (:type row)
              :item (item-row row)
              :spacer (spacer-row row)
              :group-header (group-header-row row)))
          data)]]])