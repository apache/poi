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

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.Internal;
import org.apache.poi.xssf.usermodel.CustomIndexedColorMap;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.IndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFBuiltinTableStyle;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFactory;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFTableStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;

/**
 * Table of styles shared across all sheets in a workbook.
 */
public class StylesTable extends POIXMLDocumentPart implements Styles {
    private final SortedMap<Short, String> numberFormats = new TreeMap<>();
    private final List<XSSFFont> fonts = new ArrayList<>();
    private final List<XSSFCellFill> fills = new ArrayList<>();
    private final List<XSSFCellBorder> borders = new ArrayList<>();
    private final List<CTXf> styleXfs = new ArrayList<>();
    private final List<CTXf> xfs = new ArrayList<>();

    private final List<CTDxf> dxfs = new ArrayList<>();
    private final Map<String, TableStyle> tableStyles = new HashMap<>();
    
    private IndexedColorMap indexedColors = new DefaultIndexedColorMap();
    
    /**
     * The first style id available for use as a custom style
     */
    public static final int FIRST_CUSTOM_STYLE_ID = BuiltinFormats.FIRST_USER_DEFINED_FORMAT_INDEX + 1;
    // Is this right? Number formats (XSSFDataFormat) and cell styles (XSSFCellStyle) are different. What's up with the plus 1?
    private static final int MAXIMUM_STYLE_ID = SpreadsheetVersion.EXCEL2007.getMaxCellStyles();
    
    private static final short FIRST_USER_DEFINED_NUMBER_FORMAT_ID = BuiltinFormats.FIRST_USER_DEFINED_FORMAT_INDEX;
    /**
     * Depending on the version of Excel, the maximum number of number formats in a workbook is between 200 and 250
     * See https://support.office.com/en-us/article/excel-specifications-and-limits-1672b34d-7043-467e-8e27-269d656771c3
     * POI defaults this limit to 250, but can be increased or decreased on a per-StylesTable basis with
     * {@link #setMaxNumberOfDataFormats(int)} if needed.
     */
    private int MAXIMUM_NUMBER_OF_DATA_FORMATS = 250;
    
    /**
     * Changes the maximum number of data formats that may be in a style table
     *
     * @param num the upper limit on number of data formats in the styles table when adding new data formats
     * @throws IllegalArgumentException if <code>num</code> < 0
     * @throws IllegalStateException if <code>num</code> < current number of data formats in the style table.
     * Data formats must be explicitly removed before the limit can be decreased.
     */
    public void setMaxNumberOfDataFormats(int num) {
        if (num < getNumDataFormats()) {
            if (num < 0) {
                throw new IllegalArgumentException("Maximum Number of Data Formats must be greater than or equal to 0");
            } else {
                throw new IllegalStateException("Cannot set the maximum number of data formats less than the current quantity." +
                        "Data formats must be explicitly removed (via StylesTable.removeNumberFormat) before the limit can be decreased.");
            }
        }
        MAXIMUM_NUMBER_OF_DATA_FORMATS = num;
    }
    
    /**
     * Get the upper limit on the number of data formats that has been set for the style table.
     * To get the current number of data formats in use, use {@link #getNumDataFormats()}.
     *
     * @return the maximum number of data formats allowed in the workbook
     */
    public int getMaxNumberOfDataFormats() {
        return MAXIMUM_NUMBER_OF_DATA_FORMATS;
    }

    private StyleSheetDocument doc;
    private XSSFWorkbook workbook;
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

    /**
     * @since POI 3.14-Beta1
     */
    public StylesTable(PackagePart part) throws IOException {
        super(part);
        readFrom(part.getInputStream());
    }
    
    public void setWorkbook(XSSFWorkbook wb) {
        this.workbook = wb;
    }

    /**
     * Get the current Workbook's theme table, or null if the
     *  Workbook lacks any themes.
     * <p>Use {@link #ensureThemesTable()} to have a themes table
     *  created if needed
     */
    public ThemesTable getTheme() {
        return theme;
    }

    public void setTheme(ThemesTable theme) {
        this.theme = theme;

        if (theme != null) theme.setColorMap(getIndexedColors());
        
        // Pass the themes table along to things which need to 
        //  know about it, but have already been created by now
        for(XSSFFont font : fonts) {
            font.setThemesTable(theme);
        }
        for(XSSFCellBorder border : borders) {
            border.setThemesTable(theme);
        }
    }
    
    /**
     * If there isn't currently a {@link ThemesTable} for the
     *  current Workbook, then creates one and sets it up.
     * After this, calls to {@link #getTheme()} won't give null
     */
    public void ensureThemesTable() {
        if (theme != null) return;

        setTheme((ThemesTable)workbook.createRelationship(XSSFRelation.THEME, XSSFFactory.getInstance()));
    }

    /**
     * Read this shared styles table from an XML file.
     *
     * @param is The input stream containing the XML document.
     * @throws IOException if an error occurs while reading.
     */
    public void readFrom(InputStream is) throws IOException {
        try {
            doc = StyleSheetDocument.Factory.parse(is, DEFAULT_XML_OPTIONS);

            CTStylesheet styleSheet = doc.getStyleSheet();

            // Grab all the different bits we care about
            
            // keep this first, as some constructors below want it
            IndexedColorMap customColors = CustomIndexedColorMap.fromColors(styleSheet.getColors());
            if (customColors != null) indexedColors = customColors;
            
            CTNumFmts ctfmts = styleSheet.getNumFmts();
            if( ctfmts != null){
                for (CTNumFmt nfmt : ctfmts.getNumFmtArray()) {
                    short formatId = (short)nfmt.getNumFmtId();
                    numberFormats.put(formatId, nfmt.getFormatCode());
                }
            }

            CTFonts ctfonts = styleSheet.getFonts();
            if(ctfonts != null){
                int idx = 0;
                for (CTFont font : ctfonts.getFontArray()) {
                    // Create the font and save it. Themes Table supplied later
                    XSSFFont f = new XSSFFont(font, idx, indexedColors);
                    fonts.add(f);
                    idx++;
                }
            }
            CTFills ctfills = styleSheet.getFills();
            if(ctfills != null){
                for (CTFill fill : ctfills.getFillArray()) {
                    fills.add(new XSSFCellFill(fill, indexedColors));
                }
            }

            CTBorders ctborders = styleSheet.getBorders();
            if(ctborders != null) {
                for (CTBorder border : ctborders.getBorderArray()) {
                    borders.add(new XSSFCellBorder(border, indexedColors));
                }
            }

            CTCellXfs cellXfs = styleSheet.getCellXfs();
            if(cellXfs != null) xfs.addAll(Arrays.asList(cellXfs.getXfArray()));

            CTCellStyleXfs cellStyleXfs = styleSheet.getCellStyleXfs();
            if(cellStyleXfs != null) styleXfs.addAll(Arrays.asList(cellStyleXfs.getXfArray()));

            CTDxfs styleDxfs = styleSheet.getDxfs();
            if(styleDxfs != null) dxfs.addAll(Arrays.asList(styleDxfs.getDxfArray()));

            CTTableStyles ctTableStyles = styleSheet.getTableStyles();
            if (ctTableStyles != null) {
                int idx = 0;
                for (CTTableStyle style : Arrays.asList(ctTableStyles.getTableStyleArray())) {
                    tableStyles.put(style.getName(), new XSSFTableStyle(idx, styleDxfs, style, indexedColors));
                    idx++;
                }
            }
            
        } catch (XmlException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }

    // ===========================================================
    //  Start of style related getters and setters
    // ===========================================================

    /**
     * Get number format string given its id
     * 
     * @param fmtId number format id
     * @return number format code
     */
    @Override
    public String getNumberFormatAt(short fmtId) {
        return numberFormats.get(fmtId);
    }
    
    private short getNumberFormatId(String fmt) {
        // Find the key, and return that
        for (Entry<Short,String> numFmt : numberFormats.entrySet()) {
            if(numFmt.getValue().equals(fmt)) {
                return numFmt.getKey();
            }
        }
        throw new IllegalStateException("Number format not in style table: " + fmt);
    }

    /**
     * Puts <code>fmt</code> in the numberFormats map if the format is not
     * already in the the number format style table.
     * Does nothing if <code>fmt</code> is already in number format style table.
     *
     * @param fmt the number format to add to number format style table
     * @return the index of <code>fmt</code> in the number format style table
     * @throws IllegalStateException if adding the number format to the styles table
     * would exceed the {@link #MAXIMUM_NUMBER_OF_DATA_FORMATS} allowed.
     */
    @Override
    public int putNumberFormat(String fmt) {
        // Check if number format already exists
        if (numberFormats.containsValue(fmt)) {
            try {
                return getNumberFormatId(fmt);
            } catch (final IllegalStateException e) {
                throw new IllegalStateException("Found the format, but couldn't figure out where - should never happen!");
            }
        }
        
        
        if (numberFormats.size() >= MAXIMUM_NUMBER_OF_DATA_FORMATS) {
            throw new IllegalStateException("The maximum number of Data Formats was exceeded. " +
                    "You can define up to " + MAXIMUM_NUMBER_OF_DATA_FORMATS + " formats in a .xlsx Workbook.");
        }

        // Find a spare key, and add that
        final short formatIndex;
        if (numberFormats.isEmpty()) {
            formatIndex = FIRST_USER_DEFINED_NUMBER_FORMAT_ID;
        }
        else {
            // get next-available numberFormat index.
            // Assumption: gaps in number format ids are acceptable
            // to catch arithmetic overflow, nextKey's data type
            // must match numberFormat's key data type
            short nextKey = (short) (numberFormats.lastKey() + 1);
            if (nextKey < 0) {
                throw new IllegalStateException(
                        "Cowardly avoiding creating a number format with a negative id." +
                        "This is probably due to arithmetic overflow.");
            }
            formatIndex = (short) Math.max(nextKey, FIRST_USER_DEFINED_NUMBER_FORMAT_ID);
        }
        
        numberFormats.put(formatIndex, fmt);
        return formatIndex;
    }
    
    
    /**
     * Add a number format with a specific ID into the numberFormats map.
     * If a format with the same ID already exists, overwrite the format code
     * with <code>fmt</code>
     * This may be used to override built-in number formats.
     *
     * @param index the number format ID
     * @param fmt the number format code
     */
    @Override
    public void putNumberFormat(short index, String fmt) {
        numberFormats.put(index, fmt);
    }
    
    /**
     * Remove a number format from the style table if it exists.
     * All cell styles with this number format will be modified to use the default number format.
     * 
     * @param index the number format id to remove
     * @return true if the number format was removed
     */
    @Override
    public boolean removeNumberFormat(short index) {
        String fmt = numberFormats.remove(index);
        boolean removed = (fmt != null);
        if (removed) {
            for (final CTXf style : xfs) {
                if (style.isSetNumFmtId() && style.getNumFmtId() == index) {
                    style.unsetApplyNumberFormat();
                    style.unsetNumFmtId();
                }
            }
        }
        return removed;
    }
    
    /**
     * Remove a number format from the style table if it exists
     * All cell styles with this number format will be modified to use the default number format
     * 
     * @param fmt the number format to remove
     * @return true if the number format was removed
     */
    @Override
    public boolean removeNumberFormat(String fmt) {
        short id = getNumberFormatId(fmt);
        return removeNumberFormat(id);
    }

    @Override
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
    @Override
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

    @Override
    public int putFont(XSSFFont font) {
        return putFont(font, false);
    }

    /**
     *
     * @param idx style index
     * @return XSSFCellStyle or null if idx is out of bounds for xfs array
     */
    @Override
    public XSSFCellStyle getStyleAt(int idx) {
        int styleXfId = 0;

        if (idx < 0 || idx >= xfs.size()) {
            //BUG-60343
            return null;
        }
        // 0 is the empty default
        if(xfs.get(idx).getXfId() > 0) {
            styleXfId = (int) xfs.get(idx).getXfId();
        }

        return new XSSFCellStyle(idx, styleXfId, this, theme);
    }

    @Override
    public int putStyle(XSSFCellStyle style) {
        CTXf mainXF = style.getCoreXf();

        if(! xfs.contains(mainXF)) {
            xfs.add(mainXF);
        }
        return xfs.indexOf(mainXF);
    }

    @Override
    public XSSFCellBorder getBorderAt(int idx) {
        return borders.get(idx);
    }

    /**
     * Adds a border to the border style table if it isn't already in the style table
     * Does nothing if border is already in borders style table
     *
     * @param border border to add
     * @return the index of the added border
     */
    @Override
    public int putBorder(XSSFCellBorder border) {
        int idx = borders.indexOf(border);
        if (idx != -1) {
            return idx;
        }
        borders.add(border);
        border.setThemesTable(theme);
        return borders.size() - 1;
    }

    @Override
    public XSSFCellFill getFillAt(int idx) {
        return fills.get(idx);
    }

    public List<XSSFCellBorder> getBorders(){
        return Collections.unmodifiableList(borders);
    }

    public List<XSSFCellFill> getFills(){
        return Collections.unmodifiableList(fills);
    }

    public List<XSSFFont> getFonts(){
        return Collections.unmodifiableList(fonts);
    }

    public Map<Short, String> getNumberFormats(){
        return Collections.unmodifiableMap(numberFormats);
    }

    /**
     * Adds a fill to the fill style table if it isn't already in the style table
     * Does nothing if fill is already in fill style table
     *
     * @param fill fill to add
     * @return the index of the added fill
     */
    @Override
    public int putFill(XSSFCellFill fill) {
        int idx = fills.indexOf(fill);
        if (idx != -1) {
            return idx;
        }
        fills.add(fill);
        return fills.size() - 1;
    }

    @Internal
    public CTXf getCellXfAt(int idx) {
        return xfs.get(idx);
    }
    
    /**
     * Adds a cell to the styles table.
     * Does not check for duplicates.
     * 
     * @param cellXf the cell to add to the styles table
     * @return the added cell ID in the style table
     */
    @Internal
    public int putCellXf(CTXf cellXf) {
        xfs.add(cellXf);
        return xfs.size();
    }
    
    @Internal
    public void replaceCellXfAt(int idx, CTXf cellXf) {
        xfs.set(idx, cellXf);
    }

    @Internal
    public CTXf getCellStyleXfAt(int idx) {
        try {
            return styleXfs.get(idx);
        }
        catch (final IndexOutOfBoundsException e) {
            return null;
        }
    }
    
    /**
     * Adds a cell style to the styles table.
     * Does not check for duplicates.
     * 
     * @param cellStyleXf the cell style to add to the styles table
     * @return the cell style ID in the style table
     */
    @Internal
    public int putCellStyleXf(CTXf cellStyleXf) {
        styleXfs.add(cellStyleXf);
        // TODO: check for duplicate
        return styleXfs.size();
    }
    
    @Internal
    protected void replaceCellStyleXfAt(int idx, CTXf cellStyleXf) {
        styleXfs.set(idx, cellStyleXf);
    }

    /**
     * get the size of cell styles
     */
    @Override
    public int getNumCellStyles() {
        // Each cell style has a unique xfs entry
        // Several might share the same styleXfs entry
        return xfs.size();
    }

    /**
     * @return number of data formats in the styles table
     */
    @Override
    public int getNumDataFormats() {
        return numberFormats.size();
    }

    /**
     * For unit testing only
     */
    @Internal
    /*package*/ int _getXfsSize() {
        return xfs.size();
    }
    /**
     * For unit testing only
     */
    @Internal
    public int _getStyleXfsSize() {
        return styleXfs.size();
    }
    
    /**
     * For unit testing only!
     */
    @Internal
    public CTStylesheet getCTStylesheet() {
        return doc.getStyleSheet();
    }
    
    @Internal
    public int _getDXfsSize() {
        return dxfs.size();
    }


    /**
     * Write this table out as XML.
     *
     * @param out The stream to write to.
     * @throws IOException if an error occurs while writing.
     */
    public void writeTo(OutputStream out) throws IOException {
        // Work on the current one
        // Need to do this, as we don't handle
        //  all the possible entries yet
        CTStylesheet styleSheet = doc.getStyleSheet();

        // Formats
        CTNumFmts formats = CTNumFmts.Factory.newInstance();
        formats.setCount(numberFormats.size());
        for (final Entry<Short, String> entry : numberFormats.entrySet()) {
            CTNumFmt ctFmt = formats.addNewNumFmt();
            ctFmt.setNumFmtId(entry.getKey());
            ctFmt.setFormatCode(entry.getValue());
        }
        styleSheet.setNumFmts(formats);

        int idx;
        // Fonts
        CTFonts ctFonts = styleSheet.getFonts();
        if (ctFonts == null) {
            ctFonts = CTFonts.Factory.newInstance();
        }
        ctFonts.setCount(fonts.size());
        CTFont[] ctfnt = new CTFont[fonts.size()];
        idx = 0;
        for(XSSFFont f : fonts) ctfnt[idx++] = f.getCTFont();
        ctFonts.setFontArray(ctfnt);
        styleSheet.setFonts(ctFonts);

        // Fills
        CTFills ctFills = styleSheet.getFills();
        if (ctFills == null) {
            ctFills = CTFills.Factory.newInstance();
        }
        ctFills.setCount(fills.size());
        CTFill[] ctf = new CTFill[fills.size()];
        idx = 0;
        for(XSSFCellFill f : fills) ctf[idx++] = f.getCTFill();
        ctFills.setFillArray(ctf);
        styleSheet.setFills(ctFills);

        // Borders
        CTBorders ctBorders = styleSheet.getBorders();
        if (ctBorders == null) {
            ctBorders = CTBorders.Factory.newInstance();
        }
        ctBorders.setCount(borders.size());
        CTBorder[] ctb = new CTBorder[borders.size()];
        idx = 0;
        for(XSSFCellBorder b : borders) ctb[idx++] = b.getCTBorder();
        ctBorders.setBorderArray(ctb);
        styleSheet.setBorders(ctBorders);

        // Xfs
        if(xfs.size() > 0) {
            CTCellXfs ctXfs = styleSheet.getCellXfs();
            if (ctXfs == null) {
                ctXfs = CTCellXfs.Factory.newInstance();
            }
            ctXfs.setCount(xfs.size());
            ctXfs.setXfArray(
                    xfs.toArray(new CTXf[xfs.size()])
            );
            styleSheet.setCellXfs(ctXfs);
        }

        // Style xfs
        if(styleXfs.size() > 0) {
            CTCellStyleXfs ctSXfs = styleSheet.getCellStyleXfs();
            if (ctSXfs == null) {
                ctSXfs = CTCellStyleXfs.Factory.newInstance();
            }
            ctSXfs.setCount(styleXfs.size());
            ctSXfs.setXfArray(
                    styleXfs.toArray(new CTXf[styleXfs.size()])
            );
            styleSheet.setCellStyleXfs(ctSXfs);
        }

        // Style dxfs
        if(dxfs.size() > 0) {
            CTDxfs ctDxfs = styleSheet.getDxfs();
            if (ctDxfs == null) {
                ctDxfs = CTDxfs.Factory.newInstance();
            }
            ctDxfs.setCount(dxfs.size());
            ctDxfs.setDxfArray(dxfs.toArray(new CTDxf[dxfs.size()]));
            styleSheet.setDxfs(ctDxfs);
        }

        // Save
        doc.save(out, DEFAULT_XML_OPTIONS);
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
        fills.add(new XSSFCellFill(ctFill[0], indexedColors));
        fills.add(new XSSFCellFill(ctFill[1], indexedColors));

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
        XSSFFont xssfFont=new XSSFFont(ctFont, 0, null);
        xssfFont.setFontHeightInPoints(XSSFFont.DEFAULT_FONT_SIZE);
        xssfFont.setColor(XSSFFont.DEFAULT_FONT_COLOR);//setTheme
        xssfFont.setFontName(XSSFFont.DEFAULT_FONT_NAME);
        xssfFont.setFamily(FontFamily.SWISS);
        xssfFont.setScheme(FontScheme.MINOR);
        return xssfFont;
    }

    @Internal
    public CTDxf getDxfAt(int idx) {
        return dxfs.get(idx);
    }

    /**
     * Adds a Dxf to the style table
     * Does not check for duplicates.
     *
     * @param dxf the Dxf to add
     * @return added dxf ID in the style table
     */
    @Internal
    public int putDxf(CTDxf dxf) {
        this.dxfs.add(dxf);
        return this.dxfs.size();
    }
    
    /**
     * NOTE: this only returns explicitly defined styles
     * @param name of the table style
     * @return defined style, or null if not explicitly defined
     * 
     * @since 3.17 beta 1
     */
    public TableStyle getExplicitTableStyle(String name) {
        return tableStyles.get(name);
    }
    
    /**
     * @return names of all explicitly defined table styles in the workbook
     * @since 3.17 beta 1
     */
    public Set<String> getExplicitTableStyleNames() {
        return tableStyles.keySet();
    }
    
    /**
     * @param name of the table style
     * @return defined style, either explicit or built-in, or null if not found
     * 
     * @since 3.17 beta 1
     */
    public TableStyle getTableStyle(String name) {
        if (name == null) return null;
        try {
            return XSSFBuiltinTableStyle.valueOf(name).getStyle();
        } catch (IllegalArgumentException e) {
            return getExplicitTableStyle(name);
        }
    }
    
    /**
     * Create a cell style in this style table.
     * Note - End users probably want to call {@link XSSFWorkbook#createCellStyle()}
     * rather than working with the styles table directly.
     * @throws IllegalStateException if the maximum number of cell styles has been reached. 
     */
    public XSSFCellStyle createCellStyle() {
        if (getNumCellStyles() > MAXIMUM_STYLE_ID) {
            throw new IllegalStateException("The maximum number of Cell Styles was exceeded. " +
                      "You can define up to " + MAXIMUM_STYLE_ID + " style in a .xlsx Workbook");
        }

        int xfSize = styleXfs.size();
        CTXf xf = CTXf.Factory.newInstance();
        xf.setNumFmtId(0);
        xf.setFontId(0);
        xf.setFillId(0);
        xf.setBorderId(0);
        xf.setXfId(0);
        int indexXf = putCellXf(xf);
        return new XSSFCellStyle(indexXf - 1, xfSize - 1, this, theme);
    }
    
    /**
     * Finds a font that matches the one with the supplied attributes,
     * where color is the indexed-value, not the actual color.
     */
    public XSSFFont findFont(boolean bold, short color, short fontHeight, String name, boolean italic, boolean strikeout, short typeOffset, byte underline) {
        for (XSSFFont font : fonts) {
            if (    (font.getBold() == bold)
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
    
    /**
     * Finds a font that matches the one with the supplied attributes,
     * where color is the actual Color-value, not the indexed color
     */
    public XSSFFont findFont(boolean bold, Color color, short fontHeight, String name, boolean italic, boolean strikeout, short typeOffset, byte underline) {
        for (XSSFFont font : fonts) {
            if (    (font.getBold() == bold)
                    && font.getXSSFColor().equals(color)
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

    /**
     * @return default or custom indexed color to RGB mapping
     */
    public IndexedColorMap getIndexedColors() {
        return indexedColors;
    }
}
