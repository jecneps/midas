(ns midas.database.core)

(def DUMMY_DATA [{:amount 1 :description "coffee" :date "2019-01-01" :tags [:food :coffee]}
                 {:amount 2 :description "tea" :date "2019-01-02" :tags [:food]}
                 {:amount 3 :description "milk" :date "2019-01-03" :tags [:food :groceries]}
                 {:amount 7 :description "atm" :date "2019-01-03" :tags [:atm]}])


(def BIG_DUMMY_DATA (reduce (fn [m [indx item]]
                              (assoc m indx (assoc item :id indx)))
                            {}
                            (map-indexed vector (flatten (for [year ["2019" "2020" "2021" "2022"]]
                                                           (for [month ["01" "02" "03" "04"]]
                                                             (map #(assoc % :date (str year "-" month "-15")) DUMMY_DATA)))))))

(defn group-data [grp-preds items depth]
  (if (empty? grp-preds)
    {:type :list
     :depth depth
     :data items}
    (let [[[k pred dir] & rst] grp-preds]
      {:type :group
       :depth depth
       :name k
       :data (->> (group-by pred items)
                  (sort-by (fn [[k _]] k))
                  (map (fn [[k v]]
                         [{:g-val k
                           :depth (inc depth)
                           :count (count v)
                           :sum (apply + (map :amount v))}
                          (group-data rst v (inc depth))])))})))

(defn spacer-row [depth]
  {:type :spacer
   :depth depth})

(defn group-row [g-vals n]
  (merge {:name n
          :type :group-header}
         g-vals))

(defn year [date]
  (-> date
      (clojure.string/split #"-")
      first))

(defn month [date]
  (-> date
      (clojure.string/split #"-")
      second))

(defn flatten-groups [groups]
  (if (= :list (:type groups))

    (concat (map #(assoc % :depth (:depth groups) :type :item)
                 (:data groups))
            [(spacer-row (:depth groups))])
    (let [data (:data groups)]
      (concat
       (mapcat (fn [[g-data group]]
                 (concat [(group-row g-data (:name groups))] (flatten-groups group)))
               data)
       [(spacer-row (:depth groups))]))))


(def edn-read (comp read-string slurp))

(defn- edn-write [path data]
  (spit path (prn-str data)))

(defn- edn-update [path update-fn]
  (->> (edn-read path)
       update-fn
       (edn-write path)))

(defn- set-item-id [id]
  (edn-write "next-item-id.edn" id))

(defn init-item-id []
  (set-item-id 0))

(defn- next-item-id []
  (let [id (edn-read "next-item-id.edn")]
    (set-item-id (inc id))
    (str id)))

(defn read-line-items []
  (edn-read "line-items.edn"))

(defn add-line-item [item]
  (edn-update "line-items.edn" (fn [line-items]
                                 (let [id (next-item-id)]
                                   (assoc line-items id (assoc item :id id))))))

(defn add-line-items [items]
  (edn-update "line-items.edn" (fn [line-items]
                                 (reduce (fn [items item]
                                           (let [id (next-item-id)]
                                             (assoc items id (assoc item :id id))))
                                         line-items
                                         items))))

(defn delete-line-item [id]
  (edn-update "line-items.edn" (fn [line-items]
                                 (dissoc line-items id))))

(defn read-tags []
  (edn-read "tags.edn"))