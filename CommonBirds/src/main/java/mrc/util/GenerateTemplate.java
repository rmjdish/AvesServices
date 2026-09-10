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
import java.util.Map;
import java.util.ArrayList;
import java.util.logging.Logger;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.loader.Servlet5Loader;
import io.pebbletemplates.pebble.template.PebbleTemplate;

// Use jakarta.servlet because your POM specifies Jakarta EE 6.0+
import jakarta.servlet.ServletContext; 

import org.apache.commons.lang3.StringUtils;

public class GenerateTemplate 
{
    /*
     * Class Variables
     */
    private static final Logger log = Logger.getLogger("mrc.user");
    
    // The webapp-relative path where templates live.
    // If you placed them in src/main/webapp/local/templates/, use "/local/templates/"
    // If you placed them in src/main/webapp/WEB-INF/local/templates/, use "/WEB-INF/local/templates/"
    private static String defaultDir = "/local/templates/";
    private static String defaultExt = ".html";
    
    // Fallback static context if not passed in constructor
    private static ServletContext globalServletContext;
    
    /*
     * Instance Variables
     */
    private String aTemplate  = "default";
    private PebbleTemplate printReady;
    private String cPath;
    private String topic;
    private ArrayList<String> results;
    private int numCols;
    
    /**
     * Constructor requiring a ServletContext. (Recommended)
     */
    public GenerateTemplate(ServletContext servletContext) 
    {
        super();
        initializeEngine(null, servletContext);
    }
    
    /**
     * Constructor requiring a ServletContext and a specific template name. (Recommended)
     */
    public GenerateTemplate(ServletContext servletContext, String myTemplate) 
    {
        super();
        if (StringUtils.isNotBlank(myTemplate)) {
            this.aTemplate = myTemplate;
        }
        initializeEngine(myTemplate, servletContext);
    }
    
    /**
     * Fallback constructor using the global ServletContext.
     * (Requires setGlobalServletContext() to have been called at app startup).
     */
    public GenerateTemplate() 
    {
        super();
        initializeEngine(null, globalServletContext);
    }
    
    public GenerateTemplate(String myTemplate) 
    {
        super();
        if (StringUtils.isNotBlank(myTemplate)) {
            this.aTemplate = myTemplate;
        }
        initializeEngine(myTemplate, globalServletContext);
    }
    
    /**
     * Initializes the Pebble engine with a ServletLoader
     */
    private void initializeEngine(String templateName, ServletContext servletContext) {
        if (servletContext == null) {
            log.severe("GenerateTemplate: ServletContext is null! Cannot initialize ServletLoader. " +
                       "Ensure ServletContext is passed to the constructor or set globally.");
            return;
        }

        try {
            // Create a ServletLoader tied to the web application
            Servlet5Loader servletLoader = new Servlet5Loader(servletContext);
            
            // Set the directory prefix and file extension
            servletLoader.setPrefix(GenerateTemplate.defaultDir);
            servletLoader.setSuffix(GenerateTemplate.defaultExt);
            
            // Build the Pebble engine
            PebbleEngine engine = new PebbleEngine.Builder()
                .loader(servletLoader)
                .build();
            
            // Load the template
            String templateToLoad = templateName != null ? templateName : this.aTemplate;
            if (StringUtils.isNotBlank(templateToLoad)) {
                this.printReady = engine.getTemplate(templateToLoad);
                log.info("GenerateTemplate: Loaded template '" + templateToLoad + "' via ServletLoader");
            }
            
        } catch (PebbleException e) {
            log.severe("GenerateTemplate: Failed to initialize Pebble engine or load template '" + templateName + "': " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            log.severe("GenerateTemplate: Unexpected error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void rollPress (PrintWriter press, Map<String,Object> context)
    {
        if (press != null && context != null) {
            try {
                if (this.printReady != null) {
                    this.printReady.evaluate(press, context);
                } else {
                    log.severe("rollPress: printReady template is null. Template may not have been loaded.");
                }
            } catch (PebbleException | IOException e) {
                log.severe("rollPress failed to print template: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            log.warning("rollPress called with a null argument");
        }
    }
    
    public String getTopic() { return this.topic; }
    public void setTopic(String tpc) { this.topic = tpc; }
    
    public ArrayList<String> getResults() { return this.results; }
    public void setResults(ArrayList<String> res) { this.results = res; }
    
    public String getcPath() { return this.cPath; }
    public void setcPath(String path) { this.cPath = path; }
    
    public int getnumCols() { return this.numCols; }
    public void setnumCols(int num) { this.numCols = num; }

    /*
     * Class Methods for updating Class Variables
     */
    public static void setDefaultDir (String dir) {
        if (StringUtils.isNotBlank(dir)) {
            GenerateTemplate.defaultDir = dir;
        }
    }
    
    public static void setDefaultExt (String ext) {
        if (StringUtils.isNotBlank(ext)) {
            GenerateTemplate.defaultExt = ext;
        }
    }
    
    /**
     * Optional: Set a global ServletContext if you cannot pass it via constructors.
     * Typically called from a ServletContextListener or a base Servlet's init() method.
     */
    public static void setGlobalServletContext(ServletContext ctx) {
        GenerateTemplate.globalServletContext = ctx;
    }
}
