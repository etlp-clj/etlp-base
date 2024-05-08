(ns etl.core-test
  (:require [clojure.string :as s]
            [clojure.core.async :as a]
            [etlp.core :as etlp]
            [etlp.utils.core :refer [wrap-record]]
            [etlp.processors.stdout :refer [create-stdout-destination!]]
            [clojure.test :refer :all]))



