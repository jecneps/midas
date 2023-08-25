(ns midas.frontend.components.tabs.groups-tab
  (:require [re-frame.core :as rf]
            [midas.frontend.components.misc.searchable-select :refer [Searchable-select]]
            [midas.frontend.components.misc.modal :refer [Modal]]
            [midas.frontend.components.misc.prefix-tree-options :refer [Prefix-options]]))


(defn add-groups [active-groups unused-options]
  (let [show-modal? @(rf/subscribe [:modal/show? :modal/add-groups])
        selection @(rf/subscribe [:pto/selection :modal/add-groups])]
    (println "SELECTION: " selection)
    [:div {:on-click (if show-modal?
                       (fn [_] nil)
                       (fn [_]
                         (rf/dispatch [:pto/init-pto :modal/add-groups unused-options])
                         (rf/dispatch [:modal/show :modal/add-groups])))}
     (str "+ Add " (if (empty? active-groups) "Group" "Subgroup"))
     (if show-modal?
       (Modal :modal/add-groups (Prefix-options :modal/add-groups)))
     (if (and (not (nil? selection)) show-modal?)
       (do
         (rf/dispatch [:modal/close :modal/add-groups])
         (rf/dispatch [:add-group (first selection)])))]))


(defn active-group-ctrls [indx [g-name _ _]]
  (let [options @(rf/subscribe [:possible-groups])]
    [:li
     [:div
      (Searchable-select (map name (keys options)) (name g-name) :update-group indx)
      [:div {:on-click #(rf/dispatch [:remove-group indx])}
       "X"]
      [:div "DRAG"]]]))

(defn Groups-tab []
  (let [active-groups @(rf/subscribe [:active-groups])
        unused-options @(rf/subscribe [:unused-options])]
    [:div {:style {:display "flex" :flex-direction "column"}}
     [:div "Group by"]
     [:br]
     [:div {:style {:display "flex" :flex-direction "column" :justify-content "space-between"}}
      [:ul
       (map-indexed active-group-ctrls active-groups)]
      (add-groups active-groups unused-options)]]))