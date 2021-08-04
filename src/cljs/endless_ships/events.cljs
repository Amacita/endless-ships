(ns endless-ships.events
  (:require [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.edn :as ajax]
            [endless-ships.views.utils :refer [kebabize]]
            [endless-ships.utils.outfits :as outfits]
            [clojure.pprint :refer [pprint]]))

(def initial-outfit-settings
  (reduce (fn [settings [name {:keys [initial-ordering]}]]
            (assoc settings
                   name
                   {:ordering [[0 :asc]]}))
          {}
          outfits/types))

(rf/reg-event-fx ::initialize
                 (fn [{db :db} _]
                   {:db {:loading? true
                         :loading-failed? false
                         :route [:ships {}]
                         :ships {}
                         :ship-modifications {}
                         :outfits {}
                         :outfitters []
                         :version {}
                         :gw-version {}
                         :licenses {}
                         :plugins {}
                         :settings (merge {:ships {:ordering [[0 :asc]]
                                                   :filters-collapsed? true
                                                   :race-filter {}
                                                   :category-filter {}
                                                   :license-filter {}}}
                                          initial-outfit-settings)}
                    :http-xhrio {:method :get
                                 :uri "data.edn"
                                 :response-format (ajax/edn-response-format)
                                 :on-success [::data-loaded]
                                 :on-failure [::data-failed-to-load]}}))

(defn- index-by-name [coll]
  (reduce (fn [indexed {:keys [name] :as item}]
            (assoc indexed (kebabize name) item))
          {}
          coll))

(defn- group-modifications [modifications]
  (reduce (fn [grouped {:keys [name modification] :as mod}]
            (assoc-in grouped [(kebabize name) (kebabize modification)] mod))
          {}
          modifications))

(defn- process-outfitters [outfitters]
  (map (fn [outfitter]
         (-> outfitter
             (dissoc :name)
             (update :outfits set)))
       outfitters))

(defn- toggle-filter [filter value]
  (update filter value not))

(defn- initial-filter [values]
  (->> values
       (into #{})
       (reduce toggle-filter (sorted-map))))

(rf/reg-event-db ::data-loaded
                 (fn [db [_ data]]
                   (-> db
                       (assoc :loading? false
                              :ships (:ships data)
                              :ship-modifications (group-modifications (:ship-modifications data))
                              :outfits (:outfits data)
                              :outfitters (process-outfitters (:outfitters data))
                              :version (:version data)
                              :gw-version (:gw-version data)
                              :licenses (:licenses data)
                              :plugins (:plugins data))
                       (update-in [:settings :ships]
                                  merge
                                  {:race-filter (->> (:ships data)
                                                     (map :race)
                                                     initial-filter)
                                   :category-filter (->> (:ships data)
                                                         (map :category)
                                                         initial-filter)
                                   :license-filter (->> (:ships data)
                                                        (map :licenses)
                                                        (apply concat)
                                                        (keep identity)
                                                        initial-filter)}))))

(rf/reg-event-db ::data-failed-to-load
                 (fn [db _]
                   (assoc db
                          :loading? false
                          :loading-failed? true)))

(rf/reg-event-db ::navigate-to
                 (fn [db [_ route]]
                   (assoc db :route route)))

;; Maintains multiple sort columns each with individual directions. The
;; column numbers are in model coordinates.
;;
;; A column is initially set to ascending order and toggled thereafter.
;; If a column is not present in list it is appended or becomes the only
;; element when 'append' is false.
(rf/reg-event-db ::toggle-ordering
                 (fn [db [_ entity-type model-col append]]
                   (update-in db
                              [:settings entity-type :ordering]
                              (fn [sorting]
                                (if-not append
                                  [[model-col (if (= (first sorting) [model-col :asc]) :desc :asc)]]
                                  (loop [sorting sorting
                                         found false
                                         result []]
                                    (let [column (first sorting)
                                          this-col (first column)
                                          this-dir (second column)]
                                      (if column
                                        (if (= model-col this-col)
                                          (recur (rest sorting)
                                                 true
                                                 (conj result [model-col (if (= this-dir :asc) :desc :asc)]))
                                          (recur (rest sorting)
                                                 found
                                                 (conj result column)))
                                        (if found
                                          result
                                          (conj result [model-col :asc]))))))))))

(rf/reg-event-db ::toggle-ship-filters-visibility
                 (fn [db]
                   (update-in db
                              [:settings :ships :filters-collapsed?]
                              not)))

(rf/reg-event-db ::toggle-filter
                 (fn [db [_ entity-type filter-type item]]
                   (update-in db
                              [:settings entity-type filter-type item]
                              not)))

(rf/reg-event-db ::toggle-filter-group
                 (fn [db [_ entity-type filter-type]]
                   (let [path [:settings entity-type filter-type]
                         filters (vals (get-in db path))
                         new-val (cond (every? true? filters) false
                                       (some true? filters) true
                                       :else true)]
                     (update-in db path #(into (sorted-map) (map (fn [[k,v]] [k new-val]) %))))))
