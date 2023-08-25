(ns midas.frontend.components.misc.modal
  (:require [re-frame.core :as rf]))

(defn click-handler [id modal-el evt]
  ;(println "click-handler: " id "\n" modal-el "\n" evt)
  (if (or (= (.-target evt) modal-el) (.contains modal-el (.-target evt)))
    (rf/dispatch [:modal/set-ref id modal-el])
    (do (.stopPropagation evt)
        (rf/dispatch [:modal/close id]))))

(rf/reg-event-fx
 :modal/set-ref
 (fn [{:keys [db]} [_ id el]]
  ; (println "set-ref event")
   (let [handler (partial click-handler id el)]
     {:db (assoc-in db [:modals id :event-handler] handler)
      :modal/add-event-listener handler})))

(rf/reg-event-fx
 :modal/close
 (fn [{:keys [db]} [_ id]]
  ; (println "close modal event")
   {:db (update db :modals dissoc id)
    :modal/remove-event-listener (:event-handler (get-in db [:modals id :event-handler]))}))

(rf/reg-event-db
 :modal/show
 (fn [db [_ id]]
   (println "show modal")
   (assoc-in db [:modals id :show] true)))

(rf/reg-sub
 :modal/show?
 (fn [db [_ id]]
  ; (println "modal/show?" id)
   (get-in db [:modals id :show])))

(rf/reg-fx
 :modal/add-event-listener
 (fn [handler]
  ; (println "addeventlist fx")
   (.addEventListener js/window "click" handler (clj->js {"once" true}))))

(rf/reg-fx
 :modal/remove-event-listener
 (fn [handler]
  ; (println "remove-event-listener fx")
   (.removeEventListener js/window "click" handler)))


(defn Modal [id component]
  [:div {:style {:border "1px solid black"}
         :ref (fn [el]
                ;(println "modal ref function" id el)
                (if (nil? el)
                  (println "el is nil")
                  (rf/dispatch [:modal/set-ref id el])))}

   component])