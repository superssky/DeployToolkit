/*
 * Copyright 2001-2004 (C) MetaStuff, Ltd. All Rights Reserved.
 * 
 * This software is open source. 
 * See the bottom of this file for the licence.
 * 
 * $Id: AbstractDemo.java,v 1.4 2005/01/29 14:52:57 maartenc Exp $
 */

package com.wyb.tool.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dom4j.Document;
import org.dom4j.io.DOMReader;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.InputSource;

/**
 * xml util
 * @author wyb
 *
 */
public class Config {

    /** The writer of XML */
    private static XMLWriter writer;
    private static OutputFormat format = OutputFormat.createPrettyPrint();
    private static String encoding = "UTF-8";
    
    public static Document parse(String url) throws Exception {
    	// parse a DOM tree
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder builder = factory.newDocumentBuilder();
    	
    	
    	org.w3c.dom.Document domDocument = builder.parse(new InputSource(new InputStreamReader(new FileInputStream(url), encoding)));
    	
    	
    	// now convert to DOM4J model
    	DOMReader reader = new DOMReader();
    	Document document = reader.read(domDocument);
    	
    	
    	return document;
    }
    
    public static void write(Document document) throws Exception {
    	getXMLWriter().write(document);
    	writer.close();
    }
    
    public static void write(Document document, File outFile) throws Exception {
        FileOutputStream out = new FileOutputStream(outFile);
        writer = getXMLWriter(out);
        writer.write(document);
        writer.close();
    }
    
    public static XMLWriter getXMLWriter() throws Exception {
        if (writer == null) {
            writer = createXMLWriter();
        }
        return writer;
    }

    public static XMLWriter getXMLWriter(FileOutputStream out) throws Exception {
    	if(out == null) {
    		return getXMLWriter();
    	}
        return createXMLWriter(out);
    }
    /**
     * A Factory Method to create an <code>XMLWriter</code> instance allowing
     * derived classes to change this behaviour
     */
    public static XMLWriter createXMLWriter() throws Exception {
    	format.setEncoding(encoding);
        return new XMLWriter(format);
    }
    
    private static XMLWriter createXMLWriter(FileOutputStream out) throws Exception {
    	format.setEncoding(encoding);
        return new XMLWriter(out, format);
    }
    
    public static void setXMLWriter(XMLWriter writer) {
    	Config.writer = writer;
    }
    
}
