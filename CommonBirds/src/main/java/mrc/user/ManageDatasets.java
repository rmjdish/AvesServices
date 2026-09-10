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

public class ManageDatasets extends FileDownload {
	
    private String availableQuery() {
    	String query="select t1.basketid, t1.status, t2.description, t2.createDate "+
                "from uploaddownload as t1 INNER JOIN basketdetails as t2 on t1.basketID=t2.basketID "+
                " order by t2.createDate";
    	return query;
    }


}
