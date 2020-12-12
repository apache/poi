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

import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.main.CTDashStop;

@Beta
public class XDDFDashStop {
    private CTDashStop stop;

    @Internal
    protected XDDFDashStop(CTDashStop stop) {
        this.stop = stop;
    }

    @Internal
    protected CTDashStop getXmlObject() {
        return stop;
    }

    public int getDashLength() {
        return POIXMLUnits.parsePercent(stop.xgetD());
    }

    public void setDashLength(int length) {
        stop.setD(length);
    }

    public int getSpaceLength() {
        return POIXMLUnits.parsePercent(stop.xgetSp());
    }

    public void setSpaceLength(int length) {
        stop.setSp(length);
    }
}
