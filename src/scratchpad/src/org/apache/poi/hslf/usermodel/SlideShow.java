
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
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.SlideAtom;
import org.apache.poi.hslf.record.SlideListWithText;
import org.apache.poi.hslf.record.SlideListWithText.*;
import org.apache.poi.hslf.record.PersistPtrHolder;
import org.apache.poi.hslf.record.PositionDependentRecord;

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

  // Friendly objects for people to deal with
  private Slide[] _slides;
  private Notes[] _notes;
  // private MetaSheets[] _msheets;

  /**
   *  right now this function takes one parameter: a ppt file, and outputs
   *  the text it can find for it
   */
  public static void main(String args[]) throws IOException
  {
	HSLFSlideShow basefoo = new HSLFSlideShow(args[0]);
	SlideShow foo = new SlideShow(basefoo);

	Slide[] slides = foo.getSlides();
	for(int i=0; i<slides.length; i++) {
		Slide slide = slides[i];
		System.out.println("*Slide " + slide.getSheetNumber() + ":");
		TextRun[] runs = slide.getTextRuns();
		for(int j=0; j<runs.length; j++) {
			TextRun run = runs[j];
			System.out.println("  * Text run " + run.getRunType());
			System.out.println("\n" + run.getText() + "\n");
		}
	}
  }

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

	// Find the versions of the core records we'll want to use
	findMostRecentCoreRecords();

	// Build up the model level Slides and Notes
	buildSlidesAndNotes();
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
	// For holding Document Records
	Vector documentsV = new Vector(10);
	// For holding SlideListWithText Records
	Vector slwtV = new Vector(10);

	// Look for Notes, Slides and Documents
	for(int i=0; i<_records.length; i++) {
		if(_records[i] instanceof org.apache.poi.hslf.record.Notes) {
			notesV.add(_records[i]);
		}
		if(_records[i] instanceof org.apache.poi.hslf.record.Slide) {
			slidesV.add(_records[i]);
		}
		if(_records[i].getRecordType() == 1000l) {
			documentsV.add(_records[i]);
		}
	}


	// Also look for SlideListWithTexts in Documents
	//
	// Need to get the SlideAtomsSets for all of these. Then, query the
	//  SlidePersistAtom, and group stuff together between SLWT blocks
	//  based on the refID/slideID. Finally, build up a list of all the
	//  SlideAtomsSets for a given refID / slideID, and pass them on to
	//  the Slide when creating
	//
	// If a notes sheet exists, can normally match the Notes sheet ID
	//  to the slide ID in the SlidePersistAtom. Since there isn't always,
	//  and we can't find the ID in the slide, just order on the slide ID,
	//  and hand off to the Slides in turn.
	// (Based on output from dev.SLWTTextListing and dev.SlideAndNotesAtomListing)
	//
	// There is often duplicate text, especially for the first few
	//  Slides. Currently, it's up to the Slide model code to detect
	//  and ignore those

	for(int i=0; i<documentsV.size(); i++) {
		Record docRecord = (Record)documentsV.get(i);
		Record[] docChildren = docRecord.getChildRecords();
		for(int j=0; j<docChildren.length; j++) {
			if(docChildren[j] instanceof SlideListWithText) {
				//System.out.println("Found SLWT in document " + i);
				//System.out.println("  Has " + docChildren[j].getChildRecords().length + " children");
				slwtV.add(docChildren[j]);
			}
		}
	}

	// For now, grab out all the sets of Atoms in the SlideListWithText's
	// Only store those which aren't empty
	Vector setsV = new Vector();
	for(int i=0; i<slwtV.size(); i++) {
		SlideListWithText slwt = (SlideListWithText)slwtV.get(i);
		SlideAtomsSet[] thisSets = slwt.getSlideAtomsSets();
		for(int j=0; j<thisSets.length; j++) {
			setsV.add(thisSets[j]);
		}
	}


	// Now, sort the SlideAtomSets together into groups for the same slide ID,
	//  and order them by the slide ID

	// Find the unique IDs
	HashSet uniqueSlideIDs = new HashSet();
	for(int i=0; i<setsV.size(); i++) {
		SlideAtomsSet thisSet = (SlideAtomsSet)setsV.get(i);
		int id = thisSet.getSlidePersistAtom().getSlideIdentifier();
		Integer idI = new Integer(id);
		if(! uniqueSlideIDs.contains(idI) ) {
			uniqueSlideIDs.add(idI);
		}
	}
	int[] slideIDs = new int[uniqueSlideIDs.size()];
	int pos = 0;
	for(Iterator getIDs = uniqueSlideIDs.iterator(); getIDs.hasNext(); pos++) {
		Integer id = (Integer)getIDs.next();
		slideIDs[pos] = id.intValue();
	}
	// Sort
	Arrays.sort(slideIDs);
	// Group
	Vector[] sortedSetsV = new Vector[slideIDs.length];
	for(int i=0; i<setsV.size(); i++) {
		SlideAtomsSet thisSet = (SlideAtomsSet)setsV.get(i);
		int id = thisSet.getSlidePersistAtom().getSlideIdentifier();
		int arrayPos = -1;
		for(int j=0; j<slideIDs.length; j++) {
			if(slideIDs[j] == id) { arrayPos = j; }
		}
		if(sortedSetsV[arrayPos] == null) { sortedSetsV[arrayPos] = new Vector(); }
		sortedSetsV[arrayPos].add(thisSet);
	}


	// ******************* Do the real model layer creation ****************


	// Create our Notes
	// (Need to create first, as passed to the Slides)
	_notes = new Notes[notesV.size()];
	for(int i=0; i<_notes.length; i++) {
		_notes[i] = new Notes((org.apache.poi.hslf.record.Notes)notesV.get(i));
	}


	// Create our Slides
	_slides = new Slide[slidesV.size()];
	for(int i=0; i<_slides.length; i++) {
		// Grab the slide Record
		org.apache.poi.hslf.record.Slide slideRecord = (org.apache.poi.hslf.record.Slide)slidesV.get(i);

		// Do they have a Notes?
		Notes thisNotes = null;
		// Find their SlideAtom, and use this to check for a Notes
		Record[] slideRecordChildren = slideRecord.getChildRecords();		
		for(int j=0; j<slideRecordChildren.length; j++) {
			if(slideRecordChildren[j] instanceof SlideAtom) {
				SlideAtom sa = (SlideAtom)slideRecordChildren[j];
				int notesID = sa.getNotesID();
				if(notesID != 0) {
					for(int k=0; k<_notes.length; k++) {
						if(_notes[k].getSheetNumber() == notesID) {
							thisNotes = _notes[k];
						}
					}
				}
			}
		}

		// Grab the (hopefully) corresponding block of Atoms
		SlideAtomsSet[] sets;
		if(sortedSetsV.length > i) {
			Vector thisSetsV = sortedSetsV[i];
			sets = new SlideAtomsSet[thisSetsV.size()];
			for(int j=0; j<sets.length; j++) {
				sets[j] = (SlideAtomsSet)thisSetsV.get(j);
			}
			//System.out.println("For slide " + i + ", found " + sets.length + " Sets of text");
		} else {
			// Didn't find enough SlideAtomSets to give any to this sheet
			sets = new SlideAtomsSet[0];
		}

			// Create the Slide model layer
		_slides[i] = new Slide(slideRecord,thisNotes,sets);
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
}
