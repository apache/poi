
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
        

package org.apache.poi.hssf.record;

/**
 * Subrecords are part of the OBJ class.
 */
abstract public class SubRecord
        extends Record
{
    public SubRecord()
    {
    }

    public SubRecord( short id, short size, byte[] data )
    {
        super( id, size, data );
    }

    public SubRecord( short id, short size, byte[] data, int offset )
    {
        super( id, size, data, offset );
    }

    public static Record createSubRecord( short subRecordSid, short size, byte[] data, int offset )
    {
        Record r = null;

        short adjustedSize = size;
        if ( size < 0 )
        {
            adjustedSize = 0;
        }
        else if ( offset + size > data.length )
        {
            adjustedSize = (short) ( data.length - offset );
            if ( adjustedSize > 4 )
            {
                adjustedSize -= 4;
            }
        }

        switch ( subRecordSid )
        {
            case CommonObjectDataSubRecord.sid:
                r = new CommonObjectDataSubRecord( subRecordSid, adjustedSize, data, offset );
                break;
            case GroupMarkerSubRecord.sid:
                r = new GroupMarkerSubRecord( subRecordSid, adjustedSize, data, offset );
                break;
            case EndSubRecord.sid:
                r = new EndSubRecord( subRecordSid, adjustedSize, data, offset );
                break;
            default:
                r = new UnknownRecord( subRecordSid, adjustedSize, data, offset );
        }

        return r;
    }
}
