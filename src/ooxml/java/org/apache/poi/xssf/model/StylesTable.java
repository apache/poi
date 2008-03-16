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
import java.util.LinkedList;

import org.apache.poi.ss.usermodel.SharedStringSource;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxml4j.opc.PackagePart;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTNumFmt;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTNumFmts;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.StyleSheetDocument;;


/**
 * Table of styles shared across all sheets in a workbook.
 * 
 * FIXME: I don't like having a dependency on PackagePart (from OpenXML4J) in model classes.
 * I'd rather let Workbook keep track of all part-document relationships and keep all other
 * classes clean. -- Ugo
 * 
 * @version $Id: SharedStringsTable.java 612495 2008-01-16 16:08:22Z ugo $
 */
public class StylesTable {
    private final LinkedList<String> numberFormats = new LinkedList<String>();
    private final LinkedList<CTFont> fonts = new LinkedList<CTFont>();
    private final LinkedList<CTFill> fills = new LinkedList<CTFill>();
    private final LinkedList<CTBorder> borders = new LinkedList<CTBorder>();
    
    private PackagePart part;
    private StyleSheetDocument doc;
   
    /**
     * Create a new StylesTable by reading it from a PackagePart.
     * 
     * @param part The PackagePart to read.
     * @throws IOException if an error occurs while reading.
     */
    public StylesTable(PackagePart part) throws IOException {
        this.part = part;
        InputStream is = part.getInputStream();
        try {
            readFrom(is);
        } finally {
            if (is != null) is.close();
        }
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
        		numberFormats.add(nfmt.getFormatCode());
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

    public String getNumberFormatAt(int idx) {
        return numberFormats.get(idx);
    }

    public synchronized int putNumberFormat(String fmt) {
        if (numberFormats.contains(fmt)) {
            return numberFormats.indexOf(fmt);
        }
        numberFormats.add(fmt);
        return numberFormats.size() - 1;
    }

    /**
     * Save this table to its own PackagePart.
     * 
     * @throws IOException if an error occurs while writing.
     */
    public void save() throws IOException {
        OutputStream out = this.part.getOutputStream();
        try {
            writeTo(out);
        } finally {
            out.close();
        }
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
    	for (String fmt : numberFormats) {
    		formats.addNewNumFmt().setFormatCode(fmt);
    	}
    	doc.getStyleSheet().setNumFmts(formats);
    	
    	// Fonts
    	
    	// Fills
    	
    	// Borders
    	
        // Save
        doc.save(out);
    }
}
