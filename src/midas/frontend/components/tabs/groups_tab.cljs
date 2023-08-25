(ns midas.frontend.components.tabs.groups-tab
  (:require [re-frame.core :as rf]
            [midas.frontend.components.misc.searchable-select :refer [Searchable-select]]))



(defn active-group-ctrls [indx [g-name _ _]]
  (let [options @(rf/subscribe [:possible-groups])]
    [:li
     [:div
      (Searchable-select (map name (keys options)) (name g-name) :update-group indx)
      [:div {:on-click #(rf/dispatch [:remove-group indx])}
       "X"]
      [:div "DRAG"]]]))

(defn Groups-tab []
  (let [active-groups @(rf/subscribe [:active-groups])]
    [:div {:style {:display "flex" :flex-direction "column"}}
     [:div "Group by"]
     [:br]
     [:div {:style {:display "flex" :flex-direction "column" :justify-content "space-between"}}
      [:ul
       (map-indexed active-group-ctrls active-groups)]
      [:div {:on-click #(rf/dispatch [:add-group :month])}
       (str "+ Add " (if (empty? active-groups) "Group" "Subgroup"))]]]))