
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
        


package org.apache.poi.hslf.model;

import java.util.*;

import org.apache.poi.hslf.record.*;
import org.apache.poi.hslf.record.SlideListWithText.*;
import org.apache.poi.util.LittleEndian;

/**
 * This class represents a slide in a PowerPoint Document. It allows 
 *  access to the text within, and the layout. For now, it only does
 *  the text side of things though
 *
 * @author Nick Burch
 */

public class Slide extends Sheet
{

  private int _sheetNo;
  private org.apache.poi.hslf.record.Slide _slide;
  private SlideAtomsSet[] _atomSet;
  private TextRun[] _runs;
  private TextRun[] _otherRuns; // Any from the PPDrawing, shouldn't really be any though
  private Notes _notes;

  /**
   * Constructs a Slide from the Slide record, and the SlideAtomsSets
   *  for ones not embeded in the PPDrawing.
   * Initialises TextRuns, to provide easier access to the text
   *
   * @param slide the Slide record we're based on
   * @param atomSet the SlideAtomsSet to get the text from
   */
  public Slide(org.apache.poi.hslf.record.Slide slide, Notes notes, SlideAtomsSet[] atomSet) {
	_slide = slide;
	_notes = notes;
	_atomSet = atomSet;

	// Grab the sheet number
	//_sheetNo = _slide.getSlideAtom().getSheetNumber();
	_sheetNo = -1;

	// Grab the TextRuns from the PPDrawing
	_otherRuns = findTextRuns(_slide.getPPDrawing());


	// Ensure we've only got only copy of each SlideAtomSet
	// When in doubt, prefere the later one
	Hashtable seenSets = new Hashtable();
	Vector useSets = new Vector();
	for(int i=0; i<_atomSet.length; i++) {
		SlideAtomsSet set = _atomSet[i];
		int id = set.getSlidePersistAtom().getRefID();
		Integer idI = new Integer(id);
		if(seenSets.containsKey(idI)) {
			// Replace old one
			Integer replacePos = (Integer)seenSets.get(idI);
			useSets.set(replacePos.intValue(),set);
		} else {
			// Use for now
			useSets.add(set);
			seenSets.put(idI,new Integer(useSets.size()-1));
		}
	}

	// For the text coming in from the SlideAtomsSet:
	// Build up TextRuns from pairs of TextHeaderAtom and
	//  one of TextBytesAtom or TextCharsAtom
	Vector runSets = new Vector();
	for(int i=0; i<useSets.size(); i++) {
		SlideAtomsSet set = (SlideAtomsSet)useSets.get(i);
		findTextRuns(set.getSlideRecords(),runSets);
	}
	// Build an array, more useful than a vector
	_runs = new TextRun[runSets.size()];
	for(int i=0; i<_runs.length; i++) {
		_runs[i] = (TextRun)runSets.get(i);
	}
  }


  // Accesser methods follow

  /**
   * Returns an array of all the TextRuns found
   */
  public TextRun[] getTextRuns() { return _runs; }

  /**
   * Returns the sheet number
   */
  public int getSheetNumber() { return _sheetNo; }

  /**
   * Returns the Notes Sheet for this slide, or null if there isn't one
   */
  public Notes getNotesSheet() { return _notes; }
} 
