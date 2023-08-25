(ns midas.frontend.components.misc.searchable-select
  (:require [re-frame.core :as rf]
            [midas.frontend.components.misc.prefix-tree-options :refer [Prefix-options]]))


;####################################################
; Imagining the top level for a sec
;####################################################

;; (defn Group-Tab []
;;   (let [active-groups @(rf/subscribe [:active-groups])
;;         show-modal? @(rf/subscribe [:modal/show? :groups-tab-modal])]
;;     [:div 
;;      [:div (if (empty? active-groups) 
;;              "Group" 
;;              (str "Grouped by " (count active-groups) " field"))]
;;      (if show-modal?
;;        (Modal :groups-tab-modal (Groups-tab)))]))

;####################################################
;
;####################################################

;; (defn test-select [options v event id]
;;   (let [show-modal? @(rf/subscribe [:modal/show? id])]
;;     [:div
;;      [:div v]
;;      [:div "V"]
;;      (if show-modal?
;;        (Prefix-options))]))


;TODO: do the searchable part
(defn Searchable-select [options v event indx]
  [:select {:value v
            :on-change #(rf/dispatch [event indx (-> % .-target .-value keyword)])}
   (for [option options]
     ^{:key option}
     [:option {:value option} option])])