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
import java.util.Vector;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.hslf.exceptions.CorruptPowerPointFileException;

/**
 * This abstract class represents a record in the PowerPoint document.
 * Record classes should extend with RecordContainer or RecordAtom, which
 *  extend this in turn.
 *
 * @author Nick Burch
 */

public abstract class Record
{
    // For logging
    protected POILogger logger = POILogFactory.getLogger(this.getClass());

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
	 * Build and return the Record at the given offset.
	 * Note - does less error checking and handling than findChildRecords
	 * @param b The byte array to build from
	 * @param offset The offset to build at
	 */
	public static Record buildRecordAtOffset(byte[] b, int offset) {
		long type = LittleEndian.getUShort(b,offset+2);
		long rlen = LittleEndian.getUInt(b,offset+4);

		// Sanity check the length
		int rleni = (int)rlen;
		if(rleni < 0) { rleni = 0; }

		return createRecordForType(type,b,offset,8+rleni);
	}

	/**
	 * Default method for finding child records of a container record
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

			// Abort if first record is of type 0000 and length FFFF,
			//  as that's a sign of a screwed up record
			if(pos == 0 && type == 0l && rleni == 0xffff) {
				throw new CorruptPowerPointFileException("Corrupt document - starts with record of type 0000 and length 0xFFFF");
			}

			Record r = createRecordForType(type,b,pos,8+rleni);
			if(r != null) {
				children.add(r);
			} else {
				// Record was horribly corrupt
			}
			pos += 8;
			pos += rleni;
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

		// Handle case of a corrupt last record, whose claimed length
		//  would take us passed the end of the file
		if(start + len > b.length) {
			System.err.println("Warning: Skipping record of type " + type + " at position " + start + " which claims to be longer than the file! (" + len + " vs " + (b.length-start) + ")");
			return null;
		}

		// We use the RecordTypes class to provide us with the right
		//  class to use for a given type
		// A spot of reflection gets us the (byte[],int,int) constructor
		// From there, we instanciate the class
		// Any special record handling occurs once we have the class
		Class c = null;
		try {
			c = RecordTypes.recordHandlingClass((int)type);
			if(c == null) {
				// How odd. RecordTypes normally subsitutes in
				//  a default handler class if it has heard of the record
				//  type but there's no support for it. Explicitly request
				//  that now
				c = RecordTypes.recordHandlingClass( RecordTypes.Unknown.typeID );
			}

			// Grab the right constructor
			java.lang.reflect.Constructor con = c.getDeclaredConstructor(new Class[] { byte[].class, Integer.TYPE, Integer.TYPE });
			// Instantiate
			toReturn = (Record)(con.newInstance(new Object[] { b, Integer.valueOf(start), Integer.valueOf(len) }));
		} catch(InstantiationException ie) {
			throw new RuntimeException("Couldn't instantiate the class for type with id " + type + " on class " + c + " : " + ie, ie);
		} catch(java.lang.reflect.InvocationTargetException ite) {
			throw new RuntimeException("Couldn't instantiate the class for type with id " + type + " on class " + c + " : " + ite + "\nCause was : " + ite.getCause(), ite);
		} catch(IllegalAccessException iae) {
			throw new RuntimeException("Couldn't access the constructor for type with id " + type + " on class " + c + " : " + iae, iae);
		} catch(NoSuchMethodException nsme) {
			throw new RuntimeException("Couldn't access the constructor for type with id " + type + " on class " + c + " : " + nsme, nsme);
		}

		// Handling for special kinds of records follow

		// If it's a position aware record, tell it where it is
		if(toReturn instanceof PositionDependentRecord) {
			PositionDependentRecord pdr = (PositionDependentRecord)toReturn;
			pdr.setLastOnDiskOffset(start);
		}

		// Return the created record
		return toReturn;
	}
}
