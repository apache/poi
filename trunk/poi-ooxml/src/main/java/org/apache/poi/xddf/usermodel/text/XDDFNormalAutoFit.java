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
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextNormalAutofit;

@Beta
public class XDDFNormalAutoFit implements XDDFAutoFit {
    private CTTextNormalAutofit autofit;

    public XDDFNormalAutoFit() {
        this(CTTextNormalAutofit.Factory.newInstance());
    }

    @Internal
    protected XDDFNormalAutoFit(CTTextNormalAutofit autofit) {
        this.autofit = autofit;
    }

    @Internal
    protected CTTextNormalAutofit getXmlObject() {
        return autofit;
    }

    @Override
    public int getFontScale() {
        if (autofit.isSetFontScale()) {
            return POIXMLUnits.parsePercent(autofit.xgetFontScale());
        } else {
            return 100_000;
        }
    }

    public void setFontScale(Integer value) {
        if (value == null) {
            if (autofit.isSetFontScale()) {
                autofit.unsetFontScale();
            }
        } else {
            autofit.setFontScale(value);
        }
    }

    @Override
    public int getLineSpaceReduction() {
        if (autofit.isSetLnSpcReduction()) {
            return POIXMLUnits.parsePercent(autofit.xgetLnSpcReduction());
        } else {
            return 0;
        }
    }

    public void setLineSpaceReduction(Integer value) {
        if (value == null) {
            if (autofit.isSetLnSpcReduction()) {
                autofit.unsetLnSpcReduction();
            }
        } else {
            autofit.setLnSpcReduction(value);
        }
    }
}
