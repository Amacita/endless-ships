(ns endless-ships.utils.outfits)

(defn- damage [damage-type gun]
  (get-in gun [:weapon damage-type :per-second]))

(def types
  (array-map :thrusters {:header "Thrusters"
                         :filter #(contains? % :thrust)
                         :initial-ordering {:column-name "Thrust per space"
                                            :order :desc}}
             :steerings {:header "Steerings"
                         :filter #(contains? % :turn)
                         :initial-ordering {:column-name "Turn per space"
                                            :order :desc}}
             :reactors {:header "Reactors"
                        :filter #(or (contains? % :energy-generation)
                                     (contains? % :solar-collection))
                        :initial-ordering {:column-name "Energy per space"
                                           :order :desc}}
             :batteries {:header "Batteries"
                         :filter #(and (contains? % :energy-capacity)
                                       (#{"Power" "Systems"} (:category %)))
                         :initial-ordering {:column-name "Energy per space"
                                            :order :desc}}
             :coolers {:header "Coolers"
                       :filter #(or (contains? % :cooling)
                                    (contains? % :active-cooling))
                       :initial-ordering {:column-name "Cooling per space"
                                          :order :desc}}
             :shields {:header "Shield generators"
                       :filter #(contains? % :shield-generation)
                       :initial-ordering {:column-name "Shield per space"
                                          :order :desc}}
             :hull-repair {:header "Hull repair modules"
                           :filter #(contains? % :hull-repair-rate)
                           :initial-ordering {:column-name "Hull per space"
                                              :order :desc}}
             :ramscoops {:header "Ramscoops"
                         :filter #(contains? % :ramscoop)
                         :initial-ordering {:column-name "Ramscoop per space"
                                            :order :desc}}
             :guns {:header "Guns"
                    :filter #(= (:category %) "Guns")
                    :initial-ordering {:column-name "Shield damage / space"
                                       :order :desc}}
             :secondary {:header "Secondary weapons"
                         :filter #(= (:category %) "Secondary Weapons")
                         :initial-ordering {:column-name "Shield damage / space"
                                            :order :desc}}
             :turrets {:header "Turrets"
                       :filter #(and (= (:category %) "Turrets")
                                     (or (some? (damage :shield-damage %))
                                         (some? (damage :hull-damage %))))
                       :initial-ordering {:column-name "Shield damage / space"
                                          :order :desc}}
             :anti-missile {:header "Anti-missile turrets"
                            :filter #(-> % :weapon (contains? :anti-missile))
                            :initial-ordering {:column-name "Anti-missile"
                                               :order :desc}}
             :hand-to-hand {:header "Hand to Hand"
                            :filter #(= (:category %) "Hand to Hand")
                            :initial-ordering {:column-name "Capture attack"
                                               :order :desc}}))
