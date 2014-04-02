/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.xslf.usermodel;

import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTHslColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTScRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSystemColor;
import org.w3c.dom.Node;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates logic to read color definitions from DrawingML and convert them to java.awt.Color
 *
 * @author Yegor Kozlov
 */
@Beta
@Internal
public class XSLFColor {
    private XmlObject _xmlObject;
    private Color _color;
    private CTSchemeColor _phClr;

    public XSLFColor(XmlObject obj, XSLFTheme theme, CTSchemeColor phClr) {
        _xmlObject = obj;
        _phClr = phClr;
        _color = toColor(obj, theme);
    }

    @Internal
    public XmlObject getXmlObject() {
        return _xmlObject;
    }

    /**
     *
     * @return  the displayed color as a Java Color.
     * If not color information was found in the supplied xml object then a null is returned.
     */
    public Color getColor() {
        return _color == null ? null : applyColorTransform(_color);
    }

    private Color applyColorTransform(Color color){
        Color result = color;

        int alpha = getAlpha();
        if(alpha != -1){
            result = new Color(
                    result.getRed(), result.getGreen(), result.getBlue(), 
                    Math.round(255 * alpha * 0.01f));
        }

        int lumOff = getLumOff();
        int lumMod = getLumMod();
        if(lumMod != -1 || lumOff != -1){
            result = modulateLuminanace(result,
                    lumMod == -1 ? 100 : lumMod,
                    lumOff == -1 ? 0 : lumOff);
        }

        int shade = getShade();
        if(shade != -1){
        	result = shade(result, shade);
        }

        int tint = getTint();
        if(tint != -1){
            result = tint(result, tint);
        }

        return result;
    }

    Color toColor(XmlObject obj, XSLFTheme theme) {
        Color color = null;
        for (XmlObject ch : obj.selectPath("*")) {
            if (ch instanceof CTHslColor) {
                CTHslColor hsl = (CTHslColor)ch;
                int h = hsl.getHue2();
                int s = hsl.getSat2();
                int l = hsl.getLum2();
                // This conversion is not correct and differs from PowerPoint.
                // TODO: Revisit and improve.
                color = Color.getHSBColor(h / 60000f, s / 100000f, l / 100000f);
            } else if (ch instanceof CTPresetColor) {
                CTPresetColor prst = (CTPresetColor)ch;
                String colorName = prst.getVal().toString();
                color = presetColors.get(colorName);
            } else if (ch instanceof CTSchemeColor) {
                CTSchemeColor schemeColor = (CTSchemeColor)ch;
                String colorRef = schemeColor.getVal().toString();
                if(_phClr != null) {
                    // context color overrides the theme
                    colorRef = _phClr.getVal().toString();
                }
                // find referenced CTColor in the theme and convert it to java.awt.Color via a recursive call
                CTColor ctColor = theme.getCTColor(colorRef);
                if(ctColor != null) color = toColor(ctColor, null);
            } else if (ch instanceof CTScRgbColor) {
                // same as CTSRgbColor but with values expressed in percents
                CTScRgbColor scrgb = (CTScRgbColor)ch;
                int r = scrgb.getR();
                int g = scrgb.getG();
                int b = scrgb.getB();
                color = new Color(255 * r / 100000, 255 * g / 100000, 255 * b / 100000);
            } else if (ch instanceof CTSRgbColor) {
                CTSRgbColor srgb = (CTSRgbColor)ch;
                byte[] val = srgb.getVal();
                color = new Color(0xFF & val[0], 0xFF & val[1], 0xFF & val[2]);
            } else if (ch instanceof CTSystemColor) {
                CTSystemColor sys = (CTSystemColor)ch;
                if(sys.isSetLastClr()) {
                    byte[] val = sys.getLastClr();
                    color = new Color(0xFF & val[0], 0xFF & val[1], 0xFF & val[2]);
                } else {
                    // YK: color is a string like "menuText" or "windowText", we return black for such cases
                    String colorName = sys.getVal().toString();
                    color = Color.black;
                }
            } else {
                throw new IllegalArgumentException("Unexpected color choice: " + ch.getClass());
            }
        }
        return color;
    }

    /**
     * Read a perecentage value from the supplied xml bean.
     * Example:
     *   <a:tint val="45000"/>
     *
     * the returned value is 45
     *
     * @return  the percentage value in the range [0 .. 100]
     */
    private int getPercentageValue(String elem){
        String query = "declare namespace a='http://schemas.openxmlformats.org/drawingml/2006/main' $this//a:" + elem;

        XmlObject[] obj;

        // first ask the context color and if not found, ask the actual color bean
        if(_phClr != null){
            obj = _phClr.selectPath(query);
            if(obj.length == 1){
                Node attr = obj[0].getDomNode().getAttributes().getNamedItem("val");
                if(attr != null) {
                    return Integer.parseInt(attr.getNodeValue()) / 1000;
                }
            }
        }

        obj = _xmlObject.selectPath(query);
        if(obj.length == 1){
            Node attr = obj[0].getDomNode().getAttributes().getNamedItem("val");
            if(attr != null) {
                return Integer.parseInt(attr.getNodeValue()) / 1000;
            }
        }


        return -1;
    }

    private int getAngleValue(String elem){
        String color = "declare namespace a='http://schemas.openxmlformats.org/drawingml/2006/main' $this//a:" + elem;
        XmlObject[] obj;

        // first ask the context color and if not found, ask the actual color bean
        if(_phClr != null){
            obj = _xmlObject.selectPath( color );
            if(obj.length == 1){
                Node attr = obj[0].getDomNode().getAttributes().getNamedItem("val");
                if(attr != null) {
                    return Integer.parseInt(attr.getNodeValue()) / 60000;
                }
            }
        }

        obj = _xmlObject.selectPath( color );
        if(obj.length == 1){
            Node attr = obj[0].getDomNode().getAttributes().getNamedItem("val");
            if(attr != null) {
                return Integer.parseInt(attr.getNodeValue()) / 60000;
            }
        }
        return -1;
    }

    /**
     * the opacity as expressed by a percentage value
     *
     * @return  opacity in percents in the range [0..100]
     * or -1 if the value is not set
     */
    int getAlpha(){
        return getPercentageValue("alpha");        
    }

    /**
     * the opacity as expressed by a percentage relative to the input color
     *
     * @return  opacity in percents in the range [0..100]
     * or -1 if the value is not set
     */
    int getAlphaMod(){
        return getPercentageValue("alphaMod");
    }

    /**
     * the opacity as expressed by a percentage offset increase or decrease relative to
     * the input color. Increases will never increase the opacity beyond 100%, decreases will
     * never decrease the opacity below 0%.
     *
     * @return  opacity shift in percents in the range [0..100]
     * or -1 if the value is not set
     */
    int getAlphaOff(){
        return getPercentageValue("alphaOff");
    }


    int getHue(){
        return getAngleValue("hue");
    }

    int getHueMod(){
        return getPercentageValue("hueMod");
    }

    int getHueOff(){
        return getPercentageValue("hueOff");
    }

    /**
     * specifies the input color with the specified luminance,
     * but with its hue and saturation unchanged.
     *
     * @return  luminance in percents in the range [0..100]
     * or -1 if the value is not set
     */
    int getLum(){
        return getPercentageValue("lum");
    }

    /**
     * the luminance as expressed by a percentage relative to the input color
     *
     * @return  luminance in percents in the range [0..100]
     * or -1 if the value is not set
     */
    int getLumMod(){
        return getPercentageValue("lumMod");
    }

    /**
     * the luminance shift as expressed by a percentage relative to the input color
     *
     * @return  luminance shift in percents in the range [0..100]
     * or -1 if the value is not set
     */
    int getLumOff(){
        return getPercentageValue("lumOff");
    }

    /**
     * specifies the input color with the specified saturation,
     * but with its hue and luminance unchanged.
     *
     * @return  saturation in percents in the range [0..100]
     * or -1 if the value is not set
     */
    int getSat(){
        return getPercentageValue("sat");
    }

    /**
     * the saturation as expressed by a percentage relative to the input color
     *
     * @return  saturation in percents in the range [0..100]
     * or -1 if the value is not set
     */
    int getSatMod(){
        return getPercentageValue("satMod");
    }

    /**
     * the saturation shift as expressed by a percentage relative to the input color
     *
     * @return  saturation shift in percents in the range [0..100]
     * or -1 if the value is not set
     */
    int getSatOff(){
        return getPercentageValue("satOff");
    }

    /**
     * specifies the input color with the specific red component, but with the blue and green color
     * components unchanged
     * 
     * @return the value of the red component specified as a
     * percentage with 0% indicating minimal blue and 100% indicating maximum
     * or -1 if the value is not set
     */
    int getRed(){
        return getPercentageValue("red");
    }

    int getRedMod(){
        return getPercentageValue("redMod");
    }

    int getRedOff(){
        return getPercentageValue("redOff");
    }

    /**
     * specifies the input color with the specific green component, but with the red and blue color
     * components unchanged
     *
     * @return the value of the green component specified as a
     * percentage with 0% indicating minimal blue and 100% indicating maximum
     * or -1 if the value is not set
     */
    int getGreen(){
        return getPercentageValue("green");
    }

    int getGreenMod(){
        return getPercentageValue("greenMod");
    }

    int getGreenOff(){
        return getPercentageValue("greenOff");
    }

    /**
     * specifies the input color with the specific blue component, but with the red and green color
     * components unchanged
     *
     * @return the value of the blue component specified as a
     * percentage with 0% indicating minimal blue and 100% indicating maximum
     * or -1 if the value is not set
     */
    int getBlue(){
        return getPercentageValue("blue");
    }

    int getBlueMod(){
        return getPercentageValue("blueMod");
    }

    int getBlueOff(){
        return getPercentageValue("blueOff");
    }

    /**
     * specifies a darker version of its input color.
     * A 10% shade is 10% of the input color combined with 90% black.
     * 
     * @return the value of the shade specified as a
     * percentage with 0% indicating minimal shade and 100% indicating maximum
     * or -1 if the value is not set
     */
    int getShade(){
        return getPercentageValue("shade");
    }

    /**
     * specifies a lighter version of its input color.
     * A 10% tint is 10% of the input color combined with 90% white.
     *
     * @return the value of the tint specified as a
     * percentage with 0% indicating minimal tint and 100% indicating maximum
     * or -1 if the value is not set
     */
    int getTint(){
        return getPercentageValue("tint");
    }


    /**
     * Apply lumMod / lumOff adjustments
     *
     * @param c the color to modify
     * @param lumMod luminance modulation in the range [0..100]
     * @param lumOff luminance offset in the range [0..100]
     * @return  modified color
     */
    private static Color modulateLuminanace(Color c, int lumMod, int lumOff) {
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

    /**
     * This algorithm returns result different from PowerPoint.
     * TODO: revisit and improve
     */
    private static Color shade(Color c, int shade) {
        return new Color(
                (int)(c.getRed() * shade * 0.01),
                (int)(c.getGreen() * shade * 0.01),
                (int)(c.getBlue() * shade * 0.01),
                c.getAlpha());
    }

    /**
     * This algorithm returns result different from PowerPoint.
     * TODO: revisit and improve
     */
    private static Color tint(Color c, int tint) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();

        float ftint = tint / 100.0f;

        int red = Math.round(ftint * r + (1 - ftint) * 255);
        int green = Math.round(ftint * g + (1 - ftint) * 255);
        int blue = Math.round(ftint * b + (1 - ftint) * 255);

        return new Color(red, green, blue);
    }

    /**
     * Preset colors defined in DrawingML
     */
    static final Map<String, Color> presetColors;

    static {
        presetColors = new HashMap<String, Color>();    
        presetColors.put("aliceBlue", new Color(240, 248, 255));
        presetColors.put("antiqueWhite", new Color(250, 235, 215));
        presetColors.put("aqua", new Color(0, 255, 255));
        presetColors.put("aquamarine", new Color(127, 255, 212));
        presetColors.put("azure", new Color(240, 255, 255));
        presetColors.put("beige", new Color(245, 245, 220));
        presetColors.put("bisque", new Color(255, 228, 196));
        presetColors.put("black", new Color(0, 0, 0));
        presetColors.put("blanchedAlmond", new Color(255, 235, 205));
        presetColors.put("blue", new Color(0, 0, 255));
        presetColors.put("blueViolet", new Color(138, 43, 226));
        presetColors.put("brown", new Color(165, 42, 42));
        presetColors.put("burlyWood", new Color(222, 184, 135));
        presetColors.put("cadetBlue", new Color(95, 158, 160));
        presetColors.put("chartreuse", new Color(127, 255, 0));
        presetColors.put("chocolate", new Color(210, 105, 30));
        presetColors.put("coral", new Color(255, 127, 80));
        presetColors.put("cornflowerBlue", new Color(100, 149, 237));
        presetColors.put("crimson", new Color(220, 20, 60));
        presetColors.put("cyan", new Color(0, 255, 255));
        presetColors.put("deepPink", new Color(255, 20, 147));
        presetColors.put("deepSkyBlue", new Color(0, 191, 255));
        presetColors.put("dimGray", new Color(105, 105, 105));
        presetColors.put("dkBlue", new Color(0, 0, 139));
        presetColors.put("dkCyan", new Color(0, 139, 139));
        presetColors.put("dkGoldenrod", new Color(184, 134, 11));
        presetColors.put("dkGray", new Color(169, 169, 169));
        presetColors.put("dkGreen", new Color(0, 100, 0));
        presetColors.put("dkKhaki", new Color(189, 183, 107));
        presetColors.put("dkMagenta", new Color(139, 0, 139));
        presetColors.put("dkOliveGreen", new Color(85, 107, 47));
        presetColors.put("dkOrange", new Color(255, 140, 0));
        presetColors.put("dkOrchid", new Color(153, 50, 204));
        presetColors.put("dkRed", new Color(139, 0, 0));
        presetColors.put("dkSalmon", new Color(233, 150, 122));
        presetColors.put("dkSeaGreen", new Color(143, 188, 139));
        presetColors.put("dkSlateBlue", new Color(72, 61, 139));
        presetColors.put("dkSlateGray", new Color(47, 79, 79));
        presetColors.put("dkTurquoise", new Color(0, 206, 209));
        presetColors.put("dkViolet", new Color(148, 0, 211));
        presetColors.put("dodgerBlue", new Color(30, 144, 255));
        presetColors.put("firebrick", new Color(178, 34, 34));
        presetColors.put("floralWhite", new Color(255, 250, 240));
        presetColors.put("forestGreen", new Color(34, 139, 34));
        presetColors.put("fuchsia", new Color(255, 0, 255));
        presetColors.put("gainsboro", new Color(220, 220, 220));
        presetColors.put("ghostWhite", new Color(248, 248, 255));
        presetColors.put("gold", new Color(255, 215, 0));
        presetColors.put("goldenrod", new Color(218, 165, 32));
        presetColors.put("gray", new Color(128, 128, 128));
        presetColors.put("green", new Color(0, 128, 0));
        presetColors.put("greenYellow", new Color(173, 255, 47));
        presetColors.put("honeydew", new Color(240, 255, 240));
        presetColors.put("hotPink", new Color(255, 105, 180));
        presetColors.put("indianRed", new Color(205, 92, 92));
        presetColors.put("indigo", new Color(75, 0, 130));
        presetColors.put("ivory", new Color(255, 255, 240));
        presetColors.put("khaki", new Color(240, 230, 140));
        presetColors.put("lavender", new Color(230, 230, 250));
        presetColors.put("lavenderBlush", new Color(255, 240, 245));
        presetColors.put("lawnGreen", new Color(124, 252, 0));
        presetColors.put("lemonChiffon", new Color(255, 250, 205));
        presetColors.put("lime", new Color(0, 255, 0));
        presetColors.put("limeGreen", new Color(50, 205, 50));
        presetColors.put("linen", new Color(250, 240, 230));
        presetColors.put("ltBlue", new Color(173, 216, 230));
        presetColors.put("ltCoral", new Color(240, 128, 128));
        presetColors.put("ltCyan", new Color(224, 255, 255));
        presetColors.put("ltGoldenrodYellow", new Color(250, 250, 120));
        presetColors.put("ltGray", new Color(211, 211, 211));
        presetColors.put("ltGreen", new Color(144, 238, 144));
        presetColors.put("ltPink", new Color(255, 182, 193));
        presetColors.put("ltSalmon", new Color(255, 160, 122));
        presetColors.put("ltSeaGreen", new Color(32, 178, 170));
        presetColors.put("ltSkyBlue", new Color(135, 206, 250));
        presetColors.put("ltSlateGray", new Color(119, 136, 153));
        presetColors.put("ltSteelBlue", new Color(176, 196, 222));
        presetColors.put("ltYellow", new Color(255, 255, 224));
        presetColors.put("magenta", new Color(255, 0, 255));
        presetColors.put("maroon", new Color(128, 0, 0));
        presetColors.put("medAquamarine", new Color(102, 205, 170));
        presetColors.put("medBlue", new Color(0, 0, 205));
        presetColors.put("medOrchid", new Color(186, 85, 211));
        presetColors.put("medPurple", new Color(147, 112, 219));
        presetColors.put("medSeaGreen", new Color(60, 179, 113));
        presetColors.put("medSlateBlue", new Color(123, 104, 238));
        presetColors.put("medSpringGreen", new Color(0, 250, 154));
        presetColors.put("medTurquoise", new Color(72, 209, 204));
        presetColors.put("medVioletRed", new Color(199, 21, 133));
        presetColors.put("midnightBlue", new Color(25, 25, 112));
        presetColors.put("mintCream", new Color(245, 255, 250));
        presetColors.put("mistyRose", new Color(255, 228, 225));
        presetColors.put("moccasin", new Color(255, 228, 181));
        presetColors.put("navajoWhite", new Color(255, 222, 173));
        presetColors.put("navy", new Color(0, 0, 128));
        presetColors.put("oldLace", new Color(253, 245, 230));
        presetColors.put("olive", new Color(128, 128, 0));
        presetColors.put("oliveDrab", new Color(107, 142, 35));
        presetColors.put("orange", new Color(255, 165, 0));
        presetColors.put("orangeRed", new Color(255, 69, 0));
        presetColors.put("orchid", new Color(218, 112, 214));
        presetColors.put("paleGoldenrod", new Color(238, 232, 170));
        presetColors.put("paleGreen", new Color(152, 251, 152));
        presetColors.put("paleTurquoise", new Color(175, 238, 238));
        presetColors.put("paleVioletRed", new Color(219, 112, 147));
        presetColors.put("papayaWhip", new Color(255, 239, 213));
        presetColors.put("peachPuff", new Color(255, 218, 185));
        presetColors.put("peru", new Color(205, 133, 63));
        presetColors.put("pink", new Color(255, 192, 203));
        presetColors.put("plum", new Color(221, 160, 221));
        presetColors.put("powderBlue", new Color(176, 224, 230));
        presetColors.put("purple", new Color(128, 0, 128));
        presetColors.put("red", new Color(255, 0, 0));
        presetColors.put("rosyBrown", new Color(188, 143, 143));
        presetColors.put("royalBlue", new Color(65, 105, 225));
        presetColors.put("saddleBrown", new Color(139, 69, 19));
        presetColors.put("salmon", new Color(250, 128, 114));
        presetColors.put("sandyBrown", new Color(244, 164, 96));
        presetColors.put("seaGreen", new Color(46, 139, 87));
        presetColors.put("seaShell", new Color(255, 245, 238));
        presetColors.put("sienna", new Color(160, 82, 45));
        presetColors.put("silver", new Color(192, 192, 192));
        presetColors.put("skyBlue", new Color(135, 206, 235));
        presetColors.put("slateBlue", new Color(106, 90, 205));
        presetColors.put("slateGray", new Color(112, 128, 144));
        presetColors.put("snow", new Color(255, 250, 250));
        presetColors.put("springGreen", new Color(0, 255, 127));
        presetColors.put("steelBlue", new Color(70, 130, 180));
        presetColors.put("tan", new Color(210, 180, 140));
        presetColors.put("teal", new Color(0, 128, 128));
        presetColors.put("thistle", new Color(216, 191, 216));
        presetColors.put("tomato", new Color(255, 99, 71));
        presetColors.put("turquoise", new Color(64, 224, 208));
        presetColors.put("violet", new Color(238, 130, 238));
        presetColors.put("wheat", new Color(245, 222, 179));
        presetColors.put("white", new Color(255, 255, 255));
        presetColors.put("whiteSmoke", new Color(245, 245, 245));
        presetColors.put("yellow", new Color(255, 255, 0));
        presetColors.put("yellowGreen", new Color(154, 205, 50));
    }
}
