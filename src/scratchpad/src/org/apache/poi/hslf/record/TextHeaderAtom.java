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
import java.io.IOException;
import java.io.OutputStream;

/**
 * A TextHeaderAtom  (type 3999). Holds information on what kind of
 *  text is contained in the TextBytesAtom / TextCharsAtom that follows
 *  straight after
 *
 * @author Nick Burch
 */

public final class TextHeaderAtom extends RecordAtom implements ParentAwareRecord
{
	private byte[] _header;
	private static long _type = 3999l;
	private RecordContainer parentRecord;

	public static final int TITLE_TYPE = 0;
	public static final int BODY_TYPE = 1;
	public static final int NOTES_TYPE = 2;
	public static final int OTHER_TYPE = 4;
	public static final int CENTRE_BODY_TYPE = 5;
	public static final int CENTER_TITLE_TYPE = 6;
	public static final int HALF_BODY_TYPE = 7;
	public static final int QUARTER_BODY_TYPE = 8;

	/** The kind of text it is */
	private int textType;

	public int getTextType() { return textType; }
	public void setTextType(int type) { textType = type; }

	public RecordContainer getParentRecord() { return parentRecord; }
	public void setParentRecord(RecordContainer record) { this.parentRecord = record; }

	/* *************** record code follows ********************** */

	/**
	 * For the TextHeader Atom
	 */
	protected TextHeaderAtom(byte[] source, int start, int len) {
		// Sanity Checking - we're always 12 bytes long
		if(len < 12) {
			len = 12;
			if(source.length - start < 12) {
				throw new RuntimeException("Not enough data to form a TextHeaderAtom (always 12 bytes long) - found " + (source.length - start));
			}
		}

		// Get the header
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

		// Grab the type
		textType = LittleEndian.getInt(source,start+8);
	}

	/**
	 * Create a new TextHeader Atom, for an unknown type of text
	 */
	public TextHeaderAtom() {
		_header = new byte[8];
		LittleEndian.putUShort(_header, 0, 0);
		LittleEndian.putUShort(_header, 2, (int)_type);
		LittleEndian.putInt(_header, 4, 4);

		textType = OTHER_TYPE;
	}

	/**
	 * We are of type 3999
	 */
	public long getRecordType() { return _type; }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		// Header - size or type unchanged
		out.write(_header);

		// Write out our type
		writeLittleEndian(textType,out);
	}
}
