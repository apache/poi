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
import org.apache.poi.util.Units;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextTabStop;

@Beta
public class XDDFTabStop {
    private CTTextTabStop stop;

    @Internal
    protected XDDFTabStop(CTTextTabStop stop) {
        this.stop = stop;
    }

    @Internal
    protected CTTextTabStop getXmlObject() {
        return stop;
    }

    public TabAlignment getAlignment() {
        if (stop.isSetAlgn()) {
            return TabAlignment.valueOf(stop.getAlgn());
        } else {
            return null;
        }
    }

    public void setAlignment(TabAlignment align) {
        if (align == null) {
            if (stop.isSetAlgn()) {
                stop.unsetAlgn();
            }
        } else {
            stop.setAlgn(align.underlying);
        }
    }

    public Double getPosition() {
        if (stop.isSetPos()) {
            return Units.toPoints(POIXMLUnits.parseLength(stop.xgetPos()));
        } else {
            return null;
        }
    }

    public void setPosition(Double position) {
        if (position == null) {
            if (stop.isSetPos()) {
                stop.unsetPos();
            }
        } else {
            stop.setPos(Units.toEMU(position));
        }
    }
}
