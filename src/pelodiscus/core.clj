(ns pelodiscus.core
  (:require [pelodiscus.es :as es]
            [pelodiscus.esv :as esv]
            [pelodiscus.util :refer [load-files]]))

;; This is just a way to load the expert systems and load all the files

(defn -main [& args]
  (load-files "./src/pelodiscus/systems"))
  
