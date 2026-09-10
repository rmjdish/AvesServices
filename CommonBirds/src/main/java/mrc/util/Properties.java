/*
    This file is part of Jay/Condor/Swift.

    Jay/Condor/Swift is free software: you can redistribute it and/or
    modify it under the terms of the GNU General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    Jay/Condor/Swift is distributed in the hope that it will be
    useful, but WITHOUT ANY WARRANTY; without even the implied
    warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
    PURPOSE. See the GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Jay/Condor/Swift. If not, see
    <https://www.gnu.org/licenses/>.

*/

package mrc.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class Properties {

	private static final Logger log = Logger.getLogger("mrc.user");
	private Enumeration<String> keys = null;
	private ResourceBundle bundle = null;

	public Properties (String propfilename) {
		try {
			this.bundle = new PropertyResourceBundle(new FileInputStream(propfilename));			
		} catch (FileNotFoundException fnfe) {
			log.severe(HostInfo.tell()+": Properties file not found: " + propfilename);
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			log.severe(HostInfo.tell()+": Properties: IO Error: " + propfilename);
		}
		this.keys = this.bundle.getKeys();
	}
	
	public String keySearch (String ky) {
		String result = null;
		if (this.bundle.containsKey(ky)) {
			result = this.bundle.getString(ky);
		}
		return result;
	}
}
