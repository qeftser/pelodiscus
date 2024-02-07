(ns pelodiscus.systems.waifu
  (:use pelodiscus.es))

;; Waifu classifying expert system. I think it is pretty cool but
;; it might actually be kind of dumb. This is a good example of
;; how questions and rules are arranged and it has made me realize
;; some of the problems with my current system.

(def-system :waifu)

(set-default "Conclusion: Unknown")

(set-sharpness 10)

(range-question :emotion-expression (str "Enter the degree to which this character\n"
                                         "expresses emotion on a scale from 0 to 10:")
                0 10)
(range-question :positive-expression (str "Enter the degree to which this character\n"
                                          "expresses positive emotion on a scale from\n"
                                          "0 to 10:")
                0 10)
(range-question :negative-expression (str "Enter the degree to which this character\n"
                                          "expresses negative emotion on a scale from\n"
                                          "0 to 10:")
                0 10)
(range-question :agressive-tendency (str "Enter the degree to which this character\n"
                                         "possesses agressive tendencies on a scale\n"
                                         "from 0 to 10:")
                0 10)
(range-question :violent-tendency (str "Enter the degree to which this character\n"
                                       "posesses violent tendencies on a scale from\n"
                                       "0 to 10:")
                0 10)
(range-question :intelligence (str "Enter the degree to which you would consider\n"
                                   "this character intelligent on a scale of 0 to\n"
                                   "10 with 5 being average:")
                0 10)
(range-question :social-interaction (str "Enter the degree to which this character\n"
                                         "engages with others socially on a scale\n"
                                         "from 0 to 10:")
                0 10)
(range-question :romantic-tendency (str "Enter the degree to which this character is\n"
                                        "likely to desire or enter into some kind of\n"
                                        "romantic relationship from 0 to 10:")
                0 10)
(range-question :superiority (str "Enter the degree to which this character is likely\n"
                                  "to proclaim themself as or to exhibit feelings of\n"
                                  "superiority towards others on a scale from 0 to 10:")
                0 10)
(range-question :exaggeration (str "Enter the degree to which this character is likely\n"
                                   "to exaggerate thier abilities on a scale from 0 to 10:")
                0 10)

(rule "Conclusion: Dojikko"
      [(condition :agressive-tendency #(within-degree 0.2 (is :agressive-tendency very-low 10)))]
      [(condition :emotion-expression #(is :emotion-expression high 10))
       (condition :positive-expression #(is :positive-expression moderatly-high 10))
       (condition :intelligence #(is :intelligence moderatly-low 10))])
(rule "Conclusion: Tsundere"
      [(condition :emotion-expression #(within-degree 0.2 (is :emotion-expression high 10)))
       (condition :agressive-tendency #(within-degree 0.3 (is :agressive-tendency high 10)))
       (condition :violent-tendency #(within-degree 0.7 (is :violent-tendency very-low 10)))]
      [(condition :positive-expression #(is :positive-expression high 10))
       (condition :negative-expression #(is :negative-expression moderatly-high 10))
       (condition :intelligence #(is :intelligence moderatly-high 10))
       (condition :romantic-tendency #(is :romantic-tendency high 10))])
(rule "Conclusion: Meganekko"
      [(condition :intelligence #(within-degree 0.2 (is :intelligence high 10)))]
      [(condition :emotion-expression #(is :emotion-expression low 10))
       (condition :agressive-tendency #(is :agressive-tendency low 10))])
(rule "Conclusion: Chuunibyo"
      [(condition :emotion-expression #(within-degree 0.3 (is :emotion-expression very-high 10)))
       (condition :exaggeration #(within-degree 0.2 (is :exaggeration very-high 10)))]
      [(condition :positive-expression #(is :positive-expression high 10))
       (condition :negative-expression #(is :negative-expression low 10))
       (condition :agressive-tendency #(is :agressive-tendency moderate 10))
       (condition :superiority #(is :superiority moderatly-high 10))])
(rule "Conclusion: Kuudere"
      [(condition :emotion-expression #(within-degree 0.2 (is :emotion-expression very-low 10)))
       (condition :social-interaction #(within-degree 0.2 (is :social-interaction very-low 10)))]
      [(condition :intelligence #(is :intelligence high 10))])
(rule "Conclusion: Dandere"
      [(condition :agressive-tendency #(within-degree 0.2 (is :agressive-tendency very-low 10)))
       (condition :social-interaction #(within-degree 0.2 (is :social-interaction low 10)))]
      [(condition :emotion-expression #(is :emotion-expression moderate 10))
       (condition :romantic-tendency #(is :romantic-tendency moderatly-high 10))])
(rule "Conclusion: Yangire"
      [(condition :agressive-tendency #(within-degree 0.15 (is :agressive-tendency very-high 10)))
       (condition :violent-tendency #(within-degree 0.15 (is :violent-tendency very-high 10)))]
      [(condition :romantic-tendency #(is :romantic-tendency low 10))
       (condition :emotion-expression #(is :emotion-expression high 10))])
(rule "Conclusion: Himedere"
      [(condition :superiority #(within-degree 0.2 (is :superiority high 10)))]
      [(condition :emotion-expression #(is :emotion-expression moderatly-high 10))
       (condition :negative-expression #(is :negative-expression moderatly-high 10))
       (condition :agressive-tendency #(is :agressive-tendency moderate 10))])
(rule "Conclusion: Yandere"
      [(condition :agressive-tendency #(within-degree 0.2 (is :agressive-tendency very-high 10)))
       (condition :violent-tendency #(within-degree 0.2 (is :violent-tendency very-high 10)))
       (condition :romantic-tendency #(within-degree 0.3 (is :romantic-tendency very-high 10)))]
      [(condition :emotion-expression #(is :emotion-expression high 10))])
(rule "Conclusion: Osananajimi"
      []
      [(condition :social-interaction #(is :social-interaction moderate 10))
       (condition :romantic-tendency #(is :romantic-tendency high 10))])
(rule "Conclusion: Lolimouto"
      [(condition :positive-expression #(within-degree 0.2 (is :positive-expression high 10)))
       (condition :negative-expression #(within-degree 0.2 (is :negative-expression low 10)))]
      [(condition :emotion-expression #(is :emotion-expression high 10))
       (condition :romantic-tendency #(is :romantic-tendency low 10))])
(rule "Conclusion: Genki"
      [(condition :positive-expression #(within-degree 0.2 (is :positive-expression high 10)))
       (condition :negative-expression #(within-degree 0.2 (is :negative-expression very-low 10)))]
      [(condition :emotion-expression #(is :emotion-expression high 10))
       (condition :social-interaction #(is :social-interaction high 10))])
(rule "Conclusion: Fujoshi"
      [(condition :romantic-tendency #(within-degree 0.2 (is :romantic-tendency very-high 10)))]
      [(condition :emotion-expression #(is :emotion-expression high 10))
       (condition :positive-expression #(is :positive-expression high 10))])


      






