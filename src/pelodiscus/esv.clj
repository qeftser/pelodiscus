(ns pelodiscus.esv
  (:require [pelodiscus.fuzz :as fuzz]))

;;;; A new - hopefully better - expert system implementation

;; Basic 

;; Unless

;; Gradual

{:if 'A :then 'B}

{:if 'A :then 'B :unless 'C}

{:if 'A :relates 'B}

(def system-data (atom {:default {}}))
(def session-data (atom {}))

(def system-rules (atom {:default {}}))
(def session-rules (atom {}))

(def system-questions (atom {:default {}}))
(def session-questions (atom {}))

(defn scale [x l h]
  (/ x (- h l)))

(defn check [x y]
  (fuzz/gaussian x 15 y))

(defn if-then-unless-eval [a b c]
  (let [if-degree     (check (first a) (last a))
        unless-degree (check (first c) (last c))
        degree        (min 0 (- if-degree unless-degree))]
    (update-certainty b degree)))

(defn if-then-rule-eval [a b]
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
          (and if-cond relates-cond with-cond)
          (if-relates-with-eval if-cond relates-cond)
          :else (throw (Exception. "Given rule does not match any expected syntax")))))


