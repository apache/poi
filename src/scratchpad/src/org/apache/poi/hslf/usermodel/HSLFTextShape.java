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

import static org.apache.poi.hslf.record.RecordTypes.*;

import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.*;

import org.apache.poi.ddf.*;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.hslf.record.*;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.DrawTextShape;
import org.apache.poi.sl.usermodel.*;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Units;

/**
 * A common superclass of all shapes that can hold text.
 *
 * @author Yegor Kozlov
 */
public abstract class HSLFTextShape extends HSLFSimpleShape
implements TextShape<HSLFShape,HSLFTextParagraph> {

    /**
     * How to anchor the text
     */
    /* package */ static final int AnchorTop = 0;
    /* package */ static final int AnchorMiddle = 1;
    /* package */ static final int AnchorBottom = 2;
    /* package */ static final int AnchorTopCentered = 3;
    /* package */ static final int AnchorMiddleCentered = 4;
    /* package */ static final int AnchorBottomCentered = 5;
    /* package */ static final int AnchorTopBaseline = 6;
    /* package */ static final int AnchorBottomBaseline = 7;
    /* package */ static final int AnchorTopCenteredBaseline = 8;
    /* package */ static final int AnchorBottomCenteredBaseline = 9;

    /**
     * How to wrap the text
     */
    public static final int WrapSquare = 0;
    public static final int WrapByPoints = 1;
    public static final int WrapNone = 2;
    public static final int WrapTopBottom = 3;
    public static final int WrapThrough = 4;

    /**
     * TextRun object which holds actual text and format data
     */
    protected List<HSLFTextParagraph> _paragraphs = new ArrayList<HSLFTextParagraph>();

    /**
     * Escher container which holds text attributes such as
     * TextHeaderAtom, TextBytesAtom ot TextCharsAtom, StyleTextPropAtom etc.
     */
    protected EscherTextboxWrapper _txtbox;

    /**
     * This setting is used for supporting a deprecated alignment
     * 
     * @see <a href=""></a>
     */
    boolean alignToBaseline = false;
    
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
        _escherContainer = createSpContainer(parent instanceof HSLFGroupShape);
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
     * <code>PPDrawing</code> about it.
     *
     * @param sh the sheet we are adding to
     */
    protected void afterInsert(HSLFSheet sh){
        super.afterInsert(sh);

        storeText();
        
        EscherTextboxWrapper thisTxtbox = getEscherTextboxWrapper();
        if(thisTxtbox != null){
            _escherContainer.addChildRecord(thisTxtbox.getEscherRecord());
            
            PPDrawing ppdrawing = sh.getPPDrawing();
            ppdrawing.addTextboxWrapper(thisTxtbox);
            // Ensure the escher layer knows about the added records
            try {
                thisTxtbox.writeOut(null);
            } catch (IOException e){
                throw new HSLFException(e);
            }
            if(getAnchor().equals(new Rectangle()) && !"".equals(getText())) resizeToFitText();
        }
        for (HSLFTextParagraph htp : _paragraphs) {
            htp.setShapeId(getShapeId());
        }
        sh.onAddTextShape(this);
    }

    protected EscherTextboxWrapper getEscherTextboxWrapper(){
        if(_txtbox != null) return _txtbox;
        
        EscherTextboxRecord textRecord = getEscherChild(EscherTextboxRecord.RECORD_ID);
        if (textRecord == null) return null;
        
        HSLFSheet sheet = getSheet();
        if (sheet != null) {
            PPDrawing drawing = sheet.getPPDrawing();
            if (drawing != null) {
                EscherTextboxWrapper wrappers[] = drawing.getTextboxWrappers();
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

    /**
     * Adjust the size of the shape so it encompasses the text inside it.
     *
     * @return a <code>Rectangle2D</code> that is the bounds of this shape.
     */
    public Rectangle2D resizeToFitText(){
        Rectangle anchor = getAnchor();
        if(anchor.getWidth() == 0.) {
            logger.log(POILogger.WARN, "Width of shape wasn't set. Defaulting to 200px");
            anchor.setSize(200, (int)anchor.getHeight());
            setAnchor(anchor);
        }
        double height = getTextHeight(); 
        height += 1; // add a pixel to compensate rounding errors
        
        anchor.setRect(anchor.getX(), anchor.getY(), anchor.getWidth(), height);
        setAnchor(anchor);
        
        return anchor;
    }   
    
    /**
    * Returns the type of the text, from the TextHeaderAtom.
    * Possible values can be seen from TextHeaderAtom
    * @see org.apache.poi.hslf.record.TextHeaderAtom
    */
    public int getRunType() {
        getEscherTextboxWrapper();
        if (_txtbox == null) return -1;
        List<HSLFTextParagraph> paras = HSLFTextParagraph.findTextParagraphs(_txtbox, getSheet());
        return (paras.isEmpty()) ? -1 : paras.get(0).getRunType();
    }

    /**
    * Changes the type of the text. Values should be taken
    *  from TextHeaderAtom. No checking is done to ensure you
    *  set this to a valid value!
    * @see org.apache.poi.hslf.record.TextHeaderAtom
    */
    public void setRunType(int type) {
        getEscherTextboxWrapper();
        if (_txtbox == null) return;
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
    /* package */ int getAlignment(){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherProperties.TEXT__ANCHORTEXT);
        int align = HSLFTextShape.AnchorTop;
        if (prop == null){
            /**
             * If vertical alignment was not found in the shape properties then try to
             * fetch the master shape and search for the align property there.
             */
            int type = getRunType();
            if(getSheet() != null && getSheet().getMasterSheet() != null){
                HSLFMasterSheet master = getSheet().getMasterSheet();
                HSLFTextShape masterShape = master.getPlaceholderByTextType(type);
                if(masterShape != null) align = masterShape.getAlignment();
            } else {
                //not found in the master sheet. Use the hardcoded defaults.
                switch (type){
                     case org.apache.poi.hslf.record.TextHeaderAtom.TITLE_TYPE:
                     case org.apache.poi.hslf.record.TextHeaderAtom.CENTER_TITLE_TYPE:
                         align = HSLFTextShape.AnchorMiddle;
                         break;
                     default:
                         align = HSLFTextShape.AnchorTop;
                         break;
                 }
            }
        } else {
            align = prop.getPropertyValue();
        }

        alignToBaseline = (align == AnchorBottomBaseline || align == AnchorBottomCenteredBaseline
            || align == AnchorTopBaseline || align == AnchorTopCenteredBaseline);
        
        return align;
    }

    /**
     * Sets the type of alignment for the text.
     * One of the <code>Anchor*</code> constants defined in this class.
     *
     * @param align - the type of alignment
     */
    /* package */ void setAlignment(Boolean isCentered, VerticalAlignment vAlign) {
        int align[];
        switch (vAlign) {
        case TOP:
            align = new int[]{AnchorTop, AnchorTopCentered, AnchorTopBaseline, AnchorTopCenteredBaseline};
            break;
        default:
        case MIDDLE:
            align = new int[]{AnchorMiddle, AnchorMiddleCentered, AnchorMiddle, AnchorMiddleCentered};
            break;
        case BOTTOM:
            align = new int[]{AnchorBottom, AnchorBottomCentered, AnchorBottomBaseline, AnchorBottomCenteredBaseline};
            break;
        }
        
        int align2 = align[(isCentered ? 1 : 0)+(alignToBaseline ? 2 : 0)];
        
        setEscherProperty(EscherProperties.TEXT__ANCHORTEXT, align2);
    }
    
    @Override
    public VerticalAlignment getVerticalAlignment() {
        int va = getAlignment();
        switch (va) {
        case AnchorTop:
        case AnchorTopCentered:
        case AnchorTopBaseline:
        case AnchorTopCenteredBaseline: return VerticalAlignment.TOP;
        case AnchorBottom:
        case AnchorBottomCentered:
        case AnchorBottomBaseline:
        case AnchorBottomCenteredBaseline: return VerticalAlignment.BOTTOM;
        default:
        case AnchorMiddle:
        case AnchorMiddleCentered: return VerticalAlignment.MIDDLE;
        }
    }

    /**
     * @return true, if vertical alignment is relative to baseline
     * this is only used for older versions less equals Office 2003 
     */
    public boolean isAlignToBaseline() {
        getAlignment();
        return alignToBaseline;
    }

    /**
     * Sets the vertical alignment relative to the baseline
     *
     * @param alignToBaseline if true, vertical alignment is relative to baseline
     */
    public void setAlignToBaseline(boolean alignToBaseline) {
        this.alignToBaseline = alignToBaseline;
        setAlignment(isHorizontalCentered(), getVerticalAlignment());
    }
    
    @Override
    public boolean isHorizontalCentered() {
        int va = getAlignment();
        switch (va) {
        case AnchorTopCentered:
        case AnchorTopCenteredBaseline:
        case AnchorBottomCentered:
        case AnchorBottomCenteredBaseline:
        case AnchorMiddleCentered:
            return true;
        default:
            return false;
        }
    }
    
    public void setVerticalAlignment(VerticalAlignment vAlign) {
        setAlignment(isHorizontalCentered(), vAlign);
    }

    /**
     * Sets if the paragraphs are horizontal centered
     *
     * @param isCentered true, if the paragraphs are horizontal centered
     * A {@code null} values unsets this property.
     * 
     * @see TextShape#isHorizontalCentered()
     */
    public void setHorizontalCentered(Boolean isCentered){
        setAlignment(isCentered, getVerticalAlignment());
    }

    /**
     * Returns the distance (in points) between the bottom of the text frame
     * and the bottom of the inscribed rectangle of the shape that contains the text.
     * Default value is 1/20 inch.
     *
     * @return the botom margin
     */
    public double getBottomInset(){
        return getInset(EscherProperties.TEXT__TEXTBOTTOM, .05);
    }

    /**
     * Sets the botom margin.
     * @see #getBottomInset()
     *
     * @param margin    the bottom margin
     */
    public void setBottomInset(double margin){
        setInset(EscherProperties.TEXT__TEXTBOTTOM, margin);
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
        return getInset(EscherProperties.TEXT__TEXTLEFT, .1);
    }

    /**
     * Sets the left margin.
     * @see #getLeftInset()
     *
     * @param margin    the left margin
     */
    public void setLeftInset(double margin){
        setInset(EscherProperties.TEXT__TEXTLEFT, margin);
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
        return getInset(EscherProperties.TEXT__TEXTRIGHT, .1);
    }

    /**
     * Sets the right margin.
     * @see #getRightInset()
     *
     * @param margin    the right margin
     */
    public void setRightInset(double margin){
        setInset(EscherProperties.TEXT__TEXTRIGHT, margin);
    }

     /**
     *  Returns the distance (in points) between the top of the text frame
     *  and the top of the inscribed rectangle of the shape that contains the text.
     *  Default value is 1/20 inch.
     *
     * @return the top margin
     */
    public double getTopInset(){
        return getInset(EscherProperties.TEXT__TEXTTOP, .05);
    }

   /**
     * Sets the top margin.
     * @see #getTopInset()
     *
     * @param margin    the top margin
     */
    public void setTopInset(double margin){
        setInset(EscherProperties.TEXT__TEXTTOP, margin);
    }

    /**
     * Returns the distance (in points) between the edge of the text frame
     * and the edge of the inscribed rectangle of the shape that contains the text.
     * Default value is 1/20 inch.
     * 
     * @param propId the id of the inset edge
     * @return the inset in points
     */
    private double getInset(short propId, double defaultInch) {
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, propId);
        int val = prop == null ? (int)(Units.toEMU(Units.POINT_DPI)*defaultInch) : prop.getPropertyValue();
        return Units.toPoints(val);        
    }

    /**
     * @param propId the id of the inset edge
     * @param margin the inset in points
     */
    private void setInset(short propId, double margin){
        setEscherProperty(propId, Units.toEMU(margin));
    }    
    
    @Override
    public boolean getWordWrap(){
        int ww = getWordWrapEx();
        return (ww != WrapNone);
    }

    /**
     * Returns the value indicating word wrap.
     *
     * @return the value indicating word wrap.
     *  Must be one of the <code>Wrap*</code> constants defined in this class.
     *
     * @see <a href="https://msdn.microsoft.com/en-us/library/dd948168(v=office.12).aspx">MSOWRAPMODE</a>
     */
    public int getWordWrapEx() {
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherProperties.TEXT__WRAPTEXT);
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
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherProperties.TEXT__TEXTID);
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

    @Override
    public List<HSLFTextParagraph> getTextParagraphs(){
        if (!_paragraphs.isEmpty()) return _paragraphs;
        
        _txtbox = getEscherTextboxWrapper();
        if (_txtbox == null) {
            _paragraphs.addAll(HSLFTextParagraph.createEmptyParagraph());
            _txtbox = _paragraphs.get(0).getTextboxWrapper();
        } else {
            _paragraphs = HSLFTextParagraph.findTextParagraphs(_txtbox, getSheet());
            if (_paragraphs == null) {
                // there are actually TextBoxRecords without extra data - see #54722
                _paragraphs = HSLFTextParagraph.createEmptyParagraph(_txtbox);
            }
                
            if (_paragraphs.isEmpty()) {
                logger.log(POILogger.WARN, "TextRecord didn't contained any text lines");
            }
//            initParagraphsFromSheetRecords();
//            if (_paragraphs.isEmpty()) {
//                List<List<HSLFTextParagraph>> llhtp = HSLFTextParagraph.findTextParagraphs(_txtbox);
//                if (!llhtp.isEmpty()) {
//                    _paragraphs.addAll(llhtp.get(0));
//                }
//            }
        }

        for (HSLFTextParagraph p : _paragraphs) {
            p.setParentShape(this);
        }
        
        return _paragraphs;
    }

    public void setSheet(HSLFSheet sheet) {
        _sheet = sheet;

        // Initialize _txtrun object.
        // (We can't do it in the constructor because the sheet
        //  is not assigned then, it's only built once we have
        //  all the records)
        List<HSLFTextParagraph> paras = getTextParagraphs();
        if (paras != null) {
            for (HSLFTextParagraph htp : paras) {
                // Supply the sheet to our child RichTextRuns
                htp.supplySheet(_sheet);
            }
        }
    }

//    protected void initParagraphsFromSheetRecords(){
//        EscherTextboxWrapper txtbox = getEscherTextboxWrapper();
//        HSLFSheet sheet = getSheet();
//
//        if (sheet == null || txtbox == null) return;
//        List<List<HSLFTextParagraph>> sheetRuns = _sheet.getTextParagraphs();
//        if (sheetRuns == null) return;
//
//        _paragraphs.clear();
//        OutlineTextRefAtom ota = (OutlineTextRefAtom)txtbox.findFirstOfType(OutlineTextRefAtom.typeID);
//
//        if (ota != null) {
//            int idx = ota.getTextIndex();
//            for (List<HSLFTextParagraph> r : sheetRuns) {
//                if (r.isEmpty()) continue;
//                int ridx = r.get(0).getIndex();
//                if (ridx > idx) break;
//                if (ridx == idx) _paragraphs.addAll(r);
//            }
//            if(_paragraphs.isEmpty()) {
//                logger.log(POILogger.WARN, "text run not found for OutlineTextRefAtom.TextIndex=" + idx);
//            }
//        } else {
//            int shapeId = getShapeId();
//            for (List<HSLFTextParagraph> r : sheetRuns) {
//                if (r.isEmpty()) continue;
//                if (r.get(0).getShapeId() == shapeId) _paragraphs.addAll(r);
//            }
//        }
//
//        // ensure the same references child records of TextRun - see #48916
////        if(_txtrun != null) {
////            for (int i = 0; i < child.length; i++) {
////                for (Record r : _txtrun.getRecords()) {
////                    if (child[i].getRecordType() == r.getRecordType()) {
////                        child[i] = r;
////                    }
////                }
////            }
////        }
//    }

    /*
        // 0xB acts like cariage return in page titles and like blank in the others
        char replChr;
        switch(tha == null ? -1 : tha.getTextType()) {
            case -1:
            case TextHeaderAtom.TITLE_TYPE:
            case TextHeaderAtom.CENTER_TITLE_TYPE:
                replChr = '\n';
                break;
            default:
                replChr = ' ';
                break;
        }

        // PowerPoint seems to store files with \r as the line break
        // The messes things up on everything but a Mac, so translate
        //  them to \n
        String text = rawText.replace('\r','\n').replace('\u000b', replChr);
     */
    
    /**
     * Return <code>OEPlaceholderAtom</code>, the atom that describes a placeholder.
     *
     * @return <code>OEPlaceholderAtom</code> or <code>null</code> if not found
     */
    public OEPlaceholderAtom getPlaceholderAtom(){
        return getClientDataRecord(OEPlaceholderAtom.typeID);
    }

    /**
     *
     * Assigns a hyperlink to this text shape
     *
     * @param linkId    id of the hyperlink, @see org.apache.poi.hslf.usermodel.SlideShow#addHyperlink(Hyperlink)
     * @param      beginIndex   the beginning index, inclusive.
     * @param      endIndex     the ending index, exclusive.
     * @see org.apache.poi.hslf.usermodel.HSLFSlideShow#addHyperlink(HSLFHyperlink)
     */
    public void setHyperlink(int linkId, int beginIndex, int endIndex){
        //TODO validate beginIndex and endIndex and throw IllegalArgumentException

        InteractiveInfo info = new InteractiveInfo();
        InteractiveInfoAtom infoAtom = info.getInteractiveInfoAtom();
        infoAtom.setAction(org.apache.poi.hslf.record.InteractiveInfoAtom.ACTION_HYPERLINK);
        infoAtom.setHyperlinkType(org.apache.poi.hslf.record.InteractiveInfoAtom.LINK_Url);
        infoAtom.setHyperlinkID(linkId);

        _txtbox.appendChildRecord(info);

        TxInteractiveInfoAtom txiatom = new TxInteractiveInfoAtom();
        txiatom.setStartIndex(beginIndex);
        txiatom.setEndIndex(endIndex);
        _txtbox.appendChildRecord(txiatom);

    }

    @Override
    public boolean isPlaceholder() {
        OEPlaceholderAtom oep = getPlaceholderAtom();
        if (oep != null) return true;

        //special case for files saved in Office 2007
        RoundTripHFPlaceholder12 hldr = getClientDataRecord(RoundTripHFPlaceholder12.typeID);
        if (hldr != null) return true;

        return false;
    }


    @Override
    public Iterator<HSLFTextParagraph> iterator() {
        return _paragraphs.iterator();
    }

    @Override
    public Insets2D getInsets() {
        Insets2D insets = new Insets2D(getTopInset(), getLeftInset(), getBottomInset(), getRightInset());
        return insets;
    }

    @Override
    public double getTextHeight(){
        DrawFactory drawFact = DrawFactory.getInstance(null);
        DrawTextShape dts = drawFact.getDrawable(this);
        return dts.getTextHeight();
    }

    @Override
    public TextDirection getTextDirection() {
        // TODO: determine vertical text setting
        return TextDirection.HORIZONTAL;
    }

    /**
     * Returns the raw text content of the shape. This hasn't had any
     * changes applied to it, and so is probably unlikely to print
     * out nicely.
     */
    public String getRawText() {
        return HSLFTextParagraph.getRawText(getTextParagraphs());
    }

    /**
     * Returns the text contained in this text frame, which has been made safe
     * for printing and other use.
     *
     * @return the text string for this textbox.
     */
    public String getText() {
        String rawText = getRawText();
        return HSLFTextParagraph.toExternalString(rawText, getRunType());
    }

    
    // Update methods follow

      /**
       * Adds the supplied text onto the end of the TextParagraphs,
       * creating a new RichTextRun for it to sit in.
       * 
       * @param text the text string used by this object.
       */
      public HSLFTextRun appendText(String text, boolean newParagraph) {
          // init paragraphs
          List<HSLFTextParagraph> paras = getTextParagraphs();
          return HSLFTextParagraph.appendText(paras, text, newParagraph);
      }

      /**
       * Sets (overwrites) the current text.
       * Uses the properties of the first paragraph / textrun
       * 
       * @param text the text string used by this object.
       * 
       * @return the last text run of the splitted text
       */
      public HSLFTextRun setText(String text) {
          // init paragraphs
          List<HSLFTextParagraph> paras = getTextParagraphs();
          HSLFTextRun htr = HSLFTextParagraph.setText(paras, text);
          setTextId(text.hashCode());
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
      // Accesser methods follow

    /**
     * Returns the array of all hyperlinks in this text run
     * 
     * @return the array of all hyperlinks in this text run or <code>null</code>
     *         if not found.
     */
    public List<HSLFHyperlink> getHyperlinks() {
        return HSLFHyperlink.find(this);
    }


}