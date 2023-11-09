(ns pelodiscus.systems.test
  (:use pelodiscus.es))

;; System used for demostration of what values can be set and how to go about arranging
;; your rules. Can be run to see how execution works.

(def-system :testing)

(set-default "Final Answer: Default")

(set-sharpness 100)

(t-f-question :value-1 "Value 1? Y/N:")
(question :value-2 "Value 2:")
(range-question :value-3 "Value 3:" 0 1)
(range-question :value-4 "Value 4:" 0 1)

(rule "Final Answer: Rule 0"
      [(condition :value-1 #(within-degree 0 (value :value-1)))
       (condition :value-2 #(within-degree 0.8 (is :value-2 exactly 2)))]
      [(condition :value-3 #(is :value-3 low))
       (condition :value-4 #(is :value-4 high))])

(rule "Final Answer: Rule 1"
      [(condition :value-1 #(within-degree 0 (value :value-1)))
       (condition :value-2 #(within-degree 0 (is  :value-2 nonfactor nonfactor)))]
      [(condition :value-3 #(is :value-3 moderate))
       (condition :value-4 #(is :value-4 moderate))])

(rule "Final Answer: Rule 2"
      [(condition :value-1 #(within-degree 0 (value :value-1)))
       (condition :value-2 #(within-degree 0.8 (is :value-2 exactly 2)))]
      [(condition :value-3 #(is :value-3 moderate))])

(rule "Final Answer: Rule 3"
      []
      [(condition :value-3 #(is :value-3 very-high))])

(rule "Final Answer: Rule 4"
      [(condition :value-1 #(within-degree 0 (value :value-1)))]
      [(condition :value-3 #(is :value-3 low))
       (condition :value-4 #(is :value-4 moderate))])






