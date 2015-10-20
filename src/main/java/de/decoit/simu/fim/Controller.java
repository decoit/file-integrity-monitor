/* 
 * Copyright (C) 2015 DECOIT GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.decoit.simu.fim;

import de.decoit.simu.fim.FileFolder.File;
import de.decoit.simu.fim.FileFolder.Folder;
import de.decoit.simu.fim.IFmap.ReadConfig;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.HashSet;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.InputStreamReader;

/**
 *
 * @author Artur Schmidt (schmidt@decoit.de)
 */
public class Controller {

	private static Controller controller;
	private static final HashSet<Path> folders = new HashSet<>();
	private static final HashSet<Path> files = new HashSet<>();
	private static final HashMap<Path, Importance> file = new HashMap<>();
	private static final HashSet<Path> filesInFolder = new HashSet<>();

	private static Calendar cal;
	private static final String[] stringArray = new String[4];
	private static String IP = "";

	/*
	 *   Enum for Importance-Type
	 */
	public enum Importance {

		low, medium, high, critical
	}

	private static File fileThread;

	private static final HashMap<Path, Importance> filePaths = new HashMap<Path, Importance>();

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		switch (System.getProperty("os.name")) {
			case ( "Linux" ):
				readConfigFile();
				break; //Objekte fue Linux erstellen;
			case ( "Windows" ):
				break; //Objekte fuer windows erstellen;
			default:
				System.out.println("MAC WIRD NICHT UNTERSTUETZT!!!");
		}
	}

	private static void readConfigFile() {
		FileReader fr = null;
		String line = "";
		Importance i = null;
		String system = "";
		try {
			InputStream in = Controller.class.getResourceAsStream("/config/paths.txt");
			//fr = new FileReader("/config/paths.txt");
			try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
				System.out.println("The following Files/Folders are monitored: ");
				while (( line = br.readLine() ) != null) {
					String[] fileImportance = line.split("[,=]");
					if (fileImportance.length == 1) {
						continue;
					}
					fileImportance[0] = fileImportance[0].trim();
					Path p = Paths.get(fileImportance[0]);
					if (Files.exists(p) == true) {
						switch (fileImportance[1].toLowerCase().trim()) {
							case ( "critical" ):
								i = Importance.critical;
								break;
							case ( "low" ):
								i = Importance.low;
								break;
							case ( "medium" ):
								i = Importance.medium;
								break;
							case ( "high" ):
								i = Importance.high;
								break;
							default:
								System.out.println("importance for " + fileImportance[0]
										+ " was not right. Importance set to " + Importance.medium);
								i = Importance.medium;
								break;
						}
						if (Files.isDirectory(p) == true) {
							System.out.println(p);
							fileList(p.toString(), i);
							new Folder(p, i).start();
						} else {
							if (Files.isRegularFile(p) == true) {
								System.out.println(p);
								filePaths.put(p, i);
								file.put(p, i);
							}
						}
						break;
					}

				}
			}
			in.close();
			fileThread = new File(file);
			fileThread.start();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
			System.out.println("File not found!");
		} catch (IOException ex) {
			Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
		}
		ReadConfig rc;
		rc = new ReadConfig();
		system = rc.useIcingaOrIfmap();
		IP = rc.icingaIP();
		switch (system.toLowerCase().trim()) {
			case ( "ifmap" ):
				new TransmitterIFmap().start();
				break;
			case ( "icinga" ):
				new TransmitterIcinga().start();
				break;
		}
	}

	/**
	 *
	 * @param paths
	 */
	public static void startWatcher(final HashSet<Path> paths) {
		sort(paths);

		//AKTIVIEREN DER TRANSMITTER 
		for (Path folder : folders) {
			new Folder(folder, Importance.critical).start();
		}
		if (!files.isEmpty()) {
            //File threadFiles = new File(files,Importance.CRITICAL);
			//threadFiles.start();
		}
		new TransmitterIFmap().start();
	}

	/**
	 * Sortiert das übergebene Array nach Daten- und Ordnerpfaden
	 */
	private static void sort(final HashSet<Path> paths) {
		for (Path p : paths) {
			if (Files.exists(p) == true) {
				if (Files.isDirectory(p) == true) {
					folders.add(p);
				} else {
					if (Files.isRegularFile(p) == true) {
						files.add(p);
					}
				}
			}
		}
	}

	/**
	 *
	 * @return
	 */
	public HashSet<Path> getFolders() {
		return folders;
	}

	/**
	 *
	 * @param kind of modification : deleted, modified, new, exists
	 * @param path of the modified File
	 * @param importance
	 */
	synchronized public void send(final String kind, final String path, String importance) {
		//System.out.println(kind + "  " + path);
		stringArray[0] = Controller.getTime();
		stringArray[1] = kind;
		stringArray[2] = path;
		stringArray[3] = importance;
		Queue.fillQueue(stringArray);
	}

	/**
	 *
	 * @return Instance of Controller.
	 */
	public static Controller getController() {
		if (controller == null) {
			controller = new Controller();
		}
		return controller;
	}

	/**
	 * Clears all Arrays. Only used when GUI is activated
	 */
	public void clearArrays() {
		files.clear();
		folders.clear();
	}

	/**
	 *
	 * @return current Time: yyyy-mm-dd hh:mm:ss
	 */
	public static String getTime() {
		String time = "";
		cal = Calendar.getInstance();
		time = cal.get(Calendar.YEAR) + "-"
				+ ( ( cal.get(Calendar.MONTH) + 1 ) < 10 ? "0" + ( cal.get(Calendar.MONTH) + 1 ) : ( cal.get(
								Calendar.MONTH) + 1 ) ) + "-"
				+ ( cal.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + cal.get(Calendar.DAY_OF_MONTH) : cal.get(
								Calendar.DAY_OF_MONTH) ) + " "
				+ ( cal.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + cal.get(Calendar.HOUR_OF_DAY) : cal.get(
								Calendar.HOUR_OF_DAY) ) + ":"
				+ ( cal.get(Calendar.MINUTE) < 10 ? "0" + cal.get(Calendar.MINUTE) : cal.get(Calendar.MINUTE) ) + ":"
				+ ( cal.get(Calendar.SECOND) < 10 ? "0" + cal.get(Calendar.SECOND) : cal.get(Calendar.SECOND) );
		return time;
	}

	/**
	 *
	 * @return
	 */
	public static HashMap<Path, Importance> getFiles() {
		return filePaths;
	}

	/**
	 * Put all Files in the directory into a Hashmap
	 * <p>
	 * @param directory
	 * @param importance
	 */
	private static void fileList(String directory, Importance importance) {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
			for (Path p : directoryStream) {
				if (!filePaths.containsKey(p)) {
					filePaths.put(p, importance);
				}
			}
		} catch (IOException ex) {
		}
	}

	public static String getIP() {
		return IP;
	}

	public static void setIP(String IP) {
		Controller.IP = IP;
	}
}
