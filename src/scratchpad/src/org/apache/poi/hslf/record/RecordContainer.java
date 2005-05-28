
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

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hslf.util.MutableByteArrayOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

/**
 * Abstract class which all container records will extend. Providers
 *  helpful methods for writing child records out to disk
 *
 * @author Nick Burch
 */

public abstract class RecordContainer extends Record
{
	/** 
	 * We're not an atom
	 */
	public boolean isAnAtom() { return false; }

	/**
	 * Write out our header, and our children.
	 * @param headerA the first byte of the header
	 * @param headerB the second byte of the header
	 * @param type the record type
	 * @param children our child records
	 * @param out the stream to write to
	 */
	public void writeOut(byte headerA, byte headerB, long type, Record[] children, OutputStream out) throws IOException {
		// If we have a mutable output stream, take advantage of that
		if(out instanceof MutableByteArrayOutputStream) {
			MutableByteArrayOutputStream mout = 
				(MutableByteArrayOutputStream)out;

			// Grab current size
			int oldSize = mout.getBytesWritten();

			// Write out our header, less the size
			mout.write(new byte[] {headerA,headerB});
			byte[] typeB = new byte[2];
			LittleEndian.putShort(typeB,(short)type);
			mout.write(typeB);
			mout.write(new byte[4]);

			// Write out the children
			for(int i=0; i<children.length; i++) {
				children[i].writeOut(mout);
			}

			// Update our header with the size
			// Don't forget to knock 8 more off, since we don't include the
			//  header in the size
			int length = mout.getBytesWritten() - oldSize - 8;
			byte[] size = new byte[4];
			LittleEndian.putInt(size,0,length);
			mout.overwrite(size, oldSize+4);
		} else {
			// Going to have to do it a slower way, because we have
			// to update the length come the end

			// Create a ByteArrayOutputStream to hold everything in
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Write out our header, less the size
			baos.write(new byte[] {headerA,headerB});
			byte[] typeB = new byte[2];
			LittleEndian.putShort(typeB,(short)type);
			baos.write(typeB);
			baos.write(new byte[] {0,0,0,0});

			// Write out our children
			for(int i=0; i<children.length; i++) {
				children[i].writeOut(baos);
			}

			// Grab the bytes back
			byte[] toWrite = baos.toByteArray();

			// Update our header with the size
			// Don't forget to knock 8 more off, since we don't include the
			//  header in the size
			LittleEndian.putInt(toWrite,4,(toWrite.length-8));

			// Write out the bytes
			out.write(toWrite);
		}
	}
}
