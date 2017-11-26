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
import org.apache.poi.xddf.usermodel.text.XDDFTextBody;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLegendEntry;

@Beta
public class XDDFLegendEntry {
    private CTLegendEntry entry;

    @Internal
    protected XDDFLegendEntry(CTLegendEntry entry) {
        this.entry = entry;
    }

    @Internal
    protected CTLegendEntry getXmlObject() {
        return entry;
    }

    public XDDFTextBody getTextBody() {
        if (entry.isSetTxPr()) {
            return new XDDFTextBody(entry.getTxPr());
        } else {
            return null;
        }
    }

    public void setTextBody(XDDFTextBody body) {
        if (body == null) {
            entry.unsetTxPr();
        } else {
            entry.setTxPr(body.getXmlObject());
        }
    }

    public boolean getDelete() {
        if (entry.isSetDelete()) {
            return entry.getDelete().getVal();
        } else {
            return false;
        }
    }

    public void setDelete(Boolean delete) {
        if (delete == null) {
            entry.unsetDelete();
        } else {
            if (entry.isSetDelete()) {
                entry.getDelete().setVal(delete);
            } else {
                entry.addNewDelete().setVal(delete);
            }
        }
    }

    public long getIndex() {
        return entry.getIdx().getVal();
    }

    public void setIndex(long index) {
        entry.getIdx().setVal(index);
    }

    public void setExtensionList(XDDFChartExtensionList list) {
        if (list == null) {
            entry.unsetExtLst();
        } else {
            entry.setExtLst(list.getXmlObject());
        }
    }

    public XDDFChartExtensionList getExtensionList() {
        if (entry.isSetExtLst()) {
            return new XDDFChartExtensionList(entry.getExtLst());
        } else {
            return null;
        }
    }
}
