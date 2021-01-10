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
package org.apache.poi.xddf.usermodel.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.poi.ooxml.POIXMLFactory;
import org.apache.poi.ooxml.POIXMLRelation;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartSpace;

class TestXDDFChart {
    @Test
    void testConstruct() {
        // minimal test to cause ooxml-lite to include all the classes in poi-ooxml-lite
        XDDFChart xddfChart = newXDDFChart();

        assertNotNull(xddfChart.getCTChartSpace());
        assertNotNull(xddfChart.getCTPlotArea());
    }

    @Test
    void testSetExternalId() {
        XDDFChart xddfChart = newXDDFChart();
        CTChartSpace ctChartSpace = xddfChart.getCTChartSpace();

        xddfChart.setExternalId("rid1");
        assertEquals("rid1", ctChartSpace.getExternalData().getId());

        xddfChart.setExternalId("rid2");
        assertEquals("rid2", ctChartSpace.getExternalData().getId());
    }

    private XDDFChart newXDDFChart() {
        XDDFChart xddfChart = new XDDFChart() {
            @Override
            protected POIXMLRelation getChartRelation() {
                return null;
            }

            @Override
            protected POIXMLRelation getChartWorkbookRelation() {
                return null;
            }

            @Override
            protected POIXMLFactory getChartFactory() {
                return null;
            }
        };
        return xddfChart;
    }
}