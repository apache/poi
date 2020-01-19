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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.usermodel.AbstractColorStyle;
import org.apache.poi.sl.usermodel.ColorStyle;
import org.apache.poi.sl.usermodel.PresetColor;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTFontReference;
import org.openxmlformats.schemas.drawingml.x2006.main.CTHslColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveFixedPercentage;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTScRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSystemColor;
import org.w3c.dom.Node;

/**
 * Encapsulates logic to read color definitions from DrawingML and convert them to java.awt.Color
 */
@Beta
@Internal
public class XSLFColor {
    private final static POILogger LOGGER = POILogFactory.getLogger(XSLFColor.class);

    private XmlObject _xmlObject;
    private Color _color;
    private CTSchemeColor _phClr;
    private XSLFSheet _sheet;

    @SuppressWarnings("WeakerAccess")
    public XSLFColor(XmlObject obj, XSLFTheme theme, CTSchemeColor phClr, XSLFSheet sheet) {
        _xmlObject = obj;
        _phClr = phClr;
        _sheet = sheet;
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
        return DrawPaint.applyColorTransform(getColorStyle());
    }


    @SuppressWarnings("WeakerAccess")
    public ColorStyle getColorStyle() {
        return new XSLFColorStyle(_xmlObject, _color, _phClr);
    }

    private Color toColor(CTHslColor hsl) {
        return DrawPaint.HSL2RGB(
            hsl.getHue2() / 60000d,
            hsl.getSat2() / 1000d,
            hsl.getLum2() / 1000d,
            1d);
    }

    private Color toColor(CTPresetColor prst) {
        String colorName = prst.getVal().toString();
        PresetColor pc = PresetColor.valueOfOoxmlId(colorName);
        return (pc != null) ? pc.color : null;
    }

    private Color toColor(CTSchemeColor schemeColor, XSLFTheme theme) {
        String colorRef = schemeColor.getVal().toString();
        if(_phClr != null) {
            // context color overrides the theme
            colorRef = _phClr.getVal().toString();
        }
        // find referenced CTColor in the theme and convert it to java.awt.Color via a recursive call
        CTColor ctColor = theme == null ? null : theme.getCTColor(_sheet.mapSchemeColor(colorRef));
        return (ctColor != null) ? toColor(ctColor, null) : null;
    }

    private Color toColor(CTScRgbColor scrgb) {
        // color in percentage is in linear RGB color space, i.e. needs to be gamma corrected for AWT color
        return new Color(DrawPaint.lin2srgb(scrgb.getR()), DrawPaint.lin2srgb(scrgb.getG()), DrawPaint.lin2srgb(scrgb.getB()));
    }

    private Color toColor(CTSRgbColor srgb) {
        // color in sRGB color space, i.e. same as AWT Color
        byte[] val = srgb.getVal();
        return new Color(0xFF & val[0], 0xFF & val[1], 0xFF & val[2]);
    }

    private Color toColor(CTSystemColor sys) {
        if (sys.isSetLastClr()) {
            byte[] val = sys.getLastClr();
            return new Color(0xFF & val[0], 0xFF & val[1], 0xFF & val[2]);
        } else {
            String colorName = sys.getVal().toString();
            PresetColor pc = PresetColor.valueOfOoxmlId(colorName);
            return (pc != null && pc.color != null) ? pc.color : Color.black;
        }
    }

    private Color toColor(XmlObject obj, XSLFTheme theme) {
        Color color = null;
        List<XmlObject> xo = new ArrayList<>();
        xo.add(obj);
        xo.addAll(Arrays.asList(obj.selectPath("*")));
        boolean isFirst = true;
        for (XmlObject ch : xo) {
            if (ch instanceof CTHslColor) {
                color = toColor((CTHslColor)ch);
            } else if (ch instanceof CTPresetColor) {
                color = toColor((CTPresetColor)ch);
            } else if (ch instanceof CTSchemeColor) {
                color = toColor((CTSchemeColor)ch, theme);
            } else if (ch instanceof CTScRgbColor) {
                color = toColor((CTScRgbColor)ch);
            } else if (ch instanceof CTSRgbColor) {
                color = toColor((CTSRgbColor)ch);
            } else if (ch instanceof CTSystemColor) {
                color = toColor((CTSystemColor)ch);
            } else if (!(ch instanceof CTFontReference)) {
                if (!isFirst) {
                    throw new IllegalArgumentException("Unexpected color choice: " + ch.getClass());
                }
            }
            if (color != null) {
                break;
            }
            isFirst = false;
        }
        return color;
    }

    /**
     * Sets the solid color
     *
     * @param color solid color
     */
    @Internal
    protected void setColor(Color color) {
        if (!(_xmlObject instanceof CTSolidColorFillProperties)) {
            LOGGER.log(POILogger.ERROR, "XSLFColor.setColor currently only supports CTSolidColorFillProperties");
            return;
        }
        CTSolidColorFillProperties fill = (CTSolidColorFillProperties)_xmlObject;
        if (fill.isSetSrgbClr()) {
            fill.unsetSrgbClr();
        }

        if (fill.isSetScrgbClr()) {
            fill.unsetScrgbClr();
        }

        if (fill.isSetHslClr()) {
            fill.unsetHslClr();
        }

        if (fill.isSetPrstClr()) {
            fill.unsetPrstClr();
        }

        if (fill.isSetSchemeClr()) {
            fill.unsetSchemeClr();
        }

        if (fill.isSetSysClr()) {
            fill.unsetSysClr();
        }

        float[] rgbaf = color.getRGBComponents(null);
        boolean addAlpha = (rgbaf.length == 4 && rgbaf[3] < 1f);
        CTPositiveFixedPercentage alphaPct;

        // see office open xml part 4 - 5.1.2.2.30 and 5.1.2.2.32
        if (isInt(rgbaf[0]) && isInt(rgbaf[1]) && isInt(rgbaf[2])) {
            // sRGB has a gamma of 2.2
            CTSRgbColor rgb = fill.addNewSrgbClr();

            byte[] rgbBytes = {(byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue()};
            rgb.setVal(rgbBytes);
            alphaPct = (addAlpha) ? rgb.addNewAlpha() : null;
        } else {
            CTScRgbColor rgb = fill.addNewScrgbClr();
            rgb.setR(DrawPaint.srgb2lin(rgbaf[0]));
            rgb.setG(DrawPaint.srgb2lin(rgbaf[1]));
            rgb.setB(DrawPaint.srgb2lin(rgbaf[2]));
            alphaPct = (addAlpha) ? rgb.addNewAlpha() : null;
        }

        // alpha (%)
        if (alphaPct != null) {
            alphaPct.setVal((int)(100000 * rgbaf[3]));
        }
    }

    /**
     * @return true, if this is an integer color value
     */
    private static boolean isInt(float f) {
        return Math.abs((f*255d) - Math.rint(f*255d)) < 0.00001;
    }

    private static int getRawValue(CTSchemeColor phClr, XmlObject xmlObject, String elem) {
        String query = "declare namespace a='http://schemas.openxmlformats.org/drawingml/2006/main' $this//a:" + elem;

        XmlObject[] obj;

        // first ask the context color and if not found, ask the actual color bean
        if (phClr != null){
            obj = phClr.selectPath(query);
            if (obj.length == 1){
                Node attr = obj[0].getDomNode().getAttributes().getNamedItem("val");
                if(attr != null) {
                    return Integer.parseInt(attr.getNodeValue());
                }
            }
        }

        obj = xmlObject.selectPath(query);
        if (obj.length == 1){
            Node attr = obj[0].getDomNode().getAttributes().getNamedItem("val");
            if(attr != null) {
                return Integer.parseInt(attr.getNodeValue());
            }
        }

        return -1;
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
        int val = getRawValue(_phClr, _xmlObject, elem);
        return (val == -1) ? val : (val / 1000);
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


    @SuppressWarnings("unused")
    int getHue(){
        int val = getRawValue(_phClr, _xmlObject, "hue");
        return (val == -1) ? val : (val / 60000);
    }

    @SuppressWarnings("unused")
    int getHueMod(){
        return getPercentageValue("hueMod");
    }

    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    int getRedMod(){
        return getPercentageValue("redMod");
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    int getGreenMod(){
        return getPercentageValue("greenMod");
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    int getBlueMod(){
        return getPercentageValue("blueMod");
    }

    @SuppressWarnings("unused")
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
    @SuppressWarnings("WeakerAccess")
    public int getShade(){
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
    public int getTint(){
        return getPercentageValue("tint");
    }

    private static class XSLFColorStyle extends AbstractColorStyle {
        private XmlObject xmlObject;
        private Color color;
        private CTSchemeColor phClr;

        XSLFColorStyle(XmlObject xmlObject, Color color, CTSchemeColor phClr) {
            this.xmlObject = xmlObject;
            this.color = color;
            this.phClr = phClr;
        }

        @Override
        public Color getColor() {
            return color;
        }

        @Override
        public int getAlpha() {
            return getRawValue(phClr, xmlObject, "alpha");
        }

        @Override
        public int getHueOff() {
            return getRawValue(phClr, xmlObject, "hueOff");
        }

        @Override
        public int getHueMod() {
            return getRawValue(phClr, xmlObject, "hueMod");
        }

        @Override
        public int getSatOff() {
            return getRawValue(phClr, xmlObject, "satOff");
        }

        @Override
        public int getSatMod() {
            return getRawValue(phClr, xmlObject, "satMod");
        }

        @Override
        public int getLumOff() {
            return getRawValue(phClr, xmlObject, "lumOff");
        }

        @Override
        public int getLumMod() {
            return getRawValue(phClr, xmlObject, "lumMod");
        }

        @Override
        public int getShade() {
            return getRawValue(phClr, xmlObject, "shade");
        }

        @Override
        public int getTint() {
            return getRawValue(phClr, xmlObject, "tint");
        }
    }
}
