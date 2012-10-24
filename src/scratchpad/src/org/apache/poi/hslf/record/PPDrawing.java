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

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogger;

import org.apache.poi.ddf.*;
import org.apache.poi.hslf.model.ShapeTypes;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;

/**
 * These are actually wrappers onto Escher drawings. Make use of
 *  the DDF classes to do useful things with them.
 * For now, creates a tree of the Escher records, and then creates any
 *  PowerPoint (hslf) records found within the EscherTextboxRecord
 *  (msofbtClientTextbox) records.
 * Also provides easy access to the EscherTextboxRecords, so that their
 *  text may be extracted and used in Sheets
 *
 * @author Nick Burch
 */

// For now, pretending to be an atom. Might not always be, but that
//  would require a wrapping class
public final class PPDrawing extends RecordAtom {
	private byte[] _header;
	private long _type;

	private EscherRecord[] childRecords;
	private EscherTextboxWrapper[] textboxWrappers;

	//cached EscherDgRecord
	private EscherDgRecord dg;

	/**
	 * Get access to the underlying Escher Records
	 */
	public EscherRecord[] getEscherRecords() { return childRecords; }

	/**
	 * Get access to the atoms inside Textboxes
	 */
	public EscherTextboxWrapper[] getTextboxWrappers() { return textboxWrappers; }


	/* ******************** record stuff follows ********************** */

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
		final byte[] contents = new byte[len];
		System.arraycopy(source,start,contents,0,len);

		// Build up a tree of Escher records contained within
		final DefaultEscherRecordFactory erf = new DefaultEscherRecordFactory();
		final Vector<EscherRecord> escherChildren = new Vector<EscherRecord>();
		findEscherChildren(erf, contents, 8, len-8, escherChildren);
		this.childRecords = (EscherRecord[]) escherChildren.toArray(new EscherRecord[escherChildren.size()]);

		if (1 == this.childRecords.length && (short)0xf002 == this.childRecords[0].getRecordId() && this.childRecords[0] instanceof EscherContainerRecord) {
			this.textboxWrappers = findInDgContainer((EscherContainerRecord) this.childRecords[0]);
		} else {
			// Find and EscherTextboxRecord's, and wrap them up
			final Vector<EscherTextboxWrapper> textboxes = new Vector<EscherTextboxWrapper>();
			findEscherTextboxRecord(childRecords, textboxes);
			this.textboxWrappers = (EscherTextboxWrapper[]) textboxes.toArray(new EscherTextboxWrapper[textboxes.size()]);
		}
	}
	private EscherTextboxWrapper[] findInDgContainer(final EscherContainerRecord escherContainerF002) {
		final List<EscherTextboxWrapper> found = new LinkedList<EscherTextboxWrapper>();
		final EscherContainerRecord SpgrContainer = findFirstEscherContainerRecordOfType((short)0xf003, escherContainerF002);
		final EscherContainerRecord[] escherContainersF004 = findAllEscherContainerRecordOfType((short)0xf004, SpgrContainer);
		for (EscherContainerRecord spContainer : escherContainersF004) {
			StyleTextProp9Atom nineAtom = findInSpContainer(spContainer);
			EscherSpRecord sp = null;
			final EscherRecord escherContainerF00A = findFirstEscherRecordOfType((short)0xf00a, spContainer);
			if (null != escherContainerF00A) {
				if (escherContainerF00A instanceof EscherSpRecord) {
					sp = (EscherSpRecord) escherContainerF00A;
				}
			}
			final EscherRecord escherContainerF00D = findFirstEscherRecordOfType((short)0xf00d, spContainer);
			if (null == escherContainerF00D) { continue; }
			if (escherContainerF00D instanceof EscherTextboxRecord) {
				EscherTextboxRecord tbr = (EscherTextboxRecord) escherContainerF00D;
				EscherTextboxWrapper w = new EscherTextboxWrapper(tbr);
				w.setStyleTextProp9Atom(nineAtom);
				if (null != sp) {
					w.setShapeId(sp.getShapeId());
				}
				found.add(w);
			}
		}
		return (EscherTextboxWrapper[]) found.toArray(new EscherTextboxWrapper[found.size()]);
	}
	private StyleTextProp9Atom findInSpContainer(final EscherContainerRecord spContainer) {
		final EscherContainerRecord escherContainerF011 = findFirstEscherContainerRecordOfType((short)0xf011, spContainer);
		if (null == escherContainerF011) { return null; }
		final EscherContainerRecord escherContainer1388 = findFirstEscherContainerRecordOfType((short)0x1388, escherContainerF011);
		if (null == escherContainer1388) { return null; }
		final EscherContainerRecord escherContainer138A = findFirstEscherContainerRecordOfType((short)0x138A, escherContainer1388);
		if (null == escherContainer138A) { return null; }
		int size = escherContainer138A.getChildRecords().size();
		if (2 != size) { return null; }
		final Record r0 = buildFromUnknownEscherRecord((UnknownEscherRecord) escherContainer138A.getChild(0));
		final Record r1 = buildFromUnknownEscherRecord((UnknownEscherRecord) escherContainer138A.getChild(1));
		if (!(r0 instanceof CString)) { return null; }
		if (!("___PPT9".equals(((CString) r0).getText()))) { return null; };
		if (!(r1 instanceof BinaryTagDataBlob )) { return null; }
		final BinaryTagDataBlob blob = (BinaryTagDataBlob) r1;
		if (1 != blob.getChildRecords().length) { return null; }
		return (StyleTextProp9Atom) blob.findFirstOfType(0x0FACL);
	}
	/**
	 * Creates a new, empty, PPDrawing (typically for use with a new Slide
	 *  or Notes)
	 */
	public PPDrawing(){
		_header = new byte[8];
		LittleEndian.putUShort(_header, 0, 15);
		LittleEndian.putUShort(_header, 2, RecordTypes.PPDrawing.typeID);
		LittleEndian.putInt(_header, 4, 0);

		textboxWrappers = new EscherTextboxWrapper[]{};
		create();
	}

	/**
	 * Tree walking way of finding Escher Child Records
	 */
	private void findEscherChildren(DefaultEscherRecordFactory erf, byte[] source, int startPos, int lenToGo, Vector<EscherRecord> found) {

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
	private void findEscherTextboxRecord(EscherRecord[] toSearch, Vector<EscherTextboxWrapper> found) {
		for(int i=0; i<toSearch.length; i++) {
			if(toSearch[i] instanceof EscherTextboxRecord) {
				EscherTextboxRecord tbr = (EscherTextboxRecord)toSearch[i];
				EscherTextboxWrapper w = new EscherTextboxWrapper(tbr);
				found.add(w);
				for (int j = i; j >= 0; j--) {
					if(toSearch[j] instanceof EscherSpRecord){
						EscherSpRecord sp = (EscherSpRecord)toSearch[j];
						w.setShapeId(sp.getShapeId());
						break;
					}
				}
			} else {
				// If it has children, walk them
				if(toSearch[i].isContainerRecord()) {
					List<EscherRecord> childrenL = toSearch[i].getChildRecords();
					EscherRecord[] children = new EscherRecord[childrenL.size()];
					childrenL.toArray(children);
					findEscherTextboxRecord(children,found);
				}
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
		for(int i=0; i<textboxWrappers.length; i++) {
			textboxWrappers[i].writeOut(null);
		}

		// Find the new size of the escher children;
		int newSize = 0;
		for(int i=0; i<childRecords.length; i++) {
			newSize += childRecords[i].getRecordSize();
		}

		// Update the size (header bytes 5-8)
		LittleEndian.putInt(_header,4,newSize);

		// Write out our header
		out.write(_header);

		// Now grab the children's data
		byte[] b = new byte[newSize];
		int done = 0;
		for(int i=0; i<childRecords.length; i++) {
			int written = childRecords[i].serialize( done, b );
			done += written;
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

		EscherDgRecord dg = new EscherDgRecord();
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
		sp.setOptions((short)((ShapeTypes.NotPrimitive << 4) + 2));
		sp.setFlags(EscherSpRecord.FLAG_PATRIARCH | EscherSpRecord.FLAG_GROUP);
		spContainer.addChildRecord(sp);
		spgrContainer.addChildRecord(spContainer);
		dgContainer.addChildRecord(spgrContainer);

		spContainer = new EscherContainerRecord();
		spContainer.setOptions((short)15);
		spContainer.setRecordId(EscherContainerRecord.SP_CONTAINER);
		sp = new EscherSpRecord();
		sp.setOptions((short)((ShapeTypes.Rectangle << 4) + 2));
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

		childRecords = new EscherRecord[]{
			dgContainer
		};
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
	 * Return EscherDgRecord which keeps track of the number of shapes and shapeId in this drawing group
	 *
	 * @return EscherDgRecord
	 */
	public EscherDgRecord getEscherDgRecord(){
		if(dg == null){
			EscherContainerRecord dgContainer = (EscherContainerRecord)childRecords[0];
			for(Iterator<EscherRecord> it = dgContainer.getChildIterator(); it.hasNext();){
				EscherRecord r = it.next();
				if(r instanceof EscherDgRecord){
					dg = (EscherDgRecord)r;
					break;
				}
			}
		}
		return dg;
	}

    protected EscherContainerRecord findFirstEscherContainerRecordOfType(short type, EscherContainerRecord parent) {
    	if (null == parent) { return null; }
		final List<EscherContainerRecord> children = parent.getChildContainers();
		for (EscherContainerRecord child : children) {
			if (type == child.getRecordId()) {
				return child;
			}
		}
		return null;
    }
    protected EscherRecord findFirstEscherRecordOfType(short type, EscherContainerRecord parent) {
    	if (null == parent) { return null; }
		final List<EscherRecord> children = parent.getChildRecords();
		for (EscherRecord child : children) {
			if (type == child.getRecordId()) {
				return child;
			}
		}
		return null;
    }
    protected EscherContainerRecord[] findAllEscherContainerRecordOfType(short type, EscherContainerRecord parent) {
    	if (null == parent) { return new EscherContainerRecord[0]; }
		final List<EscherContainerRecord> children = parent.getChildContainers();
		final List<EscherContainerRecord> result = new LinkedList<EscherContainerRecord>();
		for (EscherContainerRecord child : children) {
			if (type == child.getRecordId()) {
				result.add(child);
			}
		}
		return (EscherContainerRecord[]) result.toArray(new EscherContainerRecord[result.size()]);
    }
    protected Record buildFromUnknownEscherRecord(UnknownEscherRecord unknown) {
		byte[] bingo = unknown.getData();
		byte[] restoredRecord = new byte[8 + bingo.length];
		System.arraycopy(bingo, 0, restoredRecord, 8, bingo.length);
		short recordVersion = unknown.getVersion();
		short recordId = unknown.getRecordId();
		int recordLength = unknown.getRecordSize();
		LittleEndian.putShort(restoredRecord, 0, recordVersion);
		LittleEndian.putShort(restoredRecord, 2, recordId);
		LittleEndian.putInt(restoredRecord, 4, recordLength);
		return Record.createRecordForType(recordId, restoredRecord, 0, restoredRecord.length);
    }

    public StyleTextProp9Atom[] getNumberedListInfo() {
    	final List<StyleTextProp9Atom> result = new LinkedList<StyleTextProp9Atom>();
    	EscherRecord[] escherRecords = this.getEscherRecords();
    	for (EscherRecord escherRecord : escherRecords) {
			if (escherRecord instanceof EscherContainerRecord && (short)0xf002 == escherRecord.getRecordId()) {
				EscherContainerRecord escherContainerF002 = (EscherContainerRecord) escherRecord;
				final EscherContainerRecord escherContainerF003 = findFirstEscherContainerRecordOfType((short)0xf003, escherContainerF002);
				final EscherContainerRecord[] escherContainersF004 = findAllEscherContainerRecordOfType((short)0xf004, escherContainerF003);
				for (EscherContainerRecord containerF004 : escherContainersF004) {
					final EscherContainerRecord escherContainerF011 = findFirstEscherContainerRecordOfType((short)0xf011, containerF004);
					if (null == escherContainerF011) { continue; }
					final EscherContainerRecord escherContainer1388 = findFirstEscherContainerRecordOfType((short)0x1388, escherContainerF011);
					if (null == escherContainer1388) { continue; }
					final EscherContainerRecord escherContainer138A = findFirstEscherContainerRecordOfType((short)0x138A, escherContainer1388);
					if (null == escherContainer138A) { continue; }
					int size = escherContainer138A.getChildRecords().size();
					if (2 != size) { continue; }
					final Record r0 = buildFromUnknownEscherRecord((UnknownEscherRecord) escherContainer138A.getChild(0));
					final Record r1 = buildFromUnknownEscherRecord((UnknownEscherRecord) escherContainer138A.getChild(1));
					if (!(r0 instanceof CString)) { continue; }
					if (!("___PPT9".equals(((CString) r0).getText()))) { continue; };
					if (!(r1 instanceof BinaryTagDataBlob )) { continue; }
					final BinaryTagDataBlob blob = (BinaryTagDataBlob) r1;
					if (1 != blob.getChildRecords().length) { continue; }
					result.add((StyleTextProp9Atom) blob.findFirstOfType(0x0FACL));
				}
			}
    	}
    	return (StyleTextProp9Atom[]) result.toArray(new StyleTextProp9Atom[result.size()]);
	}
}
