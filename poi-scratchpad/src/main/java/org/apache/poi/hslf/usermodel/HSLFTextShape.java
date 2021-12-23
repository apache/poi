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

package org.apache.poi.hslf.usermodel;

import static org.apache.poi.hslf.record.RecordTypes.OEPlaceholderAtom;
import static org.apache.poi.hslf.record.RecordTypes.RoundTripHFPlaceholder12;
import static org.apache.poi.sl.usermodel.TextShape.TextPlaceholder.CENTER_TITLE;
import static org.apache.poi.sl.usermodel.TextShape.TextPlaceholder.TITLE;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherPropertyTypes;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.ddf.EscherTextboxRecord;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.hslf.model.HSLFMetroShape;
import org.apache.poi.hslf.model.textproperties.TextPropCollection;
import org.apache.poi.hslf.record.EscherTextboxWrapper;
import org.apache.poi.hslf.record.OEPlaceholderAtom;
import org.apache.poi.hslf.record.PPDrawing;
import org.apache.poi.hslf.record.RoundTripHFPlaceholder12;
import org.apache.poi.hslf.record.StyleTextPropAtom;
import org.apache.poi.hslf.record.TextBytesAtom;
import org.apache.poi.hslf.record.TextCharsAtom;
import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.DrawTextShape;
import org.apache.poi.sl.usermodel.Insets2D;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextRun;
import org.apache.poi.sl.usermodel.TextShape;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.util.Units;

/**
 * A common superclass of all shapes that can hold text.
 */
public abstract class HSLFTextShape extends HSLFSimpleShape
implements TextShape<HSLFShape,HSLFTextParagraph> {
    private static final Logger LOG = LogManager.getLogger(HSLFTextShape.class);

    /**
     * How to anchor the text
     */
    private enum HSLFTextAnchor {
        TOP                   (0, VerticalAlignment.TOP,    false, false),
        MIDDLE                (1, VerticalAlignment.MIDDLE, false, false),
        BOTTOM                (2, VerticalAlignment.BOTTOM, false, false),
        TOP_CENTER            (3, VerticalAlignment.TOP,    true,  false),
        MIDDLE_CENTER         (4, VerticalAlignment.MIDDLE, true,  false),
        BOTTOM_CENTER         (5, VerticalAlignment.BOTTOM, true,  false),
        TOP_BASELINE          (6, VerticalAlignment.TOP,    false, true),
        BOTTOM_BASELINE       (7, VerticalAlignment.BOTTOM, false, true),
        TOP_CENTER_BASELINE   (8, VerticalAlignment.TOP,    true,  true),
        BOTTOM_CENTER_BASELINE(9, VerticalAlignment.BOTTOM, true,  true);

        public final int nativeId;
        public final VerticalAlignment vAlign;
        public final boolean centered;
        public final Boolean baseline;

        HSLFTextAnchor(int nativeId, VerticalAlignment vAlign, boolean centered, Boolean baseline) {
            this.nativeId = nativeId;
            this.vAlign = vAlign;
            this.centered = centered;
            this.baseline = baseline;
        }

        static HSLFTextAnchor fromNativeId(int nativeId) {
            for (HSLFTextAnchor ta : values()) {
                if (ta.nativeId == nativeId) {
                    return ta;
                }
            }
            return null;
        }
    }

    /**
     * Specifies that a line of text will continue on subsequent lines instead
     * of extending into or beyond a margin.
     * Office Excel 2007, Excel 2010, PowerPoint 97, and PowerPoint 2010 read
     * and use this value properly but do not write it.
     */
    public static final int WrapSquare = 0;
    /**
     * Specifies a wrapping rule that is equivalent to that of WrapSquare
     * Excel 97, Excel 2000, Excel 2002, and Office Excel 2003 use this value.
     * All other product versions listed at the beginning of this appendix ignore this value.
     */
    public static final int WrapByPoints = 1;
    /**
     * Specifies that a line of text will extend into or beyond a margin instead
     * of continuing on subsequent lines.
     * Excel 97, Word 97, Excel 2000, Word 2000, Excel 2002,
     * and Office Excel 2003 do not use this value.
     */
    public static final int WrapNone = 2;
    /**
     * Specifies a wrapping rule that is undefined and MUST be ignored.
     */
    public static final int WrapTopBottom = 3;
    /**
     * Specifies a wrapping rule that is undefined and MUST be ignored.
     */
    public static final int WrapThrough = 4;


    /**
     * TextRun object which holds actual text and format data
     */
    private List<HSLFTextParagraph> _paragraphs = new ArrayList<>();

    /**
     * Escher container which holds text attributes such as
     * TextHeaderAtom, TextBytesAtom or TextCharsAtom, StyleTextPropAtom etc.
     */
    private EscherTextboxWrapper _txtbox;

    /**
     * This setting is used for supporting a deprecated alignment
     *
     * @see <a href=""></a>
     */
//    boolean alignToBaseline = false;

    /**
     * Create a TextBox object and initialize it from the supplied Record container.
     *
     * @param escherRecord       {@code EscherSpContainer} container which holds information about this shape
     * @param parent    the parent of the shape
     */
    protected HSLFTextShape(EscherContainerRecord escherRecord, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super(escherRecord, parent);
    }

    /**
     * Create a new TextBox. This constructor is used when a new shape is created.
     *
     * @param parent    the parent of this Shape. For example, if this text box is a cell
     * in a table then the parent is Table.
     */
    public HSLFTextShape(ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super(null, parent);
        createSpContainer(parent instanceof HSLFGroupShape);
    }

    /**
     * Create a new TextBox. This constructor is used when a new shape is created.
     *
     */
    public HSLFTextShape(){
        this(null);
    }

    /**
     * Set default properties for the  TextRun.
     * Depending on the text and shape type the defaults are different:
     *   TextBox: align=left, valign=top
     *   AutoShape: align=center, valign=middle
     *
     */
    protected void setDefaultTextProperties(HSLFTextParagraph _txtrun){

    }

    /**
     * When a textbox is added to  a sheet we need to tell upper-level
     * {@code PPDrawing} about it.
     *
     * @param sh the sheet we are adding to
     */
    @Override
    protected void afterInsert(HSLFSheet sh){
        super.afterInsert(sh);

        storeText();

        EscherTextboxWrapper thisTxtbox = getEscherTextboxWrapper();
        if(thisTxtbox != null){
            getSpContainer().addChildRecord(thisTxtbox.getEscherRecord());

            PPDrawing ppdrawing = sh.getPPDrawing();
            ppdrawing.addTextboxWrapper(thisTxtbox);
            // Ensure the escher layer knows about the added records
            try {
                thisTxtbox.writeOut(null);
            } catch (IOException e){
                throw new HSLFException(e);
            }
            boolean isInitialAnchor = getAnchor().equals(new Rectangle2D.Double());
            boolean isFilledTxt = !"".equals(getText());
            if (isInitialAnchor && isFilledTxt) {
                resizeToFitText();
            }
        }
        for (HSLFTextParagraph htp : _paragraphs) {
            htp.setShapeId(getShapeId());
        }
        sh.onAddTextShape(this);
    }

    protected EscherTextboxWrapper getEscherTextboxWrapper(){
        if(_txtbox != null) {
            return _txtbox;
        }

        EscherTextboxRecord textRecord = getEscherChild(EscherTextboxRecord.RECORD_ID);
        if (textRecord == null) {
            return null;
        }

        HSLFSheet sheet = getSheet();
        if (sheet != null) {
            PPDrawing drawing = sheet.getPPDrawing();
            if (drawing != null) {
                EscherTextboxWrapper[] wrappers = drawing.getTextboxWrappers();
                if (wrappers != null) {
                    for (EscherTextboxWrapper w : wrappers) {
                        // check for object identity
                        if (textRecord == w.getEscherRecord()) {
                            _txtbox = w;
                            return _txtbox;
                        }
                    }
                }
            }
        }

        _txtbox = new EscherTextboxWrapper(textRecord);
        return _txtbox;
    }

    private void createEmptyParagraph() {
        TextHeaderAtom tha = (TextHeaderAtom)_txtbox.findFirstOfType(TextHeaderAtom._type);
        if (tha == null) {
            tha = new TextHeaderAtom();
            tha.setParentRecord(_txtbox);
            _txtbox.appendChildRecord(tha);
        }

        TextBytesAtom tba = (TextBytesAtom)_txtbox.findFirstOfType(TextBytesAtom._type);
        TextCharsAtom tca = (TextCharsAtom)_txtbox.findFirstOfType(TextCharsAtom._type);
        if (tba == null && tca == null) {
            tba = new TextBytesAtom();
             tba.setText(new byte[0]);
             _txtbox.appendChildRecord(tba);
        }

        final String text = ((tba != null) ? tba.getText() : tca.getText());

        StyleTextPropAtom sta = (StyleTextPropAtom)_txtbox.findFirstOfType(StyleTextPropAtom._type);
        TextPropCollection paraStyle = null, charStyle = null;
        if (sta == null) {
            int parSiz = text.length();
            sta = new StyleTextPropAtom(parSiz+1);
            if (_paragraphs.isEmpty()) {
                paraStyle = sta.addParagraphTextPropCollection(parSiz+1);
                charStyle = sta.addCharacterTextPropCollection(parSiz+1);
            } else {
                for (HSLFTextParagraph htp : _paragraphs) {
                    int runsLen = 0;
                    for (HSLFTextRun htr : htp.getTextRuns()) {
                        runsLen += htr.getLength();
                        charStyle = sta.addCharacterTextPropCollection(htr.getLength());
                        htr.setCharacterStyle(charStyle);
                    }
                    paraStyle = sta.addParagraphTextPropCollection(runsLen);
                    htp.setParagraphStyle(paraStyle);
                }
                assert (paraStyle != null && charStyle != null);
            }
            _txtbox.appendChildRecord(sta);
        } else {
            paraStyle = sta.getParagraphStyles().get(0);
            charStyle = sta.getCharacterStyles().get(0);
        }

        if (_paragraphs.isEmpty()) {
            HSLFTextParagraph htp = new HSLFTextParagraph(tha, tba, tca, _paragraphs);
            htp.setParagraphStyle(paraStyle);
            htp.setParentShape(this);
            _paragraphs.add(htp);

            HSLFTextRun htr = new HSLFTextRun(htp);
            htr.setCharacterStyle(charStyle);
            htr.setText(text);
            htp.addTextRun(htr);
        }
    }

    @Override
    public Rectangle2D resizeToFitText() {
        return resizeToFitText(null);
    }

    @Override
    public Rectangle2D resizeToFitText(Graphics2D graphics) {
        Rectangle2D anchor = getAnchor();
        if(anchor.getWidth() == 0.) {
            LOG.atWarn().log("Width of shape wasn't set. Defaulting to 200px");
            anchor.setRect(anchor.getX(), anchor.getY(), 200., anchor.getHeight());
            setAnchor(anchor);
        }
        double height = getTextHeight(graphics);
        height += 1; // add a pixel to compensate rounding errors

        Insets2D insets = getInsets();
        anchor.setRect(anchor.getX(), anchor.getY(), anchor.getWidth(), height+insets.top+insets.bottom);
        setAnchor(anchor);

        return anchor;
    }

    /**
    * Returns the type of the text, from the TextHeaderAtom.
    * Possible values can be seen from TextHeaderAtom
    * @see TextHeaderAtom
    */
    public int getRunType() {
        getEscherTextboxWrapper();
        if (_txtbox == null) {
            return -1;
        }
        List<HSLFTextParagraph> paras = HSLFTextParagraph.findTextParagraphs(_txtbox, getSheet());
        return (paras.isEmpty() || paras.get(0).getIndex() == -1) ? -1 : paras.get(0).getRunType();
    }

    /**
    * Changes the type of the text. Values should be taken
    *  from TextHeaderAtom. No checking is done to ensure you
    *  set this to a valid value!
    * @see TextHeaderAtom
    */
    public void setRunType(int type) {
        getEscherTextboxWrapper();
        if (_txtbox == null) {
            return;
        }
        List<HSLFTextParagraph> paras = HSLFTextParagraph.findTextParagraphs(_txtbox, getSheet());
        if (!paras.isEmpty()) {
            paras.get(0).setRunType(type);
        }
    }

    /**
     * Returns the type of vertical alignment for the text.
     * One of the <code>Anchor*</code> constants defined in this class.
     *
     * @return the type of alignment
     */
    /* package */ HSLFTextAnchor getAlignment(){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherPropertyTypes.TEXT__ANCHORTEXT);
        final HSLFTextAnchor align;
        if (prop == null){
            /**
             * If vertical alignment was not found in the shape properties then try to
             * fetch the master shape and search for the align property there.
             */
            int type = getRunType();
            HSLFSheet sh = getSheet();
            HSLFMasterSheet master = (sh != null) ? sh.getMasterSheet() : null;
            HSLFTextShape masterShape = (master != null) ? master.getPlaceholderByTextType(type) : null;
            if (masterShape != null && type != TextPlaceholder.OTHER.nativeId) {
                align = masterShape.getAlignment();
            } else {
                //not found in the master sheet. Use the hardcoded defaults.
                align = (TextPlaceholder.isTitle(type)) ? HSLFTextAnchor.MIDDLE : HSLFTextAnchor.TOP;
            }
        } else {
            align = HSLFTextAnchor.fromNativeId(prop.getPropertyValue());
        }

        return (align == null) ?  HSLFTextAnchor.TOP : align;
    }

    /**
     * Sets the type of alignment for the text.
     * One of the {@code Anchor*} constants defined in this class.
     *
     * @param isCentered horizontal centered?
     * @param vAlign vertical alignment
     * @param baseline aligned to baseline?
     */
    /* package */ void setAlignment(Boolean isCentered, VerticalAlignment vAlign, boolean baseline) {
        for (HSLFTextAnchor hta : HSLFTextAnchor.values()) {
            if (
                (hta.centered == (isCentered != null && isCentered)) &&
                (hta.vAlign == vAlign) &&
                (hta.baseline == null || hta.baseline == baseline)
            ) {
                setEscherProperty(EscherPropertyTypes.TEXT__ANCHORTEXT, hta.nativeId);
                break;
            }
        }
    }

    /**
     * @return true, if vertical alignment is relative to baseline
     * this is only used for older versions less equals Office 2003
     */
    public boolean isAlignToBaseline() {
        return getAlignment().baseline;
    }

    /**
     * Sets the vertical alignment relative to the baseline
     *
     * @param alignToBaseline if true, vertical alignment is relative to baseline
     */
    public void setAlignToBaseline(boolean alignToBaseline) {
        setAlignment(isHorizontalCentered(), getVerticalAlignment(), alignToBaseline);
    }

    @Override
    public boolean isHorizontalCentered() {
        return getAlignment().centered;
    }

    @Override
    public void setHorizontalCentered(Boolean isCentered) {
        setAlignment(isCentered, getVerticalAlignment(), getAlignment().baseline);
    }

    @Override
    public VerticalAlignment getVerticalAlignment() {
        return getAlignment().vAlign;
    }

    @Override
    public void setVerticalAlignment(VerticalAlignment vAlign) {
        setAlignment(isHorizontalCentered(), vAlign, getAlignment().baseline);
    }

    /**
     * Returns the distance (in points) between the bottom of the text frame
     * and the bottom of the inscribed rectangle of the shape that contains the text.
     * Default value is 1/20 inch.
     *
     * @return the botom margin
     */
    public double getBottomInset(){
        return getInset(EscherPropertyTypes.TEXT__TEXTBOTTOM, .05);
    }

    /**
     * Sets the botom margin.
     * @see #getBottomInset()
     *
     * @param margin    the bottom margin
     */
    public void setBottomInset(double margin){
        setInset(EscherPropertyTypes.TEXT__TEXTBOTTOM, margin);
    }

    /**
     *  Returns the distance (in points) between the left edge of the text frame
     *  and the left edge of the inscribed rectangle of the shape that contains
     *  the text.
     *  Default value is 1/10 inch.
     *
     * @return the left margin
     */
    public double getLeftInset(){
        return getInset(EscherPropertyTypes.TEXT__TEXTLEFT, .1);
    }

    /**
     * Sets the left margin.
     * @see #getLeftInset()
     *
     * @param margin    the left margin
     */
    public void setLeftInset(double margin){
        setInset(EscherPropertyTypes.TEXT__TEXTLEFT, margin);
    }

    /**
     *  Returns the distance (in points) between the right edge of the
     *  text frame and the right edge of the inscribed rectangle of the shape
     *  that contains the text.
     *  Default value is 1/10 inch.
     *
     * @return the right margin
     */
    public double getRightInset(){
        return getInset(EscherPropertyTypes.TEXT__TEXTRIGHT, .1);
    }

    /**
     * Sets the right margin.
     * @see #getRightInset()
     *
     * @param margin    the right margin
     */
    public void setRightInset(double margin){
        setInset(EscherPropertyTypes.TEXT__TEXTRIGHT, margin);
    }

     /**
     *  Returns the distance (in points) between the top of the text frame
     *  and the top of the inscribed rectangle of the shape that contains the text.
     *  Default value is 1/20 inch.
     *
     * @return the top margin
     */
    public double getTopInset(){
        return getInset(EscherPropertyTypes.TEXT__TEXTTOP, .05);
    }

   /**
     * Sets the top margin.
     * @see #getTopInset()
     *
     * @param margin    the top margin
     */
    public void setTopInset(double margin){
        setInset(EscherPropertyTypes.TEXT__TEXTTOP, margin);
    }

    /**
     * Returns the distance (in points) between the edge of the text frame
     * and the edge of the inscribed rectangle of the shape that contains the text.
     * Default value is 1/20 inch.
     *
     * @param type the type of the inset edge
     * @return the inset in points
     */
    private double getInset(EscherPropertyTypes type, double defaultInch) {
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, type);
        int val = prop == null ? (int)(Units.toEMU(Units.POINT_DPI)*defaultInch) : prop.getPropertyValue();
        return Units.toPoints(val);
    }

    /**
     * @param type the type of the inset edge
     * @param margin the inset in points
     */
    private void setInset(EscherPropertyTypes type, double margin){
        setEscherProperty(type, Units.toEMU(margin));
    }

    /**
     * Returns the value indicating word wrap.
     *
     * @return the value indicating word wrap.
     *  Must be one of the {@code Wrap*} constants defined in this class.
     *
     * @see <a href="https://msdn.microsoft.com/en-us/library/dd948168(v=office.12).aspx">MSOWRAPMODE</a>
     */
    public int getWordWrapEx() {
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherPropertyTypes.TEXT__WRAPTEXT);
        return prop == null ? WrapSquare : prop.getPropertyValue();
    }

    /**
     *  Specifies how the text should be wrapped
     *
     * @param wrap  the value indicating how the text should be wrapped.
     *  Must be one of the {@code Wrap*} constants defined in this class.
     */
    public void setWordWrapEx(int wrap){
        setEscherProperty(EscherPropertyTypes.TEXT__WRAPTEXT, wrap);
    }

    @Override
    public boolean getWordWrap(){
        int ww = getWordWrapEx();
        return (ww != WrapNone);
    }

    @Override
    public void setWordWrap(boolean wrap) {
        setWordWrapEx(wrap ? WrapSquare : WrapNone);
    }

    /**
     * @return id for the text.
     */
    public int getTextId(){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherPropertyTypes.TEXT__TEXTID);
        return prop == null ? 0 : prop.getPropertyValue();
    }

    /**
     * Sets text ID
     *
     * @param id of the text
     */
    public void setTextId(int id){
        setEscherProperty(EscherPropertyTypes.TEXT__TEXTID, id);
    }

    @Override
    public List<HSLFTextParagraph> getTextParagraphs(){
        if (!_paragraphs.isEmpty()) {
            return _paragraphs;
        }

        _txtbox = getEscherTextboxWrapper();
        if (_txtbox == null) {
            _txtbox = new EscherTextboxWrapper();
            createEmptyParagraph();
        } else {
            List<HSLFTextParagraph> pList = HSLFTextParagraph.findTextParagraphs(_txtbox, getSheet());
            if (pList == null) {
                // there are actually TextBoxRecords without extra data - see #54722
                createEmptyParagraph();
            } else {
                _paragraphs = pList;
            }

            if (_paragraphs.isEmpty()) {
                LOG.atWarn().log("TextRecord didn't contained any text lines");
            }
        }

        for (HSLFTextParagraph p : _paragraphs) {
            p.setParentShape(this);
        }

        return _paragraphs;
    }


    @Override
    public void setSheet(HSLFSheet sheet) {
        super.setSheet(sheet);

        // Initialize _txtrun object.
        // (We can't do it in the constructor because the sheet
        //  is not assigned then, it's only built once we have
        //  all the records)
        List<HSLFTextParagraph> ltp = getTextParagraphs();
        HSLFTextParagraph.supplySheet(ltp, sheet);
    }

    /**
     * Return {@link OEPlaceholderAtom}, the atom that describes a placeholder.
     *
     * @return {@link OEPlaceholderAtom} or {@code null} if not found
     */
    public OEPlaceholderAtom getPlaceholderAtom(){
        return getClientDataRecord(OEPlaceholderAtom.typeID);
    }

    /**
     * Return {@link RoundTripHFPlaceholder12}, the atom that describes a header/footer placeholder.
     * Compare the {@link RoundTripHFPlaceholder12#getPlaceholderId()} with
     * {@link Placeholder#HEADER} or {@link Placeholder#FOOTER}, to find out
     * what kind of placeholder this is.
     *
     * @return {@link RoundTripHFPlaceholder12} or {@code null} if not found
     *
     * @since POI 3.14-Beta2
     */
    public RoundTripHFPlaceholder12 getHFPlaceholderAtom() {
        // special case for files saved in Office 2007
        return getClientDataRecord(RoundTripHFPlaceholder12.typeID);
    }

    @Override
    public boolean isPlaceholder() {
        return
            ((getPlaceholderAtom() != null) ||
            //special case for files saved in Office 2007
            (getHFPlaceholderAtom() != null));
    }


    @Override
    public Iterator<HSLFTextParagraph> iterator() {
        return _paragraphs.iterator();
    }

    /**
     * @since POI 5.2.0
     */
    @Override
    public Spliterator<HSLFTextParagraph> spliterator() {
        return _paragraphs.spliterator();
    }

    @Override
    public Insets2D getInsets() {
        return new Insets2D(getTopInset(), getLeftInset(), getBottomInset(), getRightInset());
    }

    @Override
    public void setInsets(Insets2D insets) {
        setTopInset(insets.top);
        setLeftInset(insets.left);
        setBottomInset(insets.bottom);
        setRightInset(insets.right);
    }

    @Override
    public double getTextHeight() {
        return getTextHeight(null);
    }

    @Override
    public double getTextHeight(Graphics2D graphics) {
        DrawFactory drawFact = DrawFactory.getInstance(graphics);
        DrawTextShape dts = drawFact.getDrawable(this);
        return dts.getTextHeight(graphics);
    }

    @Override
    public TextDirection getTextDirection() {
        // see 2.4.5 MSOTXFL
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherPropertyTypes.TEXT__TEXTFLOW);
        int msotxfl = (prop == null) ? 0 : prop.getPropertyValue();
        switch (msotxfl) {
            default:
            case 0: // msotxflHorzN
            case 4: // msotxflHorzA
                return TextDirection.HORIZONTAL;
            case 1: // msotxflTtoBA
            case 3: // msotxflTtoBN
            case 5: // msotxflVertN
                return TextDirection.VERTICAL;
            case 2: // msotxflBtoT
                return TextDirection.VERTICAL_270;
            // TextDirection.STACKED is not supported
        }
    }

    @Override
    public void setTextDirection(TextDirection orientation) {
        AbstractEscherOptRecord opt = getEscherOptRecord();
        int msotxfl;
        if (orientation == null) {
            msotxfl = -1;
        } else {
            switch (orientation) {
                default:
                case STACKED:
                    // not supported -> remove
                    msotxfl = -1;
                    break;
                case HORIZONTAL:
                    msotxfl = 0;
                    break;
                case VERTICAL:
                    msotxfl = 1;
                    break;
                case VERTICAL_270:
                    // always interpreted as horizontal
                    msotxfl = 2;
                    break;
            }
        }
        setEscherProperty(opt, EscherPropertyTypes.TEXT__TEXTFLOW, msotxfl);
    }

    @Override
    public Double getTextRotation() {
        // see 2.4.6 MSOCDIR
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherPropertyTypes.TEXT__FONTROTATION);
        return (prop == null) ? null : (90. * prop.getPropertyValue());
    }

    @Override
    public void setTextRotation(Double rotation) {
        AbstractEscherOptRecord opt = getEscherOptRecord();
        if (rotation == null) {
            opt.removeEscherProperty(EscherPropertyTypes.TEXT__FONTROTATION);
        } else {
            int rot = (int)(Math.round(rotation / 90.) % 4L);
            setEscherProperty(EscherPropertyTypes.TEXT__FONTROTATION, rot);
        }
    }

    /**
     * Returns the raw text content of the shape. This hasn't had any
     * changes applied to it, and so is probably unlikely to print
     * out nicely.
     */
    public String getRawText() {
        return HSLFTextParagraph.getRawText(getTextParagraphs());
    }

    @Override
    public String getText() {
        String rawText = getRawText();
        return HSLFTextParagraph.toExternalString(rawText, getRunType());
    }

    @Override
    public HSLFTextRun appendText(String text, boolean newParagraph) {
        // init paragraphs
        List<HSLFTextParagraph> paras = getTextParagraphs();
        HSLFTextRun htr = HSLFTextParagraph.appendText(paras, text, newParagraph);
        setTextId(getRawText().hashCode());
        return htr;
    }

    @Override
    public HSLFTextRun setText(String text) {
        // init paragraphs
        List<HSLFTextParagraph> paras = getTextParagraphs();
        HSLFTextRun htr = HSLFTextParagraph.setText(paras, text);
        setTextId(getRawText().hashCode());
        return htr;
    }

    /**
     * Saves the modified paragraphs/textrun to the records.
     * Also updates the styles to the correct text length.
     */
    protected void storeText() {
        List<HSLFTextParagraph> paras = getTextParagraphs();
        HSLFTextParagraph.storeText(paras);
    }

    /**
     * Returns the array of all hyperlinks in this text run
     *
     * @return the array of all hyperlinks in this text run or {@code null}
     *         if not found.
     */
    public List<HSLFHyperlink> getHyperlinks() {
        return HSLFHyperlink.find(this);
    }

    @Override
    public void setTextPlaceholder(TextPlaceholder placeholder) {
        // TOOD: check for correct placeholder handling - see org.apache.poi.hslf.model.Placeholder
        Placeholder ph = null;
        int runType;
        switch (placeholder) {
            default:
            case BODY:
                runType = TextPlaceholder.BODY.nativeId;
                ph = Placeholder.BODY;
                break;
            case TITLE:
                runType = TITLE.nativeId;
                ph = Placeholder.TITLE;
                break;
            case CENTER_BODY:
                runType = TextPlaceholder.CENTER_BODY.nativeId;
                ph = Placeholder.BODY;
                break;
            case CENTER_TITLE:
                runType = CENTER_TITLE.nativeId;
                ph = Placeholder.TITLE;
                break;
            case HALF_BODY:
                runType = TextPlaceholder.HALF_BODY.nativeId;
                ph = Placeholder.BODY;
                break;
            case QUARTER_BODY:
                runType = TextPlaceholder.QUARTER_BODY.nativeId;
                ph = Placeholder.BODY;
                break;
            case NOTES:
                runType = TextPlaceholder.NOTES.nativeId;
                break;
            case OTHER:
                runType = TextPlaceholder.OTHER.nativeId;
                break;
        }
        setRunType(runType);
        if (ph != null) {
            setPlaceholder(ph);
        }
    }

    @Override
    public TextPlaceholder getTextPlaceholder() {
        return TextPlaceholder.fromNativeId(getRunType());
    }


    /**
     * Get alternative representation of text shape stored as metro blob escher property.
     * The returned shape is the first shape in stored group shape of the metro blob
     *
     * @return null, if there's no alternative representation, otherwise the text shape
     */
    public <
        S extends Shape<S,P>,
        P extends TextParagraph<S,P,? extends TextRun>
    > Shape<S,P> getMetroShape() {
        return new HSLFMetroShape<S,P>(this).getShape();
    }
}