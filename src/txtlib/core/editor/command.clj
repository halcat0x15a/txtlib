(ns txtlib.core.editor.command
  (:require [txtlib.core.lens :refer :all]
            [txtlib.core.buffer :as buffer]
            [txtlib.core.history :as history]
            [txtlib.core.format :as format]
            [txtlib.core.editor :as editor]))

(def function (compose (lens :function) editor/current))

(def parameters (compose (lens :parameters) editor/current))

(def target (compose (lens :target) editor/current))

(defn execute [editor]
  (let [[_ & parameters'] (parameters editor)
        argument (-> editor editor/buffer buffer/text)]
    (if (empty? parameters')
      (-> editor
          (editor/switch (target editor))
          ((function editor) argument))
      (-> editor
          (update function #(fn [editor argument] (partial % editor argument)))
          (parameters parameters')))))

(def keymap
  {#{:enter} execute
   #{:backspace} editor/backspace
   #{:left} #(editor/move % :left buffer/character)
   #{:right} #(editor/move % :right buffer/character)
   :run editor/input})

(defrecord Command [buffer history bounds target function parameters]
  editor/Buffer
  (keymap [command] keymap)
  (hint [command] :absolute))

(defn command [editor function & parameters]
  (Command. buffer/null
            (history/history buffer/null)
            (format/rectangle 0 0 (:width editor) 1)
            (editor/path editor)
            function
            parameters))

(defn search [editor]
  (editor/add editor "*search*" (command editor editor/search "query")))
