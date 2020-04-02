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
package org.apache.poi.hwpf.model;

import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

@Internal
public class Xstz
{
    private static final POILogger log = POILogFactory.getLogger( Xstz.class );

    private final short _chTerm = 0;
    private Xst _xst;

    public Xstz()
    {
        _xst = new Xst();
    }

    public Xstz( byte[] data, int startOffset )
    {
        fillFields( data, startOffset );
    }

    public void fillFields( byte[] data, int startOffset )
    {
        int offset = startOffset;

        _xst = new Xst( data, offset );
        offset += _xst.getSize();

        short term = LittleEndian.getShort( data, offset );
        if ( term != 0 )
        {
            if (log.check(POILogger.WARN)) {
                log.log(POILogger.WARN, "chTerm at the end of Xstz at offset ",
                        offset, " is not 0");
            }
        }
    }

    public String getAsJavaString()
    {
        return _xst.getAsJavaString();
    }

    public int getSize()
    {
        return _xst.getSize() + LittleEndianConsts.SHORT_SIZE;
    }

    public int serialize( byte[] data, int startOffset )
    {
        int offset = startOffset;

        _xst.serialize( data, offset );
        offset += _xst.getSize();

        LittleEndian.putUShort( data, offset, _chTerm );
        offset += LittleEndianConsts.SHORT_SIZE;

        return offset - startOffset;
    }

    @Override
    public String toString()
    {
        return "[Xstz]" + _xst.getAsJavaString() + "[/Xstz]";
    }
}
