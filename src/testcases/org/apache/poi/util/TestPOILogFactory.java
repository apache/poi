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
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Nicola Ken Barozzi (nicolaken at apache.org)
 */
public final class TestPOILogFactory extends TestCase {


    /**
     * test log creation
     */
    public void testLog() {
        //NKB Testing only that logging classes use gives no exception
        //    Since logging can be disabled, no checking of logging
        //    output is done.

        POILogger l1 = POILogFactory.getLogger( "org.apache.poi.hssf.test" );
        POILogger l2 = POILogFactory.getLogger( "org.apache.poi.hdf.test" );

        l1.log( POILogger.FATAL, "testing cat org.apache.poi.hssf.*:FATAL" );
        l1.log( POILogger.ERROR, "testing cat org.apache.poi.hssf.*:ERROR" );
        l1.log( POILogger.WARN, "testing cat org.apache.poi.hssf.*:WARN" );
        l1.log( POILogger.INFO, "testing cat org.apache.poi.hssf.*:INFO" );
        l1.log( POILogger.DEBUG, "testing cat org.apache.poi.hssf.*:DEBUG" );

        l2.log( POILogger.FATAL, "testing cat org.apache.poi.hdf.*:FATAL" );
        l2.log( POILogger.ERROR, "testing cat org.apache.poi.hdf.*:ERROR" );
        l2.log( POILogger.WARN, "testing cat org.apache.poi.hdf.*:WARN" );
        l2.log( POILogger.INFO, "testing cat org.apache.poi.hdf.*:INFO" );
        l2.log( POILogger.DEBUG, "testing cat org.apache.poi.hdf.*:DEBUG" );

    }
}
