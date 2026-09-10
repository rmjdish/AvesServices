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

import java.util.logging.Logger;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

@WebListener 
public class SessMgr implements HttpSessionListener {

	//private static final Logger log = Logger.getLogger(HostInfo.tell() + ":" + SessMgr.class.getName());
	private static final Logger log = Logger.getLogger("mrc.util");
	private static final String hostname = HostInfo.chezmoi();
	
	@Override
	public void sessionCreated(HttpSessionEvent se) {
		// TODO Auto-generated method stub
		HttpSession session = se.getSession();
        String sessid=session.getId();
        log.fine("SessMgr: Session Created for user: "+sessid);
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {

		HttpSession session = se.getSession();
		String sessid = session.getId();
        String username=(String)session.getAttribute("username");
		log.fine(HostInfo.tell()+" SessMgr: Session Destroyed for user: "+username+" Session Id: "+sessid);
		try {
			// ConnectionManager.purge();
		} catch (Exception e) {
			log.severe(HostInfo.tell()+" sessionDestroyed: Error purging DB connection pool");
			e.printStackTrace();
		}
	}
	
	public final String getMyName() {
		return hostname;
	}
	
		
}


