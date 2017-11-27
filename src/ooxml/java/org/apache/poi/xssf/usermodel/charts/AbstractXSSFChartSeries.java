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

package org.apache.poi.xssf.usermodel.charts;

import org.apache.poi.ss.usermodel.charts.ChartSeries;
import org.apache.poi.ss.usermodel.charts.TitleType;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Removal;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData.Series;;

/**
 * Base of all XSSF Chart Series
 *
 * @deprecated use {@link Series} instead
 */
@Deprecated
@Removal(version="4.2")
public abstract class AbstractXSSFChartSeries implements ChartSeries {

    private String titleValue;
    private CellReference titleRef;
    private TitleType titleType;

    @Override
    public void setTitle(CellReference titleReference) {
        titleType = TitleType.CELL_REFERENCE;
        titleRef = titleReference;
    }

    @Override
    public void setTitle(String title) {
        titleType = TitleType.STRING;
        titleValue = title;
    }

    @Override
    public CellReference getTitleCellReference() {
        if (TitleType.CELL_REFERENCE.equals(titleType)) {
            return titleRef;
        }
        throw new IllegalStateException("Title type is not CellReference.");
    }

    @Override
    public String getTitleString() {
        if (TitleType.STRING.equals(titleType)) {
            return titleValue;
        }
        throw new IllegalStateException("Title type is not String.");
    }

    @Override
    public TitleType getTitleType() {
        return titleType;
    }

    protected boolean isTitleSet() {
        return titleType != null;
    }

    protected CTSerTx getCTSerTx() {
        CTSerTx tx = CTSerTx.Factory.newInstance();
        switch (titleType) {
            case CELL_REFERENCE:
                tx.addNewStrRef().setF(titleRef.formatAsString());
                return tx;
            case STRING:
                tx.setV(titleValue);
                return tx;
            default:
                throw new IllegalStateException("Unkown title type: " + titleType);
        }
    }
}
