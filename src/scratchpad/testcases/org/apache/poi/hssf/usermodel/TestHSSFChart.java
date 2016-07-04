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

package org.apache.poi.hssf.usermodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.chart.SeriesRecord;
import org.apache.poi.hssf.usermodel.HSSFChart.HSSFSeries;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressBase;
import org.junit.Test;

/**
 * Tests for {@link HSSFChart}
 */
public final class TestHSSFChart {

	@Test
	public void testSingleChart() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("WithChart.xls");

		HSSFSheet s1 = wb.getSheetAt(0);
		HSSFSheet s2 = wb.getSheetAt(1);
		HSSFSheet s3 = wb.getSheetAt(2);

		assertEquals(0, HSSFChart.getSheetCharts(s1).length);
		assertEquals(1, HSSFChart.getSheetCharts(s2).length);
		assertEquals(0, HSSFChart.getSheetCharts(s3).length);

		HSSFChart[] charts;

		// Check the chart on the 2nd sheet
		charts = HSSFChart.getSheetCharts(s2);
		assertEquals(1, charts.length);

		assertEquals(2, charts[0].getSeries().length);
		assertEquals("1st Column", charts[0].getSeries()[0].getSeriesTitle());
		assertEquals("2nd Column", charts[0].getSeries()[1].getSeriesTitle());
		assertNull(charts[0].getChartTitle());

		// Check x, y, width, height
		assertEquals(0, charts[0].getChartX());
		assertEquals(0, charts[0].getChartY());
		assertEquals(26492928, charts[0].getChartWidth());
		assertEquals(15040512, charts[0].getChartHeight());
	}

	@Test
	public void testTwoCharts() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("WithTwoCharts.xls");

		HSSFSheet s1 = wb.getSheetAt(0);
		HSSFSheet s2 = wb.getSheetAt(1);
		HSSFSheet s3 = wb.getSheetAt(2);

		assertEquals(0, HSSFChart.getSheetCharts(s1).length);
		assertEquals(1, HSSFChart.getSheetCharts(s2).length);
		assertEquals(1, HSSFChart.getSheetCharts(s3).length);

		HSSFChart[] charts;

		// Check the chart on the 2nd sheet
		charts = HSSFChart.getSheetCharts(s2);
		assertEquals(1, charts.length);

		assertEquals(2, charts[0].getSeries().length);
		assertEquals("1st Column", charts[0].getSeries()[0].getSeriesTitle());
		assertEquals("2nd Column", charts[0].getSeries()[1].getSeriesTitle());
		assertNull(charts[0].getChartTitle());

		// And the third sheet
		charts = HSSFChart.getSheetCharts(s3);
		assertEquals(1, charts.length);

		assertEquals(2, charts[0].getSeries().length);
		assertEquals("Squares", charts[0].getSeries()[0].getSeriesTitle());
		assertEquals("Base Numbers", charts[0].getSeries()[1].getSeriesTitle());
		assertNull(charts[0].getChartTitle());
	}

	@Test
	public void testThreeCharts() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("WithThreeCharts.xls");

		HSSFSheet s1 = wb.getSheetAt(0);
		HSSFSheet s2 = wb.getSheetAt(1);
		HSSFSheet s3 = wb.getSheetAt(2);

		assertEquals(0, HSSFChart.getSheetCharts(s1).length);
		assertEquals(2, HSSFChart.getSheetCharts(s2).length);
		assertEquals(1, HSSFChart.getSheetCharts(s3).length);

		HSSFChart[] charts;

		// Check the charts on the 2nd sheet
		charts = HSSFChart.getSheetCharts(s2);
		assertEquals(2, charts.length);

		assertEquals(2, charts[0].getSeries().length);
		assertEquals("1st Column", charts[0].getSeries()[0].getSeriesTitle());
		assertEquals("2nd Column", charts[0].getSeries()[1].getSeriesTitle());
		assertEquals(6, charts[0].getSeries()[0].getNumValues());
		assertEquals(6, charts[0].getSeries()[1].getNumValues());
		assertEquals(SeriesRecord.CATEGORY_DATA_TYPE_NUMERIC, charts[0].getSeries()[0].getValueType());
		assertEquals(SeriesRecord.CATEGORY_DATA_TYPE_NUMERIC, charts[0].getSeries()[1].getValueType());
		assertNull(charts[0].getChartTitle());

		assertEquals(1, charts[1].getSeries().length);
		assertNull(charts[1].getSeries()[0].getSeriesTitle());
		assertEquals("Pie Chart Title Thingy", charts[1].getChartTitle());

		// And the third sheet
		charts = HSSFChart.getSheetCharts(s3);
		assertEquals(1, charts.length);

		assertEquals(2, charts[0].getSeries().length);
		assertEquals("Squares", charts[0].getSeries()[0].getSeriesTitle());
		assertEquals("Base Numbers", charts[0].getSeries()[1].getSeriesTitle());
		assertEquals("Sheet 3 Chart with Title", charts[0].getChartTitle());
	}
	
    @Test
    public void testExistingSheet3() throws Exception {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("49581.xls");
        
        HSSFSheet sheet = wb.getSheetAt( 2 ) ;
        HSSFChart[] charts = HSSFChart.getSheetCharts( sheet ) ;
        assertEquals(1, charts.length);
        
        for ( HSSFChart chart : charts ) {
            for ( HSSFSeries series : chart.getSeries() ) {
                chart.removeSeries( series ) ;
            }
        }
        
        // Save and re-check
        wb = HSSFITestDataProvider.instance.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt( 2 ) ;
        assertEquals(1, HSSFChart.getSheetCharts(sheet).length);
        
        HSSFChart c = HSSFChart.getSheetCharts(sheet)[0];
        assertEquals(0, c.getSeries().length);
    }

    @Test
    public void testExistingSheet2() throws Exception {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("49581.xls");
        HSSFSheet sheet = wb.getSheetAt( 1 ) ;
        HSSFChart[] charts = HSSFChart.getSheetCharts( sheet ) ;
        
        assertEquals(1, charts.length);
        for ( HSSFChart chart : charts ) {
            HSSFSeries series ;
            
            // Starts with one
            assertEquals(1, chart.getSeries().length);

            // Add two more
            series = chart.createSeries() ;
            series.setCategoryLabelsCellRange( new CellRangeAddress( 3, 4, 0, 0 ) ) ;
            series.setValuesCellRange( new CellRangeAddress( 3, 4, 1, 1 ) ) ;

            series = chart.createSeries() ;
            series.setCategoryLabelsCellRange( new CellRangeAddress( 6, 7, 0, 0 ) ) ;
            series.setValuesCellRange( new CellRangeAddress( 6, 7, 1, 1 ) ) ;
        }
        
        // Save and re-check
        wb = HSSFITestDataProvider.instance.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt( 1 ) ;
        assertEquals(1, HSSFChart.getSheetCharts(sheet).length);
        
        HSSFChart c = HSSFChart.getSheetCharts(sheet)[0];
        assertEquals(3, c.getSeries().length);
    }

    @Test
    public void testExistingSheet1() throws Exception {
       HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("49581.xls");
        HSSFSheet sheet = wb.getSheetAt( 0 ) ;
        HSSFChart[] charts = HSSFChart.getSheetCharts( sheet ) ;
        
        for ( HSSFChart chart : charts ) {
            //System.out.println( chart.getType() ) ;
            HSSFSeries[] seriesArray = chart.getSeries() ;
            //System.out.println( "seriesArray.length=" + seriesArray.length ) ;
            for ( HSSFSeries series : seriesArray )
            {
                //System.out.println( "serie.getNumValues()=" + series.getNumValues() ) ;
                CellRangeAddressBase range ;

                range = series.getValuesCellRange() ;
                //System.out.println( range.toString() ) ;
                range.setLastRow( range.getLastRow() + 1 ) ;
                series.setValuesCellRange( range ) ;

                range = series.getCategoryLabelsCellRange() ;
                //System.out.println( range.toString() ) ;
                range.setLastRow( range.getLastRow() + 1 ) ;
                series.setCategoryLabelsCellRange( range ) ;
            }

            for ( int id = 0 ; id < 2 ; id++ )
            {
                HSSFSeries newSeries = chart.createSeries() ;
                newSeries.setValuesCellRange( new CellRangeAddress( 1 + id, 4, 3, 3 ) ) ;
                String oldSeriesTitle = newSeries.getSeriesTitle() ;
                if ( oldSeriesTitle != null )
                {
                    //System.out.println( "old series title: " + oldSeriesTitle ) ;
                    newSeries.setSeriesTitle( "new series" ) ;
                }
            }
        }

        HSSFChart chart = charts[ 2 ] ;
        chart.removeSeries( chart.getSeries()[ 0 ] ) ;
    }
    
    /**
     * Bug 26862: HSSFWorkbook.cloneSheet copies charts
     */
    @Test
    public void test26862() throws IOException, Exception {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("SimpleChart.xls");
        HSSFSheet srcSheet = wb.getSheetAt(0);
        HSSFChart[] srcCharts = HSSFChart.getSheetCharts(srcSheet);
        assertEquals(1, srcCharts.length);
        HSSFChart srcChart = srcCharts[0];
        
        // Clone the sheet
        HSSFSheet clonedSheet = wb.cloneSheet(0);
        
        // Verify the chart was copied
        HSSFChart[] clonedCharts = HSSFChart.getSheetCharts(clonedSheet);
        assertEquals(1, clonedCharts.length);
        HSSFChart clonedChart = clonedCharts[0];
        assertNotSame(srcChart, clonedChart); //refer to different objects
        assertEquals(srcChart.getType(), clonedChart.getType());
        assertEquals(srcChart.getChartTitle(), clonedChart.getChartTitle());
        assertEquals(srcChart.getChartWidth(), clonedChart.getChartWidth());
        assertEquals(srcChart.getChartHeight(), clonedChart.getChartHeight());
        assertEquals(srcChart.getChartX(), clonedChart.getChartX());
        assertEquals(srcChart.getChartY(), clonedChart.getChartY());
        
        // Check if chart was shallow copied or deep copied
        clonedChart.setChartWidth(clonedChart.getChartWidth()+10);
        assertEquals(srcChart.getChartWidth()+10, clonedChart.getChartWidth());
        
        wb.close();
    }
}
