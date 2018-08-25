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

import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBulletSizePercent;

@Beta
public class XDDFBulletSizePercent implements XDDFBulletSize {
    private CTTextBulletSizePercent percent;
    private Double scale;

    public XDDFBulletSizePercent(double value) {
        this(CTTextBulletSizePercent.Factory.newInstance(), null);
        setPercent(value);
    }

    @Internal
    protected XDDFBulletSizePercent(CTTextBulletSizePercent percent, Double scale) {
        this.percent = percent;
        this.scale = (scale == null) ? 0.001 : scale * 0.001;
    }

    @Internal
    protected CTTextBulletSizePercent getXmlObject() {
        return percent;
    }

    public double getPercent() {
        return percent.getVal() * scale;
    }

    public void setPercent(double value) {
        percent.setVal((int) (1000 * value));
    }
}
