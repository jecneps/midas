(ns midas.frontend.main.core
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [reitit.core :as r]
            [reitit.frontend.easy :as rfe]
            [lambdaisland.fetch :as fetch]
            [cljs.reader :refer [read-string]]
            [clojure.set :refer [subset?]]
            [midas.frontend.components.itemFeed.core :refer [feed]]
            [midas.frontend.components.grid-view.core :refer [Grid-view]]
            [midas.frontend.components.misc.modal :refer [Modal]]
            [midas.frontend.components.tabs.groups-tab :refer [Groups-tab]]
            [midas.router.core :refer [routes]]))

;; define your app data so that it doesn't get over-written on reload

(def DUMMY_DATA {1 {:amount 1 :description "coffee" :date "2019-01-01" :tags [:food :coffee] :id 1}
                 2 {:amount 2 :description "tea" :date "2019-01-02" :tags [:food] :id 2}
                 3 {:amount 3 :description "milk" :date "2019-01-03" :tags [:food :groceries] :id 3}
                 4 {:amount 7 :description "atm" :date "2019-01-03" :tags [:atm] :id 4}})

(def D_DATA [{:amount 1 :description "coffee" :date "2019-01-01" :tags [:food :coffee]}
             {:amount 2 :description "tea" :date "2019-01-02" :tags [:food]}
             {:amount 3 :description "milk" :date "2019-01-03" :tags [:food :groceries]}
             {:amount 7 :description "atm" :date "2019-01-03" :tags [:atm]}])


(def BIG_DUMMY_DATA (reduce (fn [m [indx item]]
                              (assoc m indx (assoc item :id indx)))
                            {}
                            (map-indexed vector (flatten (for [year ["2019" "2020" "2021" "2022"]]
                                                           (for [month ["01" "02" "03" "04"]]
                                                             (map #(assoc % :date (str year "-" month "-15")) D_DATA)))))))

(def router (r/router routes))

;;######################################################################
;; Misc Computation
;;######################################################################

(defn group-data [grp-preds items depth]
  (println "group-data: " grp-preds depth)
  (if (empty? grp-preds)
    {:type :list
     :depth depth
     :data items}
    (let [[[k pred dir] & rst] grp-preds]
      (println "group-data: " k pred dir)
      {:type :group
       :depth depth
       :g-name k
       :data (->> (group-by pred items)
                  (sort-by (fn [[k _]] k))
                  (map (fn [[k v]]
                         [{:g-val k
                           :depth (inc depth)
                           :cnt (count v)
                           :sum (apply + (map :amount v))}
                          (group-data rst v (inc depth))])))})))

(defn spacer-row [depth]
  {:type :spacer
   :depth depth})

(defn group-row [g-vals n id collapsed?]
  (merge {:g-name n
          :type :group-header
          :id id
          :collapsed? collapsed?}
         g-vals))

(defn year [date]
  (-> date
      (clojure.string/split #"-")
      first))

(defn month [date]
  (-> date
      (clojure.string/split #"-")
      second))

(def g-preds [[:year #(year (:date %)) :asc]])

(defn flatten-groups [collapsed? groups id-prefix-map]
  (if (= :list (:type groups))

    (concat (map #(assoc % :depth (:depth groups) :type :item)
                 (:data groups))
            [(spacer-row (:depth groups))])
    (let [data (:data groups)]
      (concat
       (mapcat (fn [[g-data group]]
                 (let [id-map (merge id-prefix-map {(:g-name groups) (:g-val g-data)})]
                   (if (contains? collapsed? id-map)
                     [(group-row g-data (:g-name groups) id-map true)]
                     (concat [(group-row g-data (:g-name groups) id-map false)] (flatten-groups collapsed? group id-map)))))
               data)
       [(spacer-row (:depth groups))]))))

(defn vec-remove [i v]
  (into (subvec v 0 i) (subvec v (inc i))))

(defn all-group-headers [groups]
  (->> (flatten-groups #{} groups {})
       (filter #(= :group-header (:type %)))
       (map :id)
       (into #{})))

;;######################################################################
;; Event Handlers
;;######################################################################

(rf/reg-event-fx
 :initialize-db
 (fn [_ _]
   {:db {:items BIG_DUMMY_DATA
         :active-filters [:coffee :food :groceries :atm]
         :tags {:tada {} :naha {}}
         :active-groups g-preds
         :possible-groups {:date :date :tags :tags :amount :amount :description :description :year year :month month}
         :collapsed-groups #{}
         :modals {}
         :prefix-tree-options {}
         :route "/"}
    :fx [[:dispatch [:api-call :api/read-tags {} nil :update-tags]]]}))

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

(rf/reg-event-fx
 :api-call
 (fn [coefx event]
   {:api-call (into [] (rest event))}))

(rf/reg-event-db
 :toggle-group-header
 (fn [db [_ id]]
   (if (contains? (:collapsed-groups db) id)
     (update db :collapsed-groups disj id)
     (update db :collapsed-groups conj id))))

(rf/reg-event-fx
 :remove-group
 (fn [{:keys [db]} [_ indx]]
   {:db (update db :active-groups (partial vec-remove indx))
    :fx [[:dispatch [:normalize-collapsed-groups]]]}))

(rf/reg-event-fx
 :update-group
 (fn [{:keys [db]} [_ indx g-name]]
   {:db (update db :active-groups assoc indx [g-name (get-in db [:possible-groups g-name]) :asc])
    :fx [[:dispatch [:normalize-collapsed-groups]]]}))

(rf/reg-event-db
 :add-group
 (fn [db [_ g-id]]
   (update db :active-groups conj [g-id (get-in db [:possible-groups g-id]) :asc])))

(rf/reg-event-db
 :normalize-collapsed-groups
 (fn [db [_]]
   (let [active-groups (:active-groups db)
         active-g-names (into #{} (map first active-groups))]
     (update db :collapsed-groups (fn [c]
                                    (->> c
                                         (filter #(subset? % active-g-names)) ;TODO: ignores reordering
                                         (into #{})))))))

(rf/reg-event-db
 :group/collapse-all
 (fn [db [_ collapsed-groups]]
   (assoc db :collapsed-groups collapsed-groups)))

(rf/reg-event-db
 :group/expand-all
 (fn [db [_]]
   (assoc db :collapsed-groups #{})))

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
   ;(println (str "In :api-call effect handler, " name params data))
   (if-let [match (r/match-by-name router name params)]
     (-> (case (get-in match [:data :req-method])
           :GET (do ;(println match)
                  (fetch/get (:path match)))
           :POST (fetch/post (:path match)
                             {:body (prn-str data)}))
         (.then (fn [resp]
                  (-> resp
                      :body)))
         (.then (fn [data]
                  ;(println "DATA pre conver: " data)
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

(rf/reg-sub
 :active-groups
 (fn [db _]
   (:active-groups db)))

(rf/reg-sub
 :collapsed-groups
 (fn [db _]
   (:collapsed-groups db)))

(rf/reg-sub
 :possible-groups
 (fn [db _]
   (:possible-groups db)))

;;##########################
;; Layer 3 Subs
;;##########################

(rf/reg-sub
 :active-items
 (fn [_]
   [(rf/subscribe [:items]) (rf/subscribe [:active-filters])])
 (fn [[items active-filters] _]
  ;; (filter #(some (into #{} (:tags %)) active-filters) items)))
   items))

(rf/reg-sub
 :page
 (fn []
   (rf/subscribe [:route]))
 (fn [route _]
   ;(println (str "reg-sub page: " route))
   (if-let [match (r/match-by-path router route)]
     [(get-in match [:data :name]) (:path-params match)]
     [:pages/home {}])))

(rf/reg-sub
 :tag-names
 (fn []
   (rf/subscribe [:tags]))
 (fn [tags]
   ;(println "tag sub: type=" (first (keys tags)))
   (->> tags
        keys
        (map name)
        (map #(as-> % $
                (clojure.string/split $ #"-")
                (map clojure.string/capitalize $)
                (clojure.string/join " " $))))))

(rf/reg-sub
 :grouped-items
 (fn []
   [(rf/subscribe [:active-items]) (rf/subscribe [:active-groups])])
 (fn [[items groupings] _]
   (group-data groupings items 0)))

(rf/reg-sub
 :flattened-items
 (fn []
   [(rf/subscribe [:grouped-items]) (rf/subscribe [:collapsed-groups])])
 (fn [[grouped-items collapsed-groups] _]
   (flatten-groups collapsed-groups grouped-items {})))

(rf/reg-sub
 :unused-groups
 (fn []
   [(rf/subscribe [:active-groups]) (rf/subscribe [:possible-groups])])
 (fn [[active-groups possible-groups] _]
   (let [active (into #{} (map first active-groups))
         possible (into #{} (map first possible-groups))]
     (clojure.set/difference possible active))))

(rf/reg-sub
 :unused-options
 (fn []
   (rf/subscribe [:unused-groups]))
 (fn [unused-groups]
   (map #(vector % (name %)) unused-groups)))

(rf/reg-sub
 :group/all-group-headers
 (fn []
   (rf/subscribe [:grouped-items]))
 (fn [grouped-items]
   (all-group-headers grouped-items)))


;;######################################################################
;; COMPONENTS
;;######################################################################

(defn test-modal [component]
  (let [show-modal? @(rf/subscribe [:modal/show? :test])]
    [:div {:class "modal-parent"}
     [:button {:on-click #(rf/dispatch [:modal/show :test])} "Show Modal"]
     (if show-modal?
       (Modal :test show-modal? component))]))

(defn r1 []
  (let []
    [:div
     [:h1 "Route 1"]]))

(defn r2 []
  (let []
    [:div
     [:h1 "Route 2  "]]))

(defn Feed []
  (let [items @(rf/subscribe [:flattened-items])]
    [:div
     (test-modal (Groups-tab))
     [:h1 "Feeds! Love em!"]
     [:div (Grid-view items)]]))

(defn tag [tag-name]
  [:div {:style {:background-color "#ADD8E6" :margin "5px" :width "fit-content"}} tag-name])



;;##############################################################
;;
;;##############################################################

(defn on-navigate [match]
  (rf/dispatch [:navigated (:path match)]))



(defn init-routes! []
  (rfe/start! router on-navigate {:use-fragment false}))

(defn app []
  (let [[page params] @(rf/subscribe [:page])
        tags @(rf/subscribe [:tag-names])]
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
