# Quick Start Tutorial for ETLP and Smart Data Connectors

Welcome to the Quick Start Guide for ETLP (Extract, Transform, Load, and Process), featuring the innovative Jute Mapping language. This tutorial will walk you through the essentials of using the ETLP framework with Jute to efficiently manage data transformations and mappings with minimal coding.


## Prelude

ETLP provides a robust platform for data integration, leveraging the Jute Data Mapping DSL (Domain-Specific Language) to simplify and streamline data transformations. Jute is a YAML-based language that allows developers to describe complex data mappings and transformations declaratively.

#### JSON Example 

```JSON

{
  "speaker": { "login": "mlapshin", "email": "mlapshin@health-samurai.io" },
  "fhir?": true,
  "topics": ["mapping", "dsl", "jute", "fhir"],
}

```

#### YAML Example

```YAML

speaker:
  login: spock
  email: spock@health-cloud.io
fhir?: true
topics:
  - mapping
  - dsl
  - jute
  - fhir


```
  
What's more, JUTE templates are safe to evaluate, ensuring a secure runtime environment. Say goodbye to the complexities of writing JSON by hand, as JUTE's YAML-based approach streamlines the data mapping process, making it more accessible and efficient. 
Let's say we have the following JSON/YAML Document:


```YAML

book:
  author:
    name: M. Soloviev
    title: PHD
    gender: m
  title: Approach to Cockroach
  chapters:
    - type: preface
      content: A preface chapter
    - type: content
      content: Chapter 1
    - type: content
      content: Chapter 2
    - type: content
      content: Chapter 3
    - type: afterwords
      content: Afterwords

```

and for some reason, we want to change it to a slightly different format 

### Template

```YAML

type: book
author: $ book.author.name
title: $ book.title
content:
  $map: $ book.chapters.*(this.type = "content")
  $as: i
  $body: $ i.content

```


### Result 

```YAML
type: book
author: M. Soloviev
title: Approach to Cockroach
content:
  - Chapter 1
  - Chapter 2
  - Chapter 3

```


With Low Code Data Mapping, we can integrate these capabilities and develop tools for seamless data integration and transformation.


## Setting Up Your Environment

Before diving into this tutorial, ensure that the ETLP mapper service is running locally as it is essential for fetching the mapping specifications. This service should be accessible at `http://localhost:3031`.


### Example CSV Input

Consider we have the following CSV data being read by our system:

```
Name,Age,Email,Salary
John Doe,30,johndoe@example.com,50000
Jane Smith,25,janesmith@sample.com,55000
```

This CSV data consists of columns for name, age, email, and salary, which the data connector will read from the standard input (stdin).

### Jute Templates for Data Transformation

To begin transforming data, you first need to define mapping rules in the ETLP mapper service. These rules are defined in Jute and can be set up by making HTTP POST requests to the ETLP mapper service. Below are examples of how to create these rules for CSV header mapping and CSV to JSON transformations.


#### 1. **CSV Headers Mapping**

This Jute template is used to define and map the headers of the CSV data:

```yaml
$let:
  headers:
    - Name
    - Age
    - Email
    - Salary

$body:
  headers:
    $map: $ headers
    $as: i
    $body: $ toKeyword(i)
  use-headers: true
  delimiter: ","
```

**Explanation**:
- `$let` is used to define a set of headers.
- `$body` processes these headers:
  - `$map` iterates over each header, converting them into Clojure keywords (using `$toKeyword`). This is essential for programmatically accessing these columns later in the transformation.
  - `use-headers: true` instructs the system to treat the first row of the CSV as headers.
  - `delimiter: ","` specifies that the delimiter for the CSV is a comma.
  
  

#### Register CSV Headers Rule

Headers specify how to interpret the incoming CSV data. Here's how to define a CSV header:

- **Endpoint**: `POST http://localhost:3031/mappings`
- **Payload**:

```json
{
    "title": "Set CSV Headers spec",
    "content": {
        "tags": ["csv", "mapping", "headers"],
        "yaml": "$let:\n  headers:\n    - Name\n    - Age\n    - Email\n    - Salary\n\n$body:\n  headers:\n    $map: $ headers\n    $as: i\n    $body: $ toKeyword(i)\n  use-headers: true\n  delimiter: \",\""
    }
}
```

This mapping sets up CSV headers and configures them to be used as keywords in Clojure, enabling easy access to data fields.


#### 2. **CSV to Nested JSON Transformation**

The following template illustrates transforming flat CSV data into a structured JSON format:

```yaml
employee:
  full_name: $ Name
  age: $ Age
contact:
  email: $ Email
portfolio:
  fixed_income:
    - $ Salary
```

**Explanation**:
- This template maps CSV columns directly to a JSON structure.
- `employee`, `contact`, and `portfolio` are top-level keys in the resulting JSON.
  - Under `employee`, `full_name` and `age` are mapped from the `Name` and `Age` CSV columns.
  - `contact` contains `email`, mapped from the `Email` CSV column.
  - `portfolio` includes `fixed_income`, where `Salary` is listed under this category.


##### Register CSV to Nested JSON transforation rule

Transform the structured CSV data into a more complex nested JSON format:

- **Endpoint**: `POST http://localhost:3031/mappings`
- **Payload**:

```json
{
    "title": "Transform Sample CSV",
    "content": {
        "tags": [],
        "yaml": "employee: \n  full_name: $ Name\n  age: $ Age\ncontact:\n  email: $ Email\nportfolio:\n  fixed_income:\n    - $ Salary"
    }
}
```

## Understanding the Clojure Code for the CSV-Stream Data Connector

In this section, we'll walk through the Clojure code used to configure and run the CSV-stream data connector in the ETLP framework. This connector utilizes the Jute templates to transform CSV input into structured JSON output. We'll break down the key parts of the code to help you understand how each component works together. Additionally, we'll include examples of CSV data and Jute templates to illustrate how the transformations work in practice.

### Namespace and Dependencies

The Clojure code begins by defining the namespace and requiring necessary libraries. This setup includes JSON processing, file I/O, asynchronous operations, string manipulation, and logging facilities, along with specific ETLP utilities.

```clojure
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
```

### CSV Processor Function

The `create-csv-processor` function is the heart of the data connector. It configures how data is read, transformed, and written. This function takes a configuration map with keys for general settings, mapper specifications, and options.

Here’s how the provided Clojure code uses these Jute templates:

```clojure
(defn create-csv-processor [{:keys [config mapper options]}]
  (let [csv-specs          (mapper :CSV-SPECS)
        csv-transform-jute (mapper :CSV-TRANSFORM)
        csv-spec-jute      (csv-specs {:headers [] :use-headers false :delimiter ","})
        in-source          {...}]

    {:source      (create-stdin-source! in-source)
     :destination (create-stdout-destination! destination-conf)
     :xform       (comp
                   (filter (fn[item] (not (empty? item))))
                   (keep (fn [item] (csv-transform-jute item)))
                   (map wrap-record))
     :threads     (options :threads)}))
```

**Flow Explanation**:
1. **Source Configuration**: Configures how to read CSV data based on the headers mapping template.
2. **Transformation**:
   - The data is first filtered to remove any empty rows.
   - `keep` is used with `csv-transform-jute`, applying the transformation template to each row, converting it into the structured JSON format.
   - `map` with `wrap-record` further processes the data into a suitable format for output.
3. **Destination**: Configures output to standard output (stdout), displaying the transformed data.


### Data Source Configuration

Inside `create-csv-processor`, the data source configuration specifies how CSV data should be read and processed. This includes threading settings, partition details, and specific reducers for CSV processing.

```clojure
(let [csv-specs          (mapper :CSV-SPECS)
      csv-transform-jute (mapper :CSV-TRANSFORM)
      csv-spec-jute      (csv-specs {:headers [] :use-headers false :delimiter ","})
      in-source          {:threads    (options :threads)
                          :partitions (options :partitions)
                          :reducers   {:csv-reducer (csv-xform csv-spec-jute)}
                          :reducer    :csv-reducer}
      ...
  )
```

### Destination Configuration

The destination configuration specifies how the output should be handled, including how many threads and partitions to use for output processing.

```clojure
(destination-conf   {:threads    (options :threads)
                     :partitions (options :partitions)})
```

### Transformation Pipeline

The transformation pipeline is set up using `comp`, which composes several functions to process each item:

1. **Filter out empty items**: Ensures that no empty records are processed.
2. **Apply transformations**: Uses the Jute mapping to transform each CSV row.
3. **Wrap records**: Converts each transformed item into a standard format for output.

```clojure
(:xform       (comp
               (filter (fn[item] (not (empty? item))))
               (keep (fn [item] (csv-transform-jute item)))
               (map wrap-record))
 ...)
```

### Connector Configuration

Finally, the function returns a map defining the source and destination of the data, as well as the transformation function to be used. This configuration ties together all the components.

```clojure
{:source      (create-stdin-source! in-source)
 :destination (create-stdout-destination! destination-conf)
 :xform       ...}
```

### Instantiating the Data Connector

The `etlp-fs-processor` map specifies the processor configuration using environment variables for dynamic mapping IDs, ensuring the system remains flexible and configurable.

```clojure
(def etlp-fs-processor {:name       :csv-stream
                        :process-fn  create-csv-processor
                        :etlp-config {}
                        :etlp-mapper {:base-url (System/getenv "ETLP_MAPPER_SERVICE")
                                      :specs    {:CSV-SPECS     (System/getenv "ETLP_MAPPER_CSV_KEY")
                                                 :CSV-TRANSFORM (System/getenv "ETLP_MAPPER_CSV_TRANSFORM_KEY")}})
```


## Running the Data Connector

With your mappings created and the ETLP service running, you can now run the data connector. The data connector reads from `stdin` and writes to `stdout`, applying transformations as defined:

```sh
# Set environment variables with the mapping keys
export ETLP_NAME="etlp-csv"
export ETLP_MAPPER_SERVICE="http://localhost:3031"
export ETLP_MAPPER_CSV_KEY="44"
export ETLP_MAPPER_CSV_TRANSFORM_KEY="45"

# Build Connector 

./bin/setup.sh

# Run the data connector
cat seed-csv-data/*.csv | ./bin/etl --threads 2 csv-stream
```

This command starts the data connector with 2 threads, reading CSV data, applying transformations, and outputting JSON to the console.


### Conclusion

By following this detailed walkthrough, you can understand how the Clojure code in the ETLP framework works together with Jute templates to transform CSV data into complex JSON structures. This powerful combination allows for flexible, low-code data transformations that can be easily updated without redeploying your application, making ETLP a robust solution for various data integration and transformation needs.

