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
 *  An atom record that specifies whether a shape is a placeholder shape.
 * </p>
 *
 * @author Yegor Kozlov
 */

public final class OEPlaceholderAtom extends RecordAtom{

    /**
     * The full size of the master body text placeholder shape.
     */
    public static final int PLACEHOLDER_FULLSIZE = 0;

    /**
     * Half of the size of the master body text placeholder shape.
     */
    public static final int PLACEHOLDER_HALFSIZE = 1;

    /**
     * A quarter of the size of the master body text placeholder shape.
     */
    public static final int PLACEHOLDER_QUARTSIZE = 2;

    /**
     * MUST NOT be used for this field.
     */
    public static final byte None = 0;

    /**
     * The corresponding shape contains the master title text.
     * The corresponding slide MUST be a main master slide.
     */
    public static final byte MasterTitle = 1;

    /**
     * The corresponding shape contains the master body text.
     * The corresponding slide MUST be a main master slide.
     */
    public static final byte MasterBody = 2;

    /**
     * The corresponding shape contains the master center title text.
     * The corresponding slide MUST be a title master slide.
     */
    public static final byte MasterCenteredTitle = 3;

    /**
     * The corresponding shape contains the master sub-title text.
     * The corresponding slide MUST be a title master slide.
     */
    public static final byte MasterSubTitle = 4;

    /**
     * The corresponding shape contains the shared properties for slide image shapes.
     * The corresponding slide MUST be a notes master slide.
     */
    public static final byte MasterNotesSlideImage = 5;

    /**
     * The corresponding shape contains the master body text.
     * The corresponding slide MUST be a notes master slide.
     */
    public static final byte MasterNotesBody = 6;

    /**
     * The corresponding shape contains the date text field.
     * The corresponding slide MUST be a main master slide, title master slide, notes master slide, or handout master slide.
     */
    public static final byte MasterDate = 7;

    /**
     * The corresponding shape contains a slide number text field.
     * The corresponding slide MUST be a main master slide, title master slide, notes master slide, or handout master slide.
     */
    public static final byte MasterSlideNumber = 8;

    /**
     * The corresponding shape contains a footer text field.
     * The corresponding slide MUST be a main master slide, title master slide, notes master slide, or handout master slide.
     */
    public static final byte MasterFooter = 9;

    /**
     * The corresponding shape contains a header text field.
     * The corresponding slide must be a notes master slide or handout master slide.
     */
    public static final byte MasterHeader = 10;

    /**
     * The corresponding shape contains a presentation slide image.
     * The corresponding slide MUST be a notes slide.
     */
    public static final byte NotesSlideImage = 11;

    /**
     * The corresponding shape contains the notes text.
     * The corresponding slide MUST be a notes slide.
     */
    public static final byte NotesBody = 12;

    /**
     *  The corresponding shape contains the title text.
     *  The corresponding slide MUST be a presentation slide.
     */
    public static final byte Title = 13;

    /**
     * The corresponding shape contains the body text.
     * The corresponding slide MUST be a presentation slide.
     */
    public static final byte Body = 14;

    /**
     * The corresponding shape contains the title text.
     * The corresponding slide MUST be a presentation slide.
     */
    public static final byte CenteredTitle = 15;

    /**
     * The corresponding shape contains the sub-title text.
     * The corresponding slide MUST be a presentation slide.
     */
    public static final byte Subtitle = 16;

    /**
     * The corresponding shape contains the title text with vertical text flow.
     * The corresponding slide MUST be a presentation slide.
     */
    public static final byte VerticalTextTitle = 17;

    /**
     * The corresponding shape contains the body text with vertical text flow.
     * The corresponding slide MUST be a presentation slide.
     */
    public static final byte VerticalTextBody = 18;

    /**
     *  The corresponding shape contains a generic object.
     *  The corresponding slide MUST be a presentation slide.
     */
    public static final byte Object = 19;

    /**
     * The corresponding shape contains a chart object.
     * The corresponding slide MUST be a presentation slide.
     */
    public static final byte Graph = 20;

    /**
     * The corresponding shape contains a table object.
     * The corresponding slide MUST be a presentation slide.
     */
    public static final byte Table = 21;

    /**
     * The corresponding shape contains a clipart object.
     * The corresponding slide MUST be a presentation slide.
     */
    public static final byte ClipArt = 22;

    /**
     * The corresponding shape contains an organization chart object.
     * The corresponding slide MUST be a presentation slide.
     */
    public static final byte OrganizationChart = 23;

    /**
     * The corresponding shape contains a media object.
     * The corresponding slide MUST be a presentation slide.
     */
    public static final byte MediaClip = 24;

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
        int offset = start;
        System.arraycopy(source,start,_header,0,8);
        offset += _header.length;

        placementId = LittleEndian.getInt(source, offset); offset += 4;
        placeholderId = LittleEndian.getUnsignedByte(source, offset); offset++;
        placeholderSize = LittleEndian.getUnsignedByte(source, offset); offset++;
	}

    /**
     * @return type of this record {@link RecordTypes#OEPlaceholderAtom}.
     */
	public long getRecordType() { return RecordTypes.OEPlaceholderAtom.typeID; }

    /**
     * Returns the placement Id.
     * <p>
     * The placement Id is a number assigned to the placeholder. It goes from -1 to the number of placeholders.
     * It SHOULD be unique among all PlacholderAtom records contained in the corresponding slide.
     * The value 0xFFFFFFFF specifies that the corresponding shape is not a placeholder shape.
     * </p>
     *
     * @return the placement Id.
     */
    public int getPlacementId(){
        return placementId;
    }

    /**
     * Sets the placement Id.
     * <p>
     * The placement Id is a number assigned to the placeholder. It goes from -1 to the number of placeholders.
     * It SHOULD be unique among all PlacholderAtom records contained in the corresponding slide.
     * The value 0xFFFFFFFF specifies that the corresponding shape is not a placeholder shape.
     * </p>
     *
     * @param id the placement Id.
     */
   public void setPlacementId(int id){
        placementId = id;
    }

    /**
     * Returns the placeholder Id.
     *
     * <p>
     * placeholder Id specifies the type of the placeholder shape.
     * The value MUST be one of the static constants defined in this class
     * </p>
     *
     * @return the placeholder Id.
     */
    public int getPlaceholderId(){
        return placeholderId;
    }

    /**
     * Sets the placeholder Id.
     *
     * <p>
     * placeholder Id specifies the type of the placeholder shape.
     * The value MUST be one of the static constants defined in this class
     * </p>
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
