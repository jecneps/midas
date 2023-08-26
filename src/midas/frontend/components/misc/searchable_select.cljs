(ns midas.frontend.components.misc.searchable-select
  (:require [re-frame.core :as rf]
            [midas.frontend.components.misc.prefix-tree-options :refer [Prefix-options]]
            [midas.frontend.components.misc.modal :refer [Modal]]))





(defn Searchable-select [id cur-option options on-select-event]
  (let [show-modal? @(rf/subscribe [:modal/show? id])
        selection @(rf/subscribe [:pto/selection id])]
    (println "SS: selection=" selection)
    [:div {:class "searchable-select-container modal-parent"
           :on-click (if show-modal?
                       (fn [_] nil)
                       (fn [_]
                         (rf/dispatch [:pto/init id options])
                         (rf/dispatch [:modal/show id])))}
     [:div (second cur-option)]
     [:div "V"]
     (cond
       (and (nil? selection) show-modal?) (Modal id show-modal? (Prefix-options id))
       (and (nil? selection) (not show-modal?)) nil
       (and (not (nil? selection)) show-modal?) (do
                                                  (rf/dispatch [:modal/close id])
                                                  (rf/dispatch [:pto/remove id])
                                                  (rf/dispatch (conj on-select-event (first selection)))))]))