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

package org.apache.poi.hslf.usermodel;

import org.apache.poi.hslf.*;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.hslf.blip.*;
import org.apache.poi.hslf.model.*;
import junit.framework.TestCase;

import java.io.*;
import java.util.Arrays;

/**
 * Test reading sound data from a ppt
 *
 * @author Yegor Kozlov
 */
public class TestSoundData extends TestCase{

    protected File cwd;

    public void setUp() throws Exception {
        cwd = new File(System.getProperty("HSLF.testdata.path"));
    }

    /**
     * Read a reference sound file from disk and compare it from the data extracted from the slide show
     */ 
    public void testSounds() throws Exception {
        //read the reference sound file
        File f = new File(cwd, "ringin.wav");
        int length = (int)f.length();
        byte[] ref_data = new byte[length];
        FileInputStream is = new FileInputStream(f);
        is.read(ref_data);
        is.close();

        is = new FileInputStream(new File(cwd, "sound.ppt"));
        SlideShow ppt = new SlideShow(is);
        is.close();

        SoundData[] sound = ppt.getSoundData();
        assertEquals("Expected 1 sound", 1, sound.length);

        assertTrue(Arrays.equals(ref_data, sound[0].getData()));
    }
}
