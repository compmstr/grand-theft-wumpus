(ns gtw.ui
  (require [gtw.city-map :as city-map]
           gviz)
  (import [javax.swing
           JScrollPane JLabel JPanel ImageIcon JButton JFrame SwingUtilities
           JCheckBox JOptionPane]
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

(let [frame (JFrame. "Grand Theft Wumpus")
      img (ImageIcon.)
      label (JLabel. img)
      map-pane (JScrollPane. label)
      msg-line (JLabel. "Messages")
      charge-check (JCheckBox. "Charge")
      option-panel (JPanel.)
      nav-panel (JPanel.)]
  (defn pop-up-message
    [message]
    (JOptionPane/showMessageDialog frame message))

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
  (defn set-message
    [new-msg]
    (on-evt-thread
     (doto msg-line
       (.setText new-msg)
       (.revalidate)
       (.repaint))))
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
  (defn reset-ui
    []
    (.setSelected charge-check false)
    (set-message ""))
  (defn start-ui
    []
    (doto option-panel
      (.add charge-check))
    (doto frame
      (.add map-pane BorderLayout/CENTER)
      (.add (doto (JPanel.)
              (.add msg-line BorderLayout/NORTH)
              (.add option-panel BorderLayout/WEST)
              (.add nav-panel BorderLayout/EAST))
            BorderLayout/SOUTH)
      (.setSize 1024 768)
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setVisible true))
    (reset-ui)))

(defn- test-click-fn
  [num]
  (println (str "Going to: " num
                (when (charge?)
                  "\nCharge!!!"))))
(defn test-ui
  []
  (start-ui)
  (set-buttons {2 (partial test-click-fn 2)
                3 (partial test-click-fn 3)
                5 (partial test-click-fn 5)
                4 (partial test-click-fn 4)}))