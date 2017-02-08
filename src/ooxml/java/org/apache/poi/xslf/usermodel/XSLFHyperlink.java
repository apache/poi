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

import java.net.URI;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.sl.usermodel.Hyperlink;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.main.CTHyperlink;

public class XSLFHyperlink implements Hyperlink<XSLFShape,XSLFTextParagraph> {
    final XSLFSheet _sheet;
    final CTHyperlink _link;

    XSLFHyperlink(CTHyperlink link, XSLFSheet sheet){
        _sheet = sheet;
        _link = link;
    }

    @Internal
    public CTHyperlink getXmlObject(){
        return _link;
    }

    @Override
    public void setAddress(String address) {
        linkToUrl(address);
    }

    @Override
    public String getAddress() {
        String id = _link.getId();
        if (id == null || "".equals(id)) {
            return _link.getAction();
        }

        URI targetURI = _sheet.getPackagePart().getRelationship(id).getTargetURI();
        
        return targetURI.toASCIIString();
    }

    @Override
    public String getLabel() {
        return _link.getTooltip();
    }

    @Override
    public void setLabel(String label) {
        _link.setTooltip(label);
    }

    /* (non-Javadoc)
     * @deprecated POI 3.15. Use {@link #getTypeEnum()} instead.
     * Will return a HyperlinkType enum in the future
     */
    @Override
    public int getType() {
        return getTypeEnum().getCode();
    }
    
    @Override
    public HyperlinkType getTypeEnum() {
        String action = _link.getAction();
        if (action == null) {
            action = "";
        }
        if (action.equals("ppaction://hlinksldjump") || action.startsWith("ppaction://hlinkshowjump")) {
            return HyperlinkType.DOCUMENT;
        }
        
        String address = getAddress();
        if (address == null) {
            address = "";
        }
        if (address.startsWith("mailto:")) {
            return HyperlinkType.EMAIL;
        } else {
            return HyperlinkType.URL;
        }
    }

    @Override
    public void linkToEmail(String emailAddress) {
        linkToExternal("mailto:"+emailAddress);
        setLabel(emailAddress);
    }

    @Override
    public void linkToUrl(String url) {
        linkToExternal(url);
        setLabel(url);
    }

    private void linkToExternal(String url) {
        PackagePart thisPP = _sheet.getPackagePart();
        if (_link.isSetId() && !_link.getId().isEmpty()) {
            thisPP.removeRelationship(_link.getId());
        }
        PackageRelationship rel = thisPP.addExternalRelationship(url, XSLFRelation.HYPERLINK.getRelation());
        _link.setId(rel.getId());
        if (_link.isSetAction()) {
            _link.unsetAction();
        }
    }

    @Override
    public void linkToSlide(Slide<XSLFShape,XSLFTextParagraph> slide) {
        PackagePart thisPP = _sheet.getPackagePart();
        PackagePartName otherPPN = ((XSLFSheet)slide).getPackagePart().getPartName();
        if (_link.isSetId() && !_link.getId().isEmpty()) {
            thisPP.removeRelationship(_link.getId());
        }
        PackageRelationship rel =
            thisPP.addRelationship(otherPPN, TargetMode.INTERNAL, XSLFRelation.SLIDE.getRelation());
        _link.setId(rel.getId());
        _link.setAction("ppaction://hlinksldjump");
    }

    @Override
    public void linkToNextSlide() {
        linkToRelativeSlide("nextslide");
    }

    @Override
    public void linkToPreviousSlide() {
        linkToRelativeSlide("previousslide");
    }

    @Override
    public void linkToFirstSlide() {
        linkToRelativeSlide("firstslide");
    }

    @Override
    public void linkToLastSlide() {
        linkToRelativeSlide("lastslide");
    }
    
    private void linkToRelativeSlide(String jump) {
        PackagePart thisPP = _sheet.getPackagePart();
        if (_link.isSetId() && !_link.getId().isEmpty()) {
            thisPP.removeRelationship(_link.getId());
        }
        _link.setId("");
        _link.setAction("ppaction://hlinkshowjump?jump="+jump);
    }
}