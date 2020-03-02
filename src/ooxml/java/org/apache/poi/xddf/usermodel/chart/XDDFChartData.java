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

package org.apache.poi.xddf.usermodel.chart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xddf.usermodel.XDDFFillProperties;
import org.apache.poi.xddf.usermodel.XDDFLineProperties;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumRef;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrRef;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTUnsignedInt;

/**
 * Base of all XDDF Chart Data
 */
@Beta
public abstract class XDDFChartData {
    protected XDDFChart parent;
    protected List<Series> series;
    private XDDFCategoryAxis categoryAxis;
    private List<XDDFValueAxis> valueAxes;

    protected XDDFChartData(XDDFChart chart) {
        this.parent = chart;
        this.series = new ArrayList<>();
    }

    protected void defineAxes(CTUnsignedInt[] axes, Map<Long, XDDFChartAxis> categories,
            Map<Long, XDDFValueAxis> values) {
        List<XDDFValueAxis> list = new ArrayList<>(axes.length);
        for (CTUnsignedInt axe : axes) {
            Long axisId = axe.getVal();
            XDDFChartAxis category = categories.get(axisId);
            if (category == null) {
                XDDFValueAxis axis = values.get(axisId);
                if (axis != null) {
                    list.add(axis);
                }
            } else if (category instanceof XDDFCategoryAxis) {
                this.categoryAxis = (XDDFCategoryAxis) category;
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

    /**
     * Calls to <code>getSeries().add(series)</code> or to <code>getSeries().remove(series)</code>
     * may corrupt the workbook.
     *
     * <p>
     * Instead, use the following methods:
     * <ul>
     * <li>{@link #getSeriesCount()}</li>
     * <li>{@link #getSeries(int)}</li>
     * <li>{@link #addSeries(XDDFDataSource,XDDFNumericalDataSource)}</li>
     * <li>{@link #removeSeries(int)}</li>
     * </ul>
     *
     * @deprecated since POI 4.1.1
     * @return
     */
    @Deprecated
    public List<Series> getSeries() {
        return Collections.unmodifiableList(series);
    }

    public final int getSeriesCount() {
        return series.size();
    }

    public final Series getSeries(int n) {
        return series.get(n);
    }

    public final void removeSeries(int n) {
        final String procName = "removeSeries";
        if (n < 0 || series.size() <= n) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "%s(%d): illegal index", procName, n));
        }
        series.remove(n);
        removeCTSeries(n);
    }

    /**
     * This method should be implemented in every class that extends <code>XDDFChartData</code>.
     * <p>
     * A typical implementation would be
     *
     * <pre><code>
    protected void removeCTSeries(int n) {
        chart.removeSer(n);
    }

     * </code></pre>
     *
     * @param n
     */
    @Internal
    protected abstract void removeCTSeries(int n);

    public abstract void setVaryColors(Boolean varyColors);

    public abstract XDDFChartData.Series addSeries(XDDFDataSource<?> category,
            XDDFNumericalDataSource<? extends Number> values);

    public abstract class Series {
        protected abstract CTSerTx getSeriesText();

        public abstract void setShowLeaderLines(boolean showLeaderLines);
        public abstract XDDFShapeProperties getShapeProperties();
        public abstract void setShapeProperties(XDDFShapeProperties properties);

        protected XDDFDataSource<?> categoryData;
        protected XDDFNumericalDataSource<? extends Number> valuesData;

        protected abstract CTAxDataSource getAxDS();

        protected abstract CTNumDataSource getNumDS();

        protected abstract void setIndex(long index);

        protected abstract void setOrder(long order);

        protected Series(XDDFDataSource<?> category, XDDFNumericalDataSource<? extends Number> values) {
            replaceData(category, values);
        }

        public void replaceData(XDDFDataSource<?> category, XDDFNumericalDataSource<? extends Number> values) {
            if (category == null || values == null) {
                throw new IllegalStateException("Category and values must be defined before filling chart data.");
            }
            int numOfPoints = category.getPointCount();
            if (numOfPoints != values.getPointCount()) {
                throw new IllegalStateException("Category and values must have the same point count.");
            }
            this.categoryData = category;
            this.valuesData = values;
        }

        public void setTitle(String title, CellReference titleRef) {
            if (titleRef == null) {
                getSeriesText().setV(title);
            } else {
                CTStrRef ref;
                if (getSeriesText().isSetStrRef()) {
                    ref = getSeriesText().getStrRef();
                } else {
                    ref = getSeriesText().addNewStrRef();
                }
                ref.setF(titleRef.formatAsString());
                if (title != null) {
                    CTStrData cache;
                    if (ref.isSetStrCache()) {
                        cache = ref.getStrCache();
                    } else {
                        cache = ref.addNewStrCache();
                    }
                    if (cache.sizeOfPtArray() < 1) {
                        cache.addNewPtCount().setVal(1);
                        cache.addNewPt().setIdx(0);;
                    }
                    cache.getPtArray(0).setV(title);
                }
            }
        }

        public XDDFDataSource<?> getCategoryData() {
            return categoryData;
        }

        public XDDFNumericalDataSource<? extends Number> getValuesData() {
            return valuesData;
        }

        public void plot() {
            if (categoryData.isNumeric()) {
                CTNumData cache = retrieveNumCache(getAxDS(), categoryData);
                categoryData.fillNumericalCache(cache);
            } else {
                CTStrData cache = retrieveStrCache(getAxDS(), categoryData);
                categoryData.fillStringCache(cache);
            }
            CTNumData cache = retrieveNumCache(getNumDS(), valuesData);
            valuesData.fillNumericalCache(cache);
        }

        /**
         * @param fill
         *      fill property for the shape representing the series.
         * @since POI 4.1.1
         */
        public void setFillProperties(XDDFFillProperties fill) {
            XDDFShapeProperties properties = getShapeProperties();
            if (properties == null) {
                properties = new XDDFShapeProperties();
            }
            properties.setFillProperties(fill);
            setShapeProperties(properties);
        }

        /**
         * @param line
         *      line property for the shape representing the series.
         * @since POI 4.1.1
         */
        public void setLineProperties(XDDFLineProperties line) {
            XDDFShapeProperties properties = getShapeProperties();
            if (properties == null) {
                properties = new XDDFShapeProperties();
            }
            properties.setLineProperties(line);
            setShapeProperties(properties);
        }

        private CTNumData retrieveNumCache(final CTAxDataSource axDataSource, XDDFDataSource<?> data) {
            CTNumData numCache;
            if (data.isReference()) {
                CTNumRef numRef;
                if (axDataSource.isSetNumRef()) {
                    numRef = axDataSource.getNumRef();
                } else {
                    numRef = axDataSource.addNewNumRef();
                }
                if (numRef.isSetNumCache()) {
                    numCache = numRef.getNumCache();
                } else {
                    numCache = numRef.addNewNumCache();
                }
                numRef.setF(data.getDataRangeReference());
                if (axDataSource.isSetNumLit()) {
                    axDataSource.unsetNumLit();
                }
            } else {
                if (axDataSource.isSetNumLit()) {
                    numCache = axDataSource.getNumLit();
                } else {
                    numCache = axDataSource.addNewNumLit();
                }
                if (axDataSource.isSetNumRef()) {
                    axDataSource.unsetNumRef();
                }
            }
            return numCache;
        }

        private CTStrData retrieveStrCache(final CTAxDataSource axDataSource, XDDFDataSource<?> data) {
            CTStrData strCache;
            if (data.isReference()) {
                CTStrRef strRef;
                if (axDataSource.isSetStrRef()) {
                    strRef = axDataSource.getStrRef();
                } else {
                    strRef = axDataSource.addNewStrRef();
                }
                if (strRef.isSetStrCache()) {
                    strCache = strRef.getStrCache();
                } else {
                    strCache = strRef.addNewStrCache();
                }
                strRef.setF(data.getDataRangeReference());
                if (axDataSource.isSetStrLit()) {
                    axDataSource.unsetStrLit();
                }
            } else {
                if (axDataSource.isSetStrLit()) {
                    strCache = axDataSource.getStrLit();
                } else {
                    strCache = axDataSource.addNewStrLit();
                }
                if (axDataSource.isSetStrRef()) {
                    axDataSource.unsetStrRef();
                }
            }
            return strCache;
        }

        private CTNumData retrieveNumCache(final CTNumDataSource numDataSource, XDDFDataSource<?> data) {
            CTNumData numCache;
            if (data.isReference()) {
                CTNumRef numRef;
                if (numDataSource.isSetNumRef()) {
                    numRef = numDataSource.getNumRef();
                } else {
                    numRef = numDataSource.addNewNumRef();
                }
                if (numRef.isSetNumCache()) {
                    numCache = numRef.getNumCache();
                } else {
                    numCache = numRef.addNewNumCache();
                }
                numRef.setF(data.getDataRangeReference());
                if (numDataSource.isSetNumLit()) {
                    numDataSource.unsetNumLit();
                }
            } else {
                if (numDataSource.isSetNumLit()) {
                    numCache = numDataSource.getNumLit();
                } else {
                    numCache = numDataSource.addNewNumLit();
                }
                if (numDataSource.isSetNumRef()) {
                    numDataSource.unsetNumRef();
                }
            }
            return numCache;
        }
    }
}
