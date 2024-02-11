(ns pelodiscus.systems.water
  (:use pelodiscus.esv))

(system :water)

(def-what :water "An expert system designed to diagnose issues with your home water heater.")

(def-rule :overheated [:or {:if :pressure :relates [:with :it]} {:if :average-temp :relates [:with :it]}])
(def-rule :leaking [:and {:if [:visible-leaking :true] :then :it} {:if :pressure :relates [:against :it]}])
(def-rule :empty [:and {:if [:visible-leaking :false] :then :it}
                  {:if :pressure :relates [:against :it]} {:if :fill :relates [:against :it]}])
(def-rule :no-issues [:or {:if [:pressure moderate] :then :it}
                      {:if [:visible-leaking :false] :then :it} {:if [:average-temp moderate] :then :it}])

(def-what :overheated (str "Your water heater is overheating your water. \nPlease consider turning the heat"
                           " down or calling a technician if that does not work."))
(def-what :leaking "Your water heater is leaking. \nPlease call a technician as soon as possible.")
(def-what :empty "Your water heater is empty. \nPlease contact your local water company if the issue continues.")
(def-what :no-issues "Your water heater is fine.")

(def-question :pressure [0 120] "Please enter the water pressure in cubic feet.")
(def-question :average-temp [0 60] "Please enter the average water temperature in degrees celcius.")
(def-question :visible-leaking {"yes" :true "no" :false} "Does the water heater show signs of leaking?")
(def-question :fill [0 100] "Please enter the percentage full that your water heater is.")

(def-what :pressure (str "This is current pressure in cubic feet that the water is under in the water heater.\n"
                         "This value can be read from the readout on the upper left side of the water heater."))
(def-what :average-temp (str "This should be an estimate of the average temperature of the water coming from the water heater.\n"
                             "To find this value turn on a faucet or other water source that pulls from the heater and set it\n"
                             "to half hot water. Then feel the warmth and enter an estimate of that value in degrees celcius."))
(def-what :visible-leaking (str "If the water heater is leaking there may be signs of it surrounding the water heater.\n"
                                "Check the ground and area around the water heater for wet surfaces or other signs that\n"
                                "water may be escaping from the water heater."))
(def-what :fill (str "If the water heater is leaking or empty, it will be less full. Your water heater should\n"
                     "have a transparent section where you can see how much water the tank contains. Please\n"
                     "check that area and estimate the fill from viewing that area."))
