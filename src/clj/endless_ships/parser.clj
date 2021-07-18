(ns endless-ships.parser
  (:require [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [clojure.java.io :refer [file resource]]
            [clojure.string :as str]
            [instaparse.core :as insta])
  (:import [java.lang Float Integer]))

(defn preprocess
  "Removes comments and blank lines from the given text. Ensures the text ends in a newline."
  [text-str]
  (let [text-lines        (str/split-lines text-str)
        lines-no-comments (map #(str/replace % #"#.*" "") text-lines)
        lines-no-blanks   (remove str/blank? lines-no-comments)
        text-no-blanks    (str/join \newline lines-no-blanks)
        end-with-nl       (str/replace text-no-blanks #"\z" "\n")]
    end-with-nl))

(def files-vanilla
  "All vanilla files containing game data."
  (->> "game/data"
       resource
       file
       file-seq
       (filter #(.endsWith (.getName %) ".txt"))))

(def files-gw
  "All Galactic War files containing game data."
  (->> "gw/data"
       resource
       file
       file-seq
       (filter #(.endsWith (.getName %) ".txt"))))

#_(def files-both files-gw)

(defn- transform-block [[_ name & args] & child-blocks]
  (let [processed-children (reduce (fn [children [child-name & child-contents]]
                                     (update children
                                             child-name
                                             #(conj (or % [])
                                                    (vec child-contents))))
                                   {}
                                   child-blocks)]
    [name (vec args) processed-children]))

(def transform-options
  {:data vector
   :0-indented-block transform-block
   :1-indented-block transform-block
   :2-indented-block transform-block
   :3-indented-block transform-block
   :4-indented-block transform-block
   :5-indented-block transform-block
   :6-indented-block transform-block
   :string identity
   :integer #(Long/parseLong %)
   :float #(Float/parseFloat (str/replace % "," "."))})

(defn parse [file]
  (let [parser (insta/parser (resource "parser.bnf"))
        filename (.getName file)]
    (print (str "Parsing " filename "... "))
    (time
       (let [text              (-> file slurp preprocess)
             parsed            (parser text :optimize :memory)
             transformed       (insta/transform transform-options parsed)
             labelled-objects  (map #(assoc-in % [2 "file"] filename) transformed)]
         labelled-objects))))

(def data
   (doall
     (mapcat parse files-vanilla)))

(defn ->map [m]
  (reduce (fn [data [attr-name attr-value]]
            (assoc data
                   (->kebab-case-keyword attr-name)
                   (get-in attr-value [0 0 0])))
          {}
          m))

(comment
  ;; object counts by type
  (->> data
       (map first)
       (reduce (fn [counts object]
                 (update counts object #(inc (or % 0))))
               {})
       (sort-by last >))

  ;; ship counts by file
  (->> data
       (filter #(and (= (first %) "ship") (= (count (second %)) 1)))
       (remove #(= (second %) ["Unknown Ship Type"]))
       (map #(get-in % [2 "file"]))
       (reduce (fn [counts object]
                 (update counts object #(inc (or % 0))))
               {})
       (sort-by last >))

  ;; outfit counts by file
  (->> data
       (filter #(= (first %) "outfit"))
       (map #(get-in % [2 "file"]))
       (reduce (fn [counts object]
                 (update counts object #(inc (or % 0))))
               {})
       (sort-by last >))

  ;; parsing errors
  (->> data
       (filter #(keyword? (first %)))))
