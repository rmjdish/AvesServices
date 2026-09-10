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

package mrc.user;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import mrc.util.Page;
import mrc.util.HostInfo;
import mrc.util.TransX;

/*
 * Class: SearchRest
 * 	Displays available DICOM files from LHA XNAT server
 * 
 * See Also:
 * 	<TransX>
 * 
 */
public class SearchRest extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	/*
	 * About: xnat
	 * 	The static string xnat is bound to the URI of the XNAT server REST API and defines
	 * 	the format of the result (can be csv/html/xml)
	 */
	private static final String xnat = "http://swan.mrceuston.local:8980/data/search?format=xml";
	private static final Logger log = Logger.getLogger("mrc.user");
	private static final String whome = "swift";
	private static final String mepas = "X}16grNW@b7p";
	private static final String xnatquery = "xnatgensearch.xml";
	/* Procedure: doGet
	* 	Overriden procedure that accepts HTTP GET requests from Tomcat Java Servlet framework
    * 
    * Parameters:
    * 	request - HttpServletRequest object carrying attributes of the HTML GET
    * 	response - HttpServletResponse object carrying attributes of the HTML output channel
    * 
    */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {
        HttpSession s = request.getSession();
		String accessLevel = s.getAttribute("accessLevel").toString();        
        PrintWriter out = response.getWriter();
	    Page p = new Page("SearchRest-displayResults");	
	    StringBuffer textlines = new StringBuffer();
	    ArrayList<String> data = new ArrayList<String>();
	    /* About: colheads
	     * The CSV file returned from XNAT does not have nice
	     * column headings.  This is just to translate them into
	     * something understandable.
	     */
	    HashMap<String,String> colheads = new HashMap<String,String>();
	    colheads.put("sub_project_identifier_nshd1946","NTAG");
	    colheads.put("subjectid" , "Xnat_ID");
	    colheads.put("projects" , "Project_List");
	    colheads.put("project" , "Project_Name");
	    colheads.put("cnt_xnat_petmrsessiondata_nshd1946" , "Num_PETMR_Sessions");
	    colheads.put("cnt_xnat_mrsessiondata_nshd1946" , "Num_MR_Sessions");
	    colheads.put("cnt_xnat_otherdicomsessiondata_nshd1946" , "Num_DXA_Sessions");
	    colheads.put("cnt_xnat_petsessiondata_nshd1946" , "Num_Pet_Sessions");
	    colheads.put("quarantine_status" , "Xnat_Status");
	    colheads.put("key" , "Xnat_ID");
	    
	    /* About: Credentials
	     * 	The POST request requires authentication for it to work.
	     * 	This has to be crafted into the POST HttpClient object somehow.
	     * 	In this case we build it in at the object construction but 
	     * 	there is also the option of adding headers to the request.
	     */
	    CredentialsProvider provider = new BasicCredentialsProvider();
	    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(whome, mepas);
	    provider.setCredentials(AuthScope.ANY, credentials);
		CloseableHttpClient hclient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
		/*
		 * About: Http Entities
		 * 	There are two kinds of HTTP messages; those that carry entities and 
		 * 	those that don't.  We're using POST to send an XML encoded query so
		 * 	we need to create an entity and tie it to the HttpPost object we're
		 * 	going to use for the request.
		 */
		StringEntity queryEntity = new StringEntity(getQuery(), 
				   ContentType.create("text/plain", "UTF-8"));
		HttpPost hpost = new HttpPost(xnat);
		hpost.setEntity(queryEntity);
		CloseableHttpResponse searchresp = hclient.execute(hpost);

		try {
		    // Get any results returned in searchresp
			HttpEntity resEntity = searchresp.getEntity();
			if (resEntity != null) {
				BufferedReader instream = new BufferedReader(new InputStreamReader(resEntity.getContent()));
				String inputLine;
				int numrows = 0;
				textlines.append("<?xml version=\"1.0\" ?>"); // Push protocol onto front of text
				while ((inputLine = instream.readLine()) != null) {
					numrows++;
					textlines.append(inputLine);
					if (numrows <= 10) 
						log.warning(HostInfo.tell()+" SearchRest: bad line returned from XNAT: "+inputLine);
				}
				Reader thesource = new StringReader(textlines.toString());
				log.info(HostInfo.tell()+": Characters read from post result were: "+textlines.length());
				log.info(HostInfo.tell()+": Source XML has "+numrows+" lines of text starting with ["+textlines.toString().substring(0,10)+"]");
				TransX hitme = new TransX();
	            ResourceBundle bdl = new PropertyResourceBundle(new FileInputStream(dnget() + "/system.properties"));
	            String xstyle = bdl.getString("xstyle");
	            BufferedReader stylesheet = new BufferedReader(new FileReader(xstyle));
				p.XMLPage(out, "XNAT Search Results", s,hitme.x2table(thesource,stylesheet));
			}
		} catch (Exception e) {
			log.severe(HostInfo.tell()+" SearchResp error decoding XNAT response.");
			e.printStackTrace();
		}
		finally {
			searchresp.close();
		}
    } 

    /* Function: doPost 
	* 	Overriden procedure that accepts HTTP GET requests from Tomcat Java Servlet framework
	* 	Allows internal users to save new shopping baskets.
    * 
    * Parameters:
    * 	request - HttpServletRequest object carrying attributes of the HTML GET
    * 	response - HttpServletResponse object carrying attributes of the HTML output channel

    */
    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {    
    	
    }
    /* Function: getQuery
     * 	Reads the appropriate XML search query from the FORMS folder and returns it
     * 	as a String
     * 
     * Returns: String
     */
    private String getQuery() throws FileNotFoundException, IOException 
    {
    	ResourceBundle bdl = new PropertyResourceBundle(new FileInputStream(dnget() + "/system.properties"));
        String dir = bdl.getString("xforms");  // Get the value of the xforms key in properties file
    	String restquery = dir + "\\" + xnatquery;  // a file with the query inside the FORMS folder in the distribution
    	StringBuilder results = new StringBuilder();
    	BufferedReader queryfile = null;
    	try {
    		queryfile = new BufferedReader(new FileReader(restquery));
    		String xmlLine;
    		while ((xmlLine = queryfile.readLine()) != null) {
				results.append(xmlLine);
			}    		

    	} 
    	catch (Exception e) {
    		log.severe(HostInfo.tell()+" SearchRest: Error reading XML query file from: "+restquery);
    		e.printStackTrace();
    	}  	
    	return results.toString();
    }


    /* Function: dnget
     * 	Gets local directory name for properties file via the context of the web app.
     * 	Replaces all instances of HTML encoded spaces (%20) with spaces
     * 
     * Returns: String
     * 	Returns everything up to the first / in the URL context
     *
     * See Also:
     * 	<cnget>	
    */
    public String dnget()
    {
       java.net.URL u = this.getClass().getResource(cnget());  
       String dn = u.getPath().replaceAll("%20", " ");  
       return dn.substring(0,dn.lastIndexOf("/"));
    }

    /* Function: cnget	
     * 	Gets local class name for properties file.
     * 
     * Returns: String
     * 	"<ClassName>.class"
     */
    public String cnget()
    {
       String c = this.getClass().getName();
       c = c.substring(c.lastIndexOf(".") + 1,c.length());
       return c+=".class";
    }
     
	
}
