### ETLP-BASE Starter Kit Setup Guide

#### Prerequisites

Before setting up the ETLP-BASE boilerplate repository, ensure you have the following installed:

1. **Java Development Kit (JDK)**:
   - You need to have Java installed on your system. Follow the [official Java installation guide](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) to install JDK.


#### Setting up Clojure and Leiningen

1. **Install Clojure**:
   - Clojure runs on the Java Virtual Machine (JVM). Follow the [official Clojure installation guide](https://clojure.org/guides/install_clojure#java) to set up Clojure on your system.

2. **Install Leiningen**:
   - Leiningen is a build automation tool for Clojure projects. Follow the instructions on the [Leiningen website](https://leiningen.org/#install) to install it on your system.

#### Configuring ETLP-BASE Repository

1. **Clone the Repository**:
   - Clone the ETLP-BASE repository from [GitHub](https://github.com/etlp-clj/etlp-base) to your local machine.

2. **Copy Environment Template**:
   - Locate the `env.tpl` file in the repository root.
   - Create a new file named `.env` by copying `env.tpl`.
   - Update the variables in `.env` file according to your ETLP-Mapper configurations.

#### Building and Running the Data Connector

1. **Setup Project**:
   - Run the `./bin/setup.sh` script in the repository root to set up the project dependencies.

2. **Running the Data Connector**:
   - Use the following command to run the data connector:
     ```
     ./bin/etl --threads 2 csv-stream
     ```
   - This command starts the data connector with 2 threads, listening for CSV rows on stdin, and applying Jute-based transformations to the data.
   - You can adjust the number of threads as per your requirements.

#### Example Usage

```sh
git clone https://github.com/your-repo-url.git
cd etlp-base
cp env.tpl .env
# Update variables in .env file
./bin/setup.sh
cat  ~/Downloads/input.csv |./bin/etl --threads 1 csv-stream
```

Follow these steps to set up and run the ETLP-BASE boilerplate repository on your local machine. Customize the environment variables and configurations as needed for your ETLP-Mapper setup.

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2024 Rahul Gaur

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
