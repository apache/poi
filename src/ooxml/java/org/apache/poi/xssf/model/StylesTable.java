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

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.StylesSource;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellStyleXfs;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellXfs;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFonts;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTNumFmt;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTNumFmts;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTStylesheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.StyleSheetDocument;
import javax.xml.namespace.QName;



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
    private final LinkedList<CTXf> styleXfs = new LinkedList<CTXf>();
    private final LinkedList<CTXf> xfs = new LinkedList<CTXf>();
    
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
    	doc.addNewStyleSheet();
    	
    	// Add a single, default cell xf and cell style xf
    	// Excel seems to require these
    	CTXf[] ctxfs = new CTXf[2];
    	for (int i = 0; i < ctxfs.length; i++) {
			ctxfs[i] = CTXf.Factory.newInstance(); 
			ctxfs[i].setNumFmtId(0);
		}
    	xfs.add(ctxfs[0]);
    	styleXfs.add(ctxfs[1]);
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
        	if(doc.getStyleSheet().getNumFmts() != null)
        	for (CTNumFmt nfmt : doc.getStyleSheet().getNumFmts().getNumFmtArray()) {
        		numberFormats.put(nfmt.getNumFmtId(), nfmt.getFormatCode());
        	}
        	if(doc.getStyleSheet().getFonts() != null)
        	for (CTFont font : doc.getStyleSheet().getFonts().getFontArray()) {
        		fonts.add(font);
        	}
        	if(doc.getStyleSheet().getFills() != null)
        	for (CTFill fill : doc.getStyleSheet().getFills().getFillArray()) {
        		fills.add(fill);
        	}
        	if(doc.getStyleSheet().getBorders() != null)
        	for (CTBorder border : doc.getStyleSheet().getBorders().getBorderArray()) {
        		borders.add(border);
        	}
        	if(doc.getStyleSheet().getCellXfs() != null)
        	for (CTXf xf : doc.getStyleSheet().getCellXfs().getXfArray()) {
        		xfs.add(xf);
        	}
        	if(doc.getStyleSheet().getCellStyleXfs() != null)
        	for (CTXf xf : doc.getStyleSheet().getCellStyleXfs().getXfArray()) {
        		styleXfs.add(xf);
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
    
    public CellStyle getStyleAt(long idx) {
    	CTXf mainXf = xfs.get((int)idx);
    	CTXf styleXf = null;
    	
    	// 0 is the empty default
    	if(mainXf.getXfId() > 0) {
    		styleXf = styleXfs.get((int)mainXf.getXfId());
    	}
    	
		return new XSSFCellStyle(mainXf, styleXf, this);
	}
    public synchronized long putStyle(CellStyle style) {
    	XSSFCellStyle xStyle = (XSSFCellStyle)style;
    	CTXf mainXF = xStyle.getCoreXf();
    	
    	if(! xfs.contains(mainXF)) {
    		xfs.add(mainXF);
    	}
		return xfs.indexOf(mainXF);
    }
	
	public XSSFCellBorder getBorderAt(long idx) {
		return new XSSFCellBorder(borders.get((int)idx));
	}
	public long putBorder(XSSFCellBorder border) {
		return putBorder(border.getCTBorder());
	}
	public synchronized long putBorder(CTBorder border) {
		if(borders.contains(border)) {
			return borders.indexOf(border);
		}
		borders.add(border);
		return borders.size() - 1;
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
     * For unit testing only
     */
    public int _getXfsSize() {
    	return xfs.size();
    }
    /**
     * For unit testing only
     */
    public int _getStyleXfsSize() {
    	return styleXfs.size();
    }
    /**
     * For unit testing only!
     */
    public CTStylesheet _getRawStylesheet() {
    	return doc.getStyleSheet();
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
        options.setUseDefaultNamespace();
        
        // Requests use of whitespace for easier reading
        options.setSavePrettyPrint();

        
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
    	
    	// Xfs
    	if(xfs.size() > 0) {
	    	CTCellXfs ctXfs = CTCellXfs.Factory.newInstance();
	    	ctXfs.setCount(xfs.size());
	    	ctXfs.setXfArray(
	    			xfs.toArray(new CTXf[xfs.size()])
	    	);
	    	doc.getStyleSheet().setCellXfs(ctXfs);
    	}
    	
    	// Style xfs
    	if(styleXfs.size() > 0) {
        	CTCellStyleXfs ctSXfs = CTCellStyleXfs.Factory.newInstance();
        	ctSXfs.setCount(styleXfs.size());
        	ctSXfs.setXfArray(
        			styleXfs.toArray(new CTXf[styleXfs.size()])
        	);
        	doc.getStyleSheet().setCellStyleXfs(ctSXfs);
    	}
    	
        // Save
        doc.save(out, options);
    }
}
