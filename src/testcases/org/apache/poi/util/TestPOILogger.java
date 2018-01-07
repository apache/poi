
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
 */
public final class TestPOILogger extends POILogger {
    private String lastLog = "";
    private Throwable lastEx;
    
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
            
            TestPOILogger tLog = (TestPOILogger)log;
    
            log.log(POILogger.WARN, "Test = ", 1);
            assertEquals("Test = 1", tLog.lastLog);
            
            log.log(POILogger.ERROR, "Test ", 1,2,new Exception("bla"));
            assertEquals("Test 12", tLog.lastLog);
            assertNotNull(tLog.lastEx);
            
            log.log(POILogger.ERROR, "log\nforging", "\nevil","\nlog");
            assertEquals("log forging evil log", tLog.lastLog);
        } finally {
            POILogFactory._loggerClassName = oldLCN;
        }
    }
    
    // ---------- POI Logger methods implemented for testing ----------

    @Override
    public void initialize(String cat) {
    }

    @Override
    protected void _log(int level, Object obj1) {
        lastLog = (obj1 == null) ? "" : obj1.toString();
        lastEx = null;
    }

    @Override
    protected void _log(int level, Object obj1, Throwable exception) {
        lastLog = (obj1 == null) ? "" : obj1.toString();
        lastEx = exception;
    }

    @Override
    public boolean check(int level) {
        return true;
    }
}
