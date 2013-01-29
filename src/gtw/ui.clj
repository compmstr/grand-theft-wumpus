(ns gtw.ui
  (require [gtw.city-map :as city-map]
           gviz)
  (import [javax.swing
           JScrollPane JLabel JPanel ImageIcon JButton JFrame SwingUtilities
           JCheckBox]
          [java.awt BorderLayout]
          [java.awt.event MouseListener]
          [java.io File]
          [java.awt.image BufferedImage]
          [javax.imageio ImageIO]))

(defmacro on-evt-thread
  [& body]
  `(SwingUtilities/invokeLater
    (fn []
      ~@body)))

(let [frame (JFrame.)
      img (ImageIcon.)
      label (JLabel. img)
      map-pane (JScrollPane. label)
      charge-check (JCheckBox. "Charge")
      option-panel (JPanel.)
      nav-panel (JPanel.)]
  (defn redraw
    []
    (on-evt-thread
     (.repaint map-pane)
     (.repaint frame)))
  (defn set-img
    [filename]
    (on-evt-thread
     (doto img
       (.setImage (ImageIO/read (File. filename))))
     (doto label
       (.setIcon img)
       (.revalidate)
       (.repaint))))
  (defn- create-button
    [[label callback]]
    (doto (JButton. (if (number? label)
                      (str label)
                      (name label)))
      (.addMouseListener (proxy [MouseListener] []
                           (mouseClicked [e] (callback))
                           (mouseEntered [_] nil)
                           (mouseExited [_] nil)
                           (mousePressed [_] nil)
                           (mouseReleased [_] nil)))))
  (defn set-buttons
    "Takes in a map of label: callback kv pairs, and creates buttons for that"
    [button-info]
    (on-evt-thread
     (let [buttons (map create-button button-info)]
       (.removeAll nav-panel)
       (doseq [button buttons]
         (.add nav-panel button))
       (doto nav-panel
         (.revalidate)
         (.repaint)))))
  (defn charge?
    []
    (.isSelected charge-check))
  (defn start-ui
    []
    (doto option-panel
      (.add charge-check))
    (doto frame
      (.add map-pane BorderLayout/CENTER)
      (.add (doto (JPanel.)
              (.add option-panel BorderLayout/WEST)
              (.add nav-panel BorderLayout/EAST))
            BorderLayout/SOUTH)
      (.setSize 1024 768)
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setVisible true))))

(defn- test-click-fn
  [num]
  (println (str "Going to: " num "\n"
                (if (charge?)
                  "Charge!!!"))))
(defn test-ui
  []
  (start-ui)
  (set-buttons {2 #(println "Going to 2")
                5 #(println "Going to 5")
                3 #(println "Going to 3")
                4 #(println "Going to 4")}))