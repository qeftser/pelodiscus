(ns pelodiscus.esv
  (:require [pelodiscus.fuzz :as fuzz]))

;;;; A new - hopefully better - expert system implementation

;; Basic 

;; Unless

;; Gradual

{:if 'A :then 'B}

{:if 'A :then 'B :unless 'C}

{:if 'A :relates 'B}

(def system-rules (atom {:default {}}))
(def session-rules (atom {}))

(def system-questions (atom {:default {}}))
(def session-questions (atom {}))

(def system-what (atom {:default {}}))
(def session-what (atom {}))

(def current-system (atom :default))
(def current-question (atom :default))
(def session-data (atom {}))

(defn question? [x]
  (contains? @session-questions x))

(defn rule? [x]
  (contains? @session-rules x))

(defn value [x]
  (cond (contains? @session-data x) (get @session-data x)
        (question? x) (let [q (get-question x)]
                        (eval-question x (get q :options) (get q :text)))))

(defn print-rule [x]
  (let [if-cond (get x :if)
        then-cond (get x :then)
        unless-cond (get x :unless)
        relates-cond (get x :relates)]
    (cond (and if-cond then-cond unless-cond)
          (println "IF " (first if-cond) " IS " (last if-cond) " THEN "
                   then-cond " IS TRUE UNLESS " (first unless-cond) " IS "
                   (last unless-cond) ".")
          (and if-cond then-cond)
          (println "IF " (first if-cond) " IS " (last if-cond) " THEN "
                   then-cond " IS TRUE.")
          (and if-cond relates-cond)
          (do (print "THE DEGREE " (first if-cond) " IS " (last if-cond))
              (cond (= :with (first relates-cond))
                    (println " POSITIVELY IMPACTS " (last relates-cond))
                    :else (println " NEGATIVELY IMPACTS " (last relates-cond)))))))

(defn what-handler [x]
  (if (contains? @session-what x)
    (println (get @session-what x))
    (println "No further data on " x " to give")))

(defn why-handler []
  (print-rule @current-question))

(defn scale [x l h]
  (/ x (- (abs h) (abs l))))

(defn update-certainty [x y]
  (let [data (get @session-data x)]
    (if data
      (swap! session-data assoc x (/ (+ data y) 2))
      (swap! session-data assoc x y))))

(defn check [x y]
  (let [nx (value x)]
    (if (keyword? nx)
      (if (= nx y)
        1 0)
      (fuzz/gaussian  nx 15 y))))

(defn if-then-unless-eval [a b c]
  (let [if-degree     (check (first a) (last a))
        unless-degree (check (first c) (last c))
        degree        (min 0 (- if-degree unless-degree))]
    (update-certainty b degree)))

(defn if-then-eval [a b]
  (let [degree (check (first a) (last a))]
    (update-certainty b degree)))

(defn if-relates-with-eval [a b]
  (let [if-degree (check (first a) (last a))]
    (cond (= (first b) :with)
          (update-certainty (last b) if-degree)
          (= (first b) :against)
          (update-certainty (last b) (- 1 if-degree))
          :else (throw (Exception. "Given b condition is not one of [:with :against]")))))

(defn eval-rule [x]
  (let [if-cond      (get x :if)
        then-cond    (get x :then)
        unless-cond  (get x :unless)
        relates-cond (get x :relates)]
    (cond (and if-cond then-cond unless-cond)
          (if-then-unless-eval if-cond then-cond unless-cond)
          (and if-cond then-cond)
          (if-then-eval if-cond then-cond)
          (and if-cond relates-cond)
          (if-relates-with-eval if-cond relates-cond)
          :else (throw (Exception. "Given rule does not match any expected syntax")))))

(defn eval-question
  ([value options text]
   (cond (map? options)
         (let [values (keys options)]
           (println text)
           (println "Plese enter one of the following:")
           (map #(print "%s, " %) (rest values))
           (print (first values) ".\n>> ")
           (let [ret (read-line)]
             (cond (= ret "why") (do (why-handler) (recur value options text))
                   (= ret "what") (do (what-handler value) (recur value options text))
                   (some true? (map #(= ret %) values))
                   (update-certainty value (get options ret))
                   :else (do (println "AAAHH!!! Value entered not one of options listed")
                             (recur value options text)))))
         (number? options)
         (do
           (println text)
           (println "Enter a number from " (first options) " to " (last options))
           (print ">> ")
           (let [ret (read-line)]
             (cond (= ret "why") (do (why-handler) (recur value options text))
                   (= ret "what") (do (what-handler value) (recur value options text)))
             (let [nret (read-string ret)]
               (if (and (number? ret) (< (first options) ret (last options)))
                 (update-certainty value (scale value (first options) (last options)))
                 (recur value options text)))))
         :else (eval-question value text)))
  ([value text]
   (println text)
   (println "Enter a numver from 0 to 1")
   (let [ret (read-line)]
     (cond (= ret "why") (do (why-handler) (recur value text))
           (= ret "what") (do (what-handler value) (recur value text)))
     (let [nret (read-string ret)]
       (if (and (number? ret) (< 0 ret 1))
         (update-certainty value ret)
         (do (println "Given value either not a number or not in range")
             (recur value text)))))))

(defn get-question [x]
  (get @session-questions x))

(defn def-system [x]
  (if (contains? @system-questions x)
    (println "System already defined")
    (do (swap! system-questions assoc x {})
        (swap! system-rules assoc x {})
        (swap! system-what assoc x {}))))

(defn in-system [x]
  (if (contains? @system-questions x)
    (reset! current-system x)
    (println "No system with the given key " x " exists")))

(defn system [x]
  (def-system x)
  (in-system x))

(defn def-question [value options text]
  (swap! system-questions update-in [@current-system] assoc value {:options options :text text}))

(defn def-what [value text]
  (swap! system-what update-in [@current-system] assoc value text))

(defn def-rule [value rulestructure]
  (swap! system-rules update-in [@current-system] assoc value rulestructure))

(defn valid-question? [x]
  (let [values (second x)]
    (if (and (string? (get values :text)) (or (and (sequential? (get values :options))
                                                   (number? (first x))
                                                   (number? (last x)))
                                              (nil? (get values :options))
                                              (map? (get values :options))))
      nil
      (println "MALFORMED QUESTION: " x))))

(defn valid-rulestructure? [x]
  (cond (map? x)
        (let [if-cond (get x :if)
              then-cond (get x :then)
              unless-cond (get x :unless)
              relates-cond (get x :relates)]
          (cond (and if-cond then-cond unless-cond)
                (and (sequential? if-cond) (symbol? (first if-cond))
                     (symbol? then-cond)
                     (sequential? unless-cond) (symbol? (first relates-cond)))
                (and if-cond then-cond)
                (and (sequential? if-cond) (symbol? (first if-cond))
                     (symbol? then-cond))
                (and if-cond relates-cond)
                (and (sequential? if-cond) (symbol? (first if-cond))
                     (sequential? relates-cond)
                     (or (= :with (first relates-cond)) (= :against (first relates-cond)))
                     (symbol? (last relates-cond)))
                :else false))
        (sequential? x)
        (and (or (= (first x) :and) (= (first x) :or)) (every? valid-rulestructure? (rest x)))))

(defn valid-rule? [x]
  (if (valid-rulestructure? (second x))
    nil
    (println "MALFORMED RULE: " x)))

(defn new-session
  ([x] (in-system x) (new-session))
  ([]
   (reset! session-questions (get system-questions @current-system))
   (reset! session-rules (get system-rules @current-system))
   (reset! session-what (get system-what @system-what))
   (reset! session-data (merge (map #(hash-map % 0.5) (keys @session-rules))))))

(defn collect-and-rule [x] x)

(defn collect-or-rule [x]
  (merge (map #(if (sequential? %) (if (= :and (first %))
                                     (collect-and-rule (rest %))
                                     (collect-or-rule (rest %)))
                   %)
              x)))

(defn collect-and-rule [x]
  (if (sequential? (first x))
    (if (= :and (first (first x)))
      (recur (rest (first x)))
      (collect-or-rule (rest (first x))))
    (first x)))

(defn collect-rules [x]
  (let [ret (if (= :and (first x))
              (collect-and-rule (rest x))
              (collect-or-rule (rest x)))]
    (if (seqable? ret)
      (flatten ret)
      ret)))

(defn eval-and-sequence-for-condition [x y])

(defn eval-or-sequence-for-condition [x y]
  (cond (and (seqable? y) (seq (rest y)))
        (if (= (first y) :and)
          (cons :and (eval-and-sequence-for-condition x (rest y)))
          (cons :or (remove nil? (map #(eval-or-sequence-for-condition x %) (rest y)))))
        (and (seqable? y) (not (seq (rest y))))
        nil
        (map? y)
        (if (= x (first (get y :if)))
          (println y)
          y)))

(defn eval-and-sequence-for-condition [x y]
  (let [va (first y)]
    (cond (and (seqable? va) (seq (rest va)))
          (let [ev (eval-or-sequence-for-condition x va)]
            (if ev
              (cons ev (rest y))
              (rest y)))
          (and (seqable? va) (not (seq (rest va))))
          nil
          (map? va)
          (if (= x (first (get va :if)))
            (do (println va)
                (rest y))
            y))))

(defn eval-sequence-for-condition [x y]
  (if (= (first y) :or)
    (eval-or-sequence-for-condition x y)
    (cons :and (eval-and-sequence-for-condition x (rest y)))))
    







