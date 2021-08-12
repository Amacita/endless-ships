(ns endless-ships.core
  (:require [clojure.java.io :refer [file resource]]
            [clojure.string :as str]
            [fipp.edn :refer [pprint] :rename {pprint fipp}]
            [camel-snake-kebab.core :as csk]
            [endless-ships.outfits :refer [outfits licenses->race]]
            [endless-ships.outfitters :refer [outfitters]]
            [endless-ships.ships :refer [modifications-data ships-data]]
            [endless-ships.plugins :as plugins]
            [endless-ships.parser :refer [parse-data-files]]))

(defn find-data-files [search-dirs]
  "Finds data files in resources/<dir> for each <dir> in search-dirs."
  (let [unwanted-files (plugins/ignored-files)]
    (->> (apply concat (map #(-> % resource file file-seq) search-dirs))
         (filter #(-> % .getName (.endsWith ".txt")))
         (map (fn [data-file] (let [resource-root (-> (resource "game") file .getParent)
                                    absolute-path (-> data-file .getPath)
                                    relative-path (subs absolute-path (+ 1 (count resource-root)))]
                                relative-path)))
         (remove #(contains? unwanted-files %)))))

(defn government-colors [files]
  "Gets government colors in CSS format. The CSS for government labels is maintained by hand."
  (->> (parse-data-files files)
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
       (map (fn [[government color]]
              (str ".label-" (csk/->kebab-case-symbol government) " {\n    background-color: " color "\n}\n\n")))))

(defn generate-data []
  "Attempts to parse the data files and save the resulting info."
  (try
    (let [data (parse-data-files (find-data-files ["game/data" "gw/data"]))
          complete-outfits (outfits data)
          complete-ships (ships-data data complete-outfits)
          complete-modifications (modifications-data data complete-outfits)
          complete-outfitters (outfitters data)
          edn-data {:ships complete-ships
                    :ship-modifications complete-modifications
                    :outfits complete-outfits
                    :outfitters complete-outfitters
                    :licenses (licenses->race complete-outfits complete-ships)
                    :plugins (plugins/processed-plugins)}]
      (println "Formatting data...")
      (let [edn-pretty-data (time (with-out-str (fipp edn-data)))]
        (println "Saving data.edn...")
        (time (spit "public/data.edn" edn-pretty-data))))
    (catch Exception e
      (println (format "Error while parsing '%s'" (:file (ex-data e))))
      (println "Line numbers may be inaccurate due to preprocessing.")
      (println (:failure (ex-data e))))))

(comment
  (generate-data)
  (find-data-files '("game/data" "gw/data"))
)
