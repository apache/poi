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
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLayout;

@Beta
public class XDDFLayout {
    private CTLayout layout;

    public XDDFLayout() {
        this(CTLayout.Factory.newInstance());
    }

    @Internal
    protected XDDFLayout(CTLayout layout) {
        this.layout = layout;
    }

    @Internal
    protected CTLayout getXmlObject() {
        return layout;
    }

    public void setExtensionList(XDDFChartExtensionList list) {
        if (list == null) {
            if (layout.isSetExtLst()) {
                layout.unsetExtLst();
            }
        } else {
            layout.setExtLst(list.getXmlObject());
        }
    }

    public XDDFChartExtensionList getExtensionList() {
        if (layout.isSetExtLst()) {
            return new XDDFChartExtensionList(layout.getExtLst());
        } else {
            return null;
        }
    }

    public void setManualLayout(XDDFManualLayout manual) {
        if (manual == null) {
            if (layout.isSetManualLayout()) {
                layout.unsetManualLayout();
            }
        } else {
            layout.setManualLayout(manual.getXmlObject());
        }
    }

    public XDDFManualLayout getManualLayout() {
        if (layout.isSetManualLayout()) {
            return new XDDFManualLayout(layout);
        } else {
            return null;
        }
    }
}
