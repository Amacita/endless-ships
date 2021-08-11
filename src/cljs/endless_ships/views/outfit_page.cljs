(ns endless-ships.views.outfit-page
  (:require [re-frame.core :as rf]
            [endless-ships.subs :as subs]
            [endless-ships.views.utils :refer [render-attribute render-description
                                               kebabize nbspize]]
            [endless-ships.routes :as routes]))

(defn- render-license [outfit]
  (let [[license] (:licenses outfit)]
    (when (some? license)
      [:p.italic
       {:style {:margin-top 20}}
       (str "This outfit requires a " license " license.")])))

(defn- image-url [outfit]
  (let [filename (str (-> outfit :thumbnail js/window.encodeURI) ".png")
        plugin @(rf/subscribe [::subs/plugin (get-in outfit [:meta :image :origin])])]
    (str (:base-image-url plugin) filename)))

(defn- render-ammo [outfit]
  (when (contains? outfit :ammo)
    [:li "ammo: " (routes/outfit-link (:ammo outfit))]))

(defn weapon-attributes [weapon]
  [:div
   [:br]
   (render-ammo weapon)
   (render-attribute weapon :range "range")
   (render-attribute weapon (comp :per-second :shield-damage) "shield damage / second")
   (render-attribute weapon (comp :per-second :hull-damage) "hull damage / second")
   (render-attribute weapon (comp :per-second :heat-damage) "heat damage / second")
   (render-attribute weapon (comp :per-second :ion-damage) "ion damage / second")
   (render-attribute weapon (comp :per-second :disruption-damage) "disruption damage / second")
   (render-attribute weapon (comp :per-second :slowing-damage) "slowing damage / second")
   (render-attribute weapon (comp :per-second :firing-energy) "firing energy / second")
   (render-attribute weapon (comp :per-second :firing-heat) "firing heat / second")
   (render-attribute weapon (comp :per-second :firing-fuel) "firing fuel / second")
   (render-attribute weapon :shots-per-second "shots / second")
   (render-attribute weapon :turret-turn "turret turn rate")
   (when (and (contains? weapon :shots-per-second)
              (number? (:shots-per-second weapon)))
     [:br])
   (render-attribute weapon (comp :per-shot :shield-damage) "shield damage / shot")
   (render-attribute weapon (comp :per-shot :hull-damage) "hull damage / shot")
   (render-attribute weapon (comp :per-shot :heat-damage) "heat damage / shot")
   (render-attribute weapon (comp :per-shot :ion-damage) "ion damage / shot")
   (render-attribute weapon (comp :per-shot :disruption-damage) "disruption damage / shot")
   (render-attribute weapon (comp :per-shot :slowing-damage) "slowing damage / shot")
   (render-attribute weapon (comp :per-shot :firing-energy) "firing energy / shot")
   (render-attribute weapon (comp :per-shot :firing-heat) "firing heat / shot")
   (render-attribute weapon :inaccuracy "inaccuracy")
   (render-attribute weapon :anti-missile "anti-missile")])

(defn outfit-page [outfit-name]
  (let [outfit @(rf/subscribe [::subs/outfit outfit-name])
        installations @(rf/subscribe [::subs/outfit-installations (:name outfit)])
        planets @(rf/subscribe [::subs/outfit-planets (:name outfit)])]
    [:div.app
     [:div.row.gy-4
      [:div.col-md-12
       [:div.card.text-dark.bg-light
        [:h3.card-header (:name outfit)]
        [:div.card-body.clearfix
         [:div.row.gy-3
          [:div.col-md-4
           (when (contains? outfit :thumbnail)
             [:img.ship-sprite.float-end.mb-3.ms-md-3
              {:src (image-url outfit)}])
           (if (seq (:description outfit))
             (render-description outfit)
             [:p.italic "No description."])
          (render-license outfit)]
         [:div.col-md-4
          [:ul
           (render-attribute outfit :category "category")
           (render-attribute outfit :cost "cost")
           (render-attribute outfit :outfit-space "outfit space needed")
           (render-attribute outfit :weapon-capacity "weapon capacity needed")
           (render-attribute outfit :engine-capacity "engine capacity needed")]]
         [:div.col-md-4
          (when (contains? outfit :hyperdrive)
            [:p.italic "Allows you to make hyperjumps."])
          (when (contains? outfit :jump-drive)
            [:p.italic "Lets you jump to any nearby system."])
          (when (= (:installable outfit) -1)
            [:p.italic "This is not an installable item."])
          [:ul
           (render-attribute outfit :mass "mass")
           (render-attribute outfit :thrust "thrust")
           (render-attribute outfit :thrusting-energy "thrusting energy")
           (render-attribute outfit :thrusting-heat "thrusting heat")
           (render-attribute outfit :turn "turn")
           (render-attribute outfit :turning-energy "turning energy")
           (render-attribute outfit :turning-heat "turning heat")
           (render-attribute outfit :afterburner-energy "afterburner energy")
           (render-attribute outfit :afterburner-fuel "afterburner fuel")
           (render-attribute outfit :afterburner-heat "afterburner heat")
           (render-attribute outfit :afterburner-thrust "afterburner thrust")
           (render-attribute outfit :reverse-thrust "reverse thrust")
           (render-attribute outfit :reverse-thrusting-energy "reverse thrusting energy")
           (render-attribute outfit :reverse-thrusting-heat "reverse thrusting heat")
           (render-attribute outfit :energy-generation "energy generation")
           (render-attribute outfit :solar-collection "solar collection")
           (render-attribute outfit :energy-capacity "energy capacity")
           (render-attribute outfit :energy-consumption "energy consumption")
           (render-attribute outfit :heat-generation "heat generation")
           (render-attribute outfit :cooling "cooling")
           (render-attribute outfit :active-cooling "active cooling")
           (render-attribute outfit :cooling-energy "cooling energy")
           (render-attribute outfit :hull-repair-rate "hull repair rate")
           (render-attribute outfit :hull-energy "hull energy")
           (render-attribute outfit :hull-heat "hull heat")
           (render-attribute outfit :shield-generation "shield generation")
           (render-attribute outfit :shield-energy "shield energy")
           (render-attribute outfit :shield-heat "shield heat")
           (render-attribute outfit :ramscoop "ramscoop")
           (render-attribute outfit :required-crew "required crew")
           (render-attribute outfit :capture-attack "capture attack")
           (render-attribute outfit :capture-defense "capture defense")
           (render-attribute outfit :illegal "illegal")
           (render-attribute outfit :cargo-space "cargo space")
           (render-attribute outfit :cooling-inefficiency "cooling inefficiency")
           (render-attribute outfit :heat-dissipation "heat dissipation")
           (render-attribute outfit :fuel-capacity "fuel capacity")
           (render-attribute outfit :jump-fuel "jump fuel")
           (render-attribute outfit :jump-speed "jump speed")
           (render-attribute outfit :scram-drive "scram drive")
           (render-attribute outfit :atmosphere-scan "atmosphere scan")
           (render-attribute outfit :cargo-scan-power "cargo scan power")
           (render-attribute outfit :cargo-scan-speed "cargo scan speed")
           (render-attribute outfit :outfit-scan-power "outfit scan power")
           (render-attribute outfit :outfit-scan-speed "outfit scan speed")
           (render-attribute outfit :asteroid-scan-power "asteroid scan power")
           (render-attribute outfit :tactical-scan-power "tactical scan power")
           (render-attribute outfit :scan-interference "scan interference")
           (render-attribute outfit :radar-jamming "radar jamming")
           (render-attribute outfit :cloak "cloak")
           (render-attribute outfit :cloaking-energy "cloaking energy")
           (render-attribute outfit :cloaking-fuel "cloaking fuel")
           (render-attribute outfit :bunks "bunks")
           (render-attribute outfit :automaton "automaton")
           (render-attribute outfit :quantum-keystone "quantum keystone")
           (render-attribute outfit :map "map")
           (render-ammo outfit)
           (render-attribute outfit :gatling-round-capacity "gatling round capacity")
           (render-attribute outfit :javelin-capacity "javelin capacity")
           (render-attribute outfit :finisher-capacity "finisher capacity")
           (render-attribute outfit :tracker-capacity "tracker capacity")
           (render-attribute outfit :rocket-capacity "rocket capacity")
           (render-attribute outfit :minelayer-capacity "minelayer capacity")
           (render-attribute outfit :piercer-capacity "piercer capacity")
           (render-attribute outfit :meteor-capacity "meteor capacity")
           (render-attribute outfit :railgun-slug-capacity "railgun slug capacity")
           (render-attribute outfit :sidewinder-capacity "sidewinder capacity")
           (render-attribute outfit :thunderhead-capacity "thunderhead capacity")
           (render-attribute outfit :torpedo-capacity "torpedo capacity")
           (render-attribute outfit :typhoon-capacity "typhoon capacity")
           (render-attribute outfit :emp-torpedo-capacity "EMP torpedo capacity")
           (when (contains? outfit :weapon)
             [weapon-attributes (:weapon outfit)])]
          (when (contains? outfit :unplunderable)
            [:p.italic "This outfit cannot be plundered."])]]]]]
 [:div.col-md-6
  [:div.card.text-dark.bg-light
   [:div.card-header (str "Installed on " (count installations) " ships")]
   (when (seq installations)
     [:div.card-body
      [:div.list-group
       (for [{:keys [ship-name ship-modification quantity]} installations]
         (let [url (if (some? ship-modification)
                     (routes/ship-modification-url ship-name ship-modification)
                     (routes/ship-url ship-name))
               link-text (if (some? ship-modification)
                           ship-modification ship-name)
               key [ship-name ship-modification]]
           ^{:key key}
             (if (= quantity 1)
               [:li.list-group-item.list-group-item-action [:a {:href url} link-text]]
               [:li
                {:class [:list-group-item
                         :d-flex
                         :justify-content-between
                         :align-items-center]}
                [:a {:href url} link-text] [:span.badge.bg-secondary.rounded-pill quantity]])))]])]]
 [:div.col-md-6
  [:div.card.text-dark.bg-light
   [:div.card-header (str "Sold at " (count planets) " planets")]
   (when (seq planets)
     [:div.card-body
      [:ul.list-group
       (for [{:keys [name system]} planets]
         ^{:key name} [:li.list-group-item
                       name
                       " "
                       [:span.badge.bg-secondary system]])]])]]]]))
