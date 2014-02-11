(ns txtlib.core.parser.plain
  (:require [clojure.string :as string]
            [txtlib.core.parser :as parser]
            [txtlib.core.format :as format]))

(def cursor
  (parser/chain
   (fn [_ value _] (format/->Label :cursor value))
   (parser/string format/negative)
   (parser/regex #"[\s\S]")
   (parser/string format/normal)))

(def selection
  (parser/chain
   (fn [_ value _] (format/->Label :selection value))
   (parser/string format/underline)
   (parser/many
    (parser/choice
     cursor
     (parser/chain
      (fn [_ value] value)
      (parser/not (parser/string format/normal))
      (parser/regex #"[\s\S]"))))
   (parser/string format/normal)))

(def parser
  (parser/many
   (parser/choice
    cursor
    selection
    (parser/regex #"[\s\S]"))))

(defn parse [buffer]
  (-> buffer format/show parser :value))
