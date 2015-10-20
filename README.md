# DECOIT File Integrity Monitor

This is a prototype implementation of a file integrity monitor service that was developed during the [iMonitor](http://www.imonitor-project.de/) and [SIMU](http://www.simu-project.de/) research projects. It is implemented in Java and allows a user to monitor files and folders on a Linux machine for changes. If changes are detected, the results may be sent to either an Icinga instance or an IF-MAP MAP-Server. The iMonitor project used the Icinga approach while the SIMU project used the MAP-Server with a custom metadata and identifier structure.

## Preparation ##

This project requires the SIMU Metadata Factory to be installed in your local Maven repository. The factory may be [downloaded here](https://github.com/decoit/simu-metadata-factory). Besides this library there are some further requirements to compile and run the file integrity monitor:

* Java 7 or higher
* Maven 3

To compile this project the Oracle JDK is preferred but it may work as well on other JDK implementations. Any Java 7 compatible JRE (Oracle, OpenJDK, Apple) should be able to run the application.

## Installation ##

Follow these steps to compile the project:

* Make sure that you have the SIMU Metadata Factory installed
* Open a command prompt and change directory to the root of this project
* Execute `mvn package`
* Unpack the contents of `target/file-integrity-monitor-1.0-SNAPSHOT.dist.zip` or `target/file-integrity-monitor-1.0-SNAPSHOT.dist.tar.gz` to your chosen installation directory

## Configuration ##

The file integrity monitor uses two configuration files, both located inside the `config/` directory inside the installation location.

The first one is the Config.properties file that contains basic application configuration such as the Icinga or MAP-Server address, mode selection etc. The comments inside the sample file describe each setting, please refer to those to select the correct settings.

The second file is the paths.txt that contains a list of files and folders that will be monitored by the application. It uses a simple line format to specify the monitor targets:

```
/path/to/target , importance
```

The `/path/to/target/` must be a valid path on the file system to either a file or folder. If the target is a folder, all files inside it will be added to the list of monitored files. Additonally any newly created file inside that folder will be detected and added to the list. The `importance` parameter may be one of the following:

* low
* medium
* high
* critical

This parameter is used to specifiy how critical a change to a file or folder is considered by the user. For example, a correlation rule of a SIEM system may filter file changes for this parameter and only react to those with importance high or critical while ignoring any other value.

## Run the application ##

To run the application simple execute `java -jar file-integrity-monitor-1.0-SNAPSHOT.jar` inside the installation directory. It will automatically connect to the selected Icinga instance or MAP-Server and start monitoring the files specified before.

Please remember that this is a prototype implementation and as such it may (and probably will) contain bugs and is not secured against any type of force quit action. The monitor application may be shut down by simply killing its process or the JVM process. Because of this it is not advised to use the software on production systems!

## License ##

The source code and all other contents of this repository are copyright by DECOIT GmbH and licensed under the terms of the [GNU General Public License Version 3](http://www.gnu.org/licenses/gpl.txt). A copy of the license may be found inside the LICENSE file.
