(ns endless-ships.views.ships-list
  (:require [re-frame.core :as rf]
            [reagent.core :as ra]
            [reagent-table.core :as rt]
            [endless-ships.events :as events]
            [endless-ships.subs :as subs]
            [endless-ships.views.utils :refer [nbsp nbspize race-label format-number]]
            [endless-ships.utils.ships :refer [total-cost or-zero columns]]
            [endless-ships.utils.filters :as filters]
            [endless-ships.routes :as routes]
            [clojure.string :as str]))

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
   [filters/ui :ships]
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
