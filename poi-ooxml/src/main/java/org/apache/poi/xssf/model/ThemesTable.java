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
import java.io.OutputStream;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xssf.usermodel.IndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColorScheme;
import org.openxmlformats.schemas.drawingml.x2006.main.ThemeDocument;

/**
 * Class that represents theme of XLSX document. The theme includes specific
 * colors and fonts.
 */
public class ThemesTable extends POIXMLDocumentPart implements Themes {
   public enum ThemeElement {
       LT1(0, "Lt1"),
       DK1(1,"Dk1"),
       LT2(2,"Lt2"),
       DK2(3,"Dk2"),
       ACCENT1(4,"Accent1"),
       ACCENT2(5,"Accent2"),
       ACCENT3(6,"Accent3"),
       ACCENT4(7,"Accent4"),
       ACCENT5(8,"Accent5"),
       ACCENT6(9,"Accent6"),
       HLINK(10,"Hlink"),
       FOLHLINK(11,"FolHlink"),
       UNKNOWN(-1,null);
       
       public static ThemeElement byId(int idx) {
           if (idx >= values().length || idx < 0) return UNKNOWN;
           return values()[idx];
       }
       private ThemeElement(int idx, String name) {
           this.idx = idx; this.name = name;
       }
       public final int idx;
       public final String name;
   }

    private IndexedColorMap colorMap;
    private ThemeDocument theme;

    /**
     * Create a new, empty ThemesTable
     */
    public ThemesTable() {
        super();
        theme = ThemeDocument.Factory.newInstance();
        theme.addNewTheme().addNewThemeElements();
    }
    
    /**
     * Construct a ThemesTable.
     * @param part A PackagePart.
     * 
     * @since POI 3.14-Beta1
     */
    public ThemesTable(PackagePart part) throws IOException {
        super(part);
        
        try {
           theme = ThemeDocument.Factory.parse(part.getInputStream(), DEFAULT_XML_OPTIONS);
        } catch(XmlException e) {
           throw new IOException(e.getLocalizedMessage(), e);
        }
    }
    
    /**
     * Construct a ThemesTable from an existing ThemeDocument.
     * @param theme A ThemeDocument.
     */
    public ThemesTable(ThemeDocument theme) {
        this.theme = theme;
    }

    /**
     * called from {@link StylesTable} when setting theme, used to adjust colors if a custom indexed mapping is defined
     * @param colorMap
     */
    protected void setColorMap(IndexedColorMap colorMap) {
        this.colorMap = colorMap;
    }
    
    /**
     * Convert a theme "index" (as used by fonts etc) into a color.
     * @param idx A theme "index"
     * @return The mapped XSSFColor, or null if not mapped.
     */
    @Override
    public XSSFColor getThemeColor(int idx) {
        // Theme color references are NOT positional indices into the color scheme,
        // i.e. these keys are NOT the same as the order in which theme colors appear
        // in theme1.xml. They are keys to a mapped color.
        CTColorScheme colorScheme = theme.getTheme().getThemeElements().getClrScheme();
        CTColor ctColor;
        switch (ThemeElement.byId(idx)) {
            case LT1: ctColor = colorScheme.getLt1(); break;
            case DK1: ctColor = colorScheme.getDk1(); break;
            case LT2: ctColor = colorScheme.getLt2(); break;
            case DK2: ctColor = colorScheme.getDk2(); break;
            case ACCENT1: ctColor = colorScheme.getAccent1(); break;
            case ACCENT2: ctColor = colorScheme.getAccent2(); break;
            case ACCENT3: ctColor = colorScheme.getAccent3(); break;
            case ACCENT4: ctColor = colorScheme.getAccent4(); break;
            case ACCENT5: ctColor = colorScheme.getAccent5(); break;
            case ACCENT6: ctColor = colorScheme.getAccent6(); break;
            case HLINK:   ctColor = colorScheme.getHlink();   break;
            case FOLHLINK:ctColor = colorScheme.getFolHlink();break;
            default: return null;
        }

        byte[] rgb = null;
        if (ctColor.isSetSrgbClr()) {
            // Color is a regular one
            rgb = ctColor.getSrgbClr().getVal();
        } else if (ctColor.isSetSysClr()) {
            // Color is a tint of white or black
            rgb = ctColor.getSysClr().getLastClr();
        } else {
            return null;
        }
        return new XSSFColor(rgb, colorMap);        
    }
    
    /**
     * If the colour is based on a theme, then inherit
     *  information (currently just colours) from it as
     *  required.
     */
    @Override
    public void inheritFromThemeAsRequired(XSSFColor color) {
       if(color == null) {
          // Nothing for us to do
          return;
       }
       if(! color.getCTColor().isSetTheme()) {
          // No theme set, nothing to do
          return;
       }

       // Get the theme colour
       XSSFColor themeColor = getThemeColor(color.getTheme());
       // Set the raw colour, not the adjusted one
       // Do a raw set, no adjusting at the XSSFColor layer either
       color.getCTColor().setRgb(themeColor.getCTColor().getRgb());

       // All done
    }
    
    /**
     * Write this table out as XML.
     * 
     * @param out The stream to write to.
     * @throws IOException if an error occurs while writing.
     */
    public void writeTo(OutputStream out) throws IOException {
        theme.save(out, DEFAULT_XML_OPTIONS);
    }

    @Override
    protected void commit() throws IOException {
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        writeTo(out);
        out.close();
    }
}
