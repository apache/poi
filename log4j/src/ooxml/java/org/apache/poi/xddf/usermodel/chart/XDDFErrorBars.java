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

import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTErrBars;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumRef;

/**
 * @since POI 4.1.2
 */
@Beta
public class XDDFErrorBars {
    private CTErrBars bars;

    public XDDFErrorBars() {
        this(CTErrBars.Factory.newInstance());
    }

    @Internal
    protected XDDFErrorBars(CTErrBars bars) {
        this.bars = bars;
    }

    @Internal
    protected XmlObject getXmlObject() {
        return bars;
    }

    public XDDFChartExtensionList getExtensionList() {
        if (bars.isSetExtLst()) {
            return new XDDFChartExtensionList(bars.getExtLst());
        } else {
            return null;
        }
    }

    public void setExtensionList(XDDFChartExtensionList list) {
        if (list == null) {
            if (bars.isSetExtLst()) {
                bars.unsetExtLst();
            }
        } else {
            bars.setExtLst(list.getXmlObject());
        }
    }

    public XDDFShapeProperties getShapeProperties() {
        if (bars.isSetSpPr()) {
            return new XDDFShapeProperties(bars.getSpPr());
        } else {
            return null;
        }
    }

    public void setShapeProperties(XDDFShapeProperties properties) {
        if (properties == null) {
            if (bars.isSetSpPr()) {
                bars.unsetSpPr();
            }
        } else {
            if (bars.isSetSpPr()) {
                bars.setSpPr(properties.getXmlObject());
            } else {
                bars.addNewSpPr().set(properties.getXmlObject());
            }
        }
    }

    public ErrorBarType getErrorBarType() {
        return bars.getErrBarType() == null ? null : ErrorBarType.valueOf(bars.getErrBarType().getVal());
    }

    public void setErrorBarType(ErrorBarType barType) {
        bars.getErrBarType().setVal(barType.underlying);
    }

    public ErrorValueType getErrorValueType() {
        return bars.getErrValType() == null ? null : ErrorValueType.valueOf(bars.getErrValType().getVal());
    }

    public void setErrorValueType(ErrorValueType valueType) {
        bars.getErrValType().setVal(valueType.underlying);
    }

    public ErrorDirection getErrorDirection() {
        if (bars.isSetErrDir()) {
            return ErrorDirection.valueOf(bars.getErrDir().getVal());
        } else {
            return null;
        }
    }

    public void setErrorDirection(ErrorDirection direction) {
        if (direction == null) {
            if (bars.isSetErrDir()) {
                bars.unsetErrDir();
            }
        } else {
            if (bars.isSetErrDir()) {
                bars.getErrDir().setVal(direction.underlying);
            } else {
                bars.addNewErrDir().setVal(direction.underlying);
            }
        }
    }

    public Boolean getNoEndCap() {
        if (bars.isSetVal()) {
            return bars.getNoEndCap().getVal();
        } else {
            return null;
        }
    }

    public void setNoEndCap(Boolean noEndCap) {
        if (noEndCap == null) {
            if (bars.isSetNoEndCap()) {
                bars.unsetNoEndCap();
            }
        } else {
            if (bars.isSetNoEndCap()) {
                bars.getNoEndCap().setVal(noEndCap);
            } else {
                bars.addNewNoEndCap().setVal(noEndCap);
            }
        }
    }

    public Double getValue() {
        if (bars.isSetVal()) {
            return bars.getVal().getVal();
        } else {
            return null;
        }
    }

    public void setValue(Double value) {
        if (value == null) {
            if (bars.isSetVal()) {
                bars.unsetVal();
            }
        } else {
            if (bars.isSetVal()) {
                bars.getVal().setVal(value);
            } else {
                bars.addNewVal().setVal(value);
            }
        }
    }

    public XDDFNumericalDataSource<Double> getMinus() {
        if (bars.isSetMinus()) {
            return XDDFDataSourcesFactory.fromDataSource(bars.getMinus());
        } else {
            return null;
        }
    }

    public void setMinus(XDDFNumericalDataSource<Double> ds) {
        if (ds == null) {
            if (bars.isSetMinus()) {
                bars.unsetMinus();
            }
        } else {
            if (bars.isSetMinus()) {
                ds.fillNumericalCache(retrieveCache(bars.getMinus(), ds.getDataRangeReference()));
            } else {
                CTNumDataSource ctDS = bars.addNewMinus();
                ctDS.addNewNumLit();
                ds.fillNumericalCache(retrieveCache(ctDS, ds.getDataRangeReference()));
            }
        }
    }

    public XDDFNumericalDataSource<Double> getPlus() {
        if (bars.isSetPlus()) {
            return XDDFDataSourcesFactory.fromDataSource(bars.getPlus());
        } else {
            return null;
        }
    }

    public void setPlus(XDDFNumericalDataSource<Double> ds) {
        if (ds == null) {
            if (bars.isSetPlus()) {
                bars.unsetPlus();
            }
        } else {
            if (bars.isSetPlus()) {
                ds.fillNumericalCache(retrieveCache(bars.getPlus(), ds.getDataRangeReference()));
            } else {
                CTNumDataSource ctDS = bars.addNewPlus();
                ctDS.addNewNumLit();
                ds.fillNumericalCache(retrieveCache(ctDS, ds.getDataRangeReference()));
            }
        }
    }

    private CTNumData retrieveCache(CTNumDataSource ds, String dataRangeReference) {
        if (ds.isSetNumRef()) {
            CTNumRef numRef = ds.getNumRef();
            numRef.setF(dataRangeReference);
            return numRef.getNumCache();
        } else {
            return ds.getNumLit();
        }
    }
}
