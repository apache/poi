
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

import org.apache.poi.hslf.record.PPDrawing;

/**
 * This class represents a slide's notes in a PowerPoint Document. It 
 *  allows access to the text within, and the layout. For now, it only 
 *  does the text side of things though
 *
 * @author Nick Burch
 */

public class Notes extends Sheet
{
  private int _refSheetNo;
  private int _slideNo;
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
	
	// Grab our internal sheet ID
	_refSheetNo = notes.getSheetId();

	// Grab the number of the slide we're for, via the NotesAtom
	_slideNo = _notes.getNotesAtom().getSlideID();

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
   * Returns the (internal, RefID based) sheet number, as used 
   *  to in PersistPtr stuff.
   */
  public int _getSheetRefId() { return _refSheetNo; }
  /**
   * Returns the (internal, SlideIdentifer based) number of the 
   *  slide we're attached to 
   */
  public int _getSheetNumber() { return _slideNo; }
  
  protected PPDrawing getPPDrawing() { return _notes.getPPDrawing(); }} 
