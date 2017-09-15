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
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.common.usermodel.fonts.FontInfo;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.hpsf.ClassID;
import org.apache.poi.hslf.exceptions.CorruptPowerPointFileException;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.hslf.model.HeadersFooters;
import org.apache.poi.hslf.model.MovieShape;
import org.apache.poi.hslf.record.Document;
import org.apache.poi.hslf.record.DocumentAtom;
import org.apache.poi.hslf.record.ExAviMovie;
import org.apache.poi.hslf.record.ExControl;
import org.apache.poi.hslf.record.ExEmbed;
import org.apache.poi.hslf.record.ExEmbedAtom;
import org.apache.poi.hslf.record.ExMCIMovie;
import org.apache.poi.hslf.record.ExObjList;
import org.apache.poi.hslf.record.ExObjListAtom;
import org.apache.poi.hslf.record.ExOleObjAtom;
import org.apache.poi.hslf.record.ExOleObjStg;
import org.apache.poi.hslf.record.ExVideoContainer;
import org.apache.poi.hslf.record.FontCollection;
import org.apache.poi.hslf.record.HeadersFootersContainer;
import org.apache.poi.hslf.record.MainMaster;
import org.apache.poi.hslf.record.Notes;
import org.apache.poi.hslf.record.PersistPtrHolder;
import org.apache.poi.hslf.record.PositionDependentRecord;
import org.apache.poi.hslf.record.PositionDependentRecordContainer;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RecordContainer;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.hslf.record.Slide;
import org.apache.poi.hslf.record.SlideListWithText;
import org.apache.poi.hslf.record.SlideListWithText.SlideAtomsSet;
import org.apache.poi.hslf.record.SlidePersistAtom;
import org.apache.poi.hslf.record.TxMasterStyleAtom;
import org.apache.poi.hslf.record.UserEditAtom;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.sl.usermodel.MasterSheet;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.Resources;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Units;

/**
 * This class is a friendly wrapper on top of the more scary HSLFSlideShow.
 *
 * TODO: - figure out how to match notes to their correct sheet (will involve
 * understanding DocSlideList and DocNotesList) - handle Slide creation cleaner
 */
public final class HSLFSlideShow implements SlideShow<HSLFShape,HSLFTextParagraph>, Closeable {
    /** Powerpoint document entry/stream name */
    public static final String POWERPOINT_DOCUMENT = "PowerPoint Document";
    
    enum LoadSavePhase {
        INIT, LOADED
	}
    private static final ThreadLocal<LoadSavePhase> loadSavePhase = new ThreadLocal<LoadSavePhase>();
    
    // What we're based on
	private final HSLFSlideShowImpl _hslfSlideShow;

	// Pointers to the most recent versions of the core records
	// (Document, Notes, Slide etc)
	private Record[] _mostRecentCoreRecords;
	// Lookup between the PersitPtr "sheet" IDs, and the position
	// in the mostRecentCoreRecords array
	private Map<Integer,Integer> _sheetIdToCoreRecordsLookup;

	// Records that are interesting
	private Document _documentRecord;

	// Friendly objects for people to deal with
	private final List<HSLFSlideMaster> _masters = new ArrayList<HSLFSlideMaster>();
	private final List<HSLFTitleMaster> _titleMasters = new ArrayList<HSLFTitleMaster>();
	private final List<HSLFSlide> _slides = new ArrayList<HSLFSlide>();
	private final List<HSLFNotes> _notes = new ArrayList<HSLFNotes>();
	private FontCollection _fonts;

	// For logging
	private static final POILogger logger = POILogFactory.getLogger(HSLFSlideShow.class);


	/**
	 * Constructs a Powerpoint document from the underlying
	 * HSLFSlideShow object. Finds the model stuff from this
	 *
	 * @param hslfSlideShow the HSLFSlideShow to base on
	 */
	public HSLFSlideShow(HSLFSlideShowImpl hslfSlideShow) {
	    loadSavePhase.set(LoadSavePhase.INIT);
	    
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
		
		loadSavePhase.set(LoadSavePhase.LOADED);
	}

	/**
	 * Constructs a new, empty, Powerpoint document.
	 */
	public HSLFSlideShow() {
		this(HSLFSlideShowImpl.create());
	}

	/**
	 * Constructs a Powerpoint document from an input stream.
	 */
	@SuppressWarnings("resource")
    public HSLFSlideShow(InputStream inputStream) throws IOException {
		this(new HSLFSlideShowImpl(inputStream));
	}

    /**
     * Constructs a Powerpoint document from an POIFSFileSystem.
     */
    @SuppressWarnings("resource")
    public HSLFSlideShow(NPOIFSFileSystem npoifs) throws IOException {
        this(new HSLFSlideShowImpl(npoifs));
    }

    /**
     * Constructs a Powerpoint document from an DirectoryNode.
     */
    @SuppressWarnings("resource")
    public HSLFSlideShow(DirectoryNode root) throws IOException {
        this(new HSLFSlideShowImpl(root));
    }

    /**
     * @return the current loading/saving phase
     */
    protected static LoadSavePhase getLoadSavePhase() {
        return loadSavePhase.get();
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
			if (!(record instanceof PositionDependentRecord)) {
                continue;
            }
			
			PositionDependentRecord pdr = (PositionDependentRecord) record;
			int recordAt = pdr.getLastOnDiskOffset();

			Integer thisID = mostRecentByBytesRev.get(recordAt);
			
			if (thisID == null) {
                continue;
            }
			
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
			} /*else {
				// No record at this number
				// Odd, but not normally a problem
			}*/
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
			return _mostRecentCoreRecords[coreRecordId];
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

		findMasterSlides();
		
		// Having sorted out the masters, that leaves the notes and slides
        Map<Integer,Integer> slideIdToNotes = new HashMap<Integer,Integer>();

        // Start by finding the notes records
        findNotesSlides(slideIdToNotes);

		// Now, do the same thing for our slides
		findSlides(slideIdToNotes);
	}

	/**
     * Find master slides
     * These can be MainMaster records, but oddly they can also be
     * Slides or Notes, and possibly even other odd stuff....
     * About the only thing you can say is that the master details are in the first SLWT.
	 */
	private void findMasterSlides() {
        SlideListWithText masterSLWT = _documentRecord.getMasterSlideListWithText();
        if (masterSLWT == null) {
            return;
        }
        
        for (SlideAtomsSet sas : masterSLWT.getSlideAtomsSets()) {
            Record r = getCoreRecordForSAS(sas);
            int sheetNo = sas.getSlidePersistAtom().getSlideIdentifier();
            if (r instanceof Slide) {
                HSLFTitleMaster master = new HSLFTitleMaster((Slide)r, sheetNo);
                master.setSlideShow(this);
                _titleMasters.add(master);
            } else if (r instanceof MainMaster) {
                HSLFSlideMaster master = new HSLFSlideMaster((MainMaster)r, sheetNo);
                master.setSlideShow(this);
                _masters.add(master);
            }
        }
	}
	
	private void findNotesSlides(Map<Integer,Integer> slideIdToNotes) {
        SlideListWithText notesSLWT = _documentRecord.getNotesSlideListWithText();

        if (notesSLWT == null) {
            return;
        }
        
        // Match up the records and the SlideAtomSets
        int idx = -1;
        for (SlideAtomsSet notesSet : notesSLWT.getSlideAtomsSets()) {
            idx++;
            // Get the right core record
            Record r = getCoreRecordForSAS(notesSet);
            SlidePersistAtom spa = notesSet.getSlidePersistAtom();

            String loggerLoc = "A Notes SlideAtomSet at "+idx+" said its record was at refID "+spa.getRefID();

            // we need to add null-records, otherwise the index references to other existing don't work anymore
            if (r == null) {
                logger.log(POILogger.WARN, loggerLoc+", but that record didn't exist - record ignored.");
                continue;
            }

            // Ensure it really is a notes record
            if (!(r instanceof Notes)) {
                logger.log(POILogger.ERROR, loggerLoc+", but that was actually a " + r);
                continue;
            }
            
            Notes notesRecord = (Notes) r;

            // Record the match between slide id and these notes
            int slideId = spa.getSlideIdentifier();
            slideIdToNotes.put(slideId, idx);
            
            HSLFNotes hn = new HSLFNotes(notesRecord);
            hn.setSlideShow(this);
            _notes.add(hn);
        }
	}

	private void findSlides(Map<Integer,Integer> slideIdToNotes) {
        SlideListWithText slidesSLWT = _documentRecord.getSlideSlideListWithText();
        if (slidesSLWT == null) {
            return;
        }

        // Match up the records and the SlideAtomSets
        int idx = -1;
        for (SlideAtomsSet sas : slidesSLWT.getSlideAtomsSets()) {
            idx++;
            // Get the right core record
            SlidePersistAtom spa = sas.getSlidePersistAtom();
            Record r = getCoreRecordForSAS(sas);

            // Ensure it really is a slide record
            if (!(r instanceof Slide)) {
                logger.log(POILogger.ERROR, "A Slide SlideAtomSet at " + idx
                        + " said its record was at refID "
                        + spa.getRefID()
                        + ", but that was actually a " + r);
                continue;
            }
            
            Slide slide = (Slide)r;

            // Do we have a notes for this?
            HSLFNotes notes = null;
            // Slide.SlideAtom.notesId references the corresponding notes slide.
            // 0 if slide has no notes.
            int noteId = slide.getSlideAtom().getNotesID();
            if (noteId != 0) {
                Integer notesPos = slideIdToNotes.get(noteId);
                if (notesPos != null && 0 <= notesPos && notesPos < _notes.size()) {
                    notes = _notes.get(notesPos);
                } else {
                    logger.log(POILogger.ERROR, "Notes not found for noteId=" + noteId);
                }
            }

            // Now, build our slide
            int slideIdentifier = spa.getSlideIdentifier();
            HSLFSlide hs = new HSLFSlide(slide, notes, sas, slideIdentifier, (idx + 1));
            hs.setSlideShow(this);
            _slides.add(hs);
        }
	}
	
	@Override
	public void write(OutputStream out) throws IOException {
	    // check for text paragraph modifications
	    for (HSLFSlide sl : getSlides()) {
	        writeDirtyParagraphs(sl);
	    }

	    for (HSLFSlideMaster sl : getSlideMasters()) {
	        boolean isDirty = false;
	        for (List<HSLFTextParagraph> paras : sl.getTextParagraphs()) {
	            for (HSLFTextParagraph p : paras) {
	                isDirty |= p.isDirty();
	            }
	        }
	        if (isDirty) {
	            for (TxMasterStyleAtom sa : sl.getTxMasterStyleAtoms()) {
	                if (sa != null) {
	                    // not all master style atoms are set - index 3 is typically null
	                    sa.updateStyles();
	                }
	            }
	        }
	    }
	    
		_hslfSlideShow.write(out);
	}
	
	private void writeDirtyParagraphs(HSLFShapeContainer container) {
        for (HSLFShape sh : container.getShapes()) {
            if (sh instanceof HSLFShapeContainer) {
                writeDirtyParagraphs((HSLFShapeContainer)sh);
            } else if (sh instanceof HSLFTextShape) {
                HSLFTextShape hts = (HSLFTextShape)sh;
                boolean isDirty = false;
                for (HSLFTextParagraph p : hts.getTextParagraphs()) {
                    isDirty |= p.isDirty();
                }
                if (isDirty) {
                    hts.storeText();
                }
            }
        }
	}

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
	@Override
	public List<HSLFSlide> getSlides() {
		return _slides;
	}

	/**
	 * Returns an array of all the normal Notes found in the slideshow
	 */
	public List<HSLFNotes> getNotes() {
		return _notes;
	}

	/**
	 * Returns an array of all the normal Slide Masters found in the slideshow
	 */
	@Override
	public List<HSLFSlideMaster> getSlideMasters() {
		return _masters;
	}
	
	/**
	 * Returns an array of all the normal Title Masters found in the slideshow
	 */
	public List<HSLFTitleMaster> getTitleMasters() {
		return _titleMasters;
	}

	@Override
	public List<HSLFPictureData> getPictureData() {
		return _hslfSlideShow.getPictureData();
	}

	/**
	 * Returns the data of all the embedded OLE object in the SlideShow
	 */
	public HSLFObjectData[] getEmbeddedObjects() {
		return _hslfSlideShow.getEmbeddedObjects();
	}

	/**
	 * Returns the data of all the embedded sounds in the SlideShow
	 */
	public HSLFSoundData[] getSoundData() {
		return HSLFSoundData.find(_documentRecord);
	}

	@Override
	public Dimension getPageSize() {
		DocumentAtom docatom = _documentRecord.getDocumentAtom();
		int pgx = (int)Units.masterToPoints((int)docatom.getSlideSizeX());
		int pgy = (int)Units.masterToPoints((int)docatom.getSlideSizeY());
		return new Dimension(pgx, pgy);
	}

	@Override
	public void setPageSize(Dimension pgsize) {
		DocumentAtom docatom = _documentRecord.getDocumentAtom();
		docatom.setSlideSizeX(Units.pointsToMaster(pgsize.width));
		docatom.setSlideSizeY(Units.pointsToMaster(pgsize.height));
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
		if (oldSlideNumber > _slides.size() || newSlideNumber > _slides.size()) {
			throw new IllegalArgumentException(
					"Old and new slide numbers must not exceed the number of slides ("
							+ _slides.size() + ")");
		}

		// The order of slides is defined by the order of slide atom sets in the
		// SlideListWithText container.
		SlideListWithText slwt = _documentRecord.getSlideSlideListWithText();
		SlideAtomsSet[] sas = slwt.getSlideAtomsSets();

		SlideAtomsSet tmp = sas[oldSlideNumber - 1];
		sas[oldSlideNumber - 1] = sas[newSlideNumber - 1];
		sas[newSlideNumber - 1] = tmp;

		Collections.swap(_slides, oldSlideNumber - 1, newSlideNumber - 1);
		_slides.get(newSlideNumber - 1).setSlideNumber(newSlideNumber);
		_slides.get(oldSlideNumber - 1).setSlideNumber(oldSlideNumber);
		
		ArrayList<Record> lst = new ArrayList<Record>();
		for (SlideAtomsSet s : sas) {
			lst.add(s.getSlidePersistAtom());
			lst.addAll(Arrays.asList(s.getSlideRecords()));
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
	public HSLFSlide removeSlide(int index) {
		int lastSlideIdx = _slides.size() - 1;
		if (index < 0 || index > lastSlideIdx) {
			throw new IllegalArgumentException("Slide index (" + index + ") is out of range (0.."
					+ lastSlideIdx + ")");
		}

		SlideListWithText slwt = _documentRecord.getSlideSlideListWithText();
		SlideAtomsSet[] sas = slwt.getSlideAtomsSets();

		List<Record> records = new ArrayList<Record>();
		List<SlideAtomsSet> sa = new ArrayList<SlideAtomsSet>(Arrays.asList(sas));

		HSLFSlide removedSlide = _slides.remove(index);
		_notes.remove(removedSlide.getNotes());
		sa.remove(index);
		
		int i=0;
		for (HSLFSlide s : _slides) {
            s.setSlideNumber(i++);
        }
		
		for (SlideAtomsSet s : sa) {
            records.add(s.getSlidePersistAtom());
            records.addAll(Arrays.asList(s.getSlideRecords()));
		}
		if (sa.isEmpty()) {
			_documentRecord.removeSlideListWithText(slwt);
		} else {
			slwt.setSlideAtomsSets(sa.toArray(new SlideAtomsSet[sa.size()]));
			slwt.setChildRecord(records.toArray(new Record[records.size()]));
		}

		// if the removed slide had notes - remove references to them too

        int notesId = removedSlide.getSlideRecord().getSlideAtom().getNotesID();
		if (notesId != 0) {
			SlideListWithText nslwt = _documentRecord.getNotesSlideListWithText();
			records = new ArrayList<Record>();
			ArrayList<SlideAtomsSet> na = new ArrayList<SlideAtomsSet>();
			for (SlideAtomsSet ns : nslwt.getSlideAtomsSets()) {
				if (ns.getSlidePersistAtom().getSlideIdentifier() == notesId) {
                    continue;
                }
				na.add(ns);
				records.add(ns.getSlidePersistAtom());
				if (ns.getSlideRecords() != null) {
					records.addAll(Arrays.asList(ns.getSlideRecords()));
				}
			}
			if (na.isEmpty()) {
				_documentRecord.removeSlideListWithText(nslwt);
			} else {
				nslwt.setSlideAtomsSets(na.toArray(new SlideAtomsSet[na.size()]));
				nslwt.setChildRecord(records.toArray(new Record[records.size()]));
			}
		}

		return removedSlide;
	}

	/**
	 * Create a blank <code>Slide</code>.
	 *
	 * @return the created <code>Slide</code>
	 */
	@Override
	public HSLFSlide createSlide() {
		// We need to add the records to the SLWT that deals
		// with Slides.
		// Add it, if it doesn't exist
		SlideListWithText slist = _documentRecord.getSlideSlideListWithText();
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
		HSLFSlide slide = new HSLFSlide(sp.getSlideIdentifier(), sp.getRefID(), _slides.size() + 1);
		slide.setSlideShow(this);
		slide.onCreate();

		// Add in to the list of Slides
		_slides.add(slide);
		logger.log(POILogger.INFO, "Added slide " + _slides.size() + " with ref " + sp.getRefID()
				+ " and identifier " + sp.getSlideIdentifier());

		// Add the core records for this new Slide to the record tree
		Slide slideRecord = slide.getSlideRecord();
		int psrId = addPersistentObject(slideRecord);
		sp.setRefID(psrId);
		slideRecord.setSheetId(psrId);
		
		slide.setMasterSheet(_masters.get(0));
		// All done and added
		return slide;
	}

	@Override
	public HSLFPictureData addPicture(byte[] data, PictureType format) throws IOException {
		if (format == null || format.nativeId == -1) {
			throw new IllegalArgumentException("Unsupported picture format: " + format); 
		}
		
		HSLFPictureData pd = findPictureData(data);
		if (pd != null) {
			// identical picture was already added to the SlideShow
			return pd;
		}
		
		EscherContainerRecord bstore;

		EscherContainerRecord dggContainer = _documentRecord.getPPDrawingGroup().getDggContainer();
		bstore = (EscherContainerRecord) HSLFShape.getEscherChild(dggContainer,
				EscherContainerRecord.BSTORE_CONTAINER);
		if (bstore == null) {
			bstore = new EscherContainerRecord();
			bstore.setRecordId(EscherContainerRecord.BSTORE_CONTAINER);

			dggContainer.addChildBefore(bstore, EscherOptRecord.RECORD_ID);
		}

		HSLFPictureData pict = HSLFPictureData.create(format);
		pict.setData(data);

		int offset = _hslfSlideShow.addPicture(pict);

		EscherBSERecord bse = new EscherBSERecord();
		bse.setRecordId(EscherBSERecord.RECORD_ID);
		bse.setOptions((short) (0x0002 | (format.nativeId << 4)));
		bse.setSize(pict.getRawData().length + 8);
		byte[] uid = HSLFPictureData.getChecksum(data);
		bse.setUid(uid);

		bse.setBlipTypeMacOS((byte) format.nativeId);
		bse.setBlipTypeWin32((byte) format.nativeId);

		if (format == PictureType.EMF) {
			bse.setBlipTypeMacOS((byte) PictureType.PICT.nativeId);
		} else if (format == PictureType.WMF) {
			bse.setBlipTypeMacOS((byte) PictureType.PICT.nativeId);
		} else if (format == PictureType.PICT) {
			bse.setBlipTypeWin32((byte) PictureType.WMF.nativeId);
		}

		bse.setRef(0);
		bse.setOffset(offset);
		bse.setRemainingData(new byte[0]);

		bstore.addChildRecord(bse);
		int count = bstore.getChildRecords().size();
		bstore.setOptions((short) ((count << 4) | 0xF));

		return pict;
	}

	/**
	 * Adds a picture to the presentation.
	 *
	 * @param is	        The stream to read the image from
	 * @param format        The format of the picture.
	 *
	 * @return the picture data.
	 * @since 3.15 beta 2
	 */
	@Override
	public HSLFPictureData addPicture(InputStream is, PictureType format) throws IOException {
	    if (format == null || format.nativeId == -1) { // fail early
	        throw new IllegalArgumentException("Unsupported picture format: " + format);
	    }
	    return addPicture(IOUtils.toByteArray(is), format);
	}

	/**
	 * Adds a picture to the presentation.
	 *
	 * @param pict
	 *            the file containing the image to add
	 * @param format
	 *            The format of the picture.
	 *
	 * @return the picture data.
	 * @since 3.15 beta 2
	 */
	@Override
	public HSLFPictureData addPicture(File pict, PictureType format) throws IOException {
	    if (format == null || format.nativeId == -1) { // fail early
	        throw new IllegalArgumentException("Unsupported picture format: " + format);
	    }
		int length = (int) pict.length();
		byte[] data = new byte[length];
        FileInputStream is = new FileInputStream(pict);
		try {
			IOUtils.readFully(is, data);
		} finally {
            is.close();
        }
		return addPicture(data, format);
	}
	
    /**
     * check if a picture with this picture data already exists in this presentation
     * 
     * @param pictureData The picture data to find in the SlideShow
     * @return {@code null} if picture data is not found in this slideshow
     * @since 3.15 beta 3
     */
    @Override
    public HSLFPictureData findPictureData(byte[] pictureData) {
        byte[] uid = HSLFPictureData.getChecksum(pictureData);

        for (HSLFPictureData pic : getPictureData()) {
            if (Arrays.equals(pic.getUID(), uid)) {
                return pic;
            }
        }
        return null;
    }

	/**
	 * Add a font in this presentation
	 *
	 * @param fontInfo the font to add
	 * @return the registered HSLFFontInfo - the font info object is unique based on the typeface
	 */
	public HSLFFontInfo addFont(FontInfo fontInfo) {
		return getDocumentRecord().getEnvironment().getFontCollection().addFont(fontInfo);
	}

	/**
	 * Get a font by index
	 *
	 * @param idx
	 *            0-based index of the font
	 * @return of an instance of <code>PPFont</code> or <code>null</code> if not
	 *         found
	 */
	public HSLFFontInfo getFont(int idx) {
	    return getDocumentRecord().getEnvironment().getFontCollection().getFontInfo(idx);
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
		return new HeadersFooters(this, HeadersFootersContainer.SlideHeadersFootersContainer);
	}

	/**
	 * Return Header / Footer settings for notes
	 *
	 * @return Header / Footer settings for notes
	 */
	public HeadersFooters getNotesHeadersFooters() {
	    if (_notes.isEmpty()) {
	        return new HeadersFooters(this, HeadersFootersContainer.NotesHeadersFootersContainer);
	    } else {
	        return new HeadersFooters(_notes.get(0), HeadersFootersContainer.NotesHeadersFootersContainer);
	    }
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
		ExObjList lst = getDocumentRecord().getExObjList(true);
		ExObjListAtom objAtom = lst.getExObjListAtom();
		// increment the object ID seed
		int objectId = (int) objAtom.getObjectIDSeed() + 1;
		objAtom.setObjectIDSeed(objectId);

		lst.addChildAfter(exObj, objAtom);
		
		return objectId;
	}

    protected static Map<String,ClassID> getOleMap() {
    	Map<String,ClassID> olemap = new HashMap<String,ClassID>();
    	olemap.put(POWERPOINT_DOCUMENT, ClassID.PPT_SHOW);
    	olemap.put("Workbook", ClassID.EXCEL97); // as per BIFF8 spec
    	olemap.put("WORKBOOK", ClassID.EXCEL97); // Typically from third party programs
    	olemap.put("BOOK", ClassID.EXCEL97); // Typically odd Crystal Reports exports
    	// ... to be continued
    	return olemap;
    }

    protected int addPersistentObject(PositionDependentRecord slideRecord) {
    	slideRecord.setLastOnDiskOffset(HSLFSlideShowImpl.UNSET_OFFSET);
		_hslfSlideShow.appendRootLevelRecord((Record)slideRecord);

        // For position dependent records, hold where they were and now are
        // As we go along, update, and hand over, to any Position Dependent
        // records we happen across
		Map<RecordTypes,PositionDependentRecord> interestingRecords =
                new HashMap<RecordTypes,PositionDependentRecord>();

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

    @Override
    public MasterSheet<HSLFShape,HSLFTextParagraph> createMasterSheet() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Resources getResources() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return the handler class which holds the hslf records
     */
    @Internal
    public HSLFSlideShowImpl getSlideShowImpl() {
        return _hslfSlideShow;
    }
    
    @Override
    public void close() throws IOException {
        _hslfSlideShow.close();
    }
}
