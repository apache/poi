/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.xssf.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.StylesSource;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFonts;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTNumFmt;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTNumFmts;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.StyleSheetDocument;;


/**
 * Table of styles shared across all sheets in a workbook.
 * 
 * @version $Id: SharedStringsTable.java 612495 2008-01-16 16:08:22Z ugo $
 */
public class StylesTable implements StylesSource, XSSFModel {
    private final Hashtable<Long,String> numberFormats = new Hashtable<Long,String>();
    private final LinkedList<CTFont> fonts = new LinkedList<CTFont>();
    private final LinkedList<CTFill> fills = new LinkedList<CTFill>();
    private final LinkedList<CTBorder> borders = new LinkedList<CTBorder>();
    
    /**
     * The first style id available for use as a custom style
     */
    public static final long FIRST_CUSTOM_STYLE_ID = 165;
    
    private StyleSheetDocument doc;
   
    /**
     * Create a new StylesTable, by reading it from 
     *  the InputStream of a a PackagePart.
     * 
     * @param is The input stream containing the XML document.
     * @throws IOException if an error occurs while reading.
     */
    public StylesTable(InputStream is) throws IOException {
        readFrom(is);
    }
    /**
     * Create a new, empty StylesTable
     */
    public StylesTable() {
    	doc = StyleSheetDocument.Factory.newInstance();
    }

    /**
     * Read this shared styles table from an XML file.
     * 
     * @param is The input stream containing the XML document.
     * @throws IOException if an error occurs while reading.
     */
    public void readFrom(InputStream is) throws IOException {
        try {
        	doc = StyleSheetDocument.Factory.parse(is);
        	
        	// Grab all the different bits we care about
        	for (CTNumFmt nfmt : doc.getStyleSheet().getNumFmts().getNumFmtArray()) {
        		numberFormats.put(nfmt.getNumFmtId(), nfmt.getFormatCode());
        	}
        	for (CTFont font : doc.getStyleSheet().getFonts().getFontArray()) {
        		fonts.add(font);
        	}
        	for (CTFill fill : doc.getStyleSheet().getFills().getFillArray()) {
        		fills.add(fill);
        	}
        	for (CTBorder border : doc.getStyleSheet().getBorders().getBorderArray()) {
        		borders.add(border);
        	}
        } catch (XmlException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }

    // ===========================================================
    //  Start of style related getters and setters
    // ===========================================================
    
    public String getNumberFormatAt(long idx) {
        return numberFormats.get(idx);
    }
    public synchronized long putNumberFormat(String fmt) {
        if (numberFormats.containsValue(fmt)) {
        	// Find the key, and return that
        	for(Enumeration<Long> keys = numberFormats.keys(); keys.hasMoreElements();) {
        		Long key = keys.nextElement();
        		if(numberFormats.get(key).equals(fmt)) {
        			return key;
        		}
        	}
        	throw new IllegalStateException("Found the format, but couldn't figure out where - should never happen!");
        }
        
        // Find a spare key, and add that
        long newKey = FIRST_CUSTOM_STYLE_ID;
        while(numberFormats.containsKey(newKey)) {
        	newKey++;
        }
        numberFormats.put(newKey, fmt);
        return newKey;
    }
    
    public Font getFontAt(long idx) {
    	// TODO
    	return null;
    }
    public synchronized long putFont(Font font) {
    	// TODO
    	return -1;
    }
    
    /**
     * For unit testing only
     */
    public int _getNumberFormatSize() {
    	return numberFormats.size();
    }
    /**
     * For unit testing only
     */
    public int _getFontsSize() {
    	return fonts.size();
    }
    /**
     * For unit testing only
     */
    public int _getFillsSize() {
    	return fills.size();
    }
    /**
     * For unit testing only
     */
    public int _getBordersSize() {
    	return borders.size();
    }
    

    /**
     * Write this table out as XML.
     * 
     * @param out The stream to write to.
     * @throws IOException if an error occurs while writing.
     */
    public void writeTo(OutputStream out) throws IOException {
        XmlOptions options = new XmlOptions();
        options.setSaveOuter();
        
        // Work on the current one
        // Need to do this, as we don't handle
        //  all the possible entries yet

    	// Formats
    	CTNumFmts formats = CTNumFmts.Factory.newInstance(); 
    	formats.setCount(numberFormats.size());
    	for (Entry<Long, String> fmt : numberFormats.entrySet()) {
    		CTNumFmt ctFmt = formats.addNewNumFmt();
    		ctFmt.setNumFmtId(fmt.getKey());
    		ctFmt.setFormatCode(fmt.getValue());
    	}
    	doc.getStyleSheet().setNumFmts(formats);
    	
    	// Fonts
    	CTFonts fnts = CTFonts.Factory.newInstance();
    	fnts.setCount(fonts.size());
    	fnts.setFontArray(
    			fonts.toArray(new CTFont[fonts.size()])
    	);
    	doc.getStyleSheet().setFonts(fnts);
    	
    	// Fills
    	// TODO
    	
    	// Borders
    	// TODO
    	
        // Save
        doc.save(out);
    }
}
