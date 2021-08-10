(ns endless-ships.utils.filters
  (:require [re-frame.core :as rf]
            [reagent.core :as ra]
            [endless-ships.views.utils :refer [kebabize]]
            [endless-ships.events :as events]
            [endless-ships.subs :as subs]
            [clojure.string :as str]))

(defn- page-filters []
  {:outfits [["Race" :outfits :race-filter]
             ["License" :outfits :license-filter]]
   :settings [["Plugins" :plugins :plugin-filter]]
   :ships [["Race" :ships :race-filter]
           ["Category" :ships :category-filter]
           ["License" :ships :license-filter]]})

(defn- filter-group
  [header entity-type filter-type]
  [:div.col-lg-2.col-md-3
   (let [header-checkbox-id (str "filter-group-" (kebabize header))]
     [:div.form-check
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
       header]])
   (for [[item checked?] @(rf/subscribe [::subs/filter entity-type filter-type])]
     (let [item-checkbox-id (str "filter-item-" (kebabize header) "-" (kebabize (name item)))]
       ^{:key item-checkbox-id}
       [:div.form-check
        [:input.form-check-input
         {:type "checkbox"
          :value ""
          :id item-checkbox-id
          :checked checked?
          :on-change #(rf/dispatch [::events/toggle-filter entity-type filter-type item])}]
        [:label.form-check-label
         {:for item-checkbox-id}
         (str/capitalize (name item))]]))])

(defn ui [page]
  (let [height (ra/atom nil)]
    (fn []
      (let [collapsed? @(rf/subscribe [::subs/ship-filters-collapsed?])]
        [:div.filters-group
         [:div.collapse {:id :filters-collapse
                         :class (when (not collapsed?) :show)}
          [:div.container-fluid
           [:div.row
             (doall (for [[header entity-type filter-type] (page (page-filters))]
               ^{:key header} [filter-group header entity-type filter-type]))]]]
         [:button.btn.btn-primary
          {:type "button"
           :data-bs-toggle "collapse"
           :data-bs-target "#filters-collapse"
           :aria-expanded collapsed?
           :aria-controls "filters-collapse"
           :on-click #(rf/dispatch [::events/toggle-ship-filters-visibility])}
          "Filters "
          (if collapsed?
            [:i.bi.bi-chevron-down]
            [:i.bi.bi-chevron-up])]]))))
