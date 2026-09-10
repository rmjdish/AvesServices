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

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.ws.RequestWrapper;
import jakarta.xml.ws.ResponseWrapper;

import jakarta.activation.DataHandler;

/*
 * This is the CXF2 Service Endpoint Definition
 * The claim is that CXF implements the JAX-WS APIs to make building web services easy.
 * 
 * 2 methods are defined in this WS: throwjob: (String,String) -> NULL
 * 									 catchjob: String -> DataHandler
 * 
 * Defining the WS with 2 methods leaves open the possibility of just asking for
 * previously build ZIP files without having to generate them
 * Note that JAX-WS adds a RequestWrapper and ResponseWrapper automatically when using
 * the Eclipse built in WS wizard.  These handle the communication between Tomcat like
 * services and the WS
 */

/*
 * Class: ZIPService
 *	 Defines an Apache CXF2 web service that defines three methods: *throwjob*, *lobjob*, and *catchjob*
 *	 See the Apache site here: http://cxf.apache.org/
 *	 This web service expects Python programs to be available to run on the remote host
 *
 * See Also:
 * 	 <BasketBuilder>
 */
@WebService(targetNamespace="http://zip.service.ucl/")
public interface ZIPService {

	@RequestWrapper(className = "ucl.service.zip.jaxws.Throwjob", localName = "throwjob", targetNamespace = "http://zip.service.ucl/")
	@ResponseWrapper(className = "ucl.service.zip.jaxws.ThrowjobResponse", localName = "throwjobResponse", targetNamespace = "http://zip.service.ucl/")
	@WebResult(name = "return")
	@WebMethod
    public void throwjob( @WebParam(name = "basket") String basket, @WebParam(name = "username") String username); // Must be implemented in Boomerang.java
	
	@RequestWrapper(className = "ucl.service.zip.jaxws.Lobjob", localName = "lobjob", targetNamespace = "http://zip.service.ucl/")
	@ResponseWrapper(className = "ucl.service.zip.jaxws.LobjobResponse", localName = "lobjobResponse", targetNamespace = "http://zip.service.ucl/")
	@WebResult(name = "return")
	@WebMethod 
	public void lobjob( @WebParam(name = "basket") String basket, @WebParam(name = "jobname") String jobname, @WebParam(name = "parmlist") String parmlist); // Must be implemented in Boomerang.java

	@RequestWrapper(className = "ucl.service.zip.jaxws.Catchjob", localName = "catchjob", targetNamespace = "http://zip.service.ucl/")
	@ResponseWrapper(className = "ucl.service.zip.jaxws.CatchjobResponse", localName = "catchjobResponse", targetNamespace = "http://zip.service.ucl/")
	@WebResult(name = "return")
	@WebMethod
	public DataHandler catchjob(@WebParam(name = "basket") String basket, @WebParam(name = "type") String type); // Must be implemented in Boomerang.java
	
}

