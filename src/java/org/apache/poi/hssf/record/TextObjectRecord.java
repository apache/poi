/* ====================================================================
   Copyright 2003-2004   Apache Software Foundation

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

package org.apache.poi.hssf.record;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.util.StringUtil;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.HexDump;
import java.io.UnsupportedEncodingException;

public class TextObjectRecord
        extends TextObjectBaseRecord
{
    HSSFRichTextString str = new HSSFRichTextString( "" );
    int continueRecordCount = 0;    // how many times has continue record been called?

    public TextObjectRecord()
    {
    }

    public TextObjectRecord( short id, short size, byte[] data )
    {
        super( id, size, data );
    }

    public TextObjectRecord( short id, short size, byte[] data, int offset )
    {
        super( id, size, data, offset );
    }

    public int getRecordSize()
    {
        int continue1Size = 0;
        int continue2Size = 0;
        if (str.length() != 0)
        {
            continue1Size = str.length() * 2 + 1 + 4;
            continue2Size = (str.numFormattingRuns() + 1) * 8 + 4;
        }
        return super.getRecordSize() + continue1Size + continue2Size;
    }

    public int serialize( int offset, byte[] data )
    {
        // Temporarily blank out str so that record size is calculated without the continue records.
        HSSFRichTextString temp = str;
        str = new HSSFRichTextString("");
        int bytesWritten1 = super.serialize( offset, data );
        str = temp;

        int pos = offset + bytesWritten1;
        if ( str.toString().equals( "" ) == false )
        {
            ContinueRecord c1 = createContinue1();
            ContinueRecord c2 = createContinue2();
            int bytesWritten2 = c1.serialize( pos, data );
            pos += bytesWritten2;
            int bytesWritten3 = c2.serialize( pos, data );
            pos += bytesWritten3;

            int size = bytesWritten1 + bytesWritten2 + bytesWritten3;
            if ( size != getRecordSize() )
                throw new RecordFormatException(size + " bytes written but getRecordSize() reports " + getRecordSize());
            return size;
        }
        if ( bytesWritten1 != getRecordSize() )
            throw new RecordFormatException(bytesWritten1 + " bytes written but getRecordSize() reports " + getRecordSize());
        return bytesWritten1;
    }

    private ContinueRecord createContinue1()
    {
        ContinueRecord c1 = new ContinueRecord();
        byte[] c1Data = new byte[str.length() * 2 + 1];
        try
        {
            c1Data[0] = 1;
            System.arraycopy( str.toString().getBytes( "UTF-16LE" ), 0, c1Data, 1, str.length() * 2 );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new RuntimeException( e.getMessage() );
        }
        c1.setData( c1Data );
        return c1;
    }

    private ContinueRecord createContinue2()
    {
        ContinueRecord c2 = new ContinueRecord();
        byte[] c2Data = new byte[str.numFormattingRuns() * 8 + 8];
        int pos = 0;
        for ( int i = 0; i < str.numFormattingRuns(); i++ )
        {
            LittleEndian.putShort( c2Data, pos, (short) str.getIndexOfFormattingRun( i ) );
            pos += 2;
            LittleEndian.putShort( c2Data, pos, str.getFontOfFormattingRun( i ) == str.NO_FONT ? 0 : str.getFontOfFormattingRun( i ) );
            pos += 2;
            pos += 4;  // skip reserved
        }
        LittleEndian.putShort( c2Data, pos, (short) str.length() );
        pos += 2;
        LittleEndian.putShort( c2Data, pos, (short) 0 );
        pos += 2;
        pos += 4;  // skip reserved

        c2.setData( c2Data );

        return c2;
    }

    public void processContinueRecord( byte[] data )
    {
        if ( continueRecordCount == 0 )
            processRawString( data );
        else
            processFontRuns( data );
        continueRecordCount++;
    }

    private void processFontRuns( byte[] data )
    {
        int pos = 0;
        do
        {
            short index = LittleEndian.getShort( data, pos );
            pos += 2;
            short iFont = LittleEndian.getShort( data, pos );
            pos += 2;
            pos += 4;  // skip reserved.

            if ( index >= str.length() )
                break;
            str.applyFont( index, str.length(), iFont );
        }
        while ( true );
    }

    private void processRawString( byte[] data )
    {
        String s;
        int pos = 0;
        byte compressByte = data[pos++];
        boolean isCompressed = compressByte == 0;
        try
        {
            if ( isCompressed )
            {
                s = new String( data, pos, getTextLength(), StringUtil.getPreferredEncoding() );
            }
            else
            {
                s = new String( data, pos, getTextLength() * 2, "UTF-16LE" );
            }
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new RuntimeException( e.getMessage() );
        }
        str = new HSSFRichTextString( s );
    }

    public HSSFRichTextString getStr()
    {
        return str;
    }

    public void setStr( HSSFRichTextString str )
    {
        this.str = str;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append( "[TXO]\n" );
        buffer.append( "    .options              = " )
                .append( "0x" ).append( HexDump.toHex( getOptions() ) )
                .append( " (" ).append( getOptions() ).append( " )" );
        buffer.append( System.getProperty( "line.separator" ) );
        buffer.append( "         .reserved1                = " ).append( isReserved1() ).append( '\n' );
        buffer.append( "         .HorizontalTextAlignment     = " ).append( getHorizontalTextAlignment() ).append( '\n' );
        buffer.append( "         .VerticalTextAlignment     = " ).append( getVerticalTextAlignment() ).append( '\n' );
        buffer.append( "         .reserved2                = " ).append( getReserved2() ).append( '\n' );
        buffer.append( "         .textLocked               = " ).append( isTextLocked() ).append( '\n' );
        buffer.append( "         .reserved3                = " ).append( getReserved3() ).append( '\n' );
        buffer.append( "    .textOrientation      = " )
                .append( "0x" ).append( HexDump.toHex( getTextOrientation() ) )
                .append( " (" ).append( getTextOrientation() ).append( " )" );
        buffer.append( System.getProperty( "line.separator" ) );
        buffer.append( "    .reserved4            = " )
                .append( "0x" ).append( HexDump.toHex( getReserved4() ) )
                .append( " (" ).append( getReserved4() ).append( " )" );
        buffer.append( System.getProperty( "line.separator" ) );
        buffer.append( "    .reserved5            = " )
                .append( "0x" ).append( HexDump.toHex( getReserved5() ) )
                .append( " (" ).append( getReserved5() ).append( " )" );
        buffer.append( System.getProperty( "line.separator" ) );
        buffer.append( "    .reserved6            = " )
                .append( "0x" ).append( HexDump.toHex( getReserved6() ) )
                .append( " (" ).append( getReserved6() ).append( " )" );
        buffer.append( System.getProperty( "line.separator" ) );
        buffer.append( "    .textLength           = " )
                .append( "0x" ).append( HexDump.toHex( getTextLength() ) )
                .append( " (" ).append( getTextLength() ).append( " )" );
        buffer.append( System.getProperty( "line.separator" ) );
        buffer.append( "    .reserved7            = " )
                .append( "0x" ).append( HexDump.toHex( getReserved7() ) )
                .append( " (" ).append( getReserved7() ).append( " )" );
        buffer.append( System.getProperty( "line.separator" ) );

        buffer.append( "    .string = " ).append(str).append('\n');

        buffer.append( "[/TXO]\n" );
        return buffer.toString();
    }
}
