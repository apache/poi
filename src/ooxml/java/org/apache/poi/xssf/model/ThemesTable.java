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

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColorScheme;
import org.openxmlformats.schemas.drawingml.x2006.main.ThemeDocument;

/**
 * Class that represents theme of XLSX document. The theme includes specific
 * colors and fonts.
 */
public class ThemesTable extends POIXMLDocumentPart {
    private ThemeDocument theme;

    /**
     * Construct a ThemesTable.
     * @param part A PackagePart.
     * @param rel A PackageRelationship.
     */
    public ThemesTable(PackagePart part, PackageRelationship rel) throws IOException {
        super(part, rel);
        
        try {
           theme = ThemeDocument.Factory.parse(part.getInputStream());
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
     * Convert a theme "index" into a color.
     * @param idx A theme "index"
     * @return The mapped XSSFColor, or null if not mapped.
     */
    public XSSFColor getThemeColor(int idx) {
        // Theme color references are NOT positional indices into the color scheme,
        // i.e. these keys are NOT the same as the order in which theme colors appear
        // in theme1.xml. They are keys to a mapped color.
        CTColorScheme colorScheme = theme.getTheme().getThemeElements().getClrScheme();
        CTColor ctColor;
        switch (idx) {
            case  0: ctColor = colorScheme.getLt1(); break;
            case  1: ctColor = colorScheme.getDk1(); break;
            case  2: ctColor = colorScheme.getLt2(); break;
            case  3: ctColor = colorScheme.getDk2(); break;
            case  4: ctColor = colorScheme.getAccent1(); break;
            case  5: ctColor = colorScheme.getAccent2(); break;
            case  6: ctColor = colorScheme.getAccent3(); break;
            case  7: ctColor = colorScheme.getAccent4(); break;
            case  8: ctColor = colorScheme.getAccent5(); break;
            case  9: ctColor = colorScheme.getAccent6(); break;
            case 10: ctColor = colorScheme.getHlink(); break;
            case 11: ctColor = colorScheme.getFolHlink(); break;
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
        return new XSSFColor(rgb);        
    }
    
    /**
     * If the colour is based on a theme, then inherit
     *  information (currently just colours) from it as
     *  required.
     */
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
}
