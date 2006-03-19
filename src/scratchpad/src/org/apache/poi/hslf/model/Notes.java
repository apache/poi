
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
 * This class represents a slide's notes in a PowerPoint Document. It 
 *  allows access to the text within, and the layout. For now, it only 
 *  does the text side of things though
 *
 * @author Nick Burch
 */

public class Notes extends Sheet
{

  private int _sheetNo;
  private org.apache.poi.hslf.record.Notes _notes;
  private TextRun[] _runs;

  /**
   * Constructs a Notes Sheet from the given Notes record.
   * Initialises TextRuns, to provide easier access to the text
   *
   * @param notes the Notes record to read from
   */
  public Notes (org.apache.poi.hslf.record.Notes notes) {
	_notes = notes;

	// Grab the sheet number, via the NotesAtom
	_sheetNo = _notes.getNotesAtom().getSlideID();

	// Now, build up TextRuns from pairs of TextHeaderAtom and
	//  one of TextBytesAtom or TextCharsAtom, found inside 
	//  EscherTextboxWrapper's in the PPDrawing
	_runs = findTextRuns(_notes.getPPDrawing());
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
  
  protected PPDrawing getPPDrawing() { return _notes.getPPDrawing(); }} 
