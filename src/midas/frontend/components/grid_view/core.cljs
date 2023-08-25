(ns midas.frontend.components.grid-view.core
  (:require [re-frame.core :as rf]))

(def TEST_COLUMNS ["Description" "Amount" "Date" "Tags"])


(defn group-header-comp [{:keys [g-name g-val cnt collapsed?]}]
  [:div {:style {:display "flex"
                 :flex-direction "row"
                 :min-width "300px"
                 :align-items "center"
                 :justify-content "space-between"}}
   [:div {:style {:font-size "xx-large"
                  :padding-left "15px"}}
    (if collapsed? ">" "v")]
   [:div {:style {:display "flex"
                  :flex-direction "column"
                  :width "100%"
                  :padding-left "15px"}}
    [:p g-name]
    [:p g-val]]
   [:div {:style {:display "flex" :flex-direction "row" :padding-right "15px"}}
    [:p "Count:"]
    [:p cnt]]])

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
   [:td (group-header-comp group-header)]
   [:td {:colspan 3}]])



(defn Grid-view [data]
  [:div
   [:table {:cellspacing "0"}
    [:thead
     [:tr
      (for [column TEST_COLUMNS]
        ^{:key column}
        [:th column])]]
    [:tbody
     (map (fn [row]
            (case (:type row)
              :item (item-row row)
              :spacer (spacer-row row)
              :group-header (group-header-row row)))
          data)]]])