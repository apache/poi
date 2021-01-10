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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.poi.xddf.usermodel.chart.ErrorBarType;
import org.apache.poi.xddf.usermodel.chart.ErrorDirection;
import org.apache.poi.xddf.usermodel.chart.ErrorValueType;
import org.apache.poi.xddf.usermodel.chart.XDDFErrorBars;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;
import org.openxmlformats.schemas.drawingml.x2006.main.*;

// aim is to get these classes loaded and included in poi-ooxml-lite.jar
class TestNecessaryOOXMLClasses {

    @Test
    void testProblemClasses() {
        CTXYAdjustHandle ctxyAdjustHandle = CTXYAdjustHandle.Factory.newInstance();
        assertNotNull(ctxyAdjustHandle);
        CTPolarAdjustHandle ctPolarAdjustHandle = CTPolarAdjustHandle.Factory.newInstance();
        assertNotNull(ctPolarAdjustHandle);
        CTChartLines ctChartLines = CTChartLines.Factory.newInstance();
        assertNotNull(ctChartLines);
        CTDashStop ctDashStop = CTDashStop.Factory.newInstance();
        assertNotNull(ctDashStop);
        CTSurface ctSurface = CTSurface.Factory.newInstance();
        assertNotNull(ctSurface);
        CTLegendEntry ctLegendEntry = CTLegendEntry.Factory.newInstance();
        assertNotNull(ctLegendEntry);
        CTShape3D ctShape3D = CTShape3D.Factory.newInstance();
        assertNotNull(ctShape3D);
        CTScene3D ctScene3D = CTScene3D.Factory.newInstance();
        assertNotNull(ctScene3D);
        CTEffectContainer ctEffectContainer = CTEffectContainer.Factory.newInstance();
        assertNotNull(ctEffectContainer);
        CTConnectionSite ctConnectionSite = CTConnectionSite.Factory.newInstance();
        assertNotNull(ctConnectionSite);
        STLblAlgn stLblAlgn = STLblAlgn.Factory.newInstance();
        assertNotNull(stLblAlgn);
        STBlackWhiteMode stBlackWhiteMode = STBlackWhiteMode.Factory.newInstance();
        assertNotNull(stBlackWhiteMode);
        STRectAlignment stRectAlignment = STRectAlignment.Factory.newInstance();
        assertNotNull(stRectAlignment);
        STTileFlipMode stTileFlipMode = STTileFlipMode.Factory.newInstance();
        assertNotNull(stTileFlipMode);
        STPresetPatternVal stPresetPatternVal = STPresetPatternVal.Factory.newInstance();
        assertNotNull(stPresetPatternVal);
        STOnOffStyleType stOnOffStyleType = STOnOffStyleType.Factory.newInstance();
        assertNotNull(stOnOffStyleType);
        CTLineJoinBevel ctLineJoinBevel = CTLineJoinBevel.Factory.newInstance();
        assertNotNull(ctLineJoinBevel);
        CTLineJoinMiterProperties ctLineJoinMiterProperties = CTLineJoinMiterProperties.Factory.newInstance();
        assertNotNull(ctLineJoinMiterProperties);
        CTTileInfoProperties ctTileInfoProperties = CTTileInfoProperties.Factory.newInstance();
        assertNotNull(ctTileInfoProperties);
        CTTableStyleTextStyle ctTableStyleTextStyle = CTTableStyleTextStyle.Factory.newInstance();
        assertNotNull(ctTableStyleTextStyle);
        CTHeaderFooter ctHeaderFooter = CTHeaderFooter.Factory.newInstance();
        assertNotNull(ctHeaderFooter);
        CTMarkerSize ctMarkerSize = CTMarkerSize.Factory.newInstance();
        assertNotNull(ctMarkerSize);
        CTDLbls ctdLbls = CTDLbls.Factory.newInstance();
        assertNotNull(ctdLbls);
        CTMarker ctMarker = CTMarker.Factory.newInstance();
        assertNotNull(ctMarker);
        STMarkerStyle stMarkerStyle = STMarkerStyle.Factory.newInstance();
        assertNotNull(stMarkerStyle);
        CTMarkerStyle ctMarkerStyle = CTMarkerStyle.Factory.newInstance();
        assertNotNull(ctMarkerStyle);
        CTExternalData ctExternalData = CTExternalData.Factory.newInstance();
        assertNotNull(ctExternalData);
        CTAxisUnit ctAxisUnit = CTAxisUnit.Factory.newInstance();
        assertNotNull(ctAxisUnit);
        CTLblAlgn ctLblAlgn = CTLblAlgn.Factory.newInstance();
        assertNotNull(ctLblAlgn);
        CTDashStopList ctDashStopList = CTDashStopList.Factory.newInstance();
        assertNotNull(ctDashStopList);
        STDispBlanksAs stDashBlanksAs = STDispBlanksAs.Factory.newInstance();
        assertNotNull(stDashBlanksAs);
        CTDispBlanksAs ctDashBlanksAs = CTDispBlanksAs.Factory.newInstance();
        assertNotNull(ctDashBlanksAs);
        CTAreaChart ctAreaChart = CTAreaChart.Factory.newInstance();
        assertNotNull(ctAreaChart);
        CTArea3DChart ctArea3DChart = CTArea3DChart.Factory.newInstance();
        assertNotNull(ctArea3DChart);
        CTSurfaceChart ctSurfaceChart = CTSurfaceChart.Factory.newInstance();
        assertNotNull(ctSurfaceChart);
        CTDoughnutChart ctDoughnutChart = CTDoughnutChart.Factory.newInstance();
        assertNotNull(ctDoughnutChart);
        CTBar3DChart ctBar3DChart = CTBar3DChart.Factory.newInstance();
        assertNotNull(ctBar3DChart);
        CTLine3DChart ctLine3DChart = CTLine3DChart.Factory.newInstance();
        assertNotNull(ctLine3DChart);
        CTPie3DChart ctPie3DChart = CTPie3DChart.Factory.newInstance();
        assertNotNull(ctPie3DChart);
        CTSurface3DChart ctSurface3DChart = CTSurface3DChart.Factory.newInstance();
        assertNotNull(ctSurface3DChart);
        CTAreaSer ctAreaSer = CTAreaSer.Factory.newInstance();
        assertNotNull(ctAreaSer);
        CTSurfaceSer ctSurfaceSer = CTSurfaceSer.Factory.newInstance();
        assertNotNull(ctSurfaceSer);
        CTView3D ctView3D = CTView3D.Factory.newInstance();
        assertNotNull(ctView3D);
        STShape stShape = STShape.Factory.newInstance();
        assertNotNull(stShape);

        STLblAlgn.Enum e1 = STLblAlgn.Enum.forString("ctr");
        assertNotNull(e1);
        STBlackWhiteMode.Enum e2 = STBlackWhiteMode.Enum.forString("clr");
        assertNotNull(e2);
        STRectAlignment.Enum e3 = STRectAlignment.Enum.forString("ctr");
        assertNotNull(e3);
        STTileFlipMode.Enum e4 = STTileFlipMode.Enum.forString("xy");
        assertNotNull(e4);
        STPresetPatternVal.Enum e5 = STPresetPatternVal.Enum.forString("horz");
        assertNotNull(e5);
        STMarkerStyle.Enum e6 = STMarkerStyle.Enum.forString("circle");
        assertNotNull(e6);
        STDispBlanksAs.Enum e7 = STDispBlanksAs.Enum.forString("span");
        assertNotNull(e7);
        STShape.Enum e8 = STShape.Enum.forString("cone");
        assertNotNull(e8);


        CTTextBulletTypefaceFollowText ctTextBulletTypefaceFollowText = CTTextBulletTypefaceFollowText.Factory.newInstance();
        assertNotNull(ctTextBulletTypefaceFollowText);
        CTTextBulletSizeFollowText ctTextBulletSizeFollowText = CTTextBulletSizeFollowText.Factory.newInstance();
        assertNotNull(ctTextBulletSizeFollowText);
        CTTextBulletColorFollowText ctTextBulletColorFollowText = CTTextBulletColorFollowText.Factory.newInstance();
        assertNotNull(ctTextBulletColorFollowText);
        CTTextBlipBullet ctTextBlipBullet = CTTextBlipBullet.Factory.newInstance();
        assertNotNull(ctTextBlipBullet);
        CTRotX ctRotX = CTRotX.Factory.newInstance();
        assertNotNull(ctRotX);
        CTRotY ctRotY = CTRotY.Factory.newInstance();
        assertNotNull(ctRotY);
        CTPerspective ctPerspective = CTPerspective.Factory.newInstance();
        assertNotNull(ctPerspective);
        CTDepthPercent ctDepthPercent = CTDepthPercent.Factory.newInstance();
        assertNotNull(ctDepthPercent);
        CTHPercent ctHPercent = CTHPercent.Factory.newInstance();
        assertNotNull(ctHPercent);
        CTShape ctShape = CTShape.Factory.newInstance();
        assertNotNull(ctShape);
        CTOverlap ctOverlap = CTOverlap.Factory.newInstance();
        assertNotNull(ctOverlap);
        CTFirstSliceAng ctFirstSliceAng = CTFirstSliceAng.Factory.newInstance();
        assertNotNull(ctFirstSliceAng);

        assertNotNull(ErrorBarType.BOTH);
        assertNotNull(ErrorValueType.CUSTOM);
        assertNotNull(ErrorDirection.X);
        XDDFErrorBars xeb = new XDDFErrorBars();
        assertNotNull(xeb);
        assertNull(xeb.getErrorBarType());
        assertNull(xeb.getErrorDirection());
        assertNull(xeb.getErrorValueType());

        assertNotNull(CTErrBarType.Factory.newInstance());
        assertNotNull(CTErrValType.Factory.newInstance());
        assertNotNull(CTErrDir.Factory.newInstance());

        STErrBarType.Enum e9 = STErrBarType.Enum.forString("both");
        assertNotNull(e9);
        STErrValType.Enum e10 = STErrValType.Enum.forString("percentage");
        assertNotNull(e10);
        STErrDir.Enum e11 = STErrDir.Enum.forString("x");
        assertNotNull(e11);
        CTErrBars bars = CTErrBars.Factory.newInstance();
        assertNotNull(bars);
    }

}
