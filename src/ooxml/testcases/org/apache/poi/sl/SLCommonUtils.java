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

package org.apache.poi.sl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;

public class SLCommonUtils {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();
    
    /** a generic way to open a sample slideshow document **/
    public static SlideShow<?,?> openSampleSlideshow(String sampleName) throws IOException {
        try (InputStream is = _slTests.openResourceAsStream(sampleName)) {
            return SlideShowFactory.create(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests, if the scratchpad classes are on the classpath
     * 
     * @return true, if only xslf is on the classpath, and false, if both classpaths
     *    (XSLF and HSLF) can be used/referenced 
     */
    public static boolean xslfOnly() {
        try {
            Class.forName("org.apache.poi.hslf.usermodel.HSLFSlideShow");
            return false;
        } catch (Exception e) {
            return true;
        }
    }

}
