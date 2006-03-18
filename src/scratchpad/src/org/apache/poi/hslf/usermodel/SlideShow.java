
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
import java.io.*;

import org.apache.poi.util.LittleEndian;

import org.apache.poi.hslf.*;
import org.apache.poi.hslf.model.*;
import org.apache.poi.hslf.record.FontCollection;
import org.apache.poi.hslf.record.ParentAwareRecord;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RecordContainer;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.hslf.record.SlideAtom;
import org.apache.poi.hslf.record.SlideListWithText;
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
  
  // Records that are interesting
  private Record _documentRecord;

  // Friendly objects for people to deal with
  private Slide[] _slides;
  private Notes[] _notes;
  private FontCollection _fonts;
  // MetaSheets (eg masters) not yet supported
  // private MetaSheets[] _msheets;


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
	byte[] _docstream = _hslfSlideShow.getUnderlyingBytes();
	
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

	// Also, work out where we're going to put them in the array
	Hashtable slideIDtoRecordLookup = new Hashtable();
	int[] allIDs = new int[_mostRecentCoreRecords.length];
	Enumeration ids = mostRecentByBytes.keys();
	for(int i=0; i<allIDs.length; i++) {
		Integer id = (Integer)ids.nextElement();
		allIDs[i] = id.intValue();
	}
	Arrays.sort(allIDs);
	for(int i=0; i<allIDs.length; i++) {
		slideIDtoRecordLookup.put(new Integer(allIDs[i]), new Integer(i));
	}

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
						(Integer)slideIDtoRecordLookup.get(thisID);
					int storeAt = storeAtI.intValue();
					
					// Finally, save the record
					_mostRecentCoreRecords[storeAt] = _records[i];
				}
			}
		}
	}
	
	// Now look for the interesting records in there
	for(int i=0; i<_mostRecentCoreRecords.length; i++) {
		if(_mostRecentCoreRecords[i].getRecordType() == RecordTypes.Document.typeID) {
			_documentRecord = _mostRecentCoreRecords[i];
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
	// For holding SlideListWithText Records
	Vector slwtV = new Vector(10);

	// Look for Notes, Slides and Documents
	for(int i=0; i<_mostRecentCoreRecords.length; i++) {
		if(_mostRecentCoreRecords[i] instanceof org.apache.poi.hslf.record.Notes) {
			notesV.add(_mostRecentCoreRecords[i]);
		}
		if(_mostRecentCoreRecords[i] instanceof org.apache.poi.hslf.record.Slide) {
			slidesV.add(_mostRecentCoreRecords[i]);
		}
	}

	// Ensure we really found a Document record earlier
	// If we didn't, then the file is probably corrupt
	if(_documentRecord == null) {
		throw new CorruptPowerPointFileException("The PowerPoint file didn't contain a Document Record in its PersistPtr blocks. It is probably corrupt.");
	}


	// Now look for SlideListWithTexts in the most up-to-date Document Record
	//
	// Need to get the SlideAtomsSets for all of these. Then, query the
	//  SlidePersistAtom, and group stuff together between SLWT blocks
	//  based on the refID/slideID
	//
	// If a notes sheet exists, can normally match the Notes sheet ID
	//  to the slide ID in the SlidePersistAtom. Since there isn't always,
	//  and we can't find the ID in the slide, just order on the slide ID,
	//  and hand off to the Slides in turn. 
	// (Based on output from dev.SLWTTextListing and dev.SlideAndNotesAtomListing)
	//
	// We're trusting that the ordering of slides from the persistence
	//  layer will match the ordering found here. However, we should
	//  really find a PPT file with random sheets inserted to check with
	//
	// There shouldn't be any text duplication - only using the most
	//  record Document record's SLWTs should see to that

	Record[] docChildren = _documentRecord.getChildRecords();
	for(int i=0; i<docChildren.length; i++) {
		// Look for SlideListWithText
		if(docChildren[i] instanceof SlideListWithText) {
			slwtV.add(docChildren[i]);
		}
		// Look for FontCollection under Environment
		if(docChildren[i].getRecordType() == RecordTypes.Environment.typeID) {
			Record[] envChildren = docChildren[i].getChildRecords();
			for(int j=0; j<envChildren.length; j++) {
				if(envChildren[j] instanceof FontCollection) {
					_fonts = (FontCollection)envChildren[j];
				}
			}
		}
	}

	// For now, grab out all the sets of Atoms in the SlideListWithText's
	// Only store those which aren't empty
	// Also, get the list of IDs while we're at it
	HashSet uniqueSlideIDs = new HashSet();
	Vector setsV = new Vector();
	for(int i=0; i<slwtV.size(); i++) {
		SlideListWithText slwt = (SlideListWithText)slwtV.get(i);
		SlideAtomsSet[] thisSets = slwt.getSlideAtomsSets();
		for(int j=0; j<thisSets.length; j++) {
			SlideAtomsSet thisSet = thisSets[j];
			setsV.add(thisSet);

			int id = thisSet.getSlidePersistAtom().getSlideIdentifier();
			Integer idI = new Integer(id);
			if(! uniqueSlideIDs.contains(idI) ) {
				uniqueSlideIDs.add(idI);
			} else {
				System.err.println("** WARNING - Found two SlideAtomsSets for a given slide (" + id + ") - only using the first one **");
			}
		}
	}


	// Now, order the SlideAtomSets by their slide's ID
	int[] slideIDs = new int[uniqueSlideIDs.size()];
	int pos = 0;
	for(Iterator getIDs = uniqueSlideIDs.iterator(); getIDs.hasNext(); pos++) {
		Integer id = (Integer)getIDs.next();
		slideIDs[pos] = id.intValue();
	}
	// Sort
	Arrays.sort(slideIDs);
	// Group
	SlideAtomsSet[] slideAtomSets = new SlideAtomsSet[slideIDs.length];
	for(int i=0; i<setsV.size(); i++) {
		SlideAtomsSet thisSet = (SlideAtomsSet)setsV.get(i);
		int id = thisSet.getSlidePersistAtom().getSlideIdentifier();
		int arrayPos = -1;
		for(int j=0; j<slideIDs.length; j++) {
			if(slideIDs[j] == id) { arrayPos = j; }
		}
		slideAtomSets[arrayPos] = thisSet;
	}



	// ******************* Do the real model layer creation ****************


	// Create our Notes
	// (Need to create first, as passed to the Slides)
	_notes = new Notes[notesV.size()];
	for(int i=0; i<_notes.length; i++) {
		_notes[i] = new Notes((org.apache.poi.hslf.record.Notes)notesV.get(i));
		
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
		// Grab the slide Record
		org.apache.poi.hslf.record.Slide slideRecord = (org.apache.poi.hslf.record.Slide)slidesV.get(i);

		// Decide if we've got a SlideAtomSet to use
		SlideAtomsSet atomSet = null;
		if(i < slideAtomSets.length) {
			atomSet = slideAtomSets[i];
		}

		// Do they have a Notes?
		Notes thisNotes = null;
		// Find their SlideAtom, and use this to check for a Notes
		SlideAtom sa = slideRecord.getSlideAtom();
		int notesID = sa.getNotesID();
		if(notesID != 0) {
			for(int k=0; k<_notes.length; k++) {
				if(_notes[k].getSheetNumber() == notesID) {
					thisNotes = _notes[k];
				}
			}
		}

		// Create the Slide model layer
		_slides[i] = new Slide(slideRecord,thisNotes,atomSet);
		
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


  // Accesser methods follow

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
	public Picture[] getPictures() throws IOException {
		return _hslfSlideShow.getPictures();
	}
	
	/**
	 * Helper method for usermodel: Get the font collection
	 */
	protected FontCollection getFontCollection() { return _fonts; }
	/**
	 * Helper method for usermodel: Get the document record
	 */
	protected Record getDocumentRecord() { return _documentRecord; }
}
