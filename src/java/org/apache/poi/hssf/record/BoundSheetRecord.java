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

package org.apache.poi.hssf.record;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

/**
 * Title:        Bound Sheet Record (aka BundleSheet) <P>
 * Description:  Defines a sheet within a workbook.  Basically stores the sheetname
 *               and tells where the Beginning of file record is within the HSSF
 *               file. <P>
 * REFERENCE:  PG 291 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Sergei Kozello (sergeikozello at mail.ru)
 * @version 2.0-pre
 */
public final class BoundSheetRecord extends Record {
    public final static short sid = 0x0085;

	private static final BitField hiddenFlag = BitFieldFactory.getInstance(0x01);
	private static final BitField veryHiddenFlag = BitFieldFactory.getInstance(0x02);
    private int field_1_position_of_BOF;
    private short field_2_option_flags;
    private byte field_3_sheetname_length;
    private byte field_4_compressed_unicode_flag;   // not documented
    private String field_5_sheetname;

    public BoundSheetRecord()
    {
    }

    /**
     * Constructs a BoundSheetRecord and sets its fields appropriately
     *
     * @param in the RecordInputstream to read the record from
     */

    public BoundSheetRecord( RecordInputStream in )
    {
        super( in );
    }

    protected void validateSid( short id )
    {
        if ( id != sid )
        {
            throw new RecordFormatException( "NOT A Bound Sheet RECORD" );
        }
    }

    /**
     *  UTF8:
     *	sid + len + bof + flags + len(str) + unicode +   str
     *	 2  +  2  +  4  +   2   +    1     +    1    + len(str)
     *
     * 	UNICODE:
     *	sid + len + bof + flags + len(str) + unicode +   str
     *	 2  +  2  +  4  +   2   +    1     +    1    + 2 * len(str)
     *
     */

    protected void fillFields( RecordInputStream in )
    {
        field_1_position_of_BOF = in.readInt();	// bof
        field_2_option_flags = in.readShort();	// flags
        field_3_sheetname_length = in.readByte();						// len(str)
        field_4_compressed_unicode_flag = in.readByte();						// unicode

        int nameLength = LittleEndian.ubyteToInt( field_3_sheetname_length );
        if ( ( field_4_compressed_unicode_flag & 0x01 ) == 1 )
        {
            field_5_sheetname = in.readUnicodeLEString(nameLength);
        }
        else
        {
            field_5_sheetname = in.readCompressedUnicode(nameLength);
        }
    }

    /**
     * set the offset in bytes of the Beginning of File Marker within the HSSF Stream part of the POIFS file
     *
     * @param pos  offset in bytes
     */

    public void setPositionOfBof( int pos )
    {
        field_1_position_of_BOF = pos;
    }

    /**
     * set the option flags (unimportant for HSSF supported sheets)
     *
     * @param flags to set
     */

    public void setOptionFlags( short flags )
    {
        field_2_option_flags = flags;
    }

    /**
     * Set the length of the sheetname in characters
     *
     * @param len  number of characters in the sheet name
     * @see #setSheetname(String)
     */

    public void setSheetnameLength( byte len )
    {
        field_3_sheetname_length = len;
    }

    /**
     * set whether or not to interperate the Sheetname as compressed unicode (8/16 bit)
     * (This is undocumented but can be found as Q187919 on the Microsoft(tm) Support site)
     * @param flag (0/1) 0- compressed, 1 - uncompressed (16-bit)
     */

    public void setCompressedUnicodeFlag( byte flag )
    {
        field_4_compressed_unicode_flag = flag;
    }

    /**
     * Set the sheetname for this sheet.  (this appears in the tabs at the bottom)
     * @param sheetname the name of the sheet
     * @throws IllegalArgumentException if sheet name will cause excel to crash. 
     */

    public void setSheetname( String sheetname )
    {
        
        if ((sheetname == null) || (sheetname.length()==0)
                || (sheetname.length()>31)
                || (sheetname.indexOf("/") > -1)
                || (sheetname.indexOf("\\") > -1)
                || (sheetname.indexOf("?") > -1)
                || (sheetname.indexOf("*") > -1)
                || (sheetname.indexOf("]") > -1)
                || (sheetname.indexOf("[") > -1) ){
                    throw new IllegalArgumentException("Sheet name cannot be blank, greater than 31 chars, or contain any of /\\*?[]");
        }
        field_5_sheetname = sheetname;
        setCompressedUnicodeFlag(StringUtil.hasMultibyte(sheetname) ?  (byte)1 : (byte)0);        
    }

    /**
     * get the offset in bytes of the Beginning of File Marker within the HSSF Stream part of the POIFS file
     *
     * @return offset in bytes
     */

    public int getPositionOfBof()
    {
        return field_1_position_of_BOF;
    }

    /**
     * get the option flags (unimportant for HSSF supported sheets)
     *
     * @return flags to set
     */

    public short getOptionFlags()
    {
        return field_2_option_flags;
    }

    /**
     * get the length of the sheetname in characters
     *
     * @return number of characters in the sheet name
     * @see #getSheetname()
     */

    public byte getSheetnameLength()
    {
        return field_3_sheetname_length;
    }

    /**
     * get the length of the raw sheetname in characters
     * the length depends on the unicode flag
     *
     * @return number of characters in the raw sheet name
     */

    public byte getRawSheetnameLength()
    {
        return (byte) ( ( ( field_4_compressed_unicode_flag & 0x01 ) == 1 )
                ? 2 * field_3_sheetname_length
                : field_3_sheetname_length );
    }

    /**
     * get whether or not to interperate the Sheetname as compressed unicode (8/16 bit)
     * (This is undocumented but can be found as Q187919 on the Microsoft(tm) Support site)
     * @return flag (0/1) 0- compressed, 1 - uncompressed (16-bit)
     */

    public byte getCompressedUnicodeFlag()
    {
        return field_4_compressed_unicode_flag;
    }

    /**
     * get the sheetname for this sheet.  (this appears in the tabs at the bottom)
     * @return sheetname the name of the sheet
     */

    public String getSheetname()
    {
        return field_5_sheetname;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append( "[BOUNDSHEET]\n" );
        buffer.append( "    .bof             = " )
                .append( Integer.toHexString( getPositionOfBof() ) ).append( "\n" );
        buffer.append( "    .optionflags     = " )
                .append( Integer.toHexString( getOptionFlags() ) ).append( "\n" );
        buffer.append( "    .sheetname length= " )
                .append( Integer.toHexString( getSheetnameLength() ) ).append( "\n" );
        buffer.append( "    .unicodeflag     = " )
                .append( Integer.toHexString( getCompressedUnicodeFlag() ) )
                .append( "\n" );
        buffer.append( "    .sheetname       = " ).append( getSheetname() )
                .append( "\n" );
        buffer.append( "[/BOUNDSHEET]\n" );
        return buffer.toString();
    }

    public int serialize( int offset, byte[] data )
    {
        LittleEndian.putShort( data, 0 + offset, sid );
        LittleEndian.putShort( data, 2 + offset, (short) ( 8 + getRawSheetnameLength() ) );
        LittleEndian.putInt( data, 4 + offset, getPositionOfBof() );
        LittleEndian.putShort( data, 8 + offset, getOptionFlags() );
        data[10 + offset] = (byte) ( getSheetnameLength() );
        data[11 + offset] = getCompressedUnicodeFlag();

        if ( ( field_4_compressed_unicode_flag & 0x01 ) == 1 )
            StringUtil.putUnicodeLE( getSheetname(), data, 12 + offset );
        else
            StringUtil.putCompressedUnicode( getSheetname(), data, 12 + offset );


        return getRecordSize();

        /*
		byte[] fake = new byte[] {	(byte)0x85, 0x00, 			// sid
		    							0x1a, 0x00, 			// length
		    							0x3C, 0x09, 0x00, 0x00, // bof
		    							0x00, 0x00, 			// flags
		    							0x09, 					// len( str )
		    							0x01, 					// unicode
		    							// <str>
		    							0x21, 0x04, 0x42, 0x04, 0x40, 0x04, 0x30, 0x04, 0x3D,
		    							0x04, 0x38, 0x04, 0x47, 0x04, 0x3A, 0x04, 0x30, 0x04
		    							// </str>
		    						};

		    						sid + len + bof + flags + len(str) + unicode +   str
		    						 2  +  2  +  4  +   2   +    1     +    1    + len(str)

		System.arraycopy( fake, 0, data, offset, fake.length );

		return fake.length;
		*/
    }

    public int getRecordSize()
    {
        // Includes sid length + size length
        return 12 + getRawSheetnameLength();
    }

    public short getSid()
    {
        return sid;
    }

    /**
     * Is the sheet hidden? Different from very hidden 
     */
    public boolean isHidden() {
	    return hiddenFlag.isSet(field_2_option_flags);
    }

    /**
     * Is the sheet hidden? Different from very hidden 
     */
    public void setHidden(boolean hidden) {
	    field_2_option_flags = hiddenFlag.setShortBoolean(field_2_option_flags, hidden);
    }

    /**
     * Is the sheet very hidden? Different from (normal) hidden 
     */
    public boolean isVeryHidden() {
	    return veryHiddenFlag.isSet(field_2_option_flags);
    }

    /**
     * Is the sheet very hidden? Different from (normal) hidden 
     */
    public void setVeryHidden(boolean veryHidden) {
	    field_2_option_flags = veryHiddenFlag.setShortBoolean(field_2_option_flags, veryHidden);
    }
    
    /**
     * Takes a list of BoundSheetRecords, and returns the all
     *  ordered by the position of their BOFs.
     */
    public static BoundSheetRecord[] orderByBofPosition(List boundSheetRecords) {
    	BoundSheetRecord[] bsrs = (BoundSheetRecord[])boundSheetRecords.toArray(
    			new BoundSheetRecord[boundSheetRecords.size()]);
    	
    	// Sort
    	Arrays.sort(bsrs, new BOFComparator());
    	
    	// All done
    	return bsrs;
    }
    private static class BOFComparator implements Comparator {
		public int compare(Object bsr1, Object bsr2) {
			return compare((BoundSheetRecord)bsr1, (BoundSheetRecord)bsr2);
		}
		public int compare(BoundSheetRecord bsr1, BoundSheetRecord bsr2) {
			if(bsr1.field_1_position_of_BOF < bsr2.field_1_position_of_BOF)
				return -1;
			if(bsr1.field_1_position_of_BOF == bsr2.field_1_position_of_BOF)
				return 0;
			return 1;
		}
    }
}
