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
package org.apache.poi.xslf.usermodel;

import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.main.CTHyperlink;

import java.net.URI;

/**
 * @author Yegor Kozlov
 */
public class XSLFHyperlink {
    final XSLFTextRun _r;
    final CTHyperlink _link;

    XSLFHyperlink(CTHyperlink link, XSLFTextRun r){
        _r = r;
        _link = link;
    }

    @Internal
    public CTHyperlink getXmlObject(){
        return _link;
    }

    public void setAddress(String address){
        XSLFSheet sheet = _r.getParentParagraph().getParentShape().getSheet();
        PackageRelationship rel =
                sheet.getPackagePart().
                        addExternalRelationship(address, XSLFRelation.HYPERLINK.getRelation());
        _link.setId(rel.getId());

    }

    public void setAddress(XSLFSlide slide){
        XSLFSheet sheet = _r.getParentParagraph().getParentShape().getSheet();
        PackageRelationship rel =
                sheet.getPackagePart().
                        addRelationship(slide.getPackagePart().getPartName(),
                                TargetMode.INTERNAL,
                                XSLFRelation.SLIDE.getRelation());
        _link.setId(rel.getId());
        _link.setAction("ppaction://hlinksldjump");
    }

    @Internal
    public URI getTargetURI(){
        XSLFSheet sheet = _r.getParentParagraph().getParentShape().getSheet();
        String id = _link.getId();
        return sheet.getPackagePart().getRelationship(id).getTargetURI();
    }
}
