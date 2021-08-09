(ns endless-ships.views.ships-list
  (:require [re-frame.core :as rf]
            [reagent.core :as ra]
            [reagent-table.core :as rt]
            [endless-ships.events :as events]
            [endless-ships.subs :as subs]
            [endless-ships.views.utils :refer [nbsp nbspize race-label format-number]]
            [endless-ships.utils.ships :refer [total-cost or-zero columns]]
            [endless-ships.routes :as routes]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as str]))

(defn- filter-group
  [header subscription entity-type filter-type]
  [:div.col-lg-2.col-md-3
   [:div.checkbox
    [:label.checkbox-header
     [:input
      {:type "checkbox"
       :checked (let [filters (vals @(rf/subscribe [subscription]))]
                  (cond (every? true? filters) true
                        (some true? filters) false
                        :else false))
       :on-change #(rf/dispatch [::events/toggle-filter-group entity-type filter-type])}]
     header]]
   (for [[item checked?] @(rf/subscribe [subscription])]
     ^{:key item} [:div.checkbox
                   [:label
                    [:input {:type "checkbox"
                             :checked checked?
                             :on-change #(rf/dispatch [::events/toggle-filter entity-type filter-type item])}]
                    (str/capitalize (name item))]])])

(defn ships-filter []
  (let [height (ra/atom nil)]
    (fn []
      (let [collapsed? @(rf/subscribe [::subs/ship-filters-collapsed?])
            race-filter @(rf/subscribe [::subs/ships-race-filter])
            category-filter @(rf/subscribe [::subs/ships-category-filter])
            license-filter @(rf/subscribe [::subs/ships-license-filter])]
        [:div.filters-group
         [:div {:style {:overflow "hidden"
                        :transition "max-height 0.8s"
                        :max-height (if collapsed? 0 @height)}}
          [:div.container-fluid
           {:ref #(when % (reset! height (.-clientHeight %)))}
           [:div.row
             (filter-group "Race" ::subs/ships-race-filter :ships :race-filter)
             (filter-group "Category" ::subs/ships-category-filter :ships :category-filter)
             (filter-group "License" ::subs/ships-license-filter :ships :license-filter)
             ]]]
         [:button.btn.btn-default
          {:type "button"
           :on-click #(rf/dispatch [::events/toggle-ship-filters-visibility])}
          "Filters "
          (if collapsed?
            [:span.glyphicon.glyphicon-menu-down]
            [:span.glyphicon.glyphicon-menu-up])]]))))

(def table-columns
  [{:header "Name"
    :path [:name]
    :key :name
    :format #(routes/ship-link %)}
   {:header "Race"
    :path [:race]
    :key :race
    :format #(race-label %)
    :attrs (fn [data] {:style {:text-align "center" :display "block"}})}
   {:header "Licenses"
    :path [:licenses]
    :key :licenses
    :format #(interpose " "
                        (doall (map (fn [license] @(rf/subscribe [::subs/license-label license])) %)))
    :attrs (fn [data] {:style {:text-align "center" :display "block"}})}
   {:header "Cost"
    :expr #(total-cost %)
    :key :cost
    :format format-number}
   {:header "Category"
    :path [:category]
    :key :category
    :format #(nbspize %)}
   {:header "Hull"
    :path [:hull]
    :key :hull
    :format format-number}
   {:header "Shields"
    :path [:shields]
    :key :shields
    :format format-number}
   {:header "Mass"
    :path [:mass]
    :key :mass
    :format format-number}
   {:header "Engine Cap."
    :path [:engine-capacity]
    :key :engine-capacity
    :format format-number}
   {:header "Weapon Cap."
    :path [:weapon-capacity]
    :key :weapon-capacity
    :format format-number}
   {:header "Fuel Cap."
    :path [:fuel-capacity]
    :key :fuel-capacity
    :format format-number}
   {:header "Outfit Sp."
    :path [:outfit-space]
    :key :outfit-space
    :format format-number}
   {:header "Cargo Sp."
    :path [:cargo-space]
    :key :cargo-space
    :format format-number}
   {:header "Crew / bunks"
    :expr #(str (format-number (:required-crew %))
                nbsp "/" nbsp
                (format-number (:bunks %)))
    :key :crew-bunks}])

(defn ships-list []
  [:div.app
   [ships-filter]
   (let [config {:column-model table-columns,
                        :data-root-key :ships,
                        :entity-type :ships}]
     [rt/reagent-table (rf/subscribe [::subs/filtered-ships table-columns]) config])])

(comment
  (print (.-stack *e))
  (print *e)
  (take 3 @(rf/subscribe [::subs/filtered-ships table-columns]))
  [@(rf/subscribe [::subs/debug [:settings]])]
  [@(rf/subscribe [::subs/debug [:settings :ships :ordering]])]
  (rf/dispatch [::events/toggle-ordering :ships 4 false])
  )
