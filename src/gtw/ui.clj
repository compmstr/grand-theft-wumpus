(ns gtw.ui
  (require [gtw.city-map :as city-map]
           gviz)
  (import [javax.swing
           JScrollPane JLabel ImageIcon JButton JFrame]
          [java.awt BorderLayout]
          [java.io File]
          [java.awt.image BufferedImage]
          [javax.imageio ImageIO]))

(let [frame (JFrame.)
      img (ImageIcon.)
      label (JLabel. img)
      panel (JScrollPane. label)]
  (defn redraw
    []
    (.repaint panel)
    (.repaint frame))
  (defn set-img
    [filename]
    (doto img
      (.setImage (ImageIO/read (File. filename))))
    (doto label
      (.setIcon img)
      (.revalidate)
      (.repaint))
    (redraw))
  (defn start-ui
    []
    (doto frame
      (.add panel BorderLayout/CENTER)
      (.setSize 1024 768)
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setVisible true))))
    