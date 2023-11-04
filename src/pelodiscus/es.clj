(ns pelodiscus.es
  (:require [pelodiscus.fuzz :as fuzz]
            [clojure.edn :as edn]))

;;;; This file  holds the expert system shell related code.

;; =====================================================================================================
;; Constants
;; =====================================================================================================

(def nonfactor 0)
(def very-low 0.1)
(def low 0.25)
(def moderatly-low 0.35)
(def moderate 0.5)
(def moderatly-high 0.65)
(def high 0.75)
(def very-high 0.9)
(def exactly 1)

(def expert-system    "The current expert system 'namespace' used to direct lookups in many functions."                                             (atom :default))
(def question-sets    "Holds all questions defined in each expert system."                                                                          (atom {:default {}}))
(def rule-sets        "Holds all rule sets defined in each expert system."                                                                          (atom {:default {}}))
(def rule-num         "Holds the number of rules in each expert system."                                                                            (atom {:default 0}))
(def system-sharpness "Holds the degree of sharpness to apply to the gaussian norm in the 'is' function for each expert system."                    (atom {:default 15}))
(def system-default   "Holds the default response for each expert system. This will be shown if no suitable rule is found."                         (atom {:default "default response"}))
(def session-data     "Holds the current known values in the given session."                                                                        (atom {}))
(def session-rules    "Holds the current rules in the given session. Nessesary because rules evaluate lambda expressions as the system progresses." (atom {}))

;; =====================================================================================================
;; Setup
;; =====================================================================================================


(defn def-system
  "Defines a new expert system name space and 
  enters it. This allows questions and rules to
  be assigned to the expert system.
  VAR: x - should be a key"
  [x]
  (reset! expert-system x)
  (if (false? (get @question-sets x false))
    (swap! question-sets assoc x {}))
  (if (false? (get @rule-sets x false))
    (swap! rule-sets assoc x {}))
  (if (false? (get @rule-num x false))
    (swap! rule-num assoc x 0))
  (if (false? (get @system-default x false))
    (swap! system-default assoc x "-"))
  (if (false? (get @system-sharpness x false))
    (swap! system-sharpness assoc x 15)))

(defn in-system 
  "Sets the current expert system namespace.
  VAR: x - should be a key to an 
           existing expert-system namespace"
  [x]
  (reset! expert-system x))

(defn new-session 
  "Clears old session data and reloads rule set in 
  current expert system namespace."
  []
  (reset! session-data {})
  (reset! session-rules (get @rule-sets @expert-system)))

;; =====================================================================================================
;; Questions
;; =====================================================================================================

(defn question 
  "Defines a new standard question in the current expert system namespace.
  VAR: get-value - the value the question will aquire. Should be a key.
  VAR: text - the text provided as a prompt to the user."
  [get-value text]
  (swap! question-sets update-in [@expert-system] assoc get-value {:text text})
  'question-created)

(defn range-question 
  "Defines a new ranged question in the current expert system namespace.
  VAR: get-value - the value the question will aquire. Should be a key.
  VAR: text - the text provided as a prompt to the user.
  VAR: low - the lower limit of desired values. System will
             prompt user again if a value lower than this is entered.
  VAR: high - the upper limit of desired values. System will 
              prompt user again if a value higher than this is entered."
  [get-value text low high]
  (swap! question-sets update-in [@expert-system] assoc get-value {:text text :low low :high high})
  'range-question-created)

(defn t-f-question 
  "Defines a new T/F or Y/N question in the current expert system namespace.
  This function will prompt the user again if a value of T, Y, F, or N
  is not entered.
  VAR: get-value - the value the question will aquire. Should be a key.
  VAR: text - the text provided as a prompt to the user."
  [get-value text]
  (swap! question-sets update-in [@expert-system] assoc get-value {:text text :t-f true})
  't-f-question-created)

(defn get-in-range
  "Accepts an input and returns the input when it is a number and
  it falls at or between the upper and lower limits. Otherwise, prompt
  the user and ask for another input. Not directly
  called by user.
  VAR: low - lower limit
  VAR: high - upper limit"
  [low high]
  (let [output (edn/read-string (read-line))]
    (if (and (number? output) (>= output low) (<= output high))
      output
      (do (printf "Invalid Input.%nPlease enter a number between %d and %d.%n" low high)
          (flush)
          (recur low high)))))

(defn get-t-f 
  "Accepts an input and returns true when it is T or Y and false when
  it is F or N. If input is anything else prompt the user again.
  Not called directly by user."
  []
  (let [output (read-line)]
    (if (not-any? #(= output %) '("T" "F" "Y" "N"))
      (do (printf "Invalid Input.%nPlease enter either T/F or Y/N.%n")
          (flush)
          (recur))
      (cond (or (= output "T") (= output "Y")) 1
            :else 0))))

(defn get-num 
  "Accepts an input and returns it when the input is a number. Otherwise,
  prompt the user again and get another input. Not directly called by the 
  user."
  []
  (let [output (edn/read-string (read-line))]
    (if (number? output)
      output
      (do (printf "Invalid Input.%nPlease enter a number.%n")
          (flush)
          (recur)))))

(defn ask-question 
  "Directing function for questions. Asks either a range, t-f, or standard
  question based on the type associated with the provided value. Question to 
  ask is determined by the current expert system namespace.
  VAR: get-value - the value key to use when finding the question."
  [get-value]
  (let [question (get (get @question-sets @expert-system) get-value)]
    (println (get question :text))
    (cond (get question :t-f false) (get-t-f)
          (get question :low false) (get-in-range (get question :low) (get question :high))
          :else (get-num))))

(defn value 
  "Will return a value unless there is no associated question. Returns the
  value associated with the provided key in the current session data.
  VAR: get-value - the key to the value requested."
  [get-value]
  (let [data (get @session-data get-value false)]
    (if data
      data
      (let [new-val (ask-question get-value)]
        (swap! session-data assoc get-value new-val)
        new-val))))

;; =====================================================================================================
;; Conditions - Rules
;; =====================================================================================================

(defn rule-count 
  "Returns the total number of rules in the current expert system namespace."
  []
  (@rule-num @expert-system))

(defn rule 
  "Creates a new rule in the current expert system namespace.
  VAR: text - this is the text shown if the rule is chosen as the
              final one to apply.
  VAR: required - this should be a vector of conditions absolutly
                  nessesary for the rule to be followed.
  VAR: normal - this should be a vector of conditions used to determine
                the degree of certainty that a rule is correct."
  [text required normal]
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

(defn condition 
  "Some syntatic sugar over a single key-value map. Used in
  conjunction with the rule function.
  VAR: get-value - the value being used in the condition.
  VAR: function - this should be a lambda function that gets the
                  value provided in get-value. No other values
                  should be involved. This function should return true
                  or false in the required field and a number between 
                  0 and 1 in the normal field."
  [get-value function]
  {get-value function})

(defn eval-condition 
  "Evaluates the lambda function in the provided rule and updates
  the session-rules accordingly.
  VAR: n - the rule number
  VAR: t - the field. Can be :normal or :required
  VAR: get-value - the key associated with the function
                   to evaluate."
  [n t get-value]
  (swap! session-rules update-in [n t] assoc get-value
         ((get-in @session-rules [n t get-value]))))

(defn val-sort-map 
  "Sorts a map by values and returns a sorted map."
  [m]
  (into (sorted-map-by (fn [key1 key2]
                         (compare [(get m key2) key2]
                                  [(get m key1) key1])))
        m))

(defn conditions-counted 
  "Returns all condition keys in a given expert system ordered by 
  how often they occur in decreasing order.
  METHOD 1:
  VAR t - the field. Can either be :required or :normal
  VAR to-check - a list of the rules to check.
  METHOD 2:
  VAR t - the field. Can either be :required or :normal
  VAR n - a list of the remaining rules to check.
  VAR m - the current map. Will be added to and eventually returned
          as the function recurs."
  ([to-check t] (conditions-counted t to-check {}))
  ([t n m]
   (if (empty? n)
     (keys (val-sort-map m))
     (let [k (keys (get-in @session-rules [(first n) t]))]
       (recur t (rest n) (merge (map #(if (get m %) {% (inc (get m %))} {% 1}) k)))))))

(defn unevaluated-conditions-counted
  "Removes all conditions with found values from the list returned by
  conditions counted.
  VAR t- the field. Can either be :required or :normal
  VAR to-check - a list of the rules to check."
  [to-check t]
  (remove #(get @session-data %) (conditions-counted to-check t)))

(defn rules-with-condition
  "Returns all rule numbers in the given expert system that are 
  dependent on the given value key.
  VAR: to-check - a list of the rule numbers to check
  VAR: get-value - the value the function is searching for"
  [to-check get-value]
  (filter identity
          (map #(if (or (get-in @session-rules [% :required get-value])
                        (get-in @session-rules [% :normal get-value]))
                  % nil)
               to-check)))

(defn meets-requirements? 
  "Returns true if none of the :required conditions in the
  given rule are false. Returns false otherwise.
  Non-evaluated conditions are considered true. 
  VAR: n - the rule number"
  [n]
  (not-any? false? (vals (get-in @session-rules [n :required]))))

(defn failing-rules
 "Returns the numbers of all rules failing in the given session-rules.
 A rule is failing if meets-requirements? returns false for it.
  VAR: to-check - a list of the rule numbers to check" 
  [to-check]
  (filter identity
          (map #(if (not (meets-requirements? %))
                  % nil)
               to-check)))

(defn passing-rules 
 "Returns the numbers of all rules passing in the given session-rules.
 A rule is passing of meets-requirements? returns true for it.
  VAR: to-check - a list of the rule numbers to check" 
  [to-check]
  (filter identity
          (map #(if (meets-requirements? %)
                  % nil)
               to-check)))

(defn total-conditions 
  "Returns the total number of :normal condition in the
  given rule. :required is ignored because this function is
  only used in computing certainty.
  VAR: n - the rule to check"
  [n]
  (count (get-in @session-rules [n :normal])))

(defn set-default 
  "Sets the default rule for the current expert system. This rule
  will be shown only if all other rules fail their nessesary conditions.
  VAR: x - the default rule message string"
  [x]
  (swap! system-default assoc @expert-system x))

;; =====================================================================================================
;; Certainty
;; =====================================================================================================

(defn set-sharpness 
  "Sets the sharpness value used by the function is.
  This sets the sharpness for the current 
  expert system namespace only.
  VAR: x - the new sharpness value."
  [x]
  (swap! system-sharpness assoc @expert-system x))

(defn sharpness 
  "Gets the sharpness value for the current expert
  system namespace."
  []
  (get @system-sharpness @expert-system))

(defn within-degree 
  "Is the value greater than or equal to 1 - cutoff?
  VAR: cutoff - one minus this value is compared to x
  VAR: x - the value in question"
  [cutoff x]
  (<= (- 1 cutoff) x))

(defn is
  "Returns a number between zero and one based on the distance
  of the provided number from the expected number. This is a 
  selecting of a point x over the fuzzy gaussian norm set with
  a peak f and a sharpness degree given by system-sharpness.
  METHOD 1:
  VAR: x - the point to select
  VAR: f - the peak of the gaussian norm
  METHOD 2:
  VAR: x - the point to select
  VAR: f - the peak of the gaussian norm
  VAR: scale - the scale to multiply f by. 
               This is used as syntatic sugar."
  ([x f]
   (let [result (fuzz/gaussian x (sharpness) f)]
     (if (< result 0.0001)
       0
       result)))
  ([x f scale]
   (is x (* f scale))))

(defn calculate-certainty 
  "Sets the :certainty field of a rule to the average of all :normal
  condition values. Updates in session-rules.
  VAR: n - the rule to evaluate"
  [n]
  (swap! session-rules update-in [n] assoc :certainty
         (/ (apply + (vals (get-in @session-rules [n :normal])))
            (total-conditions n))))

(defn get-certainty 
  "Returns the certainty for the given rule in the current session-rules.
  Value is zero when certainty is uncalculated.
  VAR: n - the rule in question"
  [n]
  (get-in @session-rules [n :certainty]))

(defn get-text 
  "Returns the value in the text field of the given rule in the
  current session-rules.
  VAR: n - the rule in question"
  [n]
  (get-in @session-rules [n :text]))

(defn certainty-comperator 
  "Compares two rules by certainty
  VAR: a - rule one
  VAR: b - rule two"
  [a b]
  (> (get-certainty a) (get-certainty b)))

(defn order-by-certainty 
  "Returns a list of rule numbers in decreasing order by certainty.
  Maps over session-rules.
  VAR: to-check - the rules to compare."
  [to-check]
  (sort certainty-comperator to-check))

;; =====================================================================================================
;; Expert System
;; =====================================================================================================

(defn run-system
  "The main funtion used to handle evaluation of the expert system.
  METHOD 1:
  This is the method the user will call if already in the desired expert
  system namespace. It will call method three with the needed values.
  METHOD 2:
  Switches into the desired expert system namespace before calling method one.
  VAR x - the expert system to switch to.
  METHOD 3:
  Recusive method called over the course of the expert system's functioning. 
  Repeatedly aquires new values and eleminates values from required-stack and
  normal-stack. When both stacks are exhausted returns the rule with the highest
  certainty from the rules remaining.
  VAR: required-stack - the stack of values which any of the remaining rules being
                        considered have listed under :required. This stack must be
                        empty before normal-stack is considered.
  VAR: normal-stack - the stack of values which any of the remaining rules being
                      considered have listed under :normal.
  VAR: considering - a list of all rule numbers still being considered, or all rules
                     that still have no failing values in :required."
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
                                            (let [rules (passing-rules considering)]
                                            `(~(unevaluated-conditions-counted rules :required) 
                                              ~(unevaluated-conditions-counted rules :normal) ~rules)))
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
   (let [rules (range (rule-count))]
      (run-system (conditions-counted rules :required)
                  (conditions-counted rules :normal)
                  rules)))
  ([x] (in-system x) (run-system)))


