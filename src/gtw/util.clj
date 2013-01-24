(ns gtw.util)

(defn rand-set
  [num max]
  (if (< max num)
    (throw (IllegalArgumentException. "Can't create a set with more items than available numbers")))
  (loop [rset (set (take num (repeatedly #(rand-int max))))]
    (if (= (count rset) num)
      (shuffle rset)
      (recur (set (concat
                   rset
                   (take (- num (count rset)) (repeatedly #(rand-int max)))))))))
