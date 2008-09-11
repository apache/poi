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
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorders;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellStyleXfs;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellXfs;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxfs;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFills;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFonts;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTNumFmt;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTNumFmts;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTStylesheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPatternType;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.StyleSheetDocument;


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
    
    private final LinkedList<CTDxf> dxfs = new LinkedList<CTDxf>();
    
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
    	// Initialization required in order to make the document readable by MSExcel
    	initialize();
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
        	// dxf
        	if(doc.getStyleSheet().getDxfs() != null)
            	for (CTDxf dxf : doc.getStyleSheet().getDxfs().getDxfArray()) {
            		dxfs.add(dxf);
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
    	return new XSSFFont(fonts.get((int) idx));
    }
    
    public synchronized long putFont(Font font) {
    	return putFont((XSSFFont)font, fonts);
    }
    
	public CellStyle getStyleAt(long idx) {
    	int styleXfId = 0;
    	
    	// 0 is the empty default
    	if(xfs.get((int) idx).getXfId() > 0) {
    		styleXfId = (int) xfs.get((int) idx).getXfId();
    	}
    	
		return new XSSFCellStyle((int) idx, styleXfId, this);
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
		return putBorder(border, borders);
	}

	public XSSFCellFill getFillAt(long idx) {
		return new XSSFCellFill(fills.get((int) idx));
	}
	public long putFill(XSSFCellFill fill) {
		return putFill(fill, fills);
	}
	
	public CTXf getCellXfAt(long idx) {
		return xfs.get((int) idx);
	}
	public long putCellXf(CTXf cellXf) {
		xfs.add(cellXf);
		return xfs.size();
	}
	
	public CTXf getCellStyleXfAt(long idx) {
		return styleXfs.get((int) idx);
	}
	public long putCellStyleXf(CTXf cellStyleXf) {
		styleXfs.add(cellStyleXf);
		return styleXfs.size();
	}
    /**
     * get the size of cell styles
     */
    public int getNumCellStyles(){
        return styleXfs.size();
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
    	CTFonts ctFonts = CTFonts.Factory.newInstance();
    	ctFonts.setCount(fonts.size());
    	ctFonts.setFontArray(
    			fonts.toArray(new CTFont[fonts.size()])
    	);
    	doc.getStyleSheet().setFonts(ctFonts);
    	
    	// Fills
    	CTFills ctFills = CTFills.Factory.newInstance();
    	ctFills.setCount(fills.size());
    	ctFills.setFillArray(fills.toArray(new CTFill[fills.size()]));
    	doc.getStyleSheet().setFills(ctFills);
    	
    	// Borders
    	CTBorders ctBorders = CTBorders.Factory.newInstance();
    	ctBorders.setCount(borders.size());
    	ctBorders.setBorderArray(borders.toArray(new CTBorder[borders.size()]));
    	doc.getStyleSheet().setBorders(ctBorders);
    	
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
    	
    	// Style dxf
    	if(dxfs.size() > 0) {
        	CTDxfs ctDxfs = CTDxfs.Factory.newInstance();
        	ctDxfs.setCount(dxfs.size());
        	ctDxfs.setDxfArray(dxfs.toArray(new CTDxf[dxfs.size()])
        	);
        	doc.getStyleSheet().setDxfs(ctDxfs);
    	}
    	
    	
    	
        // Save
        doc.save(out, options);
    }
    
	private long putBorder(XSSFCellBorder border, LinkedList<CTBorder> borders) {
		return border.putBorder(borders);
	}
	
	private long putFill(XSSFCellFill fill, LinkedList<CTFill> fills) {
		return fill.putFill(fills);
	}

    private long putFont(XSSFFont font, LinkedList<CTFont> fonts) {
    	return font.putFont(fonts);
	}
	private void initialize() {
		//CTFont ctFont = createDefaultFont();
		XSSFFont xssfFont = createDefaultFont();
    	fonts.add(xssfFont.getCTFont());
    	
    	CTFill ctFill = createDefaultFill();
    	fills.add(ctFill);
    	
    	CTBorder ctBorder = createDefaultBorder();
    	borders.add(ctBorder);
    	
    	CTXf styleXf = createDefaultXf();
    	styleXfs.add(styleXf);
    	CTXf xf = createDefaultXf();
    	xf.setXfId(0);
    	xfs.add(xf);
	}
	
	private CTXf createDefaultXf() {
		CTXf ctXf = CTXf.Factory.newInstance();
    	ctXf.setNumFmtId(0);
    	ctXf.setFontId(0);
    	ctXf.setFillId(0);
    	ctXf.setBorderId(0);
    	return ctXf;
	}
	private CTBorder createDefaultBorder() {
		CTBorder ctBorder = CTBorder.Factory.newInstance();
    	ctBorder.addNewBottom();
    	ctBorder.addNewTop();
    	ctBorder.addNewLeft();
    	ctBorder.addNewRight();
    	ctBorder.addNewDiagonal();
		return ctBorder;
	}
	
	private CTFill createDefaultFill() {
		CTFill ctFill = CTFill.Factory.newInstance();
    	ctFill.addNewPatternFill().setPatternType(STPatternType.NONE);
		return ctFill;
	}
	
	private XSSFFont createDefaultFont() {
		/*
		CTFont ctFont = CTFont.Factory.newInstance();
    	ctFont.addNewSz().setVal(11);
    	ctFont.addNewColor().setTheme(1);
    	ctFont.addNewName().setVal("Calibri");
    	ctFont.addNewFamily().setVal(2);
    	ctFont.addNewScheme().setVal(STFontScheme.MINOR);
    	XSSFFont font=new XSSFFont(ctFont);
		return font;
		*/
		
		XSSFFont xssfFont=new XSSFFont();
		xssfFont.setFontHeightInPoints(XSSFFont.DEFAULT_FONT_SIZE);
		xssfFont.setColor(XSSFFont.DEFAULT_FONT_COLOR);//setTheme 
		xssfFont.setFontName(XSSFFont.DEFAULT_FONT_NAME);
		xssfFont.setFamily(XSSFFont.FONT_FAMILY_SWISS);
		xssfFont.setScheme(XSSFFont.SCHEME_MINOR);
		return xssfFont;
	}
	
	
	
	
	public CTDxf getDxf(long idx) {
		if(dxfs.size()==0)
			return CTDxf.Factory.newInstance();
		else
			return dxfs.get((int) idx);
	}
	
	
	public long putDxf(CTDxf dxf) {
			this.dxfs.add(dxf);
			return this.dxfs.size();
	}
	
	
	
}
