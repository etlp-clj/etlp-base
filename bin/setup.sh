#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

(cd $DIR/../ && lein clean && lein deps && lein uberjar)

$DIR/etl.build
