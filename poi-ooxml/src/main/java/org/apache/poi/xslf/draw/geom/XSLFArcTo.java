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

package org.apache.poi.xslf.draw.geom;

import org.apache.poi.sl.draw.geom.ArcToCommandIf;
import org.apache.poi.util.Beta;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPath2DArcTo;

/**
 * Wrapper / delegate for XmlBeans custom geometry
 */
@Beta
public class XSLFArcTo implements ArcToCommandIf {
    private final CTPath2DArcTo arc;

    public XSLFArcTo(CTPath2DArcTo arc) {
        this.arc = arc;
    }

    @Override
    public String getHR() {
        return arc.xgetHR().getStringValue();
    }

    @Override
    public void setHR(String hr) {
        arc.setHR(hr);
    }

    @Override
    public String getWR() {
        return arc.xgetHR().getStringValue();
    }

    @Override
    public void setWR(String wr) {
        arc.setWR(wr);
    }

    @Override
    public String getStAng() {
        return arc.xgetStAng().getStringValue();
    }

    @Override
    public void setStAng(String stAng) {
        arc.setStAng(stAng);
    }

    @Override
    public String getSwAng() {
        return arc.xgetSwAng().getStringValue();
    }

    @Override
    public void setSwAng(String swAng) {
        arc.setSwAng(swAng);
    }

}
