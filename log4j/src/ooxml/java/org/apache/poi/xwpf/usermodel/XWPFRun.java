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

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Removal;
import org.apache.poi.util.Units;
import org.apache.poi.wp.usermodel.CharacterRun;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.XmlToken;
import org.apache.xmlbeans.impl.values.XmlAnyTypeImpl;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlip;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlipFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObjectData;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualPictureProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.openxmlformats.schemas.drawingml.x2006.main.STShapeType;
import org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture;
import org.openxmlformats.schemas.drawingml.x2006.picture.CTPictureNonVisual;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTAnchor;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STHexColorRGB;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STOnOff1;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STVerticalAlignRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * XWPFRun object defines a region of text with a common set of properties
 */
public class XWPFRun implements ISDTContents, IRunElement, CharacterRun {
    private final CTR run;
    private final String pictureText;
    private final IRunBody parent;
    private final List<XWPFPicture> pictures;

    /**
     * @param r the CTR bean which holds the run attributes
     * @param p the parent paragraph
     */
    public XWPFRun(CTR r, IRunBody p) {
        this.run = r;
        this.parent = p;

        /*
         * reserve already occupied drawing ids, so reserving new ids later will
         * not corrupt the document
         */
        for (CTDrawing ctDrawing : r.getDrawingArray()) {
            for (CTAnchor anchor : ctDrawing.getAnchorArray()) {
                if (anchor.getDocPr() != null) {
                    getDocument().getDrawingIdManager().reserve(anchor.getDocPr().getId());
                }
            }
            for (CTInline inline : ctDrawing.getInlineArray()) {
                if (inline.getDocPr() != null) {
                    getDocument().getDrawingIdManager().reserve(inline.getDocPr().getId());
                }
            }
        }

        // Look for any text in any of our pictures or drawings
        StringBuilder text = new StringBuilder();
        List<XmlObject> pictTextObjs = new ArrayList<>();
        pictTextObjs.addAll(Arrays.asList(r.getPictArray()));
        pictTextObjs.addAll(Arrays.asList(r.getDrawingArray()));
        for (XmlObject o : pictTextObjs) {
            XmlObject[] ts = o.selectPath("declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//w:t");
            for (XmlObject t : ts) {
                NodeList kids = t.getDomNode().getChildNodes();
                for (int n = 0; n < kids.getLength(); n++) {
                    if (kids.item(n) instanceof Text) {
                        if (text.length() > 0) {
                            text.append("\n");
                        }
                        text.append(kids.item(n).getNodeValue());
                    }
                }
            }
        }
        pictureText = text.toString();

        // Do we have any embedded pictures?
        // (They're a different CTPicture, under the drawingml namespace)
        pictures = new ArrayList<>();
        for (XmlObject o : pictTextObjs) {
            for (CTPicture pict : getCTPictures(o)) {
                XWPFPicture picture = new XWPFPicture(pict, this);
                pictures.add(picture);
            }
        }
    }

    /**
     * @deprecated Use {@link XWPFRun#XWPFRun(CTR, IRunBody)}
     */
    @Deprecated
    public XWPFRun(CTR r, XWPFParagraph p) {
        this(r, (IRunBody) p);
    }

    /**
     * Add the xml:spaces="preserve" attribute if the string has leading or trailing white spaces
     *
     * @param xs the string to check
     */
    static void preserveSpaces(XmlString xs) {
        String text = xs.getStringValue();
        if (text != null && text.length() >= 1
                && (Character.isWhitespace(text.charAt(0)) || Character.isWhitespace(text.charAt(text.length()-1)))) {
            XmlCursor c = xs.newCursor();
            c.toNextToken();
            c.insertAttributeWithValue(new QName("http://www.w3.org/XML/1998/namespace", "space"), "preserve");
            c.dispose();
        }
    }

    private List<CTPicture> getCTPictures(XmlObject o) {
        List<CTPicture> pics = new ArrayList<>();
        XmlObject[] picts = o.selectPath("declare namespace pic='" + CTPicture.type.getName().getNamespaceURI() + "' .//pic:pic");
        for (XmlObject pict : picts) {
            if (pict instanceof XmlAnyTypeImpl) {
                // Pesky XmlBeans bug - see Bugzilla #49934
                try {
                    pict = CTPicture.Factory.parse(pict.toString(), DEFAULT_XML_OPTIONS);
                } catch (XmlException e) {
                    throw new POIXMLException(e);
                }
            }
            if (pict instanceof CTPicture) {
                pics.add((CTPicture) pict);
            }
        }
        return pics;
    }

    /**
     * Get the currently used CTR object
     *
     * @return ctr object
     */
    @Internal
    public CTR getCTR() {
        return run;
    }

    /**
     * Get the currently referenced paragraph/SDT object
     *
     * @return current parent
     */
    public IRunBody getParent() {
        return parent;
    }

    /**
     * Get the currently referenced paragraph, or null if a SDT object
     *
     * @deprecated use {@link XWPFRun#getParent()} instead
     */
    @Deprecated
    public XWPFParagraph getParagraph() {
        if (parent instanceof XWPFParagraph) {
            return (XWPFParagraph) parent;
        }
        return null;
    }

    /**
     * @return The {@link XWPFDocument} instance, this run belongs to, or
     * <code>null</code> if parent structure (paragraph > document) is not properly set.
     */
    public XWPFDocument getDocument() {
        if (parent != null) {
            return parent.getDocument();
        }
        return null;
    }

    /**
     * For isBold, isItalic etc
     */
    private static boolean isCTOnOff(CTOnOff onoff) {
        return !onoff.isSetVal() || POIXMLUnits.parseOnOff(onoff);
    }

    /**
     * Get the language tag associated with this run, if any.
     *
     * @return the language tag associated with this run, if any
     */
    public String getLang() {
        CTRPr pr = getRunProperties(false);
        Object lang = (pr == null || pr.sizeOfLangArray() == 0) ? null : pr.getLangArray(0).getVal();
        return (String) lang;
    }

    /**
     * Set the language tag associated with this run.
     *
     * @param lang the language tag associated with this run
     * @since 4.1.0
     */
    public void setLang(String lang) {
        CTRPr pr = getRunProperties(true);
        CTLanguage ctLang = pr.sizeOfLangArray() > 0 ? pr.getLangArray(0) : pr.addNewLang();
        ctLang.setVal(lang);
    }

    /**
     * Whether the bold property shall be applied to all non-complex script
     * characters in the contents of this run when displayed in a document
     *
     * @return <code>true</code> if the bold property is applied
     */
    @Override
    public boolean isBold() {
        CTRPr pr = getRunProperties(false);
        return pr != null && pr.sizeOfBArray() > 0 && isCTOnOff(pr.getBArray(0));
    }

    /**
     * Whether the bold property shall be applied to all non-complex script
     * characters in the contents of this run when displayed in a document.
     * <p>
     * This formatting property is a toggle property, which specifies that its
     * behavior differs between its use within a style definition and its use as
     * direct formatting. When used as part of a style definition, setting this
     * property shall toggle the current state of that property as specified up
     * to this point in the hierarchy (i.e. applied to not applied, and vice
     * versa). Setting it to <code>false</code> (or an equivalent) shall
     * result in the current setting remaining unchanged. However, when used as
     * direct formatting, setting this property to true or false shall set the
     * absolute state of the resulting property.
     * </p>
     * <p>
     * If this element is not present, the default value is to leave the
     * formatting applied at previous level in the style hierarchy. If this
     * element is never applied in the style hierarchy, then bold shall not be
     * applied to non-complex script characters.
     * </p>
     *
     * @param value <code>true</code> if the bold property is applied to
     *              this run
     */
    @Override
    public void setBold(boolean value) {
        CTRPr pr = getRunProperties(true);
        CTOnOff bold = pr.sizeOfBArray() > 0 ? pr.getBArray(0) : pr.addNewB();
        bold.setVal(value ? STOnOff1.ON : STOnOff1.OFF);
    }

    /**
     * Get text color. The returned value is a string in the hex form "RRGGBB".
     */
    public String getColor() {
        String color = null;
        if (run.isSetRPr()) {
            CTRPr pr = getRunProperties(false);
            if (pr != null && pr.sizeOfColorArray() > 0) {
                CTColor clr = pr.getColorArray(0);
                color = clr.xgetVal().getStringValue();
            }
        }
        return color;
    }

    /**
     * Set text color.
     *
     * @param rgbStr - the desired color, in the hex form "RRGGBB".
     */
    public void setColor(String rgbStr) {
        CTRPr pr = getRunProperties(true);
        CTColor color = pr.sizeOfColorArray() > 0 ? pr.getColorArray(0) : pr.addNewColor();
        color.setVal(rgbStr);
    }

    /**
     * Return the string content of this text run
     *
     * @return the text of this text run or <code>null</code> if not set
     */
    public String getText(int pos) {
        return run.sizeOfTArray() == 0 ? null : run.getTArray(pos)
                .getStringValue();
    }

    /**
     * Returns text embedded in pictures
     */
    public String getPictureText() {
        return pictureText;
    }

    /**
     * Sets the text of this text run
     *
     * @param value the literal text which shall be displayed in the document
     */
    public void setText(String value) {
        setText(value, run.sizeOfTArray());
    }

    /**
     * Sets the text of this text run in the
     *
     * @param value the literal text which shall be displayed in the document
     * @param pos   - position in the text array (NB: 0 based)
     */
    public void setText(String value, int pos) {
        if (pos > run.sizeOfTArray()) {
            throw new ArrayIndexOutOfBoundsException("Value too large for the parameter position in XWPFRun.setText(String value,int pos)");
        }
        CTText t = (pos < run.sizeOfTArray() && pos >= 0) ? run.getTArray(pos) : run.addNewT();
        t.setStringValue(value);
        preserveSpaces(t);
    }

    /**
     * Whether the italic property should be applied to all non-complex script
     * characters in the contents of this run when displayed in a document.
     *
     * @return <code>true</code> if the italic property is applied
     */
    @Override
    public boolean isItalic() {
        CTRPr pr = getRunProperties(false);
        return pr != null && pr.sizeOfIArray() > 0 && isCTOnOff(pr.getIArray(0));
    }

    /**
     * Whether the bold property shall be applied to all non-complex script
     * characters in the contents of this run when displayed in a document
     * <p>
     * <p>
     * This formatting property is a toggle property, which specifies that its
     * behavior differs between its use within a style definition and its use as
     * direct formatting. When used as part of a style definition, setting this
     * property shall toggle the current state of that property as specified up
     * to this point in the hierarchy (i.e. applied to not applied, and vice
     * versa). Setting it to <code>false</code> (or an equivalent) shall
     * result in the current setting remaining unchanged. However, when used as
     * direct formatting, setting this property to true or false shall set the
     * absolute state of the resulting property.
     * </p>
     * <p>
     * If this element is not present, the default value is to leave the
     * formatting applied at previous level in the style hierarchy. If this
     * element is never applied in the style hierarchy, then bold shall not be
     * applied to non-complex script characters.
     * </p>
     *
     * @param value <code>true</code> if the italic property is applied to
     *              this run
     */
    @Override
    public void setItalic(boolean value) {
        CTRPr pr = getRunProperties(true);
        CTOnOff italic = pr.sizeOfIArray() > 0 ? pr.getIArray(0) : pr.addNewI();
        italic.setVal(value ? STOnOff1.ON : STOnOff1.OFF);
    }

    /**
     * Get the underline setting for the run.
     *
     * @return the Underline pattern applied to this run
     * @see (@link UnderlinePatterns}
     */
    public UnderlinePatterns getUnderline() {
        UnderlinePatterns value = UnderlinePatterns.NONE;
        CTUnderline underline = getCTUnderline(false);
        if (underline != null) {
            STUnderline.Enum baseValue = underline.getVal();
            if (baseValue != null) {
                value = UnderlinePatterns.valueOf(baseValue.intValue());
            }
        }
        return value;
    }

    /**
     * Specifies that the contents of this run should be displayed along with an
     * underline appearing directly below the character height.
     * <p>
     * If this element is not present, the default value is to leave the
     * formatting applied at previous level in the style hierarchy. If this
     * element is never applied in the style hierarchy, then an underline shall
     * not be applied to the contents of this run.
     * </p>
     *
     * @param value -
     *              underline type
     * @see {@link UnderlinePatterns} : all possible patterns that could be applied
     */
    public void setUnderline(UnderlinePatterns value) {
        CTUnderline underline = getCTUnderline(true);
        underline.setVal(STUnderline.Enum.forInt(value.getValue()));
    }

    /**
     * Get the CTUnderline for the run.
     * @param create Create a new underline if necessary
     * @return The underline, or null create is false and there is no underline.
     */
    private CTUnderline getCTUnderline(boolean create) {
        CTRPr pr = getRunProperties(true);
        return pr.sizeOfUArray() > 0 ? pr.getUArray(0) : (create ? pr.addNewU() : null);
    }

    /**
     * Set the underline color for the run's underline, if any.
     *
     * @param color An RGB color value (e.g, "a0C6F3") or "auto".
     * @since 4.0.0
     */
    public void setUnderlineColor(String color) {
        CTUnderline underline = getCTUnderline(true);
        SimpleValue svColor = null;
        if (color.equals("auto")) {
            STHexColorAuto hexColor = STHexColorAuto.Factory.newInstance();
            hexColor.setEnumValue(STHexColorAuto.Enum.forString(color));
            svColor = (SimpleValue) hexColor;
        } else {
            STHexColorRGB rgbColor = STHexColorRGB.Factory.newInstance();
            rgbColor.setStringValue(color);
            svColor = (SimpleValue) rgbColor;
        }
        underline.setColor(svColor);
    }

    /**
     * Set the underline theme color for the run's underline, if any.
     *
     * @param themeColor A theme color name (see {@link STThemeColor.Enum}).
     * @since 4.0.0
     */
    public void setUnderlineThemeColor(String themeColor) {
        CTUnderline underline = getCTUnderline(true);
        STThemeColor.Enum val = STThemeColor.Enum.forString(themeColor);
        if (val != null) {
            underline.setThemeColor(val);
        }
    }

    /**
     * Get the underline theme color for the run's underline, if any.
     *
     * @return The {@link STThemeColor.Enum}.
     * @since 4.0.0
     */
    public STThemeColor.Enum getUnderlineThemeColor() {
        CTUnderline underline = getCTUnderline(false);
        STThemeColor.Enum color = STThemeColor.NONE;
        if (underline != null) {
            color = underline.getThemeColor();
        }
        return color;
    }

    /**
     * Get the underline color for the run's underline, if any.
     *
     * @return The RGB color value as as a string of hexadecimal digits (e.g., "A0B2F1") or "auto".
     * @since 4.0.0
     */
    public String getUnderlineColor() {
        CTUnderline underline = getCTUnderline(true);
        String colorName = "auto";
        Object rawValue = underline.getColor();
        if (rawValue != null) {
            if (rawValue instanceof String) {
                colorName = (String)rawValue;
            } else {
                byte[] rgbColor = (byte[])rawValue;
                colorName = HexDump.toHex(rgbColor[0]) + HexDump.toHex(rgbColor[1]) + HexDump.toHex(rgbColor[2]);
            }
        }
        return colorName;
    }

    /**
     * Specifies that the contents of this run shall be displayed with a single
     * horizontal line through the center of the line.
     *
     * @return <code>true</code> if the strike property is applied
     */
    @Override
    public boolean isStrikeThrough() {
        CTRPr pr = getRunProperties(false);
        return pr != null && pr.sizeOfStrikeArray() > 0 && isCTOnOff(pr.getStrikeArray(0));
    }

    /**
     * Specifies that the contents of this run shall be displayed with a single
     * horizontal line through the center of the line.
     * <p>
     * This formatting property is a toggle property, which specifies that its
     * behaviour differs between its use within a style definition and its use as
     * direct formatting. When used as part of a style definition, setting this
     * property shall toggle the current state of that property as specified up
     * to this point in the hierarchy (i.e. applied to not applied, and vice
     * versa). Setting it to false (or an equivalent) shall result in the
     * current setting remaining unchanged. However, when used as direct
     * formatting, setting this property to true or false shall set the absolute
     * state of the resulting property.
     * </p>
     * <p>
     * If this element is not present, the default value is to leave the
     * formatting applied at previous level in the style hierarchy. If this
     * element is never applied in the style hierarchy, then strikethrough shall
     * not be applied to the contents of this run.
     * </p>
     *
     * @param value <code>true</code> if the strike property is applied to
     *              this run
     */
    @Override
    public void setStrikeThrough(boolean value) {
        CTRPr pr = getRunProperties(true);
        CTOnOff strike = pr.sizeOfStrikeArray() > 0 ? pr.getStrikeArray(0) : pr.addNewStrike();
        strike.setVal(value ? STOnOff1.ON : STOnOff1.OFF);
    }

    @Deprecated
    public boolean isStrike() {
        return isStrikeThrough();
    }

    @Deprecated
    public void setStrike(boolean value) {
        setStrikeThrough(value);
    }

    /**
     * Specifies that the contents of this run shall be displayed with a double
     * horizontal line through the center of the line.
     *
     * @return <code>true</code> if the double strike property is applied
     */
    @Override
    public boolean isDoubleStrikeThrough() {
        CTRPr pr = getRunProperties(false);
        return pr != null && pr.sizeOfDstrikeArray() > 0 && isCTOnOff(pr.getDstrikeArray(0));
    }

    /**
     * Specifies that the contents of this run shall be displayed with a
     * double horizontal line through the center of the line.
     *
     * @see #setStrikeThrough(boolean) for the rules about this
     */
    @Override
    public void setDoubleStrikethrough(boolean value) {
        CTRPr pr = getRunProperties(true);
        CTOnOff dstrike = pr.sizeOfDstrikeArray() > 0 ? pr.getDstrikeArray(0) : pr.addNewDstrike();
        dstrike.setVal(value ? STOnOff1.ON : STOnOff1.OFF);
    }

    @Override
    public boolean isSmallCaps() {
        CTRPr pr = getRunProperties(false);
        return pr != null && pr.sizeOfSmallCapsArray() > 0 && isCTOnOff(pr.getSmallCapsArray(0));
    }

    @Override
    public void setSmallCaps(boolean value) {
        CTRPr pr = getRunProperties(true);
        CTOnOff caps = pr.sizeOfSmallCapsArray() > 0 ? pr.getSmallCapsArray(0) : pr.addNewSmallCaps();
        caps.setVal(value ? STOnOff1.ON : STOnOff1.OFF);
    }

    @Override
    public boolean isCapitalized() {
        CTRPr pr = getRunProperties(false);
        return pr != null && pr.sizeOfCapsArray() > 0 && isCTOnOff(pr.getCapsArray(0));
    }

    @Override
    public void setCapitalized(boolean value) {
        CTRPr pr = getRunProperties(true);
        CTOnOff caps = pr.sizeOfCapsArray() > 0 ? pr.getCapsArray(0) : pr.addNewCaps();
        caps.setVal(value ? STOnOff1.ON : STOnOff1.OFF);
    }

    @Override
    public boolean isShadowed() {
        CTRPr pr = getRunProperties(false);
        return pr != null && pr.sizeOfShadowArray() > 0 && isCTOnOff(pr.getShadowArray(0));
    }

    @Override
    public void setShadow(boolean value) {
        CTRPr pr = getRunProperties(true);
        CTOnOff shadow = pr.sizeOfShadowArray() > 0 ? pr.getShadowArray(0) : pr.addNewShadow();
        shadow.setVal(value ? STOnOff1.ON : STOnOff1.OFF);
    }

    @Override
    public boolean isImprinted() {
        CTRPr pr = getRunProperties(false);
        return pr != null && pr.sizeOfImprintArray() > 0 && isCTOnOff(pr.getImprintArray(0));
    }

    @Override
    public void setImprinted(boolean value) {
        CTRPr pr = getRunProperties(true);
        CTOnOff imprinted = pr.sizeOfImprintArray() > 0 ? pr.getImprintArray(0) : pr.addNewImprint();
        imprinted.setVal(value ? STOnOff1.ON : STOnOff1.OFF);
    }

    @Override
    public boolean isEmbossed() {
        CTRPr pr = getRunProperties(false);
        return pr != null && pr.sizeOfEmbossArray() > 0 && isCTOnOff(pr.getEmbossArray(0));
    }

    @Override
    public void setEmbossed(boolean value) {
        CTRPr pr = getRunProperties(true);
        CTOnOff emboss = pr.sizeOfEmbossArray() > 0 ? pr.getEmbossArray(0) : pr.addNewEmboss();
        emboss.setVal(value ? STOnOff1.ON : STOnOff1.OFF);
    }

    /**
     * Specifies the alignment which shall be applied to the contents of this
     * run in relation to the default appearance of the run's text. This allows
     * the text to be repositioned as subscript or superscript without altering
     * the font size of the run properties.
     * <p>
     * If this element is not present, the default value is to leave the
     * formatting applied at previous level in the style hierarchy. If this
     * element is never applied in the style hierarchy, then the text shall not
     * be subscript or superscript relative to the default baseline location for
     * the contents of this run.
     * </p>
     *
     * @param valign Type of vertical align to apply
     * @see VerticalAlign
     */
    public void setSubscript(VerticalAlign valign) {
        CTRPr pr = getRunProperties(true);
        CTVerticalAlignRun ctValign = pr.sizeOfVertAlignArray() > 0 ? pr.getVertAlignArray(0) : pr.addNewVertAlign();
        ctValign.setVal(STVerticalAlignRun.Enum.forInt(valign.getValue()));
    }

    @Override
    public int getKerning() {
        CTRPr pr = getRunProperties(false);
        if (pr == null || pr.sizeOfKernArray() == 0) {
            return 0;
        }
        return (int)POIXMLUnits.parseLength(pr.getKernArray(0).xgetVal());
    }

    @Override
    public void setKerning(int kern) {
        CTRPr pr = getRunProperties(true);
        CTHpsMeasure kernmes = pr.sizeOfKernArray() > 0 ? pr.getKernArray(0) : pr.addNewKern();
        kernmes.setVal(BigInteger.valueOf(kern));
    }

    @Override
    public boolean isHighlighted() {
        CTRPr pr = getRunProperties(false);
        if (pr == null || pr.sizeOfHighlightArray() == 0) {
            return false;
        }
        STHighlightColor.Enum val = pr.getHighlightArray(0).getVal();
        if (val == null || val == STHighlightColor.NONE) {
            return false;
        }
        return true;
    }
    // TODO Provide a wrapper round STHighlightColor, then expose getter/setter
    //  for the highlight colour. Ideally also then add to CharacterRun interface

    @Override
    public int getCharacterSpacing() {
        CTRPr pr = getRunProperties(false);
        if (pr == null || pr.sizeOfSpacingArray() == 0) {
            return 0;
        }
        return (int)Units.toDXA(POIXMLUnits.parseLength(pr.getSpacingArray(0).xgetVal()));
    }

    @Override
    public void setCharacterSpacing(int twips) {
        CTRPr pr = getRunProperties(true);
        CTSignedTwipsMeasure spc = pr.sizeOfSpacingArray() > 0 ? pr.getSpacingArray(0) : pr.addNewSpacing();
        spc.setVal(BigInteger.valueOf(twips));
    }

    /**
     * Gets the fonts which shall be used to display the text contents of
     * this run. Specifies a font which shall be used to format all characters
     * in the ASCII range (0 - 127) within the parent run
     *
     * @return a string representing the font family
     */
    public String getFontFamily() {
        return getFontFamily(null);
    }

    /**
     * Specifies the fonts which shall be used to display the text contents of
     * this run. Specifies a font which shall be used to format all characters
     * in the ASCII range (0 - 127) within the parent run.
     * <p>
     * Also sets the other font ranges, if they haven't been set before
     *
     * @param fontFamily The font family to apply
     * @see FontCharRange
     */
    public void setFontFamily(String fontFamily) {
        setFontFamily(fontFamily, null);
    }

    /**
     * Alias for {@link #getFontFamily()}
     */
    @Override
    public String getFontName() {
        return getFontFamily();
    }

    /**
     * Gets the font family for the specified font char range.
     * If fcr is null, the font char range "ascii" is used
     *
     * @param fcr the font char range, defaults to "ansi"
     * @return a string representing the font famil
     */
    public String getFontFamily(FontCharRange fcr) {
        CTRPr pr = getRunProperties(false);
        if (pr == null || pr.sizeOfRFontsArray() == 0) {
            return null;
        }

        CTFonts fonts = pr.getRFontsArray(0);
        switch (fcr == null ? FontCharRange.ascii : fcr) {
            default:
            case ascii:
                return fonts.getAscii();
            case cs:
                return fonts.getCs();
            case eastAsia:
                return fonts.getEastAsia();
            case hAnsi:
                return fonts.getHAnsi();
        }
    }

    /**
     * Specifies the fonts which shall be used to display the text contents of
     * this run. The default handling for fcr == null is to overwrite the
     * ascii font char range with the given font family and also set all not
     * specified font ranges
     *
     * @param fontFamily The font family to apply
     * @param fcr        FontCharRange or null for default handling
     */
    public void setFontFamily(String fontFamily, FontCharRange fcr) {
        CTRPr pr = getRunProperties(true);
        CTFonts fonts = pr.sizeOfRFontsArray() > 0 ? pr.getRFontsArray(0) : pr.addNewRFonts();

        if (fcr == null) {
            fonts.setAscii(fontFamily);
            if (!fonts.isSetHAnsi()) {
                fonts.setHAnsi(fontFamily);
            }
            if (!fonts.isSetCs()) {
                fonts.setCs(fontFamily);
            }
            if (!fonts.isSetEastAsia()) {
                fonts.setEastAsia(fontFamily);
            }
        } else {
            switch (fcr) {
                case ascii:
                    fonts.setAscii(fontFamily);
                    break;
                case cs:
                    fonts.setCs(fontFamily);
                    break;
                case eastAsia:
                    fonts.setEastAsia(fontFamily);
                    break;
                case hAnsi:
                    fonts.setHAnsi(fontFamily);
                    break;
            }
        }
    }

    /**
     * Specifies the font size which shall be applied to all non complex script
     * characters in the contents of this run when displayed.
     *
     * @return value representing the font size (non-integer size will be rounded with half rounding up,
     * -1 is returned if size not set)
     * @deprecated use {@link #getFontSizeAsDouble()}
     */
    @Deprecated
    @Removal(version = "6.0.0")
    @Override
    public int getFontSize() {
        BigDecimal bd = getFontSizeAsBigDecimal(0);
        return bd == null ? -1 : bd.intValue();
    }

    /**
     * Specifies the font size which shall be applied to all non complex script
     * characters in the contents of this run when displayed.
     *
     * @return value representing the font size (can be null if size not set)
     * @since POI 5.0.0
     */
    @Override
    public Double getFontSizeAsDouble() {
        BigDecimal bd = getFontSizeAsBigDecimal(1);
        return bd == null ? null : bd.doubleValue();
    }

    private BigDecimal getFontSizeAsBigDecimal(int scale) {
        CTRPr pr = getRunProperties(false);
        return (pr != null && pr.sizeOfSzArray() > 0)
            ? BigDecimal.valueOf(Units.toPoints(POIXMLUnits.parseLength(pr.getSzArray(0).xgetVal()))).divide(BigDecimal.valueOf(4), scale, RoundingMode.HALF_UP)
            : null;
    }

    /**
     * Specifies the font size which shall be applied to all non complex script
     * characters in the contents of this run when displayed.
     * <p>
     * If this element is not present, the default value is to leave the value
     * applied at previous level in the style hierarchy. If this element is
     * never applied in the style hierarchy, then any appropriate font size may
     * be used for non complex script characters.
     * </p>
     *
     * @param size The font size as number of point measurements.
     * @see #setFontSize(double)
     */
    @Override
    public void setFontSize(int size) {
        BigInteger bint = BigInteger.valueOf(size);
        CTRPr pr = getRunProperties(true);
        CTHpsMeasure ctSize = pr.sizeOfSzArray() > 0 ? pr.getSzArray(0) : pr.addNewSz();
        ctSize.setVal(bint.multiply(BigInteger.valueOf(2)));
    }

    /**
     * Specifies the font size which shall be applied to all non complex script
     * characters in the contents of this run when displayed.
     * <p>
     * If this element is not present, the default value is to leave the value
     * applied at previous level in the style hierarchy. If this element is
     * never applied in the style hierarchy, then any appropriate font size may
     * be used for non complex script characters.
     * </p>
     *
     * @param size The font size as number of point measurements.
     * @see #setFontSize(int)
     * @since POI 5.0.0
     */
    @Override
    public void setFontSize(double size) {
        BigDecimal bd = BigDecimal.valueOf(size);
        CTRPr pr = getRunProperties(true);
        CTHpsMeasure ctSize = pr.sizeOfSzArray() > 0 ? pr.getSzArray(0) : pr.addNewSz();
        ctSize.setVal(bd.multiply(BigDecimal.valueOf(2)).setScale(0, RoundingMode.HALF_UP).toBigInteger());
    }

    /**
     * This element specifies the amount by which text shall be raised or
     * lowered for this run in relation to the default baseline of the
     * surrounding non-positioned text. This allows the text to be repositioned
     * without altering the font size of the contents.
     *
     * @return a big integer representing the amount of text shall be "moved"
     */
    public int getTextPosition() {
        CTRPr pr = getRunProperties(false);
        return (pr != null && pr.sizeOfPositionArray() > 0) ? (int)(Units.toPoints(POIXMLUnits.parseLength(pr.getPositionArray(0).xgetVal())) / 2.)
                : -1;
    }

    /**
     * This element specifies the amount by which text shall be raised or
     * lowered for this run in relation to the default baseline of the
     * surrounding non-positioned text. This allows the text to be repositioned
     * without altering the font size of the contents.
     * <p>
     * If the val attribute is positive, then the parent run shall be raised
     * above the baseline of the surrounding text by the specified number of
     * half-points. If the val attribute is negative, then the parent run shall
     * be lowered below the baseline of the surrounding text by the specified
     * number of half-points.
     * </p>
     * <p>
     * If this element is not present, the default value is to leave the
     * formatting applied at previous level in the style hierarchy. If this
     * element is never applied in the style hierarchy, then the text shall not
     * be raised or lowered relative to the default baseline location for the
     * contents of this run.
     * </p>
     *
     * @param val Positive values will raise the baseline of the text, negative
     *            values will lower it.
     */
    public void setTextPosition(int val) {
        BigInteger bint = new BigInteger(Integer.toString(val));
        CTRPr pr = getRunProperties(true);
        CTSignedHpsMeasure position = pr.sizeOfPositionArray() > 0 ? pr.getPositionArray(0) : pr.addNewPosition();
        position.setVal(bint);
    }

    /**
     *
     */
    public void removeBreak() {
        // TODO
    }

    /**
     * Specifies that a break shall be placed at the current location in the run
     * content.
     * A break is a special character which is used to override the
     * normal line breaking that would be performed based on the normal layout
     * of the document's contents.
     *
     * @see #addCarriageReturn()
     */
    public void addBreak() {
        run.addNewBr();
    }

    /**
     * Specifies that a break shall be placed at the current location in the run
     * content.
     * A break is a special character which is used to override the
     * normal line breaking that would be performed based on the normal layout
     * of the document's contents.
     * <p>
     * The behavior of this break character (the
     * location where text shall be restarted after this break) shall be
     * determined by its type values.
     * </p>
     *
     * @see BreakType
     */
    public void addBreak(BreakType type) {
        CTBr br = run.addNewBr();
        br.setType(STBrType.Enum.forInt(type.getValue()));
    }

    /**
     * Specifies that a break shall be placed at the current location in the run
     * content. A break is a special character which is used to override the
     * normal line breaking that would be performed based on the normal layout
     * of the document's contents.
     * <p>
     * The behavior of this break character (the
     * location where text shall be restarted after this break) shall be
     * determined by its type (in this case is BreakType.TEXT_WRAPPING as default) and clear attribute values.
     * </p>
     *
     * @see BreakClear
     */
    public void addBreak(BreakClear clear) {
        CTBr br = run.addNewBr();
        br.setType(STBrType.Enum.forInt(BreakType.TEXT_WRAPPING.getValue()));
        br.setClear(STBrClear.Enum.forInt(clear.getValue()));
    }

    /**
     * Specifies that a tab shall be placed at the current location in
     * the run content.
     */
    public void addTab() {
        run.addNewTab();
    }

    public void removeTab() {
        //TODO
    }

    /**
     * Specifies that a carriage return shall be placed at the
     * current location in the run content.
     * A carriage return is used to end the current line of text in
     * Wordprocess.
     * The behavior of a carriage return in run content shall be
     * identical to a break character with null type and clear attributes, which
     * shall end the current line and find the next available line on which to
     * continue.
     * The carriage return character forced the following text to be
     * restarted on the next available line in the document.
     */
    public void addCarriageReturn() {
        run.addNewCr();
    }

    public void removeCarriageReturn() {
        //TODO
    }

    /**
     * Adds a picture to the run. This method handles
     * attaching the picture data to the overall file.
     *
     * @param pictureData The raw picture data
     * @param pictureType The type of the picture, eg {@link Document#PICTURE_TYPE_JPEG}
     * @param width       width in EMUs. To convert to / from points use {@link org.apache.poi.util.Units}
     * @param height      height in EMUs. To convert to / from points use {@link org.apache.poi.util.Units}
     * @throws InvalidFormatException If the format of the picture is not known.
     * @throws IOException            If reading the picture-data from the stream fails.
     * @see org.apache.poi.xwpf.usermodel.Document#PICTURE_TYPE_EMF
     * @see org.apache.poi.xwpf.usermodel.Document#PICTURE_TYPE_WMF
     * @see org.apache.poi.xwpf.usermodel.Document#PICTURE_TYPE_PICT
     * @see org.apache.poi.xwpf.usermodel.Document#PICTURE_TYPE_JPEG
     * @see org.apache.poi.xwpf.usermodel.Document#PICTURE_TYPE_PNG
     * @see org.apache.poi.xwpf.usermodel.Document#PICTURE_TYPE_DIB
     */
    public XWPFPicture addPicture(InputStream pictureData, int pictureType, String filename, int width, int height)
            throws InvalidFormatException, IOException {
        String relationId;
        XWPFPictureData picData;

        // Work out what to add the picture to, then add both the
        //  picture and the relationship for it
        // TODO Should we have an interface for this sort of thing?
        if (parent.getPart() instanceof XWPFHeaderFooter) {
            XWPFHeaderFooter headerFooter = (XWPFHeaderFooter) parent.getPart();
            relationId = headerFooter.addPictureData(pictureData, pictureType);
            picData = (XWPFPictureData) headerFooter.getRelationById(relationId);
        } else {
            @SuppressWarnings("resource")
            XWPFDocument doc = parent.getDocument();
            relationId = doc.addPictureData(pictureData, pictureType);
            picData = (XWPFPictureData) doc.getRelationById(relationId);
        }

        // Create the drawing entry for it
        try {
            CTDrawing drawing = run.addNewDrawing();
            CTInline inline = drawing.addNewInline();

            // Do the fiddly namespace bits on the inline
            // (We need full control of what goes where and as what)
            String xml =
                    "<a:graphic xmlns:a=\"" + CTGraphicalObject.type.getName().getNamespaceURI() + "\">" +
                            "<a:graphicData uri=\"" + CTPicture.type.getName().getNamespaceURI() + "\">" +
                            "<pic:pic xmlns:pic=\"" + CTPicture.type.getName().getNamespaceURI() + "\" />" +
                            "</a:graphicData>" +
                            "</a:graphic>";
            InputSource is = new InputSource(new StringReader(xml));
            org.w3c.dom.Document doc = DocumentHelper.readDocument(is);
            inline.set(XmlToken.Factory.parse(doc.getDocumentElement(), DEFAULT_XML_OPTIONS));

            // Setup the inline
            inline.setDistT(0);
            inline.setDistR(0);
            inline.setDistB(0);
            inline.setDistL(0);

            CTNonVisualDrawingProps docPr = inline.addNewDocPr();
            long id = getParent().getDocument().getDrawingIdManager().reserveNew();
            docPr.setId(id);
            /* This name is not visible in Word 2010 anywhere. */
            docPr.setName("Drawing " + id);
            docPr.setDescr(filename);

            CTPositiveSize2D extent = inline.addNewExtent();
            extent.setCx(width);
            extent.setCy(height);

            // Grab the picture object
            CTGraphicalObject graphic = inline.getGraphic();
            CTGraphicalObjectData graphicData = graphic.getGraphicData();
            CTPicture pic = getCTPictures(graphicData).get(0);

            // Set it up
            CTPictureNonVisual nvPicPr = pic.addNewNvPicPr();

            CTNonVisualDrawingProps cNvPr = nvPicPr.addNewCNvPr();
            /* use "0" for the id. See ECM-576, 20.2.2.3 */
            cNvPr.setId(0L);
            /* This name is not visible in Word 2010 anywhere */
            cNvPr.setName("Picture " + id);
            cNvPr.setDescr(filename);

            CTNonVisualPictureProperties cNvPicPr = nvPicPr.addNewCNvPicPr();
            cNvPicPr.addNewPicLocks().setNoChangeAspect(true);

            CTBlipFillProperties blipFill = pic.addNewBlipFill();
            CTBlip blip = blipFill.addNewBlip();
            blip.setEmbed(parent.getPart().getRelationId(picData));
            blipFill.addNewStretch().addNewFillRect();

            CTShapeProperties spPr = pic.addNewSpPr();
            CTTransform2D xfrm = spPr.addNewXfrm();

            CTPoint2D off = xfrm.addNewOff();
            off.setX(0);
            off.setY(0);

            CTPositiveSize2D ext = xfrm.addNewExt();
            ext.setCx(width);
            ext.setCy(height);

            CTPresetGeometry2D prstGeom = spPr.addNewPrstGeom();
            prstGeom.setPrst(STShapeType.RECT);
            prstGeom.addNewAvLst();

            // Finish up
            XWPFPicture xwpfPicture = new XWPFPicture(pic, this);
            pictures.add(xwpfPicture);
            return xwpfPicture;
        } catch (XmlException | SAXException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * this method add chart template into document
     *
     * @param chartRelId relation id of chart in document relation file
     * @throws InvalidFormatException
     * @throws IOException
     * @since POI 4.0.0
     */
    @Internal
    public CTInline addChart(String chartRelId)
            throws InvalidFormatException, IOException {
        try {
            CTInline inline = run.addNewDrawing().addNewInline();

            //xml part of chart in document
            String xml =
                    "<a:graphic xmlns:a=\"" + CTGraphicalObject.type.getName().getNamespaceURI() + "\">" +
                            "<a:graphicData uri=\"" + CTChart.type.getName().getNamespaceURI() + "\">" +
                            "<c:chart xmlns:c=\"" + CTChart.type.getName().getNamespaceURI() + "\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" r:id=\"" + chartRelId + "\" />" +
                            "</a:graphicData>" +
                            "</a:graphic>";

            InputSource is = new InputSource(new StringReader(xml));

            org.w3c.dom.Document doc = DocumentHelper.readDocument(is);

            inline.set(XmlToken.Factory.parse(doc.getDocumentElement(), DEFAULT_XML_OPTIONS));

            // Setup the inline with 0 margin
            inline.setDistT(0);
            inline.setDistR(0);
            inline.setDistB(0);
            inline.setDistL(0);

            CTNonVisualDrawingProps docPr = inline.addNewDocPr();
            long id = getParent().getDocument().getDrawingIdManager().reserveNew();
            docPr.setId(id);
            //This name is not visible in Word anywhere.
            docPr.setName("chart " + id);

            return inline;
        } catch (XmlException | SAXException e) {
            throw new IllegalStateException(e);
        }
    }


    /**
     * Returns the embedded pictures of the run. These
     * are pictures which reference an external,
     * embedded picture image such as a .png or .jpg
     */
    public List<XWPFPicture> getEmbeddedPictures() {
        return pictures;
    }

    /**
     * Set the style ID for the run.
     *
     * @param styleId ID (not name) of the style to set for the run, e.g. "BoldItalic" (not "Bold Italic").
     * @since POI 4.1.1
     */
    public void setStyle(String styleId) {
        CTRPr pr = getCTR().getRPr();
        if (null == pr) {
           pr = getCTR().addNewRPr();
        }
        CTString style = pr.sizeOfRStyleArray() > 0 ? pr.getRStyleArray(0) : pr.addNewRStyle();
        style.setVal(styleId);
    }

    /**
     * Return this run's style ID. If this run has no style (no run properties or properties without a style),
     * an empty string is returned.
     *
     * @since 4.1.1
     */
    public String getStyle() {
        CTRPr pr = getCTR().getRPr();
        if (pr == null) {
            return "";
        }

        CTString style = pr.getRStyleArray(0);
        if (style == null) {
            return "";
        }

        return style.getVal();
    }


    /**
     * Returns the string version of the text and the phonetic string
     */
    @Override
    public String toString() {
        String phonetic = getPhonetic();
        if (phonetic.length() > 0) {
            return text() + " (" + phonetic + ")";
        } else {
            return text();
        }
    }

    /**
     * Returns the string version of the text, with tabs and
     * carriage returns in place of their xml equivalents.
     */
    @Override
    public String text() {
        StringBuilder text = new StringBuilder(64);

        // Grab the text and tabs of the text run
        // Do so in a way that preserves the ordering
        XmlCursor c = run.newCursor();
        c.selectPath("./*");
        while (c.toNextSelection()) {
            XmlObject o = c.getObject();
            if (o instanceof CTRuby) {
                handleRuby(o, text, false);
                continue;
            }
            _getText(o, text);
        }
        c.dispose();
        return text.toString();

    }

    /**
     * @return the phonetic (ruby) string associated with this run or an empty String if none exists
     */
    public String getPhonetic() {
        StringBuilder text = new StringBuilder(64);

        // Grab the text and tabs of the text run
        // Do so in a way that preserves the ordering
        XmlCursor c = run.newCursor();
        c.selectPath("./*");
        while (c.toNextSelection()) {
            XmlObject o = c.getObject();
            if (o instanceof CTRuby) {
                handleRuby(o, text, true);
            }
        }
        // Any picture text?
        if (pictureText != null && pictureText.length() > 0) {
            text.append("\n").append(pictureText).append("\n");
        }
        c.dispose();
        return text.toString();
    }

    /**
     * @param rubyObj         rubyobject
     * @param text            buffer to which to append the content
     * @param extractPhonetic extract the phonetic (rt) component or the base component
     */
    private void handleRuby(XmlObject rubyObj, StringBuilder text, boolean extractPhonetic) {
        XmlCursor c = rubyObj.newCursor();

        //according to the spec, a ruby object
        //has the phonetic (rt) first, then the actual text (base)
        //second.

        c.selectPath(".//*");
        boolean inRT = false;
        boolean inBase = false;
        while (c.toNextSelection()) {
            XmlObject o = c.getObject();
            if (o instanceof CTRubyContent) {
                String tagName = o.getDomNode().getNodeName();
                if ("w:rt".equals(tagName)) {
                    inRT = true;
                } else if ("w:rubyBase".equals(tagName)) {
                    inRT = false;
                    inBase = true;
                }
            } else {
                if (extractPhonetic && inRT) {
                    _getText(o, text);
                } else if (!extractPhonetic && inBase) {
                    _getText(o, text);
                }
            }
        }
        c.dispose();
    }

    private void _getText(XmlObject o, StringBuilder text) {

        if (o instanceof CTText) {
            String tagName = o.getDomNode().getNodeName();
            // Field Codes (w:instrText, defined in spec sec. 17.16.23)
            //  come up as instances of CTText, but we don't want them
            //  in the normal text output
            if (!"w:instrText".equals(tagName)) {
                text.append(((CTText) o).getStringValue());
            }
        }

        // Complex type evaluation (currently only for extraction of check boxes)
        if (o instanceof CTFldChar) {
            CTFldChar ctfldChar = ((CTFldChar) o);
            if (ctfldChar.getFldCharType() == STFldCharType.BEGIN) {
                if (ctfldChar.getFfData() != null) {
                    for (CTFFCheckBox checkBox : ctfldChar.getFfData().getCheckBoxList()) {
                        text.append((checkBox.getDefault() != null && POIXMLUnits.parseOnOff(checkBox.getDefault().xgetVal())) ? "|X|" : "|_|");
                    }
                }
            }
        }

        if (o instanceof CTPTab) {
            text.append('\t');
        }
        if (o instanceof CTBr) {
            text.append('\n');
        }
        if (o instanceof CTEmpty) {
            // Some inline text elements get returned not as
            //  themselves, but as CTEmpty, owing to some odd
            //  definitions around line 5642 of the XSDs
            // This bit works around it, and replicates the above
            //  rules for that case
            String tagName = o.getDomNode().getNodeName();
            if ("w:tab".equals(tagName) || "tab".equals(tagName)) {
                text.append('\t');
            }
            if ("w:br".equals(tagName) || "br".equals(tagName)) {
                text.append('\n');
            }
            if ("w:cr".equals(tagName) || "cr".equals(tagName)) {
                text.append('\n');
            }
        }
        if (o instanceof CTFtnEdnRef) {
            CTFtnEdnRef ftn = (CTFtnEdnRef) o;
            String footnoteRef = ftn.getDomNode().getLocalName().equals("footnoteReference") ?
                    "[footnoteRef:" + ftn.getId().intValue() + "]" : "[endnoteRef:" + ftn.getId().intValue() + "]";
            text.append(footnoteRef);
        }
    }

    /**
     * @see <a href="http://msdn.microsoft.com/en-us/library/ff533743(v=office.12).aspx">[MS-OI29500] Run Fonts</a>
     */
    public static enum FontCharRange {
        ascii /* char 0-127 */,
        cs /* complex symbol */,
        eastAsia /* east asia */,
        hAnsi /* high ansi */
    }

    /**
     * Set the text expand/collapse scale value.
     *
     * @param percentage The percentage to expand or compress the text
     * @since 4.0.0
     */
    public void setTextScale(int percentage) {
        CTRPr pr = getRunProperties(true);
        CTTextScale scale = pr.sizeOfWArray() > 0 ? pr.getWArray(0) : pr.addNewW();
        scale.setVal(percentage);
    }

    /**
     * Gets the current text scale value.
     *
     * @return Value is an integer percentage
     * @since 4.0.0
     */
    public int getTextScale() {
        CTRPr pr = getRunProperties(false);
        if (pr == null || pr.sizeOfWArray() == 0) {
            return 100;
        }

        int value = POIXMLUnits.parsePercent(pr.getWArray(0).xgetVal());
        // 100% scaling, that is, no change. See 17.3.2.43 w (Expanded/Compressed Text)
        return value == 0 ? 100 : value / 1000;
    }

    /**
     * Set the highlight color for the run. Silently does nothing of colorName is not a recognized value.
     *
     * @param colorName The name of the color as defined in the ST_HighlightColor simple type ({@link STHightlightColor})
     * @since 4.0.0
     */
    public void setTextHighlightColor(String colorName) {
        CTRPr pr = getRunProperties(true);
        CTHighlight highlight = pr.sizeOfHighlightArray() > 0 ? pr.getHighlightArray(0) : pr.addNewHighlight();
        STHighlightColor color = highlight.xgetVal();
        if (color == null) {
            color = STHighlightColor.Factory.newInstance();
        }
        STHighlightColor.Enum val = STHighlightColor.Enum.forString(colorName);
        if (val != null) {
            color.setStringValue(val.toString());
            highlight.xsetVal(color);
        }

    }

    /**
     * Gets the highlight color for the run
     *
     * @return {@link STHighlightColor} for the run.
     * @since 4.0.0
     */
    public STHighlightColor.Enum getTextHightlightColor() {
        CTRPr pr = getRunProperties(true);
        CTHighlight highlight = pr.sizeOfHighlightArray() > 0 ? pr.getHighlightArray(0) : pr.addNewHighlight();
        STHighlightColor color = highlight.xgetVal();
        if (color == null) {
            color = STHighlightColor.Factory.newInstance();
            color.setEnumValue(STHighlightColor.NONE);
        }
        return (STHighlightColor.Enum)(color.getEnumValue());
    }

    /**
     * Get the vanish (hidden text) value
     *
     * @return True if the run is hidden text.
     * @since 4.0.0
     */
    public boolean isVanish() {
        CTRPr pr = getRunProperties(true);
        return pr != null && pr.sizeOfVanishArray() > 0 && isCTOnOff(pr.getVanishArray(0));
    }

    /**
     * The vanish (hidden text) property for the run.
     *
     * @param value Set to true to make the run hidden text.
     * @since 4.0.0
     */
    public void setVanish(boolean value) {
        CTRPr pr = getRunProperties(true);
        CTOnOff vanish = pr.sizeOfVanishArray() > 0 ? pr.getVanishArray(0) : pr.addNewVanish();
        vanish.setVal(value ? STOnOff1.ON : STOnOff1.OFF);
    }

    /**
     * Get the vertical alignment value
     *
     * @return {@link STVerticalAlignRun.Enum} value (see 22.9.2.17 ST_VerticalAlignRun (Vertical Positioning Location))
     * @since 4.0.0
     */
    public STVerticalAlignRun.Enum getVerticalAlignment() {
        CTRPr pr = getRunProperties(true);
        CTVerticalAlignRun vertAlign = pr.sizeOfVertAlignArray() > 0 ? pr.getVertAlignArray(0) : pr.addNewVertAlign();
        STVerticalAlignRun.Enum val = vertAlign.getVal();
        if (val == null) {
            val = STVerticalAlignRun.BASELINE;
        }
        return val;
    }

    /**
     * Set the vertical alignment of the run.
     *
     * @param verticalAlignment Vertical alignment value, one of "baseline", "superscript", or "subscript".
     * @since 4.0.0
     */
    public void setVerticalAlignment(String verticalAlignment) {
        CTRPr pr = getRunProperties(true);
        CTVerticalAlignRun vertAlign = pr.sizeOfVertAlignArray() > 0 ? pr.getVertAlignArray(0) : pr.addNewVertAlign();
        STVerticalAlignRun align = vertAlign.xgetVal();
        if (align == null) {
            align = STVerticalAlignRun.Factory.newInstance();
        }
        STVerticalAlignRun.Enum val = STVerticalAlignRun.Enum.forString(verticalAlignment);
        if (val != null) {
            align.setStringValue(val.toString());
            vertAlign.xsetVal(align);
        }


    }

    /**
     * Get the emphasis mark value for the run.
     *
     * @return {@link STEm.Enum} emphasis mark type enumeration. See 17.18.24 ST_Em (Emphasis Mark Type).
     * @since 4.0.0
     */
    public STEm.Enum getEmphasisMark() {
        CTRPr pr = getRunProperties(true);
        CTEm emphasis = pr.sizeOfEmArray() > 0 ? pr.getEmArray(0) : pr.addNewEm();

        STEm.Enum val = emphasis.getVal();
        if (val == null) {
            val = STEm.NONE;
        }
        return val;
    }

    /**
     * Set the emphasis mark for the run. The emphasis mark goes above or below the run
     * text.
     *
     * @param markType Emphasis mark type name, e.g., "dot" or "none". See 17.18.24 ST_Em (Emphasis Mark Type)
     * @since 4.0.0
     */
    public void setEmphasisMark(String markType) {
        CTRPr pr = getRunProperties(true);
        CTEm emphasisMark = pr.sizeOfEmArray() > 0 ? pr.getEmArray(0) : pr.addNewEm();
        STEm mark = emphasisMark.xgetVal();
        if (mark == null) {
            mark = STEm.Factory.newInstance();
        }
        STEm.Enum val = STEm.Enum.forString(markType);
        if (val != null) {
            mark.setStringValue(val.toString());
            emphasisMark.xsetVal(mark);
        }


    }

    /**
     * Get the run properties for the run.
     *
     * @param create If true, create the properties, if false, do not.
     * @return The run properties or null if there are no properties and create is false.
     */
    protected CTRPr getRunProperties(boolean create) {
        CTRPr pr = run.isSetRPr() ? run.getRPr() : null;
        if (create && pr == null) {
            pr = run.addNewRPr();
        }
        return pr;
    }

}
