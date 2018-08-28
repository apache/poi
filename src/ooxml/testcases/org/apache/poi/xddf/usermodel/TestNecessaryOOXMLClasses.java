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

package org.apache.poi.xddf.usermodel;

import org.junit.Assert;
import org.junit.Test;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;
import org.openxmlformats.schemas.drawingml.x2006.main.*;

// aim is to get these classes loaded and included in poi-ooxml-schemas.jar
public class TestNecessaryOOXMLClasses {

    @Test
    public void testProblemClasses() {
        CTXYAdjustHandle ctxyAdjustHandle = CTXYAdjustHandle.Factory.newInstance();
        Assert.assertNotNull(ctxyAdjustHandle);
        CTPolarAdjustHandle ctPolarAdjustHandle = CTPolarAdjustHandle.Factory.newInstance();
        Assert.assertNotNull(ctPolarAdjustHandle);
        CTChartLines ctChartLines = CTChartLines.Factory.newInstance();
        Assert.assertNotNull(ctChartLines);
        CTDashStop ctDashStop = CTDashStop.Factory.newInstance();
        Assert.assertNotNull(ctDashStop);
        CTSurface ctSurface = CTSurface.Factory.newInstance();
        Assert.assertNotNull(ctSurface);
        CTLegendEntry ctLegendEntry = CTLegendEntry.Factory.newInstance();
        Assert.assertNotNull(ctLegendEntry);
        CTShape3D ctShape3D = CTShape3D.Factory.newInstance();
        Assert.assertNotNull(ctShape3D);
        CTScene3D ctScene3D = CTScene3D.Factory.newInstance();
        Assert.assertNotNull(ctScene3D);
        CTEffectContainer ctEffectContainer = CTEffectContainer.Factory.newInstance();
        Assert.assertNotNull(ctEffectContainer);
        CTConnectionSite ctConnectionSite = CTConnectionSite.Factory.newInstance();
        Assert.assertNotNull(ctConnectionSite);
        STLblAlgn stLblAlgn = STLblAlgn.Factory.newInstance();
        Assert.assertNotNull(stLblAlgn);
        STBlackWhiteMode stBlackWhiteMode = STBlackWhiteMode.Factory.newInstance();
        Assert.assertNotNull(stBlackWhiteMode);
        STRectAlignment stRectAlignment = STRectAlignment.Factory.newInstance();
        Assert.assertNotNull(stRectAlignment);
        STTileFlipMode stTileFlipMode = STTileFlipMode.Factory.newInstance();
        Assert.assertNotNull(stTileFlipMode);
        STPresetPatternVal stPresetPatternVal = STPresetPatternVal.Factory.newInstance();
        Assert.assertNotNull(stPresetPatternVal);
        STOnOffStyleType stOnOffStyleType = STOnOffStyleType.Factory.newInstance();
        Assert.assertNotNull(stOnOffStyleType);
        CTLineJoinBevel ctLineJoinBevel = CTLineJoinBevel.Factory.newInstance();
        Assert.assertNotNull(ctLineJoinBevel);
        CTLineJoinMiterProperties ctLineJoinMiterProperties = CTLineJoinMiterProperties.Factory.newInstance();
        Assert.assertNotNull(ctLineJoinMiterProperties);
        CTTileInfoProperties ctTileInfoProperties = CTTileInfoProperties.Factory.newInstance();
        Assert.assertNotNull(ctTileInfoProperties);
        CTTableStyleTextStyle ctTableStyleTextStyle = CTTableStyleTextStyle.Factory.newInstance();
        Assert.assertNotNull(ctTableStyleTextStyle);
        CTHeaderFooter ctHeaderFooter = CTHeaderFooter.Factory.newInstance();
        Assert.assertNotNull(ctHeaderFooter);
        CTMarkerSize ctMarkerSize = CTMarkerSize.Factory.newInstance();
        Assert.assertNotNull(ctMarkerSize);
        CTDLbls ctdLbls = CTDLbls.Factory.newInstance();
        Assert.assertNotNull(ctdLbls);
        CTMarker ctMarker = CTMarker.Factory.newInstance();
        Assert.assertNotNull(ctMarker);
        STMarkerStyle stMarkerStyle = STMarkerStyle.Factory.newInstance();
        Assert.assertNotNull(stMarkerStyle);
        CTMarkerStyle ctMarkerStyle = CTMarkerStyle.Factory.newInstance();
        Assert.assertNotNull(ctMarkerStyle);
        CTExternalData ctExternalData = CTExternalData.Factory.newInstance();
        Assert.assertNotNull(ctExternalData);
        CTAxisUnit ctAxisUnit = CTAxisUnit.Factory.newInstance();
        Assert.assertNotNull(ctAxisUnit);
        CTLblAlgn ctLblAlgn = CTLblAlgn.Factory.newInstance();
        Assert.assertNotNull(ctLblAlgn);
        CTDashStopList ctDashStopList = CTDashStopList.Factory.newInstance();
        Assert.assertNotNull(ctDashStopList);

        STLblAlgn.Enum e1 = STLblAlgn.Enum.forString("ctr");
        Assert.assertNotNull(e1);
        STBlackWhiteMode.Enum e2 = STBlackWhiteMode.Enum.forString("clr");
        Assert.assertNotNull(e2);
        STRectAlignment.Enum e3 = STRectAlignment.Enum.forString("ctr");
        Assert.assertNotNull(e3);
        STTileFlipMode.Enum e4 = STTileFlipMode.Enum.forString("xy");
        Assert.assertNotNull(e4);
        STPresetPatternVal.Enum e5 = STPresetPatternVal.Enum.forString("horz");
        Assert.assertNotNull(e5);
        STMarkerStyle.Enum e6 = STMarkerStyle.Enum.forString("circle");
        Assert.assertNotNull(e6);

        CTTextBulletTypefaceFollowText ctTextBulletTypefaceFollowText = CTTextBulletTypefaceFollowText.Factory.newInstance();
        Assert.assertNotNull(ctTextBulletTypefaceFollowText);
        CTTextBulletSizeFollowText ctTextBulletSizeFollowText = CTTextBulletSizeFollowText.Factory.newInstance();
        Assert.assertNotNull(ctTextBulletSizeFollowText);
        CTTextBulletColorFollowText ctTextBulletColorFollowText = CTTextBulletColorFollowText.Factory.newInstance();
        Assert.assertNotNull(ctTextBulletColorFollowText);
        CTTextBlipBullet ctTextBlipBullet = CTTextBlipBullet.Factory.newInstance();
        Assert.assertNotNull(ctTextBlipBullet);
    }

}
