(ns pelodiscus.esv
  (:require [pelodiscus.fuzz :as fuzz]
            [pelodiscus.util :refer [val-sort-map]]
            [clojure.edn :as edn]))

;;;; A new - hopefully better - expert system implementation

;; Basic 

;; Unless

;; Gradual

{:if 'A :then 'B}

{:if 'A :then 'B :unless 'C}

{:if 'A :relates 'B}

(def nonfactor 0)
(def very-low 0.1)
(def low 0.25)
(def moderatly-low 0.35)
(def moderate 0.5)
(def moderatly-high 0.65)
(def high 0.75)
(def very-high 0.9)
(def exactly 1)

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

(defn rulestruct? [x]
  (if (map? x)
    (let [if-cond (get x :if)
          then-cond (get x :then)
          unless-cond (get x :unless)
          relates-cond (get x :relates)]
      (if (or (and if-cond then-cond unless-cond)
              (and if-cond then-cond)
              (and if-cond unless-cond))
        true
        false))
    false))

(defn get-question [x])
(defn eval-question [x])

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
          (if (sequential? if-cond)
            (do (print "THE DEGREE " (first if-cond) " IS " (last if-cond))
                (cond (= :with (first relates-cond))
                      (println " POSITIVELY IMPACTS " (last relates-cond))
                      :else (println " NEGATIVELY IMPACTS " (last relates-cond))))
            (do (print "THE DEGREE " if-cond " IS TRUE ")
                (cond (= :with (first relates-cond))
                      (println " POSITIVELY IMPACTS " (last relates-cond))
                      :else (println " NEGATIVELY IMPACTS " (last relates-cond))))))))

(defn print-rules [x]
  (cond (sequential? x) (doall (map print-rules x))
        (rulestruct? x) (print-rule x)))

(defn print-all-rules []
  (doall (map (comp print-rules second) @session-rules))
  'fin)

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
      (let [v (/ (+ data y) 2)]
        (swap! session-data assoc x v))
      (do (swap! session-data assoc x y) y))))

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
  (let [if-degree (if (sequential? a)
                    (check (first a) (last a))
                    (value a))]
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
           (flush)
           (let [ret (read-line)]
             (cond (= ret "why") (do (why-handler) (recur value options text))
                   (= ret "what") (do (what-handler value) (recur value options text))
                   (some true? (map #(= ret %) values))
                   (update-certainty value (get options ret))
                   :else (do (println "AAAHH!!! Value entered not one of options listed")
                             (recur value options text)))))
         (sequential? options)
         (do
           (println text)
           (println "Enter a number from " (first options) " to " (last options))
           (print ">> ")
           (flush)
           (let [ret (read-line)]
             (cond (= ret "why") (do (why-handler) (eval-question value options text))
                   (= ret "what") (do (what-handler value) (eval-question value options text)))
             (let [nret (edn/read-string ret)]
               (if (and (number? nret) (< (first options) nret (last options)))
                 (update-certainty value (scale nret (first options) (last options)))
                 (eval-question value options text)))))
         :else (eval-question value text)))
  ([value text]
   (println text)
   (println "Enter a number from 0 to 1")
   (print ">> ")
   (flush)
   (let [ret (read-line)]
     (cond (= ret "why") (do (why-handler) (eval-question value text))
           (= ret "what") (do (what-handler value) (eval-question value text)))
     (let [nret (edn/read-string ret)]
       (if (and (number? nret) (< 0 nret 1))
         (update-certainty value nret)
         (do (println "Given value either not a number or not in range")
             (recur value text)))))))

(defn get-question [x]
  (get @session-questions x))

(defn replace-in-seq [old new s]
  (cond (sequential? s) (map #(replace-in-seq old new %) s)
        (map? s) (into {} (map #(if (= (second %) old) [(first %) new] %) s))
        (= old s) new
        :else s))

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
  (swap! system-rules update-in [@current-system] assoc value (replace-in-seq :it value rulestructure)))

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
                (and (or (and (sequential? if-cond) (symbol? (first if-cond))) (keyword? if-cond))
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
   (reset! session-questions (get @system-questions @current-system))
   (reset! session-rules (get @system-rules @current-system))
   (reset! session-what (get @system-what @system-what))
   (reset! session-data (apply merge (map #(hash-map % 0.5) (keys @session-rules))))))

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
    (if (sequential? ret)
      (flatten ret)
      ret)))

(defn collect-all-rules []
  (flatten (map (comp collect-rules second) @session-rules)))

(defn or-eval [x r])
(defn and-eval [x r])

(defn rule-eval [x r]
  (let [del (first r)
        ret (if (= del :and)
              (and-eval x (rest r))
              (or-eval x (rest r)))]
    (cond (and ret (= del :and))
          (cons :and ret)
          (and ret (= del :or))
          (cons :or ret)
          :else nil)))

(defn and-eval [x l]
  (cond (empty? l) nil
        (sequential? (first l)) (remove nil? (cons (rule-eval x (first l)) (rest l)))
        (or (= x (get (first l) :if)) (= x (first (get (first l) :if)))) (do (eval-rule (first l)) (rest l))
        :else l))

(defn or-eval [x l]
  (if (empty? l) nil
      (remove nil? (map #(if (sequential? %) (rule-eval x %)
                             (if (or (= (get % :if) x)
                                     (= (first (get % :if)) x)) (eval-rule %)
                                 %)) l))))

(defn eval-rules [x]
  (map #(swap! session-rules assoc (first %) (rule-eval x (second %))) @session-rules))

(defn distill-rule [x]
  (let [if-cond (get x :if)
        then-cond (get x :then)
        relates-cond (get x :relates)]
    (cond (and if-cond then-cond)
          (list (first if-cond) then-cond)
          (and relates-cond (keyword? if-cond))
          (list if-cond relates-cond)
          :else (list (first if-cond) relates-cond))))

(defn distill-rules []
  (map distill-rule (collect-all-rules)))

(defn count-value-keywords [x l]
  (count (remove #(not (= x (first %))) l)))

(defn remove-below-cutoff [x l]
  (remove #(<= (value (second %)) x) l))

(defn cut-to [x l]
  (if (or (empty? l) (= x 0)) nil
      (cons (first l)
            (cut-to (dec x) (rest l)))))

(defn sort-rules [x]
  (let [i1 (cut-to 5 (sort-by (comp value second) > x))]
    (sort-by #(count-value-keywords (first %) i1) > i1)))

(defn conclude []
  (let [c (first (remove question? (val-sort-map @session-data)))]
    (println "\nCONCLUSION: " (first c))
    (println "\n CERTAINTY: " (second c))
    (what-handler (first c))))

(defn cycle-system []
  (let [val (first (first (sort-rules (distill-rules))))]
    (if val
      (do (doall (eval-rules val)) (recur))
      (conclude))))

(defn run-system
  ([x] (in-system x) (run-system))
  ([] (new-session)
      (cycle-system)))



