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

// NOTE: projectID/basketID not yet updated for EXTERNAL users
package mrc.user;


import java.io.*;
import java.sql.*;
import java.util.Date;
import org.simpleframework.xml.*;
import org.simpleframework.xml.core.*;

import mrc.db.ConnectDB;
import mrc.util.BasketXML;

public class StoreXMLBasket 
{
	/**
	 * Stores a Basket in XML format into a table in robin database
	 * @author LHA SST
	 */
	private BasketXML doc;
	
	public  StoreXMLBasket(String username, String basket, String description, String projectId) 
	{
		try
		{
			/*
			 * About
			 * the Download.java class originally stored baskets as files on the server.  This is another
			 * approach where we aim to push the XML into a table keyed by basket name.
			 * The following two lines are used to access the directory name for files to be downloaded
			 * Not sure we're going this way!
		        	ResourceBundle bdl = new PropertyResourceBundle(new FileInputStream(dnget() + "/system.properties"));
		            String dir = bdl.getString("downloadDir");
			 */
			/*
			 * We now have a list of variables; time to instantiate BasketXML objects
			 */
			BasketXML xbasket = new BasketXML(basket);  // Create the Root XML object
			xbasket.setUserName(username);              // Associate with the user
			xbasket.setDescription(description);        // A description of this Basket
			xbasket.setDate(new Date());                // The creation date, i.e. now
			xbasket.setProjectId(projectId);			// Initially storing the basketId in here
			this.doc = xbasket;
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void plusVar(String vname) {
			this.doc.addVar(vname);
	}
	
	
	public void storeDoc() {
        /*
         * Now serialise the xbasket object to output
         */
        Serializer serializer = new Persister();
	
			/*
			 * Now serialise the xbasket object to output and push to robin.xmlbaskets
			 */
		try {
				ByteArrayOutputStream xoutf = new ByteArrayOutputStream();
				serializer.write(this.doc, xoutf);  // This should write the file to the outputstream
				String basketId = this.doc.getBasketId();
				String query = "INSERT INTO xmlbaskets (basketId, xmlcontent) VALUES (\'"+basketId+"\',\'"+xoutf.toString()+"\')";
	            ConnectDB c = new ConnectDB();
	            c.capture();
	            int numinserted = c.doUpdate(query);
				c.release();
				xoutf.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			} 


		}
	}





