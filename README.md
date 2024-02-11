# Pelodiscus

Simple expert system shell written in clojure that uses conditions to
determine the correct action to take. Fuzzy sets are used to compute truth
values and degrees of relation to certain conditions.

## Overview

There are two different systems included here. One is the namespace pelodiscus.es and the other is pelodiscus.esv. es was the first iteration of the expert system, and esv was the second, with the v standing for verbose. The systems are not cross compatable so don't try to load both of them at once, as you will get some issues. Each system functions in a similar way. You start by declaring a new namespace for the system, if you want to have multiple in one session. Otherwise you can just load and run the default. Then you declare rules and conclusions using the syntax of the system. This is all done in clojure, but the resulting look for the verbose system is much closer to english. Both systems have several options for rule types, and the verbose system allows for control over rule evaluation order.

## Usage
There are two systems in this project: es and esv. The es was my first iteration and is much less user friendly. The esv system is much better than the es system as it is more extensible and looks more like English. I will provide a brief overview here, and there are also some examples in the systems folder. I highly recommend using the esv system over the es one, the es system is there as reference for how the esv system improves things. 
### Setup
Make sure to include esv in your clojure namespace:
```
(ns clojure.name.space
  (:use pelodiscus.esv)) ;; this gives access to expert system
```
Then set the system namespace and enter it:
```
(define-system :my-system)
(in-system :my-system)

(system :my-system) ;; Same as above
```
### Variables
There are some simple global variables provided in the system for ease of use:
```
(def nonfactor 0)
(def very-low 0.1)
(def low 0.25)
(def moderatly-low 0.35)
(def moderate 0.5)
(def moderatly-high 0.65)
(def high 0.75)
(def very-high 0.9)
(def exactly 1)
```
These are very helpful when writing rules.
### Questions
This system works by seperating questions and rules into seperate groups. Each question has a value associated with it that it updates when the question is asked. Each rule interprets the values associated with the questions to determine the degree of certainty the rule has. Rules work by calling the questions associated with each value, so it is important to provide a question for each value a rule uses. Otherwise the program will crash.    

There are several question types. Define a question that accepts a range from 0 to 1 with:
```
(def-question :value-1 nil "question prompt")
```
When a rule requests value-1, this question will be selected, the given question prompt will display and a value between 0 and 1 will be collected from the user.   
To specify a range other than 0-1 use:
```
(def-question :value-2 [0 10] "question prompt")
```
This would call when value-2 is requested. It displays the prompt and accepts a value in the range 0-10 as specified in the rule.   
If you want to define a question that accepts only discrete values, use the syntax:
```
(def-question :value-3 {"option-1" :option-1
                        "option-2" :option-2
                        "option-3" :option-3} "question prompt")
```
When value-3 is requested, the question prompt will be shown, as well as the possible inputs. Only one of the inputs will be accepted.   
A note about the questions. If the value is requested and a question has aleady been asked, the value already obtained is used. The same question will not be asked twice.
### Rules
Rules mkae evaluations based on the values provided in the questions, and use this to come to a conclusion based on the input the user provides. A rule defines an order and guideline for evaluating information, while the questions specify what information to collect.   
Define a basic rule:
```
(def-rule :outcome-1 [:or {:if [:value-1 low] :then :outcome-1}])
(def-rule :outcome-1 [:or {:if [:value-1 low] :then :it}]) ;; Identical to first input
```
This is probably the simplest rule you could to. It will ask the question associated with value 1, then compute the degree of membership value-1 has with the value low, and merge that with the certainty of outcome-1 to get an updated value.   
A more complicated rule might look like this:
```
(def-rule :outcome-2 [:and {:if [:value-1 high] :then :it}
                       [:or {:if [:value-2 moderate] :then :it :unless [:value-3 :option-1]}
                            {:if [:value-3 :option-2] :then :it}]])
```
This rule takes advantage of the way the expert system evaluates rules. The and indicates that the rules must be evaluated in order, while the or shows that either rule could be evaluated first. The :unless condition will cause the second value to be checked and the resulting certainty value to be subtracted against the first one. Note that because value 3 is a discrete value, we specify an option instead of a number. The discrete values will evaluate to 1 if they match the expected input and zero otherwise. So in this rule if value 3 is equal to option 1 then the first rule of the or block will return 0. If value 3 is option 2 though, the second rule will return 1 and the first rule will return the degree of membership value-2 has with the moderate fuzzy set. Again, the values returned by these rules will be merged to create a new certainty value for outcome 2.   
There is one more option you can provide with a rule:
```
(def-rule :outcome-3 [:and {:if :value-1 :relates [:with it]}
                           {:if :value-2 :relates [:against :it]}])
```
Here value 1 is directly merged with outcome 3, and the inverse of value 2 is merged with value 3. You can also use the [:value-1 high] or [:value-3 :option-3] after the if with the relates clause.   
A note: the and/or keys are nestable, so the code below is valid:
```
(def-rule :outcome-4 [:and [:or [:and [:or [:or [:and {:if [:value-1 very-low] :then :outcome-4}]]]]]])
```
To efficiently determine it's conclusion, the system cannot evaluate every question and rule. Instead it works with the rules that are most certain, and ignores the others unless thier certainty increases. This behavior is achived by first ordering the rules by certainty and then cutting off the values below a certain mark. Each of these rules then has values selected for evaluation based on the :and and :or keys. If an :and key is first in a rule, only the first rule (or set of rules if an :or key block is next) is added to the list of rules. If an :or key is first, each value in the block is added for consideration, and any sub-blocks are recursivly evaluated. To determine which value to get, whichever value appeared most out of those selected has it's question evaluated, and all rules are updated according to how they evaluate the value. This allows rules below the cutoff to rise to it, if values they rely on happen to be evaluated.
### Adding context
You can use def-what to add context or an explanation to values:
```
(def-what :value-1 "This is a value - this will print if the user types 'what' into the terminal during the evaluation of the
                                      question associated with this value")
(def-what :outcome-1 "This will print if the selected outcome is chosen as most likely at the end of the expert system's collection of input
                      you should make sure to define this for outcomes so the user knows what the conclusion the recived means")
(def-what :my-system "This will display when the system starts running to give the user context as to what it does.
                      you should make sure to include this as well")
```
### Running the system
First open a clojure repl and load the expert system file:
```
> (load "path/to/expert/system")
```
Then enter the namespace and run the system:
```
> (in-system :my-system)
> (esv/run-system)
```
Or:
```
> (esv/run-system :my-system)
```
If you get an error that esv is not a valid namespace try including it in your repl manually:
```
> (load "esv")
```
If you are running the repl in the pelodiscus project though, this shouldn't be an issue.
## Demo


https://github.com/qeftser/pelodiscus/assets/144874443/1e2d1cdd-3a13-460a-b284-e488e4018abd


## Why the name?

Pelodiscus is a sub-species of soft shell turtles. The soft shell turtle
has a soft shell, so I thought it was a fitting namesake
for this project.

