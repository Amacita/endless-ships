(ns endless-ships.core
  (:require [clojure.java.io :refer [file resource]]
            [clojure.java.shell :refer [sh]]
            [clojure.set :refer [rename-keys]]
            [clojure.string :as str]
            [endless-ships.outfits :refer [outfits-data]]
            [endless-ships.outfitters :refer [outfitters]]
            [endless-ships.ships :refer [modifications-data ships-data]]
            [endless-ships.parser :refer [parse-data-files]]))

(defn find-data-files [dir]
  "Finds all data files starting at ./resources/<dir>"
  (->> (file-seq (file (resource dir)))
       (filter #(.endsWith (.getName %) ".txt"))))

(def game-version
  (let [git-cmd (fn [& args]
                  (->> (concat ["git"] args [:dir "./resources/game"])
                       (apply sh)
                       :out
                       str/trim))
        commit-hash (git-cmd "rev-parse" "HEAD")
        commit-date (-> (git-cmd "show" "-s" "--format=%ci" "HEAD")
                        (str/split #" ")
                        first)
        [tag commits-since-tag] (-> (git-cmd "describe" "HEAD")
                                    (str/split #"-"))]
    (merge {:hash commit-hash
            :date commit-date}
           (when (nil? commits-since-tag)
             {:tag tag}))))

(def gw-version
  (let [git-cmd (fn [& args]
                  (->> (concat ["git"] args [:dir "./resources/gw"])
                       (apply sh)
                       :out
                       str/trim))
        commit-hash (git-cmd "rev-parse" "HEAD")
        commit-date (-> (git-cmd "show" "-s" "--format=%ci" "HEAD")
                        (str/split #" ")
                        first)
        [tag commits-since-tag] (-> (git-cmd "describe" "HEAD")
                                    (str/split #"-"))]
    (merge {:hash commit-hash
            :date commit-date}
           (when (nil? commits-since-tag)
             {:tag tag}))))

(defn edn [dir]
  (let [files (find-data-files dir)
        data (parse-data-files files)
        complete-outfits (outfits-data data)
        complete-ships (ships-data data complete-outfits)
        complete-modifications (modifications-data data complete-outfits)
        complete-outfitters (outfitters data)
        edn-data {:ships complete-ships
                  :ship-modifications complete-modifications
                  :outfits complete-outfits
                  :outfitters complete-outfitters
                  :version game-version
                  :gw-version gw-version}]
    (with-out-str (clojure.pprint/pprint edn-data))))

(comment
  ;; generate data for frontend development
  (spit "public/data.edn" edn)
  ;; get a list of all possible attribute names
  (->> (ships-data data)
       (map keys)
       (apply concat)
       (into #{}))
  ;; get ship counts by race
  (->> (ships-data data)
       (map :race)
       (reduce (fn [counts object]
                 (update counts object #(inc (or % 0))))
               {})
       (sort-by last >))
  ;; get government colors in CSS format
  (->> endless-ships.parser/data
       (filter #(= (first %) "government"))
       (map (fn [[_ [name] {[[colors]] "color"}]]
              [name colors]))
       (filter #(some? (second %)))
       (map (fn [[government colors]]
              [government (->> colors
                               (map (partial * 255))
                               (map int)
                               (map (partial format "%02x"))
                               clojure.string/join
                               (str "#"))]))
       (into {})))
