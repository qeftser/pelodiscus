(ns pelodiscus.util)

(defn val-sort-map 
  "Sorts a map by values and returns a sorted map."
  [m]
  (into (sorted-map-by (fn [key1 key2]
                         (compare [(get m key2) key2]
                                  [(get m key1) key1])))
        m))

(defn load-files 
  "Loads all files in the provided directory"
  [path]
  (let [file (java.io.File. path)
        files (.listFiles file)]
    (doseq [x files]
      (when (.isFile x)
        (println "Loading " (.toString x))
        (load-file (.getCanonicalPath x))))))
