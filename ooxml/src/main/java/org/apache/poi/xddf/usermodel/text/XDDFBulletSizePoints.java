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
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBulletSizePoint;

@Beta
public class XDDFBulletSizePoints implements XDDFBulletSize {
    private CTTextBulletSizePoint points;

    public XDDFBulletSizePoints(double value) {
        this(CTTextBulletSizePoint.Factory.newInstance());
        setPoints(value);
    }

    @Internal
    protected XDDFBulletSizePoints(CTTextBulletSizePoint points) {
        this.points = points;
    }

    @Internal
    protected CTTextBulletSizePoint getXmlObject() {
        return points;
    }

    public double getPoints() {
        return points.getVal() * 0.01;
    }

    public void setPoints(double value) {
        points.setVal(Math.toIntExact(Math.round(100 * value)));
    }
}
