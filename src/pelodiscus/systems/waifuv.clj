(ns pelodiscus.systems.waifuv
  (:use pelodiscus.esv))

(def-system :waifu)
(in-system :waifu)

(def-question :emotion-expression [0 10] (str "Enter the degree to which this character\n"
                                              "expresses emotion on a scale from 0 to 10:"))

(def-question :positive-expression [0 10] (str "Enter the degree to which this character\n"
                                               "expresses positive emotion on a scale from\n"
                                               "0 to 10:"))

(def-question :negative-expression [0 10] (str "Enter the degree to which this character\n"
                                               "expresses negative emotion on a scale from\n"
                                               "0 to 10:"))

(def-question :agressive-tendency [0 10] (str "Enter the degree to which this character\n"
                                              "possesses agressive tendencies on a scale\n"
                                              "from 0 to 10:"))

(def-question :violent-tendency [0 10] (str "Enter the degree to which this character\n"
                                            "posesses violent tendencies on a scale from\n"
                                            "0 to 10:"))

(def-question :intelligence [0 10] (str "Enter the degree to which you would consider\n"
                                        "this character intelligent on a scale of 0 to\n"
                                        "10 with 5 being average:"))

(def-question :social-interaction [0 10] (str "Enter the degree to which this character\n"
                                              "engages with others socially on a scale\n"
                                              "from 0 to 10:"))

(def-question :romantic-tendency [0 10] (str "Enter the degree to which this character is\n"
                                             "likely to desire or enter into some kind of\n"
                                             "romantic relationship from 0 to 10:"))

(def-question :superiority [0 10] (str "Enter the degree to which this character is likely\n"
                                       "to proclaim themself as or to exhibit feelings of\n"
                                       "superiority towards others on a scale from 0 to 10:"))

(def-question :exaggeration [0 10] (str "Enter the degree to which this character is likely\n"
                                        "to exaggerate thier abilities on a scale from 0 to 10:"))

(def-rule :dojikko [:or 
                    {:if [:agressive-tendency very-low] :then :it}
                    [:or 
                     {:if [:emotion-expression high] :then :it}
                     {:if [:positive-expression moderatly-high] :then :it}
                     {:if [:intelligence moderatly-low] :then :it}]])

(def-rule :tsundere [:or
                     {:if [:emotion-expression high] :then :it}
                     {:if [:agressive-tendency high] :then :it}
                     {:if [:violent-tendency very-low] :then :it}
                     [:or
                      {:if [:positive-expression high] :then :it}
                      {:if [:negative-expression moderatly-high] :then :it}
                      {:if [:intelligence moderatly-high] :then :it}
                      {:if [:romantic-tendency high] :then :it}]])

(def-rule :meganekko [:or
                      {:if [:intelligence very-high] :then :it}
                      [:or
                       {:if [:emotion-expression low] :then :it}
                       {:if [:agressive-tendency low] :then :it}]])

(def-rule :chuunibyo [:or
                      {:if [:emotion-expression very-high] :then :it}
                      {:if [:exaggeration very-high] :then :it}
                      [:or
                       {:if [:positive-expression high] :then :it}
                       {:if [:negative-expression low] :then :it}
                       {:if [:agressive-tendency moderate] :then :it}
                       {:if [:superiority moderatly-high] :then :it}]])

(def-rule :kuudere [:or
                    {:if [:emotion-expression very-low] :then :it}
                    {:if [:social-interaction very-low] :then :it}
                    [:or
                     {:if [:intelligence high] :then :it}]])

(def-rule :dandere [:or 
                    {:if [:agressive-tendency very-low] :then :it}
                    {:if [:social-interaction low] :then :it}
                    [:or 
                     {:if [:emotion-expression moderate] :then :it}
                     {:if [:romantic-tendency low] :then :it}]])

(def-rule :yangire [:or 
                    {:if [:agressive-tendency very-high] :then :it}
                    {:if [:violent-tendency very-high] :then :it}
                    [:or 
                     {:if [:romantic-tendency low] :then :it}
                     {:if [:emotion-expression high] :then :it}]])

(def-rule :himedere [:or
                     {:if [:superiority high] :then :it}
                     [:or 
                      {:if [:emotion-expression moderatly-high] :then :it}
                      {:if [:negative-expression moderatly-high] :then :it}
                      {:if [:agressive-tendency moderate] :then :it}]])

(def-rule :yandere [:or
                    {:if [:agressive-tendency very-high] :then :it}
                    {:if [:violent-tendency very-high] :then :it}
                    {:if [:romantic-tendency very-high] :then :it}
                    [:or
                     {:if [:emotion-expression high] :then :it}]])

(def-rule :osananajimi [:or 
                        {:if [:social-interaction moderate] :then :it}
                        {:if [:romantic-tendency high] :then :it}])

(def-rule :lolimouto [:or 
                      {:if [:positive-expression high] :then :it}
                      {:if [:negative-expression low] :then :it}
                      [:or
                       {:if [:emotion-expression high] :then :it}
                       {:if [:romantic-tendency low] :then :it}]])

(def-rule :genki [:or
                  {:if [:positive-expression high] :then :it}
                  {:if [:negative-expression very-low] :then :it}
                  [:or 
                   {:if [:emotion-expression high] :then :it}
                   {:if [:social-interaction high] :then :it}]])

(def-rule :fujoshi [:or 
                    {:if [:romantic-tendency very-high] :then :it}
                    [:or 
                     {:if [:emotion-expression high] :then :it}
                     {:if [:positive-expression high] :then :it}]])
                            
                      
                    

