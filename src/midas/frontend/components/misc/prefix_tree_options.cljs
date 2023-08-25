(ns midas.frontend.components.misc.prefix-tree-options
  (:require [re-frame.core :as rf]
            [clojure.string]))


;; {:options [[k s] [k s]...]
;;  :cur-text "..."
;;  :focused-option
;;  :selection [k s] or nil}

;; options are [key str] pairs

(rf/reg-event-fx
 :pto/init-pto
 (fn [{:keys [db]} [_ id options]]
   {:db (assoc-in db [:prefix-tree-options id] {:options options
                                                :cur-text ""
                                                :selection nil
                                                :focused-option nil})}))

(rf/reg-event-db
 :pto/clear-text
 (fn [db [_ id]]
   (update-in db [:prefix-tree-options id :cur-text] (constantly ""))))

(rf/reg-event-db
 :pto/text-changed
 (fn [db [_ id text]]
   (assoc-in db [:prefix-tree-options id :cur-text] text)))

(rf/reg-event-db
 :pto/option-selected
 (fn [db [_ id option]]
   (assoc-in db [:prefix-tree-options id :selection] option)))


;;#######################################################################

(rf/reg-sub
 :pto/selection
 (fn [db [_ id]]
   (get-in db [:prefix-tree-options id :selection])))

(rf/reg-sub
 :pto/cur-text
 (fn [db [_ id]]
   (get-in db [:prefix-tree-options id :cur-text])))

(rf/reg-sub
 :pto/options
 (fn [db [_ id]]
   ;(println "pto options sub: " id)
   (get-in db [:prefix-tree-options id :options])))

(rf/reg-sub
 :pto/matching-options
 (fn [[_ id]]
   [(rf/subscribe [:pto/options id])
    (rf/subscribe [:pto/cur-text id])])
 (fn [[options text]]
   (if (clojure.string/blank? text)
     options
     (filter (fn [[_ opt-text]]
               (clojure.string/includes? opt-text text))
             options))))


(defn option-row [id [_ text :as option]]
  [:div {:on-click #(rf/dispatch [:pto/option-selected id option])}
   [:p text]])

(defn Prefix-options [id]
  (let [matches @(rf/subscribe [:pto/matching-options id])]
    ;(println "pto comp, matches: " matches)
    [:div {:style {:display "flex" :flex-direction "column"}}
     [:div {:display "flex" :flex-direction "row"}
      [:input {:type "text"
               :on-change #(rf/dispatch [:pto/text-changed id (-> % .-target .-value)])
               :value @(rf/subscribe [:pto/cur-text id])}]
      [:div {:on-click #(rf/dispatch [:pto/clear-text id])}
       "X"]]
     [:div
      (map (partial option-row id) matches)]]))