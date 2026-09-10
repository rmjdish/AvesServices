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

package ucl.service.zip;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import jakarta.jws.WebService;
import jakarta.xml.ws.soap.MTOM;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.activation.MimetypesFileTypeMap;
import mrc.util.HostInfo;
/*
 * Class: Boomerang
 * 	 Implements the web service End Point Interface (EPI) for the Swift 2.0 *ZIPService*
 */
@MTOM(enabled=true, threshold=4096)
@WebService(endpointInterface = "ucl.service.zip.ZIPService",
	    wsdlLocation = "WEB-INF/wsdl/boomerang.wsdl",
	    serviceName = "Boomerang", 
	    portName = "BoomerangPort",
	    targetNamespace="http://zip.service.ucl/")  
public class Boomerang implements ZIPService {
    /*
     * This is a simple web service that has three callable methods:
     * 
     * throwjob takes 2 arguments, a basket name and a Condor user name
     * It builds baskets of datasets: it creates a
     * shell script in /tmp, creates a project directory in the right place and
     * executes the shell script to put output files in the project directory
     *
     * lobjob takes 3 arguments, a basket name, a jobname (i.e. the name of a Python
     * program to call), and parmlist (a list of parameters contained in a string)
     * Other than that it does the same as throwjob, except it is mostly used to run
     * the scrambling progarm sdum.py
     * 
     * catchjob takes one argument, a basket name It uses DataHandler to (hopefully)
     * return the zip file located at the project directory created by throwjob
     * Watch out catchjob can deliver null if it can't find the zipfile!!!
     */
    private static final Logger log = Logger.getLogger("ucl.service.zip");
    private static final String PATHSEP = "/";
    private static final String LOBSUFFIX = "-SCRAMBLED";
    private static final String tempdir = "/tmp";
    private static final String scriptext = ".sh";
    private static final String BASHPATH = "/usr/bin/bash";
    private static final String LOBJOBNAME = "/opt/swift/bash/lobjob.sh";
    private static final String THROWJOBNAME = "/opt/swift/bash/throwjob.sh";
    private static final String propfile = "zipservice.properties";
    
    /*
     * 
     * Function: throwjob
     * 	 Accepts parameters and uses them to invoke a Python program to create data and
     * 	 metadata CSV files based on an Swift basket.  Creates these in an appropriate place
     * 	 corresponding to the Swift user name of the basket owner.  Uses zip to bundle up 
     * 	 all products
     * 
     * Parameters:
     * 	 basket - the name of the Swift basket
     * 	 username - the Swift user name of the basket owner
     * 
     * See Also:
     * 	 <BasketBuilder>
     * 
     */
    @Override	
    public void throwjob(String basket, String username) {
	// Save file to temporary directory
	ResourceBundle bdl = null;
	try {
	    bdl = new PropertyResourceBundle(new FileInputStream(dnget() + PATHSEP + propfile));
	}
	catch (Exception e) {
	    log.severe(HostInfo.tell()+"throwjob: Error:"+e.toString()+" reading properties file.");
	    return;       	   			
	}
	String remotedir   = bdl.getString("pyLinPath"); // where on remote server will data files be created?
	String buildsuffix = bdl.getString("pyBuildSuf"); // the sub-folder for storing built datasets
	String scramsuffix = bdl.getString("pyScramSuf"); // the sub-folder for storing scrambled datasets
	String buildfolder = remotedir + PATHSEP + basket + PATHSEP + buildsuffix;
	String scramfolder = remotedir + PATHSEP + basket + PATHSEP + scramsuffix;
	String workdir = buildfolder;
	String scriptfile = tempdir+PATHSEP+basket+scriptext;
	log.fine(HostInfo.tell()+" Boomerang: throwjob: workfolder: "+workdir+" script file: "+scriptfile);
	try 
	    {
		/*
		 * Create a file in /tmp that will be the script to be executed
		 */
		log.fine(HostInfo.tell()+" Boomerang: throwjob: executing: python "+
			 bdl.getString("pyScriptPath") + PATHSEP + "swift_download.py " + basket + " " + username);
		
		BufferedWriter out = new BufferedWriter(new FileWriter(scriptfile));
		out.write("#!/bin/bash");out.newLine();
		out.write("${THROWJOBNAME} " + basket + " " + username);out.newLine();
		out.close();
	    }
	catch (IOException e)
	    {
		log.severe(HostInfo.tell()+" Error creating script to execute: "+tempdir+PATHSEP+basket+scriptext);
		log.severe(HostInfo.tell()+" Error:"+e.toString());
		return;
	    }
	log.fine(HostInfo.tell()+" Boomerang: script file generated.");
	// Execute the shell script just created 
	File fDir;
	
	try 
	    {
		// Create project directory within pyLinPath for output files
		fDir = new File(workdir+PATHSEP+basket);
		if (!fDir.exists())
		    {
			log.fine(HostInfo.tell()+" Boomerang: throwjob: trying to make dir:"+workdir+PATHSEP+basket);
			fDir.mkdir();  // create it if it doesn't exist
		    }
		// Drop to the shell and execute shell script created above 
		log.info(HostInfo.tell()+"Boomerang: throwjob: Executing scriptfile: "+scriptfile);
		Process shell = Runtime.getRuntime().exec("/bin/bash "+ scriptfile);
		shell.waitFor(60,TimeUnit.SECONDS); // Need to give this some time to do its work
		log.fine(HostInfo.tell()+"Boomerang: throwjob: Finished execution.");
	    }
	catch (Exception e) 
	    {
		log.severe(HostInfo.tell()+"Error:"+e.toString()+" executing script on this host.");
		return;
	    }
	
    }
    
    /*
     * 
     * Function: lobjob
     * 	 Accepts parameters and uses them to invoke a Python program to scramble existing data
     * 	 and metadata CSV files based on an Swift basket id.  Creates these in an appropriate place
     * 	 corresponding to the basket id.  Uses zip to bundle up 
     * 	 all products
     * 
     * Parameters:
     * 	 basket - the name of the Swift basket
     * 	 jobname - the name of the Python program to call
     * 	 parmlist - the string of parameters for the Python program
     * 
     * See Also:
     * 
     * 
     */
    @Override	
    public void lobjob(String basket, String jobname, String parmlist) {
        // Save file to temporary directory
	
	ResourceBundle bdl = null;
	try {
	    bdl = new PropertyResourceBundle(new FileInputStream(dnget() + PATHSEP + propfile));
	}
	catch (Exception e) {
	    log.severe(HostInfo.tell()+"throwjob: Error:"+e.toString()+" reading properties file.");
            return;       	   			
	}
	String remotedir   = bdl.getString("pyLinPath"); // where on remote server will data files be created?
	String buildsuffix = bdl.getString("pyBuildSuf"); // the sub-folder for storing built datasets
	String scramsuffix = bdl.getString("pyScramSuf"); // the sub-folder for storing scrambled datasets
	String buildfolder = remotedir + PATHSEP + basket + PATHSEP + buildsuffix;
	String scramfolder = remotedir + PATHSEP + basket + PATHSEP + scramsuffix;
	String workdir     = scramfolder;
	String scriptfile  = tempdir+PATHSEP+basket+LOBSUFFIX+scriptext;
	log.fine(HostInfo.tell()+" Boomerang: lobjob: workfolder: "+workdir+" script file: "+scriptfile);
        try // to create the shell script file with commands to execute
	    {
    		log.fine(HostInfo.tell()+" Boomerang: lobjob: executing: python "+
    	        	 bdl.getString("pyScriptPath") + PATHSEP + jobname + " " + parmlist);
        	/*
        	 * Create a file in /tmp that will be the script to be executed
        	 */
		BufferedWriter out = new BufferedWriter(new FileWriter(scriptfile));
		out.write("#!/bin/bash");out.newLine();
		out.write("${LOBJOBNAME} " + basket + " " + jobname);out.newLine();
		out.close();
	    }
        catch (IOException e)
	    {
        	log.severe(HostInfo.tell()+"Error creating script to execute: "+tempdir+PATHSEP+basket+LOBSUFFIX+scriptext);
        	log.severe(HostInfo.tell()+"Error returned:"+e.toString());
		return;
	    }
        // Execute the shell script just created 
        log.info(HostInfo.tell()+"Boomerang: throwjob: Executing scriptfile: "+scriptfile);
        File fDir;
        try 
	    {
		// Create project directory within pyLinPath for output files
		fDir = new File(workdir);
		if (!fDir.exists())
		    {
			fDir.mkdir();  // create it if it doesn't exist
		    }
		// Drop to the shell and execute shell script created above 
		Process shell = Runtime.getRuntime().exec("/bin/bash "+scriptfile);
		shell.waitFor(60,TimeUnit.SECONDS); // Need to give this some time to do its work
	    }
	catch (Exception e) 
	    {
		log.severe(HostInfo.tell()+"Error:"+e.toString()+" executing script on this host.");
		return;
	    }
	
    }
    
    /*	 * 
     * Function: catchjob
     * 	Looks up the location of a ZIP file that contains data and metadata and
     * 	returns a DataHandler reference to that ZIP file if it exists.  Returns a 
     * 	null file reference in the case where it does not exist.  Uses the Java 
     * 	properties file to guide where to look.
     * 
     * Parameter:
     * 	basket - the name of the basket that has (hopefully) been built into CSV files
     * 
     * Returns: fileDataHandler
     * 	 A DataHandler object that refers to a ZIP file in the host file system
     */
    @Override	
    public DataHandler catchjob(String basket, String type) {
	// Need to workout where the file is from the basket name
	/* About:
	 * 	The type argument should be either 'scramble' or 'build'  this is the means
	 * 	by which the operation knows where to look for the file and the structure of the
	 * 	file name.
	 * 		build    ==> .../raw/<basketID>.zip
	 * 		scramble ==> .../scrambled/<basketID>-SCRAMBLED.zip
	 */
        ResourceBundle bdl = null;
        try {
            bdl = new PropertyResourceBundle(new FileInputStream(dnget() + propfile));       	   
        }
        catch (Exception e) {
	    log.severe("catchjob: Error reading properties file.");
            e.printStackTrace();       	   
        }
	String remotedir   = bdl.getString("pyLinPath"); // where on remote server will data files be created?
	String buildsuffix = bdl.getString("pyBuildSuf"); // the sub-folder for storing built datasets
	String scramsuffix = bdl.getString("pyScramSuf"); // the sub-folder for storing scrambled datasets
	String buildfolder = remotedir + PATHSEP + basket + PATHSEP + buildsuffix;
	String scramfolder = remotedir + PATHSEP + basket + PATHSEP + scramsuffix;
	String workdir = null;
	File zipfile = null;
	if (type.equalsIgnoreCase("build")) {
	    workdir = buildfolder;
	    zipfile = new File(workdir+PATHSEP+basket+".zip");
	} else {
	    workdir = scramfolder;
	    zipfile = new File(workdir+PATHSEP+basket+LOBSUFFIX+".zip");
	}
        // Now we know where the file should be, but is it there?
        if (zipfile.exists()) {
	    log.info(HostInfo.tell()+" Boomerang: catchjob: zipfile created at "+workdir);
            FileDataSource dataSource = new FileDataSource(zipfile);
            // It's important to set the Mime type of the file via a FileTypeMap object
            MimetypesFileTypeMap mftp = new MimetypesFileTypeMap();
            mftp.addMimeTypes("application/zip" );
            dataSource.setFileTypeMap(mftp);
            DataHandler fileDataHandler = new DataHandler(dataSource);
	    return fileDataHandler;
	    
        } else {
	    try {
		log.info("ZIPFile catchjob returning empty file ref: "+zipfile.getCanonicalPath());
	    } catch (IOException e) {
		log.severe("ZIPFile catchjob error:" + e.toString()+" numerating path for empty zipfile.");
		return null;
	    }
	    return null;
        }
    }
    
    
    /**
     * Gets local directory name for properties file.
     */
    private String dnget() {
	java.net.URL u = this.getClass().getResource(cnget());
	String dn = u.getPath().replaceAll("%20", " ");
	return dn.substring(0, dn.lastIndexOf(PATHSEP));
    }
    
    /**
     * Gets local class name for properties file.
     */
    private String cnget() {
	String c = this.getClass().getName();
	c = c.substring(c.lastIndexOf(".") + 1, c.length());
	return c += ".class";
    }
    
}
