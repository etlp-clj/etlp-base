(ns etl.utils
  (:require [clojure.tools.logging.readable :refer [debug info warn]]
            [clojure.data.csv :as csv]))


(defn csv-xform
  "A transducer for parsing CSV data according to specified options."
  [{:keys [delimiter use-headers headers] :or {delimiter "," use-headers false} :as params}]
  (let [header-line (atom true)
        parse-line (fn [line]
                     (if (not= line :etlp-stdin-eof)
                     ;; TODO Update below block to allow passing down the csv parse params
                     (-> line
                         csv/read-csv
                         first)
                     []))
        process-headers (fn [line]
                          (if headers
                            headers
                            (parse-line line)))]
    (fn [rf]
      (let [parsed-headers (atom nil)]
        (fn
          ([] (rf))
          ([result] (rf result))
          ([result input]
           (cond
             @header-line
             ;; Handle the header line or set custom headers
             (do (reset! header-line false)
                 (reset! parsed-headers (process-headers input))
                 (if use-headers
                   result
                   (rf result (parse-line input))))

             use-headers
             ;; Combine headers with data into a map
             (let [data (parse-line input)
                   headers @parsed-headers]
               (rf result (zipmap headers data)))

             :else
             ;; Output as a vector of data fields
             (rf result (parse-line input)))))))))
