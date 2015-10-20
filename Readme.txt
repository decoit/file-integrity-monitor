FIM ReadMe
##########

The File Integrity Monitor(FIM) is for monitoring files and folder. If a monitored file or an file inside a monitored folder is changed, an Event will be send to an Icinga- or IF-MAP-server.



Getting started!
################

* Unzip the FIM_mvn-1.0-SNAPSHOT-dist.zip. 
* Enter the Folder FIM_mvn-1.0-SNAPSHOT and open the config.properties inside the config Folder. 
* Change the Settings of the config.properties. If you are using IF-MAP, you have to replace the following Information

	-- mapserver.url=https://<IP-Address>:<Port>  	//IP-Address and Port of the Mapserver 
	-- mapserver.keystore.path=<enter Path> 	//Path to the Keystore and the File iptablesmap.jks
	-- mapserver.keystore.password=<password>	//Password of the Keystorefiles
	-- mapserver.truststore.path=<enter Path>	//Path to your truststorefiles
	-- mapserver.truststore.password=<password> 	//Password of the Truststorefiles	
	-- mapserver.basicauth.enabled=[true|false]	//true if basic authentication is enabled, false if it is disabled 
	-- mapserver.basicauth.user=<username>		//Enter here your username
	-- mapserver.basicauth.password=<password>	//Your Username
  	-- IP-Address=<IP-Address>			//Enter here the IP-Address of your Device.
	-- Icinga.or.Ifmap=<ifmap|icinga>		//Choose between using ifmap or icinga just by typing "icinga" or "ifmap". If 								//the entered value is incorrect, the system will use 
							// IF-MAP and try to connect to a mapserver.
 
   If using Icinga, the mapserver information are not necessarily. 

* Don't forget to save you changes! 
* Next step is to specify the monitored files or folders. Therefore you have to modify the paths.txt
* Just enter the Path(file paths or folder paths) and the importance of it (low, medium, high, critical).
  The following example shows how your file should look like:

		/testroot/testfolder/testfile.txt , low
		/home , critical
		/etc/network, critical

* Save the file! Now you can run the jar with the following command:
		java -jar FIM_mvn-1.0-SNAPSHOT.jar
* The programm should display the monitored files and folders! 

HAVE FUN! 
	





