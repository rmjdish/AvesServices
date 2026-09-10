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

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.CharBuffer;
import java.util.List;
import java.util.logging.Logger;

/*
 * Class: TransX
 * 	Utility class that applies XSLT transform to XML source files
 * 	and (hopefully) returns resulting text
 */
public class TransX {
	private static final Logger log = Logger.getLogger("mrc.user");
	private static String XMLFROMXNAT;  

	/*
	 * Variable: transform
	 * 	Basic transformer engine
	 */
	private Transformer morph;
	/*
	 * Variable: factory
	 * 	TransformerFactory for creation of transformer
	 */
	private TransformerFactory factory;
	/*
	 * Constructor: Public basic constructor
	 */
	public TransX() {
		this.factory = TransformerFactory.newInstance();
		Properties pfile = new Properties(Util.dnget(this)+"system.properties");
		XMLFROMXNAT = pfile.keySearch("xstyle");
	}
	
	/* About: Overloaded process methods
	 * 	Transform an XML and XSL document as <code>Reader</code>s, placing the
	 * 	resulting transformed document in a <code>Writer</code>. Convenient for
	 * 	handling an XML document as a String (<code>StringReader</code>) residing in
	 * 	memory, not on disk. The output document could easily be handled as a String
	 * 	(<code>StringWriter</code>) or as a <code>JSPWriter</code> in a JavaServer
	 * 	page.
	 */

	public void process(Reader xmlFile, Reader xslFile, Writer output) throws TransformerException {
		process(new StreamSource(xmlFile), new StreamSource(xslFile), new StreamResult(output));
	}

	/* Function: process
	 * 	Transform an XML and XSL document as <code>File</code>s, placing the
	 * 	resulting transformed document in a <code>Writer</code>. The output document
	 * 	could easily be handled as a String (<code>StringWriter</code)> or as 
	 *  a <code>JSPWriter</code> in a JavaServer page.
	 *  
	 *  Parameters:
	 *  	xmlFile - XML source to be transformed 
	 *  	xslFile - XSLT stylesheet specifying the transformation
	 *  	output - Result of transformation
	 */
	public void process(File xmlFile, File xslFile, Writer output) throws TransformerException {
		process(new StreamSource(xmlFile), new StreamSource(xslFile), new StreamResult(output));
	}

	/* Function: process
	 * 	Transform an XML <code>File</code> based on an XSL <code>File</code>, placing
	 * 	the resulting transformed document in a <code>OutputStream</code>. Convenient
	 * 	for handling the result as a <code>FileOutputStream</code> or
	 * 	<code>ByteArrayOutputStream</code>.
	 *  
	 *  Parameters:
	 *  	xmlFile - XML source to be transformed 
	 *  	xslFile - XSLT stylesheet specifying the transformation
	 *  	output - Result of transformation
	 */

	public void process(File xmlFile, File xslFile, OutputStream out) throws TransformerException {
		process(new StreamSource(xmlFile), new StreamSource(xslFile), new StreamResult(out));
	}

	/* Function: process
	 * Transform an XML source using XSLT based on a new template for the source XSL
	 * document. The resulting transformed document is placed in the passed in
	 * <code>Result</code> object.
	 *  
	 *  Parameters:
	 *  	xmlFile - XML source to be transformed 
	 *  	xslFile - XSLT stylesheet specifying the transformation
	 *  	output - Result of transformation
	 */

	public void process(Source xml, Source xsl, Result result) throws TransformerException {
		try {
			Templates template = factory.newTemplates(xsl);
			Transformer transformer = template.newTransformer();
			transformer.transform(xml, result);
		} catch (TransformerConfigurationException tce) {
			throw new TransformerException(tce.getMessageAndLocation());
		} catch (TransformerException te) {
			throw new TransformerException(te.getMessageAndLocation());
		}
	}
	

	/*
	 * Function: x2table
	 * 	This method takes a source XML in the form of a string
	 * 	and uses process to do the actual work.
	 * 	
	 */
	public String x2table(Reader thesourcexml, Reader thestylesheet)  {
		StringWriter theoutput = new StringWriter();
		try {
			this.process(thesourcexml,thestylesheet,theoutput);			
		} catch (Exception te) {
			log.severe(HostInfo.tell()+" x2table: Error with call to transform");
			this.what(thesourcexml, thestylesheet);
			te.printStackTrace();
		}
		log.info(HostInfo.tell()+" TransX: x2table: returning transformed XML as string of length: "+theoutput.toString().length());
		return theoutput.toString();
	}
	
	private void what(Reader thesourcexml, Reader thestylesheet) {
		final int peek = 10;
		CharBuffer bf1 = CharBuffer.allocate(peek+1);
		CharBuffer bf2 = CharBuffer.allocate(peek+1);
		try {
			thesourcexml.reset();
			thestylesheet.reset();
			thesourcexml.read(bf1);
			thestylesheet.read(bf2);
			log.severe(HostInfo.tell()+": TransX: what: the first chars of the source xml:"+bf1.toString());
			log.severe(HostInfo.tell()+": TransX: what: the first chars of the stylesheet:"+bf2.toString());			
		} catch (IOException io) {
			log.severe(HostInfo.tell()+": TransX: what: Can't reset source XML or stylesheet.");
		}
	}
	
}
