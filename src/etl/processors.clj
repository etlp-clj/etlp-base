(ns etl.processors
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.core.async :as a]
            [clojure.string :as s]
            [etlp.core :as etlp]
            [clojure.pprint :refer [pprint]]
            [etl.utils :refer [csv-xform]]
            [etlp.processors.stdin :refer [create-stdin-source!]]
            [etlp.processors.stdout :refer [create-stdout-destination!]]
            [etlp.utils.core :refer [wrap-record wrap-log]]
            [clojure.tools.logging :refer [info debug]]))


(defn create-csv-processor [{:keys [config mapper options]}]
  (pprint options)
  (let [csv-specs          (mapper :CSV-SPECS)
        csv-transform-jute (mapper :CSV-TRANSFORM)
        csv-spec-jute      (csv-specs {:headers [] :use-headers false :delimiter ","})
        in-source          {:threads    (options :threads)
                            :partitions (options :partitions)
                            :reducers   {:csv-reducer (csv-xform csv-spec-jute)}
                            :reducer    :csv-reducer}
        destination-conf   {:threads    (options :threads)
                            :partitions (options :partitions)}]

    {:source      (create-stdin-source! in-source)
     :destination (create-stdout-destination! destination-conf)
     :xform       (comp
                   (filter (fn[item]
                             (not (empty? item))))
                   (keep (fn [item]
                           (csv-transform-jute item)))
                   (map wrap-record))
     :threads     (options :threads)}))


(def etlp-fs-processor {:name       :csv-stream
                       :process-fn  create-csv-processor
                       :etlp-config {}
                       :etlp-mapper {:base-url (System/getenv "ETLP_MAPPER_SERVICE")
                                     :specs    {:CSV-SPECS     (System/getenv "ETLP_MAPPER_CSV_KEY")
                                                :CSV-TRANSFORM (System/getenv "ETLP_MAPPER_CSV_TRANSFORM_KEY")}}})

(def fs-stream-processor {:id        6
                          :component :etlp.core/processors
                          :ctx       etlp-fs-processor})
