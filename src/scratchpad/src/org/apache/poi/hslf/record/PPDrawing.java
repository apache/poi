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

package org.apache.poi.hslf.record;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherBoolProperty;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherDgRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.ddf.EscherRGBProperty;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.ddf.EscherSpgrRecord;
import org.apache.poi.ddf.EscherTextboxRecord;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogger;

/**
 * These are actually wrappers onto Escher drawings. Make use of
 *  the DDF classes to do useful things with them.
 * For now, creates a tree of the Escher records, and then creates any
 *  PowerPoint (hslf) records found within the EscherTextboxRecord
 *  (msofbtClientTextbox) records.
 * Also provides easy access to the EscherTextboxRecords, so that their
 *  text may be extracted and used in Sheets
 */

// For now, pretending to be an atom. Might not always be, but that
//  would require a wrapping class
public final class PPDrawing extends RecordAtom {

	//arbitrarily selected; may need to increase
	private static final int MAX_RECORD_LENGTH = 10_485_760;


	private byte[] _header;
	private long _type;

	private final List<EscherRecord> childRecords = new ArrayList<>();
	private EscherTextboxWrapper[] textboxWrappers;

	//cached EscherDgRecord
	private EscherDgRecord dg;

	/**
	 * Get access to the underlying Escher Records
	 */
	public List<EscherRecord> getEscherRecords() { return childRecords; }

	/**
	 * Get access to the atoms inside Textboxes
	 */
	public EscherTextboxWrapper[] getTextboxWrappers() { return textboxWrappers; }


	/* ******************** record stuff follows ********************** */

    /**
     * Creates a new, empty, PPDrawing (typically for use with a new Slide
     *  or Notes)
     */
    public PPDrawing() {
        _header = new byte[8];
        LittleEndian.putUShort(_header, 0, 15);
        LittleEndian.putUShort(_header, 2, RecordTypes.PPDrawing.typeID);
        LittleEndian.putInt(_header, 4, 0);

        textboxWrappers = new EscherTextboxWrapper[]{};
        create();
    }

	/**
	 * Sets everything up, groks the escher etc
	 */
	protected PPDrawing(byte[] source, int start, int len) {
		// Get the header
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

		// Get the type
		_type = LittleEndian.getUShort(_header,2);

		// Get the contents for now
		final byte[] contents = IOUtils.safelyAllocate(len, MAX_RECORD_LENGTH);
		System.arraycopy(source,start,contents,0,len);

		// Build up a tree of Escher records contained within
		final DefaultEscherRecordFactory erf = new HSLFEscherRecordFactory();
		findEscherChildren(erf, contents, 8, len-8, childRecords);
		EscherContainerRecord dgContainer = getDgContainer();

		if (dgContainer != null) {
			textboxWrappers = findInDgContainer(dgContainer);
		} else {
			// Find and EscherTextboxRecord's, and wrap them up
			final List<EscherTextboxWrapper> textboxes = new ArrayList<>();
			findEscherTextboxRecord(childRecords, textboxes);
			this.textboxWrappers = textboxes.toArray(new EscherTextboxWrapper[textboxes.size()]);
		}
	}
	private EscherTextboxWrapper[] findInDgContainer(final EscherContainerRecord dgContainer) {
		final List<EscherTextboxWrapper> found = new LinkedList<>();
		final EscherContainerRecord spgrContainer = findFirstEscherContainerRecordOfType(RecordTypes.EscherSpgrContainer, dgContainer);
		final EscherContainerRecord[] spContainers = findAllEscherContainerRecordOfType(RecordTypes.EscherSpContainer, spgrContainer);
		for (EscherContainerRecord spContainer : spContainers) {
			EscherSpRecord sp = (EscherSpRecord)findFirstEscherRecordOfType(RecordTypes.EscherSp, spContainer);
			EscherTextboxRecord clientTextbox = (EscherTextboxRecord)findFirstEscherRecordOfType(RecordTypes.EscherClientTextbox, spContainer);
			if (null == clientTextbox) { continue; }

			EscherTextboxWrapper w = new EscherTextboxWrapper(clientTextbox);
            StyleTextProp9Atom nineAtom = findInSpContainer(spContainer);
			w.setStyleTextProp9Atom(nineAtom);
			if (null != sp) {
			    w.setShapeId(sp.getShapeId());
			}
			found.add(w);
		}
		return found.toArray(new EscherTextboxWrapper[found.size()]);
	}
	
	private StyleTextProp9Atom findInSpContainer(final EscherContainerRecord spContainer) {
        HSLFEscherClientDataRecord cldata = spContainer.getChildById(RecordTypes.EscherClientData.typeID);
        if (cldata == null) {
            return null;
        }
        DummyPositionSensitiveRecordWithChildren progTags =
            getChildRecord(cldata.getHSLFChildRecords(), RecordTypes.ProgTags);
        if (progTags == null) {
            return null;
        }
        DummyPositionSensitiveRecordWithChildren progBinaryTag =
            (DummyPositionSensitiveRecordWithChildren)progTags.findFirstOfType(RecordTypes.ProgBinaryTag.typeID);
        if (progBinaryTag == null) {
            return null;
        }
		int size = progBinaryTag.getChildRecords().length;
		if (2 != size) { return null; }

		final Record r0 = progBinaryTag.getChildRecords()[0];
		final Record r1 = progBinaryTag.getChildRecords()[1];
		
		if (!(r0 instanceof CString)) { return null; }
		if (!("___PPT9".equals(((CString) r0).getText()))) { return null; }
		if (!(r1 instanceof BinaryTagDataBlob )) { return null; }
		final BinaryTagDataBlob blob = (BinaryTagDataBlob) r1;
		if (1 != blob.getChildRecords().length) { return null; }
		return (StyleTextProp9Atom) blob.findFirstOfType(RecordTypes.StyleTextProp9Atom.typeID);
	}

	/**
	 * Tree walking way of finding Escher Child Records
	 */
	private void findEscherChildren(DefaultEscherRecordFactory erf, byte[] source, int startPos, int lenToGo, List<EscherRecord> found) {

		int escherBytes = LittleEndian.getInt( source, startPos + 4 ) + 8;

		// Find the record
		EscherRecord r = erf.createRecord(source,startPos);
		// Fill it in
		r.fillFields( source, startPos, erf );
		// Save it
		found.add(r);

		// Wind on
		int size = r.getRecordSize();
		if(size < 8) {
			logger.log(POILogger.WARN, "Hit short DDF record at " + startPos + " - " + size);
		}

		/**
		 * Sanity check. Always advance the cursor by the correct value.
		 *
		 * getRecordSize() must return exactly the same number of bytes that was written in fillFields.
		 * Sometimes it is not so, see an example in bug #44770. Most likely reason is that one of ddf records calculates wrong size.
		 */
		if(size != escherBytes){
			logger.log(POILogger.WARN, "Record length=" + escherBytes + " but getRecordSize() returned " + r.getRecordSize() + "; record: " + r.getClass());
			size = escherBytes;
		}
		startPos += size;
		lenToGo -= size;
		if(lenToGo >= 8) {
			findEscherChildren(erf, source, startPos, lenToGo, found);
		}
	}

	/**
	 * Look for EscherTextboxRecords
	 */
	private void findEscherTextboxRecord(List<EscherRecord> toSearch, List<EscherTextboxWrapper> found) {
	    EscherSpRecord sp = null;
	    for (EscherRecord r : toSearch) {
	        if (r instanceof EscherSpRecord) {
	            sp = (EscherSpRecord)r;
	        } else if (r instanceof EscherTextboxRecord) {
				EscherTextboxRecord tbr = (EscherTextboxRecord)r;
				EscherTextboxWrapper w = new EscherTextboxWrapper(tbr);
				if (sp != null) {
				    w.setShapeId(sp.getShapeId());
				}
				found.add(w);
			} else if (r.isContainerRecord()) {
				// If it has children, walk them
				List<EscherRecord> children = r.getChildRecords();
				findEscherTextboxRecord(children,found);
			}
		}
	}

	/**
	 * We are type 1036
	 */
	public long getRecordType() { return _type; }

	/**
	 * We're pretending to be an atom, so return null
	 */
	public Record[] getChildRecords() { return null; }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 * Walks the escher layer to get the contents
	 */
	public void writeOut(OutputStream out) throws IOException {
		// Ensure the escher layer reflects the text changes
		for (EscherTextboxWrapper w : textboxWrappers) {
			w.writeOut(null);
		}

		// Find the new size of the escher children;
		int newSize = 0;
		for(EscherRecord er : childRecords) {
			newSize += er.getRecordSize();
		}

		// Update the size (header bytes 5-8)
		LittleEndian.putInt(_header,4,newSize);

		// Write out our header
		out.write(_header);

		// Now grab the children's data
		byte[] b = new byte[newSize];
		int done = 0;
		for(EscherRecord r : childRecords) {
		    done += r.serialize( done, b );
		}

		// Finally, write out the children
		out.write(b);
	}

	/**
	 * Create the Escher records associated with a new PPDrawing
	 */
	private void create(){
		EscherContainerRecord dgContainer = new EscherContainerRecord();
		dgContainer.setRecordId( EscherContainerRecord.DG_CONTAINER );
		dgContainer.setOptions((short)15);

		dg = new EscherDgRecord();
		dg.setOptions((short)16);
		dg.setNumShapes(1);
		dgContainer.addChildRecord(dg);

		EscherContainerRecord spgrContainer = new EscherContainerRecord();
		spgrContainer.setOptions((short)15);
		spgrContainer.setRecordId(EscherContainerRecord.SPGR_CONTAINER);

		EscherContainerRecord spContainer = new EscherContainerRecord();
		spContainer.setOptions((short)15);
		spContainer.setRecordId(EscherContainerRecord.SP_CONTAINER);

		EscherSpgrRecord spgr = new EscherSpgrRecord();
		spgr.setOptions((short)1);
		spContainer.addChildRecord(spgr);

		EscherSpRecord sp = new EscherSpRecord();
		sp.setOptions((short)((ShapeType.NOT_PRIMITIVE.nativeId << 4) + 2));
		sp.setFlags(EscherSpRecord.FLAG_PATRIARCH | EscherSpRecord.FLAG_GROUP);
		spContainer.addChildRecord(sp);
		spgrContainer.addChildRecord(spContainer);
		dgContainer.addChildRecord(spgrContainer);

		spContainer = new EscherContainerRecord();
		spContainer.setOptions((short)15);
		spContainer.setRecordId(EscherContainerRecord.SP_CONTAINER);
		sp = new EscherSpRecord();
		sp.setOptions((short)((ShapeType.RECT.nativeId << 4) + 2));
		sp.setFlags(EscherSpRecord.FLAG_BACKGROUND | EscherSpRecord.FLAG_HASSHAPETYPE);
		spContainer.addChildRecord(sp);

		EscherOptRecord opt = new EscherOptRecord();
		opt.setRecordId(EscherOptRecord.RECORD_ID);
		opt.addEscherProperty(new EscherRGBProperty(EscherProperties.FILL__FILLCOLOR, 134217728));
		opt.addEscherProperty(new EscherRGBProperty(EscherProperties.FILL__FILLBACKCOLOR, 134217733));
		opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.FILL__RECTRIGHT, 10064750));
		opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.FILL__RECTBOTTOM, 7778750));
		opt.addEscherProperty(new EscherBoolProperty(EscherProperties.FILL__NOFILLHITTEST, 1179666));
		opt.addEscherProperty(new EscherBoolProperty(EscherProperties.LINESTYLE__NOLINEDRAWDASH, 524288));
		opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.SHAPE__BLACKANDWHITESETTINGS, 9));
		opt.addEscherProperty(new EscherSimpleProperty(EscherProperties.SHAPE__BACKGROUNDSHAPE, 65537));
		spContainer.addChildRecord(opt);

		dgContainer.addChildRecord(spContainer);
		
		childRecords.add(dgContainer);
	}

	/**
	 * Add a new EscherTextboxWrapper to this <code>PPDrawing</code>.
	 */
	public void addTextboxWrapper(EscherTextboxWrapper txtbox){
		EscherTextboxWrapper[] tw = new EscherTextboxWrapper[textboxWrappers.length + 1];
		System.arraycopy(textboxWrappers, 0, tw, 0, textboxWrappers.length);

		tw[textboxWrappers.length] = txtbox;
		textboxWrappers = tw;
	}

	/**
	 * @return the container record for drawings
	 * @since POI 3.14-Beta2
	 */
	public EscherContainerRecord getDgContainer() {
	    if (childRecords.isEmpty()) {
	        return null;
	    }
	    EscherRecord r = childRecords.get(0);
	    if (r instanceof EscherContainerRecord && r.getRecordId() == RecordTypes.EscherDgContainer.typeID) {
	        return (EscherContainerRecord)r;
	    } else {
	        return null;
	    }
	}
	
	/**
	 * Return EscherDgRecord which keeps track of the number of shapes and shapeId in this drawing group
	 *
	 * @return EscherDgRecord
	 */
	public EscherDgRecord getEscherDgRecord(){
		if (dg == null) {
		    EscherContainerRecord dgr = getDgContainer();
		    if (dgr != null) {
    			for(EscherRecord r : dgr.getChildRecords()){
    				if(r instanceof EscherDgRecord){
    					dg = (EscherDgRecord)r;
    					break;
    				}
    			}
		    }
		}
		return dg;
	}

    protected EscherContainerRecord findFirstEscherContainerRecordOfType(RecordTypes type, EscherContainerRecord parent) {
    	if (null == parent) { return null; }
		final List<EscherContainerRecord> children = parent.getChildContainers();
		for (EscherContainerRecord child : children) {
			if (type.typeID == child.getRecordId()) {
				return child;
			}
		}
		return null;
    }
    protected EscherRecord findFirstEscherRecordOfType(RecordTypes type, EscherContainerRecord parent) {
    	if (null == parent) { return null; }
		final List<EscherRecord> children = parent.getChildRecords();
		for (EscherRecord child : children) {
			if (type.typeID == child.getRecordId()) {
				return child;
			}
		}
		return null;
    }
    protected EscherContainerRecord[] findAllEscherContainerRecordOfType(RecordTypes type, EscherContainerRecord parent) {
    	if (null == parent) { return new EscherContainerRecord[0]; }
		final List<EscherContainerRecord> children = parent.getChildContainers();
		final List<EscherContainerRecord> result = new LinkedList<>();
		for (EscherContainerRecord child : children) {
			if (type.typeID == child.getRecordId()) {
				result.add(child);
			}
		}
		return result.toArray(new EscherContainerRecord[result.size()]);
    }

    public StyleTextProp9Atom[] getNumberedListInfo() {
    	final List<StyleTextProp9Atom> result = new LinkedList<>();
    	EscherContainerRecord dgContainer = getDgContainer();
		final EscherContainerRecord spgrContainer = findFirstEscherContainerRecordOfType(RecordTypes.EscherSpgrContainer, dgContainer);
		final EscherContainerRecord[] spContainers = findAllEscherContainerRecordOfType(RecordTypes.EscherSpContainer, spgrContainer);
		for (EscherContainerRecord spContainer : spContainers) {
		    StyleTextProp9Atom prop9 = findInSpContainer(spContainer);
		    if (prop9 != null) {
		        result.add(prop9);
		    }
		}
    	return result.toArray(new StyleTextProp9Atom[result.size()]);
	}
    
    @SuppressWarnings("unchecked")
    private static <T extends Record> T getChildRecord(List<? extends Record> children, RecordTypes type) {
        for (Record r : children) {
            if (r.getRecordType() == type.typeID) {
                return (T)r;
            }
        }
        return null;
    }
}
