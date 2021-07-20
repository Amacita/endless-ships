(ns endless-ships.ships
  (:require [clojure.set :refer [rename-keys]]
            [endless-ships.parser :refer [->map]]
            [endless-ships.outfits :refer [assoc-outfits-cost]]))

(defn- add-key-if [cond key value]
  (if cond
    {key value}
    {}))

(def file->race
  {"kestrel.txt" :human
   "hai ships.txt" :hai
   "pug.txt" :pug
   "wanderer ships.txt" :wanderer
   "quarg ships.txt" :quarg
   "remnant ships.txt" :remnant
   "ka'het ships.txt" :ka'het
   "korath ships.txt" :korath
   "marauders.txt" :pirate
   "coalition ships.txt" :coalition
   "drak.txt" :drak
   "ships.txt" :human
   "indigenous.txt" :indigenous
   "sheragi ships.txt" :sheragi
   "Aumar ships.txt" :aumar
   "Dels ships.txt" :dels
   "Donko ships.txt" :donko
   "Erader Darua ships.txt" :erader
   "Erader Kasiva ships.txt" :erader
   "Erader Narpul ships.txt" :erader
   "Makerurader Ship.txt" :erader
   })

(defn- process-ship [[_
                      [ship-name ship-modification]
                      {[[[sprite] animation]] "sprite"
                       [[_ {[[_ license-attrs]] "licenses"
                            [[_ weapon-attrs]] "weapon"
                            :as attrs}]] "attributes"
                       [[_ outfit-attrs]] "outfits"
                       gun-points "gun"
                       turret-points "turret"
                       drone-points "drone"
                       fighter-points "fighter"
                       description-attrs "description"
                       file "file"
                       :as ship}]]
  (merge (->map attrs)
         {:name ship-name
          :modification ship-modification
          :weapon (->map weapon-attrs)
          :file file}
         (add-key-if (contains? ship "sprite")
                     :sprite
                     [sprite (not (empty? animation))])
         (add-key-if (contains? attrs "licenses")
                     :licenses
                     (-> license-attrs keys vec))
         (add-key-if (contains? ship "outfits")
                     :outfits
                     (map (fn [[outfit-name [[[quantity]]]]]
                            {:name outfit-name
                             :quantity (or quantity 1)})
                          outfit-attrs))
         (add-key-if (> (count gun-points) 0)
                     :guns
                     (count gun-points))
         (add-key-if (> (count turret-points) 0)
                     :turrets
                     (count turret-points))
         (add-key-if (> (count drone-points) 0)
                     :drones
                     (count drone-points))
         (add-key-if (> (count fighter-points) 0)
                     :fighters
                     (count fighter-points))
         (add-key-if (contains? ship "description")
                     :description
                     (->> description-attrs
                          (map #(get-in % [0 0]))
                          vec))))

(defn ships [data]
  (->> data
       (filter #(and (= (first %) "ship")
                     (= (-> % second count) 1)
                     (not= (second %) ["Unknown Ship Type"])))
       (map #(-> (process-ship %)
                 (dissoc :modification)))))

(defn ships-data [data outfit-data]
  (->> (ships data)
       (filter #(some? (file->race (:file %))))
       (map #(-> %
                 (select-keys [:name :sprite :licenses :file
                               :cost :category :hull :shields :mass
                               :engine-capacity :weapon-capacity :fuel-capacity
                               :outfits :outfit-space :cargo-space
                               :required-crew :bunks :description
                               :guns :turrets :drones :fighters
                               :self-destruct :ramscoop])
                 (assoc :race (get file->race (:file %) :other))
                 (dissoc :file)
                 (rename-keys {:cost :empty-hull-cost})))
       (map #(assoc-outfits-cost % outfit-data))))

(defn modifications [data]
  (->> data
       (filter #(and (= (first %) "ship")
                     (= (-> % second count) 2)))
       (map process-ship)))

(defn modifications-data [data outfit-data]
  (->> (modifications data)
       (map #(-> %
                 (dissoc :file)
                 (rename-keys {:cost :empty-hull-cost})))
       (map #(assoc-outfits-cost % outfit-data))))
