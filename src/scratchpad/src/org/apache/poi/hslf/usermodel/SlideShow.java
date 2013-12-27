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

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.hpsf.ClassID;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.exceptions.CorruptPowerPointFileException;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.hslf.model.HeadersFooters;
import org.apache.poi.hslf.model.Hyperlink;
import org.apache.poi.hslf.model.MovieShape;
import org.apache.poi.hslf.model.Notes;
import org.apache.poi.hslf.model.PPFont;
import org.apache.poi.hslf.model.Picture;
import org.apache.poi.hslf.model.Shape;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.SlideMaster;
import org.apache.poi.hslf.model.TitleMaster;
import org.apache.poi.hslf.record.Document;
import org.apache.poi.hslf.record.DocumentAtom;
import org.apache.poi.hslf.record.ExAviMovie;
import org.apache.poi.hslf.record.ExControl;
import org.apache.poi.hslf.record.ExEmbed;
import org.apache.poi.hslf.record.ExEmbedAtom;
import org.apache.poi.hslf.record.ExHyperlink;
import org.apache.poi.hslf.record.ExHyperlinkAtom;
import org.apache.poi.hslf.record.ExMCIMovie;
import org.apache.poi.hslf.record.ExObjList;
import org.apache.poi.hslf.record.ExObjListAtom;
import org.apache.poi.hslf.record.ExOleObjAtom;
import org.apache.poi.hslf.record.ExOleObjStg;
import org.apache.poi.hslf.record.ExVideoContainer;
import org.apache.poi.hslf.record.FontCollection;
import org.apache.poi.hslf.record.FontEntityAtom;
import org.apache.poi.hslf.record.HeadersFootersContainer;
import org.apache.poi.hslf.record.PersistPtrHolder;
import org.apache.poi.hslf.record.PositionDependentRecord;
import org.apache.poi.hslf.record.PositionDependentRecordContainer;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RecordContainer;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.hslf.record.SlideListWithText;
import org.apache.poi.hslf.record.SlideListWithText.SlideAtomsSet;
import org.apache.poi.hslf.record.SlidePersistAtom;
import org.apache.poi.hslf.record.UserEditAtom;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * This class is a friendly wrapper on top of the more scary HSLFSlideShow.
 *
 * TODO: - figure out how to match notes to their correct sheet (will involve
 * understanding DocSlideList and DocNotesList) - handle Slide creation cleaner
 *
 * @author Nick Burch
 * @author Yegor kozlov
 */
public final class SlideShow {
	// What we're based on
	private HSLFSlideShow _hslfSlideShow;

	// Pointers to the most recent versions of the core records
	// (Document, Notes, Slide etc)
	private Record[] _mostRecentCoreRecords;
	// Lookup between the PersitPtr "sheet" IDs, and the position
	// in the mostRecentCoreRecords array
	private Map<Integer,Integer> _sheetIdToCoreRecordsLookup;

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
	public SlideShow(HSLFSlideShow hslfSlideShow) {
	    // Get useful things from our base slideshow
	    _hslfSlideShow = hslfSlideShow;

		// Handle Parent-aware Records
		for (Record record : _hslfSlideShow.getRecords()) {
			if(record instanceof RecordContainer){
                RecordContainer.handleParentAwareRecords((RecordContainer)record);
            }
		}

		// Find the versions of the core records we'll want to use
		findMostRecentCoreRecords();

		// Build up the model level Slides and Notes
		buildSlidesAndNotes();
	}

	/**
	 * Constructs a new, empty, Powerpoint document.
	 */
	public SlideShow() {
		this(HSLFSlideShow.create());
	}

	/**
	 * Constructs a Powerpoint document from an input stream.
	 */
	public SlideShow(InputStream inputStream) throws IOException {
		this(new HSLFSlideShow(inputStream));
	}

	/**
	 * Use the PersistPtrHolder entries to figure out what is the "most recent"
	 * version of all the core records (Document, Notes, Slide etc), and save a
	 * record of them. Do this by walking from the oldest PersistPtr to the
	 * newest, overwriting any references found along the way with newer ones
	 */
	private void findMostRecentCoreRecords() {
		// To start with, find the most recent in the byte offset domain
		Map<Integer,Integer> mostRecentByBytes = new HashMap<Integer,Integer>();
		for (Record record : _hslfSlideShow.getRecords()) {
			if (record instanceof PersistPtrHolder) {
				PersistPtrHolder pph = (PersistPtrHolder) record;

				// If we've already seen any of the "slide" IDs for this
				// PersistPtr, remove their old positions
				int[] ids = pph.getKnownSlideIDs();
				for (int id : ids) {
					if (mostRecentByBytes.containsKey(id)) {
						mostRecentByBytes.remove(id);
					}
				}

				// Now, update the byte level locations with their latest values
				Map<Integer,Integer> thisSetOfLocations = pph.getSlideLocationsLookup();
				for (int id : ids) {
					mostRecentByBytes.put(id, thisSetOfLocations.get(id));
				}
			}
		}

		// We now know how many unique special records we have, so init
		// the array
		_mostRecentCoreRecords = new Record[mostRecentByBytes.size()];

		// We'll also want to be able to turn the slide IDs into a position
		// in this array
		_sheetIdToCoreRecordsLookup = new HashMap<Integer,Integer>();
		Integer[] allIDs = mostRecentByBytes.keySet().toArray(new Integer[mostRecentByBytes.size()]); 
		Arrays.sort(allIDs);
		for (int i = 0; i < allIDs.length; i++) {
			_sheetIdToCoreRecordsLookup.put(allIDs[i], i);
		}

		Map<Integer,Integer> mostRecentByBytesRev = new HashMap<Integer,Integer>(mostRecentByBytes.size());
		for (Map.Entry<Integer,Integer> me : mostRecentByBytes.entrySet()) {
		    mostRecentByBytesRev.put(me.getValue(), me.getKey());
		}
		
		// Now convert the byte offsets back into record offsets
		for (Record record : _hslfSlideShow.getRecords()) {
			if (!(record instanceof PositionDependentRecord)) continue;
			
			PositionDependentRecord pdr = (PositionDependentRecord) record;
			int recordAt = pdr.getLastOnDiskOffset();

			Integer thisID = mostRecentByBytesRev.get(recordAt);
			
			if (thisID == null) continue;
			
			// Bingo. Now, where do we store it?
			int storeAt = _sheetIdToCoreRecordsLookup.get(thisID);

			// Tell it its Sheet ID, if it cares
			if (pdr instanceof PositionDependentRecordContainer) {
				PositionDependentRecordContainer pdrc = (PositionDependentRecordContainer) record;
				pdrc.setSheetId(thisID);
			}

			// Finally, save the record
			_mostRecentCoreRecords[storeAt] = record;
		}

		// Now look for the interesting records in there
		for (Record record : _mostRecentCoreRecords) {
			// Check there really is a record at this number
			if (record != null) {
				// Find the Document, and interesting things in it
				if (record.getRecordType() == RecordTypes.Document.typeID) {
					_documentRecord = (Document) record;
					_fonts = _documentRecord.getEnvironment().getFontCollection();
				}
			} else {
				// No record at this number
				// Odd, but not normally a problem
			}
		}
	}

	/**
	 * For a given SlideAtomsSet, return the core record, based on the refID
	 * from the SlidePersistAtom
	 */
	private Record getCoreRecordForSAS(SlideAtomsSet sas) {
		SlidePersistAtom spa = sas.getSlidePersistAtom();
		int refID = spa.getRefID();
		return getCoreRecordForRefID(refID);
	}

	/**
	 * For a given refID (the internal, 0 based numbering scheme), return the
	 * core record
	 *
	 * @param refID
	 *            the refID
	 */
	private Record getCoreRecordForRefID(int refID) {
		Integer coreRecordId = _sheetIdToCoreRecordsLookup.get(refID);
		if (coreRecordId != null) {
			Record r = _mostRecentCoreRecords[coreRecordId];
			return r;
		}
		logger.log(POILogger.ERROR,
				"We tried to look up a reference to a core record, but there was no core ID for reference ID "
						+ refID);
		return null;
	}

	/**
	 * Build up model level Slide and Notes objects, from the underlying
	 * records.
	 */
	private void buildSlidesAndNotes() {
		// Ensure we really found a Document record earlier
		// If we didn't, then the file is probably corrupt
		if (_documentRecord == null) {
			throw new CorruptPowerPointFileException(
					"The PowerPoint file didn't contain a Document Record in its PersistPtr blocks. It is probably corrupt.");
		}

		// Fetch the SlideListWithTexts in the most up-to-date Document Record
		//
		// As far as we understand it:
		// * The first SlideListWithText will contain a SlideAtomsSet
		// for each of the master slides
		// * The second SlideListWithText will contain a SlideAtomsSet
		// for each of the slides, in their current order
		// These SlideAtomsSets will normally contain text
		// * The third SlideListWithText (if present), will contain a
		// SlideAtomsSet for each Notes
		// These SlideAtomsSets will not normally contain text
		//
		// Having indentified the masters, slides and notes + their orders,
		// we have to go and find their matching records
		// We always use the latest versions of these records, and use the
		// SlideAtom/NotesAtom to match them with the StyleAtomSet

		SlideListWithText masterSLWT = _documentRecord.getMasterSlideListWithText();
		SlideListWithText slidesSLWT = _documentRecord.getSlideSlideListWithText();
		SlideListWithText notesSLWT = _documentRecord.getNotesSlideListWithText();

		// Find master slides
		// These can be MainMaster records, but oddly they can also be
		// Slides or Notes, and possibly even other odd stuff....
		// About the only thing you can say is that the master details are in
		// the first SLWT.
		SlideAtomsSet[] masterSets = new SlideAtomsSet[0];
		if (masterSLWT != null) {
			masterSets = masterSLWT.getSlideAtomsSets();

			ArrayList<SlideMaster> mmr = new ArrayList<SlideMaster>();
			ArrayList<TitleMaster> tmr = new ArrayList<TitleMaster>();

			for (SlideAtomsSet sas : masterSets) {
				Record r = getCoreRecordForSAS(sas);
				int sheetNo = sas.getSlidePersistAtom().getSlideIdentifier();
				if (r instanceof org.apache.poi.hslf.record.Slide) {
					TitleMaster master = new TitleMaster((org.apache.poi.hslf.record.Slide) r,
							sheetNo);
					master.setSlideShow(this);
					tmr.add(master);
				} else if (r instanceof org.apache.poi.hslf.record.MainMaster) {
					SlideMaster master = new SlideMaster((org.apache.poi.hslf.record.MainMaster) r,
							sheetNo);
					master.setSlideShow(this);
					mmr.add(master);
				}
			}

			_masters = mmr.toArray(new SlideMaster[mmr.size()]);
			_titleMasters = tmr.toArray(new TitleMaster[tmr.size()]);
		}

		// Having sorted out the masters, that leaves the notes and slides

		// Start by finding the notes records to go with the entries in
		// notesSLWT
		org.apache.poi.hslf.record.Notes[] notesRecords;
		SlideAtomsSet[] notesSets = new SlideAtomsSet[0];
		Map<Integer,Integer> slideIdToNotes = new HashMap<Integer,Integer>();
		if (notesSLWT == null) {
			// None
			notesRecords = new org.apache.poi.hslf.record.Notes[0];
		} else {
			// Match up the records and the SlideAtomSets
			notesSets = notesSLWT.getSlideAtomsSets();
			List<org.apache.poi.hslf.record.Notes> notesRecordsL = 
			   new ArrayList<org.apache.poi.hslf.record.Notes>();
			for (int i = 0; i < notesSets.length; i++) {
				// Get the right core record
				Record r = getCoreRecordForSAS(notesSets[i]);

				// Ensure it really is a notes record
				if (r == null || r instanceof org.apache.poi.hslf.record.Notes) {
				    if (r == null) {
	                    logger.log(POILogger.WARN, "A Notes SlideAtomSet at " + i
	                            + " said its record was at refID "
	                            + notesSets[i].getSlidePersistAtom().getRefID()
	                            + ", but that record didn't exist - record ignored.");
				    }
				    // we need to add also null-records, otherwise the index references to other existing
				    // don't work anymore
					org.apache.poi.hslf.record.Notes notesRecord = (org.apache.poi.hslf.record.Notes) r;
					notesRecordsL.add(notesRecord);

					// Record the match between slide id and these notes
					SlidePersistAtom spa = notesSets[i].getSlidePersistAtom();
					int slideId = spa.getSlideIdentifier();
					slideIdToNotes.put(slideId, i);
				} else {
					logger.log(POILogger.ERROR, "A Notes SlideAtomSet at " + i
							+ " said its record was at refID "
							+ notesSets[i].getSlidePersistAtom().getRefID()
							+ ", but that was actually a " + r);
				}
			}
			notesRecords = new org.apache.poi.hslf.record.Notes[notesRecordsL.size()];
			notesRecords = notesRecordsL.toArray(notesRecords);
		}

		// Now, do the same thing for our slides
		org.apache.poi.hslf.record.Slide[] slidesRecords;
		SlideAtomsSet[] slidesSets = new SlideAtomsSet[0];
		if (slidesSLWT == null) {
			// None
			slidesRecords = new org.apache.poi.hslf.record.Slide[0];
		} else {
			// Match up the records and the SlideAtomSets
			slidesSets = slidesSLWT.getSlideAtomsSets();
			slidesRecords = new org.apache.poi.hslf.record.Slide[slidesSets.length];
			for (int i = 0; i < slidesSets.length; i++) {
				// Get the right core record
				Record r = getCoreRecordForSAS(slidesSets[i]);

				// Ensure it really is a slide record
				if (r instanceof org.apache.poi.hslf.record.Slide) {
					slidesRecords[i] = (org.apache.poi.hslf.record.Slide) r;
				} else {
					logger.log(POILogger.ERROR, "A Slide SlideAtomSet at " + i
							+ " said its record was at refID "
							+ slidesSets[i].getSlidePersistAtom().getRefID()
							+ ", but that was actually a " + r);
				}
			}
		}

		// Finally, generate model objects for everything
		// Notes first
		_notes = new Notes[notesRecords.length];
		for (int i = 0; i < _notes.length; i++) {
		    if (notesRecords[i] != null) {
    		    _notes[i] = new Notes(notesRecords[i]);
    			_notes[i].setSlideShow(this);
		    }
		}
		// Then slides
		_slides = new Slide[slidesRecords.length];
		for (int i = 0; i < _slides.length; i++) {
			SlideAtomsSet sas = slidesSets[i];
			int slideIdentifier = sas.getSlidePersistAtom().getSlideIdentifier();

			// Do we have a notes for this?
			Notes notes = null;
			// Slide.SlideAtom.notesId references the corresponding notes slide.
			// 0 if slide has no notes.
			int noteId = slidesRecords[i].getSlideAtom().getNotesID();
			if (noteId != 0) {
				Integer notesPos = slideIdToNotes.get(noteId);
				if (notesPos != null) {
					notes = _notes[notesPos];
				} else {
					logger.log(POILogger.ERROR, "Notes not found for noteId=" + noteId);
				}
			}

			// Now, build our slide
			_slides[i] = new Slide(slidesRecords[i], notes, sas, slideIdentifier, (i + 1));
			_slides[i].setSlideShow(this);
		}
	}

	/**
	 * Writes out the slideshow file the is represented by an instance of this
	 * class
	 *
	 * @param out
	 *            The OutputStream to write to.
	 * @throws IOException
	 *             If there is an unexpected IOException from the passed in
	 *             OutputStream
	 */
	public void write(OutputStream out) throws IOException {
		_hslfSlideShow.write(out);
	}

	/*
	 * ===============================================================
	 *                         Accessor Code
	 * ===============================================================
	 */

	/**
	 * Returns an array of the most recent version of all the interesting
	 * records
	 */
	public Record[] getMostRecentCoreRecords() {
		return _mostRecentCoreRecords;
	}

	/**
	 * Returns an array of all the normal Slides found in the slideshow
	 */
	public Slide[] getSlides() {
		return _slides;
	}

	/**
	 * Returns an array of all the normal Notes found in the slideshow
	 */
	public Notes[] getNotes() {
		return _notes;
	}

	/**
	 * Returns an array of all the normal Slide Masters found in the slideshow
	 */
	public SlideMaster[] getSlidesMasters() {
		return _masters;
	}

	/**
	 * Returns an array of all the normal Title Masters found in the slideshow
	 */
	public TitleMaster[] getTitleMasters() {
		return _titleMasters;
	}

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
	public Dimension getPageSize() {
		DocumentAtom docatom = _documentRecord.getDocumentAtom();
		int pgx = (int) docatom.getSlideSizeX() * Shape.POINT_DPI / Shape.MASTER_DPI;
		int pgy = (int) docatom.getSlideSizeY() * Shape.POINT_DPI / Shape.MASTER_DPI;
		return new Dimension(pgx, pgy);
	}

	/**
	 * Change the current page size
	 *
	 * @param pgsize
	 *            page size (in points)
	 */
	public void setPageSize(Dimension pgsize) {
		DocumentAtom docatom = _documentRecord.getDocumentAtom();
		docatom.setSlideSizeX(pgsize.width * Shape.MASTER_DPI / Shape.POINT_DPI);
		docatom.setSlideSizeY(pgsize.height * Shape.MASTER_DPI / Shape.POINT_DPI);
	}

	/**
	 * Helper method for usermodel: Get the font collection
	 */
	protected FontCollection getFontCollection() {
		return _fonts;
	}

	/**
	 * Helper method for usermodel and model: Get the document record
	 */
	public Document getDocumentRecord() {
		return _documentRecord;
	}

	/*
	 * ===============================================================
	 * Re-ordering Code
	 * ===============================================================
	 */

	/**
	 * Re-orders a slide, to a new position.
	 *
	 * @param oldSlideNumber
	 *            The old slide number (1 based)
	 * @param newSlideNumber
	 *            The new slide number (1 based)
	 */
	public void reorderSlide(int oldSlideNumber, int newSlideNumber) {
		// Ensure these numbers are valid
		if (oldSlideNumber < 1 || newSlideNumber < 1) {
			throw new IllegalArgumentException("Old and new slide numbers must be greater than 0");
		}
		if (oldSlideNumber > _slides.length || newSlideNumber > _slides.length) {
			throw new IllegalArgumentException(
					"Old and new slide numbers must not exceed the number of slides ("
							+ _slides.length + ")");
		}

		// The order of slides is defined by the order of slide atom sets in the
		// SlideListWithText container.
		SlideListWithText slwt = _documentRecord.getSlideSlideListWithText();
		SlideAtomsSet[] sas = slwt.getSlideAtomsSets();

		SlideAtomsSet tmp = sas[oldSlideNumber - 1];
		sas[oldSlideNumber - 1] = sas[newSlideNumber - 1];
		sas[newSlideNumber - 1] = tmp;

		ArrayList<Record> lst = new ArrayList<Record>();
		for (int i = 0; i < sas.length; i++) {
			lst.add(sas[i].getSlidePersistAtom());
			Record[] r = sas[i].getSlideRecords();
			for (int j = 0; j < r.length; j++) {
				lst.add(r[j]);
			}
			_slides[i].setSlideNumber(i + 1);
		}
		Record[] r = lst.toArray(new Record[lst.size()]);
		slwt.setChildRecord(r);
	}

	/**
	 * Removes the slide at the given index (0-based).
	 * <p>
	 * Shifts any subsequent slides to the left (subtracts one from their slide
	 * numbers).
	 * </p>
	 *
	 * @param index
	 *            the index of the slide to remove (0-based)
	 * @return the slide that was removed from the slide show.
	 */
	public Slide removeSlide(int index) {
		int lastSlideIdx = _slides.length - 1;
		if (index < 0 || index > lastSlideIdx) {
			throw new IllegalArgumentException("Slide index (" + index + ") is out of range (0.."
					+ lastSlideIdx + ")");
		}

		SlideListWithText slwt = _documentRecord.getSlideSlideListWithText();
		SlideAtomsSet[] sas = slwt.getSlideAtomsSets();

		Slide removedSlide = null;
		ArrayList<Record> records = new ArrayList<Record>();
		ArrayList<SlideAtomsSet> sa = new ArrayList<SlideAtomsSet>();
		ArrayList<Slide> sl = new ArrayList<Slide>();

		ArrayList<Notes> nt = new ArrayList<Notes>();
		for (Notes notes : getNotes())
			nt.add(notes);

		for (int i = 0, num = 0; i < _slides.length; i++) {
			if (i != index) {
				sl.add(_slides[i]);
				sa.add(sas[i]);
				_slides[i].setSlideNumber(num++);
				records.add(sas[i].getSlidePersistAtom());
				records.addAll(Arrays.asList(sas[i].getSlideRecords()));
			} else {
				removedSlide = _slides[i];
				nt.remove(_slides[i].getNotesSheet());
			}
		}
		if (sa.size() == 0) {
			_documentRecord.removeSlideListWithText(slwt);
		} else {
			slwt.setSlideAtomsSets(sa.toArray(new SlideAtomsSet[sa.size()]));
			slwt.setChildRecord(records.toArray(new Record[records.size()]));
		}
		_slides = sl.toArray(new Slide[sl.size()]);

		// if the removed slide had notes - remove references to them too

		if (removedSlide != null) {
			int notesId = removedSlide.getSlideRecord().getSlideAtom().getNotesID();
			if (notesId != 0) {
				SlideListWithText nslwt = _documentRecord.getNotesSlideListWithText();
				records = new ArrayList<Record>();
				ArrayList<SlideAtomsSet> na = new ArrayList<SlideAtomsSet>();
				for (SlideAtomsSet ns : nslwt.getSlideAtomsSets()) {
					if (ns.getSlidePersistAtom().getSlideIdentifier() != notesId) {
						na.add(ns);
						records.add(ns.getSlidePersistAtom());
						if (ns.getSlideRecords() != null)
							records.addAll(Arrays.asList(ns.getSlideRecords()));
					}
				}
				if (na.size() == 0) {
					_documentRecord.removeSlideListWithText(nslwt);
				} else {
					nslwt.setSlideAtomsSets(na.toArray(new SlideAtomsSet[na.size()]));
					nslwt.setChildRecord(records.toArray(new Record[records.size()]));
				}

			}
		}
		_notes = nt.toArray(new Notes[nt.size()]);

		return removedSlide;
	}

	/*
	 * ===============================================================
	 *  Addition Code
	 * ===============================================================
	 */

	/**
	 * Create a blank <code>Slide</code>.
	 *
	 * @return the created <code>Slide</code>
	 */
	public Slide createSlide() {
		SlideListWithText slist = null;

		// We need to add the records to the SLWT that deals
		// with Slides.
		// Add it, if it doesn't exist
		slist = _documentRecord.getSlideSlideListWithText();
		if (slist == null) {
			// Need to add a new one
			slist = new SlideListWithText();
			slist.setInstance(SlideListWithText.SLIDES);
			_documentRecord.addSlideListWithText(slist);
		}

		// Grab the SlidePersistAtom with the highest Slide Number.
		// (Will stay as null if no SlidePersistAtom exists yet in
		// the slide, or only master slide's ones do)
		SlidePersistAtom prev = null;
		for (SlideAtomsSet sas : slist.getSlideAtomsSets()) {
			SlidePersistAtom spa = sas.getSlidePersistAtom();
			if (spa.getSlideIdentifier() < 0) {
				// This is for a master slide
				// Odd, since we only deal with the Slide SLWT
			} else {
				// Must be for a real slide
				if (prev == null) {
					prev = spa;
				}
				if (prev.getSlideIdentifier() < spa.getSlideIdentifier()) {
					prev = spa;
				}
			}
		}

		// Set up a new SlidePersistAtom for this slide
		SlidePersistAtom sp = new SlidePersistAtom();

		// First slideId is always 256
		sp.setSlideIdentifier(prev == null ? 256 : (prev.getSlideIdentifier() + 1));

		// Add this new SlidePersistAtom to the SlideListWithText
		slist.addSlidePersistAtom(sp);

		// Create a new Slide
		Slide slide = new Slide(sp.getSlideIdentifier(), sp.getRefID(), _slides.length + 1);
		slide.setSlideShow(this);
		slide.onCreate();

		// Add in to the list of Slides
		Slide[] s = new Slide[_slides.length + 1];
		System.arraycopy(_slides, 0, s, 0, _slides.length);
		s[_slides.length] = slide;
		_slides = s;
		logger.log(POILogger.INFO, "Added slide " + _slides.length + " with ref " + sp.getRefID()
				+ " and identifier " + sp.getSlideIdentifier());

		// Add the core records for this new Slide to the record tree
		org.apache.poi.hslf.record.Slide slideRecord = slide.getSlideRecord();
		int psrId = addPersistentObject(slideRecord);
		sp.setRefID(psrId);
		slideRecord.setSheetId(psrId);
		
		slide.setMasterSheet(_masters[0]);
		// All done and added
		return slide;
	}

	/**
	 * Adds a picture to this presentation and returns the associated index.
	 *
	 * @param data
	 *            picture data
	 * @param format
	 *            the format of the picture. One of constans defined in the
	 *            <code>Picture</code> class.
	 * @return the index to this picture (1 based).
	 */
	public int addPicture(byte[] data, int format) throws IOException {
		byte[] uid = PictureData.getChecksum(data);

		EscherContainerRecord bstore;

		EscherContainerRecord dggContainer = _documentRecord.getPPDrawingGroup().getDggContainer();
		bstore = (EscherContainerRecord) Shape.getEscherChild(dggContainer,
				EscherContainerRecord.BSTORE_CONTAINER);
		if (bstore == null) {
			bstore = new EscherContainerRecord();
			bstore.setRecordId(EscherContainerRecord.BSTORE_CONTAINER);

			dggContainer.addChildBefore(bstore, EscherOptRecord.RECORD_ID);
		} else {
			Iterator<EscherRecord> iter = bstore.getChildIterator();
			for (int i = 0; iter.hasNext(); i++) {
				EscherBSERecord bse = (EscherBSERecord) iter.next();
				if (Arrays.equals(bse.getUid(), uid)) {
					return i + 1;
				}
			}
		}

		PictureData pict = PictureData.create(format);
		pict.setData(data);

		int offset = _hslfSlideShow.addPicture(pict);

		EscherBSERecord bse = new EscherBSERecord();
		bse.setRecordId(EscherBSERecord.RECORD_ID);
		bse.setOptions((short) (0x0002 | (format << 4)));
		bse.setSize(pict.getRawData().length + 8);
		bse.setUid(uid);

		bse.setBlipTypeMacOS((byte) format);
		bse.setBlipTypeWin32((byte) format);

		if (format == Picture.EMF)
			bse.setBlipTypeMacOS((byte) Picture.PICT);
		else if (format == Picture.WMF)
			bse.setBlipTypeMacOS((byte) Picture.PICT);
		else if (format == Picture.PICT)
			bse.setBlipTypeWin32((byte) Picture.WMF);

		bse.setRef(0);
		bse.setOffset(offset);
		bse.setRemainingData(new byte[0]);

		bstore.addChildRecord(bse);
		int count = bstore.getChildRecords().size();
		bstore.setOptions((short) ((count << 4) | 0xF));

		return count;
	}

	/**
	 * Adds a picture to this presentation and returns the associated index.
	 *
	 * @param pict
	 *            the file containing the image to add
	 * @param format
	 *            the format of the picture. One of constans defined in the
	 *            <code>Picture</code> class.
	 * @return the index to this picture (1 based).
	 */
	public int addPicture(File pict, int format) throws IOException {
		int length = (int) pict.length();
		byte[] data = new byte[length];
        FileInputStream is = null;
        try {
			is = new FileInputStream(pict);
			is.read(data);
		} finally {
            if(is != null) is.close();
        }
		return addPicture(data, format);
	}

	/**
	 * Add a font in this presentation
	 *
	 * @param font
	 *            the font to add
	 * @return 0-based index of the font
	 */
	public int addFont(PPFont font) {
		FontCollection fonts = getDocumentRecord().getEnvironment().getFontCollection();
		int idx = fonts.getFontIndex(font.getFontName());
		if (idx == -1) {
			idx = fonts.addFont(font.getFontName(), font.getCharSet(), font.getFontFlags(), font
					.getFontType(), font.getPitchAndFamily());
		}
		return idx;
	}

	/**
	 * Get a font by index
	 *
	 * @param idx
	 *            0-based index of the font
	 * @return of an instance of <code>PPFont</code> or <code>null</code> if not
	 *         found
	 */
	public PPFont getFont(int idx) {
		FontCollection fonts = getDocumentRecord().getEnvironment().getFontCollection();
		for (Record ch : fonts.getChildRecords()) {
			if (ch instanceof FontEntityAtom) {
				FontEntityAtom atom = (FontEntityAtom) ch;
				if (atom.getFontIndex() == idx) {
					return new PPFont(atom);
				}
			}
		}
		return null;
	}

	/**
	 * get the number of fonts in the presentation
	 *
	 * @return number of fonts
	 */
	public int getNumberOfFonts() {
		return getDocumentRecord().getEnvironment().getFontCollection().getNumberOfFonts();
	}

	/**
	 * Return Header / Footer settings for slides
	 *
	 * @return Header / Footer settings for slides
	 */
	public HeadersFooters getSlideHeadersFooters() {
		// detect if this ppt was saved in Office2007
		String tag = getSlidesMasters()[0].getProgrammableTag();
		boolean ppt2007 = "___PPT12".equals(tag);

		HeadersFootersContainer hdd = null;
		for (Record ch : _documentRecord.getChildRecords()) {
			if (ch instanceof HeadersFootersContainer
				&& ((HeadersFootersContainer) ch).getOptions() == HeadersFootersContainer.SlideHeadersFootersContainer) {
				hdd = (HeadersFootersContainer) ch;
				break;
			}
		}
		boolean newRecord = false;
		if (hdd == null) {
			hdd = new HeadersFootersContainer(HeadersFootersContainer.SlideHeadersFootersContainer);
			newRecord = true;
		}
		return new HeadersFooters(hdd, this, newRecord, ppt2007);
	}

	/**
	 * Return Header / Footer settings for notes
	 *
	 * @return Header / Footer settings for notes
	 */
	public HeadersFooters getNotesHeadersFooters() {
		// detect if this ppt was saved in Office2007
		String tag = getSlidesMasters()[0].getProgrammableTag();
		boolean ppt2007 = "___PPT12".equals(tag);

		HeadersFootersContainer hdd = null;
		for (Record ch : _documentRecord.getChildRecords()) {
			if (ch instanceof HeadersFootersContainer
					&& ((HeadersFootersContainer) ch).getOptions() == HeadersFootersContainer.NotesHeadersFootersContainer) {
				hdd = (HeadersFootersContainer) ch;
				break;
			}
		}
		boolean newRecord = false;
		if (hdd == null) {
			hdd = new HeadersFootersContainer(HeadersFootersContainer.NotesHeadersFootersContainer);
			newRecord = true;
		}
		if (ppt2007 && _notes.length > 0) {
			return new HeadersFooters(hdd, _notes[0], newRecord, ppt2007);
		}
		return new HeadersFooters(hdd, this, newRecord, ppt2007);
	}

	/**
	 * Add a movie in this presentation
	 *
	 * @param path
	 *            the path or url to the movie
	 * @return 0-based index of the movie
	 */
	public int addMovie(String path, int type) {
		ExMCIMovie mci;
		switch (type) {
			case MovieShape.MOVIE_MPEG:
				mci = new ExMCIMovie();
				break;
			case MovieShape.MOVIE_AVI:
				mci = new ExAviMovie();
				break;
			default:
				throw new IllegalArgumentException("Unsupported Movie: " + type);
		}

		ExVideoContainer exVideo = mci.getExVideo();
		exVideo.getExMediaAtom().setMask(0xE80000);
		exVideo.getPathAtom().setText(path);

		int objectId = addToObjListAtom(mci);
		exVideo.getExMediaAtom().setObjectId(objectId);
		
		return objectId;
	}

	/**
	 * Add a control in this presentation
	 *
	 * @param name
	 *            name of the control, e.g. "Shockwave Flash Object"
	 * @param progId
	 *            OLE Programmatic Identifier, e.g.
	 *            "ShockwaveFlash.ShockwaveFlash.9"
	 * @return 0-based index of the control
	 */
	public int addControl(String name, String progId) {
		ExControl ctrl = new ExControl();
		ctrl.setProgId(progId);
		ctrl.setMenuName(name);
		ctrl.setClipboardName(name);
		
		ExOleObjAtom oleObj = ctrl.getExOleObjAtom();
		oleObj.setDrawAspect(ExOleObjAtom.DRAW_ASPECT_VISIBLE);
		oleObj.setType(ExOleObjAtom.TYPE_CONTROL);
		oleObj.setSubType(ExOleObjAtom.SUBTYPE_DEFAULT);
		
		int objectId = addToObjListAtom(ctrl);
		oleObj.setObjID(objectId);
		return objectId;
	}

	/**
	 * Add a hyperlink to this presentation
	 *
	 * @return 0-based index of the hyperlink
	 */
	public int addHyperlink(Hyperlink link) {
		ExHyperlink ctrl = new ExHyperlink();
		ExHyperlinkAtom obj = ctrl.getExHyperlinkAtom();
        if(link.getType() == Hyperlink.LINK_SLIDENUMBER) {
            ctrl.setLinkURL(link.getAddress(), 0x30);
        } else {
            ctrl.setLinkURL(link.getAddress());
        }
		ctrl.setLinkTitle(link.getTitle());

		int objectId = addToObjListAtom(ctrl);
		link.setId(objectId);
		obj.setNumber(objectId);

		return objectId;
	}

	/**
	 * Add a embedded object to this presentation
	 *
	 * @return 0-based index of the embedded object
	 */
	public int addEmbed(POIFSFileSystem poiData) {
        DirectoryNode root = poiData.getRoot();
        
        // prepare embedded data
        if (new ClassID().equals(root.getStorageClsid())) {
        	// need to set class id
	        Map<String,ClassID> olemap = getOleMap();
	        ClassID classID = null;
	    	for (Map.Entry<String,ClassID> entry : olemap.entrySet()) {
	    		if (root.hasEntry(entry.getKey())) {
	    			classID = entry.getValue();
	    			break;
	    		}
	    	}
	    	if (classID == null) {
	    		throw new IllegalArgumentException("Unsupported embedded document");    		
	    	}
	    	
	    	root.setStorageClsid(classID);
        }
        
		ExEmbed exEmbed = new ExEmbed();
        // remove unneccessary infos, so we don't need to specify the type
        // of the ole object multiple times
        Record children[] = exEmbed.getChildRecords();
        exEmbed.removeChild(children[2]);
        exEmbed.removeChild(children[3]);
        exEmbed.removeChild(children[4]);

        ExEmbedAtom eeEmbed = exEmbed.getExEmbedAtom();
        eeEmbed.setCantLockServerB(true);

        ExOleObjAtom eeAtom = exEmbed.getExOleObjAtom();
        eeAtom.setDrawAspect(ExOleObjAtom.DRAW_ASPECT_VISIBLE);
        eeAtom.setType(ExOleObjAtom.TYPE_EMBEDDED);
        // eeAtom.setSubType(ExOleObjAtom.SUBTYPE_EXCEL);
        // should be ignored?!?, see MS-PPT ExOleObjAtom, but Libre Office sets it ...
        eeAtom.setOptions(1226240);

        ExOleObjStg exOleObjStg = new ExOleObjStg();
        try {
	        final String OLESTREAM_NAME = "\u0001Ole";
	        if (!root.hasEntry(OLESTREAM_NAME)) {
	            // the following data was taken from an example libre office document
	            // beside this "\u0001Ole" record there were several other records, e.g. CompObj,
	            // OlePresXXX, but it seems, that they aren't neccessary
	            byte oleBytes[] = { 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	            poiData.createDocument(new ByteArrayInputStream(oleBytes), OLESTREAM_NAME);
	        }        

	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        poiData.writeFilesystem(bos);
	        exOleObjStg.setData(bos.toByteArray());
        } catch (IOException e) {
        	throw new HSLFException(e);
        }
        
        int psrId = addPersistentObject(exOleObjStg);
        exOleObjStg.setPersistId(psrId);
        eeAtom.setObjStgDataRef(psrId);
        
		int objectId = addToObjListAtom(exEmbed);
		eeAtom.setObjID(objectId);
		return objectId;
	}

	protected int addToObjListAtom(RecordContainer exObj) {
		ExObjList lst = (ExObjList) _documentRecord.findFirstOfType(RecordTypes.ExObjList.typeID);
		if (lst == null) {
			lst = new ExObjList();
			_documentRecord.addChildAfter(lst, _documentRecord.getDocumentAtom());
		}
		ExObjListAtom objAtom = lst.getExObjListAtom();
		// increment the object ID seed
		int objectId = (int) objAtom.getObjectIDSeed() + 1;
		objAtom.setObjectIDSeed(objectId);

		lst.addChildAfter(exObj, objAtom);
		
		return objectId;
	}

    protected static Map<String,ClassID> getOleMap() {
    	Map<String,ClassID> olemap = new HashMap<String,ClassID>();
    	olemap.put("PowerPoint Document", ClassID.PPT_SHOW);
    	olemap.put("Workbook", ClassID.EXCEL97); // as per BIFF8 spec
    	olemap.put("WORKBOOK", ClassID.EXCEL97); // Typically from third party programs
    	olemap.put("BOOK", ClassID.EXCEL97); // Typically odd Crystal Reports exports
    	// ... to be continued
    	return olemap;
    }

    protected int addPersistentObject(PositionDependentRecord slideRecord) {
    	slideRecord.setLastOnDiskOffset(HSLFSlideShow.UNSET_OFFSET);
		_hslfSlideShow.appendRootLevelRecord((Record)slideRecord);

        // For position dependent records, hold where they were and now are
        // As we go along, update, and hand over, to any Position Dependent
        // records we happen across
		Map<RecordTypes.Type,PositionDependentRecord> interestingRecords =
                new HashMap<RecordTypes.Type,PositionDependentRecord>();

		try {
            _hslfSlideShow.updateAndWriteDependantRecords(null,interestingRecords);
        } catch (IOException e) {
            throw new HSLFException(e);
        }
		
		PersistPtrHolder ptr = (PersistPtrHolder)interestingRecords.get(RecordTypes.PersistPtrIncrementalBlock);
		UserEditAtom usr = (UserEditAtom)interestingRecords.get(RecordTypes.UserEditAtom);

		// persist ID is UserEditAtom.maxPersistWritten + 1
		int psrId = usr.getMaxPersistWritten() + 1;

		// Last view is now of the slide
		usr.setLastViewType((short) UserEditAtom.LAST_VIEW_SLIDE_VIEW);
		// increment the number of persistent objects
		usr.setMaxPersistWritten(psrId);

		// Add the new slide into the last PersistPtr
		// (Also need to tell it where it is)
		int slideOffset = slideRecord.getLastOnDiskOffset();
		slideRecord.setLastOnDiskOffset(slideOffset);
		ptr.addSlideLookup(psrId, slideOffset);
		logger.log(POILogger.INFO, "New slide/object ended up at " + slideOffset);

		return psrId;
    }
}
