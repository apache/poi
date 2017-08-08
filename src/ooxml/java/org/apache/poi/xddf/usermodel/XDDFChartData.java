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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Beta;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumVal;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrRef;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrVal;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTUnsignedInt;

/**
 * Base of all XDDF Chart Data
 */
@Beta
public abstract class XDDFChartData {
    protected List<Series> series;
    protected XSSFSheet sheet;
    private XDDFCategoryAxis categoryAxis;
    private List<XDDFValueAxis> valueAxes;

    protected XDDFChartData(XSSFSheet sheet) {
        this.sheet = sheet;
        this.series = new ArrayList<Series>();
    }

    protected void defineAxes(CTUnsignedInt[] axes, Map<Long, XDDFCategoryAxis> categories,
            Map<Long, XDDFValueAxis> values) {
        List<XDDFValueAxis> list = new ArrayList<XDDFValueAxis>(axes.length);
        for (CTUnsignedInt axe : axes) {
            Long axisId = axe.getVal();
            XDDFCategoryAxis category = categories.get(axisId);
            if (category == null) {
                XDDFValueAxis axis = values.get(axisId);
                if (axis != null) {
                    list.add(axis);
                }
            } else {
                this.categoryAxis = category;
            }
        }
        this.valueAxes = Collections.unmodifiableList(list);
    }

    public XDDFCategoryAxis getCategoryAxis() {
        return categoryAxis;
    }

    public List<XDDFValueAxis> getValueAxes() {
        return valueAxes;
    }

    public XDDFChartData.Series getSeries(int index) {
        return series.get(index);
    }

    public void plot() {
        for (Series s : series) {
            s.plot();
        }
    }

    private String setSheetTitle(String title) {
        sheet.createRow(0).createCell(1).setCellValue(title);
        return new CellReference(sheet.getSheetName(), 0, 1, true, true).formatAsString();
    }

    public abstract void setVaryColors(boolean varyColors);

    public abstract void addSeries(XDDFCategoryDataSource category, XDDFNumericalDataSource<? extends Number> values);

    public abstract class Series {
        protected abstract CTStrRef getSeriesTxStrRef();

        public abstract void setShowLeaderLines(boolean showLeaderLines);

        protected XDDFCategoryDataSource categoryData;
        protected XDDFNumericalDataSource<? extends Number> valuesData;

        protected abstract CTAxDataSource getAxDS();

        protected abstract CTNumDataSource getNumDS();

        protected Series(XDDFCategoryDataSource category, XDDFNumericalDataSource<? extends Number> values) {
            this.categoryData = category;
            this.valuesData = values;
        }

        public void replaceData(XDDFCategoryDataSource category, XDDFNumericalDataSource<? extends Number> values) {
            this.categoryData = category;
            this.valuesData = values;
        }

        public void setTitle(String title) {
            String titleRef = setSheetTitle(title);
            CTStrRef ref = getSeriesTxStrRef();
            ref.getStrCache().getPtArray(0).setV(title);
            ref.setF(titleRef);
        }

        public void plot() {
            if (categoryData == null || valuesData == null) {
                throw new IllegalStateException("Category and values must be defined before filling chart data.");
            }
            int numOfPoints = categoryData.getPointCount();
            if (numOfPoints != valuesData.getPointCount()) {
                throw new IllegalStateException("Category and values must have the same point count.");
            }
            fillAxDataSource(getAxDS(), numOfPoints);
            fillFirstValuesDataSource(getNumDS(), numOfPoints);
            fillSheet(numOfPoints);
        }

        private void fillAxDataSource(CTAxDataSource ds, int numOfPoints) {
            CTStrData cache = ds.getStrRef().getStrCache();
            cache.setPtArray(null); // unset old axis text
            cache.getPtCount().setVal(numOfPoints);
            for (int i = 0; i < numOfPoints; ++i) {
                String value = categoryData.getPointAt(i);
                if (value != null) {
                    CTStrVal ctStrVal = cache.addNewPt();
                    ctStrVal.setIdx(i);
                    ctStrVal.setV(value);
                }
            }
            String dataRange = new CellRangeAddress(1, numOfPoints, 0, 0).formatAsString(sheet.getSheetName(), true);
            ds.getStrRef().setF(dataRange);
        }

        private void fillFirstValuesDataSource(CTNumDataSource ds, int numOfPoints) {
            CTNumData cache = ds.getNumRef().getNumCache();
            String formatCode = valuesData.getFormatCode();
            if (formatCode == null) {
                if (cache.isSetFormatCode()) {
                    cache.unsetFormatCode();
                }
            } else {
                cache.setFormatCode(formatCode);
            }
            cache.setPtArray(null); // unset old values
            cache.getPtCount().setVal(numOfPoints);
            for (int i = 0; i < numOfPoints; ++i) {
                Number value = valuesData.getPointAt(i);
                if (value != null) {
                    CTNumVal ctNumVal = cache.addNewPt();
                    ctNumVal.setIdx(i);
                    ctNumVal.setV(value.toString());
                }
            }
            String dataRange = new CellRangeAddress(1, numOfPoints, 1, 1).formatAsString(sheet.getSheetName(), true);
            ds.getNumRef().setF(dataRange);
        }

        private void fillSheet(int numOfPoints) {
            for (int i = 0; i < numOfPoints; i++) {
                XSSFRow row = sheet.createRow(i + 1); // first row is for title
                row.createCell(0).setCellValue(categoryData.getPointAt(i));
                row.createCell(1).setCellValue(valuesData.getPointAt(i).doubleValue());
            }
        }
    }
}
