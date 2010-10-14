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
import java.util.*;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.FontFamily;
import org.apache.poi.ss.usermodel.FontScheme;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.apache.poi.POIXMLDocumentPart;
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
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;


/**
 * Table of styles shared across all sheets in a workbook.
 *
 * @author ugo
 */
public class StylesTable extends POIXMLDocumentPart {
	private final Map<Integer, String> numberFormats = new LinkedHashMap<Integer,String>();
	private final List<XSSFFont> fonts = new ArrayList<XSSFFont>();
	private final List<XSSFCellFill> fills = new ArrayList<XSSFCellFill>();
	private final List<XSSFCellBorder> borders = new ArrayList<XSSFCellBorder>();
	private final List<CTXf> styleXfs = new ArrayList<CTXf>();
	private final List<CTXf> xfs = new ArrayList<CTXf>();

	private final List<CTDxf> dxfs = new ArrayList<CTDxf>();

	/**
	 * The first style id available for use as a custom style
	 */
	public static final int FIRST_CUSTOM_STYLE_ID = BuiltinFormats.FIRST_USER_DEFINED_FORMAT_INDEX + 1;

	private StyleSheetDocument doc;
	private ThemesTable theme;

	/**
	 * Create a new, empty StylesTable
	 */
	public StylesTable() {
		super();
		doc = StyleSheetDocument.Factory.newInstance();
		doc.addNewStyleSheet();
		// Initialization required in order to make the document readable by MSExcel
		initialize();
	}

	public StylesTable(PackagePart part, PackageRelationship rel) throws IOException {
		super(part, rel);
		readFrom(part.getInputStream());
	}

	public ThemesTable getTheme() {
        return theme;
    }

    public void setTheme(ThemesTable theme) {
        this.theme = theme;
    }

	/**
	 * Read this shared styles table from an XML file.
	 *
	 * @param is The input stream containing the XML document.
	 * @throws IOException if an error occurs while reading.
	 */
    @SuppressWarnings("deprecation") //YK: getXYZArray() array accessors are deprecated in xmlbeans with JDK 1.5 support
	protected void readFrom(InputStream is) throws IOException {
		try {
			doc = StyleSheetDocument.Factory.parse(is);

            CTStylesheet styleSheet = doc.getStyleSheet();

            // Grab all the different bits we care about
			CTNumFmts ctfmts = styleSheet.getNumFmts();
            if( ctfmts != null){
                for (CTNumFmt nfmt : ctfmts.getNumFmtArray()) {
                    numberFormats.put((int)nfmt.getNumFmtId(), nfmt.getFormatCode());
                }
            }

            CTFonts ctfonts = styleSheet.getFonts();
            if(ctfonts != null){
				int idx = 0;
				for (CTFont font : ctfonts.getFontArray()) {
					XSSFFont f = new XSSFFont(font, idx);
					fonts.add(f);
					idx++;
				}
			}
            CTFills ctfills = styleSheet.getFills();
            if(ctfills != null){
                for (CTFill fill : ctfills.getFillArray()) {
                    fills.add(new XSSFCellFill(fill));
                }
            }

            CTBorders ctborders = styleSheet.getBorders();
            if(ctborders != null) {
                for (CTBorder border : ctborders.getBorderArray()) {
                    borders.add(new XSSFCellBorder(border));
                }
            }

            CTCellXfs cellXfs = styleSheet.getCellXfs();
            if(cellXfs != null) xfs.addAll(Arrays.asList(cellXfs.getXfArray()));

            CTCellStyleXfs cellStyleXfs = styleSheet.getCellStyleXfs();
            if(cellStyleXfs != null) styleXfs.addAll(Arrays.asList(cellStyleXfs.getXfArray()));

            CTDxfs styleDxfs = styleSheet.getDxfs();
			if(styleDxfs != null) dxfs.addAll(Arrays.asList(styleDxfs.getDxfArray()));

		} catch (XmlException e) {
			throw new IOException(e.getLocalizedMessage());
		}
	}

	// ===========================================================
	//  Start of style related getters and setters
	// ===========================================================

	public String getNumberFormatAt(int idx) {
		return numberFormats.get(idx);
	}

	public int putNumberFormat(String fmt) {
		if (numberFormats.containsValue(fmt)) {
			// Find the key, and return that
			for(Integer key : numberFormats.keySet() ) {
				if(numberFormats.get(key).equals(fmt)) {
					return key;
				}
			}
			throw new IllegalStateException("Found the format, but couldn't figure out where - should never happen!");
		}

		// Find a spare key, and add that
		int newKey = FIRST_CUSTOM_STYLE_ID;
		while(numberFormats.containsKey(newKey)) {
			newKey++;
		}
		numberFormats.put(newKey, fmt);
		return newKey;
	}

	public XSSFFont getFontAt(int idx) {
		return fonts.get(idx);
	}

	/**
	 * Records the given font in the font table.
	 * Will re-use an existing font index if this
	 *  font matches another, EXCEPT if forced
	 *  registration is requested.
	 * This allows people to create several fonts
	 *  then customise them later.
	 * Note - End Users probably want to call
	 *  {@link XSSFFont#registerTo(StylesTable)}
	 */
	public int putFont(XSSFFont font, boolean forceRegistration) {
		int idx = -1;
		if(!forceRegistration) {
			idx = fonts.indexOf(font);
		}

		if (idx != -1) {
			return idx;
		}
		
		idx = fonts.size();
		fonts.add(font);
		return idx;
	}
	public int putFont(XSSFFont font) {
		return putFont(font, false);
	}

	public XSSFCellStyle getStyleAt(int idx) {
		int styleXfId = 0;

		// 0 is the empty default
		if(xfs.get(idx).getXfId() > 0) {
			styleXfId = (int) xfs.get(idx).getXfId();
		}

		return new XSSFCellStyle(idx, styleXfId, this, theme);
	}
	public int putStyle(XSSFCellStyle style) {
		CTXf mainXF = style.getCoreXf();

		if(! xfs.contains(mainXF)) {
			xfs.add(mainXF);
		}
		return xfs.indexOf(mainXF);
	}

	public XSSFCellBorder getBorderAt(int idx) {
		return borders.get(idx);
	}

	public int putBorder(XSSFCellBorder border) {
		int idx = borders.indexOf(border);
		if (idx != -1) {
			return idx;
		}
		borders.add(border);
		return borders.size() - 1;
	}

	public XSSFCellFill getFillAt(int idx) {
		return fills.get(idx);
	}

	public List<XSSFCellBorder> getBorders(){
		return borders;
	}

	public List<XSSFCellFill> getFills(){
		return fills;
	}

	public List<XSSFFont> getFonts(){
		return fonts;
	}

	public Map<Integer, String> getNumberFormats(){
		return numberFormats;
	}

	public int putFill(XSSFCellFill fill) {
		int idx = fills.indexOf(fill);
		if (idx != -1) {
			return idx;
		}
		fills.add(fill);
		return fills.size() - 1;
	}

	public CTXf getCellXfAt(int idx) {
		return xfs.get(idx);
	}
	public int putCellXf(CTXf cellXf) {
		xfs.add(cellXf);
		return xfs.size();
	}

	public CTXf getCellStyleXfAt(int idx) {
		return styleXfs.get(idx);
	}
	public int putCellStyleXf(CTXf cellStyleXf) {
		styleXfs.add(cellStyleXf);
		return styleXfs.size();
	}
	/**
	 * get the size of cell styles
	 */
	public int getNumCellStyles(){
        // Each cell style has a unique xfs entry
        // Several might share the same styleXfs entry
        return xfs.size();
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
	public CTStylesheet getCTStylesheet() {
		return doc.getStyleSheet();
	}


	/**
	 * Write this table out as XML.
	 *
	 * @param out The stream to write to.
	 * @throws IOException if an error occurs while writing.
	 */
	public void writeTo(OutputStream out) throws IOException {
		XmlOptions options = new XmlOptions(DEFAULT_XML_OPTIONS);

		// Work on the current one
		// Need to do this, as we don't handle
		//  all the possible entries yet
        CTStylesheet styleSheet = doc.getStyleSheet();

		// Formats
		CTNumFmts formats = CTNumFmts.Factory.newInstance();
		formats.setCount(numberFormats.size());
		for (Entry<Integer, String> fmt : numberFormats.entrySet()) {
			CTNumFmt ctFmt = formats.addNewNumFmt();
			ctFmt.setNumFmtId(fmt.getKey());
			ctFmt.setFormatCode(fmt.getValue());
		}
		styleSheet.setNumFmts(formats);

		int idx;
		// Fonts
		CTFonts ctFonts = CTFonts.Factory.newInstance();
		ctFonts.setCount(fonts.size());
		CTFont[] ctfnt = new CTFont[fonts.size()];
		idx = 0;
		for(XSSFFont f : fonts) ctfnt[idx++] = f.getCTFont();
		ctFonts.setFontArray(ctfnt);
		styleSheet.setFonts(ctFonts);

		// Fills
		CTFills ctFills = CTFills.Factory.newInstance();
		ctFills.setCount(fills.size());
		CTFill[] ctf = new CTFill[fills.size()];
		idx = 0;
		for(XSSFCellFill f : fills) ctf[idx++] = f.getCTFill();
		ctFills.setFillArray(ctf);
		styleSheet.setFills(ctFills);

		// Borders
		CTBorders ctBorders = CTBorders.Factory.newInstance();
		ctBorders.setCount(borders.size());
		CTBorder[] ctb = new CTBorder[borders.size()];
		idx = 0;
		for(XSSFCellBorder b : borders) ctb[idx++] = b.getCTBorder();
		ctBorders.setBorderArray(ctb);
		styleSheet.setBorders(ctBorders);

		// Xfs
		if(xfs.size() > 0) {
			CTCellXfs ctXfs = CTCellXfs.Factory.newInstance();
			ctXfs.setCount(xfs.size());
			ctXfs.setXfArray(
					xfs.toArray(new CTXf[xfs.size()])
			);
			styleSheet.setCellXfs(ctXfs);
		}

		// Style xfs
		if(styleXfs.size() > 0) {
			CTCellStyleXfs ctSXfs = CTCellStyleXfs.Factory.newInstance();
			ctSXfs.setCount(styleXfs.size());
			ctSXfs.setXfArray(
					styleXfs.toArray(new CTXf[styleXfs.size()])
			);
			styleSheet.setCellStyleXfs(ctSXfs);
		}

		// Style dxfs
		if(dxfs.size() > 0) {
			CTDxfs ctDxfs = CTDxfs.Factory.newInstance();
			ctDxfs.setCount(dxfs.size());
			ctDxfs.setDxfArray(dxfs.toArray(new CTDxf[dxfs.size()])
			);
			styleSheet.setDxfs(ctDxfs);
		}

		// Save
		doc.save(out, options);
	}

	@Override
	protected void commit() throws IOException {
		PackagePart part = getPackagePart();
		OutputStream out = part.getOutputStream();
		writeTo(out);
		out.close();
	}

	private void initialize() {
		//CTFont ctFont = createDefaultFont();
		XSSFFont xssfFont = createDefaultFont();
		fonts.add(xssfFont);

		CTFill[] ctFill = createDefaultFills();
		fills.add(new XSSFCellFill(ctFill[0]));
		fills.add(new XSSFCellFill(ctFill[1]));

		CTBorder ctBorder = createDefaultBorder();
		borders.add(new XSSFCellBorder(ctBorder));

		CTXf styleXf = createDefaultXf();
		styleXfs.add(styleXf);
		CTXf xf = createDefaultXf();
		xf.setXfId(0);
		xfs.add(xf);
	}

	private static CTXf createDefaultXf() {
		CTXf ctXf = CTXf.Factory.newInstance();
		ctXf.setNumFmtId(0);
		ctXf.setFontId(0);
		ctXf.setFillId(0);
		ctXf.setBorderId(0);
		return ctXf;
	}
	private static CTBorder createDefaultBorder() {
		CTBorder ctBorder = CTBorder.Factory.newInstance();
		ctBorder.addNewBottom();
		ctBorder.addNewTop();
		ctBorder.addNewLeft();
		ctBorder.addNewRight();
		ctBorder.addNewDiagonal();
		return ctBorder;
	}


	private static CTFill[] createDefaultFills() {
		CTFill[] ctFill = new CTFill[]{CTFill.Factory.newInstance(),CTFill.Factory.newInstance()};
		ctFill[0].addNewPatternFill().setPatternType(STPatternType.NONE);
		ctFill[1].addNewPatternFill().setPatternType(STPatternType.DARK_GRAY);
		return ctFill;
	}

	private static XSSFFont createDefaultFont() {
		CTFont ctFont = CTFont.Factory.newInstance();
		XSSFFont xssfFont=new XSSFFont(ctFont, 0);
		xssfFont.setFontHeightInPoints(XSSFFont.DEFAULT_FONT_SIZE);
		xssfFont.setColor(XSSFFont.DEFAULT_FONT_COLOR);//setTheme
		xssfFont.setFontName(XSSFFont.DEFAULT_FONT_NAME);
		xssfFont.setFamily(FontFamily.SWISS);
		xssfFont.setScheme(FontScheme.MINOR);
		return xssfFont;
	}

	protected CTDxf getDxf(int idx) {
		if (dxfs.size()==0) {
			return CTDxf.Factory.newInstance();
		}
		return dxfs.get(idx);
	}

	protected int putDxf(CTDxf dxf) {
		this.dxfs.add(dxf);
		return this.dxfs.size();
	}

	public XSSFCellStyle createCellStyle() {
		CTXf xf = CTXf.Factory.newInstance();
		xf.setNumFmtId(0);
		xf.setFontId(0);
		xf.setFillId(0);
		xf.setBorderId(0);
		xf.setXfId(0);
		int xfSize = styleXfs.size();
		int indexXf = putCellXf(xf);
		return new XSSFCellStyle(indexXf - 1, xfSize - 1, this, theme);
	}

	/**
	 * Finds a font that matches the one with the supplied attributes
	 */
	public XSSFFont findFont(short boldWeight, short color, short fontHeight, String name, boolean italic, boolean strikeout, short typeOffset, byte underline) {
		for (XSSFFont font : fonts) {
			if (	(font.getBoldweight() == boldWeight)
					&& font.getColor() == color
					&& font.getFontHeight() == fontHeight
					&& font.getFontName().equals(name)
					&& font.getItalic() == italic
					&& font.getStrikeout() == strikeout
					&& font.getTypeOffset() == typeOffset
					&& font.getUnderline() == underline)
			{
				return font;
			}
		}
		return null;
	}
}
