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


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;




public final class BotCheck {
	/*
	 * Class: BotCheck
	 * 	Communicates with Google recaptcha system for LHA servers in hosts and
	 * 	checks that Google is satisfied that the web page interaction is with a 
	 * 	human(ish) being.
	 */
	private static final Logger log = Logger.getLogger("mrc.user");
	public static final String url = "https://www.google.com/recaptcha/api/siteverify";
	public static final String skylarksecret = "6LcTunkUAAAAAOrMcI5u0AvfjirH1tqpkmWrpSbE";
	public static final String kiwidevsecret = "6LeBf3kUAAAAAMVpf2q_dNLkoaT7FVZTh8U6RIhU";
	public static final String kiwisecret    = "6LcohasUAAAAAGugmTo2vI6DxRjc-ZCcLV-za7qb";
	private final static String USER_AGENT = "Mozilla/5.0";
	private static List<String> hosts = new ArrayList<String>();
	/*
	 * Function: verifyCapcha
	 * 	For those servers it knows about, it sends parameters to G recaptcha including the 
	 * 	correct secret key.  It then reads a response which comes back as an ascii JSON
	 * 	string.  Parses it and returns the boolean component "success".
	 * 
	 * Returns:
	 * 	True if unknown host or G recapcha approves
	 * 	False otherwise
	 */	
	public static boolean verifyCapcha(String gRecaptchaResponse) throws IOException {
		// hosts.add("kiwidev");
		// hosts.add("kiwi");
		hosts.add("finch");
		// If you don't know about this host just pass the test
		if ( ! hosts.contains(HostInfo.chezmoi()) )
			return true;
		if (gRecaptchaResponse == null || "".equals(gRecaptchaResponse)) {
			log.warning(HostInfo.tell()+" verifyCapcha: No response returned from Google reCAPTCHA.");
			return false;
		}

		try {
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			// add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", "en-GB,en;q=0.5");
			String postParams;
			// make sure to use the appropriate secretkey
			if (HostInfo.chezmoi().equalsIgnoreCase("finch")) {
				postParams = "secret=" + skylarksecret + "&response=" + gRecaptchaResponse;				
			} else if (HostInfo.chezmoi().equalsIgnoreCase("kiwidev")) {
				postParams = "secret=" + kiwidevsecret + "&response=" + gRecaptchaResponse;
			} else if (HostInfo.chezmoi().equalsIgnoreCase("kiwi")) {
				postParams = "secret=" + kiwisecret + "&response=" + gRecaptchaResponse;
			} else { // Server I don't know anything about
				return false;
			}


			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postParams);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			log.fine(HostInfo.tell() + "reCAPCHA: Sending 'POST' request to URL : " + url);
			log.fine(HostInfo.tell() + "reCAPCHA: Post parameters : " + postParams);
			log.fine(HostInfo.tell() + "reCAPCHA: Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			log.fine(HostInfo.tell() + " verifyCapcha: " + response.toString());

			// parse JSON response and return 'success' value
			JsonReader jsonReader = Json.createReader(new StringReader(response.toString()));
			JsonObject jsonObject = jsonReader.readObject();
			jsonReader.close();

			return jsonObject.getBoolean("success");
		} catch (Exception e) {
			log.severe(HostInfo.tell()+" BotCheck: verifyCapcha: Error.");
			e.printStackTrace();
			return false;
		}
	}
	
	

}
