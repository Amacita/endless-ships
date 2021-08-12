(ns endless-ships.views.settings
  (:require [re-frame.core :as rf]
            [endless-ships.events :as events]
            [endless-ships.utils.filters :as filters]
            [endless-ships.views.utils :refer [kebabize]]
            [endless-ships.subs :as subs]
            [clojure.string :as str]))

(defn settings-page []
   (let [header "Plugins"
         entity-type :plugins
         filter-type :plugin-filter
         header-checkbox-id (str "filter-group-" (kebabize header))]
  [:div.app
     [:div.form-check.form-switch
      [:input.form-check-input
       {:type "checkbox"
        :value ""
        :id header-checkbox-id
        :checked (let [filters (vals @(rf/subscribe [::subs/filter entity-type filter-type]))]
                   (cond (every? true? filters) true
                         (some true? filters) false
                         :else false))
        :on-change #(rf/dispatch [::events/toggle-filter-group entity-type filter-type])}]
      [:label.form-check-label.checkbox-header
       {:for header-checkbox-id}
       header]]
   (for [[item checked?] @(rf/subscribe [::subs/filter entity-type filter-type])]
     (let [item-checkbox-id (str "filter-item-" (kebabize header) "-" (kebabize (name item)))]
       ^{:key item-checkbox-id}
       [:div.form-check.form-switch
        [:input.form-check-input
         {:type "checkbox"
          :value ""
          :id item-checkbox-id
          :checked checked?
          :on-change #(rf/dispatch [::events/toggle-filter entity-type filter-type item])}]
        [:label.form-check-label
         {:for item-checkbox-id}
         (str/capitalize (name item))]]))]))

(comment
  [@(rf/subscribe [::subs/debug [:plugins]])]
  [@(rf/subscribe [::subs/plugin-keys])]
  )
