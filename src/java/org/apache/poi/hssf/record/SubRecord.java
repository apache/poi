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

/**
 * Subrecords are part of the OBJ class.
 */
abstract public class SubRecord extends Record {
    protected SubRecord() {
    }

    public static Record createSubRecord(RecordInputStream in)
    {
        Record r = null;

        /* This must surely be an earlier hack?? Delete when confident
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
*/
        switch ( in.getSid() )
        {
            case CommonObjectDataSubRecord.sid:
                r = new CommonObjectDataSubRecord( in );
                break;
            case EmbeddedObjectRefSubRecord.sid:
                r = new EmbeddedObjectRefSubRecord( in );
                break;
            case GroupMarkerSubRecord.sid:
                r = new GroupMarkerSubRecord( in );
                break;
            case EndSubRecord.sid:
                r = new EndSubRecord( in );
                break;
            case NoteStructureSubRecord.sid:
                r = new NoteStructureSubRecord( in );
                break;
            default:
                r = new UnknownRecord( in );
        }
        return r;
    }
}
