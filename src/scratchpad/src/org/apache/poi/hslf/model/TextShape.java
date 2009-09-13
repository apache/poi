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

package org.apache.poi.hslf.model;

import org.apache.poi.ddf.*;
import org.apache.poi.hslf.record.*;
import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.util.POILogger;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.io.IOException;

/**
 * A common superclass of all shapes that can hold text.
 *
 * @author Yegor Kozlov
 */
public abstract class TextShape extends SimpleShape {

    /**
     * How to anchor the text
     */
    public static final int AnchorTop = 0;
    public static final int AnchorMiddle = 1;
    public static final int AnchorBottom = 2;
    public static final int AnchorTopCentered = 3;
    public static final int AnchorMiddleCentered = 4;
    public static final int AnchorBottomCentered = 5;
    public static final int AnchorTopBaseline = 6;
    public static final int AnchorBottomBaseline = 7;
    public static final int AnchorTopCenteredBaseline = 8;
    public static final int AnchorBottomCenteredBaseline = 9;

    /**
     * How to wrap the text
     */
    public static final int WrapSquare = 0;
    public static final int WrapByPoints = 1;
    public static final int WrapNone = 2;
    public static final int WrapTopBottom = 3;
    public static final int WrapThrough = 4;

    /**
     * How to align the text
     */
    public static final int AlignLeft = 0;
    public static final int AlignCenter = 1;
    public static final int AlignRight = 2;
    public static final int AlignJustify = 3;

    /**
     * TextRun object which holds actual text and format data
     */
    protected TextRun _txtrun;

    /**
     * Escher container which holds text attributes such as
     * TextHeaderAtom, TextBytesAtom ot TextCharsAtom, StyleTextPropAtom etc.
     */
    protected EscherTextboxWrapper _txtbox;

    /**
     * Used to calculate text bounds
     */
    protected static final FontRenderContext _frc = new FontRenderContext(null, true, true);

    /**
     * Create a TextBox object and initialize it from the supplied Record container.
     *
     * @param escherRecord       <code>EscherSpContainer</code> container which holds information about this shape
     * @param parent    the parent of the shape
     */
   protected TextShape(EscherContainerRecord escherRecord, Shape parent){
        super(escherRecord, parent);

    }

    /**
     * Create a new TextBox. This constructor is used when a new shape is created.
     *
     * @param parent    the parent of this Shape. For example, if this text box is a cell
     * in a table then the parent is Table.
     */
    public TextShape(Shape parent){
        super(null, parent);
        _escherContainer = createSpContainer(parent instanceof ShapeGroup);
    }

    /**
     * Create a new TextBox. This constructor is used when a new shape is created.
     *
     */
    public TextShape(){
        this(null);
    }

    public TextRun createTextRun(){
        _txtbox = getEscherTextboxWrapper();
        if(_txtbox == null) _txtbox = new EscherTextboxWrapper();

        _txtrun = getTextRun();
        if(_txtrun == null){
            TextHeaderAtom tha = new TextHeaderAtom();
            tha.setParentRecord(_txtbox);
            _txtbox.appendChildRecord(tha);

            TextCharsAtom tca = new TextCharsAtom();
            _txtbox.appendChildRecord(tca);

            StyleTextPropAtom sta = new StyleTextPropAtom(0);
            _txtbox.appendChildRecord(sta);

            _txtrun = new TextRun(tha,tca,sta);
            _txtrun._records = new Record[]{tha, tca, sta};
            _txtrun.setText("");

            _escherContainer.addChildRecord(_txtbox.getEscherRecord());

            setDefaultTextProperties(_txtrun);
        }

        return _txtrun;
    }

    /**
     * Set default properties for the  TextRun.
     * Depending on the text and shape type the defaults are different:
     *   TextBox: align=left, valign=top
     *   AutoShape: align=center, valign=middle
     *
     */
    protected void setDefaultTextProperties(TextRun _txtrun){

    }

    /**
     * Returns the text contained in this text frame.
     *
     * @return the text string for this textbox.
     */
     public String getText(){
        TextRun tx = getTextRun();
        return tx == null ? null : tx.getText();
    }

    /**
     * Sets the text contained in this text frame.
     *
     * @param text the text string used by this object.
     */
    public void setText(String text){
        TextRun tx = getTextRun();
        if(tx == null){
            tx = createTextRun();
        }
        tx.setText(text);
        setTextId(text.hashCode());
    }

    /**
     * When a textbox is added to  a sheet we need to tell upper-level
     * <code>PPDrawing</code> about it.
     *
     * @param sh the sheet we are adding to
     */
    protected void afterInsert(Sheet sh){
        super.afterInsert(sh);

        EscherTextboxWrapper _txtbox = getEscherTextboxWrapper();
        if(_txtbox != null){
            PPDrawing ppdrawing = sh.getPPDrawing();
            ppdrawing.addTextboxWrapper(_txtbox);
            // Ensure the escher layer knows about the added records
            try {
                _txtbox.writeOut(null);
            } catch (IOException e){
                throw new HSLFException(e);
            }
            if(getAnchor().equals(new Rectangle()) && !"".equals(getText())) resizeToFitText();
        }
        if(_txtrun != null) {
            _txtrun.setShapeId(getShapeId());
            sh.onAddTextShape(this);
        }
    }

    protected EscherTextboxWrapper getEscherTextboxWrapper(){
        if(_txtbox == null){
            EscherTextboxRecord textRecord = (EscherTextboxRecord)Shape.getEscherChild(_escherContainer, EscherTextboxRecord.RECORD_ID);
            if(textRecord != null) _txtbox = new EscherTextboxWrapper(textRecord);
        }
        return _txtbox;
    }
    /**
     * Adjust the size of the TextShape so it encompasses the text inside it.
     *
     * @return a <code>Rectangle2D</code> that is the bounds of this <code>TextShape</code>.
     */
    public Rectangle2D resizeToFitText(){
        String txt = getText();
        if(txt == null || txt.length() == 0) return new Rectangle2D.Float();

        RichTextRun rt = getTextRun().getRichTextRuns()[0];
        int size = rt.getFontSize();
        int style = 0;
        if (rt.isBold()) style |= Font.BOLD;
        if (rt.isItalic()) style |= Font.ITALIC;
        String fntname = rt.getFontName();
        Font font = new Font(fntname, style, size);

        float width = 0, height = 0, leading = 0;
        String[] lines = txt.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if(lines[i].length() == 0) continue;

            TextLayout layout = new TextLayout(lines[i], font, _frc);

            leading = Math.max(leading, layout.getLeading());
            width = Math.max(width, layout.getAdvance());
            height = Math.max(height, (height + (layout.getDescent() + layout.getAscent())));
        }

        // add one character to width
        Rectangle2D charBounds = font.getMaxCharBounds(_frc);
        width += getMarginLeft() + getMarginRight() + charBounds.getWidth();

        // add leading to height
        height += getMarginTop() + getMarginBottom() + leading;

        Rectangle2D anchor = getAnchor2D();
        anchor.setRect(anchor.getX(), anchor.getY(), width, height);
        setAnchor(anchor);

        return anchor;
    }

    /**
     * Returns the type of vertical alignment for the text.
     * One of the <code>Anchor*</code> constants defined in this class.
     *
     * @return the type of alignment
     */
    public int getVerticalAlignment(){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        EscherSimpleProperty prop = (EscherSimpleProperty)getEscherProperty(opt, EscherProperties.TEXT__ANCHORTEXT);
        int valign = TextShape.AnchorTop;
        if (prop == null){
            /**
             * If vertical alignment was not found in the shape properties then try to
             * fetch the master shape and search for the align property there.
             */
            int type = getTextRun().getRunType();
            MasterSheet master = getSheet().getMasterSheet();
            if(master != null){
                TextShape masterShape = master.getPlaceholderByTextType(type);
                if(masterShape != null) valign = masterShape.getVerticalAlignment();
            } else {
                //not found in the master sheet. Use the hardcoded defaults.
                switch (type){
                     case TextHeaderAtom.TITLE_TYPE:
                     case TextHeaderAtom.CENTER_TITLE_TYPE:
                         valign = TextShape.AnchorMiddle;
                         break;
                     default:
                         valign = TextShape.AnchorTop;
                         break;
                 }
            }
        } else {
            valign = prop.getPropertyValue();
        }
        return valign;
    }

    /**
     * Sets the type of vertical alignment for the text.
     * One of the <code>Anchor*</code> constants defined in this class.
     *
     * @param align - the type of alignment
     */
    public void setVerticalAlignment(int align){
        setEscherProperty(EscherProperties.TEXT__ANCHORTEXT, align);
    }

    /**
     * Sets the type of horizontal alignment for the text.
     * One of the <code>Align*</code> constants defined in this class.
     *
     * @param align - the type of horizontal alignment
     */
    public void setHorizontalAlignment(int align){
        TextRun tx = getTextRun();
        if(tx != null) tx.getRichTextRuns()[0].setAlignment(align);
    }

    /**
     * Gets the type of horizontal alignment for the text.
     * One of the <code>Align*</code> constants defined in this class.
     *
     * @return align - the type of horizontal alignment
     */
    public int getHorizontalAlignment(){
        TextRun tx = getTextRun();
        return tx == null ? -1 : tx.getRichTextRuns()[0].getAlignment();
    }

    /**
     * Returns the distance (in points) between the bottom of the text frame
     * and the bottom of the inscribed rectangle of the shape that contains the text.
     * Default value is 1/20 inch.
     *
     * @return the botom margin
     */
    public float getMarginBottom(){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        EscherSimpleProperty prop = (EscherSimpleProperty)getEscherProperty(opt, EscherProperties.TEXT__TEXTBOTTOM);
        int val = prop == null ? EMU_PER_INCH/20 : prop.getPropertyValue();
        return (float)val/EMU_PER_POINT;
    }

    /**
     * Sets the botom margin.
     * @see #getMarginBottom()
     *
     * @param margin    the bottom margin
     */
    public void setMarginBottom(float margin){
        setEscherProperty(EscherProperties.TEXT__TEXTBOTTOM, (int)(margin*EMU_PER_POINT));
    }

    /**
     *  Returns the distance (in points) between the left edge of the text frame
     *  and the left edge of the inscribed rectangle of the shape that contains
     *  the text.
     *  Default value is 1/10 inch.
     *
     * @return the left margin
     */
    public float getMarginLeft(){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        EscherSimpleProperty prop = (EscherSimpleProperty)getEscherProperty(opt, EscherProperties.TEXT__TEXTLEFT);
        int val = prop == null ? EMU_PER_INCH/10 : prop.getPropertyValue();
        return (float)val/EMU_PER_POINT;
    }

    /**
     * Sets the left margin.
     * @see #getMarginLeft()
     *
     * @param margin    the left margin
     */
    public void setMarginLeft(float margin){
        setEscherProperty(EscherProperties.TEXT__TEXTLEFT, (int)(margin*EMU_PER_POINT));
    }

    /**
     *  Returns the distance (in points) between the right edge of the
     *  text frame and the right edge of the inscribed rectangle of the shape
     *  that contains the text.
     *  Default value is 1/10 inch.
     *
     * @return the right margin
     */
    public float getMarginRight(){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        EscherSimpleProperty prop = (EscherSimpleProperty)getEscherProperty(opt, EscherProperties.TEXT__TEXTRIGHT);
        int val = prop == null ? EMU_PER_INCH/10 : prop.getPropertyValue();
        return (float)val/EMU_PER_POINT;
    }

    /**
     * Sets the right margin.
     * @see #getMarginRight()
     *
     * @param margin    the right margin
     */
    public void setMarginRight(float margin){
        setEscherProperty(EscherProperties.TEXT__TEXTRIGHT, (int)(margin*EMU_PER_POINT));
    }

     /**
     *  Returns the distance (in points) between the top of the text frame
     *  and the top of the inscribed rectangle of the shape that contains the text.
     *  Default value is 1/20 inch.
     *
     * @return the top margin
     */
    public float getMarginTop(){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        EscherSimpleProperty prop = (EscherSimpleProperty)getEscherProperty(opt, EscherProperties.TEXT__TEXTTOP);
        int val = prop == null ? EMU_PER_INCH/20 : prop.getPropertyValue();
        return (float)val/EMU_PER_POINT;
    }

   /**
     * Sets the top margin.
     * @see #getMarginTop()
     *
     * @param margin    the top margin
     */
    public void setMarginTop(float margin){
        setEscherProperty(EscherProperties.TEXT__TEXTTOP, (int)(margin*EMU_PER_POINT));
    }


    /**
     * Returns the value indicating word wrap.
     *
     * @return the value indicating word wrap.
     *  Must be one of the <code>Wrap*</code> constants defined in this class.
     */
    public int getWordWrap(){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        EscherSimpleProperty prop = (EscherSimpleProperty)getEscherProperty(opt, EscherProperties.TEXT__WRAPTEXT);
        return prop == null ? WrapSquare : prop.getPropertyValue();
    }

    /**
     *  Specifies how the text should be wrapped
     *
     * @param wrap  the value indicating how the text should be wrapped.
     *  Must be one of the <code>Wrap*</code> constants defined in this class.
     */
    public void setWordWrap(int wrap){
        setEscherProperty(EscherProperties.TEXT__WRAPTEXT, wrap);
    }

    /**
     * @return id for the text.
     */
    public int getTextId(){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        EscherSimpleProperty prop = (EscherSimpleProperty)getEscherProperty(opt, EscherProperties.TEXT__TEXTID);
        return prop == null ? 0 : prop.getPropertyValue();
    }

    /**
     * Sets text ID
     *
     * @param id of the text
     */
    public void setTextId(int id){
        setEscherProperty(EscherProperties.TEXT__TEXTID, id);
    }

    /**
      * @return the TextRun object for this text box
      */
     public TextRun getTextRun(){
         if(_txtrun == null) initTextRun();
         return _txtrun;
     }

    public void setSheet(Sheet sheet) {
        _sheet = sheet;

        // Initialize _txtrun object.
        // (We can't do it in the constructor because the sheet
        //  is not assigned then, it's only built once we have
        //  all the records)
        TextRun tx = getTextRun();
        if (tx != null) {
            // Supply the sheet to our child RichTextRuns
            tx.setSheet(_sheet);
            RichTextRun[] rt = tx.getRichTextRuns();
            for (int i = 0; i < rt.length; i++) {
                rt[i].supplySlideShow(_sheet.getSlideShow());
            }
        }

    }

    protected void initTextRun(){
        EscherTextboxWrapper txtbox = getEscherTextboxWrapper();
        Sheet sheet = getSheet();

        if(sheet == null || txtbox == null) return;

        OutlineTextRefAtom ota = null;

        Record[] child = txtbox.getChildRecords();
        for (int i = 0; i < child.length; i++) {
            if (child[i] instanceof OutlineTextRefAtom) {
                ota = (OutlineTextRefAtom)child[i];
                break;
            }
        }

        TextRun[] runs = _sheet.getTextRuns();
        if (ota != null) {
            int idx = ota.getTextIndex();
            for (int i = 0; i < runs.length; i++) {
                if(runs[i].getIndex() == idx){
                    _txtrun = runs[i];
                    break;
                }
            }
            if(_txtrun == null) {
                logger.log(POILogger.WARN, "text run not found for OutlineTextRefAtom.TextIndex=" + idx);
            }
        } else {
            int shapeId = _escherContainer.getChildById(EscherSpRecord.RECORD_ID).getShapeId();
            if(runs != null) for (int i = 0; i < runs.length; i++) {
                if(runs[i].getShapeId() == shapeId){
                    _txtrun = runs[i];
                    break;
                }
            }
        }
    }

    public void draw(Graphics2D graphics){
        AffineTransform at = graphics.getTransform();
        ShapePainter.paint(this, graphics);
        new TextPainter(this).paint(graphics);
        graphics.setTransform(at);
    }

    /**
     * Return <code>OEPlaceholderAtom</code>, the atom that describes a placeholder.
     *
     * @return <code>OEPlaceholderAtom</code> or <code>null</code> if not found
     */
    public OEPlaceholderAtom getPlaceholderAtom(){
        return (OEPlaceholderAtom)getClientDataRecord(RecordTypes.OEPlaceholderAtom.typeID);
    }

    /**
     *
     * Assigns a hyperlink to this text shape
     *
     * @param linkId    id of the hyperlink, @see org.apache.poi.hslf.usermodel.SlideShow#addHyperlink(Hyperlink)
     * @param      beginIndex   the beginning index, inclusive.
     * @param      endIndex     the ending index, exclusive.
     * @see org.apache.poi.hslf.usermodel.SlideShow#addHyperlink(Hyperlink)
     */
    public void setHyperlink(int linkId, int beginIndex, int endIndex){
        //TODO validate beginIndex and endIndex and throw IllegalArgumentException

        InteractiveInfo info = new InteractiveInfo();
        InteractiveInfoAtom infoAtom = info.getInteractiveInfoAtom();
        infoAtom.setAction(InteractiveInfoAtom.ACTION_HYPERLINK);
        infoAtom.setHyperlinkType(InteractiveInfoAtom.LINK_Url);
        infoAtom.setHyperlinkID(linkId);

        _txtbox.appendChildRecord(info);

        TxInteractiveInfoAtom txiatom = new TxInteractiveInfoAtom();
        txiatom.setStartIndex(beginIndex);
        txiatom.setEndIndex(endIndex);
        _txtbox.appendChildRecord(txiatom);

    }

}
