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
package org.apache.poi.hwpf.converter;

import org.apache.poi.hwpf.usermodel.Range;

import junit.framework.TestCase;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.hwpf.usermodel.Table;

/**
 * Test cases for {@link AbstractWordUtils}
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
public class AbstractWordUtilsTest extends TestCase
{
    /**
     * Test case for {@link AbstractWordUtils#buildTableCellEdgesArray(Table)}
     */
    public void testBuildTableCellEdgesArray()
    {
        HWPFDocument document = HWPFTestDataSamples
                .openSampleFile( "table-merges.doc" );
        final Range range = document.getRange();
        Table table = range.getTable( range.getParagraph( 0 ) );

        int[] result = AbstractWordUtils.buildTableCellEdgesArray( table );
        assertEquals( 6, result.length );

        assertEquals( 0000, result[0] );
        assertEquals( 1062, result[1] );
        assertEquals( 5738, result[2] );
        assertEquals( 6872, result[3] );
        assertEquals( 8148, result[4] );
        assertEquals( 9302, result[5] );
    }
}
