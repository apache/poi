
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
        


package org.apache.poi.hslf.record;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Vector;
import org.apache.poi.util.LittleEndian;


/**
 * This abstract class represents a record in the PowerPoint document.
 * Record classes should extend with RecordContainer or RecordAtom, which
 *  extend this in turn.
 *
 * @author Nick Burch
 */

public abstract class Record
{
	/** 
	 * Is this record type an Atom record (only has data),
	 *  or is it a non-Atom record (has other records)?
	 */
	public abstract boolean isAnAtom();

	/**
	 * Returns the type (held as a little endian in bytes 3 and 4)
	 *  that this class handles
	 */
	public abstract long getRecordType();

	/** 
	 * Fetch all the child records of this record
	 * If this record is an atom, will return null
	 * If this record is a non-atom, but has no children, will return 
	 *  an empty array
	 */
	public abstract Record[] getChildRecords();

	/**
	 * Have the contents printer out into an OutputStream, used when
	 *  writing a file back out to disk
	 * (Normally, atom classes will keep their bytes around, but
	 *  non atom classes will just request the bytes from their 
	 *  children, then chuck on their header and return)
	 */
	public abstract void writeOut(OutputStream o) throws IOException;

	/**
	 * When writing out, write out a signed int (32bit) in Little Endian format
	 */
	public static void writeLittleEndian(int i,OutputStream o) throws IOException {
		byte[] bi = new byte[4];
		LittleEndian.putInt(bi,i);
		o.write(bi);
	}
	/**
	 * When writing out, write out a signed short (16bit) in Little Endian format
	 */
	public static void writeLittleEndian(short s,OutputStream o) throws IOException {
		byte[] bs = new byte[2];
		LittleEndian.putShort(bs,s);
		o.write(bs);
	}

	/**
	 * Default method for finding child records of a given record
	 */
	public static Record[] findChildRecords(byte[] b, int start, int len) {
		Vector children = new Vector(5);

		// Jump our little way along, creating records as we go
		int pos = start;
		while(pos <= (start+len-8)) {
			long type = LittleEndian.getUShort(b,pos+2);
			long rlen = LittleEndian.getUInt(b,pos+4);

			// Sanity check the length
			int rleni = (int)rlen;
			if(rleni < 0) { rleni = 0; }

//System.out.println("Found a " + type + " at pos " + pos + " (" + Integer.toHexString(pos) + "), len " + rlen);
			Record r = createRecordForType(type,b,pos,8+rleni);
			children.add(r);
			pos += 8;
			pos += rlen;
		}

		// Turn the vector into an array, and return
		Record[] cRecords = new Record[children.size()];
		for(int i=0; i < children.size(); i++) {
			cRecords[i] = (Record)children.get(i);
		}
		return cRecords;
	}

	/**
	 * For a given type (little endian bytes 3 and 4 in record header),
	 *  byte array, start position and length:
	 *  will return a Record object that will handle that record
	 *
	 * Remember that while PPT stores the record lengths as 8 bytes short
	 *  (not including the size of the header), this code assumes you're
	 *  passing in corrected lengths
	 */
	public static Record createRecordForType(long type, byte[] b, int start, int len) {
		Record toReturn = null;

		// Default is to use UnknownRecordPlaceholder
		// When you create classes for new Records, add them here
		switch((int)type) {
			// Document
			case 1000:
				toReturn = new DummyPositionSensitiveRecordWithChildren(b,start,len);
				break;
				
			// DocumentAtom
			case 1001:
				toReturn = new DocumentAtom(b,start,len);
				break;
				
			// "Slide"
			case 1006:
				toReturn = new Slide(b,start,len);
				break;

			// "SlideAtom"
			case 1007:
				toReturn = new SlideAtom(b,start,len);
				break;

			// "Notes"
			case 1008:
				toReturn = new Notes(b,start,len);
				break;
				
			// "NotesAtom" (Details on Notes sheets)
			case 1009:
				toReturn = new NotesAtom(b,start,len);
				break;
				
			// "SlidePersistAtom" (Details on text for a sheet)
			case 1011:
				toReturn = new SlidePersistAtom(b,start,len);
				break;
				
			// MainMaster (MetaSheet lives inside the PPDrawing inside this)
			case 1016:
				toReturn = new DummyPositionSensitiveRecordWithChildren(b,start,len);
				break;

			// PPDrawing (MetaSheet lives inside this)
			case 1036:
				toReturn = new PPDrawing(b,start,len);
				break;

			// ColorSchemeAtom (Holds the colours that make up a colour scheme)
			case 2032:
				toReturn = new ColorSchemeAtom(b,start,len);
				break;

			// TextHeaderAtom (Holds details on following text)
			case 3999:
				toReturn = new TextHeaderAtom(b,start,len);
				break;
				
			// TextCharsAtom (Text in Unicode format)
			case 4000:
				toReturn = new TextCharsAtom(b,start,len);
				break;
				
			// TextByteAtom (Text in ascii format)
			case 4008:
				toReturn = new TextBytesAtom(b,start,len);
				break;
				
			// SlideListWithText (Many Sheets live inside here)
			case 4080:
				toReturn = new SlideListWithText(b,start,len);
				break;

			// UserEditAtom (Holds pointers, last viewed etc)
			case 4085:
				toReturn = new UserEditAtom(b,start,len);
				break;

			// PersistPtrFullBlock (Don't know what it holds, but do care about where it lives)
			case 6001:
				toReturn = new PersistPtrHolder(b,start,len);
				break;
			// PersistPtrIncrementalBlock (Don't know what it holds, but do care about where it lives)
			case 6002:
				toReturn = new PersistPtrHolder(b,start,len);
				break;
				
			default:
				toReturn = new UnknownRecordPlaceholder(b,start,len);
				break;
		}

		// If it's a position aware record, tell it where it is
		if(toReturn instanceof PositionDependentRecord) {
			PositionDependentRecord pdr = (PositionDependentRecord)toReturn;
			pdr.setLastOnDiskOffset(start);
		}

		// Return the record
		return toReturn;
	}
}
