
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
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.io.IOException;

/**
 * Represents a TextFrame shape in PowerPoint.
 * <p>
 * Contains the text in a text frame as well as the properties and methods
 * that control alignment and anchoring of the text.
 * </p>
 *
 * @author Yegor Kozlov
 */
public class TextBox extends SimpleShape {

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
     * Low-level object which holds actual text and format data
     */
    protected TextRun _txtrun;

    /**
     * Escher container which holds text attributes such as
     * TextHeaderAtom, TextBytesAtom ot TextCharsAtom, StyleTextPropAtom etc.
     */
    protected EscherTextboxWrapper _txtbox;
    
    /**
     * Is the TextBox missing the text records which actually
     *  store the text?
     */
    private boolean _missingTextRecords = false;

    /**
     * Create a TextBox object and initialize it from the supplied Record container.
     * 
     * @param escherRecord       <code>EscherSpContainer</code> container which holds information about this shape
     * @param parent    the parent of the shape
     */
   protected TextBox(EscherContainerRecord escherRecord, Shape parent){
        super(escherRecord, parent);

        EscherTextboxRecord textbox = (EscherTextboxRecord)Shape.getEscherChild(_escherContainer, EscherTextboxRecord.RECORD_ID);
        _txtbox = new EscherTextboxWrapper(textbox);
    }

    /**
     * Create a new TextBox. This constructor is used when a new shape is created.
     *
     * @param parent    the parent of this Shape. For example, if this text box is a cell
     * in a table then the parent is Table.
     */
    public TextBox(Shape parent){
        super(null, parent);
        _escherContainer = createSpContainer(parent instanceof ShapeGroup);
    }

    /**
     * Create a new TextBox. This constructor is used when a new shape is created.
     *
     */
    public TextBox(){
        this(null);
    }

    /**
     * Create a new textBox and initialize internal structures
     *
     * @return the created <code>EscherContainerRecord</code> which holds shape data
     */
    protected EscherContainerRecord createSpContainer(boolean isChild){
        EscherContainerRecord spcont = super.createSpContainer(isChild);

        EscherSpRecord spRecord = spcont.getChildById(EscherSpRecord.RECORD_ID);
        short type = (ShapeTypes.TextBox << 4) | 0x2;
        spRecord.setOptions(type);

        //set default properties for a textbox
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(spcont, EscherOptRecord.RECORD_ID);
        setEscherProperty(opt, EscherProperties.TEXT__TEXTID, 0);

        setEscherProperty(opt, EscherProperties.FILL__FILLCOLOR, 0x8000004);
        setEscherProperty(opt, EscherProperties.FILL__FILLBACKCOLOR, 0x8000000);
        setEscherProperty(opt, EscherProperties.FILL__NOFILLHITTEST, 0x100000);
        setEscherProperty(opt, EscherProperties.LINESTYLE__COLOR, 0x8000001);
        setEscherProperty(opt, EscherProperties.LINESTYLE__NOLINEDRAWDASH, 0x80000);
        setEscherProperty(opt, EscherProperties.SHADOWSTYLE__COLOR, 0x8000002);

        //create EscherTextboxWrapper
        _txtbox = new EscherTextboxWrapper();

        TextHeaderAtom tha = new TextHeaderAtom();
        tha.setParentRecord(_txtbox); // TextHeaderAtom is parent aware
        _txtbox.appendChildRecord(tha);

        TextCharsAtom tca = new TextCharsAtom();
        _txtbox.appendChildRecord(tca);

        StyleTextPropAtom sta = new StyleTextPropAtom(0);
        _txtbox.appendChildRecord(sta);

        _txtrun = new TextRun(tha,tca,sta);
        _txtrun.setText("");
        spcont.addChildRecord(_txtbox.getEscherRecord());

        return spcont;
    }

    /**
     * Returns the text contained in this text frame.
     *
     * @return the text string for this textbox.
     */
     public String getText(){
        return _txtrun == null ? null : _txtrun.getText();
    }

    /**
     * Sets the text contained in this text frame.
     *
     * @param text the text string used by this object.
     */
    public void setText(String text){
        _txtrun.setText(text);
    }

    /**
     * When a textbox is added to  a sheet we need to tell upper-level
     * <code>PPDrawing</code> about it.
     *
     * @param sh the sheet we are adding to
     */
    protected void afterInsert(Sheet sh){
        PPDrawing ppdrawing = sh.getPPDrawing();
        ppdrawing.addTextboxWrapper(_txtbox);
        // Ensure the escher layer knows about the added records 
        try {
            _txtbox.writeOut(null);
        } catch (IOException e){
            throw new HSLFException(e);
        }
        if(getAnchor().equals(new java.awt.Rectangle()) && !"".equals(getText())) resizeToFitText();
    }

    /**
     * Adjust the size of the TextBox so it encompasses the text inside it.
     */
    public void resizeToFitText(){
        try{
        FontRenderContext frc = new FontRenderContext(null, true, true);
        RichTextRun rt = _txtrun.getRichTextRuns()[0];
        int size = rt.getFontSize();
        int style = 0;
        if (rt.isBold()) style |= Font.BOLD;
        if (rt.isItalic()) style |= Font.ITALIC;
        String fntname = rt.getFontName();
        Font font = new Font(fntname, style, size);

        TextLayout layout = new TextLayout(getText(), font, frc);
        int width = Math.round(layout.getAdvance());
        int height = Math.round(layout.getAscent());

        Dimension txsize = new Dimension(width, height);
        java.awt.Rectangle anchor = getAnchor();
        anchor.setSize(txsize);
        setAnchor(anchor);
        } catch (Exception e){
            e.printStackTrace();

        }
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
        int valign;
        if (prop == null){
            int type = getTextRun().getRunType();
            switch (type){
                case TextHeaderAtom.TITLE_TYPE:
                case TextHeaderAtom.CENTER_TITLE_TYPE:
                    valign = TextBox.AnchorMiddle;
                    break;
                default:
                    valign = TextBox.AnchorTop;
                    break;
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
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        setEscherProperty(opt, EscherProperties.TEXT__ANCHORTEXT, align);
    }

    public void setHorizontalAlignment(int align){
        _txtrun.getRichTextRuns()[0].setAlignment(align);
    }
    public int getHorizontalAlignment(){
        return _txtrun.getRichTextRuns()[0].getAlignment();
    }

    /**
     * Returns the distance (in points) between the bottom of the text frame
     * and the bottom of the inscribed rectangle of the shape that contains the text.
     * Default value is 1/20 inch.
     *
     * @return the botom margin
     */
    public int getMarginBottom(){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        EscherSimpleProperty prop = (EscherSimpleProperty)getEscherProperty(opt, EscherProperties.TEXT__TEXTBOTTOM);
        int val = prop == null ? EMU_PER_INCH/20 : prop.getPropertyValue();
        return val/EMU_PER_POINT;
    }

    /**
     * Sets the botom margin.
     * @see #getMarginBottom()
     *
     * @param margin    the bottom margin
     */
    public void setMarginBottom(int margin){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        setEscherProperty(opt, EscherProperties.TEXT__TEXTBOTTOM, margin*EMU_PER_POINT);
    }

    /**
     *  Returns the distance (in EMUs) between the left edge of the text frame
     *  and the left edge of the inscribed rectangle of the shape that contains
     *  the text.
     *  Default value is 1/10 inch.
     *
     * @return the left margin
     */
    public int getMarginLeft(){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        EscherSimpleProperty prop = (EscherSimpleProperty)getEscherProperty(opt, EscherProperties.TEXT__TEXTBOTTOM);
        int val = prop == null ? EMU_PER_INCH/10 : prop.getPropertyValue();
        return val/EMU_PER_POINT;
    }

    /**
     * Sets the left margin.
     * @see #getMarginLeft()
     *
     * @param margin    the left margin
     */
    public void setMarginLeft(int margin){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        setEscherProperty(opt, EscherProperties.TEXT__TEXTLEFT, margin*EMU_PER_POINT);
    }

    /**
     *  Returns the distance (in EMUs) between the right edge of the
     *  text frame and the right edge of the inscribed rectangle of the shape
     *  that contains the text.
     *  Default value is 1/10 inch.
     *
     * @return the right margin
     */
    public int getMarginRight(){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        EscherSimpleProperty prop = (EscherSimpleProperty)getEscherProperty(opt, EscherProperties.TEXT__TEXTRIGHT);
        int val = prop == null ? EMU_PER_INCH/10 : prop.getPropertyValue();
        return val/EMU_PER_POINT;
    }

    /**
     * Sets the right margin.
     * @see #getMarginRight()
     *
     * @param margin    the right margin
     */
    public void setMarginRight(int margin){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        setEscherProperty(opt, EscherProperties.TEXT__TEXTRIGHT, margin*EMU_PER_POINT);
    }

     /**
     *  Returns the distance (in EMUs) between the top of the text frame
     *  and the top of the inscribed rectangle of the shape that contains the text.
     *  Default value is 1/20 inch.
     *
     * @return the top margin
     */
    public int getMarginTop(){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        EscherSimpleProperty prop = (EscherSimpleProperty)getEscherProperty(opt, EscherProperties.TEXT__TEXTTOP);
        int val = prop == null ? EMU_PER_INCH/20 : prop.getPropertyValue();
        return val/EMU_PER_POINT;
    }

   /**
     * Sets the top margin.
     * @see #getMarginTop()
     *
     * @param margin    the top margin
     */
    public void setMarginTop(int margin){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        setEscherProperty(opt, EscherProperties.TEXT__TEXTTOP, margin*EMU_PER_POINT);
    }


    /**
     * Returns the value indicating word wrap.
     * One of the <code>Wrap*</code> constants defined in this class.
     *
     * @return the value indicating word wrap
     */
    public int getWordWrap(){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        EscherSimpleProperty prop = (EscherSimpleProperty)getEscherProperty(opt, EscherProperties.TEXT__WRAPTEXT);
        return prop == null ? WrapSquare : prop.getPropertyValue();
    }

    /**
     *  Specifies how the text should be wrapped
     *
     * @param wrap  the value indicating how the text should be wrapped
     */
    public void setWordWrap(int wrap){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        setEscherProperty(opt, EscherProperties.TEXT__WRAPTEXT, wrap);
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
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        setEscherProperty(opt, EscherProperties.TEXT__TEXTID, id);
    }

    /**
     * The color used to fill this shape.
     *
     * @param color the background color
     */
    public void setBackgroundColor(Color color){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        int rgb = new Color(color.getBlue(), color.getGreen(), color.getRed(), 0).getRGB();
        setEscherProperty(opt, EscherProperties.FILL__FILLBACKCOLOR, rgb);
    }

    /**
      * @return the TextRun object for this text box
      */
     public TextRun getTextRun(){
         return _txtrun;
     }

     public void setSheet(Sheet sheet){
        _sheet = sheet;

        // Initialize _txtrun object.
        // (We can't do it in the constructor because the sheet
        //  is not assigned then, it's only built once we have
        //  all the records)
        if(_txtrun == null) initTextRun();
        if(_txtrun == null) {
        	// No text records found, skip
        	_missingTextRecords = true;
        	return;
        } else {
        	_missingTextRecords = false;
        }
        
        // Supply the sheet to our child RichTextRuns
        _txtrun.setSheet(sheet);
        RichTextRun[] rt = _txtrun.getRichTextRuns();
        for (int i = 0; i < rt.length; i++) {
            rt[i].supplySlideShow(_sheet.getSlideShow());
        }
    }

    private void initTextRun(){
        OutlineTextRefAtom ota = null;
        
        // Find the interesting child records 
        Record[] child = _txtbox.getChildRecords();
        for (int i = 0; i < child.length; i++) {
            if (child[i] instanceof OutlineTextRefAtom) {
                ota = (OutlineTextRefAtom)child[i];
                break;
            }
        }

        Sheet sheet = getSheet();
        TextRun[] runs = sheet.getTextRuns();
        if (ota != null) {
            int idx = ota.getTextIndex();
            for (int i = 0; i < runs.length; i++) {
                if(runs[i].getIndex() == idx){
                    _txtrun = runs[i];
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
}
