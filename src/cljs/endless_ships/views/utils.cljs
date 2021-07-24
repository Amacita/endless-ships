(ns endless-ships.views.utils
  (:require [clojure.string :as str])
  (:import (goog.i18n NumberFormat)
           (goog.i18n.NumberFormat Format)))

(def nbsp "\u00a0")

(defn nbspize [s]
  (str/replace s #" " nbsp))

(defn kebabize [s]
  (-> s
      (str/replace #"\s+" "-")
      (str/replace #"[\?']" "")
      str/lower-case))

(defn race-label [race]
  ^{:key race} [:span.label
                {:class (str "label-" (-> race name kebabize))}
                race])

(defn format-number [num]
  (if (number? num)
    (let [rounded (-> num
                      (* 10)
                      js/Math.round
                      (/ 10))
          formatter (NumberFormat. Format/DECIMAL)]
      (.format formatter (str rounded)))
    num))

(defn render-attribute [m prop label]
  (let [v (prop m)]
    (when (some? v)
      (if (number? v)
        [:li (str label ": " (format-number v))]
        [:li (str label ": " v)]))))

(defn render-percentage [m prop label]
  (let [v (prop m)]
    (when (some? v)
      [:li (str label ": " (format-number (* v 100)) "%")])))

(defn render-description [entity]
  (->> (:description entity)
       (map-indexed (fn [idx paragraph]
                      [paragraph
                       ^{:key idx} [:span [:br] [:br]]]))
       (apply concat)
       butlast))
