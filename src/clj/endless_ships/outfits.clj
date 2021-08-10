(ns endless-ships.outfits
  (:require [endless-ships.parser :refer [->map]]
            [endless-ships.plugins :refer [file->plugin file->race image-source]]
            [camel-snake-kebab.core :as csk]
            [clojure.string :as str]))

(defn- outfit->image-file [outfit]
  (str (:thumbnail outfit) ".png"))

(defn- update-if-present [m k f]
  (if (contains? m k)
    (update m k f)
    m))

(defn- float-is-round? [num]
  (= (-> num int float) num))

(defn- round-to-5-digits [num]
  (if (integer? num)
    num
    (let [float-num (-> (format "%.5g" (float num))
                        (clojure.string/replace \, \.)
                        Float/parseFloat)]
      (if (float-is-round? float-num)
        (int float-num)
        float-num))))

(def attribute-convertors
  (let [times-3600 (comp round-to-5-digits (partial * 3600))
        times-60 (comp round-to-5-digits (partial * 60))]
    {:outfit-space -
     :weapon-capacity -
     :engine-capacity -
     :cost #(if (integer? %) % (Integer/parseInt %))
     ;; thrusters
     :thrust times-3600
     :thrusting-energy times-60
     :thrusting-heat times-60
     ;; steerings
     :turn times-60
     :turning-energy times-60
     :turning-heat times-60
     ;; reverse thrusters
     :reverse-thrust times-3600
     :reverse-thrusting-energy times-60
     :reverse-thrusting-heat times-60
     ;; afterburners
     :afterburner-thrust times-3600
     :afterburner-energy times-60
     :afterburner-fuel times-60
     :afterburner-heat times-60
     ;; reactors & solar collectors
     :energy-generation times-60
     :heat-generation times-60
     :solar-collection times-60
     :energy-consumption times-60
     ;; coolers
     :cooling times-60
     :active-cooling times-60
     :cooling-energy times-60
     ;; shield generators
     :shield-generation times-60
     :shield-energy times-60
     :shield-heat times-60
     ;; hull repair modules
     :hull-repair-rate times-60
     :hull-energy times-60
     :hull-heat times-60
     ;; cloaking device
     :cloaking-energy times-60
     :cloaking-fuel times-60}))

(def weapon-attribute-convertors
  (let [times-60 (partial * 60)
        times-100 (partial * 100)]
    {:ion-damage times-100
     :slowing-damage times-100
     :disruption-damage times-100
     :turret-turn times-60}))

(defn- calculate-damage [weapon-attrs submunition submunition-count damage-type]
  (let [per-shot (if (some? submunition)
                   (+ (get weapon-attrs damage-type 0)
                      (* (get-in submunition [:weapon damage-type] 0)
                         (or submunition-count 1)))
                   (get weapon-attrs damage-type))
        per-second (when (and (some? per-shot)
                              (not= per-shot 0))
                     (/ (* per-shot 60) (:reload weapon-attrs)))]
    (when (some? per-second)
      (merge {:per-second (round-to-5-digits per-second)}
             (if (> (:reload weapon-attrs) 1)
               {:per-shot (round-to-5-digits per-shot)}
               {})))))

(defn- normalize-weapon-attrs [outfits]
  (map
   (fn [{category :category
         {:keys [reload velocity velocity-override lifetime shield-damage hull-damage]
          [submunition-name submunition-count] :submunition
          :as weapon-attrs} :weapon
         :as outfit}]
     (if (#{"Guns" "Secondary Weapons" "Turrets"} category)
       (let [shots-per-second (if (= reload 1)
                                "continuous"
                                (->> reload
                                     (/ 60M)
                                     (with-precision 5)
                                     float))
             submunition (when (some? submunition-name)
                           (first (filter #(= (:name %) submunition-name) outfits)))
             range (if (some? submunition)
                     (let [final-velocity (or velocity-override velocity)
                           total-lifetime (+ (or lifetime 0)
                                             (get-in submunition [:weapon :lifetime] 1))]
                       (* final-velocity total-lifetime))
                     (* velocity lifetime))
             converted-weapon-attrs (reduce (fn [attrs [attr-name convertor]]
                                              (update-if-present attrs attr-name convertor))
                                            weapon-attrs
                                            weapon-attribute-convertors)]
         (assoc outfit
                :weapon
                (merge converted-weapon-attrs
                       {:shots-per-second shots-per-second
                        :range range}
                       (reduce (fn [damage-attrs damage-type]
                                 (if-let [attr-value (calculate-damage converted-weapon-attrs
                                                                       submunition
                                                                       submunition-count
                                                                       damage-type)]
                                   (assoc damage-attrs damage-type attr-value)
                                   damage-attrs))
                               {}
                               [:shield-damage :hull-damage :heat-damage
                                :ion-damage :disruption-damage :slowing-damage
                                :firing-energy :firing-heat :firing-fuel]))))
       (dissoc outfit :weapon)))
   outfits))

(defn outfits [data]
  (->> data
       (filter #(= (first %) "outfit"))
       (map (fn [[_
                  [name]
                  {description-attrs "description"
                   license-attrs "licenses"
                   weapon-attrs "weapon"
                   [[[category]]] "category"
                   [[[thumbnail]]] "thumbnail"
                   file "file"
                   :as attrs}]]
              (merge (->map attrs)
                     {:name name
                      :category category
                      :licenses (->> license-attrs
                                     (map #(-> % (get 1) keys))
                                     (apply concat)
                                     vec)
                      :weapon (-> weapon-attrs
                                  (get-in [0 1])
                                  ->map
                                  (assoc :submunition (get-in weapon-attrs [0 1 "submunition" 0 0])))
                      :description (->> description-attrs
                                        (map #(get-in % [0 0]))
                                        vec)
                      :file file
                      :thumbnail thumbnail
                      :race (file->race file)})))
       normalize-weapon-attrs
       (map (fn [outfit]
              (reduce (fn [attrs [attr-name convertor]]
                        (update-if-present attrs attr-name convertor))
                      outfit
                      attribute-convertors)))
       (map (fn [outfit]
              (-> outfit
                  (assoc-in [:meta :plugin] (:key (file->plugin (:file outfit))))
                  (assoc-in [:meta :image :file] (outfit->image-file outfit))
                  (#(assoc-in % [:meta :image :origin] (image-source %))))))))

(defn assoc-outfits-cost [ship outfits-data]
  (let [outfits (:outfits ship)]
    (if (empty? outfits)
      ship
      (assoc ship
             :outfits-cost
             (reduce (fn [cost {:keys [name quantity]}]
                       (let [outfit (->> outfits-data
                                         (filter #(= (:name %) name))
                                         first)]
                         (+ cost
                            (* (get outfit :cost 0)
                               quantity))))
                     0
                     outfits)))))

(defn- required-licenses [data]
  "Given outfits or ships, returns a map of all licenses that are required to purchase them."
  (->> data
       (filter #(seq (:licenses %)))
       (map #(select-keys % [:licenses :file]))
       (map #(list (:licenses %) (file->race (:file %))))
       set
       (map (fn [outer]
              (map (fn [inner]
                     (list inner (csk/->kebab-case-string (second outer))))
                   (first outer))))
       flatten
       (apply hash-map)))

(defn- defined-licenses [outfits]
  "Returns a map of all licenses that are defined as special outfits."
  (let [licenses (filter #(and
                            (= (:category %) "Special")
                            (str/ends-with? (:name %) " License"))
                         outfits)]
    (zipmap (map #(str/replace (:name %) " License" "") licenses)
            (map #(csk/->kebab-case-string (file->race (:file %))) licenses))))

(defn licenses->race [outfits ships]
  "Returns a map of all licenses to the race they belong to."
  (merge
    (required-licenses outfits)
    (required-licenses ships)
    (defined-licenses outfits)))
