(ns gviz
  (:require [clojure.string :as string :only [replace]]
            [clojure.java.shell :as sh]))

(defn- digraph
  [contents]
  (str "digraph{\n" contents "}\n"))

(defn- attrs-string
  "Returns a map as a string of 'key=value' pairs, separated by commas
   keys can be keywords/strings/symbols"
  [attrs]
  (apply str
         (interpose ", "
                    (map #(str
                           (name (first %))
                           "=\""
                           (second %)
                           "\"") attrs))))

(defn- attrs-block
  "Returns attrs block string for dot file if attrs is not empty, else nil"
  [attrs]
  (when-not (empty? attrs)
    (str " ["
         (attrs-string attrs)
         "]")))

(defn- node-name
  "Converts a string/keyword into a dot-file compatible node name
  (mainly replaces - with _)"
  [raw-name]
  (-> (if (keyword? raw-name)
        (name raw-name)
        raw-name)
      (string/replace "-" "_")
      (string/replace " " "_")))

(defn- node
  [name attrs]
  (str "  "
       (node-name name)
       (attrs-block attrs)
       ";\n"))

(defn- mapped-edge
  [from to]
  (str "  "
       (node-name from)
       " -> "
       (node-name (:label to))
       (attrs-block (:attrs to))
       ";\n"))

(defn- edges
  [from to attrs]
  (if (coll? to)
    (let [label-tos (map node-name (remove map? to))
          mapped-tos (filter map? to)]
      (str
       (when-not (empty? label-tos)
         (str "  "
              (node-name from)
              " -> {"
              (apply str (interpose "; " label-tos))
              "}"
              (attrs-block attrs)
              ";\n"))
       (when-not (empty? mapped-tos)
         (apply str
                (map (partial mapped-edge from)
                     mapped-tos)))))
    (str "  " (node-name from) " -> " (node-name to) (attrs-block attrs) ";\n")))

(defn graph->dot
  "Takes in a graph structure, which is a list of nodes, each node
  is a map with:
  :attrs (optional attributes for this node)
  :label(name of node)
  :connections (list of labels that this node connects to)
    if a connection entry is a map, it needs :label and (optional) :attrs
  Returns the text content of a graphviz .dot file
  opts - list of key/value pairs for digraph options
    ex: :concentrate \"true\" combines lines where applicable"
  [graph & opts]
  (digraph
   (str
    (let [opts-map (apply hash-map opts)]
      (apply str (for [opt opts-map] (str
                                      "  "
                                      (node-name (first opt))
                                      "=\""
                                      (node-name (second opt))
                                      "\"\n"))))
    (apply str (map #(node (:label %)
                           (:attrs %)) graph))
    (apply str (map #(edges (:label %)
                            (:connections %)
                            (:edge-attrs %)) graph)))))

(defn render-dot-string
  "Render a dot format string, to avoid temporary dot files"
  [source png-file]
  (sh/sh "fdp" "-Tpng" (str "-o" png-file) :in source))
(defn render-dot-file
  "Render a dotfile to a png"
  ([dot-file]
     (let [ext-idx (.lastIndexOf dot-file ".dot")]
       (render-dot-file dot-file (str (if (= -1 ext-idx)
                                     dot-file
                                     (subs dot-file 0 ext-idx))
                                   ".png"))))
  ([dot-file png-file]
     (println (format "Rendering %s into %s" dot-file png-file))
     (sh/sh "fdp" "-Tpng" (str "-o" png-file) dot-file)))

(def test-graph
  [{:label :node :connections []
    :attrs {:shape "box"}}
   {:label :secret
    :connections [:living-room]}
   {:label :garden
    :connections [{:label :living-room
                   :attrs {:label "East Door"}}]}
   {:label :living-room
    :connections [{:label :garden
                   :attrs {:label "West Door"}}
                  {:label :attic
                   :attrs {:label "Up Ladder"}}
                  :secret]}
   {:label :attic
    :connections [{:label :living-room
                   :attrs {:label "Down Ladder"}}]}])