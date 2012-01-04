/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.tools.freemarker.filter;


import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * A file filter processor used to filter a file
 * obtaining a filtered document
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class FreeMarkerFilter {
    private final static Logger LOGGER = LoggerFactory.getLogger(FreeMarkerFilter.class.toString());
    
    // You should initialize this ONLY ONCE in the whole application life-cycle:
    private freemarker.template.Configuration cfg = null;
    
    // Hold the template
    private Template template=null;
    
    // register the init status of this object
    private boolean initted=false;
    
    /**
     * Constructor
     * @param wd workingDir (Set the explicit directory from which to load templates)
     * @param ifn input file name (the template)
     * @param data data structure (containing variable to substitute)
     * @see Freemarker documentation for detailed accepted data types.
     */
    public FreeMarkerFilter(String wd, String ifn){
        super();
        if (!initted){
            initted=(initConfig(wd)&&initTemplate(ifn));
        }
    }
    
    /**
     * Constructor
     * @param wd workingDir (Set the explicit directory from which to load templates)
     * @param r the reader of a FreeMarker template
     * @see Freemarker documentation for detailed accepted data types.
     */
    public FreeMarkerFilter(String workingDir, Reader reader){
        super();
        if (!initted){
            initted=(initConfig(workingDir)&&initTemplate(reader));
        }
    }
    

    /**
     * Try to wrap the passed object
     * @param tm the Object containing the TemplateModel to use as root data model
     *     TemplateHashModel
     *     TemplateSequenceModel
     *     TemplateCollectionModel
     *     TemplateScalarModel
     *     TemplateNumberModel
     *     TemplateTransformModel
     * @throws NullPointerException if tm is null
     * @throws TemplateModelException if defined objectWrapper can't wrap the passed object
     */
    public TemplateModel wrapRoot(Object tm) throws NullPointerException, TemplateModelException{
        if (isInitted()){
            if (tm!=null){
                /* From FreeMarked docs:
                 * Processes the template, using data from the map, and outputs
                 * the resulting text to the supplied <tt>Writer</tt> The elements of the
                 * map are converted to template models using the default object wrapper
                 * returned by the {@link Configuration#getObjectWrapper() getObjectWrapper()}
                 * method of the <tt>Configuration</tt>.
                 * @param rootMap the root node of the data model.  If null, an
                 * empty data model is used. Can be any object that the effective object
                 * wrapper can turn into a <tt>TemplateHashModel</tt>. Basically, simple and
                 * beans wrapper can turn <tt>java.util.Map</tt> objects into hashes
                 * and the Jython wrapper can turn both a <tt>PyDictionary</tt> as well as
                 * any object that implements <tt>__getitem__</tt> into a template hash.
                 * Naturally, you can pass any object directly implementing
                 * <tt>TemplateHashModel</tt> as well.
                 */
                 return cfg.getObjectWrapper().wrap(tm);
            }
            else {
                String message="Unable to initialize filter using a null root data structure";
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new NullPointerException(message);
            }
        }
        else {
            String message="Unable to initialize filter since it is not initialized!";
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            throw new NullPointerException(message);
        }
            
    }
    
    /**
     * Check the status of this configuration
     * @return true if the init() method has already run
     * false otherwise
     */
    private boolean isInitted(){
        return initted;
    }

    /**
     * Initialize configuration
     * @return true if init ends successful
     */
    private boolean initConfig(String workingDirectory){
        if (initted)
            return initted;
        
     // singleton configuration pattern
        if (cfg==null){
            cfg=new freemarker.template.Configuration();
        }
        
        if (workingDirectory!=null && cfg!=null){
            try {
                cfg.setDirectoryForTemplateLoading(new File(workingDirectory));
            }
            catch (IOException e){
                if (LOGGER.isErrorEnabled())
                    LOGGER.error("Unable to get the working dir: "
                            +e.getLocalizedMessage(),e);
                return false;
            }
        }
        else {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Unable to get the working dir for this filter");
            return false;
        }
        
        return true;
    }
    
    private boolean initTemplate(Reader reader){

        if (initted)
            return initted;
        
        /* Get or create a template */
        if (reader!=null){
            try {
                template = new Template(null,reader,cfg,cfg.getEncoding(cfg.getLocale()));
            } catch (IOException e) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error("Unable to get the template: "+e.getLocalizedMessage(),e);
                return false;
            }
        }
        else {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Unable to set the template file check the input_name parameter");
            return false;
        }
        
        return true;
    }

    
    private boolean initTemplate(String input_name){

        if (initted)
            return initted;
        
        /* Get or create a template */
        if (input_name!=null){
            try {
                template = cfg.getTemplate(input_name);
            } catch (IOException e) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error("Unable to get the template: "+e.getLocalizedMessage(),e);
                return false;
            }
        }
        else {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Unable to set the template file check the input_name parameter");
            return false;
        }
        
        return true;
    }
    
    /**
     * This method is used to process the file using
     * this configuration
     * @param out the Writer
     * @return true if success
     * @throws TemplateException if an exception occurs during template processing
     * @throws IOException if an I/O exception occurs during writing to the writer.
     */
    public final boolean process(TemplateModel root, Writer out) throws TemplateException, IOException {
        if (isInitted()){
            try {
                template.process(root,out);
                return true;
            } catch (TemplateException e) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(e.getLocalizedMessage(),e);
                throw e;
            } catch (IOException e) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(e.getLocalizedMessage(),e);
                throw e;
            }
        }
        else {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("This filter is not well initialized");
            return false;   
        }
    }
        

}
