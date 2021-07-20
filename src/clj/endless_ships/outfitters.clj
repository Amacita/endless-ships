(ns endless-ships.outfitters)

(defn- find-object-with-name [objects name]
  (some (fn [object]
          (let [object-name (get-in object [0 0])
                child-objects (get-in object [1 "object"])]
            (or (= object-name name)
                (find-object-with-name child-objects name))))
        objects))

(defn- find-system-with-planet [planet-name data]
  (->> data
       (filter #(= (first %) "system"))
       (filter (fn [system]
                 (let [planets (get-in system [2 "object"])]
                   (find-object-with-name planets planet-name))))
       first))

(defn- planets-with-outfitter [outfitter-type data]
  (let [planets (filter #(= (first %) "planet") data)]
    (->> planets
         (filter (fn [planet]
                   (some #(= (get-in % [0 0]) outfitter-type)
                         (get-in planet [2 "outfitter"]))))
         (map (fn [[_ [planet-name]]]
                {:name planet-name
                 :system (-> (find-system-with-planet planet-name data)
                             (get-in [1 0]))})))))

(defn outfitters [data]
  (->> data
       (filter #(= (first %) "outfitter"))
       (map (fn [[_ [name] outfits]]
              {:name name
               :outfits (->> outfits keys (remove #{"file"}))
               :planets (planets-with-outfitter name data)}))))
