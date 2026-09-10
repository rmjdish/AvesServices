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

import java.util.List;
import java.util.Date;
import java.util.ArrayList;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.ElementList;


@Root(name="Basket")
public class BasketXML {
	
	@Element(name="basketId")
	private String basketId;
	
	@Attribute(name="approved")
	private Integer approved;
	
	@Element(name="username")
	private String username;
	
	@Element(name="description")
	private String description;
	
	@Element(name="createdate")
	private Date createdate;
	
	@Element(name="projectId")
	private String projectId;
	
	@ElementList(inline=true)
	private List<Var> variables;
	
	public BasketXML (String basket) { // Basic Constructor
		this.basketId = new String();
		this.approved = 0;
		this.username = new String();
		this.description = new String();
		this.createdate = new Date();
		this.projectId = new String();
		this.variables = new ArrayList<Var>();
		if (! (basket == null)) {
			this.basketId = basket;
		}
	}
	
	public String getBasketId() {
		return this.basketId;
	}
	
	public void setUserName(String name) {
		if (! (name == null)) {
			this.username = name;
		}
	}
	
	public String getUserName() {
		return this.username;
	}
	
	public void setDescription(String descrip) {
		if (! (descrip == null)) {
			this.description = descrip;
		}
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setDate(Date when) {
		if (! (when == null)) {
			this.createdate = when;
		}
	}
	
	public Date getDate() {
		return this.createdate;
	}
	
	public void setProjectId (String pid) {
		if (! (pid == null)) {
			this.projectId = pid;
		}
	}
	
	public String getProjectId() {
		return this.projectId;
	}
	
	public void addVar(String varname) {
		if (! (varname == null)) {
			BasketXML.Var avar = new Var(varname);
			this.variables.add(avar);
		}
	}
	
	public void addVar(String varname, String ver) {
		if (! (varname == null)) {
			BasketXML.Var avar = new Var(varname);
			avar.setVer(ver);
			if (! (ver == null)) {
				avar.setVer(ver);
			}
			this.variables.add(avar);
		}
	}
	
	
	@Root(name="Variable")
	static class Var {
		
		@Element(name="name")
		private String variable;
		
		@Attribute(name="version")
		private String version;
		

		public Var(String name) {
			if (! (name == null)) {
				this.variable = name;
				this.setVer("1.0");
			}
		}
		

		public void setVer (String ver) {
			if (! (ver == null)) {
				this.version = ver;
			}
		}
		

		public String getVer() {
			return this.version;
		}
	}
}
