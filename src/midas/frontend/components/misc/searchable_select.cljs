(ns midas.frontend.components.misc.searchable-select
  (:require [re-frame.core :as rf]))


;TODO: do the searchable part
(defn Searchable-select [options v event indx]
  [:select {:value v
            :on-change #(rf/dispatch [event indx (-> % .-target .-value keyword)])}
   (for [option options]
     ^{:key option}
     [:option {:value option} option])])