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

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.NullOutputStream;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestEscherDump {
    static NullPrinterStream nullPS;
    
    @BeforeClass
    public static void init() throws UnsupportedEncodingException {
        nullPS = new NullPrinterStream();
    }
    
    @Test
    public void testSimple() throws Exception {
        // simple test to at least cover some parts of the class
        EscherDump.main(new String[] {}, nullPS);
        
        new EscherDump().dump(0, new byte[] {}, nullPS);
        new EscherDump().dump(new byte[] {}, 0, 0, nullPS);
        new EscherDump().dumpOld(0, new ByteArrayInputStream(new byte[] {}), nullPS);
    }

    @Test
    public void testWithData() throws Exception {
        new EscherDump().dumpOld(8, new ByteArrayInputStream(new byte[] { 00, 00, 00, 00, 00, 00, 00, 00 }), nullPS);
    }

    @Test
    public  void testWithSamplefile() throws Exception {
        //InputStream stream = HSSFTestDataSamples.openSampleFileStream(")
        byte[] data = POIDataSamples.getDDFInstance().readFile("Container.dat");
        new EscherDump().dump(data.length, data, nullPS);
        //new EscherDump().dumpOld(data.length, new ByteArrayInputStream(data), System.out);
        
        data = new byte[2586114];
        InputStream stream = HSSFTestDataSamples.openSampleFileStream("44593.xls");
        try {
            int bytes = IOUtils.readFully(stream, data);
            assertTrue(bytes != -1);
            //new EscherDump().dump(bytes, data, System.out);
            //new EscherDump().dumpOld(bytes, new ByteArrayInputStream(data), System.out);
        } finally {
            stream.close();
        }
    }
    
    /**
     * Implementation of an OutputStream which does nothing, used
     * to redirect stdout to avoid spamming the console with output
     */
    private static class NullPrinterStream extends PrintStream {
        @SuppressWarnings("resource")
        private NullPrinterStream() throws UnsupportedEncodingException {
            super(new NullOutputStream(),true,LocaleUtil.CHARSET_1252.name());
        }      
    }    
}
