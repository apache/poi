
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests the log class.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Nicola Ken Barozzi (nicolaken at apache.org)
 */
public final class TestPOILogger extends POILogger {
    private String lastLog = "";
    private Throwable lastEx = null;
    
    /**
     * Test different types of log output.
     */
    @Test
    public void testVariousLogTypes() throws Exception {
        String oldLCN = POILogFactory._loggerClassName;
        try {
            POILogFactory._loggerClassName = TestPOILogger.class.getName();
            POILogger log = POILogFactory.getLogger( "foo" );
            assertTrue(log instanceof TestPOILogger);
            
            TestPOILogger tlog = (TestPOILogger)log;
    
            log.log(POILogger.WARN, "Test = ", 1);
            assertEquals("Test = 1", tlog.lastLog);
            
            log.logFormatted(POILogger.ERROR, "Test param 1 = %, param 2 = %d", "2", 3 );
            assertEquals("Test param 1 = 2, param 2 = 3", tlog.lastLog);
            
            log.logFormatted(POILogger.ERROR, "Test param 1 = %d, param 2 = %", new int[]{4, 5} );
            assertEquals("Test param 1 = 4, param 2 = 5", tlog.lastLog);
            
            log.logFormatted(POILogger.ERROR, "Test param 1 = %1.1, param 2 = %0.1", new double[]{4, 5.23} );
            assertEquals("Test param 1 = 4, param 2 = 5.2", tlog.lastLog);

            log.log(POILogger.ERROR, "Test ", 1,2,new Exception("bla"));
            assertEquals("Test 12", tlog.lastLog);
            assertNotNull(tlog.lastEx);
            
            log.log(POILogger.ERROR, "log\nforging", "\nevil","\nlog");
            assertEquals("log forging evil log", tlog.lastLog);
        } finally {
            POILogFactory._loggerClassName = oldLCN;
        }
    }

    public void initialize(String cat) {
    }

    public void log(int level, Object obj1) {
        lastLog = (obj1 == null) ? "" : obj1.toString();
        lastEx = null;
    }

    public void log(int level, Object obj1, Throwable exception) {
        lastLog = (obj1 == null) ? "" : obj1.toString();
        lastEx = exception;
    }

    public boolean check(int level) {
        return true;
    }
}
