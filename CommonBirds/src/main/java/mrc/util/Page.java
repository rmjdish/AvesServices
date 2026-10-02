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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

/*	Class: Page
 * 		Provides a template for all HTML pages using the 
 * 		Java Pebble templating system. 
 *
 */
public class Page 
{ 
    private static final Logger log = Logger.getLogger("mrc.user");

    /*
     * About:  Page Class
     * This is the place where all the "views" of the system are added as
     * methods to this class.  So, each java class in mrc.user creates a model
     * that accesses the views by invoking an instance of this Page class.  Pebble templates
     * are further added to the templates folder in the source bundle 
     * to allow easy changes to the appearance of
     * each view.  They use a format close to the Python Jinja templating system     
     */
    
    /* Properties: Instance Variables
     * template - Instance variable used to store the name of the Pebble html template file.
     * results - Instance variable used to store a list (String ArrayList) of elements to be 
     *           printed by the template
     * numCols - Instance variable used by template to decide when end of current row occurs
     * numResults - Instance variable used by template to obtain the length of the <results> list
     */
    private String template = "Missing-Template";
    private List<String> results = new ArrayList<String>();
    private List<String> headers = new ArrayList<String>();
    private int numCols = 0;
    private int numResults = 0;
    
    // Static template base path - kept for backward compatibility but ignored by ServletLoader
    private static String templateBasePath = null;

    /*
     * Constructor: Page
     *  Generally not used.
     */
    public Page () {
        super();
    }
    
    /*
     * Constructor: Page
     *  Called with the file name of a template to be used to generate HTML output
     * Parameter: atemp
     *  The name of the HTML file used by the Page instance
     */
    public Page(String atemp) {
        // Object created with specific template
        if (StringUtils.isNotBlank(atemp)) {
            this.template = atemp;
        }
    }
    
    /**
     * Constructor with template base path
     * @param atemp Template name
     * @param templatePath Base directory path for templates (Ignored by ServletLoader, kept for compatibility)
     */
    public Page(String atemp, String templatePath) {
        if (StringUtils.isNotBlank(atemp)) {
            this.template = atemp;
        }
        if (StringUtils.isNotBlank(templatePath)) {
            Page.templateBasePath = templatePath;
        }
    }
    
    /*
     * Function: chgTemplate
     *  Updates the template property of a Page object
     * Parameter: atemp
     *  The name of the HTML file used by the Page instance 
     */
    public void chgTemplate(String atemp) {
        // Update template
        if (StringUtils.isNotBlank(atemp)) {
            this.template = atemp;
        }
    }
    
    /*
     * Function: getTemplate
     *  Returns the current template file name
     * 
     * Returns: 
     *  String this.template
     */
    public String getTemplate() {
        if (StringUtils.isNotBlank(this.template)) {
            return this.template;
        } else return null;
    }
    
    /**
     * Sets the global template base path (Ignored by ServletLoader, kept for compatibility)
     * @param basePath Base directory path for templates
     */
    public static void setTemplateBasePath(String basePath) {
        if (StringUtils.isNotBlank(basePath)) {
            Page.templateBasePath = basePath;
        }
    }
    
    /**
     * Creates a GenerateTemplate instance using the configured template and ServletContext
     * @param servletContext The ServletContext required by the ServletLoader
     * @return GenerateTemplate instance ready to use
     */
    private GenerateTemplate createGenerateTemplate(ServletContext servletContext) {
        // ServletLoader uses webapp-relative paths, so templateBasePath is no longer needed
        return new GenerateTemplate(servletContext, this.template);
    }
    
    public void home(PrintWriter out, String title, HttpSession session) throws IOException
    {
        this.UserPage(out, title, session);
    }

    /*
     *  Method: extendPageContext 
     *      Overloaded method with Map<String,Oject> signature to add categories as Map
     */
    public void extendPageContext(PrintWriter out, String title, HttpSession session, Map<String,Object> more) throws IOException
    {
        if (session != null) {  // Check session is still valid
            GenerateTemplate tplate = createGenerateTemplate(session.getServletContext());   
            Map<String, Object> context = new HashMap<>();
	    //log.info("Page: extendPageContext: Host name: "+HostInfo.chezmoi());
	    //log.info("Page: extendPageContext: swiftVersion: "+HostInfo.tell());
            context.put("hostname", HostInfo.chezmoi());
            if (session.getAttribute("menutype") != null) {
                context.put("menu", session.getAttribute("menutype").toString());
            }
            context.put("title",title);
            context.put("sessionID", session.getId());
            context.put("username",  session.getAttribute("username"));
            context.put("firstName",  session.getAttribute("firstName"));
            context.put("lastName",  session.getAttribute("lastName"));
            String accessLevel=(String)session.getAttribute("accessLevel");
            context.put("accessLevel", accessLevel);
            context.put("numCols", this.getnumCols()); 
            context.put("swiftVersion", HostInfo.tell());
            // Basket count for the menu bar badge - read directly from
            // the session's own "items" list, so this works on every
            // page render path without each one needing its own change.
            java.util.ArrayList<?> basketItems = (java.util.ArrayList<?>) session.getAttribute("items");
            context.put("basketCount", basketItems != null ? basketItems.size() : 0);
            if (more != null) {
                for (Map.Entry<String, Object> extractx : more.entrySet() ) {
                    context.put(extractx.getKey(), extractx.getValue());
                }
            }
            tplate.rollPress(out, context);
        }
    }

    /*
     *  Method: extendPageContext 
     *      Overloaded method with List<CatTree> signature to add categories as tree list
     */
    public void extendPageContext(PrintWriter out, String title, HttpSession session, List<CatTree> more) throws IOException
    {
        if (session != null) {  // Check session is still valid
            GenerateTemplate tplate = createGenerateTemplate(session.getServletContext());   
            Map<String, Object> context = new HashMap<>();
            context.put("hostname", HostInfo.chezmoi());
            if (session.getAttribute("menutype") != null) {
                context.put("menu", session.getAttribute("menutype").toString());
            }
            context.put("title",title);
            context.put("sessionID", session.getId());
            context.put("username",  session.getAttribute("username"));
            context.put("firstName",  session.getAttribute("firstName"));
            context.put("lastName",  session.getAttribute("lastName"));
            String accessLevel=(String)session.getAttribute("accessLevel");
            context.put("accessLevel", accessLevel);
            context.put("numCols", this.getnumCols()); 
            context.put("swiftVersion", HostInfo.tell());
            // Basket count for the menu bar badge - read directly from
            // the session's own "items" list, so this works on every
            // page render path without each one needing its own change.
            java.util.ArrayList<?> basketItems = (java.util.ArrayList<?>) session.getAttribute("items");
            context.put("basketCount", basketItems != null ? basketItems.size() : 0);
            if (more != null) {
                context.put("cattree", more);
            }
            tplate.rollPress(out, context);
        }
    }
    
    public void addVarForm(PrintWriter out, String title, HttpSession session, Map<String,Object> cardList) throws IOException
    {
        if (session != null) {  // Check session is still valid
            GenerateTemplate tplate = createGenerateTemplate(session.getServletContext());   
            Map<String, Object> context = new HashMap<>();
            context.put("hostname", HostInfo.chezmoi());
            if (session.getAttribute("menutype") != null) {
                context.put("menu", session.getAttribute("menutype").toString());
            }
            context.put("title",title);
            context.put("sessionID", session.getId());
            context.put("username",  session.getAttribute("username"));
            context.put("firstName",  session.getAttribute("firstName"));
            context.put("lastName",  session.getAttribute("lastName"));
            String accessLevel=(String)session.getAttribute("accessLevel");
            context.put("accessLevel", accessLevel);
            if (cardList != null) {
                context.put("cardNumbers", cardList);   
            }
            context.put("numCols", this.getnumCols());
            context.put("swiftVersion", HostInfo.tell());
            // Basket count for the menu bar badge - read directly from
            // the session's own "items" list, so this works on every
            // page render path without each one needing its own change.
            java.util.ArrayList<?> basketItems = (java.util.ArrayList<?>) session.getAttribute("items");
            context.put("basketCount", basketItems != null ? basketItems.size() : 0);
            tplate.rollPress(out, context);
        }
    }

    public void addVarDisplay(PrintWriter out, String title, HttpSession session, Map<String,Object> wrapinfo) throws IOException
    {
        if (session != null) {  // Check session is still valid
            GenerateTemplate tplate = createGenerateTemplate(session.getServletContext());   
            Map<String, Object> context = new HashMap<>();
            context.put("hostname", HostInfo.chezmoi());
            if (session.getAttribute("menutype") != null) {
                context.put("menu", session.getAttribute("menutype").toString());
            }
            context.put("title",title);
            context.put("sessionID", session.getId());
            context.put("username",  session.getAttribute("username"));
            context.put("firstName",  session.getAttribute("firstName"));
            context.put("lastName",  session.getAttribute("lastName"));
            String accessLevel=(String)session.getAttribute("accessLevel");
            context.put("accessLevel", accessLevel);
            if (wrapinfo != null) {
                context.put("duplicate", wrapinfo.get("duplicate"));
                context.put("numrecs", wrapinfo.get("numupdates"));
                context.put("varname", wrapinfo.get("varname"));
            }
            context.put("swiftVersion", HostInfo.tell());
            // Basket count for the menu bar badge - read directly from
            // the session's own "items" list, so this works on every
            // page render path without each one needing its own change.
            java.util.ArrayList<?> basketItems = (java.util.ArrayList<?>) session.getAttribute("items");
            context.put("basketCount", basketItems != null ? basketItems.size() : 0);
            tplate.rollPress(out, context);
        }
    }

    public void chkOutBskDisplay(PrintWriter out, String title, HttpSession session, Map<String,Object> wrapinfo) throws IOException
    {
        if (session != null) {  // Check session is still valid
            GenerateTemplate tplate = createGenerateTemplate(session.getServletContext());   
            Map<String, Object> context = new HashMap<>();
            context.put("hostname", HostInfo.chezmoi());
            if (session.getAttribute("menutype") != null) {
                context.put("menu", session.getAttribute("menutype").toString());
            }
            context.put("title",title);
            context.put("sessionID", session.getId());
            context.put("username",  session.getAttribute("username"));
            context.put("firstName",  session.getAttribute("firstName"));
            context.put("lastName",  session.getAttribute("lastName"));
            String accessLevel=(String)session.getAttribute("accessLevel");
            context.put("accessLevel", accessLevel);
            if (wrapinfo != null) {
                context.put("baskets", wrapinfo.get("baskets"));
                context.put("doWhat", wrapinfo.get("doWhat"));
            }
            context.put("swiftVersion", HostInfo.tell());
            // Basket count for the menu bar badge - read directly from
            // the session's own "items" list, so this works on every
            // page render path without each one needing its own change.
            java.util.ArrayList<?> basketItems = (java.util.ArrayList<?>) session.getAttribute("items");
            context.put("basketCount", basketItems != null ? basketItems.size() : 0);
            tplate.rollPress(out, context);
        }
    }

    public void downloadBskDisplay(PrintWriter out, String title, HttpSession session, Map<String,Object> wrapinfo) throws IOException
    {
        if (session != null) {  // Check session is still valid
            GenerateTemplate tplate = createGenerateTemplate(session.getServletContext());   
            Map<String, Object> context = new HashMap<>();
            context.put("hostname", HostInfo.chezmoi());
            if (session.getAttribute("menutype") != null) {
                context.put("menu", session.getAttribute("menutype").toString());
            }
            context.put("title",title);
            context.put("sessionID", session.getId());
            context.put("username",  session.getAttribute("username"));
            context.put("firstName",  session.getAttribute("firstName"));
            context.put("lastName",  session.getAttribute("lastName"));
            String accessLevel=(String)session.getAttribute("accessLevel");
            context.put("accessLevel", accessLevel);
            if (wrapinfo != null) {
                context.put("spsfile", wrapinfo.get("spsfile"));
            }
            context.put("swiftVersion", HostInfo.tell());
            // Basket count for the menu bar badge - read directly from
            // the session's own "items" list, so this works on every
            // page render path without each one needing its own change.
            java.util.ArrayList<?> basketItems = (java.util.ArrayList<?>) session.getAttribute("items");
            context.put("basketCount", basketItems != null ? basketItems.size() : 0);
            tplate.rollPress(out, context);
        }
    }
    
    public void basketVariables(PrintWriter out, String title, HttpSession session, ArrayList<String> cardnums) throws IOException
    {
        if (session != null) {  // Check session is still valid
            GenerateTemplate tplate = createGenerateTemplate(session.getServletContext());   
            Map<String, Object> context = new HashMap<>();
            context.put("hostname", HostInfo.chezmoi());
            if (session.getAttribute("menutype") != null) {
                context.put("menu", session.getAttribute("menutype").toString());
            }
            context.put("title",title);
            context.put("sessionID", session.getId());
            context.put("username",  session.getAttribute("username"));
            context.put("firstName",  session.getAttribute("firstName"));
            context.put("lastName",  session.getAttribute("lastName"));
            String accessLevel=(String)session.getAttribute("accessLevel");
            context.put("accessLevel", accessLevel);
            if (cardnums != null) {
                context.put("cardnumbers", cardnums);
            }
            context.put("swiftVersion", HostInfo.tell());
            // Basket count for the menu bar badge - read directly from
            // the session's own "items" list, so this works on every
            // page render path without each one needing its own change.
            java.util.ArrayList<?> basketItems = (java.util.ArrayList<?>) session.getAttribute("items");
            context.put("basketCount", basketItems != null ? basketItems.size() : 0);
            tplate.rollPress(out, context);
        }
    }

    public void variableMetadata(PrintWriter out, String title, HttpSession session, ArrayList<String> vardocs,
                                                                    String descrips, 
                                                                    HashMap<String, String> catlabs, 
                                                                    ArrayList<ArrayList<String>> vallabs,
                                                                    ArrayList<String> grpmems,
                                                                    Set<String> owlAvailable,
                                                                    Map<String, String> restrictedMessages
                                                                    ) throws IOException
    {
        if (session != null) {  // Check session is still valid
            GenerateTemplate tplate = createGenerateTemplate(session.getServletContext());   
            Map<String, Object> context = new HashMap<>();
            context.put("hostname", HostInfo.chezmoi());
            if (session.getAttribute("menutype") != null) {
                context.put("menu", session.getAttribute("menutype").toString());
            }
            context.put("title",title);
            context.put("sessionID", session.getId());
            context.put("username",  session.getAttribute("username"));
            context.put("firstName",  session.getAttribute("firstName"));
            context.put("lastName",  session.getAttribute("lastName"));
            context.put("swiftVersion", HostInfo.tell());
            java.util.ArrayList<?> basketItems = (java.util.ArrayList<?>) session.getAttribute("items");
            context.put("basketCount", basketItems != null ? basketItems.size() : 0);
            String accessLevel=(String)session.getAttribute("accessLevel");
            context.put("accessLevel", accessLevel);
            context.put("results", this.getResults());
            context.put("headers", this.getHeaders());
            context.put("numCols", this.getnumCols());
            if (vardocs != null) {
                context.put("documents", vardocs);
            }
            if (descrips != null) {
                context.put("freqdesc", descrips);
            }
            if (catlabs != null) {
                context.put("catlabs", catlabs);
            }
            if (vallabs != null) {
                context.put("valuelabs", vallabs);
            }
            if (grpmems != null) {
                context.put("grpmems", grpmems);
            }
            if (owlAvailable != null) {
                context.put("owlAvailable", owlAvailable);
            }
            if (restrictedMessages != null) {
                context.put("restrictedMessages", restrictedMessages);
            }
            tplate.rollPress(out, context);
        }
    }
    
    public void XMLPage(PrintWriter out, String title, HttpSession session, String xout)
    {
        if (session != null) {  // Check session is still valid
            GenerateTemplate tplate = createGenerateTemplate(session.getServletContext());   
            Map<String, Object> context = new HashMap<>();
            context.put("hostname", HostInfo.chezmoi());
            if (session.getAttribute("menutype") != null) {
                context.put("menu", session.getAttribute("menutype").toString());
            }
            context.put("title",title);
            context.put("sessionID", session.getId());
            context.put("username",  session.getAttribute("username"));
            context.put("firstName",  session.getAttribute("firstName"));
            context.put("lastName",  session.getAttribute("lastName"));
            if (xout != null) {
                context.put("xml", xout);
            }
            context.put("swiftVersion", HostInfo.tell());
            // Basket count for the menu bar badge - read directly from
            // the session's own "items" list, so this works on every
            // page render path without each one needing its own change.
            java.util.ArrayList<?> basketItems = (java.util.ArrayList<?>) session.getAttribute("items");
            context.put("basketCount", basketItems != null ? basketItems.size() : 0);
            tplate.rollPress(out, context);
        }
    }

    public void UserPage(PrintWriter out, String title, HttpSession session) 
    /*
     * This is the workhorse method used by most classes to display templates for the client
     */
    {
        this.UserPage(out, title, session, null);
    }

    public void UserPage(PrintWriter out, String title, HttpSession session, Set<String> owlAvailable) 
    /*
     * Overload of the above, additionally accepting the set of variable
     * names (from this page's results) that are available on OWL - see
     * mrc.util.OwlAvailability. Existing callers using the 3-argument
     * version are unaffected; it simply delegates here with null.
     */
    {
        this.UserPage(out, title, session, owlAvailable, null);
    }

    public void UserPage(PrintWriter out, String title, HttpSession session, Set<String> owlAvailable, Map<String, String> restrictedMessages) 
    /*
     * Overload of the above, additionally accepting a map of variable
     * name to restriction message, for variables found to be
     * restricted - see mrc.util.RestrictedVariables. Existing callers
     * using the 3- or 4-argument versions are unaffected; both simply
     * delegate here with null for any argument they don't supply.
     */
    {
        this.UserPage(out, title, session, owlAvailable, restrictedMessages, null);
    }

    public void UserPage(PrintWriter out, String title, HttpSession session, Set<String> owlAvailable, Map<String, String> restrictedMessages, Map<String, Integer> categoryCounts) 
    /*
     * Overload of the above, additionally accepting a map of category
     * name to count (e.g. "restricted" -> 3, "added" -> 12, "linked-added"
     * -> 5), for the Basket Management confirmation page's summary
     * counts above its results table. Existing callers using any
     * earlier overload are unaffected; they all delegate here with
     * null for any argument they don't supply.
     */
    {
        this.UserPage(out, title, session, owlAvailable, restrictedMessages, categoryCounts, null);
    }

    public void UserPage(PrintWriter out, String title, HttpSession session, Set<String> owlAvailable, Map<String, String> restrictedMessages, Map<String, Integer> categoryCounts, Map<String, Object> extraFlags) 
    /*
     * Overload of the above, additionally accepting a small, generic
     * map of extra named flags/values for a specific page - kept
     * generic (rather than adding another named boolean parameter
     * every time one more is needed) since the set of things a given
     * page needs to pass through tends to grow. First use: Basket
     * Management's "keep linked variables" toggle needs hasLinkedVars
     * (whether this add produced any linked variables at all) and
     * linkedVarsKept (their current on/off state) - see Variable.java.
     * Existing callers using any earlier overload are unaffected; they
     * all delegate here with null.
     */
    {
        if (session != null) {  // Check session is still valid
            GenerateTemplate tplate = createGenerateTemplate(session.getServletContext());   
            Map<String, Object> context = new HashMap<>();
	    //log.info("Page: UserPage: Host name: "+HostInfo.chezmoi());
	    //log.info("Page: UserPage: swiftVersion: "+HostInfo.tell());
            context.put("hostname", HostInfo.chezmoi());
            if (session.getAttribute("menutype") != null) {
                context.put("menu", session.getAttribute("menutype").toString());
            }
            context.put("title",title);
            context.put("sessionID", session.getId());
            context.put("username",  session.getAttribute("username"));
            context.put("firstName",  session.getAttribute("firstName"));
            context.put("lastName",  session.getAttribute("lastName"));
            String accessLevel=(String)session.getAttribute("accessLevel");
            context.put("accessLevel", accessLevel);
            context.put("numResults", this.getnumResults());
            context.put("results", this.getResults());
            context.put("headers", this.getHeaders());
            context.put("numCols", this.getnumCols());
            context.put("swiftVersion", HostInfo.tell());
            // Basket count for the menu bar badge - read directly from
            // the session's own "items" list, so this works on every
            // page render path without each one needing its own change.
            java.util.ArrayList<?> basketItems = (java.util.ArrayList<?>) session.getAttribute("items");
            context.put("basketCount", basketItems != null ? basketItems.size() : 0);
            if (owlAvailable != null) {
                context.put("owlAvailable", owlAvailable);
            }
            if (restrictedMessages != null) {
                context.put("restrictedMessages", restrictedMessages);
            }
            if (categoryCounts != null) {
                context.put("categoryCounts", categoryCounts);
            }
            if (extraFlags != null) {
                context.putAll(extraFlags);
            }
            tplate.rollPress(out, context);
        }
    }

    public void searchForm(PrintWriter out, String title, HttpSession session) throws IOException
    {
        this.UserPage(out, title, session);
    }
    
    public void shareForm(PrintWriter out, String title, HttpSession session, String basketId) throws IOException
    /*
     * This method allows you to add a basket ID to the standard context 
     */
    {
        this.shareForm(out, title, session, basketId, null);
    }

    public void shareForm(PrintWriter out, String title, HttpSession session, String basketId, List<String> plannedBasketIds) throws IOException
    /*
     * Overload of the above, additionally accepting the list of basket
     * IDs a save is planned to actually produce - more than one when
     * the basket is being split across several saved baskets for
     * exceeding the per-basket variable limit. Existing callers using
     * the 4-argument version are unaffected; it delegates here with
     * null, and the template only shows a split preview when this is
     * both non-null and has more than one entry.
     */
    {
        if (session != null) {  // Check session is still valid
            GenerateTemplate tplate = createGenerateTemplate(session.getServletContext());   
            Map<String, Object> context = new HashMap<>();
            context.put("hostname", HostInfo.chezmoi());
            if (session.getAttribute("menutype") != null) {
                context.put("menu", session.getAttribute("menutype").toString());
            }
            context.put("title",title);
            context.put("sessionID", session.getId());
            context.put("username",  session.getAttribute("username"));
            context.put("firstName",  session.getAttribute("firstName"));
            context.put("lastName",  session.getAttribute("lastName"));
            String accessLevel=(String)session.getAttribute("accessLevel");
            context.put("accessLevel", accessLevel);
            context.put("numResults", this.getnumResults());
            if (basketId != null) {
                context.put("basketId", basketId);
            }
            if (plannedBasketIds != null) {
                context.put("plannedBasketIds", plannedBasketIds);
            }
            context.put("swiftVersion", HostInfo.tell());
            // Basket count for the menu bar badge - read directly from
            // the session's own "items" list, so this works on every
            // page render path without each one needing its own change.
            java.util.ArrayList<?> basketItems = (java.util.ArrayList<?>) session.getAttribute("items");
            context.put("basketCount", basketItems != null ? basketItems.size() : 0);
            tplate.rollPress(out, context);
        }
    }

    public void rawTablePage(PrintWriter out, String title, HttpSession session, String tableHtml) throws IOException
    /*
     * A dedicated, minimal render method for a page whose entire content
     * is one HTML table the caller has already built as a plain Java
     * string - e.g. ApproveUser-userApprovalList.html and
     * ResetPassword-userList.html, each showing every user's
     * username/firstName/lastName/affiliation in one row per user.
     * Built after a genuine, unresolved failure attempting the same
     * four-per-row grouping directly in Pebble: a nested nested {% if %}
     * approach produced rows with the wrong data shifted between them,
     * and a follow-up attempt using direct results[index] array access
     * produced no rows at all, for reasons that could not be confirmed
     * without a live test environment. Rather than keep guessing at
     * Pebble's exact semantics for this shape of data, grouping and
     * row-building now happens entirely in Java, which can actually be
     * reasoned about and reviewed, and the finished HTML is passed
     * through as one string - the same proven approach already used by
     * savedBasketsConfirmation() just above for the same underlying
     * reason (a save can produce several independent tables, better
     * built directly than pushed through machinery designed for one
     * flat, evenly-grouped table).
     */
    {
        if (session != null) {  // Check session is still valid
            GenerateTemplate tplate = createGenerateTemplate(session.getServletContext());
            Map<String, Object> context = new HashMap<>();
            context.put("hostname", HostInfo.chezmoi());
            if (session.getAttribute("menutype") != null) {
                context.put("menu", session.getAttribute("menutype").toString());
            }
            context.put("title",title);
            context.put("sessionID", session.getId());
            context.put("username",  session.getAttribute("username"));
            context.put("firstName",  session.getAttribute("firstName"));
            context.put("lastName",  session.getAttribute("lastName"));
            String accessLevel=(String)session.getAttribute("accessLevel");
            context.put("accessLevel", accessLevel);
            context.put("tableHtml", tableHtml);
            context.put("swiftVersion", HostInfo.tell());
            java.util.ArrayList<?> basketItems = (java.util.ArrayList<?>) session.getAttribute("items");
            context.put("basketCount", basketItems != null ? basketItems.size() : 0);
            tplate.rollPress(out, context);
        }
    }

    public void savedBasketsConfirmation(PrintWriter out, String title, HttpSession session, String tableHtml) throws IOException
    /*
     * A dedicated, minimal render method for the "basket(s) saved"
     * confirmation page - deliberately separate from UserPage()'s
     * results/numCols machinery, which is built around one flat,
     * evenly-grouped table. A save can produce several independent
     * saved baskets at once (when split for exceeding the per-basket
     * variable limit), each needing its own table - simplest and
     * safest handled by building that HTML directly in Java (Basket.
     * java's buildBasketTableHtml()) and passing the finished markup
     * through as one string, rather than pushing multiple, separately-
     * grouped tables through a mechanism designed for a single one.
     */
    {
        if (session != null) {  // Check session is still valid
            GenerateTemplate tplate = createGenerateTemplate(session.getServletContext());
            Map<String, Object> context = new HashMap<>();
            context.put("hostname", HostInfo.chezmoi());
            if (session.getAttribute("menutype") != null) {
                context.put("menu", session.getAttribute("menutype").toString());
            }
            context.put("title",title);
            context.put("sessionID", session.getId());
            context.put("username",  session.getAttribute("username"));
            context.put("firstName",  session.getAttribute("firstName"));
            context.put("lastName",  session.getAttribute("lastName"));
            String accessLevel=(String)session.getAttribute("accessLevel");
            context.put("accessLevel", accessLevel);
            context.put("tableHtml", tableHtml);
            context.put("swiftVersion", HostInfo.tell());
            // Basket count for the menu bar badge - read directly from
            // the session's own "items" list, so this works on every
            // page render path without each one needing its own change.
            java.util.ArrayList<?> basketItems = (java.util.ArrayList<?>) session.getAttribute("items");
            context.put("basketCount", basketItems != null ? basketItems.size() : 0);
            tplate.rollPress(out, context);
        }
    }
    
    public void login(PrintWriter out, String title, HttpSession session) throws IOException
    /*
     * Basic template with minimum attributes in the context
     */
    {       
        if (session != null) {  // Check session is still valid
            GenerateTemplate tplate = createGenerateTemplate(session.getServletContext());   
            Map<String, Object> context = new HashMap<>();
            context.put("hostname", HostInfo.chezmoi());
            if (session.getAttribute("menutype") != null) {
                context.put("menu", session.getAttribute("menutype").toString());
            }
            context.put("title",title);
            context.put("sessionID", session.getId());
            context.put("swiftVersion", HostInfo.tell());
            // Basket count for the menu bar badge - read directly from
            // the session's own "items" list, so this works on every
            // page render path without each one needing its own change.
            java.util.ArrayList<?> basketItems = (java.util.ArrayList<?>) session.getAttribute("items");
            context.put("basketCount", basketItems != null ? basketItems.size() : 0);
            tplate.rollPress(out, context);
        }
    }

    /*
     * =========================== Getters and Setters ==================================================
     */
    
    public int getnumCols() {
        return this.numCols;
    }

    public void setnumCols(int num) {
        this.numCols = num;
    }

    public int getnumResults() {
        return this.numResults;
    }

    public void setnumResults(int num) {
        this.numResults = num;
    }

    public List<String> getResults()
    {
        return this.results;
    }
    
    public void setResults(List<String> res) 
    {
        this.results = res;
    }
    
    public List<String> getHeaders()
    {
        return this.headers;
    }
    
    public void setHeaders(ArrayList<String> res) 
    {
        this.headers = res;
    }
}
