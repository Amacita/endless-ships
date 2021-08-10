(ns endless-ships.ships
  (:require [clojure.set :refer [rename-keys]]
            [endless-ships.parser :refer [->map]]
            [endless-ships.plugins :refer [file->plugin file->race image-source]]
            [endless-ships.outfits :refer [assoc-outfits-cost]]))

(defn- add-key-if [cond key value]
  (if cond
    {key value}
    {}))

(defn- process-ship [[_
                      [ship-name ship-modification]
                      {[[[sprite] animation]] "sprite"
                       [[[thumbnail]]] "thumbnail"
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
         (add-key-if (contains? ship "thumbnail")
                     :thumbnail
                     thumbnail)
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

(defn- ship->image-file [ship]
  (cond
    (contains? ship :thumbnail) (str (:thumbnail ship) ".png")
    (and (contains? ship :sprite) (false? (second (:sprite ship)))) (str (first (:sprite ship)) ".png")
    :else "animation-detection-does-not-exist"))

(defn ships-data [data outfit-data]
  (->> (ships data)
       (map (fn [cship]
              (-> cship
                 (select-keys [:name :sprite :thumbnail :licenses :file
                               :cost :category :hull :shields :mass
                               :engine-capacity :weapon-capacity :fuel-capacity
                               :outfits :outfit-space :cargo-space
                               :required-crew :bunks :description
                               :guns :turrets :drones :fighters
                               :self-destruct :ramscoop])
                 (assoc :race (file->race (:file cship)))
                 (assoc-in [:meta :plugin] (:key (file->plugin (:file cship))))
                 (assoc-in [:meta :image :file] (ship->image-file cship))
                 (#(assoc-in % [:meta :image :origin] (image-source %)))
                 (rename-keys {:cost :empty-hull-cost}))))
       (map #(assoc-outfits-cost % outfit-data))))

(defn modifications [data]
  (->> data
       (filter #(and (= (first %) "ship")
                     (= (-> % second count) 2)))
       (map process-ship)))

(defn modifications-data [data outfit-data]
  (->> (modifications data)
       (map #(-> %
                 (rename-keys {:cost :empty-hull-cost})))
       (map #(assoc-outfits-cost % outfit-data))))
