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
import java.util.logging.Logger;

import org.apache.commons.codec.digest.Crypt;
import org.apache.commons.lang3.StringUtils;
/*
 Class: PassStore
 	Create a SHA-2 512 bit hash of a string
 */
public class PassStore {
	// private static final Logger log = Logger.getLogger(HostInfo.tell()+":"+PassStore.class.getName());
	private static final Logger log = Logger.getLogger("mrc.user");

	/*
	 Method: getPass
	 	Takes a candidate password and returns it as a hashed hex string
	 
	 Parameters:
	 	cand - Candidate string to be hashed
	 
	 Returns:
	 	Hex string
	 */
	public static String getPass(String cand) throws Exception
	{
		String hexstring;
		hexstring = Crypt.crypt(cand);
		return hexstring;
		
	}
	
	/*
	 Method: getPass
	 	Takes a candidate password and salt as the second argument 
	 	and computes the hash accordingly
	 Parameters:
	 	cand - Candidate string to be hashed
	 	salt - Salt to be passed to crypt hashing function
	 
	 Returns:
	 	Hex string
	 	
	 */
	public static String getPass(String cand,String salt) throws Exception
	{
		String hexstring;
		hexstring = Crypt.crypt(cand,salt);
		return hexstring;
		
	}

	/*
	 Method: getSaltFromPass
	 	Given a hashed password string, extract the salt part if one is present
	 	
	 Parameter:
	 	pass - Hex password string
	 	
	 Returns:
	 	salt as String
	 	
	 About:
	 	Can throw Exception
	 */
	public static String getSaltFromPass(String pass) throws Exception
	{
		String salt = null;
		String [] results;
		results = StringUtils.split(pass, '$');
		log.fine(" PassStore Class: offered password.");
		/* A null means no match */
		if (results == null) {
			log.warning(" PassStore Class: cannot find salt; split returns null");
			salt = "";
		} else {
			for (int i = 0;i < results.length; i++) {
				log.fine(" PassStore Class: ["+i+"] is "+results[i]);
			}
			if (results.length > 1) { /* to get right hash alg need to prepend magic string */
			salt = "$" + results[0] + "$" + results[1]; /* Recall Java array indexes begin at 0 */
			} else {
				/* There aren't 2 substrings between dollars */
				salt = "";
			}
		}
		return salt;
	}
	
	/*
	 Method: 
	 	Extract the password part of hashed hex string if one is present
	 
	 Parameter:
	 	pass - Hex password string
	 	
	 Returns:
	 	Hashed password as String
	 	
	 About:
	 	Can throw Exception
	 
	 */
	public static String getHashFromPass(String pass) throws Exception
	{
		String thehash = null;
		String [] results;
		results = StringUtils.split(pass, '$');

		log.fine(" PassStore Class: offered password. ");
		/* A null means no match */
		if (results == null) {
			log.warning(" PassStore Class: cannot find salt; split returns null");
			thehash = "";
		} else {
			for (int i = 0;i < results.length; i++) {
				log.fine(" PassStore Class: ["+i+"] is "+results[i]);
			}
			if (results.length > 2) {
			thehash = results[2]; /* Recall Java array indexes begin at 0 */
			} else {
				/* There aren't 3 substrings between dollars */
				thehash = "";
			}
		}
		return thehash;
	}
	
}

