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

package org.apache.poi.ddf;

import junit.framework.TestCase;

import java.util.List;

import org.apache.poi.util.HexRead;
import org.apache.poi.util.HexDump;

/**
 * @author Glen Stampoultzis  (glens @ superlinksoftware.com)
 */
public class TestEscherPropertyFactory extends TestCase
{
    public void testCreateProperties() {
        String dataStr = "41 C1 " +     // propid, complex ind
                "03 00 00 00 " +         // size of complex property
                "01 00 " +              // propid, complex ind
                "00 00 00 00 " +         // value
                "41 C1 " +              // propid, complex ind
                "03 00 00 00 " +         // size of complex property
                "01 02 03 " +
                "01 02 03 "
                ;
        byte[] data = HexRead.readFromString( dataStr );
        EscherPropertyFactory f = new EscherPropertyFactory();
        List props = f.createProperties( data, 0, (short)3 );
        EscherComplexProperty p1 = (EscherComplexProperty) props.get( 0 );
        assertEquals( (short)0xC141, p1.getId() );
        assertEquals( "[01, 02, 03]", HexDump.toHex( p1.getComplexData() ) );

        EscherComplexProperty p3 = (EscherComplexProperty) props.get( 2 );
        assertEquals( (short)0xC141, p3.getId() );
        assertEquals( "[01, 02, 03]", HexDump.toHex( p3.getComplexData() ) );
    }
}

