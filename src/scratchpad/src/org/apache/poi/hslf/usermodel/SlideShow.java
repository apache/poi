
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

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
import org.apache.poi.hslf.record.Document;
import org.apache.poi.hslf.record.DocumentAtom;
import org.apache.poi.hslf.record.FontCollection;
import org.apache.poi.hslf.record.ParentAwareRecord;
import org.apache.poi.hslf.record.PositionDependentRecordContainer;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RecordContainer;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.hslf.record.SlideAtom;
import org.apache.poi.hslf.record.SlideListWithText;
import org.apache.poi.hslf.record.SlidePersistAtom;
import org.apache.poi.hslf.record.UserEditAtom;
import org.apache.poi.hslf.record.SlideListWithText.*;
import org.apache.poi.hslf.record.PersistPtrHolder;
import org.apache.poi.hslf.record.PositionDependentRecord;
import org.apache.poi.hslf.exceptions.CorruptPowerPointFileException;

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
  private Slide[] _slides;
  private Notes[] _notes;
  private FontCollection _fonts;
  // MetaSheets (eg masters) not yet supported
  // private MetaSheets[] _msheets;

  
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
			System.err.println("No core record found with ID " + (i+1) + " based on PersistPtr lookup");
		}
	}
  }

  /**
   * Build up model level Slide and Notes objects, from the underlying
   *  records.
   */
  private void buildSlidesAndNotes() {
    // For holding the Slide Records
    Vector slidesV = new Vector(10);
    // For holding the Notes Records
    Vector notesV = new Vector(10);
    // For holding the Meta Sheet Records
    Vector metaSheetsV = new Vector(10);
	  
	// Ensure we really found a Document record earlier
	// If we didn't, then the file is probably corrupt
	if(_documentRecord == null) {
		throw new CorruptPowerPointFileException("The PowerPoint file didn't contain a Document Record in its PersistPtr blocks. It is probably corrupt.");
	}


	// Fetch the SlideListWithTexts in the most up-to-date Document Record
	//
	// Then, use this to find the Slide records, and also the Notes record
	//  for each Slide (if it has one)
	//
	// The following matching algorithm is based on looking at the output
	//  of org.apache.poi.hslf.dev.SlideIdListing on a number of files:
	//
	// 1) Get the SlideAtomSets from the SlideListWithTexts of the most
	//     up-to-date Document
	// 2) Get the SlidePersistAtoms from all of these
	// 3) Get the RefId, which corresponds to a "sheet ID" from the
	//    PersistPtr Stuff
	// 4) Grab the record at that ID, and see if it's a slide or a notes
	// 5) Build a mapping between the SlideIdentifier ID and the RefId
	//     for both slides and notes
	// 6) Loop over all the slides
	// 7) Look each slide's SlideAtom to see if it has associated Notes - 
	//     if it does, the ID will be SlideIdentifier for those notes
	//     (Note: might not be the same as the SlideIdentifier of the Slide)
	// 8) Generate the model representations, giving them the matching
	//     slide atom sets, IDs etc

	SlideListWithText[] slwts = _documentRecord.getSlideListWithTexts();
	
	// To hold the lookup from SlideIdentifier IDs to RefIDs
	Hashtable slideSlideIdToRefid = new Hashtable();
	Hashtable notesSlideIdToRefid = new Hashtable();
	// To hold the lookup from SlideIdentifier IDs to SlideAtomsSets
	Hashtable slideSlideIdToSlideAtomsSet = new Hashtable();
	Hashtable notesSlideIdToSlideAtomsSet = new Hashtable();
	
	// Loop over all the SlideListWithTexts, getting their 
	//  SlideAtomSets
	for(int i=0; i<slwts.length; i++) {
		SlideAtomsSet[] sas = slwts[i].getSlideAtomsSets();
		for(int j=0; j<sas.length; j++) {
			// What does this SlidePersistAtom point to?
			SlidePersistAtom spa = sas[j].getSlidePersistAtom();
			Integer slideIdentifier = new Integer( spa.getSlideIdentifier() );
			Integer slideRefId = new Integer( spa.getRefID() ); 
			
			// Grab the record it points to
			Integer coreRecordId = (Integer)
				_sheetIdToCoreRecordsLookup.get(slideRefId);
			Record r = _mostRecentCoreRecords[coreRecordId.intValue()];
			
			// Add the IDs to the appropriate lookups
			if(r instanceof org.apache.poi.hslf.record.Slide) {
				slideSlideIdToRefid.put( slideIdentifier, slideRefId );
				// Save the SlideAtomsSet
				slideSlideIdToSlideAtomsSet.put( slideIdentifier, sas[j] );
			} else if(r instanceof org.apache.poi.hslf.record.Notes) {
				notesSlideIdToRefid.put( slideIdentifier, slideRefId );
				// Save the SlideAtomsSet
				notesSlideIdToSlideAtomsSet.put( slideIdentifier, sas[j] );
			} else if(r.getRecordType() == RecordTypes.MainMaster.typeID) {
				// Skip for now, we don't do Master slides yet
			} else {
				throw new IllegalStateException("SlidePersistAtom had a RefId that pointed to something other than a Slide or a Notes, was a " + r + " with type " + r.getRecordType());
			}
		}
	}
	
	// Now, create a model representation of a slide for each
	//  slide + slideatomset we found
	// Do it in order of the SlideIdentifiers
	int[] slideIDs = new int[slideSlideIdToRefid.size()];
	int pos = 0;
	Enumeration e = slideSlideIdToRefid.keys();
	while(e.hasMoreElements()) {
		Integer id = (Integer)e.nextElement();
		slideIDs[pos] = id.intValue();
		pos++;
	}
	// Sort
	Arrays.sort(slideIDs);
	
	// Create
	for(int i=0; i<slideIDs.length; i++) {
		// Build up the list of all the IDs we might want to use
		int slideIdentifier = slideIDs[i];
		Integer slideIdentifierI = new Integer(slideIdentifier);
		int slideNumber = (i+1);
		Integer slideRefI = (Integer)slideSlideIdToRefid.get(slideIdentifierI); 
		Integer slideCoreRecNumI = (Integer)_sheetIdToCoreRecordsLookup.get(slideRefI);
		int slideCoreRecNum = slideCoreRecNumI.intValue();
		
		// Fetch the Slide record
		org.apache.poi.hslf.record.Slide s = (org.apache.poi.hslf.record.Slide)
			_mostRecentCoreRecords[slideCoreRecNum];
		
		// Do we have a notes for this slide?
		org.apache.poi.hslf.record.Notes n = null;
		if(s.getSlideAtom().getNotesID() > 0) {
			// Get the SlideIdentifier of the Notes
			// (Note - might not be the same as the SlideIdentifier of the Slide)
			int notesSlideIdentifier = s.getSlideAtom().getNotesID();
			Integer notesSlideIdentifierI = new Integer(notesSlideIdentifier);
			
			// Grab the notes record
			Integer notesRefI = (Integer)notesSlideIdToRefid.get(notesSlideIdentifierI);
			Integer notesCoreRecNum = (Integer)_sheetIdToCoreRecordsLookup.get(notesRefI);
			n = (org.apache.poi.hslf.record.Notes)
				_mostRecentCoreRecords[notesCoreRecNum.intValue()];
		}
		
		// Grab the matching SlideAtomSet 
		SlideAtomsSet sas = (SlideAtomsSet)
			slideSlideIdToSlideAtomsSet.get(slideIdentifierI);
		
		// Build the notes model, if there's notes
		Notes notes = null;
		if(n != null) {
			// TODO: Use this
			SlideAtomsSet nsas = (SlideAtomsSet)
				notesSlideIdToSlideAtomsSet.get(slideIdentifierI);
			
			// Create the model view of the notes
			notes = new Notes(n);
			notesV.add(notes);
		}
		
		// Build the slide model
		Slide slide = new Slide(s, notes, sas, slideIdentifier, slideNumber);
		slidesV.add(slide);
	}
	
	// ******************* Finish up ****************

	// Finish setting up the notes
	_notes = new Notes[notesV.size()];
	for(int i=0; i<_notes.length; i++) {
		_notes[i] = (Notes)notesV.get(i);
		
		// Now supply ourselves to all the rich text runs
		//  of this note's TextRuns
		TextRun[] trs = _notes[i].getTextRuns(); 
		for(int j=0; j<trs.length; j++) {
			RichTextRun[] rtrs = trs[j].getRichTextRuns();
			for(int k=0; k<rtrs.length; k++) {
				rtrs[k].supplySlideShow(this);
			}
		}
	}


	// Create our Slides
	_slides = new Slide[slidesV.size()];
	for(int i=0; i<_slides.length; i++) {
		_slides[i] = (Slide)slidesV.get(i);

		// Now supply ourselves to all the rich text runs
		//  of this slide's TextRuns
		TextRun[] trs = _slides[i].getTextRuns(); 
		for(int j=0; j<trs.length; j++) {
			RichTextRun[] rtrs = trs[j].getRichTextRuns();
			for(int k=0; k<rtrs.length; k++) {
				rtrs[k].supplySlideShow(this);
			}
		}
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
	 * Returns an array of all the meta Sheets (master sheets etc) 
	 * found in the slideshow
	 */
	//public MetaSheet[] getMetaSheets() { return _msheets; }

	/**
	 * Returns all the pictures attached to the SlideShow
	 */
	public PictureData[] getPictures() throws IOException {
		return _hslfSlideShow.getPictures();
	}
	
	/**
	 * Return the current page size
	 */
	public Dimension getPageSize(){
		DocumentAtom docatom = _documentRecord.getDocumentAtom();
		return new Dimension((int)docatom.getSlideSizeX(), (int)docatom.getSlideSizeY());
	}
	
	/**
	 * Helper method for usermodel: Get the font collection
	 */
	protected FontCollection getFontCollection() { return _fonts; }
	/**
	 * Helper method for usermodel: Get the document record
	 */
	protected Document getDocumentRecord() { return _documentRecord; }

	
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
  		SlideListWithText[] slwts = _documentRecord.getSlideListWithTexts();
  		SlideListWithText slist = null;
  		
  		if(slwts.length > 1) {
  			// Just use the last one
  			slist = slwts[slwts.length - 1];
  		} else {
  			// Need to add a new one
  			slist = new SlideListWithText();
  			_documentRecord.addSlideListWithText(slist);
  			slwts = _documentRecord.getSlideListWithTexts();
  		}

  		// Grab the SlidePersistAtom with the highest Slide Number.
  		// (Will stay as null if no SlidePersistAtom exists yet in
  		//  the slide, or only master slide's ones do)
  		SlidePersistAtom prev = null;
  		for(int i=0; i<slwts.length; i++) {
  			SlideAtomsSet[] sas = slwts[i].getSlideAtomsSets();
  			for(int j=0; j<sas.length; j++) {
  				SlidePersistAtom spa = sas[j].getSlidePersistAtom();
  				if(spa.getSlideIdentifier() < 0) {
  					// This is for a master slide
  				} else {
  					// Must be for a real slide
  	  				if(prev == null) { prev = spa; }
  	  				if(prev.getSlideIdentifier() < spa.getSlideIdentifier()) {
  	  					prev = spa;
  	  				}
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
  		System.out.println("Added slide " + _slides.length + " with ref " + sp.getRefID() + " and identifier " + sp.getSlideIdentifier());
  		
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
		System.out.println("New slide ended up at " + slideOffset);

		// Last view is now of the slide
  		usr.setLastViewType((short)UserEditAtom.LAST_VIEW_SLIDE_VIEW);
  		
  		// All done and added
  		return slide;
	}


    /**
     * Adds a picture to this presentation and returns the associated index.
     *
     * @param data      picture data
     * @param format    the format of the picture.  One of constans defined in the <code>Picture</code> class.
     * @return          the index to this picture (1 based).
     */
    public int addPicture(byte[] data, int format) {
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

        EscherBSERecord bse = new EscherBSERecord();
        bse.setRecordId(EscherBSERecord.RECORD_ID);
        bse.setOptions( (short) ( 0x0002 | ( format << 4 ) ) );
        bse.setSize(data.length + PictureData.HEADER_SIZE);
        bse.setUid(uid);
        bse.setBlipTypeMacOS((byte)format);
        bse.setBlipTypeWin32((byte)format);

        bse.setRef(1);
        bse.setOffset(offset);

        bstore.addChildRecord(bse);
        int count = bstore.getChildRecords().size();
        bstore.setOptions((short)( (count << 4) | 0xF ));

        PictureData pict = new PictureData();
        pict.setUID(uid);
        pict.setData(data);
        pict.setType(format);

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
    public int addPicture(File pict, int format) {
        int length = (int)pict.length();
        byte[] data = new byte[length];
        try {
            FileInputStream is = new FileInputStream(pict);
            is.read(data);
            is.close();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
        return addPicture(data, format);
    }
}
