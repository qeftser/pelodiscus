(ns pelodiscus.es
  (:require [pelodiscus.fuzz :as fuzz]
            [clojure.edn :as edn]))

;; This file will hold the majority of the expert system shell related code.

;; First defining a combination function.

(def nonfactor 0)
(def very-low 0.1)
(def low 0.25)
(def moderatly-low 0.35)
(def moderate 0.5)
(def moderatly-high 0.65)
(def high 0.75)
(def very-high 0.9)
(def exactly 1)

(def expert-system (atom :default))
(def question-sets (atom {:default {}}))
(def rule-sets (atom {:default {}}))
(def rule-num (atom {:default 0}))
(def system-default (atom {:default "default response"}))
(def session-data (atom {}))
(def session-rules (atom {}))

(defn within-degree [cutoff x]
  (<= (- 1 cutoff) x))

(defn is
  ([x f]
   (let [result (fuzz/gaussian x 15 f)]
     (if (< result 0.0001)
       0
       result)))
  ([x f scale]
   (fuzz/gaussian x 15 (* f scale))))

(defn def-system
  "Sets the expert system variable to the given key x.
  This allows for multiple expert systems to be loaded 
  at once - kind of like namespaces."
  [x]
  (reset! expert-system x)
  (if (false? (get @question-sets x false))
    (swap! question-sets assoc x {}))
  (if (false? (get @rule-sets x false))
    (swap! rule-sets assoc x {}))
  (if (false? (get @rule-num x false))
    (swap! rule-num assoc x 0))
  (if (false? (get @system-default x false))
    (swap! system-default assoc x "-")))

(defn in-system [x]
  (reset! expert-system x))

(defn new-session []
  (reset! session-data {})
  (reset! session-rules (get @rule-sets @expert-system)))

;; =====================================================================================================
;; Questions
;; =====================================================================================================

(defn question [get-value text]
  (swap! question-sets update-in [@expert-system] assoc get-value {:text text})
  'question-created)

(defn range-question [get-value text low high]
  (swap! question-sets update-in [@expert-system] assoc get-value {:text text :low low :high high})
  'range-question-created)

(defn t-f-question [get-value text]
  (swap! question-sets update-in [@expert-system] assoc get-value {:text text :t-f true})
  't-f-question-created)

(defn get-in-range
  [low high]
  (let [output (edn/read-string (read-line))]
    (if (and (number? output) (>= output low) (<= output high))
      output
      (do (printf "Invalid Input.%nPlease enter a number between %d and %d.%n" low high)
          (flush)
          (recur low high)))))

(defn get-t-f []
  (let [output (read-line)]
    (if (not-any? #(= output %) '("T" "F" "Y" "N"))
      (do (printf "Invalid Input.%nPlease enter either T/F or Y/N.%n")
          (flush)
          (recur))
      (cond (or (= output "T") (= output "Y")) 1
            :else 0))))

(defn get-num []
  (let [output (edn/read-string (read-line))]
    (if (number? output)
      output
      (do (printf "Invalid Input.%nPlease enter a number.%n")
          (flush)
          (recur)))))

(defn ask-question [get-value]
  (let [question (get (get @question-sets @expert-system) get-value)]
    (println (get question :text))
    (cond (get question :t-f false) (get-t-f)
          (get question :low false) (get-in-range (get question :low) (get question :high))
          :else (get-num))))

(defn value [get-value]
  (let [data (get @session-data get-value false)]
    (if data
      data
      (let [new-val (ask-question get-value)]
        (swap! session-data assoc get-value new-val)
        new-val))))

;; =====================================================================================================
;; Conditions - Conclusions
;; =====================================================================================================

(defn rule-count []
  (@rule-num @expert-system))

(defn rule [text required normal]
  (swap!
   rule-sets
   update-in
   [@expert-system]
   assoc
   (rule-count)
   {:text text
    :certainty 0
    :required (apply merge required)
    :normal (apply merge normal)})
  (swap! rule-num update-in [@expert-system] inc))

(defn condition [get-value function]
  {get-value function})

(defn eval-condition [n t get-value]
  (swap! session-rules update-in [n t] assoc get-value
         ((get-in @session-rules [n t get-value]))))

(defn val-sort-map [m]
  (into (sorted-map-by (fn [key1 key2]
                         (compare [(get m key2) key2]
                                  [(get m key1) key1])))
        m))

(defn conditions-counted
  ([t] (conditions-counted t 0 {}))
  ([t n m]
   (if (= n (rule-count))
     (keys (val-sort-map m))
     (let [k (keys (get-in @session-rules [n t]))]
       (recur t (inc n) (merge (map #(if (get m %) {% (inc (get m %))} {% 1}) k)))))))

(defn rules-with-condition
  [to-check get-value]
  (filter identity
          (map #(if (or (get-in @session-rules [% :required get-value])
                        (get-in @session-rules [% :normal get-value]))
                  % nil)
               to-check)))

(defn meets-requirements? [n]
  (not-any? false? (vals (get-in @session-rules [n :required]))))

(defn failing-rules [to-check]
  (filter identity
          (map #(if (not (meets-requirements? %))
                  % nil)
               to-check)))

(defn passing-rules [to-check]
  (filter identity
          (map #(if (meets-requirements? %)
                  % nil)
               to-check)))

(defn total-conditions [n]
  (count (get-in @session-rules [n :normal])))

(defn calculate-certainty [n]
  (swap! session-rules update-in [n] assoc :certainty
         (/ (apply + (vals (get-in @session-rules [n :normal])))
            (total-conditions n))))

(defn get-certainty [n]
  (get-in @session-rules [n :certainty]))

(defn get-text [n]
  (get-in @session-rules [n :text]))

(defn certainty-comperator [a b]
  (> (get-certainty a) (get-certainty b)))

(defn order-by-certainty [to-check]
  (sort certainty-comperator to-check))

(defn set-default [x]
  (swap! system-default assoc @expert-system x))

;; =====================================================================================================
;; Expert System
;; =====================================================================================================

(defn run-system
  ([required-stack normal-stack considering]
   (println "Running cycle")
   (println "REQ-STACK: " required-stack)
   (println "NOR-STACK: " normal-stack)
   (println "CONSIDERING " considering)
   (let [nr
         (cond (empty? considering) (println (get @system-default @expert-system))
               (not-empty required-stack) (let [gval (first required-stack)]
                                            (dorun (map #(eval-condition % :required gval)
                                                        (rules-with-condition considering gval)))
                                            `(~(rest required-stack) ~normal-stack ~(passing-rules considering)))
               (not-empty normal-stack) (let [gval (first normal-stack)]
                                          (dorun (map #(eval-condition % :normal gval)
                                                      (rules-with-condition considering gval)))
                                          `(nil ~(rest normal-stack) ~considering))
               :else (do
                       (dorun (map calculate-certainty considering))
                       (println "CERTAINTIES: " (map get-certainty considering))
                       (println "ORDERED BY : " (order-by-certainty considering))
                       (println (get-text (first (order-by-certainty considering)))
                                "With certainty: " (get-certainty (first (order-by-certainty considering))))
                       nil))]
     (if nr (recur (nth nr 0) (nth nr 1) (nth nr 2))
         'sayonara)))
  ([] (new-session)
      (run-system (conditions-counted :required)
                  (conditions-counted :normal)
                  (range (rule-count))))
  ([x] (in-system x) (run-system)))




