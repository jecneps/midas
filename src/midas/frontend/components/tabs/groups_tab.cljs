(ns midas.frontend.components.tabs.groups-tab
  (:require [re-frame.core :as rf]
            [midas.frontend.components.misc.searchable-select :refer [Searchable-select]]
            [midas.frontend.components.misc.modal :refer [Modal]]
            [midas.frontend.components.misc.prefix-tree-options :refer [Prefix-options]]))









(defn add-groups [active-groups unused-options]
  (let [show-modal? @(rf/subscribe [:modal/show? :modal/add-groups])
        selection @(rf/subscribe [:pto/selection :modal/add-groups])]
    [:div {:class "modal-parent group-tab-add"
           :on-click (if show-modal?
                       (fn [_] nil)
                       (fn [_]
                         (rf/dispatch [:pto/init :modal/add-groups unused-options])
                         (rf/dispatch [:modal/show :modal/add-groups])))}
     (str "+ Add " (if (empty? active-groups) "Group" "Subgroup"))
     (if show-modal?
       (Modal :modal/add-groups show-modal? (Prefix-options :modal/add-groups)))
     (if (and (not (nil? selection)) show-modal?)
       (do
         (rf/dispatch [:modal/close :modal/add-groups])
         (rf/dispatch [:add-group (first selection)])))]))


(defn active-group-ctrls [indx [g-name _ _]]
  (let [options @(rf/subscribe [:unused-options])
        id (keyword (str "modal/change-group-" indx))]
    [:li
     [:div {:class "group-ctrl-row"}
      (Searchable-select id [g-name (name g-name)] options [:update-group indx])
      [:div {:on-click #(rf/dispatch [:remove-group indx])}
       "X"]]]))

(defn Groups-tab []
  (let [active-groups @(rf/subscribe [:active-groups])
        unused-options @(rf/subscribe [:unused-options])
        all-group-headers @(rf/subscribe [:group/all-group-headers])]
    [:div {:class "group-tab-container"}
     [:div {:class "group-tab-header"}

      [:div {:class "group-tab-title"}
       "Group by"]
      [:div {:class "group-tab-header-buttons"}
       [:div {:class "group-tab-header-button"
              :on-click (fn [] (rf/dispatch [:group/collapse-all all-group-headers]))}
        "Collapse All"]
       [:div {:class "group-tab-header-button"
              :on-click (fn [] (rf/dispatch [:group/expand-all]))}
        "Expand All"]]]
     [:hr]
     [:div {:style {:display "flex" :flex-direction "column" :justify-content "space-between"}}
      [:ul
       (doall (map-indexed active-group-ctrls active-groups))]
      (add-groups active-groups unused-options)]]))