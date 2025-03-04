(ns endless-ships.subs
  (:require [re-frame.core :as rf]
            [endless-ships.utils.ships :as ships]
            [endless-ships.utils.outfits :as outfits]
            [reagent-table.utils :as tables]
            [endless-ships.views.utils :refer [kebabize]]))

(rf/reg-sub ::loading?
            (fn [db]
              (:loading? db)))

(rf/reg-sub ::loading-failed?
            (fn [db]
              (:loading-failed? db)))

(rf/reg-sub ::route
            (fn [db]
              (:route db)))

(rf/reg-sub ::ships
            (fn [db]
              (-> db :ships)))

(rf/reg-sub ::ship-filters-collapsed?
            (fn [db]
              (get-in db [:settings :ships :filters-collapsed?])))

(rf/reg-sub ::filter
            (fn [db [_ entity-type filter-type]]
              (get-in db [:settings entity-type filter-type])))

(rf/reg-sub ::ships-race-filter
            (fn [db]
              (get-in db [:settings :ships :race-filter])))

(rf/reg-sub ::ships-category-filter
            (fn [db]
              (get-in db [:settings :ships :category-filter])))

(rf/reg-sub ::ships-license-filter
            (fn [db]
              (get-in db [:settings :ships :license-filter])))

(defn- sort-with-settings [columns ordering coll]
  (let [ordering-prop (get-in columns [(:column-name ordering) :value])]
    (sort (if (some? (:column-name ordering))
            (fn [item1 item2]
              (let [item1-prop (ordering-prop item1)
                    item2-prop (ordering-prop item2)]
                (if (= (:order ordering) :asc)
                  (compare item1-prop item2-prop)
                  (compare item2-prop item1-prop))))
            (constantly 0))
          coll)))

(rf/reg-sub ::filtered-ships
            (fn []
              [(rf/subscribe [::ships])
               (rf/subscribe [::entity-ordering :ships])
               (rf/subscribe [::filter :plugins :plugin-filter])
               (rf/subscribe [::ships-race-filter])
               (rf/subscribe [::ships-category-filter])
               (rf/subscribe [::ships-license-filter])])
            (fn [[all-ships ordering plugin-filter race-filter category-filter license-filter] [_ column-model]]
              (let [filtered-ships
                    (->> all-ships
                         (filter (fn [ship]
                                   (and (get plugin-filter (get-in ship [:meta :plugin] ship))
                                        (get race-filter (:race ship))
                                        (get category-filter (:category ship))
                                        (not-any? (fn [license]
                                                    (not (get license-filter license)))
                                                  (get ship :licenses []))))))]
                (tables/table-sort-fn filtered-ships column-model ordering))))

; Linear search
(rf/reg-sub ::ship
            (fn [db [_ name]]
              (first (filter #(= name (kebabize (:name %))) (:ships db)))))

(rf/reg-sub ::outfits
            (fn [db]
              (:outfits db)))

(rf/reg-sub ::ship-modifications-names
            (fn [db [_ ship-name]]
              (->> (get-in db [:ship-modifications ship-name])
                   vals
                   (map :modification))))

(rf/reg-sub ::ship-modification
            (fn [db [_ ship-name modification-name]]
              (get-in db [:ship-modifications ship-name modification-name])))

; Linear search
(rf/reg-sub ::outfit
            (fn [db [_ name]]
              (first (filter #(= name (kebabize (:name %))) (:outfits db)))))

(rf/reg-sub ::outfit-installations
            (fn [db [_ name]]
              (->> (concat (:ships db)
                           (->> (:ship-modifications db)
                                vals
                                (mapcat vals)))
                   (reduce (fn [installations ship]
                             (if-let [ship-outfit (->> (:outfits ship)
                                                       (filter #(= (:name %) name))
                                                       first)]
                               (conj installations
                                     {:ship-name (:name ship)
                                      :ship-modification (:modification ship)
                                      :quantity (:quantity ship-outfit)})
                               installations))
                           [])
                   (sort-by (juxt :ship-name :ship-modification)))))

(rf/reg-sub ::outfit-planets
            (fn [db [_ name]]
              (->> (:outfitters db)
                   (filter (fn [{:keys [outfits]}]
                             (outfits name)))
                   (mapcat :planets)
                   (into #{})
                   (sort-by :name))))

(rf/reg-sub ::entity-ordering
            (fn [db [_ entity-type]]
              (get-in db [:settings entity-type :ordering])))

(rf/reg-sub ::outfits-of-type
            (fn [[_ outfit-type]]
              [(rf/subscribe [::outfits])
               (rf/subscribe [::entity-ordering outfit-type])
               (rf/subscribe [::filter :plugins :plugin-filter])
               (rf/subscribe [::filter :outfits :race-filter])
               (rf/subscribe [::filter :outfits :license-filter])])
            (fn [[outfits ordering plugin-filter race-filter license-filter] [_ outfit-type column-model]]
              (let [filtered-outfits (->> outfits
                                          (filter (get-in outfits/types [outfit-type :filter])) ; filter function defined in data
                                          (filter (fn [outfit]
                                                    (and (get plugin-filter (get-in outfit [:meta :plugin] outfit))
                                                         (get race-filter (:race outfit))
                                                         (not-any? (fn [license]
                                                                     (not (get license-filter license)))
                                                                   (get outfit :licenses []))))))]
                (tables/table-sort-fn filtered-outfits column-model ordering))))

(rf/reg-sub ::plugin-version
            (fn [db [_ plugin]]
              (get-in db [:plugins plugin :version])))

(rf/reg-sub ::plugin-repository
            (fn [db [_ plugin]]
              (get-in db [:plugins plugin :repository])))

(rf/reg-sub ::plugin-name
            (fn [db [_ plugin]]
              (get-in db [:plugins plugin :name])))

(rf/reg-sub ::license-label
            (fn [db [_ license]]
              (let [style (get (:licenses db) license)]
                ^{:key license} [:span.badge.rounded-pill {:class (str "label-" style)} license])))

(rf/reg-sub ::license-style
            (fn [db [_ license]]
              (let [style (get (:licenses db) license)]
                style)))

(rf/reg-sub ::plugin
            (fn [db [_ plugin-key]]
              (get (:plugins db) plugin-key)))

(rf/reg-sub ::plugin-keys
            (fn [db [_]]
              (keys (:plugins db))))

(rf/reg-sub ::debug
            (fn [db [_ path]]
              (get-in db path)))

(rf/reg-sub ::settings
            (fn [db]
              (get-in db [:settings])))

(rf/reg-sub ::sort-mode
            (fn [[_ entity-type model-col]]
              [(rf/subscribe [::entity-ordering entity-type])])
            (fn [[ordering] [_ entity-type model-col]]
              (-> (filter #(= (first %) model-col) ordering)
                  first
                  second)))
