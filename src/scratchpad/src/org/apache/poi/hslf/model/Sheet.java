
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

import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherDgRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.hslf.record.*;
import org.apache.poi.hslf.usermodel.SlideShow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * This class defines the common format of "Sheets" in a powerpoint
 *  document. Such sheets could be Slides, Notes, Master etc
 *
 * @author Nick Burch
 */

public abstract class Sheet
{
  /**
   * The <code>SlideShow</code> we belong to
   */
  private SlideShow _slideShow; 
  
  /**
   * Returns an array of all the TextRuns in the sheet.
   */
  public abstract TextRun[] getTextRuns();

  /**
   * Returns the (internal, RefID based) sheet number, as used 
   *  to in PersistPtr stuff.
   */
  public abstract int _getSheetRefId();
  
  /**
   * Returns the (internal, SlideIdentifier based) sheet number, as used 
   *  to reference this sheet from other records.
   */
  public abstract int _getSheetNumber();
  
  /**
   * Fetch the PPDrawing from the underlying record
   */
  protected abstract PPDrawing getPPDrawing();
  
  
  /**
   * Fetch the SlideShow we're attached to
   */
  public SlideShow getSlideShow() { return _slideShow; }
  
  /**
   * Set the SlideShow we're attached to.
   * Also passes it on to our child RichTextRuns
   */
  public void setSlideShow(SlideShow ss) { 
	  _slideShow = ss;
	  TextRun[] trs = getTextRuns();
	  if(trs != null) {
		  for(int i=0; i<trs.length; i++) {
			  trs[i].supplySlideShow(_slideShow);
		  }
	  }
  }

  
  /**
   * For a given PPDrawing, grab all the TextRuns
   */
  public static TextRun[] findTextRuns(PPDrawing ppdrawing) {
	Vector runsV = new Vector();
	EscherTextboxWrapper[] wrappers = ppdrawing.getTextboxWrappers();
	for(int i=0; i<wrappers.length; i++) {
		findTextRuns(wrappers[i].getChildRecords(),runsV);
	}
    TextRun[] runs = new TextRun[runsV.size()];
	for(int i=0; i<runs.length; i++) {
		runs[i] = (TextRun)runsV.get(i);
	}
	return runs;
  }

  /**
   * Scans through the supplied record array, looking for 
   * a TextHeaderAtom followed by one of a TextBytesAtom or
   * a TextCharsAtom. Builds up TextRuns from these
   *
   * @param records the records to build from
   * @param found vector to add any found to
   */
  protected static void findTextRuns(Record[] records, Vector found) {
	// Look for a TextHeaderAtom
	for(int i=0; i<(records.length-1); i++) {
		if(records[i] instanceof TextHeaderAtom) {
			TextRun trun = null;
			TextHeaderAtom tha = (TextHeaderAtom)records[i];
			StyleTextPropAtom stpa = null;
			
			// Look for a subsequent StyleTextPropAtom
			if(i < (records.length-2)) {
				if(records[i+2] instanceof StyleTextPropAtom) {
					stpa = (StyleTextPropAtom)records[i+2];
				}
			}
			
			// See what follows the TextHeaderAtom
			if(records[i+1] instanceof TextCharsAtom) {
				TextCharsAtom tca = (TextCharsAtom)records[i+1];
				trun = new TextRun(tha,tca,stpa);
			} else if(records[i+1] instanceof TextBytesAtom) {
				TextBytesAtom tba = (TextBytesAtom)records[i+1];
				trun = new TextRun(tha,tba,stpa);
			} else if(records[i+1].getRecordType() == 4001l) {
				// StyleTextPropAtom - Safe to ignore
			} else if(records[i+1].getRecordType() == 4010l) {
				// TextSpecInfoAtom - Safe to ignore
			} else {
				System.err.println("Found a TextHeaderAtom not followed by a TextBytesAtom or TextCharsAtom: Followed by " + records[i+1].getRecordType());
				continue;
			}

			if(trun != null) {
				found.add(trun);
				i++;
			} else {
				// Not a valid one, so skip on to next and look again
			}
		}
	}
  }

  /**
   * Returns all shapes contained in this Sheet
   *
   * @return all shapes contained in this Sheet (Slide or Notes)
   */
  public Shape[] getShapes() {
	PPDrawing ppdrawing = getPPDrawing();
	
	EscherContainerRecord dg = (EscherContainerRecord)ppdrawing.getEscherRecords()[0];
	EscherContainerRecord spgr = null;
	List ch = dg.getChildRecords();

	for (Iterator it = ch.iterator(); it.hasNext();) {
		EscherRecord rec = (EscherRecord)it.next();
		if (rec.getRecordId() == EscherContainerRecord.SPGR_CONTAINER){
				spgr = (EscherContainerRecord)rec;
				break;
		}
	}
	ch = spgr.getChildRecords();

	ArrayList shapes = new ArrayList();
	for (int i=1;i<ch.size();i++) {
		EscherContainerRecord sp = (EscherContainerRecord)ch.get(i);
		Shape sh = ShapeFactory.createShape(sp, null);
		sh.setSheet(this);
		shapes.add(sh);
	}
	
	return (Shape[])shapes.toArray(new Shape[shapes.size()]);
  }

  /**
   * Add a new Shape to this Slide
   *
   * @param shape - the Shape to add
   */
  public void addShape(Shape shape){
	PPDrawing ppdrawing = getPPDrawing();

	EscherContainerRecord dgContainer = (EscherContainerRecord)ppdrawing.getEscherRecords()[0];
	EscherContainerRecord spgr = (EscherContainerRecord)Shape.getEscherChild(dgContainer, EscherContainerRecord.SPGR_CONTAINER);
	spgr.addChildRecord(shape.getSpContainer());

	EscherDgRecord dg = (EscherDgRecord)Shape.getEscherChild(dgContainer, EscherDgRecord.RECORD_ID);
	dg.setNumShapes(dg.getNumShapes()+1);
	
	shape.setSheet(this);
	shape.afterInsert(this);
	
	// If it's a TextBox, we need to tell the PPDrawing, as it has to 
	//  track TextboxWrappers specially
	if(shape instanceof TextBox) {
		TextBox tbox = (TextBox)shape;
		ppdrawing.addTextboxWrapper(tbox._txtbox);
	}
  }
    
    /**
     * Return the master sheet .
     */
    public MasterSheet getMasterSheet(){
        return null;
    }

    /**
     * Color scheme for this sheet.
     */
     public  ColorSchemeAtom getColorScheme(){
        return null;
    }
}
