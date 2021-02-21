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

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

import static java.lang.Integer.toHexString;
import static org.apache.logging.log4j.util.Unbox.box;

public class NilPICFAndBinData
{

    private static final Logger LOGGER = LogManager.getLogger(NilPICFAndBinData.class);

    private byte[] _binData;

    public NilPICFAndBinData( byte[] data, int offset )
    {
        fillFields( data, offset );
    }

    public void fillFields( byte[] data, int offset )
    {
        int lcb = LittleEndian.getInt( data, offset );
        int cbHeader = LittleEndian.getUShort( data, offset
                + LittleEndianConsts.INT_SIZE );

        if ( cbHeader != 0x44 )
        {
            LOGGER.atWarn().log("NilPICFAndBinData at offset {} cbHeader 0x{} != 0x44", box(offset), toHexString(cbHeader));
        }

        // skip the 62 ignored bytes
        int binaryLength = lcb - cbHeader;
        this._binData = Arrays.copyOfRange(data, offset + cbHeader,
                offset + cbHeader + binaryLength);
    }

    public byte[] getBinData()
    {
        return _binData;
    }

    public byte[] serialize()
    {
        byte[] bs = new byte[_binData.length + 0x44];
        LittleEndian.putInt( bs, 0, _binData.length + 0x44 );
        System.arraycopy( _binData, 0, bs, 0x44, _binData.length );
        return bs;
    }

    public int serialize( byte[] data, int offset )
    {
        LittleEndian.putInt( data, offset, _binData.length + 0x44 );
        System.arraycopy( _binData, 0, data, offset + 0x44, _binData.length );
        return 0x44 + _binData.length;
    }
}
