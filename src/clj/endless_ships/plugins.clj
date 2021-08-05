(ns endless-ships.plugins
  (:require [clojure.string :as str]
            [clojure.java.io :refer [file resource]]))

(def plugins
  {:vanilla
   {:name "Endless Sky"
    :key :vanilla
    :resource-dir "game"
    :repository "https://github.com/endless-sky/endless-sky/"
    :base-image-url "https://raw.githubusercontent.com/endless-sky/endless-sky/master/images/"
    :ignore-files #{"data/deprecated outfits.txt"
                    "data/interfaces.txt"
                    "data/sheragi/archaeology missions.txt"
                    "data/remnant/remnant missions.txt"
                    "data/korath/nanobots.txt"
                    "data/human/transport missions.txt"
                    "data/persons.txt"}
    :race-overrides {"data/human/marauders.txt" :pirate
                     "data/drak/indigenous.txt" :indigenous}}
   :galactic-war
   {:name "Galactic War"
    :key :galactic-war
    :resource-dir "gw"
    :repository "https://github.com/1010todd/Galactic-War/"
    :base-image-url "https://raw.githubusercontent.com/1010todd/Galactic-War/master/images/"
    :ignore-files #{"data/Ultaka/Ultaka mothership weapon.txt"}
    :race-overrides {}}})

(defn file->plugin [file]
  (let [root (first (str/split file #"/"))]
    (first (filter #(= (:resource-dir %) root) (vals plugins)))))

(defn image-source [item]
  "Given a ship or outfit, tells you whether its image is in the plugin or the base game."
  (let [plugin (get plugins (:plugin item))
        image (get-in item [:meta :image :file] "key-not-found")
        image-file (str (:resource-dir plugin) "/images/" image)
        image-file-alt (str (:resource-dir (:vanilla plugins)) "/images/" image)]
    (cond
      (resource image-file) (:key plugin)
      (resource image-file-alt) :vanilla
      :else [:not-found image image-file image-file-alt])))
