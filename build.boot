(set-env! :source-paths #{"src/clj"}
          :resource-paths #{"resources"}
          :dependencies '[[org.clojure/clojure "1.10.3"]
                          [instaparse "1.4.5"]
                          [camel-snake-kebab "0.4.0"]
                          [fipp "0.6.24"]])

(require '[endless-ships.core :as core])

(deftask dev
  "Starts an nREPL server."
  []
  (comp
   (wait)
   (repl :server true)))

(deftask build
  "Builds dependencies for the web application."
  []
  (dosh "yarn" "install")
  (dosh "shadow-cljs" "release" "main"))

(deftask generate-data
  "Generate the data.edn file in public/ for local development."
  []
  (try
    (core/generate-data)
    (catch Exception e
      (println "[build.boot] Encountered exceptions while processing task."))
    (finally
      (println "Quitting..."))))
