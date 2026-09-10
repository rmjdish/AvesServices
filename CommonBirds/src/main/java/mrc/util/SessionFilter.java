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

import java.io.IOException;
import java.util.logging.Logger;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/* Class: SessionFilter
 * 	Checks the session is valid and redirects to Login if not
 * 
*/
@WebFilter("/SessionFilter")
public class SessionFilter implements Filter {

	private static final Logger log = Logger.getLogger("mrc.user");
	
    /*Constructor: SessionFilter()
     *  Default constructor. 
     */
    public SessionFilter() {
        // No special behaviour here
    }

	/* Function: destroy
	 * 	Called by container (Tomcat) when app shutdown
	 * 
	*/
	public void destroy() {
		log.fine(HostInfo.tell()+" SessionFilter: destroyed");
	}

	/*Function: doFilter
	 * 	Called when specified by XML in web.xml.  Note that it's called
	 * 	with ServletRequest etc. not HttpServletRequest
	 * 
	 * Parameters:
	 * 	request - ServletRequest
	 * 	response - ServletResponse
	 * 	chain - FilterChain
	 * 
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest  req = (HttpServletRequest)  request;
		HttpServletResponse res = (HttpServletResponse) response;
		/* 
		About: Implementation
			The following code implements the test for validity and redirection 
			
		--- Java
		HttpSession s = req.getSession(false);
		if (s == null) { // Session is invalid if null
			log.severe(HostInfo.tell()+" SessionFilter: Invalid session redirecting to login.");
			String loginURL = req.getContextPath() + "/login";
			res.sendRedirect(loginURL);
		---
		 */
		HttpSession s = req.getSession(false);
		if (s == null) { // Session is invalid if null
			log.severe(HostInfo.tell()+" SessionFilter: Invalid session redirecting to login.");
			String loginURL = req.getContextPath() + "/login";
			res.sendRedirect(loginURL);
		}
		else {
			log.fine(HostInfo.tell()+" SessionFilter: valid session.");
			// pass the request along the filter chain
			chain.doFilter(request, response);
		}
	}
	/* Function: init
	 * 	Called by container (Tomcat) on app start up
	 *
	 * Parameters:
	 * 	fConfig - FilterConfig
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		log.fine(HostInfo.tell()+" SessionFilter: initialised");
	}

}
