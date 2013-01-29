(ns gtw.core
  (:require [gtw.city-map :as city-map]
            [gtw.player :as player]
            [gtw.ui :as ui]
            gviz))

(defonce game-map (city-map/generate-map))
(defn game-gviz-string
  []
  (gviz/graph->dot
   (city-map/visited->graph game-map @player/player)
   :concentrate "true"))

(player/start-player game-map)
(print (game-gviz-string))
;;(player/go-to game-map <new-loc>)

;;Example of displaying map
;;(spit "map.dot"
      ;;(-> (city-map/generate-map)
          ;;(city-map/map->graph)
          ;;(gviz/graph->dot :concentrate "true")))