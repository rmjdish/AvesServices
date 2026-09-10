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
import java.io.*;
import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import org.simpleframework.xml.*;
import org.simpleframework.xml.core.*;

import mrc.db.ConnectDB;


public class StoreForm {

	private FormXML doc;
	private String filepath;
	private String insertQuery;
	private static final Logger log = Logger.getLogger("mrc.user");
	private static String myprops = "/jaydb.properties";
	
	public StoreForm(String fp) {
		this.filepath = fp;
		this.doc = new FormXML();
		this.insertQuery = "insert into forms ("
				+ 	"prinapp, prinapppos, prinappinst, prinappemail, coapp, coapppos, coappinst, coappemail, moreapps, title,"
				+   "startdate, enddate, funded, funder, projdesc, pubsumry, otherstudies, studylist, genotyping, genolist,"
				+	"dna, dnalist, samples, samplelist, rna, rnalist, fqcexp, insight46, insight46list, imagingdata,"
				+ 	"imagingdatalist, imgexp, varlist, secmgr, secpols, datcenloc, accctrl, datamgr, contsrc, signname,"
				+	"datesub ) "
				+	"values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		
	}
	
	public void readXMLfile() throws Exception {
		log.fine(HostInfo.tell()+" StoreForm: reading XML file: "+this.filepath);
		Serializer parseForm = new Persister();
		try {
			File xmlfile = new File(this.filepath);
			this.doc = parseForm.read(FormXML.class, xmlfile);			
		} catch (Exception e) {
			log.severe(HostInfo.tell()+" StoreForm: error parsing XML input: "+this.filepath);
			e.printStackTrace();
			throw e;
		}
		log.fine(HostInfo.tell()+" StoreForm: XML:\nPrinApp: "+this.doc.getPrinapp()+"\nTitle: "+this.doc.getTitle()+"\nDateSub: "+this.doc.getDatesub());
		log.fine(HostInfo.tell()+" StoreForm: XML:\nFunded: "+this.doc.getFunded());
	}
	
	public void dbinsert() {
		
		// 42 columns in forms table of which we want to insert 41!
		HashMap<Integer,Object> theparms = new HashMap<Integer,Object>();
		theparms.put(1,  this.doc.getPrinapp());
		theparms.put(2,  this.doc.getPrinapppos());
		theparms.put(3,  this.doc.getPrinappinst());
		theparms.put(4,  this.doc.getPrinappemail());
		theparms.put(5,  this.doc.getCoapp());
		theparms.put(6,  this.doc.getCoapppos());
		theparms.put(7,  this.doc.getCoappinst());
		theparms.put(8,  this.doc.getCoappemail());
		theparms.put(9,  this.doc.getMoreapps());
		theparms.put(10, this.doc.getTitle());
		theparms.put(11, this.doc.getStartdate());
		theparms.put(12, this.doc.getEnddate());
		theparms.put(13, this.doc.getFunded());
		theparms.put(14, this.doc.getFunder());
		theparms.put(15, this.doc.getProjdesc());
		theparms.put(16, this.doc.getPubsumry());
		theparms.put(17, this.doc.getOtherstudies());
		theparms.put(18, this.doc.getStudyList());
		theparms.put(19, this.doc.getGenotyping());
		theparms.put(20, this.doc.getGenolist());
		theparms.put(21, this.doc.getDna());
		theparms.put(22, this.doc.getDnalist());
		theparms.put(23, this.doc.getSamples());
		theparms.put(24, this.doc.getSamplelist());
		theparms.put(25, this.doc.getRna());
		theparms.put(26, this.doc.getRnalist());
		theparms.put(27, this.doc.getFqcexp());
		theparms.put(28, this.doc.getInsight46());
		theparms.put(29, this.doc.getInsight46list());
		theparms.put(30, this.doc.getImagingdata());
		theparms.put(31, this.doc.getImagingdatalist());
		theparms.put(32, this.doc.getImgexp());
		theparms.put(33,this.doc.getVarlist());
		theparms.put(34, this.doc.getSecmgr());
		theparms.put(35,this.doc.getSecpols());
		theparms.put(36, this.doc.getDatcenloc());
		theparms.put(37,this.doc.getAccctrl());
		theparms.put(38, this.doc.getDatamgr());
		theparms.put(39,this.doc.getContsrc());
		theparms.put(40, this.doc.getSignname());
		theparms.put(41,this.doc.getDatesub());
		for (Integer key : theparms.keySet()) {
			log.fine(HostInfo.tell()+" StoreForm: dbinsert: Parm["+key+"] =  "+theparms.get(key));
		}
		// Put this into a query and send it
		try {
			ConnectDB c = new ConnectDB(myprops);
			c.capture();
			int rs = c.doPUpdate(this.insertQuery,theparms);
			c.release();

		} catch (Exception e) {
			log.severe(HostInfo.tell()+" StoreForm: dbinsert: Error executing insert.");
			e.printStackTrace();
			throw e;
		}
	}
	
	public void logform() {
		
	}
}
