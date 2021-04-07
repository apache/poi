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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.util.Internal;

/**
 * Utils class for storing and reading "STring TaBle stored in File"
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
@Internal
class SttbUtils
{

    private static final int CDATA_SIZE_STTB_SAVED_BY = 2; // bytes

    private static final int CDATA_SIZE_STTBF_BKMK = 2; // bytes

    private static final int CDATA_SIZE_STTBF_R_MARK = 2; // bytes

    static String[] readSttbfBkmk( byte[] buffer, int startOffset )
    {
        return new Sttb( CDATA_SIZE_STTBF_BKMK, buffer, startOffset ).getData();
    }

    static String[] readSttbfRMark( byte[] buffer, int startOffset )
    {
        return new Sttb( CDATA_SIZE_STTBF_R_MARK, buffer, startOffset )
                .getData();
    }

    static String[] readSttbSavedBy( byte[] buffer, int startOffset )
    {
        return new Sttb( CDATA_SIZE_STTB_SAVED_BY, buffer, startOffset )
                .getData();
    }

    static void writeSttbfBkmk( String[] data, OutputStream tableStream )
            throws IOException
    {
        tableStream.write( new Sttb( CDATA_SIZE_STTBF_BKMK, data ).serialize() );
    }

    static void writeSttbfRMark( String[] data, OutputStream tableStream )
            throws IOException
    {
        tableStream.write( new Sttb( CDATA_SIZE_STTBF_R_MARK, data ).serialize() );
    }

    static void writeSttbSavedBy( String[] data, OutputStream tableStream )
            throws IOException
    {
        tableStream.write( new Sttb( CDATA_SIZE_STTB_SAVED_BY, data ).serialize() );
    }

}
