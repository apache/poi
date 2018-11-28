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
package org.apache.poi.xslf.usermodel;

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBaseStyles;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColorMapping;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColorScheme;
import org.openxmlformats.schemas.drawingml.x2006.main.CTOfficeStyleSheet;
import org.openxmlformats.schemas.drawingml.x2006.main.ThemeDocument;

/**
 * A shared style sheet in a .pptx slide show
 *
 * @author Yegor Kozlov
 */
@Beta
public class XSLFTheme extends POIXMLDocumentPart {
    private CTOfficeStyleSheet _theme;
    private Map<String, CTColor> _schemeColors;

    XSLFTheme() {
        super();
        _theme = CTOfficeStyleSheet.Factory.newInstance();
    }

    /**
     * @since POI 3.14-Beta1
     */
    public XSLFTheme(PackagePart part) throws IOException, XmlException {
        super(part);
        ThemeDocument doc =
            ThemeDocument.Factory.parse(getPackagePart().getInputStream(), DEFAULT_XML_OPTIONS);
        _theme = doc.getTheme();
        initialize();
    }

    @SuppressWarnings("WeakerAccess")
    public void importTheme(XSLFTheme theme) {
        _theme = theme.getXmlObject();
        _schemeColors = theme._schemeColors;
    }

    private void initialize(){
    	CTBaseStyles elems = _theme.getThemeElements();
    	CTColorScheme scheme = elems.getClrScheme();
    	// The color scheme is responsible for defining a list of twelve colors.
    	_schemeColors = new HashMap<>(12);
    	for(XmlObject o : scheme.selectPath("*")){
    		CTColor c = (CTColor)o;
    		String name = c.getDomNode().getLocalName();
    		_schemeColors.put(name, c);
    	}
     }

    /**
     * re-map colors
     *
     * @param cmap color map defined in the master slide referencing this theme
     */
    void initColorMap(CTColorMapping cmap) {
        _schemeColors.put("bg1", _schemeColors.get(cmap.getBg1().toString()));
        _schemeColors.put("bg2", _schemeColors.get(cmap.getBg2().toString()));
        _schemeColors.put("tx1", _schemeColors.get(cmap.getTx1().toString()));
        _schemeColors.put("tx2", _schemeColors.get(cmap.getTx2().toString()));
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
    public CTColor getCTColor(String name){
    	return _schemeColors.get(name);
    }

    /**
     * While developing only!
     */
    @Internal
    public CTOfficeStyleSheet getXmlObject() {
        return _theme;
    }

    @Override
    protected final void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(
            new QName(XSLFRelation.NS_DRAWINGML, "theme"));

        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        getXmlObject().save(out, xmlOptions);
        out.close();
    }

    /**
     * @return typeface of the major font to use in a document.
     * Typically the major font is used for heading areas of a document.
     *
     */
    @SuppressWarnings("WeakerAccess")
    public String getMajorFont(){
        return _theme.getThemeElements().getFontScheme().getMajorFont().getLatin().getTypeface();
    }

    /**
     * @return typeface of the minor font to use in a document.
     * Typically the monor font is used for normal text or paragraph areas.
     *
     */
    @SuppressWarnings("WeakerAccess")
    public String getMinorFont(){
        return _theme.getThemeElements().getFontScheme().getMinorFont().getLatin().getTypeface();
    }
}
