/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xslf.usermodel;

import java.io.File;
import java.lang.reflect.Field;

import org.apache.poi.POIDataSamples;
import org.apache.poi.xslf.util.PPTX2PNG;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Date: 10/26/11
 *
 * @author Yegor Kozlov
 */
public class TestPPTX2PNG {
    private static boolean jaxpDebugEnable = false;
    
    @BeforeClass
    public static void activateJaxpDebug() {
        jaxpDebugEnable = setDebugFld(true);
    }

    @AfterClass
    public static void resetJaxpDebug() {
        setDebugFld(jaxpDebugEnable);
    }    
    
    private static boolean setDebugFld(boolean enable) {
        // enable jaxp debugging because of jaxb/stax error in gump build
        try {
            Class<?> clz = Class.forName("javax.xml.stream.FactoryFinder");
            Field fld = clz.getDeclaredField("debug");
            fld.setAccessible(true);
            boolean isDebug = (Boolean)fld.get(null);
            fld.set(null, enable);
            return isDebug;
        } catch (Exception e) {
            // ignore
            return false;
        }
    }
    
    @Test
    public void render() throws Exception {
        POIDataSamples samples = POIDataSamples.getSlideShowInstance();

        String[] testFiles = {"alterman_security.ppt","alterman_security.pptx","KEY02.pptx","themes.pptx","backgrounds.pptx","layouts.pptx", "sample.pptx", "shapes.pptx",};
        String[] args = {
            "-format", "null", // png,gif,jpg or null for test
            "-slide", "-1", // -1 for all
            "-outdir", new File("build/tmp/").getCanonicalPath(),
            "-quite",
            "dummyfile"
        };
        for(String sampleFile : testFiles){
            args[args.length-1] = samples.getFile(sampleFile).getCanonicalPath();
            try {
                PPTX2PNG.main(args);
            } catch (IllegalStateException e) {
                throw new IllegalStateException("While reading file " + sampleFile, e);
            }
        }
    }
}
