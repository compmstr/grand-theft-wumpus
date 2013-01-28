(ns gtw.core
  (:require [gtw.city-map :as city-map]
            [gtw.player :as player]
            gviz))

;;Example of displaying map
;;(spit "map.dot"
      ;;(-> (city-map/generate-map)
          ;;(city-map/map->graph)
          ;;(gviz/graph->dot :concentrate "true")))