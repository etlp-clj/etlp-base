(ns etl.core
  (:require [cli-matic.core :refer [run-cmd]]
            [clojure.string :as s]
            [clojure.tools.logging :refer [debug info]]
            [etlp.core :as etlp]
            [etl.processors :refer [fs-stream-processor]])
  (:gen-class))


(defn config-provider [processors]
  {:command     "etlp"
   :description "Build smart data connectors"
   :version     "0.4.0"
   :opts        [{:as      "The number base for default threads"
                  :default (or (Integer/parseInt (System/getenv "ETLP_PROCESS_THREADS")) 1)
                  :option  "threads"
                  :type    :int}
                 {:as      "The number base for default partitions to handle backpressure"
                  :default 1000000
                  :option  "partitions"
                  :type    :int}]

   :subcommands [{:command     "csv-stream"
                  :description "Listens to stdin for valid csv records and applies the transformation"
                  :examples    ["First example" "Second example"]
                  :opts        [{:option "threads" :as "Concurrent threads" :type :int :default 1}
                                {:option "throttle" :as "Handle backpressure" :type :int :default 100000}
                                {:option "partitions" :as "Concurrency Channel buffers size" :type :int :default 100000}]

                  :runs (fn [{:keys [threads partitions path] :as options}]
                          (processors {:processor :csv-stream :params {:command :etlp.core/start
                                                                          :options options}}))}]})


(defn -main [& args]
  (let [processors (etlp/init {:components [fs-stream-processor]})]
    (run-cmd args (config-provider processors))))
