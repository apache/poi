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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;

import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherDgRecord;
import org.apache.poi.ddf.EscherDggRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.hslf.model.HeadersFooters;
import org.apache.poi.hslf.record.CString;
import org.apache.poi.hslf.record.ColorSchemeAtom;
import org.apache.poi.hslf.record.HeadersFootersContainer;
import org.apache.poi.hslf.record.PPDrawing;
import org.apache.poi.hslf.record.RecordContainer;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.hslf.record.SheetContainer;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.util.Internal;

/**
 * This class defines the common format of "Sheets" in a powerpoint
 * document. Such sheets could be Slides, Notes, Master etc
 */

public abstract class HSLFSheet implements HSLFShapeContainer, Sheet<HSLFShape,HSLFTextParagraph> {
    /**
     * The {@code SlideShow} we belong to
     */
    private HSLFSlideShow _slideShow;

    /**
     * Sheet background
     */
    private HSLFBackground _background;

    /**
     * Record container that holds sheet data.
     * For slides it is org.apache.poi.hslf.record.Slide,
     * for notes it is org.apache.poi.hslf.record.Notes,
     * for slide masters it is org.apache.poi.hslf.record.SlideMaster, etc.
     */
    private final SheetContainer _container;

    private final int _sheetNo;

    public HSLFSheet(SheetContainer container, int sheetNo) {
        _container = container;
        _sheetNo = sheetNo;
    }

    /**
     * Returns an array of all the TextRuns in the sheet.
     */
    public abstract List<List<HSLFTextParagraph>> getTextParagraphs();

    /**
     * Returns the (internal, RefID based) sheet number, as used
     * to in PersistPtr stuff.
     */
    public int _getSheetRefId() {
        return _container.getSheetId();
    }

    /**
     * Returns the (internal, SlideIdentifier based) sheet number, as used
     * to reference this sheet from other records.
     */
    public int _getSheetNumber() {
        return _sheetNo;
    }

    /**
     * Fetch the PPDrawing from the underlying record
     */
    public PPDrawing getPPDrawing() {
        return _container.getPPDrawing();
    }

    /**
     * Fetch the SlideShow we're attached to
     */
    @Override
    public HSLFSlideShow getSlideShow() {
        return _slideShow;
    }

    /**
     * Return record container for this sheet
     */
    public SheetContainer getSheetContainer() {
        return _container;
    }

    /**
     * Set the SlideShow we're attached to.
     * Also passes it on to our child text paragraphs
     */
    @Internal
    protected void setSlideShow(HSLFSlideShow ss) {
        if (_slideShow != null) {
            throw new HSLFException("Can't change existing slideshow reference");
        }

        _slideShow = ss;
        List<List<HSLFTextParagraph>> trs = getTextParagraphs();
        if (trs == null) {
            return;
        }
        for (List<HSLFTextParagraph> ltp : trs) {
            HSLFTextParagraph.supplySheet(ltp, this);
            HSLFTextParagraph.applyHyperlinks(ltp);
        }
    }


    /**
     * Returns all shapes contained in this Sheet
     *
     * @return all shapes contained in this Sheet (Slide or Notes)
     */
    @Override
    public List<HSLFShape> getShapes() {
        PPDrawing ppdrawing = getPPDrawing();

        EscherContainerRecord dg = ppdrawing.getDgContainer();
        EscherContainerRecord spgr = null;

        for (EscherRecord rec : dg) {
            if (rec.getRecordId() == EscherContainerRecord.SPGR_CONTAINER) {
                spgr = (EscherContainerRecord) rec;
                break;
            }
        }
        if (spgr == null) {
            throw new IllegalStateException("spgr not found");
        }

        List<HSLFShape> shapeList = new ArrayList<>();
        boolean isFirst = true;
        for (EscherRecord r : spgr) {
            if (isFirst) {
                // skip first item
                isFirst = false;
                continue;
            }

            EscherContainerRecord sp = (EscherContainerRecord)r;
            HSLFShape sh = HSLFShapeFactory.createShape(sp, null);
            sh.setSheet(this);

            if (sh instanceof HSLFSimpleShape) {
                HSLFHyperlink link = HSLFHyperlink.find(sh);
                if (link != null) {
                    ((HSLFSimpleShape)sh).setHyperlink(link);
                }
            }

            shapeList.add(sh);
        }

        return shapeList;
    }

    /**
     * Add a new Shape to this Slide
     *
     * @param shape - the Shape to add
     */
    @Override
    public void addShape(HSLFShape shape) {
        PPDrawing ppdrawing = getPPDrawing();

        EscherContainerRecord dgContainer = ppdrawing.getDgContainer();
        EscherContainerRecord spgr = HSLFShape.getEscherChild(dgContainer, EscherContainerRecord.SPGR_CONTAINER);
        spgr.addChildRecord(shape.getSpContainer());

        shape.setSheet(this);
        shape.setShapeId(allocateShapeId());
        shape.afterInsert(this);
    }

    /**
     * Allocates new shape id for the new drawing group id.
     *
     * @return a new shape id.
     */
    public int allocateShapeId() {
        EscherDggRecord dgg = _slideShow.getDocumentRecord().getPPDrawingGroup().getEscherDggRecord();
        EscherDgRecord dg = _container.getPPDrawing().getEscherDgRecord();
        return dgg.allocateShapeId(dg, false);
    }

    /**
     * Removes the specified shape from this sheet.
     *
     * @param shape shape to be removed from this sheet, if present.
     * @return {@code true} if the shape was deleted.
     */
    @Override
    public boolean removeShape(HSLFShape shape) {
        PPDrawing ppdrawing = getPPDrawing();

        EscherContainerRecord dg = ppdrawing.getDgContainer();
        EscherContainerRecord spgr = dg.getChildById(EscherContainerRecord.SPGR_CONTAINER);
        if(spgr == null) {
            return false;
        }

        return spgr.removeChildRecord(shape.getSpContainer());
    }

    /**
     * Called by SlideShow ater a new sheet is created
     */
    public void onCreate(){

    }

    /**
     * Return the master sheet .
     */
    @Override
    public abstract HSLFMasterSheet getMasterSheet();

    /**
     * Color scheme for this sheet.
     */
    public ColorSchemeAtom getColorScheme() {
        return _container.getColorScheme();
    }

    /**
     * Returns the background shape for this sheet.
     *
     * @return the background shape for this sheet.
     */
    @Override
    public HSLFBackground getBackground() {
        if (_background == null) {
            PPDrawing ppdrawing = getPPDrawing();

            EscherContainerRecord dg = ppdrawing.getDgContainer();
            EscherContainerRecord spContainer = dg.getChildById(EscherContainerRecord.SP_CONTAINER);
            _background = new HSLFBackground(spContainer, null);
            _background.setSheet(this);
        }
        return _background;
    }

    @Override
    public void draw(Graphics2D graphics) {
        DrawFactory drawFact = DrawFactory.getInstance(graphics);
        Drawable draw = drawFact.getDrawable(this);
        draw.draw(graphics);
    }

    /**
     * Subclasses should call this method and update the array of text runs
     * when a text shape is added
     */
    protected void onAddTextShape(HSLFTextShape shape) {
    }

    /**
     * Return placeholder by text type
     *
     * @param type  type of text, See {@link org.apache.poi.hslf.record.TextHeaderAtom}
     * @return  {@code TextShape} or {@code null}
     */
    public HSLFTextShape getPlaceholderByTextType(int type){
        for (HSLFShape shape : getShapes()) {
            if(shape instanceof HSLFTextShape){
                HSLFTextShape tx = (HSLFTextShape)shape;
                if (tx.getRunType() == type) {
                    return tx;
                }
            }
        }
        return null;
    }

    /**
     * Search placeholder by its type
     *
     * @param type  type of placeholder to search. See {@link org.apache.poi.hslf.record.OEPlaceholderAtom}
     * @return  {@code SimpleShape} or {@code null}
     */
    public HSLFSimpleShape getPlaceholder(Placeholder type){
        for (HSLFShape shape : getShapes()) {
            if (shape instanceof HSLFSimpleShape) {
                HSLFSimpleShape ss = (HSLFSimpleShape)shape;
                if (type == ss.getPlaceholder()) {
                    return ss;
                }
            }
        }
        return null;
    }

    /**
     * Return programmable tag associated with this sheet, e.g. {@code ___PPT12}.
     *
     * @return programmable tag associated with this sheet.
     */
    public String getProgrammableTag(){
        String tag = null;
        RecordContainer progTags = (RecordContainer)
                getSheetContainer().findFirstOfType(
                            RecordTypes.ProgTags.typeID
        );
        if(progTags != null) {
            RecordContainer progBinaryTag = (RecordContainer)
                progTags.findFirstOfType(
                        RecordTypes.ProgBinaryTag.typeID
            );
            if(progBinaryTag != null) {
                CString binaryTag = (CString)
                    progBinaryTag.findFirstOfType(
                            RecordTypes.CString.typeID
                );
                if(binaryTag != null) {
                    tag = binaryTag.getText();
                }
            }
        }

        return tag;

    }

    @Override
    public Iterator<HSLFShape> iterator() {
        return getShapes().iterator();
    }

    /**
     * @since POI 5.2.0
     */
    @Override
    public Spliterator<HSLFShape> spliterator() {
        return getShapes().spliterator();
    }

    /**
     * @return whether shapes on the master sheet should be shown. By default master graphics is turned off.
     * Sheets that support the notion of master (slide, slideLayout) should override it and
     * check this setting
     */
    @Override
    public boolean getFollowMasterGraphics() {
        return false;
    }


    @Override
    public HSLFTextBox createTextBox() {
        HSLFTextBox s = new HSLFTextBox();
        s.setHorizontalCentered(true);
        s.setAnchor(new Rectangle2D.Double(0, 0, 100, 100));
        addShape(s);
        return s;
    }

    @Override
    public HSLFAutoShape createAutoShape() {
        HSLFAutoShape s = new HSLFAutoShape(ShapeType.RECT);
        s.setHorizontalCentered(true);
        s.setAnchor(new Rectangle2D.Double(0, 0, 100, 100));
        addShape(s);
        return s;
    }

    @Override
    public HSLFFreeformShape createFreeform() {
        HSLFFreeformShape s = new HSLFFreeformShape();
        s.setHorizontalCentered(true);
        s.setAnchor(new Rectangle2D.Double(0, 0, 100, 100));
        addShape(s);
        return s;
    }

    @Override
    public HSLFConnectorShape createConnector() {
        HSLFConnectorShape s = new HSLFConnectorShape();
        s.setAnchor(new Rectangle2D.Double(0, 0, 100, 100));
        addShape(s);
        return s;
    }

    @Override
    public HSLFGroupShape createGroup() {
        HSLFGroupShape s = new HSLFGroupShape();
        s.setAnchor(new Rectangle2D.Double(0, 0, 100, 100));
        addShape(s);
        return s;
    }

    @Override
    public HSLFPictureShape createPicture(PictureData pictureData) {
        if (!(pictureData instanceof HSLFPictureData)) {
            throw new IllegalArgumentException("pictureData needs to be of type HSLFPictureData");
        }
        HSLFPictureShape s = new HSLFPictureShape((HSLFPictureData)pictureData);
        s.setAnchor(new Rectangle2D.Double(0, 0, 100, 100));
        addShape(s);
        return s;
    }

    @Override
    public HSLFTable createTable(int numRows, int numCols) {
        if (numRows < 1 || numCols < 1) {
            throw new IllegalArgumentException("numRows and numCols must be greater than 0");
        }
        HSLFTable s = new HSLFTable(numRows,numCols);
        // anchor is set in constructor based on numRows/numCols
        addShape(s);
        return s;
    }

    @Override
    public HSLFObjectShape createOleShape(PictureData pictureData) {
        if (!(pictureData instanceof HSLFPictureData)) {
            throw new IllegalArgumentException("pictureData needs to be of type HSLFPictureData");
        }
        HSLFObjectShape s = new HSLFObjectShape((HSLFPictureData)pictureData);
        s.setAnchor(new Rectangle2D.Double(0, 0, 100, 100));
        addShape(s);
        return s;
    }

    /**
     * Header / Footer settings for this slide.
     *
     * @return Header / Footer settings for this slide
     */
    public HeadersFooters getHeadersFooters() {
        return new HeadersFooters(this, HeadersFootersContainer.SlideHeadersFootersContainer);
    }


    @Override
    public HSLFPlaceholderDetails getPlaceholderDetails(Placeholder placeholder) {
        final HSLFSimpleShape ph = getPlaceholder(placeholder);
        return (ph == null) ? null : new HSLFShapePlaceholderDetails(ph);
    }
}
