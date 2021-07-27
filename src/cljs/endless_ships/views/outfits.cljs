(ns endless-ships.views.outfits
  (:require [re-frame.core :as rf]
            [reagent-table.core :as rt]
            [endless-ships.views.utils :refer [kebabize race-label nbspize format-number]]
            [endless-ships.utils.outfits :as utils]
            [endless-ships.subs :as subs]
            [endless-ships.routes :as routes]
            [clojure.pprint :refer [pprint]]))

(def default-columns
  [{:header "Name"
    :path [:name]
    :key :name}
   {:header "Race"
    :path [:race]
    :key :race}
   {:header "Cost"
    :path [:cost]
    :key :cost
    :format format-number}
   {:header "Outfit sp."
    :path [:outfit-space]
    :key :outfit-space
    :format format-number}])

(defn- damage [damage-type gun]
  (get-in gun [:weapon damage-type :per-second]))


(def weapon-column-info
  [{:header "Shield damage"
    :path [:weapon :shield-damage :per-second]
    :key :shield-damage
    :format format-number}
   {:header "Shield damage / space"
    :expr #(/ (damage :shield-damage %) (:outfit-space %))
    :key :shield-damage-per-second
    :format format-number}
   {:header "Hull damage"
    :path [:weapon :shield-damage :per-second]
    :key :hull-damage
    :format format-number}
   {:header "Hull damage / space"
    :expr #(/ (damage :hull-damage %) (:outfit-space %))
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

(defn- cell-data
  "Resolve the data within a row for a specific column"
  [row cell]
  (let [{:keys [path expr]} cell]
    (or (and path
             (get-in row path))
        (and expr
             (expr row)))))

(defn- cell-fn
  "Return the cell hiccup form for rendering.
   the specific column from :column-model
  - row the current row
  - row-num the row number
  - col-num the column number in model coordinates"
  [render-info row row-num col-num]
  (let [{:keys [format attrs]
         :or   {format identity
                attrs (fn [_] {})}} render-info
        data    (cell-data row render-info)
        content (format data)
        attrs   (attrs data)]
    [:span
     attrs
     content]))


(defn date?
  "Returns true if the argument is a date, false otherwise."
  [d]
  (instance? js/Date d))

(defn date-as-sortable
  "Returns something that can be used to order dates."
  [d]
  (.getTime d))

(defn compare-vals
  "A comparator that works for the various types found in table structures.
  This is a limited implementation that expects the arguments to be of
  the same type. The :else case is to call compare, which will throw
  if the arguments are not comparable to each other or give undefined
  results otherwise.
  Both arguments can be a vector, in which case they must be of equal
  length and each element is compared in turn."
  [x y]
  (cond
    (and (vector? x)
         (vector? y)
         (= (count x) (count y)))
    (reduce #(let [r (compare (first %2) (second %2))]
               (if (not= r 0)
                 (reduced r)
                 r))
            0
            (map vector x y))

    (or (and (number? x) (number? y))
        (and (string? x) (string? y))
        (and (boolean? x) (boolean? y)))
    (compare x y)

    (and (date? x) (date? y))
    (compare (date-as-sortable x) (date-as-sortable y))

    :else ;; hope for the best... are there any other possiblities?
    (compare x y)))

(defn- sort-fn
  "Generic sort function for tabular data. Sort rows using data resolved from
  the specified columns in the column model."
  [rows column-model sorting]
  (sort (fn [row-x row-y]
          (reduce
            (fn [_ sort]
              (let [column (column-model (first sort))
                    direction (second sort)
                    cell-x (cell-data row-x column)
                    cell-y (cell-data row-y column)
                    compared (if (= direction :asc)
                               (compare-vals cell-x cell-y)
                               (compare-vals cell-y cell-x))]
                (when-not (zero? compared)
                  (reduced compared))
                ))
            0
            sorting))
        rows))

(defn- row-key-fn
  "Return the reagent row key for the given row"
  [row row-num]
  (get-in row [:name]))

(def default-table-config
  {:render-cell cell-fn
   ;:table-state  table-state
   :scroll-height "80vh"
   :sort         sort-fn
   :row-key      row-key-fn
   :table-container {:style {:border "1px solid black"}}
   :th {:style {:border "1px solid black" :background-color "white"}}
   :column-selection {:ul {:li {:class "btn"}}}
   :table {:class "table table-hover table-striped table-bordered table-transition"
           :style {:border-spacing 0
                   :border-collapse "separate"}}})

(def table-state (atom {:draggable true}))

(defn outfits []
  [:div.app
   (map (fn [[type type-attrs]]
          (let [rows (->> @(rf/subscribe [::subs/outfits-of-type type]))]
            [:div [:h2 (:header type-attrs)]
             [rt/reagent-table
              (atom rows)
              (merge {:column-model (into [] (concat default-columns (type columns)))}
                     default-table-config)]]))
        utils/types)])

(comment
(defn outfits []
  [:div.app
   (->> utils/types
        (map (fn [[type type-attrs]]
               (let [rows (->> @(rf/subscribe [::subs/outfit-names type])
                               (map (fn [name]
                                      (let [outfit @(rf/subscribe [::subs/outfit name])]
                                        ^{:key name}
                                        [:tr
                                         [left-cell ^{:key name} [routes/outfit-link name]]
                                         [left-cell (race-label (:race outfit))]
                                         [right-cell (format-number (:cost outfit))]
                                         (map-indexed (fn [idx {:keys [value]}]
                                                        ^{:key [(or (value outfit) 0) idx]}
                                                        [right-cell (format-number (value outfit))])
                                                      (-> type-attrs :columns vals))
                                         [left-cell (->> (:licenses outfit)
                                                         (map (fn [license] @(rf/subscribe [::subs/license-label license])))
                                                         (interpose " "))]])))
                               doall)
                     ordering @(rf/subscribe [::subs/outfits-ordering type])]
                 ^{:key type} [:div
                               [:h2 (:header type-attrs)]
                               [table type (utils/columns-for type) ordering rows]])))
        doall)])
)
