
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

import java.util.*;
import java.awt.Dimension;
import java.io.*;

import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.hslf.*;
import org.apache.poi.hslf.model.*;
import org.apache.poi.hslf.model.Notes;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.record.SlideListWithText.*;
import org.apache.poi.hslf.record.*;
import org.apache.poi.hslf.exceptions.CorruptPowerPointFileException;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.util.ArrayUtil;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * This class is a friendly wrapper on top of the more scary HSLFSlideShow.
 *
 * TODO:
 *  - figure out how to match notes to their correct sheet
 *    (will involve understanding DocSlideList and DocNotesList)
 *  - handle Slide creation cleaner
 * 
 * @author Nick Burch
 * @author Yegor kozlov
 */

public class SlideShow
{
  // What we're based on
  private HSLFSlideShow _hslfSlideShow;

  // Low level contents, as taken from HSLFSlideShow
  private Record[] _records;

  // Pointers to the most recent versions of the core records
  //  (Document, Notes, Slide etc)
  private Record[] _mostRecentCoreRecords;
  // Lookup between the PersitPtr "sheet" IDs, and the position
  //  in the mostRecentCoreRecords array
  private Hashtable _sheetIdToCoreRecordsLookup;
  // Used when adding new core records
  private int _highestSheetId;
  
  // Records that are interesting
  private Document _documentRecord;

  // Friendly objects for people to deal with
  private SlideMaster[] _masters;
  private TitleMaster[] _titleMasters;
  private Slide[] _slides;
  private Notes[] _notes;
  private FontCollection _fonts;

  // For logging
    private POILogger logger = POILogFactory.getLogger(this.getClass());

  
  /* ===============================================================
   *                       Setup Code
   * ===============================================================
   */
  

  /**
   * Constructs a Powerpoint document from the underlying 
   * HSLFSlideShow object. Finds the model stuff from this
   *
   * @param hslfSlideShow the HSLFSlideShow to base on
   */
  public SlideShow(HSLFSlideShow hslfSlideShow) throws IOException
  {
	// Get useful things from our base slideshow
    _hslfSlideShow = hslfSlideShow;
	_records = _hslfSlideShow.getRecords();
	
	// Handle Parent-aware Reocrds
	for(int i=0; i<_records.length; i++) {
		handleParentAwareRecords(_records[i]);
	}

	// Find the versions of the core records we'll want to use
	findMostRecentCoreRecords();
	
	// Build up the model level Slides and Notes
	buildSlidesAndNotes();
  }
  
  /**
   * Constructs a new, empty, Powerpoint document.
   */
  public SlideShow() throws IOException {
	this(new HSLFSlideShow());
  }

    /**
     * Constructs a Powerpoint document from an input stream.
     */
    public SlideShow(InputStream inputStream) throws IOException {
      this(new HSLFSlideShow(inputStream));
    }

  /**
   * Find the records that are parent-aware, and tell them
   *  who their parent is
   */
  private void handleParentAwareRecords(Record baseRecord) {
	  // Only need to do something if this is a container record
	  if(baseRecord instanceof RecordContainer) {
		RecordContainer br = (RecordContainer)baseRecord;
		Record[] childRecords = br.getChildRecords();
		
		// Loop over child records, looking for interesting ones
		for(int i=0; i<childRecords.length; i++) {
			Record record = childRecords[i];
			// Tell parent aware records of their parent
			if(record instanceof ParentAwareRecord) {
				((ParentAwareRecord)record).setParentRecord(br);
			}
			// Walk on down for the case of container records
			if(record instanceof RecordContainer) {
				handleParentAwareRecords(record);
			}
		}
	  }
  }


  /**
   * Use the PersistPtrHolder entries to figure out what is
   *  the "most recent" version of all the core records
   *  (Document, Notes, Slide etc), and save a record of them.
   * Do this by walking from the oldest PersistPtr to the newest,
   *  overwriting any references found along the way with newer ones
   */
  private void findMostRecentCoreRecords() {
	// To start with, find the most recent in the byte offset domain
	Hashtable mostRecentByBytes = new Hashtable();
	for(int i=0; i<_records.length; i++) {
		if(_records[i] instanceof PersistPtrHolder) {
			PersistPtrHolder pph = (PersistPtrHolder)_records[i];

			// If we've already seen any of the "slide" IDs for this 
			//  PersistPtr, remove their old positions
			int[] ids = pph.getKnownSlideIDs();
			for(int j=0; j<ids.length; j++) {
				Integer id = new Integer(ids[j]);
				if( mostRecentByBytes.containsKey(id)) {
					mostRecentByBytes.remove(id);
				}	
			}

			// Now, update the byte level locations with their latest values
			Hashtable thisSetOfLocations = pph.getSlideLocationsLookup();
			for(int j=0; j<ids.length; j++) {
				Integer id = new Integer(ids[j]);
				mostRecentByBytes.put(id, thisSetOfLocations.get(id));
			}
		}
	}

	// We now know how many unique special records we have, so init
	//  the array
	_mostRecentCoreRecords = new Record[mostRecentByBytes.size()];
	
	// We'll also want to be able to turn the slide IDs into a position
	//  in this array
	_sheetIdToCoreRecordsLookup = new Hashtable();
	int[] allIDs = new int[_mostRecentCoreRecords.length];
	Enumeration ids = mostRecentByBytes.keys();
	for(int i=0; i<allIDs.length; i++) {
		Integer id = (Integer)ids.nextElement();
		allIDs[i] = id.intValue();
	}
	Arrays.sort(allIDs);
	for(int i=0; i<allIDs.length; i++) {
		_sheetIdToCoreRecordsLookup.put(new Integer(allIDs[i]), new Integer(i));
	}
	// Capture the ID of the highest sheet
	_highestSheetId = allIDs[(allIDs.length-1)];

	// Now convert the byte offsets back into record offsets
	for(int i=0; i<_records.length; i++) {
		if(_records[i] instanceof PositionDependentRecord) {
			PositionDependentRecord pdr = (PositionDependentRecord)_records[i];
			Integer recordAt = new Integer(pdr.getLastOnDiskOffset());

			// Is it one we care about?
			for(int j=0; j<allIDs.length; j++) {
				Integer thisID = new Integer(allIDs[j]);
				Integer thatRecordAt = (Integer)mostRecentByBytes.get(thisID);

				if(thatRecordAt.equals(recordAt)) {
					// Bingo. Now, where do we store it?
					Integer storeAtI = 
						(Integer)_sheetIdToCoreRecordsLookup.get(thisID);
					int storeAt = storeAtI.intValue();
					
					// Tell it its Sheet ID, if it cares
					if(pdr instanceof PositionDependentRecordContainer) {
						PositionDependentRecordContainer pdrc = 
							(PositionDependentRecordContainer)_records[i];
						pdrc.setSheetId(thisID.intValue());
					}
					
					// Finally, save the record
					_mostRecentCoreRecords[storeAt] = _records[i];
				}
			}
		}
	}
	
	// Now look for the interesting records in there
	for(int i=0; i<_mostRecentCoreRecords.length; i++) {
		// Check there really is a record at this number
		if(_mostRecentCoreRecords[i] != null) {
			// Find the Document, and interesting things in it
			if(_mostRecentCoreRecords[i].getRecordType() == RecordTypes.Document.typeID) {
				_documentRecord = (Document)_mostRecentCoreRecords[i];
				_fonts = _documentRecord.getEnvironment().getFontCollection();
			}
		} else {
			// No record at this number
			// Odd, but not normally a problem
		}
	}
  }
  
  	/**
  	 * For a given SlideAtomsSet, return the core record, based on the refID from the
  	 *  SlidePersistAtom
  	 */
	private Record getCoreRecordForSAS(SlideAtomsSet sas) {
		SlidePersistAtom spa = sas.getSlidePersistAtom();
		int refID = spa.getRefID();
		return getCoreRecordForRefID(refID);
	}
  
	/**
   	 * For a given refID (the internal, 0 based numbering scheme), return the
	 *  core record
	 * @param refID the refID
	 */
	private Record getCoreRecordForRefID(int refID) {
		Integer coreRecordId = (Integer)
			_sheetIdToCoreRecordsLookup.get(new Integer(refID));
		if(coreRecordId != null) {
			Record r = _mostRecentCoreRecords[coreRecordId.intValue()];
			return r;
		} else {
			logger.log(POILogger.ERROR, "We tried to look up a reference to a core record, but there was no core ID for reference ID " + refID);
			return null;
		}
	}

  /**
   * Build up model level Slide and Notes objects, from the underlying
   *  records.
   */
  private void buildSlidesAndNotes() {
	// Ensure we really found a Document record earlier
	// If we didn't, then the file is probably corrupt
	if(_documentRecord == null) {
		throw new CorruptPowerPointFileException("The PowerPoint file didn't contain a Document Record in its PersistPtr blocks. It is probably corrupt.");
	}


	// Fetch the SlideListWithTexts in the most up-to-date Document Record
	//
	// As far as we understand it:
	//  * The first SlideListWithText will contain a SlideAtomsSet
	//     for each of the master slides
	//  * The second SlideListWithText will contain a SlideAtomsSet
	//     for each of the slides, in their current order
	//    These SlideAtomsSets will normally contain text
	//  * The third SlideListWithText (if present), will contain a
	//     SlideAtomsSet for each Notes
	//    These SlideAtomsSets will not normally contain text
	//
	// Having indentified the masters, slides and notes + their orders,
	//  we have to go and find their matching records
	// We always use the latest versions of these records, and use the
	//  SlideAtom/NotesAtom to match them with the StyleAtomSet 

	SlideListWithText masterSLWT = _documentRecord.getMasterSlideListWithText();
	SlideListWithText slidesSLWT = _documentRecord.getSlideSlideListWithText();
	SlideListWithText notesSLWT  = _documentRecord.getNotesSlideListWithText();

    // Find master slides
	// These can be MainMaster records, but oddly they can also be
	//  Slides or Notes, and possibly even other odd stuff....
	// About the only thing you can say is that the master details are in
	//  the first SLWT.
    SlideAtomsSet[] masterSets = new SlideAtomsSet[0];
    if (masterSLWT != null){
        masterSets = masterSLWT.getSlideAtomsSets();

		ArrayList mmr = new ArrayList();
        ArrayList tmr = new ArrayList();

		for(int i=0; i<masterSets.length; i++) {
			Record r = getCoreRecordForSAS(masterSets[i]);
            SlideAtomsSet sas = masterSets[i];
            int sheetNo = sas.getSlidePersistAtom().getSlideIdentifier();
			if(r instanceof org.apache.poi.hslf.record.Slide) {
                TitleMaster master = new TitleMaster((org.apache.poi.hslf.record.Slide)r, sheetNo);
                master.setSlideShow(this);
                tmr.add(master);
			} else if(r instanceof org.apache.poi.hslf.record.MainMaster) {
                SlideMaster master = new SlideMaster((org.apache.poi.hslf.record.MainMaster)r, sheetNo);
                master.setSlideShow(this);
                mmr.add(master);
            }
		}

        _masters = new SlideMaster[mmr.size()];
        mmr.toArray(_masters);

        _titleMasters = new TitleMaster[tmr.size()];
        tmr.toArray(_titleMasters);

    }


	// Having sorted out the masters, that leaves the notes and slides


	// Start by finding the notes records to go with the entries in
	//  notesSLWT
	org.apache.poi.hslf.record.Notes[] notesRecords;
	SlideAtomsSet[] notesSets = new SlideAtomsSet[0];
	Hashtable slideIdToNotes = new Hashtable();
	if(notesSLWT == null) {
		// None
		notesRecords = new org.apache.poi.hslf.record.Notes[0]; 
	} else {
		// Match up the records and the SlideAtomSets
		notesSets = notesSLWT.getSlideAtomsSets();
		ArrayList notesRecordsL = new ArrayList();
		for(int i=0; i<notesSets.length; i++) {
			// Get the right core record
			Record r = getCoreRecordForSAS(notesSets[i]);

			// Ensure it really is a notes record
			if(r instanceof org.apache.poi.hslf.record.Notes) {
                org.apache.poi.hslf.record.Notes notesRecord = (org.apache.poi.hslf.record.Notes)r;
				notesRecordsL.add( notesRecord );

				// Record the match between slide id and these notes
                SlidePersistAtom spa = notesSets[i].getSlidePersistAtom();
                Integer slideId = new Integer(spa.getSlideIdentifier());
                slideIdToNotes.put(slideId, new Integer(i));
			} else {
				logger.log(POILogger.ERROR, "A Notes SlideAtomSet at " + i + " said its record was at refID " + notesSets[i].getSlidePersistAtom().getRefID() + ", but that was actually a " + r);
			}
		}
		notesRecords = new org.apache.poi.hslf.record.Notes[notesRecordsL.size()];
		notesRecords = (org.apache.poi.hslf.record.Notes[])
			notesRecordsL.toArray(notesRecords);
	}
	
	// Now, do the same thing for our slides
	org.apache.poi.hslf.record.Slide[] slidesRecords;
	SlideAtomsSet[] slidesSets = new SlideAtomsSet[0];
	if(slidesSLWT == null) {
		// None
		slidesRecords = new org.apache.poi.hslf.record.Slide[0]; 
	} else {
		// Match up the records and the SlideAtomSets
		slidesSets = slidesSLWT.getSlideAtomsSets();
		slidesRecords = new org.apache.poi.hslf.record.Slide[slidesSets.length];
		for(int i=0; i<slidesSets.length; i++) {
			// Get the right core record
			Record r = getCoreRecordForSAS(slidesSets[i]);
			
			// Ensure it really is a slide record
			if(r instanceof org.apache.poi.hslf.record.Slide) {
				slidesRecords[i] = (org.apache.poi.hslf.record.Slide)r;
			} else {
				logger.log(POILogger.ERROR, "A Slide SlideAtomSet at " + i + " said its record was at refID " + slidesSets[i].getSlidePersistAtom().getRefID() + ", but that was actually a " + r);
			}
		}
	}
	
	// Finally, generate model objects for everything
	// Notes first
	_notes = new Notes[notesRecords.length];
	for(int i=0; i<_notes.length; i++) {
		_notes[i] = new Notes(notesRecords[i]);
		_notes[i].setSlideShow(this);
	}
	// Then slides
	_slides = new Slide[slidesRecords.length];
	for(int i=0; i<_slides.length; i++) {
		SlideAtomsSet sas = slidesSets[i];
		int slideIdentifier = sas.getSlidePersistAtom().getSlideIdentifier();

		// Do we have a notes for this?
		Notes notes = null;
        //Slide.SlideAtom.notesId references the corresponding notes slide. 0 if slide has no notes.
        int noteId = slidesRecords[i].getSlideAtom().getNotesID();
        if (noteId != 0){
            Integer notesPos = (Integer)slideIdToNotes.get(new Integer(noteId));
            if (notesPos != null) notes = _notes[notesPos.intValue()];
            else logger.log(POILogger.ERROR, "Notes not found for noteId=" + noteId);
        }

		// Now, build our slide
		_slides[i] = new Slide(slidesRecords[i], notes, sas, slideIdentifier, (i+1));
		_slides[i].setSlideShow(this);
	}
  }

  /**
   * Writes out the slideshow file the is represented by an instance of
   *  this class
   * @param out The OutputStream to write to.
   *  @throws IOException If there is an unexpected IOException from the passed
   *            in OutputStream
   */
   public void write(OutputStream out) throws IOException {
	_hslfSlideShow.write(out);
   }


   /* ===============================================================
    *                       Accessor Code
    * ===============================================================
    */
   

	/**
	 * Returns an array of the most recent version of all the interesting
	 *  records
	 */
	public Record[] getMostRecentCoreRecords() { return _mostRecentCoreRecords; }

	/**
	 * Returns an array of all the normal Slides found in the slideshow
	 */
	public Slide[] getSlides() { return _slides; }

	/**
	 * Returns an array of all the normal Notes found in the slideshow
	 */
	public Notes[] getNotes() { return _notes; }

	/**
     * Returns an array of all the normal Slide Masters found in the slideshow
	 */
    public SlideMaster[] getSlidesMasters() { return _masters; }

    /**
     * Returns an array of all the normal Title Masters found in the slideshow
     */
    public TitleMaster[] getTitleMasters() { return _titleMasters; }
	/**
	 * Returns the data of all the pictures attached to the SlideShow
	 */
	public PictureData[] getPictureData() {
		return _hslfSlideShow.getPictures();
	}

    /**
     * Returns the data of all the embedded OLE object in the SlideShow
     */
    public ObjectData[] getEmbeddedObjects() {
        return _hslfSlideShow.getEmbeddedObjects();
    }

    /**
     * Returns the data of all the embedded sounds in the SlideShow
     */
    public SoundData[] getSoundData() {
        return SoundData.find(_documentRecord);
    }

	/**
	 * Return the current page size
	 */
	public Dimension getPageSize(){
		DocumentAtom docatom = _documentRecord.getDocumentAtom();
		int pgx = (int)docatom.getSlideSizeX()*Shape.POINT_DPI/Shape.MASTER_DPI;
		int pgy = (int)docatom.getSlideSizeY()*Shape.POINT_DPI/Shape.MASTER_DPI;
		return new Dimension(pgx, pgy);
	}
	
	/**
	 * Change the current page size
	 * 
	 * @param pgsize page size (in points)
	 */
	public void setPageSize(Dimension pgsize){
		DocumentAtom docatom = _documentRecord.getDocumentAtom();
		docatom.setSlideSizeX(pgsize.width*Shape.MASTER_DPI/Shape.POINT_DPI);
		docatom.setSlideSizeY(pgsize.height*Shape.MASTER_DPI/Shape.POINT_DPI);
	}
	
	/**
	 * Helper method for usermodel: Get the font collection
	 */
	protected FontCollection getFontCollection() { return _fonts; }
	/**
	 * Helper method for usermodel and model: Get the document record
	 */
	public Document getDocumentRecord() { return _documentRecord; }

	
	/* ===============================================================
	 *                       Re-ordering Code
	 * ===============================================================
	 */
	   
	
	/**
	 * Re-orders a slide, to a new position.
	 * @param oldSlideNumer The old slide number (1 based)
	 * @param newSlideNumber The new slide number (1 based)
	 */
	public void reorderSlide(int oldSlideNumer, int newSlideNumber) {
		// Ensure these numbers are valid
		if(oldSlideNumer < 1 || newSlideNumber < 1) {
			throw new IllegalArgumentException("Old and new slide numbers must be greater than 0");
		}
		if(oldSlideNumer > _slides.length || newSlideNumber > _slides.length) {
			throw new IllegalArgumentException("Old and new slide numbers must not exceed the number of slides (" + _slides.length + ")");
		}
		
		// Shift the SlideAtomsSet
		SlideListWithText slwt = _documentRecord.getSlideSlideListWithText(); 
		slwt.repositionSlideAtomsSet( 
				slwt.getSlideAtomsSets()[(oldSlideNumer-1)],
				(newSlideNumber-1)
		);
		
		// Re-order the slides
		ArrayUtil.arrayMoveWithin(_slides, (oldSlideNumer-1), (newSlideNumber-1), 1);
		
		// Tell the appropriate slides their new numbers
		for(int i=0; i<_slides.length; i++) {
			_slides[i].setSlideNumber( (i+1) );
		}
	}

	/* ===============================================================
	 *                       Addition Code
	 * ===============================================================
	 */
	   

	/**
	 * Create a blank <code>Slide</code>.
	 *
	 * @return  the created <code>Slide</code>
	 * @throws IOException
	 */
  	public Slide createSlide() throws IOException {
  		SlideListWithText slist = null;

  		// We need to add the records to the SLWT that deals
  		//  with Slides.
  		// Add it, if it doesn't exist
  		slist = _documentRecord.getSlideSlideListWithText();
  		if(slist == null) {
  			// Need to add a new one
  			slist = new SlideListWithText();
  			_documentRecord.addSlideListWithText(slist);
  		}

  		// Grab the SlidePersistAtom with the highest Slide Number.
  		// (Will stay as null if no SlidePersistAtom exists yet in
  		//  the slide, or only master slide's ones do)
  		SlidePersistAtom prev = null;
		SlideAtomsSet[] sas = slist.getSlideAtomsSets();
  		for(int j=0; j<sas.length; j++) {
  			SlidePersistAtom spa = sas[j].getSlidePersistAtom();
  			if(spa.getSlideIdentifier() < 0) {
  				// This is for a master slide
  				// Odd, since we only deal with the Slide SLWT
  			} else {
				// Must be for a real slide
  				if(prev == null) { prev = spa; }
  				if(prev.getSlideIdentifier() < spa.getSlideIdentifier()) {
  					prev = spa;
  				}
  			}
  		}
  		
  		// Set up a new  SlidePersistAtom for this slide 
  		SlidePersistAtom sp = new SlidePersistAtom();

  		// Reference is the 1-based index of the slide container in 
  		//  the PersistPtr root.
  		// It always starts with 3 (1 is Document, 2 is MainMaster, 3 is 
  		//  the first slide), but quicksaves etc can leave gaps
  		_highestSheetId++;
  		sp.setRefID(_highestSheetId);
  		// First slideId is always 256
  		sp.setSlideIdentifier(prev == null ? 256 : (prev.getSlideIdentifier() + 1));
  		
  		// Add this new SlidePersistAtom to the SlideListWithText
  		slist.addSlidePersistAtom(sp);
  		
  		
  		// Create a new Slide
  		Slide slide = new Slide(sp.getSlideIdentifier(), sp.getRefID(), _slides.length+1);
  		// Add in to the list of Slides
  		Slide[] s = new Slide[_slides.length+1];
  		System.arraycopy(_slides, 0, s, 0, _slides.length);
  		s[_slides.length] = slide;
  		_slides = s;
  		logger.log(POILogger.INFO, "Added slide " + _slides.length + " with ref " + sp.getRefID() + " and identifier " + sp.getSlideIdentifier());
  		
  		// Add the core records for this new Slide to the record tree
  		org.apache.poi.hslf.record.Slide slideRecord = slide.getSlideRecord();
  		slideRecord.setSheetId(sp.getRefID());
  		int slideRecordPos = _hslfSlideShow.appendRootLevelRecord(slideRecord);
  		_records = _hslfSlideShow.getRecords();

  		// Add the new Slide into the PersistPtr stuff
  		int offset = 0;
  		int slideOffset = 0;
  		PersistPtrHolder ptr = null;
  		UserEditAtom usr = null;
  		for (int i = 0; i < _records.length; i++) {
  			Record record = _records[i];
  			ByteArrayOutputStream out = new ByteArrayOutputStream();
  			record.writeOut(out);
  			
  			// Grab interesting records as they come past
  			if(_records[i].getRecordType() == RecordTypes.PersistPtrIncrementalBlock.typeID){
  				ptr = (PersistPtrHolder)_records[i];
  			}
  			if(_records[i].getRecordType() == RecordTypes.UserEditAtom.typeID) {
  				usr = (UserEditAtom)_records[i];
  			}
  			
  			if(i == slideRecordPos) {
  				slideOffset = offset;
  			}
  			offset += out.size();
  		}
  		
		// Add the new slide into the last PersistPtr
  		// (Also need to tell it where it is)
		slideRecord.setLastOnDiskOffset(slideOffset);
		ptr.addSlideLookup(sp.getRefID(), slideOffset);
		logger.log(POILogger.INFO, "New slide ended up at " + slideOffset);

		// Last view is now of the slide
  		usr.setLastViewType((short)UserEditAtom.LAST_VIEW_SLIDE_VIEW);
  		usr.setMaxPersistWritten(_highestSheetId);

  		// All done and added
  		slide.setSlideShow(this);
  		return slide;
	}


    /**
     * Adds a picture to this presentation and returns the associated index.
     *
     * @param data      picture data
     * @param format    the format of the picture.  One of constans defined in the <code>Picture</code> class.
     * @return          the index to this picture (1 based).
     */
    public int addPicture(byte[] data, int format) throws IOException {
        byte[] uid = PictureData.getChecksum(data);

        EscherContainerRecord bstore;
        int offset = 0;

        EscherContainerRecord dggContainer = _documentRecord.getPPDrawingGroup().getDggContainer();
        bstore = (EscherContainerRecord)Shape.getEscherChild(dggContainer, EscherContainerRecord.BSTORE_CONTAINER);
        if (bstore == null){
            bstore = new EscherContainerRecord();
            bstore.setRecordId( EscherContainerRecord.BSTORE_CONTAINER);

            List child = dggContainer.getChildRecords();
            for ( int i = 0; i < child.size(); i++ ) {
                EscherRecord rec = (EscherRecord)child.get(i);
                if (rec.getRecordId() == EscherOptRecord.RECORD_ID){
                    child.add(i, bstore);
                    i++;
                }
            }
            dggContainer.setChildRecords(child);
        } else {
            List lst = bstore.getChildRecords();
            for ( int i = 0; i < lst.size(); i++ ) {
                EscherBSERecord bse = (EscherBSERecord) lst.get(i);
                if (Arrays.equals(bse.getUid(), uid)){
                    return i + 1;
                }
                offset += bse.getSize();
             }
        }

        PictureData pict = PictureData.create(format);
        pict.setData(data);
        pict.setOffset(offset);

        EscherBSERecord bse = new EscherBSERecord();
        bse.setRecordId(EscherBSERecord.RECORD_ID);
        bse.setOptions( (short) ( 0x0002 | ( format << 4 ) ) );
        bse.setSize(pict.getRawData().length + 8);
        bse.setUid(uid);

        bse.setBlipTypeMacOS((byte)format);
        bse.setBlipTypeWin32((byte)format);

        if (format == Picture.EMF) bse.setBlipTypeMacOS((byte)Picture.PICT);
        else if (format == Picture.WMF) bse.setBlipTypeMacOS((byte)Picture.PICT);
        else if (format == Picture.PICT) bse.setBlipTypeWin32((byte)Picture.WMF);

        bse.setRef(1);
        bse.setOffset(offset);

        bstore.addChildRecord(bse);
        int count = bstore.getChildRecords().size();
        bstore.setOptions((short)( (count << 4) | 0xF ));

        _hslfSlideShow.addPicture(pict);

        return count;
    }

    /**
     * Adds a picture to this presentation and returns the associated index.
     *
     * @param pict       the file containing the image to add
     * @param format    the format of the picture.  One of constans defined in the <code>Picture</code> class.
     * @return          the index to this picture (1 based).
     */
    public int addPicture(File pict, int format) throws IOException {
        int length = (int)pict.length();
        byte[] data = new byte[length];
        try {
            FileInputStream is = new FileInputStream(pict);
            is.read(data);
            is.close();
        } catch (IOException e){
            throw new HSLFException(e);
        }
        return addPicture(data, format);
    }

    /**
     * Add a font in this presentation
     *
     * @param font the font to add
     * @return 0-based index of the font
     */
    public int addFont(PPFont font) {
        FontCollection fonts = getDocumentRecord().getEnvironment().getFontCollection();
        int idx = fonts.getFontIndex(font.getFontName());
        if(idx == -1){
            idx = fonts.addFont(font.getFontName(), font.getCharSet(), font.getFontFlags(), font.getFontType(), font.getPitchAndFamily());
        }
        return idx;
    }

    /**
     * Get a font by index
     *
     * @param idx 0-based index of the font
     * @return of an instance of <code>PPFont</code> or <code>null</code> if not found
     */
    public PPFont getFont(int idx) {
        PPFont font = null;
        FontCollection fonts = getDocumentRecord().getEnvironment().getFontCollection();
        Record[] ch = fonts.getChildRecords();
        for (int i = 0; i < ch.length; i++) {
            if(ch[i] instanceof FontEntityAtom) {
                FontEntityAtom atom = (FontEntityAtom)ch[i];
                if(atom.getFontIndex() == idx){
                    font = new PPFont(atom);
                    break;
                }
            }
        }
        return font;
    }

    /**
     * get the number of fonts in the presentation
     *
     * @return number of fonts
     */
    public int getNumberOfFonts() {
        return getDocumentRecord().getEnvironment().getFontCollection().getNumberOfFonts();
    }
}
