
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

package org.apache.poi.util;

import junit.framework.TestCase;

/**
 * Tests the log class.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Nicola Ken Barozzi (nicolaken at apache.org)
 */
public final class TestPOILogger extends TestCase {

    /**
     * Test different types of log output.
     */
    public void testVariousLogTypes() {
        //NKB Testing only that logging classes use gives no exception
        //    Since logging can be disabled, no checking of logging
        //    output is done.

        POILogger log = POILogFactory.getLogger( "foo" );

        log.log( POILogger.WARN, "Test = ", Integer.valueOf( 1 ) );
        log.logFormatted( POILogger.ERROR, "Test param 1 = %, param 2 = %", "2", Integer.valueOf( 3 ) );
        log.logFormatted( POILogger.ERROR, "Test param 1 = %, param 2 = %", new int[]{4, 5} );
        log.logFormatted( POILogger.ERROR,
                "Test param 1 = %1.1, param 2 = %0.1", new double[]{4, 5.23} );

    }
}
