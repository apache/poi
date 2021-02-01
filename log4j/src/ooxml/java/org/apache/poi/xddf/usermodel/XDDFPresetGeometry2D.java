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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D;

@Beta
public class XDDFPresetGeometry2D {
    private CTPresetGeometry2D geometry;

    protected XDDFPresetGeometry2D(CTPresetGeometry2D geometry) {
        this.geometry = geometry;
    }

    @Internal
    protected CTPresetGeometry2D getXmlObject() {
        return geometry;
    }

    public PresetGeometry getGeometry() {
        return PresetGeometry.valueOf(geometry.getPrst());
    }

    public void setGeometry(PresetGeometry preset) {
        geometry.setPrst(preset.underlying);
    }

    public XDDFGeometryGuide addAdjustValue() {
        if (!geometry.isSetAvLst()) {
            geometry.addNewAvLst();
        }
        return new XDDFGeometryGuide(geometry.getAvLst().addNewGd());
    }

    public XDDFGeometryGuide insertAdjustValue(int index) {
        if (!geometry.isSetAvLst()) {
            geometry.addNewAvLst();
        }
        return new XDDFGeometryGuide(geometry.getAvLst().insertNewGd(index));
    }

    public void removeAdjustValue(int index) {
        if (geometry.isSetAvLst()) {
            geometry.getAvLst().removeGd(index);
        }
    }

    public XDDFGeometryGuide getAdjustValue(int index) {
        if (geometry.isSetAvLst()) {
            return new XDDFGeometryGuide(geometry.getAvLst().getGdArray(index));
        } else {
            return null;
        }
    }

    public List<XDDFGeometryGuide> getAdjustValues() {
        if (geometry.isSetAvLst()) {
            return Collections.unmodifiableList(geometry
                .getAvLst()
                .getGdList()
                .stream()
                .map(guide -> new XDDFGeometryGuide(guide))
                .collect(Collectors.toList()));
        } else {
            return Collections.emptyList();
        }
    }
}
