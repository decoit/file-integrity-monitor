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
package de.decoit.simu.fim.IFmap;

/**
 * Enum for Credential Types
 * @author Leonid Schwenke, DECOIT GmbH
 *
 */
public enum CredentialType {
	PASSWORD("Password"), PUBLIC_KEY("Public-Key"), BIOMETRIC("Biometric"), TOKEN(
			"Token"), OTHER("Other");

	public String name;

	/**
	 * Constructor
	 * @param name Credential Type
	 */
	private CredentialType(String name) {
		this.name = name;
	}

	/**
	 * Credential Type to String
	 */
	public String toString() {
		return name;
	};
}
