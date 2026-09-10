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
import java.util.Calendar;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.ElementList;

/*
 * About: The XML Schema for Jay Forms
 * 	at the moment any element which doesn't have "required=false"
 * 	will cause the db insert to fail if no value is provided for
 * 	the element in the XML file.  
 */

@Root(name = "Form", strict=false)
public class FormXML {
	
	private final static String EMPTYSTRING = "";
	
	@Element(name = "PrinApp")
	private String prinapp; // varchar(128) Parameter	1
	
	@Element(name = "PrinAppPos")
	private String prinapppos; // varchar(256)			2
	
	@Element(name = "PrinAppInst")
	private String prinappinst; // varchar(256)			3
	
	@Element(name = "PrinAppEmail")
	private String prinappemail; // varchar(256)		4
	
	@Element(name = "CoApp", required=false)
	private String coapp; // varchar(128)				5
	
	@Element(name = "CoAppPos", required=false)
	private String coapppos; // varchar(256)			6
	
	@Element(name = "CoAppInst", required=false)
	private String coappinst; // varchar(256)			7
	
	@Element(name = "CoAppEmail", required=false)
	private String coappemail; // varchar(256)			8
	
	@Element(name = "MoreApps", required=false)
	private String moreapps; // text					9
	
	@Element(name = "Title")
	private String title; // text						10
	
	@Element(name = "StartDate", required=false)
	private String startdate; // date					11
	
	@Element(name = "EndDate", required=false)
	private String enddate; // date					    12
	
	@Element(name = "Funded", required=false)
	private Integer funded; 	// tinyint(1)		    13
	
	@Element(name = "Funder", required=false)
	private String funder; // varchar(256)				14
	
	@Element(name = "ProjDesc", required=false)
	private String projdesc; // text					15
	
	@Element(name = "PubSumry", required=false)
	private String pubsumry; // text					16
	
	@Element(name = "OtherStudies", required=false)
	private Integer otherstudies; 	// tinyint(1)		17
	
	@Element(name = "StudyList", required=false)
	private String studylist; // text					18
	
	@Element(name = "Genotyping", required=false)
	private Integer genotyping; // tinyint(1)			19
	
	@Element(name = "GenoList", required=false)
	private String genolist; // text					20
	
	@Element(name = "DNA", required=false)
	private Integer dna; // tinyint(1)					21
	
	@Element(name = "DNAList", required=false)
	private String dnalist; // text						22
	
	@Element(name = "Samples", required=false)
	private Integer samples; // tinyint(1)				23
	
	@Element(name = "SampleList", required=false)
	private String samplelist; // text					24
	
	@Element(name = "RNA", required=false)
	private Integer rna; // tinyint(1)					25
	
	@Element(name = "RNAList", required=false)
	private String rnalist; // text						26
	
	@Element(name = "FQCExp", required=false)
	private String fqcexp; // text						27
	
	@Element(name = "Insight46", required=false)		
	private String insight46; // tinyint(1)				28
	
	@Element(name = "Insight46List", required=false)	
	private String insight46list; // text				29
	
	@Element(name = "ImagingData", required=false)		
	private String imagingdata; // tinyint(1)			30
	
	@Element(name = "ImagingDataList", required=false)
	private String imagingdatalist; // text				31
	
	@Element(name = "ImgExp", required=false)
	private String imgexp; // text						32
	
	@Element(name = "VarList", required=false)			
	private String varlist; // text						33
	
	@Element(name = "SecMgr", required=false)
	private String secmgr; // varchar(256)				34
	
	@Element(name = "SecPols", required=false)
	private String secpols; // text						35
	
	@Element(name = "DatDenLoc", required=false)
	private String datcenloc; // varchar(256)			36
	
	@Element(name = "AccCtrl", required=false)
	private String accctrl; // text						37
	
	@Element(name = "DataMgr", required=false)
	private String datamgr; // varchar(256)				38
	
	@Element(name = "ContSrc", required=false)
	private Integer contsrc; // smallint(5)				39
	
	@Element(name = "SignName", required=false)
	private String signname; // varchar(256)			40
	
	@Element(name = "DateSub", required=false)
	private String datesub; // date						41
	

	public FormXML() { // Basic Constructor
		this.prinapp = new String();
		this.prinapppos = new String();
		this.prinappinst = new String();
		this.prinappemail = new String();
		this.coapp = new String();
		this.coapppos = new String();
		this.coappinst = new String();
		this.coappemail = new String();
		this.moreapps = new String();
		this.title = new String();
		this.startdate = new String();
		this.enddate = new String();
		this.funded = 0;
		this.funder = new String();
		this.projdesc = new String();
		this.pubsumry = new String();
		this.otherstudies = 0;
		this.studylist = new String();
		this.genotyping = 0;
		this.genolist = new String();
		this.dna = 0;
		this.dnalist = new String();
		this.samples = 0;
		this.samplelist = new String();
		this.rna = 0;
		this.rnalist = new String();
		this.fqcexp = new String();
		this.varlist = new String();
		this.secmgr = new String();
		this.secpols = new String();
		this.datcenloc = new String();
		this.accctrl = new String();
		this.datamgr = new String();
		this.contsrc = 0;
		this.signname = new String();
		this.datesub = new String();
	}

	/**
	 * 1
	 * @return the prinapp
	 */
	String getPrinapp() {
		if (prinapp == null) 
			return EMPTYSTRING;
		else
			return prinapp;
	}

	/**
	 * 1
	 * @param prinapp the prinapp to set
	 */
	void setPrinapp(String prinapp) {
		this.prinapp = prinapp;
	}

	/**
	 * 2
	 * @return the prinapppos
	 */
	String getPrinapppos() {
		if (prinapppos == null)
			return EMPTYSTRING;
		else
			return prinapppos;
	}

	/**
	 * 2
	 * @param prinapppos the prinapppos to set
	 */
	void setPrinapppos(String prinapppos) {
		this.prinapppos = prinapppos;
	}

	/**
	 * 3
	 * @return the prinappinst
	 */
	String getPrinappinst() {
		if (prinappinst == null)
			return EMPTYSTRING;
		else
			return prinappinst;
	}

	/**
	 * 3
	 * @param prinappinst the prinappinst to set
	 */
	void setPrinappinst(String prinappinst) {
		this.prinappinst = prinappinst;
	}

	/**
	 * 4
	 * @return the primappemail
	 */
	String getPrinappemail() {
		if (prinappemail == null)
			return EMPTYSTRING;
		else
			return prinappemail;
	}

	/**
	 * 4
	 * @param primappemail the primappemail to set
	 */
	void setPrinappemail(String primappemail) {
		this.prinappemail = primappemail;
	}

	/**
	 * 5
	 * @return the coapp
	 */
	String getCoapp() {
		if (coapp == null)
			return EMPTYSTRING;
		else
			return coapp;
	}

	/**
	 * 5
	 * @param coapp the coapp to set
	 */
	void setCoapp(String coapp) {
		this.coapp = coapp;
	}

	/**
	 * 6
	 * @ return the coapppos
	 */
	String getCoapppos() {
		if (coapppos == null)
			return EMPTYSTRING;
		else
			return coapppos;
	}

	/**
	 * 6
	 * @param coapppos the coapppos to set
	 */
	void setCoapppos(String coapppos) {
		this.coapppos = coapppos;
	}

	/**
	 * 7
	 * @return the coappinst
	 */
	String getCoappinst() {
		if (coappinst == null)
			return EMPTYSTRING;
		else
			return coappinst;
	}

	/**
	 * 7
	 * @param coappinst the coappinst to set
	 */
	void setCoappinst(String coappinst) {
		this.coappinst = coappinst;
	}

	/**
	 * 8
	 * @return the coappemail
	 */
	String getCoappemail() {
		if (coappemail == null)
			return EMPTYSTRING;
		else
			return coappemail;
	}

	/**
	 * 8
	 * @param coappemail the coappemail to set
	 */
	void setCoappemail(String coappemail) {
		this.coappemail = coappemail;
	}

	/**
	 * 9
	 * @return the moreapps
	 */
	String getMoreapps() {
		if (moreapps == null)
			return EMPTYSTRING;
		else
			return moreapps;
	}

	/**
	 * 9
	 * @param moreapps the moreapps to set
	 */
	void setMoreapps(String moreapps) {
		this.moreapps = moreapps;
	}

	/**
	 * 10
	 * @return the title
	 */
	String getTitle() {
		if (title == null) 
			return EMPTYSTRING;
		else
			return title;
	}

	/**
	 * 10
	 * @param title the title to set
	 */
	void setTitle(String title) {
		this.title = title;
	}

	/**
	 * 11
	 * @return the startdate
	 */
	String getStartdate() {
		if (startdate == null)
			return EMPTYSTRING;
		else
			return startdate;
	}

	/**
	 * 11
	 * @param startdate the startdate to set
	 */
	void setStartdate(String startdate) {
		this.startdate = startdate;
	}

	/**
	 * 12
	 * @return the enddate
	 */
	String getEnddate() {
		if (enddate == null)
			return EMPTYSTRING;
		else
			return enddate;
	}

	/**
	 * 12
	 * @param enddate the enddate to set
	 */
	void setEnddate(String enddate) {
		this.enddate = enddate;
	}

	/**
	 * 13
	 * @return the funded
	 */
	Integer getFunded() {
		return funded;
	}

	/**
	 * 13
	 * @param funded the funded to set
	 */
	void setFunded(Integer funded) {
		this.funded = funded;
	}

	/**
	 * 14
	 * @return the funder
	 */
	String getFunder() {
		if (funder == null)
			return EMPTYSTRING;
		else
			return funder;
	}

	/**
	 * 14
	 * @param funder the funder to set
	 */
	void setFunder(String funder) {
		this.funder = funder;
	}

	/**
	 * 15
	 * @return the projdesc
	 */
	String getProjdesc() {
		if (projdesc == null)
			return projdesc;
		else
			return projdesc;
	}

	/**
	 * 15
	 * @param projdesc the projdesc to set
	 */
	void setProjdesc(String projdesc) {
		this.projdesc = projdesc;
	}

	/**
	 * 16
	 * @return the pubsumry
	 */
	String getPubsumry() {
		if (pubsumry == null)
			return EMPTYSTRING;
		else
			return pubsumry;
	}

	/**
	 * 16
	 * @param pubsumry the pubsumry to set
	 */
	void setPubsumry(String pubsumry) {
		this.pubsumry = pubsumry;
	}

	/**
	 * 17
	 * @return the otherstudies
	 */
	Integer getOtherstudies() {
		return otherstudies;
	}

	/**
	 * 17
	 * @param otherstudies the otherstudies to set
	 */
	void setOtherstudies(Integer otherstudies) {
		this.otherstudies = otherstudies;
	}

	/**
	 * 18
	 * @return the liststudies
	 */
	String getStudyList() {
		if (studylist == null)
			return EMPTYSTRING;
		else
			return studylist;
	}

	/**
	 * 18
	 * @param liststudies the liststudies to set
	 */
	void setStudyList(String liststudies) {
		this.studylist = liststudies;
	}

	/**
	 * 19
	 * @return the genotyping
	 */
	Integer getGenotyping() {
		return genotyping;
	}

	/**
	 * 19
	 * @param genotyping the genotyping to set
	 */
	void setGenotyping(Integer genotyping) {
		this.genotyping = genotyping;
	}

	/**
	 * 20
	 * @return the genolist
	 */
	String getGenolist() {
		if (genolist == null)
			return EMPTYSTRING;
		else
			return genolist;
	}

	/**
	 * 20
	 * @param genolist the genolist to set
	 */
	void setGenolist(String genolist) {
		this.genolist = genolist;
	}

	/**
	 * 21
	 * @return the dna
	 */
	Integer getDna() {
		return dna;
	}

	/**
	 * 21
	 * @param dna the dna to set
	 */
	void setDna(Integer dna) {
		this.dna = dna;
	}

	/**
	 * 22
	 * @return the dnalist
	 */
	String getDnalist() {
		if (dnalist == null)
			return EMPTYSTRING;
		else
			return dnalist;
	}

	/**
	 * 22
	 * @param dnalist the dnalist to set
	 */
	void setDnalist(String dnalist) {
		this.dnalist = dnalist;
	}

	/**
	 * 23
	 * @return the samples
	 */
	Integer getSamples() {
		return samples;
	}

	/**
	 * 23
	 * @param samples the samples to set
	 */
	void setSamples(Integer samples) {
		this.samples = samples;
	}

	/**
	 * 24
	 * @return the samplelist
	 */
	String getSamplelist() {
		if (samplelist == null)
			return EMPTYSTRING;
		else
			return samplelist;
	}

	/**
	 * 24
	 * @param samplelist the samplelist to set
	 */
	void setSamplelist(String samplelist) {
		this.samplelist = samplelist;
	}

	/**
	 * 25
	 * @return the rna
	 */
	Integer getRna() {
		return rna;
	}

	/**
	 * 25
	 * @param rna the rna to set
	 */
	void setRna(Integer rna) {
		this.rna = rna;
	}

	/**
	 * 26
	 * @return the rnalist
	 */
	String getRnalist() {
		if (rnalist == null)
			return EMPTYSTRING;
		else
			return rnalist;
	}

	/**
	 * 26
	 * @param rnalist the rnalist to set
	 */
	void setRnalist(String rnalist) {
		this.rnalist = rnalist;
	}

	/**
	 * 27
	 * @return the fqcexp
	 */
	String getFqcexp() {
		if (fqcexp == null)
			return EMPTYSTRING;
		else
			return fqcexp;
	}

	/**
	 * 27
	 * @param fqcexp the fqcexp to set
	 */
	void setFqcexp(String fqcexp) {
		this.fqcexp = fqcexp;
	}

	/**
	 * 28
	 * @return the Insight46
	 */
	String getInsight46() {
		if (insight46 == null)
			return EMPTYSTRING;
		else
			return insight46;
	}

	/**
	 * 28
	 * @param the Insight46 to set
	 */
	void setInsight46(String insight46) {
		this.insight46 = insight46;
	}
	
	/**
	 * 29
	 * @return the Insight46
	 */
	String getInsight46list() {
		if (insight46list == null)
			return EMPTYSTRING;
		else
			return insight46list;
	}

	/**
	 * 29
	 * @param the Insight46 to set
	 */
	void setInsight46list(String insight46list) {
		this.insight46list = insight46list;
	}
	
	/**
	 * 30
	 * @return the ImagingData
	 */
	String getImagingdata() {
		if (imagingdata == null)
			return EMPTYSTRING;
		else
			return imagingdata;
	}

	/**
	 * 30
	 * @param the ImagingData to set
	 */
	void setImagingdata(String imagingdata) {
		this.imagingdata = imagingdata;
	}
	
	/**
	 * 31
	 * @return the ImagingDataList
	 * 	 
	 */
	String getImagingdatalist() {
		if (imagingdatalist == null)
			return EMPTYSTRING;
		else
			return imagingdatalist;
	}

	/**
	 * 31
	 * @param the Insight46 to set
	 */
	void setImagingdatalist(String imagingdatalist) {
		this.imagingdatalist = imagingdatalist;
	}

	/**
	 * 32
	 * @return the ImgExp
	 */
	String getImgexp() {
		if (imgexp == null)
			return EMPTYSTRING;
		else
			return imgexp;
	}

	/**
	 * 32
	 * @param the ImgExp to set
	 */
	void setImgexp(String imgexp) {
		this.imgexp = imgexp;
	}
	
	
	/**
	 * 33
	 * @return the varlist
	 */
	String getVarlist() {
		if (varlist == null)
			return EMPTYSTRING;
		else
			return varlist;
	}

	
	/**
	 * 33
	 * @param varlist the varlist to set
	 */
	void setVarlist(String varlist) {
		this.varlist = varlist;
	}

	/**
	 * 34
	 * @return the secmgr
	 */
	String getSecmgr() {
		if (secmgr == null)
			return EMPTYSTRING;
		else
			return secmgr;
	}

	/**
	 * 34
	 * @param secmgr the secmgr to set
	 */
	void setSecmgr(String secmgr) {
		this.secmgr = secmgr;
	}

	/**
	 * 35
	 * @return the secpols
	 */
	String getSecpols() {
		if (secpols == null)
			return EMPTYSTRING;
		else
			return secpols;
	}

	/**
	 * 35
	 * @param secpols the secpols to set
	 */
	void setSecpols(String secpols) {
		this.secpols = secpols;
	}

	/**
	 * 36
	 * @return the datcenloc
	 */
	String getDatcenloc() {
		if (datcenloc == null)
			return EMPTYSTRING;
		else
			return datcenloc;
	}

	/**
	 * 36
	 * @param datcenloc the datcenloc to set
	 */
	void setDatcenloc(String datcenloc) {
		this.datcenloc = datcenloc;
	}

	/**
	 * 37
	 * @return the accctrl
	 */
	String getAccctrl() {
		if (accctrl == null)
			return EMPTYSTRING;
		else
			return accctrl;
	}

	/**
	 * 37
	 * @param accctrl the accctrl to set
	 */
	void setAccctrl(String accctrl) {
		this.accctrl = accctrl;
	}

	/**
	 * 38
	 * @return the datamgr
	 */
	String getDatamgr() {
		if (datamgr == null)
			return EMPTYSTRING;
		else
			return datamgr;
	}

	/**
	 * 38
	 * @param datamgr the datamgr to set
	 */
	void setDatamgr(String datamgr) {
		this.datamgr = datamgr;
	}

	/**
	 * 39
	 * @return the contsrc
	 */
	Integer getContsrc() {
		return contsrc;
	}

	/**
	 * 39
	 * @param contsrc the contsrc to set
	 */
	void setContsrc(Integer contsrc) {
		this.contsrc = contsrc;
	}

	/**
	 * 40
	 * @return the signname
	 */
	String getSignname() {
		if (signname == null)
			return EMPTYSTRING;
		else
			return signname;
	}

	/**
	 * 40
	 * @param signname the signname to set
	 */
	void setSignname(String signname) {
		this.signname = signname;
	}

	/**
	 * 41
	 * @return the datesub
	 */
	String getDatesub() {
		if (datesub == null)
			return EMPTYSTRING;
		else
			return datesub;
	}

	/**
	 * 41
	 * @param datesub the datesub to set
	 */
	void setDatesub(String datesub) {
		this.datesub = datesub;
	}
}
