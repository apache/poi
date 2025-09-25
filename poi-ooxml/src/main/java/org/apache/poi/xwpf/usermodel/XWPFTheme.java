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

package org.apache.poi.xwpf.usermodel;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.Internal;
import org.apache.poi.xslf.usermodel.XSLFTheme;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBaseStyles;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColorScheme;
import org.openxmlformats.schemas.drawingml.x2006.main.CTOfficeStyleSheet;
import org.openxmlformats.schemas.drawingml.x2006.main.ThemeDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

/**
 * A shared style sheet in a .docx document
 *
 * @since POI 5.2.4
 */
public class XWPFTheme extends POIXMLDocumentPart {
    private CTOfficeStyleSheet _theme;

    /**
     * Construct XWPFStyles from a package part
     *
     * @param part the package part holding the data of the styles
     */
    public XWPFTheme(PackagePart part) {
        super(part);
    }

    /**
     * Construct XWPFStyles from scratch for a new document.
     */
    public XWPFTheme() {
        _theme = CTOfficeStyleSheet.Factory.newInstance();
    }

    @SuppressWarnings("WeakerAccess")
    public void importTheme(XSLFTheme theme) {
        _theme = theme.getXmlObject();
    }

    /**
     *
     * @return name of this theme, e.g. "Office Theme"
     */
    public String getName(){
        return _theme.getName();
    }

    /**
     * Set name of this theme
     *
     * @param name name of this theme
     */
    public void setName(String name){
        _theme.setName(name);
    }

    /**
     * Get a color from the theme's color scheme by name
     *
     * @return a theme color or <code>null</code> if not found
     */
    @Internal
    public CTColor getCTColor(String name) {
        CTBaseStyles elems = _theme.getThemeElements();
        CTColorScheme scheme = (elems == null) ? null : elems.getClrScheme();
        return getMapColor(name, scheme);
    }


    private static CTColor getMapColor(String mapName, CTColorScheme scheme) {
        if (mapName == null || scheme == null) {
            return null;
        }
        switch (mapName) {
            case "accent1":
                return scheme.getAccent1();
            case "accent2":
                return scheme.getAccent2();
            case "accent3":
                return scheme.getAccent3();
            case "accent4":
                return scheme.getAccent4();
            case "accent5":
                return scheme.getAccent5();
            case "accent6":
                return scheme.getAccent6();
            case "dk1":
                return scheme.getDk1();
            case "dk2":
                return scheme.getDk2();
            case "folHlink":
                return scheme.getFolHlink();
            case "hlink":
                return scheme.getHlink();
            case "lt1":
                return scheme.getLt1();
            case "lt2":
                return scheme.getLt2();
            default:
                return null;
        }
    }

    /**
     * @return typeface of the major font to use in a document.
     * Typically, the major font is used for heading areas of a document.
     *
     */
    @SuppressWarnings("WeakerAccess")
    public String getMajorFont(){
        return _theme.getThemeElements().getFontScheme().getMajorFont().getLatin().getTypeface();
    }

    /**
     * @return typeface of the minor font to use in a document.
     * Typically, the minor font is used for normal text or paragraph areas.
     *
     */
    @SuppressWarnings("WeakerAccess")
    public String getMinorFont(){
        return _theme.getThemeElements().getFontScheme().getMinorFont().getLatin().getTypeface();
    }

    /**
     * Read document
     */
    @Override
    protected void onDocumentRead() throws IOException {
        ThemeDocument themeDoc;
        try (InputStream is = getPackagePart().getInputStream()) {
            themeDoc = ThemeDocument.Factory.parse(is, DEFAULT_XML_OPTIONS);
            setTheme(themeDoc.getTheme());
        } catch (XmlException e) {
            throw new POIXMLException("Unable to read theme", e);
        }
    }

    @Override
    protected void commit() throws IOException {
        if (_theme == null) {
            throw new IOException("Unable to write out theme that was never read in!");
        }

        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(XWPFRelation.NS_DRAWINGML, "theme"));
        PackagePart part = getPackagePart();
        try (OutputStream out = part.getOutputStream()) {
            _theme.save(out, xmlOptions);
        }
    }

    public void setTheme(CTOfficeStyleSheet theme) {
        _theme = theme;
    }
}
