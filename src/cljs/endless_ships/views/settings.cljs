(ns endless-ships.views.settings
  (:require [re-frame.core :as rf]
            [endless-ships.events :as events]
            [endless-ships.utils.filters :as filters]
            [endless-ships.views.utils :refer [kebabize]]
            [endless-ships.subs :as subs]
            [clojure.string :as str]))

(defn- plugin-version [plugin]
  (let [{:keys [hash date tag]} @(rf/subscribe [::subs/plugin-version plugin])
        repository @(rf/subscribe [::subs/plugin-repository plugin])
        plugin-name @(rf/subscribe [::subs/plugin-name plugin])]
    [:div
     (if (some? tag)
       [:a {:href (str repository "releases/tag/" tag)
            :target :blank}
        plugin-name " " tag]
       [:a {:href (str repository "commit/" hash)
            :target :blank}
        (kebabize plugin-name) "@" (subs hash 0 7)])
     " (" date ")"]))

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
     [:div.list-group
      (for [[item checked?] @(rf/subscribe [::subs/filter entity-type filter-type])]
        (let [item-checkbox-id (str "filter-item-" (kebabize header) "-" (kebabize (name item)))]
          ^{:key item-checkbox-id}
          [:div.list-group-item.d-flex.justify-content-between.align-items-center
           [:div.form-check.form-switch
            [:label.form-check-label
             [:input.form-check-input
              {:type "checkbox"
               :value ""
               :id item-checkbox-id
               :checked checked?
               :on-change #(rf/dispatch [::events/toggle-filter entity-type filter-type item])}]
             (str/capitalize (name item))]
            [plugin-version item]
            ]]))]]))

(comment
  [@(rf/subscribe [::subs/debug [:plugins]])]
  [@(rf/subscribe [::subs/plugin-keys])]
  )
