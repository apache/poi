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
package org.apache.poi.hpsf;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.apache.poi.util.LittleEndianConsts;

class Filetime {
    private static final int SIZE = LittleEndian.INT_SIZE * 2;

    private int _dwHighDateTime;
    private int _dwLowDateTime;
    
    Filetime() {}

    Filetime( int low, int high ) {
        _dwLowDateTime = low;
        _dwHighDateTime = high;
    }


    void read( LittleEndianByteArrayInputStream lei ) {
        _dwLowDateTime = lei.readInt();
        _dwHighDateTime = lei.readInt();
    }

    long getHigh() {
        return _dwHighDateTime;
    }

    long getLow() {
        return _dwLowDateTime;
    }

    byte[] toByteArray() {
        byte[] result = new byte[SIZE];
        LittleEndian.putInt( result, 0 * LittleEndianConsts.INT_SIZE, _dwLowDateTime );
        LittleEndian
                .putInt( result, 1 * LittleEndianConsts.INT_SIZE, _dwHighDateTime );
        return result;
    }

    int write( OutputStream out ) throws IOException {
        LittleEndian.putInt( _dwLowDateTime, out );
        LittleEndian.putInt( _dwHighDateTime, out );
        return SIZE;
    }
}
