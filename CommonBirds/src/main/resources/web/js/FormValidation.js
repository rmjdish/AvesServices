// SWIFT Form Validation Routines
// Divided into two categories:
// (1) Functions called by forms (public)
// (2) Functions called by other functions (private)

// ******** PUBLIC FUNCTIONS ********

// Choose Dataset to scramble

function validateScramBasketForm(theForm) {
    var reason = "";
	var bsks = theForm.elements['bsks[]'];
	// bsks is an array of check boxes each have the a JSON string as a value

	var cur = 0;
	for (var i = 0, len = bsks.length; i < len; i++) {
		if (bsks[i].checked) { // Checked for scrambling
			reason += validateEmpty(bsks[i], "Basket ID [" + i + "]");
			cur += 1;
		} else { // this basket is not checked 
			bsks[i].value = "NULL";
		}
	}
	// Now we check if cur is zero
	if (cur == 0 || bsks.length == 0) {
		reason += "You must select some baskets to scramble";
	}

	if (reason != "") {
		alert("Dataset(s) could not be scrambled because:\n" + reason);
		return false;
	}
	return true;
}

// Choose Project to link baskets to

function validateProjectChosen(theForm) {
    var reason = "";
    var opt = theForm.project
    var txt = opt.options[opt.selectedIndex].text;
    var val = opt.options[opt.selectedIndex].value;
    if (txt == "ALL") {
	alert("Baskets could not be linked because no project was chosen");
	opt.style.background = 'Red';
	return false;
    } else {
	return true;
    }
}


// Link Baskets to a Project for Jay

function validateLinkBasketForm(theForm) {
	var reason = "";
	var bskts = theForm.elements['baskets[]'];

	// bskts is an array of text values having the basket id as a value

	var cur = 0;
	for (var i = 0, len = bskts.length; i < len; i++) {
		if (bskts[i].checked) { // Checked for linking
			reason += validateEmpty(bskts[i], "Basket ID [" + i + "]");
			cur += 1;
		} else { // fids is not checked
			
		}
	}
	// Now we check if cur is zero
	if (cur == 0) {
		reason += "You must select some baskets to link";
	}
	if (reason != "") {
		alert("Project(s) could not be updated because:\n" + reason);
		return false;
	}
	return true;

}

// Approve Form for Jay

function validateScramUserForm(theForm) {
	var reason = "";
	var pids = theForm.elements['pids[]'];

	// pids is an array of text values having the project id as a value

	var cur = 0;
	for (var i = 0, len = pids.length; i < len; i++) {
		reason += validateEmpty(pids[i], "Project PID [" + i + "]");
		cur += 1;
	}

	if (reason != "") {
		alert("Project(s) could not be updated because:\n" + reason);
		return false;
	}
	return true;
}

function validateUpdateProjectsForm(theForm) {
	var reason = "";
	var pids = theForm.elements['projects[]'];
	var cods = theForm.elements['codes[]'];
	// projects is an array of text values having the project id as a value
	// codes is an array of text values containing the attribute code as a value - can be negative
	var cur = 0;
	for (var i = 0, len = cods.length; i < len; i++) {
		reason += validateEmpty(pids[i], "Project PID [" + i + "]");
		reason += validateEmpty(cods[i], "Attribute Code [" + i + "]");
		cur += 1;
	}

	if (reason != "") {
		alert("Project(s) could not be updated because:\n" + reason);
		return false;
	}
	return true;
}

function validateApproveForms(theForm) {
	var reason = "";
	var pids = theForm.elements['pids[]'];
	var fids = theForm.elements['fids[]'];
	// pids is an array of string names for project ids, possibly with empty elements
	// fids is an array of checkboxes each having the form id as a value
	var cur = 0;
	for (var i = 0, len = fids.length; i < len; i++) {
		if (fids[i].checked) { // Checked for approval
			reason += validateEmpty(pids[i], "Project PID [" + i + "]");
			cur += 1;
		} else { // fids is not checked
			pids[i].value = "NULL";
		}
	}

	if (reason != "") {
		alert("Your form approval could not be processed because:\n" + reason);
		return false;
	}
	return true;
}

function validatePublishProjectsForm(theForm) {
	var reason = "";
	var pids = theForm.elements['projects[]'];
	// projects is an array of checkboxes having the project id as a value

	var cur = 0;
	for (var i = 0, len = pids.length; i < len; i++) {
		if (pids[i].checked) { // Checked for publication
			reason += validateEmpty(pids[i], "Project PID [" + i + "]");
			cur += 1;
		} else { // fids is not checked
			pids[i].value = "NULL";
		}
	}

	if (reason != "") {
		alert("Project(s) could not be published because:\n" + reason);
		return false;
	}
	return true;
}

// registration.java
function validateRegistrationForm(theForm) {
	var reason = "";

	reason += validateEmpty(theForm.firstName, "First Name");
	reason += validateEmpty(theForm.lastName, "Last Name");
	reason += validateEmpty(theForm.userName, "Username");
	reason += validateEmpty(theForm.desiredPassword1, "Desired Password");
	reason += validateEmpty(theForm.desiredPassword2, "Confirm Password");
	reason += validateEmpty(theForm.academicAffiliation, "Academic Affiliation");
	reason += validateEmpty(theForm.address1, "Address 1");
	reason += validateEmpty(theForm.city, "City");
	reason += validateEmpty(theForm.postcode, "Postcode");
	reason += verifyPostcode(theForm.postcode);
	reason += validateEmpty(theForm.phone, "Phone");
	reason += validateEmpty(theForm.email, "Email");
	reason += verifyEmailAddress(theForm.email);

	if (reason != "") {
		alert("Your registration could not be processed because:\n" + reason);
		return false;
	}
	return true;
}

// emailEnquiry.java
function validateEnquiryForm(theForm) {
	var reason = "";

	reason += validateEmpty(theForm.name, "Name");
	reason += validateEmpty(theForm.subject, "Subject");
	reason += validateEmpty(theForm.email, "Email");
	reason += validateEmpty(theForm.body, "Message");
	reason += verifyEmailAddress(theForm.email);

	if (reason != "") {
		alert("Your registration could not be processed because:\n" + reason);
		return false;
	}
	return true;
}

// Level 2 Access
// login.java
function validateLoginForm(theForm) {
	var reason = "";

	reason += validateEmpty(theForm.username, "Username");
	reason += validateEmpty(theForm.password, "Password");
	if (reason != "") {
		alert("Your login could not be processed because:\n" + reason);
		return false;
	}
	return true;
}

// search.java - variable name
function validateSearchForm(theForm) {
	var reason = "";

	reason += validateEmpty(theForm.term, "Keyword");
	reason += validateContains(theForm.term, " ", "Keyword");

	if (reason != "") {
		alert("Your search could not be started because:\n" + reason);
		return false;
	}
	return true;
}

// changePassword.java and resetPassword.java
function validatePasswordForm(theForm) {
	var reason = "";

	reason += validateEmpty(theForm.password1, "New password");
	reason += validateEmpty(theForm.password2, "Password confirmation");
	reason += validateSame(theForm.password1, theForm.password2);

	if (reason != "") {
		alert("Your login could not be processed because:\n" + reason);
		return false;
	}
	return true;
}

// sharedBasket.java
function validateSharedBasketForm(theForm) {
	var reason = "";

	reason += validateEmpty(theForm.desc, "Description");

	if (reason != "") {
		alert("Your basket could not be shared because:\n" + reason);
		return false;
	}
	return true;
}

// changeRegistration.java
function validateChangeRegistrationForm(theForm) {
	var reason = "";

	reason += validateEmpty(theForm.firstName, "First Name");
	reason += validateEmpty(theForm.lastName, "Last Name");
	reason += validateEmpty(theForm.academicAffiliation, "Academic Affiliation");
	reason += validateEmpty(theForm.address1, "Address 1");
	reason += validateEmpty(theForm.city, "City");
	reason += validateEmpty(theForm.postcode, "Postcode");
	reason += verifyPostcode(theForm.postcode);
	reason += validateEmpty(theForm.phone, "Phone");
	reason += validateEmpty(theForm.email, "Email");
	reason += verifyEmailAddress(theForm.email);

	if (reason != "") {
		alert("Your registration could not be processed because:\n" + reason);
		return false;
	}
	return true;
}

function validateProposalForm(theForm) {
	var reason = "";

	reason += validateEmpty(theForm.PrinApp, "Principal Applicant")
	reason += validateEmpty(theForm.Title, "Project Title");
	reason += validateEmpty(theForm.StartDate, "Start Date");
	reason += validateEmpty(theForm.EndDate, "End Date");
	reason += validateEmpty(theForm.ProjDesc, "Project Description");
	reason += validateEmpty(theForm.PubSumry, "Public Summary");
	reason += validateEmpty(theForm.SignName, "Form Signatory");
	reason += validateEmpty(theForm.DateSub, "Date Submitted");
	reason += validateEmpty(theForm.SecMgr,
			"Responsible person for information security");
	reason += validateEmpty(theForm.SecPols, "Organisation security policies");
	reason += validateEmpty(theForm.DatCenLoc, "Location of data centre");
	reason += validateEmpty(theForm.AccCtrl, "How will access be controlled");
	reason += validateEmpty(theForm.DataMgr, "Data Manager")
	if (reason != "") {
		alert("Your proposal could not be processed because:\n" + reason);
		return false;
	}
	return true;
}

// Level 3 Access
// basket.java
function validateCheckoutForm(theForm) {
	var reason = "";

	reason += validateEmpty(theForm.description, "Description");

	if (reason != "") {
		alert("Your basket could not be saved because:\n" + reason);
		return false;
	}
	return true;
}

function validateChooseCSVFileForm(theForm) {
	var reason = "";

	reason += validateEmpty(theForm.projectform, "File To Upload");

	if (reason != "") {
		alert("Your request to upload a file could not be attempted because:\n"
				+ reason);
		return false;
	}
	return true;
}

function validateChooseXMLFileForm(theForm) {
	var reason = "";

	reason += validateEmpty(theForm.file, "File To Upload");

	if (reason != "") {
		alert("Your request to upload a file could not be attempted because:\n"
				+ reason);
		return false;
	}
	return true;
}

// approveLevelThree.java
function validateReviewForm(theForm) {
	var reason = "";

	reason += validateEmpty(theForm.comments, "Comments");

	if (reason != "") {
		alert("Your review could not be saved because:\n" + reason);
		return false;
	}
	return true;
}

// ******** PRIVATE FUNCTIONS ********

// Check if current character is number or not
function isInteger(s) {
	var i;

	for (i = 0; i < s.length; i++) {
		var c = s.charAt(i);
		if (((c < "0") || (c > "9")))
			return false;
	}
	return true;
}

// Search through string's characters one by one; If character is not in bag, append to returnString
function stripCharsInBag(s, bag) {
	var i;
	var returnString = "";

	for (i = 0; i < s.length; i++) {
		var c = s.charAt(i);
		if (bag.indexOf(c) == -1)
			returnString += c;
	}
	return returnString;
}

// February has 29 days in any year evenly divisible by four, EXCEPT for centurial years which are not also divisible by 400
function daysInFebruary(year) {
	return (((year % 4 == 0) && ((!(year % 100 == 0)) || (year % 400 == 0))) ? 29
			: 28);
}

function DaysArray(n) {
	for (var i = 1; i <= n; i++) {
		this[i] = 31
		if (i == 4 || i == 6 || i == 9 || i == 11) {
			this[i] = 30
		}
		if (i == 2) {
			this[i] = 29
		}
	}
	return this
}

// Date validation
function isDate(dtStr, epoch) {
	var dtCh = "/";
	var minYear = 1900;
	var maxYear = 2100;
	var rsn = "";
	var daysInMonth = DaysArray(12)
	var pos1 = dtStr.indexOf(dtCh)
	var pos2 = dtStr.indexOf(dtCh, pos1 + 1)
	var strMonth = dtStr.substring(0, pos1)
	var strDay = dtStr.substring(pos1 + 1, pos2)
	var strYear = dtStr.substring(pos2 + 1)
	strYr = strYear
	if (strDay.charAt(0) == "0" && strDay.length > 1)
		strDay = strDay.substring(1)
	if (strMonth.charAt(0) == "0" && strMonth.length > 1)
		strMonth = strMonth.substring(1)
	for (var i = 1; i <= 3; i++) {
		if (strYr.charAt(0) == "0" && strYr.length > 1)
			strYr = strYr.substring(1)
	}
	month = parseInt(strMonth)
	day = parseInt(strDay)
	year = parseInt(strYr)
	if (pos1 == -1 || pos2 == -1) {
		rsn += "The date format should be : mm/dd/yyyy for the " + epoch + "\n";
	}
	if (strMonth.length < 1 || month < 1 || month > 12) {
		rsn += "An invalid month was entered for the " + epoch + "\n";
	}
	if (strDay.length < 1 || day < 1 || day > 31
			|| (month == 2 && day > daysInFebruary(year))
			|| day > daysInMonth[month]) {
		rsn += "An invalid day was entered for the " + epoch + "\n";
	}
	if (strYear.length != 4 || year == 0 || year < minYear || year > maxYear) {
		rsn += "An invalid year was entered (must be between " + minYear
				+ " and " + maxYear + ") for the " + epoch + "\n";
	}
	if (dtStr.indexOf(dtCh, pos2 + 1) != -1
			|| isInteger(stripCharsInBag(dtStr, dtCh)) == false) {
		rsn += "Please enter a valid date for the " + epoch + "\n";
	}
	return rsn;
}

// All date checks for applications
function validateDate(theForm) {
	var reason = "";
	var start = theForm.startMonth.value + "/" + theForm.startDay.value + "/"
			+ theForm.startYear.value;
	var startReason = isDate(start, "start date");
	var end = theForm.endMonth.value + "/" + theForm.endDay.value + "/"
			+ theForm.endYear.value;
	var endReason = isDate(end, "end date");

	d1 = new Date(theForm.startYear.value, theForm.startMonth.value,
			theForm.startDay.value);
	d2 = new Date(theForm.endYear.value, theForm.endMonth.value,
			theForm.endDay.value);
	if (d1 > d2)
		reason += "Start date must precede end date";
	reason += startReason + endReason;

	if (reason != "") {
		alert("Your login could not be processed because:\n" + reason);
		return false;
	}
	return true;
}

//check postcode format is valid
function verifyPostcode(fld) {
	var reason = "";
	test = fld.value;
	size = test.length;
	test = test.toUpperCase(); //Change to uppercase
	while (test.slice(0, 1) == " ") //Strip leading spaces
	{
		test = test.substr(1, size - 1);
		size = test.length
	}
	while (test.slice(size - 1, size) == " ") //Strip trailing spaces
	{
		test = test.substr(0, size - 1);
		size = test.length
	}
	if (size < 4 || size > 15) { //Code length rule
		reason += test + " is not a valid postcode - wrong length\n";
	}
	return reason;
}

function validateEmpty(fld, errorTxt) {
	var error = "";

	if (fld.value.length == 0) {
		fld.style.background = 'Red';
		error = "The " + errorTxt + " field is empty.\n"
	} else {
		fld.style.background = 'White';
	}
	return error;
}

function validateContains(fld, character, errorTxt) {
	var error = "";

	if (fld.value == character) {
		fld.style.background = 'Red';
		error = "The " + errorTxt + " field contains an invalid character.\n"
	} else {
		fld.style.background = 'White';
	}
	return error;
}

function validateSame(fld1, fld2) {
	var error = "";

	if (fld1.value == fld2.value) {
		fld1.style.background = 'White';
		fld2.style.background = 'White';
	} else {
		fld1.style.background = 'Red';
		fld2.style.background = 'Red';
		error = "The passwords you entered do not match.\n"
	}
	return error;
}

function verifyEmailAddress(fld) {
	var error = "";
	var str = fld.value;
	var filter = /^.+@.+\..{2,3}$/

	if (!filter.test(str))
		error = "Please input a valid email address!";

	return error;
}