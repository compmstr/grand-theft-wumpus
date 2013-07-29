(ns gtw.util)

(defn rand-set
  [num max]
  {:pre [(>= max num)]}
  (take num (shuffle (range max))))
