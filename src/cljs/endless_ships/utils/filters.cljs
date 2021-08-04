(ns endless-ships.utils.filters
  (:require [re-frame.core :as rf]
            [reagent.core :as ra]
            [endless-ships.events :as events]
            [endless-ships.subs :as subs]
            [clojure.string :as str]))

(defn- filter-group
  [header entity-type filter-type]
  [:div.col-lg-2.col-md-3
   [:div.checkbox
    [:label.checkbox-header
     [:input
      {:type "checkbox"
       :checked (let [filters (vals @(rf/subscribe [::subs/filter entity-type filter-type]))]
                  (cond (every? true? filters) true
                        (some true? filters) false
                        :else false))
       :on-change #(rf/dispatch [::events/toggle-filter-group entity-type filter-type])}]
     header]]
   (for [[item checked?] @(rf/subscribe [::subs/filter entity-type filter-type])]
     ^{:key item} [:div.checkbox
                   [:label
                    [:input {:type "checkbox"
                             :checked checked?
                             :on-change #(rf/dispatch [::events/toggle-filter entity-type filter-type item])}]
                    (str/capitalize (name item))]])])

(defn ui []
  (let [height (ra/atom nil)]
    (fn []
      (let [collapsed? @(rf/subscribe [::subs/ship-filters-collapsed?])]
        [:div.filters-group
         [:div {:style {:overflow "hidden"
                        :transition "max-height 0.8s"
                        :max-height (if collapsed? 0 @height)}}
          [:div.container-fluid
           {:ref #(when % (reset! height (.-clientHeight %)))}
           [:div.row
             (filter-group "Race" :outfits :race-filter)
             (filter-group "License" :outfits :license-filter)
             ]]]
         [:button.btn.btn-default
          {:type "button"
           :on-click #(rf/dispatch [::events/toggle-ship-filters-visibility])}
          "Filters "
          (if collapsed?
            [:span.glyphicon.glyphicon-menu-down]
            [:span.glyphicon.glyphicon-menu-up])]]))))
