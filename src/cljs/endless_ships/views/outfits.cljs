(ns endless-ships.views.outfits
  (:require [re-frame.core :as rf]
            [reagent-table.core :as rt]
            [endless-ships.views.utils :refer [kebabize race-label nbspize format-number]]
            [endless-ships.utils.outfits :as utils]
            [endless-ships.subs :as subs]
            [endless-ships.routes :as routes]
            [endless-ships.utils.filters :as filters]))

(def default-columns
  [{:header "Name"
    :path [:name]
    :key :name
    :format #(routes/outfit-link %)}
   {:header "Race"
    :path [:race]
    :key :race
    :format #(race-label %)
    :attrs (fn [data] {:style {:text-align "center" :display "block"}})}
   {:header "Licenses"
    :path [:licenses]
    :key :licenses
    :format #(interpose " " (doall (map (fn [license] ^{:key license} [:span.label
                                                       {:class (str "label-" @(rf/subscribe [::subs/license-style license]))}
                                                       license]) %)))
    :attrs (fn [data] {:style {:text-align "center" :display "block"}})}
   {:header "Cost"
    :path [:cost]
    :key :cost
    :format format-number}
   {:header "Outfit sp."
    :path [:outfit-space]
    :key :outfit-space
    :format format-number}])

(def weapon-column-info
  [{:header "Shield damage"
    :path [:weapon :shield-damage :per-second]
    :key :shield-damage
    :format format-number}
   {:header "Shield damage / space"
    :expr #(/ (get-in % [:weapon :shield-damage :per-second]) (:outfit-space %))
    :key :shield-damage-per-second
    :format format-number}
   {:header "Hull damage"
    :path [:weapon :shield-damage :per-second]
    :key :hull-damage
    :format format-number}
   {:header "Hull damage / space"
    :expr #(/ (get-in % [:weapon :hull-damage :per-second]) (:outfit-space %))
    :key :hull-damage-per-space
    :format format-number}
   {:header "Range"
    :path [:weapon :range]
    :key :range
    :format format-number}
   {:header "Fire rate"
    :path [:weapon :shots-per-second]
    :key :fire-rate
    :sortable false}
   {:header "Firing energy/s"
    :path [:weapon :firing-energy :per-second]
    :key :firing-energy
    :format format-number}
   {:header "Firing heat/s"
    :path [:weapon :firing-heat :per-second]
    :key :firing-heat
    :format format-number}])

(def columns
  {:thrusters
   [{:header "Thrust"
     :path [:thrust]
     :key :thrust
     :format format-number}
    {:header "Thrust per space"
     :expr #(/ (:thrust %) (:outfit-space %))
     :key :thrust-per-space
     :format format-number}
    {:header "Thr. energy"
     :path [:thrusting-energy]
     :key :thrusting-energy
     :format format-number}
    {:header "Thr. heat"
     :path [:thrusting-heat]
     :key :thrusting-heat
     :format format-number}],

   :steerings
   [{:header "Turn"
     :path [:turn]
     :key :turn
     :format format-number}
    {:header "Turn per space"
     :expr #(/ (:turn %) (:outfit-space %))
     :key :turn-per-space
     :format format-number}
    {:header "Turn energy"
     :path [:turning-energy]
     :key :turning-energy
     :format format-number}
    {:header "Turn heat"
     :path [:turning-heat]
     :key :turning-heat
     :format format-number}],

   :reactors
   [{:header "Energy gen."
     :expr #(+ (get % :energy-generation 0) (get % :solar-collection 0))
     :key :energy-generation
     :format format-number}
    {:header "Energy per space"
     :expr #(/ (+ (get % :energy-generation 0) (get % :solar-collection 0)) (:outfit-space %))
     :key :energy-per-space
     :format format-number}
    {:header "Heat gen."
     :path [:heat-generation]
     :key :heat-generation
     :format format-number}
    {:header "Energy capacity"
     :path [:energy-capacity]
     :key :energy-capacity
     :format format-number}
    {:header "Maintenance"
     :path [:maintenance-costs]
     :key :maintenance-costs
     :format format-number}],

   :batteries
   [{:header "Energy capacity"
     :path [:energy-capacity]
     :key :energy-capacity
     :format format-number}
    {:header "Energy per space"
     :expr #(/ (:energy-capacity %) (:outfit-space %))
     :key :energy-per-space
     :format format-number}],

   :coolers
   [{:header "Cooling"
     :expr #(+ (get % :cooling 0) (get % :active-cooling 0))
     :key :total-cooling
     :format format-number}
    {:header "Cooling per space"
     :expr #(/ (+ (get % :cooling 0) (get % :active-cooling 0)) (:outfit-space %))
     :key :cooling-per-space
     :format format-number}
    {:header "Cooling energy"
     :path [:cooling-energy]
     :key :cooling-energy
     :format format-number}],

   :shields
   [{:header "Shield generation"
     :path [:shield-generation]
     :key :shield-generation
     :format format-number}
    {:header "Shield per space"
     :expr #(/ (:shield-generation %) (:outfit-space %))
     :key :shield-per-space
     :format format-number}
    {:header "Shield energy"
     :path [:shield-energy]
     :key :shield-energy
     :format format-number}
    {:header "Shield/energy"
     :expr #(/ (:shield-generation %) (:shield-energy %))
     :key :shield-generation-per-energy
     :format format-number}
    {:header "Shield heat"
     :path [:shield-heat]
     :key :shield-heat
     :format format-number}],

   :hull-repair
   [{:header "Hull repair rate"
     :path [:hull-repair-rate]
     :key :hull-repair-rate
     :format format-number}
    {:header "Hull per space"
     :expr #(/ (:hull-repair-rate %) (:outfit-space %))
     :key :hull-per-space
     :format format-number}
    {:header "Hull energy"
     :path [:hull-energy]
     :key :hull-energy
     :format format-number}
    {:header "Hull/energy"
     :expr #(/ (:hull-repair-rate %) (:hull-energy %))
     :key :hull-repair-per-energy
     :format format-number}
    {:header "Hull heat"
     :path [:hull-heat]
     :key :hull-heat
     :format format-number}],

   :ramscoops
   [{:header "Ramscoop"
     :path [:ramscoop]
     :key :ramscoop
     :format format-number}
    {:header "Ramscoop per space"
     :expr #(/ (:ramscoop %) (:outfit-space %))
     :key :ramscoop-per-space
     :format format-number}],

   :guns weapon-column-info,

   :secondary weapon-column-info,

   :turrets weapon-column-info,

   :anti-missile
   [{:header "Anti-missile"
     :path [:weapon :anti-missile]
     :key :anti-missile
     :format format-number}
    {:header "Range"
     :path [:weapon :range]
     :key :range
     :format format-number}
    {:header "Fire-rate"
     :path [:weapon :shots-per-second]
     :key :fire-rate
     :format format-number}
    {:header "Firing energy/s"
     :path [:weapon :firing-energy :per-second]
     :key :firing-energy
     :format format-number}
    {:header "Firing heat/s"
     :path [:weapon :firing-heat :per-second]
     :key :firing-heat
     :format format-number}],

   :hand-to-hand
   [{:header "Capture attack"
     :path [:capture-attack]
     :key :capture-attack
     :format format-number}
    {:header "Capture defense"
     :path [:capture-defense]
     :key :capture-defense
     :format format-number}
    {:header "Illegal"
     :path [:illegal]
     :key :illegal
     :format format-number}]})

(defn outfits []
  [:div.app
   [filters/ui :outfits]
   (doall (map (fn [[type type-attrs]]
                 (let [column-model (into [] (concat default-columns (type columns)))]
                   ^{:key type} [:div [:h2 (:header type-attrs)]
                                 [rt/reagent-table
                                  (rf/subscribe [::subs/outfits-of-type type column-model])
                                  {:column-model column-model,
                                          :data-root-key :outfits,
                                          :entity-type type}]]))
               utils/types))])
