(ns gtw.util)

(defn rand-set
  [num max]
  (if (< max num)
    (throw (IllegalArgumentException. "Can't create a set with more items than available numbers")))
  (take num (shuffle (range max))))
