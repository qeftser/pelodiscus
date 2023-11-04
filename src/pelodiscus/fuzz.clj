(ns pelodiscus.fuzz)

(defn triangular
  "Function creates hill at point m with constant slope downwards
  to points a and b"
  [x a m b]
  (cond (<= x a) 0
        (<  x  b) (min (/ (- x a) (- m a)) (/ (- b x) (- b m)))
        :else    0))

(defn uL-function
  "a is the cutoff and k determines the speed the function approaches 1"
  [x a k]
  (if (<= x a)
    0
    (- 1 (Math/pow Math/E (* (- k) (Math/pow (- x a) 2))))))

(defn S-function
  "a and b are cutoff values, anything below a gives zero and anything above b gives 1.
  The point a+b/2 is the inflection point, slope sharpens in both directions of this point."
  [x a b]
  (cond (<= x a) 0
        (<= x b) (min (* 2 (Math/pow (/ (- x a) (- b a)) 2))
                      (- 1 (* 2 (Math/pow (/ (- x b) (- b a)) 2))))
        :else    1))

(defn trapazoidal
  "a and b are cutoff values for zero. m and n define a range of values 1,
  with linear drop-offs to both sides"
  [x a m n b]
  (cond (<  x a) 0
        (<= x b) (min (/ (- x a) (- m a))
                      1
                      (/ (- b x) (- b n)))
        :else    0))

(defn gaussian
  "k is rate of decay and m is target value. Sharp drops
  in front of and behind m"
  [x k m]
  (Math/pow Math/E (* (- k) (Math/pow (- x m) 2))))

(defn expt-like
  "Similar to gaussian, but slightly less harsh. This one is wack low-key."
  [x k m]
  (/ (* k (Math/pow (- x m) 2))
     (+ 1 (* k (Math/pow (- x m) 2)))))

(defn normal?
  "If the fuzzy set x is normal, return true. If not, return nil"
  [x]
  (some #(= % 1) x))

(defn sub-normal?
  "If the fuzzy set x is sub-normal, return true. If not, return nil"
  [x]
  (not-any? #(= % 1) x))

(defn height
  "Returns the height of the given fuzzy set x"
  [x]
  (apply max x))

(defn support
  "Returns the support of a fuzzy set: all values for which
  the function f returns a non-zero value over the input set x"
  ([f x] (support f x ()))
  ([f x n]
   (if (empty? x)
     (lazy-seq (reverse n))
     (recur f (rest x)
            (if (= (f (first x)) 0) n
                (conj n (first x)))))))

(defn core
  "Returns the core of a fuzzy set: all values for which
  the function f returns a value of 1 over the input set x"
  ([f x] (core f x ()))
  ([f x n]
   (if (empty? x)
     (lazy-seq (reverse n))
     (recur f (rest x)
            (if (= (f (first x)) 1)
              (conj n (first x))
              n)))))

(defn cardinality
  "Returns the cardinality of the fuzzy set x"
  [x]
  (reduce + x))

(defn normalize
  "Performs normalization on the given fuzzy set x"
  [x]
  (let [hgt (height x)]
    (map #(/ % hgt) x)))

(defn concentrate
  "Performs concentration on the given fuzzy set x"
  ([x]
   (map #(* % %) x))
  ([x n]
   (assert (> n 1) "Error: Value n must be greater than 1")
   (map #(Math/pow % n) x)))

(defn single-dilate
  "Performs dilation on a single number x"
  ([x]
   (Math/pow x 0.5))
  ([x n]
   (Math/pow x n)))

(defn dilate
  "Performs dilation on the given fuzzy set x"
  ([x]
   (map single-dilate x))
  ([x n]
   (assert (and (< n 1) (> n 0)) "Error: Value n must be in range 0 < n < 1")
   (map single-dilate x n)))

(defn contrast-intensify
  "Performs contrast intensification on the given fuzzy set x"
  ([x] (contrast-intensify x 2))
  ([x n]
   (map #(if (and (>= % 0) (<= % 0.5))
           (* (Math/pow 2 (- n 1)) (Math/pow % n))
           (- 1 (* (Math/pow 2 (- n 1)) (Math/pow (- 1 %) n))))
        x)))

(defn fuzzify
  "Performs fuzzification on the given fuzzy set x"
  [x]
  (map #(if (<= % 0.5)
          (Math/sqrt (/ % 2))
          (- 1 (Math/sqrt (/ (- 1 %) 2))))
       x))

(defn fuzzy-set-equal?
  "Returns true if the given fuzzy sets are equal, false otherwise"
  [x & args]
  (apply = (conj args x)))

(defn fuzzy-set-included?
  "Returns true if sets y - args are included in set x.
  This function is the same as an imperfect subset."
  ([x y] (every? true? (map >= x y)))
  ([x y & args] (cond (empty? args) (fuzzy-set-included? x y)
                      (every? true? (map >= x y)) (recur x (first args) (rest args))
                      :else false)))

(defn fuzzy-subset?
  "Returns true if the given fuzzy sets y - args is a fuzzy subset of the given 
  fuzzy set x, returns false otherwise"
  ([x y] (every? true? (map > x y)))
  ([x y & args] (cond (empty? args) (fuzzy-subset? x y)
                      (every? true? (map > x y)) (recur x (first args) (rest args))
                      :else false)))

(defn subsethood
  "Returns the degree of subsethood that the fuzzy set y has to the fuzzy set x"
  [x y]
  (let [cx (cardinality x)]
    (* (/ 1 cx) (- cx (reduce + (map #(max 0 (- %1 %2)) x y)))))) 

(defn oc-cut 
  "Returns the oc 'proportional' cut of the given input set when
  mapped with the function f." 
  ([f x a] (oc-cut f x a ()))
  ([f x a n]
   (if (empty? x)
     (lazy-seq (reverse n))
     (recur f (rest x) a
            (if (<= (f (first x)) a) n
                (conj n (first x)))))))

(defn fuzzy-intersect
  "Returns the intersect of fuzzy sets x and y"
  [x y]
  (map #(min %1 %2) x y))

(defn fuzzy-union
  "Returns the union of fuzzy sets x and y"
  [x y]
  (map #(max %1 %2) x y))

(defn fuzzy-complement
  "Returns the fuzzy complement of fuzzy set x"
  [x]
  (map #(- 1 %) x))



   




