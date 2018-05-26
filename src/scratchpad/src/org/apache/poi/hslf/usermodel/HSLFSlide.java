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
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherDgRecord;
import org.apache.poi.ddf.EscherDggRecord;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.hslf.model.HeadersFooters;
import org.apache.poi.hslf.record.CString;
import org.apache.poi.hslf.record.ColorSchemeAtom;
import org.apache.poi.hslf.record.Comment2000;
import org.apache.poi.hslf.record.EscherTextboxWrapper;
import org.apache.poi.hslf.record.HeadersFootersContainer;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RecordContainer;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.hslf.record.SSSlideInfoAtom;
import org.apache.poi.hslf.record.SlideAtom;
import org.apache.poi.hslf.record.SlideAtomLayout.SlideLayoutType;
import org.apache.poi.hslf.record.SlideListWithText.SlideAtomsSet;
import org.apache.poi.hslf.record.StyleTextProp9Atom;
import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.usermodel.Notes;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.Slide;

/**
 * This class represents a slide in a PowerPoint Document. It allows
 *  access to the text within, and the layout. For now, it only does
 *  the text side of things though
 *
 * @author Nick Burch
 * @author Yegor Kozlov
 */

public final class HSLFSlide extends HSLFSheet implements Slide<HSLFShape,HSLFTextParagraph> {
	private int _slideNo;
	private SlideAtomsSet _atomSet;
	private final List<List<HSLFTextParagraph>> _paragraphs = new ArrayList<>();
	private HSLFNotes _notes; // usermodel needs to set this

	/**
	 * Constructs a Slide from the Slide record, and the SlideAtomsSet
	 *  containing the text.
	 * Initializes TextRuns, to provide easier access to the text
	 *
	 * @param slide the Slide record we're based on
	 * @param notes the Notes sheet attached to us
	 * @param atomSet the SlideAtomsSet to get the text from
	 */
	public HSLFSlide(org.apache.poi.hslf.record.Slide slide, HSLFNotes notes, SlideAtomsSet atomSet, int slideIdentifier, int slideNumber) {
        super(slide, slideIdentifier);

		_notes = notes;
		_atomSet = atomSet;
		_slideNo = slideNumber;

		// For the text coming in from the SlideAtomsSet:
		// Build up TextRuns from pairs of TextHeaderAtom and
		//  one of TextBytesAtom or TextCharsAtom
		if (_atomSet != null && _atomSet.getSlideRecords().length > 0) {
		    // Grab text from SlideListWithTexts entries
		    _paragraphs.addAll(HSLFTextParagraph.findTextParagraphs(_atomSet.getSlideRecords()));
	        if (_paragraphs.isEmpty()) {
	            throw new HSLFException("No text records found for slide");
	        }
		}

		// Grab text from slide's PPDrawing
		for (List<HSLFTextParagraph> l : HSLFTextParagraph.findTextParagraphs(getPPDrawing(), this)) {
		    if (!_paragraphs.contains(l)) {
                _paragraphs.add(l);
            }
		}
	}

	/**
	* Create a new Slide instance
	* @param sheetNumber The internal number of the sheet, as used by PersistPtrHolder
	* @param slideNumber The user facing number of the sheet
	*/
	public HSLFSlide(int sheetNumber, int sheetRefId, int slideNumber){
		super(new org.apache.poi.hslf.record.Slide(), sheetNumber);
		_slideNo = slideNumber;
        getSheetContainer().setSheetId(sheetRefId);
	}

    /**
     * Returns the Notes Sheet for this slide, or null if there isn't one
     */
    @Override
    public HSLFNotes getNotes() {
        return _notes;
    }

	/**
	 * Sets the Notes that are associated with this. Updates the
	 *  references in the records to point to the new ID
	 */
	@Override
	public void setNotes(Notes<HSLFShape,HSLFTextParagraph> notes) {
        if (notes != null && !(notes instanceof HSLFNotes)) {
            throw new IllegalArgumentException("notes needs to be of type HSLFNotes");
        }
		_notes = (HSLFNotes)notes;

		// Update the Slide Atom's ID of where to point to
		SlideAtom sa = getSlideRecord().getSlideAtom();

		if(_notes == null) {
			// Set to 0
			sa.setNotesID(0);
		} else {
			// Set to the value from the notes' sheet id
			sa.setNotesID(_notes._getSheetNumber());
		}
	}

	/**
	* Changes the Slide's (external facing) page number.
	* @see org.apache.poi.hslf.usermodel.HSLFSlideShow#reorderSlide(int, int)
	*/
	public void setSlideNumber(int newSlideNumber) {
		_slideNo = newSlideNumber;
	}

    /**
     * Called by SlideShow ater a new slide is created.
     * <p>
     * For Slide we need to do the following:
     *  <li> set id of the drawing group.
     *  <li> set shapeId for the container descriptor and background
     * </p>
     */
    @Override
    public void onCreate(){
        //initialize drawing group id
        EscherDggRecord dgg = getSlideShow().getDocumentRecord().getPPDrawingGroup().getEscherDggRecord();
        EscherContainerRecord dgContainer = getSheetContainer().getPPDrawing().getDgContainer();
        EscherDgRecord dg = HSLFShape.getEscherChild(dgContainer, EscherDgRecord.RECORD_ID);
        int dgId = dgg.getMaxDrawingGroupId() + 1;
        dg.setOptions((short)(dgId << 4));
        dgg.setDrawingsSaved(dgg.getDrawingsSaved() + 1);

        for (EscherContainerRecord c : dgContainer.getChildContainers()) {
            EscherSpRecord spr = null;
            switch(c.getRecordId()){
                case EscherContainerRecord.SPGR_CONTAINER:
                    EscherContainerRecord dc = (EscherContainerRecord)c.getChild(0);
                    spr = dc.getChildById(EscherSpRecord.RECORD_ID);
                    break;
                case EscherContainerRecord.SP_CONTAINER:
                    spr = c.getChildById(EscherSpRecord.RECORD_ID);
                    break;
                default:
                    break;
            }
            if(spr != null) {
                spr.setShapeId(allocateShapeId());
            }
        }

        //PPT doen't increment the number of saved shapes for group descriptor and background
        dg.setNumShapes(1);
    }

	/**
	 * Create a <code>TextBox</code> object that represents the slide's title.
	 *
	 * @return <code>TextBox</code> object that represents the slide's title.
	 */
	public HSLFTextBox addTitle() {
		HSLFPlaceholder pl = new HSLFPlaceholder();
		pl.setShapeType(ShapeType.RECT);
		pl.setPlaceholder(Placeholder.TITLE);
		pl.setRunType(TextHeaderAtom.TITLE_TYPE);
		pl.setText("Click to edit title");
		pl.setAnchor(new java.awt.Rectangle(54, 48, 612, 90));
		addShape(pl);
		return pl;
	}


	// Complex Accesser methods follow

	/**
	 * <p>
	 * The title is a run of text of type <code>TextHeaderAtom.CENTER_TITLE_TYPE</code> or
	 * <code>TextHeaderAtom.TITLE_TYPE</code>
	 * </p>
	 *
	 * @see TextHeaderAtom
	 */
	@Override
	public String getTitle(){
		for (List<HSLFTextParagraph> tp : getTextParagraphs()) {
		    if (tp.isEmpty()) {
                continue;
            }
			int type = tp.get(0).getRunType();
			switch (type) {
    			case TextHeaderAtom.CENTER_TITLE_TYPE:
    			case TextHeaderAtom.TITLE_TYPE:
    			    String str = HSLFTextParagraph.getRawText(tp);
    			    return HSLFTextParagraph.toExternalString(str, type);
			}
		}
		return null;
	}

    @Override
	public String getSlideName() {
        final CString name = (CString)getSlideRecord().findFirstOfType(RecordTypes.CString.typeID);
        return name != null ? name.getText() : "Slide"+getSlideNumber();
    }


	/**
	 * Returns an array of all the TextRuns found
	 */
	@Override
    public List<List<HSLFTextParagraph>> getTextParagraphs() { return _paragraphs; }

	/**
	 * Returns the (public facing) page number of this slide
	 */
	@Override
	public int getSlideNumber() { return _slideNo; }

	/**
	 * Returns the underlying slide record
	 */
	public org.apache.poi.hslf.record.Slide getSlideRecord() {
        return (org.apache.poi.hslf.record.Slide)getSheetContainer();
    }

	/**
	 * @return set of records inside <code>SlideListWithtext</code> container
	 *  which hold text data for this slide (typically for placeholders).
	 */
	protected SlideAtomsSet getSlideAtomsSet() { return _atomSet;  }

    /**
     * Returns master sheet associated with this slide.
     * It can be either SlideMaster or TitleMaster objects.
     *
     * @return the master sheet associated with this slide.
     */
     @Override
    public HSLFMasterSheet getMasterSheet(){
        int masterId = getSlideRecord().getSlideAtom().getMasterID();
        for (HSLFSlideMaster sm : getSlideShow().getSlideMasters()) {
            if (masterId == sm._getSheetNumber()) {
                return sm;
            }
        }
        for (HSLFTitleMaster tm : getSlideShow().getTitleMasters()) {
            if (masterId == tm._getSheetNumber()) {
                return tm;
            }
        }
        return null;
    }

    /**
     * Change Master of this slide.
     */
    public void setMasterSheet(HSLFMasterSheet master){
        SlideAtom sa = getSlideRecord().getSlideAtom();
        int sheetNo = master._getSheetNumber();
        sa.setMasterID(sheetNo);
    }

    /**
     * Sets whether this slide follows master background
     *
     * @param flag  <code>true</code> if the slide follows master,
     * <code>false</code> otherwise
     */
    @Override
    public void setFollowMasterBackground(boolean flag){
        SlideAtom sa = getSlideRecord().getSlideAtom();
        sa.setFollowMasterBackground(flag);
    }

    /**
     * Whether this slide follows master sheet background
     *
     * @return <code>true</code> if the slide follows master background,
     * <code>false</code> otherwise
     */
    @Override
    public boolean getFollowMasterBackground(){
        SlideAtom sa = getSlideRecord().getSlideAtom();
        return sa.getFollowMasterBackground();
    }

    /**
     * Sets whether this slide draws master sheet objects
     *
     * @param flag  <code>true</code> if the slide draws master sheet objects,
     * <code>false</code> otherwise
     */
    @Override
    public void setFollowMasterObjects(boolean flag){
        SlideAtom sa = getSlideRecord().getSlideAtom();
        sa.setFollowMasterObjects(flag);
    }

    /**
     * Whether this slide follows master color scheme
     *
     * @return <code>true</code> if the slide follows master color scheme,
     * <code>false</code> otherwise
     */
    public boolean getFollowMasterScheme(){
        SlideAtom sa = getSlideRecord().getSlideAtom();
        return sa.getFollowMasterScheme();
    }

    /**
     * Sets whether this slide draws master color scheme
     *
     * @param flag  <code>true</code> if the slide draws master color scheme,
     * <code>false</code> otherwise
     */
    public void setFollowMasterScheme(boolean flag){
        SlideAtom sa = getSlideRecord().getSlideAtom();
        sa.setFollowMasterScheme(flag);
    }

    /**
     * Whether this slide draws master sheet objects
     *
     * @return <code>true</code> if the slide draws master sheet objects,
     * <code>false</code> otherwise
     */
    @Override
    public boolean getFollowMasterObjects(){
        SlideAtom sa = getSlideRecord().getSlideAtom();
        return sa.getFollowMasterObjects();
    }

    /**
     * Background for this slide.
     */
     @Override
    public HSLFBackground getBackground() {
        if (getFollowMasterBackground()) {
            final HSLFMasterSheet ms = getMasterSheet();
            return (ms == null) ? null : ms.getBackground();
        }
        return super.getBackground();
    }

    /**
     * Color scheme for this slide.
     */
    @Override
    public ColorSchemeAtom getColorScheme() {
        if (getFollowMasterScheme()) {
            final HSLFMasterSheet ms = getMasterSheet();
            return (ms == null) ? null : ms.getColorScheme();
        }
        return super.getColorScheme();
    }

    private static RecordContainer selectContainer(final RecordContainer root, final int index, final RecordTypes... path) {
        if (root == null || index >= path.length) {
            return root;
        }
        final RecordContainer newRoot = (RecordContainer) root.findFirstOfType(path[index].typeID);
        return selectContainer(newRoot, index+1, path);
    }

    /**
     * Get the comment(s) for this slide.
     * Note - for now, only works on PPT 2000 and
     *  PPT 2003 files. Doesn't work for PPT 97
     *  ones, as they do their comments oddly.
     */
    public List<HSLFComment> getComments() {
        final List<HSLFComment> comments = new ArrayList<>();
    	// If there are any, they're in
    	//  ProgTags -> ProgBinaryTag -> BinaryTagData
        final RecordContainer binaryTags =
                selectContainer(getSheetContainer(), 0,
                        RecordTypes.ProgTags, RecordTypes.ProgBinaryTag, RecordTypes.BinaryTagData);

        if (binaryTags != null) {
            for (final Record record : binaryTags.getChildRecords()) {
                if (record instanceof Comment2000) {
                    comments.add(new HSLFComment((Comment2000)record));
                }
            }
        }

    	return comments;
    }

    /**
     * Header / Footer settings for this slide.
     *
     * @return Header / Footer settings for this slide
     */
    public HeadersFooters getHeadersFooters(){
        return new HeadersFooters(this, HeadersFootersContainer.SlideHeadersFootersContainer);
    }

    @Override
    protected void onAddTextShape(HSLFTextShape shape) {
        List<HSLFTextParagraph> newParas = shape.getTextParagraphs();
        _paragraphs.add(newParas);
    }

    /** This will return an atom per TextBox, so if the page has two text boxes the method should return two atoms. */
    public StyleTextProp9Atom[] getNumberedListInfo() {
    	return this.getPPDrawing().getNumberedListInfo();
    }

	public EscherTextboxWrapper[] getTextboxWrappers() {
		return this.getPPDrawing().getTextboxWrappers();
	}

	@Override
	public void setHidden(boolean hidden) {
		org.apache.poi.hslf.record.Slide cont =	getSlideRecord();

		SSSlideInfoAtom slideInfo =
			(SSSlideInfoAtom)cont.findFirstOfType(RecordTypes.SSSlideInfoAtom.typeID);
		if (slideInfo == null) {
			slideInfo = new SSSlideInfoAtom();
			cont.addChildAfter(slideInfo, cont.findFirstOfType(RecordTypes.SlideAtom.typeID));
		}

		slideInfo.setEffectTransitionFlagByBit(SSSlideInfoAtom.HIDDEN_BIT, hidden);
	}

    @Override
	public boolean isHidden() {
		SSSlideInfoAtom slideInfo =
			(SSSlideInfoAtom)getSlideRecord().findFirstOfType(RecordTypes.SSSlideInfoAtom.typeID);
		return (slideInfo != null) && slideInfo.getEffectTransitionFlagByBit(SSSlideInfoAtom.HIDDEN_BIT);
	}

    @Override
    public void draw(Graphics2D graphics) {
        DrawFactory drawFact = DrawFactory.getInstance(graphics);
        Drawable draw = drawFact.getDrawable(this);
        draw.draw(graphics);
    }
    
    @Override
    public boolean getFollowMasterColourScheme() {
        return false;
    }

    @Override
    public void setFollowMasterColourScheme(boolean follow) {
    }
    
    @Override
    public boolean getFollowMasterGraphics() {
        return getFollowMasterObjects();
    }

    @Override
    public boolean getDisplayPlaceholder(final Placeholder placeholder) {
        final HeadersFooters hf = getHeadersFooters();
        final SlideLayoutType slt = getSlideRecord().getSlideAtom().getSSlideLayoutAtom().getGeometryType();
        final boolean isTitle =
            (slt == SlideLayoutType.TITLE_SLIDE || slt == SlideLayoutType.TITLE_ONLY || slt == SlideLayoutType.MASTER_TITLE);
        switch (placeholder) {
        case DATETIME:
            return hf.isDateTimeVisible() && !isTitle;
        case SLIDE_NUMBER:
            return hf.isSlideNumberVisible() && !isTitle;
        case HEADER:
            return hf.isHeaderVisible() && !isTitle;
        case FOOTER:
            return hf.isFooterVisible() && !isTitle;
        default:
            return false;
        }
    }

    @Override
    public HSLFMasterSheet getSlideLayout(){
        // TODO: find out how we can find the mastersheet base on the slide layout type, i.e.
        // getSlideRecord().getSlideAtom().getSSlideLayoutAtom().getGeometryType()
        return getMasterSheet();
    }

}
