(ns endless-ships.core
  (:require [clojure.java.io :refer [file resource]]
            [clojure.java.shell :refer [sh]]
            [clojure.set :refer [rename-keys union]]
            [clojure.string :as str]
            [camel-snake-kebab.core :as csk]
            [endless-ships.outfits :refer [outfits-data licenses->race]]
            [endless-ships.outfitters :refer [outfitters]]
            [endless-ships.ships :refer [modifications-data ships-data]]
            [endless-ships.plugins :refer [plugins]]
            [endless-ships.parser :refer [parse-data-files file->relative-path]]))

(defn- remove-unwanted-files [files]
  "Filters out the files that are configured to be ingored."
  (let [unwanted-files (into #{} (apply union (map (fn [plugin]
                                                     (map (fn [file] (str (:resource-dir plugin)
                                                                          "/"
                                                                          file))
                                                          (:ignore-files plugin)))
                                                   (vals plugins))))]
    (->> (map file->relative-path files)
         (remove #(contains? unwanted-files %))
         (map resource)
         (map file))))

(defn find-data-files [dir]
  "Finds all data files starting at ./resources/<dir>"
  (->> (file-seq (file (resource dir)))
       (filter #(.endsWith (.getName %) ".txt"))
       remove-unwanted-files))

(defn repo-version [dir]
  (let [git-cmd (fn [& args]
                  (->> (concat ["git"] args [:dir dir])
                       (apply sh)
                       :out
                       str/trim))
        commit-hash (git-cmd "rev-parse" "HEAD")
        commit-date (-> (git-cmd "show" "-s" "--format=%ci" "HEAD")
                        (str/split #" ")
                        first)
        [tag commits-since-tag] (-> (git-cmd "describe" "HEAD" "--tags")
                                    (str/split #"-"))]
    (merge {:hash commit-hash
            :date commit-date}
           (when (nil? commits-since-tag)
             {:tag tag}))))

(defn edn [files]
  (let [data (parse-data-files files)
        complete-outfits (outfits-data data)
        complete-ships (ships-data data complete-outfits)
        complete-modifications (modifications-data data complete-outfits)
        complete-outfitters (outfitters data)
        edn-data {:ships complete-ships
                  :ship-modifications complete-modifications
                  :outfits complete-outfits
                  :outfitters complete-outfitters
                  :licenses (licenses->race complete-outfits complete-ships)
                  :plugins plugins
                  :version (repo-version "./resources/game")
                  :gw-version (repo-version "./resources/gw")}]
    (with-out-str (clojure.pprint/pprint edn-data))))

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
  (try
    (let [data (edn (concat (find-data-files "game/data")
                            (find-data-files "gw/data")))]
      (println "Saving data.edn...")
      (spit "public/data.edn" data))
    (catch Exception e
      (println e)
      (println (format "Error while parsing '%s'" (:file (ex-data e))))
      (println "Line numbers may be inaccurate due to preprocessing.")
      (println (:failure (ex-data e))))))

(comment
  (generate-data)
)
