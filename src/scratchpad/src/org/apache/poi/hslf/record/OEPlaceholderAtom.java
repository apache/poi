
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
 * OEPlaceholderAtom (3011).
 * <p>
 *  Atom that describes the placeholder.
 * </p>
 *
 * @author Yegor Kozlov
 */

public class OEPlaceholderAtom extends RecordAtom{

    public static final int PLACEHOLDER_FULLSIZE = 0;
    public static final int PLACEHOLDER_HALFSIZE = 1;
    public static final int PLACEHOLDER_QUARTSIZE = 2;

    public static final byte None = 0;

    public static final byte MasterTitle = 1;

    public static final byte MasterBody = 2;

    public static final byte MasterCenteredTitle = 3;

    public static final byte MasterNotesSlideImage = 4;

    public static final byte MasterNotesBodyImage = 5;

    public static final byte MasterDate = 6;

    public static final byte MasterSlideNumber = 7;

    public static final byte MasterFooter = 8;

    public static final byte MasterHeader = 9;

    public static final byte MasterSubtitle = 10;

    public static final byte GenericTextObject = 11;

    public static final byte Title = 12;

    public static final byte Body = 13;

    public static final byte NotesBody = 14;

    public static final byte CenteredTitle = 15;

    public static final byte Subtitle = 16;

    public static final byte VerticalTextTitle = 17;

    public static final byte VerticalTextBody = 18;

    public static final byte NotesSlideImage = 19;

    public static final byte Object = 20;

    public static final byte Graph = 21;

    public static final byte Table = 22;

    public static final byte ClipArt = 23;

    public static final byte OrganizationChart = 24;

    public static final byte MediaClip = 25;

	private byte[] _header;

    private int placementId;
    private int placeholderId;
    private int placeholderSize;


    /**
     * Create a new instance of <code>OEPlaceholderAtom</code>
     */
    public OEPlaceholderAtom(){
        _header = new byte[8];
        LittleEndian.putUShort(_header, 0, 0);
        LittleEndian.putUShort(_header, 2, (int)getRecordType());
        LittleEndian.putInt(_header, 4, 8);

        placementId = 0;
        placeholderId = 0;
        placeholderSize = 0;
    }

    /**
     * Build an instance of <code>OEPlaceholderAtom</code> from on-disk data
     */
	protected OEPlaceholderAtom(byte[] source, int start, int len) {
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

        placementId = LittleEndian.getInt(source, start);
        placeholderId = LittleEndian.getUnsignedByte(source, start+4);
        placeholderSize = LittleEndian.getUnsignedByte(source, start+5);
	}

    /**
     * @return type of this record {@link RecordTypes#OEPlaceholderAtom}.
     */
	public long getRecordType() { return RecordTypes.OEPlaceholderAtom.typeID; }

    /**
     * Returns the placement Id.
     *
     * @return the placement Id.
     */
    public int getPlacementId(){
        return placementId;
    }

    /**
     * Sets the placement Id.
     *
     * @param id the placement Id.
     */
   public void setPlacementId(int id){
        placementId = id;
    }

    /**
     * Returns the placeholder Id.
     *
     * @return the placeholder Id.
     */
    public int getPlaceholderId(){
        return placeholderId;
    }

    /**
     * Sets the placeholder Id.
     *
     * @param id the placeholder Id.
     */
    public void setPlaceholderId(byte id){
        placeholderId = id;
    }

    /**
     * Returns the placeholder size.
     * Must be one of the PLACEHOLDER_* static constants defined in this class.
     *
     * @return the placeholder size.
     */
    public int getPlaceholderSize(){
        return placeholderSize;
    }

    /**
     * Sets the placeholder size.
     * Must be one of the PLACEHOLDER_* static constants defined in this class.
     *
     * @param size the placeholder size.
     */
     public void setPlaceholderSize(byte size){
        placeholderSize = size;
    }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		out.write(_header);

        byte[] recdata = new byte[8];
        LittleEndian.putInt(recdata, 0, placementId);
        recdata[4] = (byte)placeholderId;
        recdata[5] = (byte)placeholderSize;

        out.write(recdata);
	}
}
