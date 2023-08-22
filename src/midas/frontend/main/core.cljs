(ns midas.frontend.main.core
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [reitit.core :as r]
            [reitit.frontend.easy :as rfe]
            [lambdaisland.fetch :as fetch]
            [cljs.reader :refer [read-string]]
            [midas.frontend.components.itemFeed.core :refer [feed]]
            [midas.router.core :refer [routes]]))

;; define your app data so that it doesn't get over-written on reload

(def DUMMY_DATA {1 {:amount 1 :description "coffee" :date "2019-01-01" :tags [:food :coffee] :id 1}
                 2 {:amount 2 :description "tea" :date "2019-01-02" :tags [:food] :id 2}
                 3 {:amount 3 :description "milk" :date "2019-01-03" :tags [:food :groceries] :id 3}
                 4 {:amount 7 :description "atm" :date "2019-01-03" :tags [:atm]} :id 4})

(def router (r/router routes))

;;######################################################################
;; API Stuff
;;######################################################################

;;######################################################################
;; Event Handlers
;;######################################################################

(rf/reg-event-fx
 :initialize-db
 (fn [_ _]
   {:db {:items DUMMY_DATA
         :active-filters [:coffee :food :groceries :atm]
         :tags {:tada {} :naha {}}
         :route "/"}
    :fx [[:dispatch [:api-call :api/read-line-items {} nil :update-items]]
         [:dispatch [:api-call :api/read-tags {} nil :update-tags]]]}))

(rf/reg-event-db
 :update-items
 (fn [db [_ items]]
   (assoc db :items items)))

(rf/reg-event-db
 :update-tags
 (fn [db [_ tags]]
   (assoc db :tags tags)))

(rf/reg-event-db
 :navigated
 (fn [db [_ path]]
   (assoc db :route path)))

(rf/reg-event-fx
 :navigate
 (fn [_ [_ route]]
   {:navigate! route}))

;; (rf/reg-event-db
;;  :new-raw-items
;;  (fn [db [_ new-items]]
;;    (assoc db :new-item-state {:cur-item (first new-items)
;;                               :remaining-items (rest new-items)})))

;; (rf/reg-event-db
;;  :add-item
;;  (fn [db [_ item]]
;;    (let [remaining-items (get-in db [:new-item-state :remaining-items])]
;;      (-> (update db :items conj item)
;;          (assoc :new-item-state {:cur-item (first remaining-items)
;;                                  :remaining-items (rest remaining-items)})))))

(rf/reg-event-fx
 :api-call
 (fn [coefx event]
   {:api-call (into [] (rest event))}))

;;######################################################################
;; Effect Handlers
;;######################################################################

(rf/reg-fx
 :navigate!
 (fn [route]
   (rfe/push-state route)))


(rf/reg-fx
 :api-call
 (fn [[name params data event-id]]
   (println (str "In :api-call effect handler, " name params data))
   (if-let [match (r/match-by-name router name params)]
     (-> (case (get-in match [:data :req-method])
           :GET (do (println match)
                    (fetch/get (:path match)))
           :POST (fetch/post (:path match)
                             {:body (prn-str data)}))
         (.then (fn [resp]
                  (-> resp
                      :body)))
         (.then (fn [data]
                  (println "DATA pre conver: " data)
                  (rf/dispatch [event-id (read-string data)]))))
     (throw (js/Error. (str "api-call error: " name " does not match a defined route."))))))



;;######################################################################
;; Subscriptions
;;######################################################################

;;##########################
;; Layer 2 Subs
;;##########################
(rf/reg-sub
 :items
 (fn [db _]
   (vals (:items db))))

(rf/reg-sub
 :active-filters
 (fn [db _]
   (:active-filters db)))

(rf/reg-sub
 :tags
 (fn [db _]
   (:tags db)))

(rf/reg-sub
 :route
 (fn [db _]
   (:route db)))

;;##########################
;; Layer 3 Subs
;;##########################

(rf/reg-sub
 :active-items
 (fn [_]
   [(rf/subscribe [:items]) (rf/subscribe [:active-filters])])
 (fn [[items active-filters] _]
   (filter #(some (into #{} (:tags %)) active-filters) items)))

(rf/reg-sub
 :page
 (fn []
   (rf/subscribe [:route]))
 (fn [route _]
   (println (str "reg-sub page: " route))
   (if-let [match (r/match-by-path router route)]
     [(get-in match [:data :name]) (:path-params match)]
     [:pages/home {}])))

(rf/reg-sub
 :tag-names
 (fn []
   (rf/subscribe [:tags]))
 (fn [tags]
   (println "tag sub: type=" (first (keys tags)))
   (->> tags
        keys
        (map name)
        (map clojure.string/capitalize))))



;;######################################################################
;; COMPONENTS
;;######################################################################

(defn r1 []
  (let []
    [:div
     [:h1 "Route 1"]]))

(defn r2 []
  (let []
    [:div
     [:h1 "Route 2  "]]))

(defn Feed []
  (let [items @(rf/subscribe [:active-items])]
    [:div
     [:h1 "Feeds! Love em!"]
     [:div (feed items)]]))

(defn tag [tag-name]
  [:div {:style {:background-color "#ADD8E6" :margin "5px" :width "fit-content"}} tag-name])

;;##############################################################
;;
;;##############################################################

(defn on-navigate [match]
  (println "on nav")
  (println match)
  (rf/dispatch [:navigated (:path match)]))



(defn init-routes! []
  (rfe/start! router on-navigate {:use-fragment false}))

(defn app []
  (let [[page params] @(rf/subscribe [:page])
        tags @(rf/subscribe [:tag-names])]
    (println page)
    [:div
     [:div
      [:a {:href (rfe/href :pages/home)} "Home"]
      [:a {:href (rfe/href :pages/feed)} "Feed"]
      [:a {:href (rfe/href :pages/data-input)} "Data Input"]
      [:button {:on-click #(rf/dispatch [:api-call :api/read-line-items {} nil :update-items])} "Get Line Items"]]
     [:div {:style {:margin "5px"}} (for [tag-name tags]
                                      ^{:key tag-name}
                                      (tag tag-name))]
     [:div (case page
             :pages/home (r1)
             :pages/feed (Feed)
             :pages/data-input (r2))]]))

(defn start []
  (rdom/render [app]
               (. js/document (getElementById "app"))))

(defn ^:export init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (rf/dispatch-sync [:initialize-db])
  (init-routes!)
  (start))

(defn stop []
  ;; stop is called before any code is reloaded
  ;; this is controlled by :before-load in the config
  (js/console.log "stop"))

(init)
