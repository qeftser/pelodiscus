(ns pelodiscus.core
  (:require [pelodiscus.es :as es]
            [pelodiscus.util :refer [load-files]]))

;; This will run the expert system when I actually finish the project

(defn -main [& args]
  (println "Wooh!")
  (load-files "./src/pelodiscus/systems"))
  
