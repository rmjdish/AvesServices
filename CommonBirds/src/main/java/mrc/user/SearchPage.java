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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import mrc.util.Page;

/**
 * Lists all possible Search types
 * @author LHA SST
 */

public class SearchPage extends HttpServlet 
{   
    /**
	 * 
	 */
	private static final long serialVersionUID = -8119429953551388189L;
	// private static final Logger log = Logger.getLogger(HostInfo.tell()+":"+SessMgr.class.getName());
	private static final Logger log = Logger.getLogger("mrc.user");

	/** 
    * Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();        
        HttpSession s = request.getSession();
        if (s == null || s.getAttribute("username") == null) {
        	request.getRequestDispatcher("/login").forward(request, response);
        }        
        Page p=new Page("SearchPage-searchForm");
        log.fine("Condor SearchPage Servlet: In searchForm creating Page object for "+p.getTemplate());
        p.UserPage(out,"NSHD Metadata Search",s);
        // Change to Pebble Template
    } 

    /** 
    * Returns a short description of the servlet.
    */
    @Override
	public String getServletInfo() 
    {
        return "This is the top-level servlet for searching.";
    }
}
