
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

import java.util.Vector;
import java.awt.*;

import org.apache.poi.hslf.record.SlideAtom;
import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.hslf.record.ColorSchemeAtom;
import org.apache.poi.hslf.record.SlideListWithText.SlideAtomsSet;

/**
 * This class represents a slide in a PowerPoint Document. It allows 
 *  access to the text within, and the layout. For now, it only does
 *  the text side of things though
 *
 * @author Nick Burch
 * @author Yegor Kozlov
 */

public class Slide extends Sheet
{
	private int _slideNo;
	private SlideAtomsSet _atomSet;
	private TextRun[] _runs;
	private Notes _notes; // usermodel needs to set this

	/**
	 * Constructs a Slide from the Slide record, and the SlideAtomsSet
	 *  containing the text.
	 * Initialises TextRuns, to provide easier access to the text
	 *
	 * @param slide the Slide record we're based on
	 * @param notes the Notes sheet attached to us
	 * @param atomSet the SlideAtomsSet to get the text from
	 */
	public Slide(org.apache.poi.hslf.record.Slide slide, Notes notes, SlideAtomsSet atomSet, int slideIdentifier, int slideNumber) {
        super(slide, slideIdentifier);

		_notes = notes;
		_atomSet = atomSet;
		_slideNo = slideNumber;

 		// Grab the TextRuns from the PPDrawing
		TextRun[] _otherRuns = findTextRuns(getPPDrawing());

		// For the text coming in from the SlideAtomsSet:
		// Build up TextRuns from pairs of TextHeaderAtom and
		//  one of TextBytesAtom or TextCharsAtom
		Vector textRuns = new Vector();
		if(_atomSet != null) {
			findTextRuns(_atomSet.getSlideRecords(),textRuns);
		} else {
			// No text on the slide, must just be pictures
		}

		// Build an array, more useful than a vector
		_runs = new TextRun[textRuns.size()+_otherRuns.length];
		// Grab text from SlideListWithTexts entries
		int i=0;
		for(i=0; i<textRuns.size(); i++) {
			_runs[i] = (TextRun)textRuns.get(i);
            _runs[i].setSheet(this);
		}
		// Grab text from slide's PPDrawing
		for(int k=0; k<_otherRuns.length; i++, k++) {
			_runs[i] = _otherRuns[k];
            _runs[i].setSheet(this);
		}
	}
  
	/**
	* Create a new Slide instance
	* @param sheetNumber The internal number of the sheet, as used by PersistPtrHolder
	* @param slideNumber The user facing number of the sheet
	*/
	public Slide(int sheetNumber, int sheetRefId, int slideNumber){
		super(new org.apache.poi.hslf.record.Slide(), sheetNumber);
		_slideNo = slideNumber;
        getSheetContainer().setSheetId(sheetRefId);
	}

	/**
	 * Sets the Notes that are associated with this. Updates the
	 *  references in the records to point to the new ID
	 */
	public void setNotes(Notes notes) {
		_notes = notes;

		// Update the Slide Atom's ID of where to point to
		SlideAtom sa = getSlideRecord().getSlideAtom();

		if(notes == null) {
			// Set to 0
			sa.setNotesID(0);
		} else {
			// Set to the value from the notes' sheet id
			sa.setNotesID(notes._getSheetNumber());
		}
	}
  
	/**
	* Changes the Slide's (external facing) page number.
	* @see org.apache.poi.hslf.usermodel.SlideShow#reorderSlide(int, int)
	*/
	public void setSlideNumber(int newSlideNumber) {
		_slideNo = newSlideNumber;
	}
  
	/**
	 * Create a <code>TextBox</code> object that represents the slide's title.
	 *
	 * @return <code>TextBox</code> object that represents the slide's title.
	 */
	public TextBox addTitle() {
		Placeholder pl = new Placeholder();
		pl.setShapeType(ShapeTypes.Rectangle);
		pl.getTextRun().setRunType(TextHeaderAtom.TITLE_TYPE);
		pl.setText("Click to edit title");
		pl.setAnchor(new java.awt.Rectangle(54, 48, 612, 90));
		addShape(pl);
		return pl;
	}


	// Complex Accesser methods follow

	/**
	 * Return title of this slide or <code>null</code> if the slide does not have title.
	 * <p>
	 * The title is a run of text of type <code>TextHeaderAtom.CENTER_TITLE_TYPE</code> or
	 * <code>TextHeaderAtom.TITLE_TYPE</code>
	 * </p>
	 *
	 * @see TextHeaderAtom
	 *
	 * @return title of this slide
	 */
	public String getTitle(){
		TextRun[] txt = getTextRuns();
		for (int i = 0; i < txt.length; i++) {
			int type = txt[i].getRunType();
			if (type == TextHeaderAtom.CENTER_TITLE_TYPE ||
			type == TextHeaderAtom.TITLE_TYPE ){
				String title = txt[i].getText();
				return title;
			}
		}
		return null;
	}
  
	// Simple Accesser methods follow

	/**
	 * Returns an array of all the TextRuns found
	 */
	public TextRun[] getTextRuns() { return _runs; }

	/**
	 * Returns the (public facing) page number of this slide
	 */
	public int getSlideNumber() { return _slideNo; }

	/**
	 * Returns the underlying slide record
	 */
	public org.apache.poi.hslf.record.Slide getSlideRecord() {
        return (org.apache.poi.hslf.record.Slide)getSheetContainer();
    }

	/**
	 * Returns the Notes Sheet for this slide, or null if there isn't one
	 */
	public Notes getNotesSheet() { return _notes; }

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
     public MasterSheet getMasterSheet(){
        SlideMaster[] master = getSlideShow().getSlidesMasters();
        SlideAtom sa = getSlideRecord().getSlideAtom();
        int masterId = sa.getMasterID();
        MasterSheet sheet = null;
        for (int i = 0; i < master.length; i++) {
            if (masterId == master[i]._getSheetNumber()) {
                sheet = master[i];
                break;
            }
        }
        if (sheet == null){
            TitleMaster[] titleMaster = getSlideShow().getTitleMasters();
            if(titleMaster != null) for (int i = 0; i < titleMaster.length; i++) {
                if (masterId == titleMaster[i]._getSheetNumber()) {
                    sheet = titleMaster[i];
                    break;
                }
            }
        }
        return sheet;
    }

    /**
     * Change Master of this slide.
     */
    public void setMasterSheet(MasterSheet master){
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
    public boolean getFollowMasterObjects(){
        SlideAtom sa = getSlideRecord().getSlideAtom();
        return sa.getFollowMasterObjects();
    }

    /**
     * Background for this slide.
     */
     public Background getBackground() {
        if(getFollowMasterBackground())
            return getMasterSheet().getBackground();
        else
            return super.getBackground();
    }

    /**
     * Color scheme for this slide.
     */
    public ColorSchemeAtom getColorScheme() {
        if(getFollowMasterScheme()){
            return getMasterSheet().getColorScheme();
        }
        return super.getColorScheme();
    }

    public void draw(Graphics2D graphics){
        MasterSheet master = getMasterSheet();
        if(getFollowMasterBackground()) master.getBackground().draw(graphics);
        if(getFollowMasterObjects()){
            Shape[] sh = master.getShapes();
            for (int i = 0; i < sh.length; i++) {
                if(MasterSheet.isPlaceholder(sh[i])) continue;

                sh[i].draw(graphics);
            }
        }
        Shape[] sh = getShapes();
        for (int i = 0; i < sh.length; i++) {
            sh[i].draw(graphics);
        }
    }

}
