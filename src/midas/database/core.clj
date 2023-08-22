(ns midas.database.core)

(def DUMMY_DATA [{:amount 1 :description "coffee" :date "2019-01-01" :tags [:food :coffee]}
                 {:amount 2 :description "tea" :date "2019-01-02" :tags [:food]}
                 {:amount 3 :description "milk" :date "2019-01-03" :tags [:food :groceries]}
                 {:amount 7 :description "atm" :date "2019-01-03" :tags [:atm]}])


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