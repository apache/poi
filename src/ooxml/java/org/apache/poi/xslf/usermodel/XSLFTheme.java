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

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBaseStyles;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColorScheme;
import org.openxmlformats.schemas.drawingml.x2006.main.ThemeDocument;
import org.openxmlformats.schemas.drawingml.x2006.main.CTOfficeStyleSheet;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetColor;

import javax.xml.namespace.QName;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.HashMap;

@Beta
public class XSLFTheme extends POIXMLDocumentPart {
    private CTOfficeStyleSheet _theme;
    private Map<String, XSLFColor> _schemeColors;
    
    XSLFTheme() {
        super();
        _theme = CTOfficeStyleSheet.Factory.newInstance();
    }

    public XSLFTheme(PackagePart part, PackageRelationship rel) throws IOException, XmlException {
        super(part, rel);
        ThemeDocument doc =
            ThemeDocument.Factory.parse(getPackagePart().getInputStream());
        _theme = doc.getTheme();
        initialize();
    }

    private void initialize(){
    	CTBaseStyles elems = _theme.getThemeElements();
    	CTColorScheme scheme = elems.getClrScheme();
    	// The color scheme is responsible for defining a list of twelve colors. 
    	_schemeColors = new HashMap<String, XSLFColor>(12);
    	for(XmlObject o : scheme.selectPath("*")){
    		CTColor c = (CTColor)o;
    		String name = c.getDomNode().getLocalName();
    		_schemeColors.put(name, new XSLFColor(c));
    	}
    	_schemeColors.put("bg1", _schemeColors.get("lt1"));
    	_schemeColors.put("bg2", _schemeColors.get("lt2"));
        _schemeColors.put("tx1", _schemeColors.get("dk1"));
        _schemeColors.put("tx2", _schemeColors.get("dk2"));
    }

    public String getName(){
        return _theme.getName();
    }

    public void setName(String name){
        _theme.setName(name);
    }

    /**
     * Get a color from the theme's color scheme by name
     * 
     * @return a theme color or <code>null</code> if not found
     */
    public XSLFColor getColor(String name){
    	return _schemeColors.get(name);
    }
    
    Color getSchemeColor(CTSchemeColor schemeColor){
        String colorRef = schemeColor.getVal().toString();
        int alpha = 0xFF;
        if(schemeColor.sizeOfAlphaArray() > 0){
            int aval = schemeColor.getAlphaArray(0).getVal();
            alpha =  Math.round(255 * aval / 100000f);
        }
        Color themeColor = _schemeColors.get(colorRef).getColor(alpha);

        int lumMod = 100, lumOff = 0;
        if (schemeColor.sizeOfLumModArray() > 0) {
            lumMod = schemeColor.getLumModArray(0).getVal() / 1000;
        }
        if (schemeColor.sizeOfLumOffArray() > 0) {
            lumOff = schemeColor.getLumOffArray(0).getVal() / 1000;
        }
        if(schemeColor.sizeOfShadeArray() > 0) {
            lumMod = schemeColor.getShadeArray(0).getVal() / 1000;
        }
        Color color = modulateLuminanace(themeColor, lumMod, lumOff);

        if(schemeColor.sizeOfTintArray() > 0) {
            float tint = schemeColor.getTintArray(0).getVal() / 100000f;
            int red = Math.round(tint * themeColor.getRed() + (1 - tint) * 255);
            int green = Math.round(tint * themeColor.getGreen() + (1 - tint) * 255);
            int blue = Math.round(tint * themeColor.getBlue() + (1 - tint) * 255);
            color = new Color(red, green, blue);
        }

        return color;
    }

    /**
     * TODO get rid of code duplication. Re-write to use xpath instead of beans
     */
    Color getPresetColor(CTPresetColor presetColor){
        String colorName = presetColor.getVal().toString();
        Color color;
        try {
            color = (Color)Color.class.getField(colorName).get(null);
        } catch (Exception e){
            color = Color.black;
        }
        if(presetColor.sizeOfAlphaArray() > 0){
            int aval = presetColor.getAlphaArray(0).getVal();
            int alpha =  Math.round(255 * aval / 100000f);
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha); 
        }

        int lumMod = 100, lumOff = 0;
        if (presetColor.sizeOfLumModArray() > 0) {
            lumMod = presetColor.getLumModArray(0).getVal() / 1000;
        }
        if (presetColor.sizeOfLumOffArray() > 0) {
            lumOff = presetColor.getLumOffArray(0).getVal() / 1000;
        }
        if(presetColor.sizeOfShadeArray() > 0) {
            lumMod = presetColor.getShadeArray(0).getVal() / 1000;
        }
        color = modulateLuminanace(color, lumMod, lumOff);

        if(presetColor.sizeOfTintArray() > 0) {
            float tint = presetColor.getTintArray(0).getVal() / 100000f;
            int red = Math.round(tint * color.getRed() + (1 - tint) * 255);
            int green = Math.round(tint * color.getGreen() + (1 - tint) * 255);
            int blue = Math.round(tint * color.getBlue() + (1 - tint) * 255);
            color = new Color(red, green, blue);
        }

        return color;
    }

    public Color brighter(Color color, double tint) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        /* From 2D group:
         * 1. black.brighter() should return grey
         * 2. applying brighter to blue will always return blue, brighter
         * 3. non pure color (non zero rgb) will eventually return white
         */
        int i = (int)(1.0/(1.0-tint));
        if ( r == 0 && g == 0 && b == 0) {
           return new Color(i, i, i);
        }
        if ( r > 0 && r < i ) r = i;
        if ( g > 0 && g < i ) g = i;
        if ( b > 0 && b < i ) b = i;

        return new Color(Math.min((int)(r/tint), 255),
                         Math.min((int)(g/tint), 255),
                         Math.min((int)(b/tint), 255));
    }

    Color getSrgbColor(CTSRgbColor srgb){
        byte[] val = srgb.getVal();
        int alpha = 0xFF;
        if(srgb.sizeOfAlphaArray() > 0){
            int aval = srgb.getAlphaArray(0).getVal();
            alpha =  Math.round(255 * aval / 100000f);
        }
        return new Color(0xFF & val[0], 0xFF & val[1], 0xFF & val[2], alpha);
    }

    Color getSolidFillColor(CTSolidColorFillProperties solidFill){
        Color color;
        if (solidFill.isSetSrgbClr()) {
            color = getSrgbColor(solidFill.getSrgbClr());
        } else if (solidFill.isSetSchemeClr()) {
            color = getSchemeColor(solidFill.getSchemeClr());
        } else {
            // TODO support other types
            color = Color.black;
        }
        return color;
    }

    Color getColor(CTColor solidFill){
        Color color;
        if (solidFill.isSetSrgbClr()) {
            color = getSrgbColor(solidFill.getSrgbClr());
        } else if (solidFill.isSetSchemeClr()) {
            color = getSchemeColor(solidFill.getSchemeClr());
        } else {
            // TODO support other types
            color = Color.black;
        }
        return color;
    }

     /**
     * While developing only!
     */
    @Internal
    public CTOfficeStyleSheet getXmlObject() {
        return _theme;
    }

    protected final void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);

        Map<String, String> map = new HashMap<String, String>();
        map.put("http://schemas.openxmlformats.org/drawingml/2006/main", "a");
        xmlOptions.setSaveSuggestedPrefixes(map);
        xmlOptions.setSaveSyntheticDocumentElement(
                new QName("http://schemas.openxmlformats.org/drawingml/2006/main", "theme"));

        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        getXmlObject().save(out, xmlOptions);
        out.close();
    }

    public static Color modulateLuminanace(Color c, int lumMod, int lumOff) {
        Color color;
        if (lumOff > 0) {
            color = new Color(
                    (int) (Math.round((255 - c.getRed()) * (100.0 - lumMod) / 100.0 + c.getRed())),
                    (int) (Math.round((255 - c.getGreen()) * lumOff / 100.0 + c.getGreen())),
                    (int) (Math.round((255 - c.getBlue()) * lumOff / 100.0 + c.getBlue())),
                    c.getAlpha()
            );
        } else {
            color = new Color(
                    (int) (Math.round(c.getRed() * lumMod / 100.0)),
                    (int) (Math.round(c.getGreen() * lumMod / 100.0)),
                    (int) (Math.round(c.getBlue() * lumMod / 100.0)),
                    c.getAlpha()
            );
        }
        return color;
    }

    public String getMajorFont(){
        return _theme.getThemeElements().getFontScheme().getMajorFont().getLatin().getTypeface();
    }

    public String getMinorFont(){
        return _theme.getThemeElements().getFontScheme().getMinorFont().getLatin().getTypeface();
    }
}
