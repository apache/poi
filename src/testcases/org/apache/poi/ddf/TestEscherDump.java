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

import java.io.ByteArrayInputStream;

import org.junit.Test;

public class TestEscherDump {
    @Test
    public void testSimple() throws Exception {
        // simple test to at least cover some parts of the class
        EscherDump.main(new String[] {});
        
        new EscherDump().dump(0, new byte[] {}, System.out);
        new EscherDump().dump(new byte[] {}, 0, 0, System.out);
        new EscherDump().dumpOld(0, new ByteArrayInputStream(new byte[] {}), System.out);
    }

    @Test
    public void testWithData() throws Exception {
        new EscherDump().dumpOld(8, new ByteArrayInputStream(new byte[] { 00, 00, 00, 00, 00, 00, 00, 00 }), System.out);
    }
}
