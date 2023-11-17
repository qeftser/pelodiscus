(ns pelodiscus.systems.testv
  (:use pelodiscus.esv))

(def-question :value-1 {"option-1" :option-1
                        "option-2" :option-2
                        "option-3" :option-3} "Select an option:")

(def-question :value-2 nil "Enter a value for 2:")

(def-question :value-3 [0 10] "Enter a value for 3:")

(def-question :value-4 [-10 10] "Enter a value for 4:")

(def-rule :outcome-1 [:and {:if [:value-1 :option-1] :then :it} {:if [:value-4 low] :then :it}])

(def-rule :outcome-2 [:or {:if [:value-1 :option-2] :then :it :unless [:value-3 high]} {:if [:value-1 :option-3] :then :it :unless [:value-3 low]}])

(def-rule :outcome-3 [:or [:and [:or [:and [:or {:if :value-2 :relates [:with :it]} {:if :value-3 :relates [:against :it]} {:if [:value-4 moderate] :relates [:with :it]}]]]]])


