package org.apache.poi.hwpf.model;

import java.io.IOException;

import org.apache.poi.hwpf.model.io.HWPFOutputStream;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

/**
 * Utils for storing and reading "STring TaBle stored in File"
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
class SttbfUtils
{
    public static String[] read( byte[] data, int startOffset )
    {
        short ffff = LittleEndian.getShort( data, startOffset );

        if ( ffff != (short) 0xffff )
        {
            // Non-extended character Pascal strings
            throw new UnsupportedOperationException(
                    "Non-extended character Pascal strings are not supported right now. "
                            + "Please, contact POI developers for update." );
        }

        // strings are extended character strings
        int offset = startOffset + 2;
        int numEntries = LittleEndian.getInt( data, offset );
        offset += 4;

        String[] entries = new String[numEntries];
        for ( int i = 0; i < numEntries; i++ )
        {
            int len = LittleEndian.getShort( data, offset );
            offset += 2;
            String value = StringUtil.getFromUnicodeLE( data, offset, len );
            offset += len * 2;
            entries[i] = value;
        }
        return entries;
    }

    public static int write( HWPFOutputStream tableStream, String[] entries )
            throws IOException
    {
        byte[] header = new byte[6];
        LittleEndian.putShort( header, 0, (short) 0xffff );

        if ( entries == null || entries.length == 0 )
        {
            LittleEndian.putInt( header, 2, 0 );
            tableStream.write( header );
            return 6;
        }

        LittleEndian.putInt( header, 2, entries.length );
        tableStream.write( header );
        int size = 6;

        for ( String entry : entries )
        {
            byte[] buf = new byte[entry.length() * 2 + 2];
            LittleEndian.putShort( buf, 0, (short) entry.length() );
            StringUtil.putUnicodeLE( entry, buf, 2 );
            tableStream.write( buf );
            size += buf.length;
        }

        return size;
    }

}
