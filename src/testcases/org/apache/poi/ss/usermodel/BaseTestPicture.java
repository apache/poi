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

package org.apache.poi.ss.usermodel;

import static org.junit.Assert.assertEquals;

import java.awt.Dimension;
import java.io.FileOutputStream;

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.util.ImageUtils;
import org.apache.poi.util.Units;

/**
 * @author Yegor Kozlov
 */
public abstract class BaseTestPicture {

    private final ITestDataProvider _testDataProvider;

    protected BaseTestPicture(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    public void baseTestResize(Picture input, Picture compare, double scaleX, double scaleY) {
        input.resize(scaleX, scaleY);
        
        ClientAnchor inpCA = input.getClientAnchor();
        ClientAnchor cmpCA = compare.getClientAnchor();
        
        Dimension inpDim = ImageUtils.getDimensionFromAnchor(input);
        Dimension cmpDim = ImageUtils.getDimensionFromAnchor(compare);

        double emuPX = Units.EMU_PER_PIXEL;
        
        assertEquals("the image height differs", inpDim.getHeight(), cmpDim.getHeight(), emuPX*6);
        assertEquals("the image width differs", inpDim.getWidth(),  cmpDim.getWidth(),  emuPX*6);
        assertEquals("the starting column differs", inpCA.getCol1(), cmpCA.getCol1());
        assertEquals("the column x-offset differs", inpCA.getDx1(), cmpCA.getDx1(), 1);
        assertEquals("the column y-offset differs", inpCA.getDy1(), cmpCA.getDy1(), 1);
        assertEquals("the ending columns differs", inpCA.getCol2(), cmpCA.getCol2());
        // can't compare row heights because of variable test heights
        
        input.resize();
        inpDim = ImageUtils.getDimensionFromAnchor(input);
        
        Dimension imgDim = input.getImageDimension();

        assertEquals("the image height differs", imgDim.getHeight(), inpDim.getHeight()/emuPX, 1);
        assertEquals("the image width differs",  imgDim.getWidth(), inpDim.getWidth()/emuPX,  1);
    }

}
