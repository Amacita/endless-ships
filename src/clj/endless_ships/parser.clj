(ns endless-ships.parser
  (:require [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [clojure.string :as str]
            [clojure.java.io :refer [resource]]
            [instaparse.core :as insta])
  (:import [java.lang Float Integer]))

(def resource-root (-> (clojure.java.io/resource "game") clojure.java.io/file .getParent))

(defn file->relative-path [file]
  (let [absPath (-> file .getPath)
        relPath (subs absPath (+ 1 (count resource-root)))]
    relPath))

(defn ignore-unwanted-map-lines [text]
  "Map files are large enough to slow down the parser, so we preprocess them to get rid of unwanted and unnecessary lines."
  (remove
      (some-fn
        #(str/starts-with? % "\tasteroids ")
        #(str/starts-with? % "\ttrade ")
        #(str/starts-with? % "\tmineables ")
        #(str/starts-with? % "\tlink ")
        #(str/starts-with? % "\tbelt ")
        #(str/starts-with? % "\thabitable ")
        #(str/starts-with? % "\t\tperiod ")
        #(str/starts-with? % "\t\tdistance ")
        #(str/starts-with? % "\t\t\tperiod ")
        #(str/starts-with? % "\t\t\tdistance ")
        #(str/starts-with? % "\t\tspaceport ")
        #(str/starts-with? % "\t\tlandscape ")
        #(str/starts-with? % "\t\tsprite star/ ")
        #(str/starts-with? % "\t\tsprite planet/ ")
        #(str/starts-with? % "\t\t\tsprite star/ ")
        #(str/starts-with? % "\t\t\tsprite planet/ "))
      text))

(defn preprocess
  "Removes comments and blank lines from the given text. Ensures the text ends in a newline."
  [text-str]
  (let [text-lines        (str/split-lines text-str)
        lines-no-comments (map #(str/replace % #"#.*" "") text-lines)
        lines-rstrip      (map #(str/replace % #"[ \t]+\z" "") lines-no-comments)
        lines-no-blanks   (remove str/blank? lines-rstrip)
        text-no-blanks    (str/join \newline lines-no-blanks)
        ;;map-cleaned       (ignore-unwanted-map-lines text-no-blanks)
        end-with-nl       (str/replace text-no-blanks #"\z" "\n")]
    end-with-nl))

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
        filename (file->relative-path file)]
    (print (str "Parsing " filename "... "))
    (time
       (let [text              (-> file slurp preprocess)
             parsed            (parser text :optimize :memory)
             transformed       (insta/transform transform-options parsed)
             labelled-objects  (map #(assoc-in % [2 "file"] filename) transformed)]
         labelled-objects))))

(defn parse-data-files [files]
   (doall
     (mapcat parse files)))

(defn ->map [m]
  (reduce (fn [data [attr-name attr-value]]
            (assoc data
                   (->kebab-case-keyword attr-name)
                   (get-in attr-value [0 0 0])))
          {}
          m))

(comment
  (def wfiles (endless-ships.core/find-data-files "gw/data/Dels"))
  (def wfile (resource "gw/data/Dels/Dels ships.txt"))
  (def wdata (parse-data-files wfiles))
  (defn foobar [lines]
    (remove (some-fn #(str/starts-with? % "\tasteroids ")) lines))
  (-> wfile slurp preprocess print)
  (endless-ships.core/edn wfiles)

  (use 'endless-ships.parser :reload-all)

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
