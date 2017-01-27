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

import static org.apache.poi.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.DocumentHelper;
import org.apache.poi.util.Internal;
import org.apache.poi.wp.usermodel.CharacterRun;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.XmlToken;
import org.apache.xmlbeans.impl.values.XmlAnyTypeImpl;
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
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTColor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDrawing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTEmpty;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFFCheckBox;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFldChar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFonts;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdnRef;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHpsMeasure;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPTab;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSignedHpsMeasure;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSignedTwipsMeasure;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTUnderline;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVerticalAlignRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBrClear;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBrType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STFldCharType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHighlightColor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STUnderline;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalAlignRun;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * XWPFRun object defines a region of text with a common set of properties
 */
public class XWPFRun implements ISDTContents, IRunElement, CharacterRun {
    private CTR run;
    private String pictureText;
    private IRunBody parent;
    private List<XWPFPicture> pictures;

    /**
     * @param r the CTR bean which holds the run attributes
     * @param p the parent paragraph
     */
    public XWPFRun(CTR r, IRunBody p) {
        this.run = r;
        this.parent = p;

        /**
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
        List<XmlObject> pictTextObjs = new ArrayList<XmlObject>();
        pictTextObjs.addAll(Arrays.asList(r.getPictArray()));
        pictTextObjs.addAll(Arrays.asList(r.getDrawingArray()));
        for (XmlObject o : pictTextObjs) {
            XmlObject[] ts = o.selectPath("declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//w:t");
            for (XmlObject t : ts) {
                NodeList kids = t.getDomNode().getChildNodes();
                for (int n = 0; n < kids.getLength(); n++) {
                    if (kids.item(n) instanceof Text) {
                        if (text.length() > 0)
                            text.append("\n");
                        text.append(kids.item(n).getNodeValue());
                    }
                }
            }
        }
        pictureText = text.toString();

        // Do we have any embedded pictures?
        // (They're a different CTPicture, under the drawingml namespace)
        pictures = new ArrayList<XWPFPicture>();
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
        if (text != null && (text.startsWith(" ") || text.endsWith(" "))) {
            XmlCursor c = xs.newCursor();
            c.toNextToken();
            c.insertAttributeWithValue(new QName("http://www.w3.org/XML/1998/namespace", "space"), "preserve");
            c.dispose();
        }
    }

    private List<CTPicture> getCTPictures(XmlObject o) {
        List<CTPicture> pics = new ArrayList<CTPicture>();
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
    public XWPFParagraph getParagraph() {
        if (parent instanceof XWPFParagraph)
            return (XWPFParagraph) parent;
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
        if (!onoff.isSetVal())
            return true;
        final STOnOff.Enum val = onoff.getVal();
        return (
            (STOnOff.TRUE == val) ||
            (STOnOff.X_1 == val) ||
            (STOnOff.ON == val)
        );
    }

    /**
     * Whether the bold property shall be applied to all non-complex script
     * characters in the contents of this run when displayed in a document
     *
     * @return <code>true</code> if the bold property is applied
     */
    public boolean isBold() {
        CTRPr pr = run.getRPr();
        if (pr == null || !pr.isSetB()) {
            return false;
        }
        return isCTOnOff(pr.getB());
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
    public void setBold(boolean value) {
        CTRPr pr = run.isSetRPr() ? run.getRPr() : run.addNewRPr();
        CTOnOff bold = pr.isSetB() ? pr.getB() : pr.addNewB();
        bold.setVal(value ? STOnOff.TRUE : STOnOff.FALSE);
    }

    /**
     * Get text color. The returned value is a string in the hex form "RRGGBB".
     */
    public String getColor() {
        String color = null;
        if (run.isSetRPr()) {
            CTRPr pr = run.getRPr();
            if (pr.isSetColor()) {
                CTColor clr = pr.getColor();
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
        CTRPr pr = run.isSetRPr() ? run.getRPr() : run.addNewRPr();
        CTColor color = pr.isSetColor() ? pr.getColor() : pr.addNewColor();
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
        if (pos > run.sizeOfTArray())
            throw new ArrayIndexOutOfBoundsException("Value too large for the parameter position in XWPFRun.setText(String value,int pos)");
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
    public boolean isItalic() {
        CTRPr pr = run.getRPr();
        if (pr == null || !pr.isSetI())
            return false;
        return isCTOnOff(pr.getI());
    }

    /**
     * Whether the bold property shall be applied to all non-complex script
     * characters in the contents of this run when displayed in a document
     * <p/>
     * <p/>
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
     * <p/>
     * If this element is not present, the default value is to leave the
     * formatting applied at previous level in the style hierarchy. If this
     * element is never applied in the style hierarchy, then bold shall not be
     * applied to non-complex script characters.
     * </p>
     *
     * @param value <code>true</code> if the italic property is applied to
     *              this run
     */
    public void setItalic(boolean value) {
        CTRPr pr = run.isSetRPr() ? run.getRPr() : run.addNewRPr();
        CTOnOff italic = pr.isSetI() ? pr.getI() : pr.addNewI();
        italic.setVal(value ? STOnOff.TRUE : STOnOff.FALSE);
    }

    /**
     * Specifies that the contents of this run should be displayed along with an
     * underline appearing directly below the character heigh
     *
     * @return the Underline pattern applyed to this run
     * @see UnderlinePatterns
     */
    public UnderlinePatterns getUnderline() {
        CTRPr pr = run.getRPr();
        return (pr != null && pr.isSetU() && pr.getU().getVal() != null)
                ? UnderlinePatterns.valueOf(pr.getU().getVal().intValue())
                : UnderlinePatterns.NONE;
    }

    /**
     * Specifies that the contents of this run should be displayed along with an
     * underline appearing directly below the character heigh
     * <p/>
     * <p/>
     * If this element is not present, the default value is to leave the
     * formatting applied at previous level in the style hierarchy. If this
     * element is never applied in the style hierarchy, then an underline shall
     * not be applied to the contents of this run.
     * </p>
     *
     * @param value -
     *              underline type
     * @see UnderlinePatterns : all possible patterns that could be applied
     */
    public void setUnderline(UnderlinePatterns value) {
        CTRPr pr = run.isSetRPr() ? run.getRPr() : run.addNewRPr();
        CTUnderline underline = (pr.getU() == null) ? pr.addNewU() : pr.getU();
        underline.setVal(STUnderline.Enum.forInt(value.getValue()));
    }

    /**
     * Specifies that the contents of this run shall be displayed with a single
     * horizontal line through the center of the line.
     *
     * @return <code>true</code> if the strike property is applied
     */
    public boolean isStrikeThrough() {
        CTRPr pr = run.getRPr();
        if (pr == null || !pr.isSetStrike())
            return false;
        return isCTOnOff(pr.getStrike());
    }

    /**
     * Specifies that the contents of this run shall be displayed with a single
     * horizontal line through the center of the line.
     * <p/>
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
     * <p/>
     * If this element is not present, the default value is to leave the
     * formatting applied at previous level in the style hierarchy. If this
     * element is never applied in the style hierarchy, then strikethrough shall
     * not be applied to the contents of this run.
     * </p>
     *
     * @param value <code>true</code> if the strike property is applied to
     *              this run
     */
    public void setStrikeThrough(boolean value) {
        CTRPr pr = run.isSetRPr() ? run.getRPr() : run.addNewRPr();
        CTOnOff strike = pr.isSetStrike() ? pr.getStrike() : pr.addNewStrike();
        strike.setVal(value ? STOnOff.TRUE : STOnOff.FALSE);
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
    public boolean isDoubleStrikeThrough() {
        CTRPr pr = run.getRPr();
        if (pr == null || !pr.isSetDstrike())
            return false;
        return isCTOnOff(pr.getDstrike());
    }

    /**
     * Specifies that the contents of this run shall be displayed with a
     * double horizontal line through the center of the line.
     *
     * @see #setStrikeThrough(boolean) for the rules about this
     */
    public void setDoubleStrikethrough(boolean value) {
        CTRPr pr = run.isSetRPr() ? run.getRPr() : run.addNewRPr();
        CTOnOff dstrike = pr.isSetDstrike() ? pr.getDstrike() : pr.addNewDstrike();
        dstrike.setVal(value ? STOnOff.TRUE : STOnOff.FALSE);
    }

    public boolean isSmallCaps() {
        CTRPr pr = run.getRPr();
        if (pr == null || !pr.isSetSmallCaps())
            return false;
        return isCTOnOff(pr.getSmallCaps());
    }

    public void setSmallCaps(boolean value) {
        CTRPr pr = run.isSetRPr() ? run.getRPr() : run.addNewRPr();
        CTOnOff caps = pr.isSetSmallCaps() ? pr.getSmallCaps() : pr.addNewSmallCaps();
        caps.setVal(value ? STOnOff.TRUE : STOnOff.FALSE);
    }

    public boolean isCapitalized() {
        CTRPr pr = run.getRPr();
        if (pr == null || !pr.isSetCaps())
            return false;
        return isCTOnOff(pr.getCaps());
    }

    public void setCapitalized(boolean value) {
        CTRPr pr = run.isSetRPr() ? run.getRPr() : run.addNewRPr();
        CTOnOff caps = pr.isSetCaps() ? pr.getCaps() : pr.addNewCaps();
        caps.setVal(value ? STOnOff.TRUE : STOnOff.FALSE);
    }

    public boolean isShadowed() {
        CTRPr pr = run.getRPr();
        if (pr == null || !pr.isSetShadow())
            return false;
        return isCTOnOff(pr.getShadow());
    }

    public void setShadow(boolean value) {
        CTRPr pr = run.isSetRPr() ? run.getRPr() : run.addNewRPr();
        CTOnOff shadow = pr.isSetShadow() ? pr.getShadow() : pr.addNewShadow();
        shadow.setVal(value ? STOnOff.TRUE : STOnOff.FALSE);
    }

    public boolean isImprinted() {
        CTRPr pr = run.getRPr();
        if (pr == null || !pr.isSetImprint())
            return false;
        return isCTOnOff(pr.getImprint());
    }

    public void setImprinted(boolean value) {
        CTRPr pr = run.isSetRPr() ? run.getRPr() : run.addNewRPr();
        CTOnOff imprinted = pr.isSetImprint() ? pr.getImprint() : pr.addNewImprint();
        imprinted.setVal(value ? STOnOff.TRUE : STOnOff.FALSE);
    }

    public boolean isEmbossed() {
        CTRPr pr = run.getRPr();
        if (pr == null || !pr.isSetEmboss())
            return false;
        return isCTOnOff(pr.getEmboss());
    }

    public void setEmbossed(boolean value) {
        CTRPr pr = run.isSetRPr() ? run.getRPr() : run.addNewRPr();
        CTOnOff emboss = pr.isSetEmboss() ? pr.getEmboss() : pr.addNewEmboss();
        emboss.setVal(value ? STOnOff.TRUE : STOnOff.FALSE);
    }

    /**
     * Specifies the alignment which shall be applied to the contents of this
     * run in relation to the default appearance of the run's text.
     * This allows the text to be repositioned as subscript or superscript without
     * altering the font size of the run properties.
     *
     * @return VerticalAlign
     * @see VerticalAlign all possible value that could be applyed to this run
     */
    public VerticalAlign getSubscript() {
        CTRPr pr = run.getRPr();
        return (pr != null && pr.isSetVertAlign()) ? VerticalAlign.valueOf(pr.getVertAlign().getVal().intValue()) : VerticalAlign.BASELINE;
    }

    /**
     * Specifies the alignment which shall be applied to the contents of this
     * run in relation to the default appearance of the run's text. This allows
     * the text to be repositioned as subscript or superscript without altering
     * the font size of the run properties.
     * <p/>
     * If this element is not present, the default value is to leave the
     * formatting applied at previous level in the style hierarchy. If this
     * element is never applied in the style hierarchy, then the text shall not
     * be subscript or superscript relative to the default baseline location for
     * the contents of this run.
     * </p>
     *
     * @param valign
     * @see VerticalAlign
     */
    public void setSubscript(VerticalAlign valign) {
        CTRPr pr = run.isSetRPr() ? run.getRPr() : run.addNewRPr();
        CTVerticalAlignRun ctValign = pr.isSetVertAlign() ? pr.getVertAlign() : pr.addNewVertAlign();
        ctValign.setVal(STVerticalAlignRun.Enum.forInt(valign.getValue()));
    }

    public int getKerning() {
        CTRPr pr = run.getRPr();
        if (pr == null || !pr.isSetKern())
            return 0;
        return pr.getKern().getVal().intValue();
    }

    public void setKerning(int kern) {
        CTRPr pr = run.isSetRPr() ? run.getRPr() : run.addNewRPr();
        CTHpsMeasure kernmes = pr.isSetKern() ? pr.getKern() : pr.addNewKern();
        kernmes.setVal(BigInteger.valueOf(kern));
    }

    public boolean isHighlighted() {
        CTRPr pr = run.getRPr();
        if (pr == null || !pr.isSetHighlight())
            return false;
        if (pr.getHighlight().getVal() == STHighlightColor.NONE)
            return false;
        return true;
    }
    // TODO Provide a wrapper round STHighlightColor, then expose getter/setter
    //  for the highlight colour. Ideally also then add to CharacterRun interface

    public int getCharacterSpacing() {
        CTRPr pr = run.getRPr();
        if (pr == null || !pr.isSetSpacing())
            return 0;
        return pr.getSpacing().getVal().intValue();
    }

    public void setCharacterSpacing(int twips) {
        CTRPr pr = run.isSetRPr() ? run.getRPr() : run.addNewRPr();
        CTSignedTwipsMeasure spc = pr.isSetSpacing() ? pr.getSpacing() : pr.addNewSpacing();
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
     * <p/>
     * Also sets the other font ranges, if they haven't been set before
     *
     * @param fontFamily
     * @see FontCharRange
     */
    public void setFontFamily(String fontFamily) {
        setFontFamily(fontFamily, null);
    }

    /**
     * Alias for {@link #getFontFamily()}
     */
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
        CTRPr pr = run.getRPr();
        if (pr == null || !pr.isSetRFonts()) return null;

        CTFonts fonts = pr.getRFonts();
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
     * @param fontFamily
     * @param fcr        FontCharRange or null for default handling
     */
    public void setFontFamily(String fontFamily, FontCharRange fcr) {
        CTRPr pr = run.isSetRPr() ? run.getRPr() : run.addNewRPr();
        CTFonts fonts = pr.isSetRFonts() ? pr.getRFonts() : pr.addNewRFonts();

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
     * @return value representing the font size
     */
    public int getFontSize() {
        CTRPr pr = run.getRPr();
        return (pr != null && pr.isSetSz()) ? pr.getSz().getVal().divide(new BigInteger("2")).intValue() : -1;
    }

    /**
     * Specifies the font size which shall be applied to all non complex script
     * characters in the contents of this run when displayed.
     * <p/>
     * If this element is not present, the default value is to leave the value
     * applied at previous level in the style hierarchy. If this element is
     * never applied in the style hierarchy, then any appropriate font size may
     * be used for non complex script characters.
     * </p>
     *
     * @param size
     */
    public void setFontSize(int size) {
        BigInteger bint = new BigInteger("" + size);
        CTRPr pr = run.isSetRPr() ? run.getRPr() : run.addNewRPr();
        CTHpsMeasure ctSize = pr.isSetSz() ? pr.getSz() : pr.addNewSz();
        ctSize.setVal(bint.multiply(new BigInteger("2")));
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
        CTRPr pr = run.getRPr();
        return (pr != null && pr.isSetPosition()) ? pr.getPosition().getVal().intValue()
                : -1;
    }

    /**
     * This element specifies the amount by which text shall be raised or
     * lowered for this run in relation to the default baseline of the
     * surrounding non-positioned text. This allows the text to be repositioned
     * without altering the font size of the contents.
     * <p/>
     * If the val attribute is positive, then the parent run shall be raised
     * above the baseline of the surrounding text by the specified number of
     * half-points. If the val attribute is negative, then the parent run shall
     * be lowered below the baseline of the surrounding text by the specified
     * number of half-points.
     * </p>
     * <p/>
     * If this element is not present, the default value is to leave the
     * formatting applied at previous level in the style hierarchy. If this
     * element is never applied in the style hierarchy, then the text shall not
     * be raised or lowered relative to the default baseline location for the
     * contents of this run.
     * </p>
     *
     * @param val
     */
    public void setTextPosition(int val) {
        BigInteger bint = new BigInteger("" + val);
        CTRPr pr = run.isSetRPr() ? run.getRPr() : run.addNewRPr();
        CTSignedHpsMeasure position = pr.isSetPosition() ? pr.getPosition() : pr.addNewPosition();
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
     * @throws org.apache.poi.openxml4j.exceptions.InvalidFormatException
     * @throws IOException
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
            XWPFHeaderFooter headerFooter = (XWPFHeaderFooter)parent.getPart();
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
        } catch (XmlException e) {
            throw new IllegalStateException(e);
        } catch (SAXException e) {
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
     * Returns the string version of the text
     */
    public String toString() {
        return text();
    }

    /**
     * Returns the string version of the text, with tabs and
     * carriage returns in place of their xml equivalents.
     */
    public String text() {
        StringBuffer text = new StringBuffer();

        // Grab the text and tabs of the text run
        // Do so in a way that preserves the ordering
        XmlCursor c = run.newCursor();
        c.selectPath("./*");
        while (c.toNextSelection()) {
            XmlObject o = c.getObject();
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
                            if (checkBox.getDefault() != null && checkBox.getDefault().getVal() == STOnOff.X_1) {
                                text.append("|X|");
                            } else {
                                text.append("|_|");
                            }
                        }
                    }
                }
            }

            if (o instanceof CTPTab) {
                text.append("\t");
            }
            if (o instanceof CTBr) {
                text.append("\n");
            }
            if (o instanceof CTEmpty) {
                // Some inline text elements get returned not as
                //  themselves, but as CTEmpty, owing to some odd
                //  definitions around line 5642 of the XSDs
                // This bit works around it, and replicates the above
                //  rules for that case
                String tagName = o.getDomNode().getNodeName();
                if ("w:tab".equals(tagName) || "tab".equals(tagName)) {
                    text.append("\t");
                }
                if ("w:br".equals(tagName) || "br".equals(tagName)) {
                    text.append("\n");
                }
                if ("w:cr".equals(tagName) || "cr".equals(tagName)) {
                    text.append("\n");
                }
            }
            if (o instanceof CTFtnEdnRef) {
                CTFtnEdnRef ftn = (CTFtnEdnRef) o;
                String footnoteRef = ftn.getDomNode().getLocalName().equals("footnoteReference") ?
                        "[footnoteRef:" + ftn.getId().intValue() + "]" : "[endnoteRef:" + ftn.getId().intValue() + "]";
                text.append(footnoteRef);
            }
        }

        c.dispose();

        // Any picture text?
        if (pictureText != null && pictureText.length() > 0) {
            text.append("\n").append(pictureText);
        }

        return text.toString();
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
}
