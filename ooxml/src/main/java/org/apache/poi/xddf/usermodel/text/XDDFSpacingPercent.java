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

package org.apache.poi.xddf.usermodel.text;

import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextSpacing;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextSpacingPercent;

@Beta
public class XDDFSpacingPercent extends XDDFSpacing {
    private CTTextSpacingPercent percent;
    private Double scale;

    public XDDFSpacingPercent(double value) {
        this(CTTextSpacing.Factory.newInstance(), CTTextSpacingPercent.Factory.newInstance(), null);
        if (spacing.isSetSpcPts()) {
            spacing.unsetSpcPts();
        }
        spacing.setSpcPct(percent);
        setPercent(value);
    }

    @Internal
    protected XDDFSpacingPercent(CTTextSpacing parent, CTTextSpacingPercent percent, Double scale) {
        super(parent);
        this.percent = percent;
        this.scale = (scale == null) ? 0.001 : scale * 0.001;
    }

    @Override
    public Kind getType() {
        return Kind.PERCENT;
    }

    public double getPercent() {
        return POIXMLUnits.parsePercent(percent.xgetVal()) * scale;
    }

    public void setPercent(double value) {
        percent.setVal((int)(1000 * value));
    }
}
