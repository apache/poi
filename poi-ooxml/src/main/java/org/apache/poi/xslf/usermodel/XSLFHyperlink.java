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

import org.apache.logging.log4j.Logger;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.logging.PoiLogManager;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLDocumentPart.RelationPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.sl.usermodel.Hyperlink;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.main.CTHyperlink;

public class XSLFHyperlink implements Hyperlink<XSLFShape,XSLFTextParagraph> {
    private static final Logger LOG = PoiLogManager.getLogger(XSLFHyperlink.class);

    private final XSLFSheet _sheet;
    private final CTHyperlink _link;

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
        final String id = _link.getId();
        if (id == null || id.isEmpty()) {
            return _link.getAction();
        }

        final PackageRelationship rel = _sheet.getPackagePart().getRelationship(id);
        if (rel == null) {
            return null;
        }

        final URI targetURI = rel.getTargetURI();
        return (targetURI == null) ? null : targetURI.toASCIIString();
    }

    @Override
    public String getLabel() {
        return _link.getTooltip();
    }

    @Override
    public void setLabel(String label) {
        _link.setTooltip(label);
    }

    @Override
    public HyperlinkType getType() {
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
        if (_link.isSetId() && !_link.getId().isEmpty()) {
            _sheet.getPackagePart().removeRelationship(_link.getId());
        }

        RelationPart rp = _sheet.addRelation(null, XSLFRelation.SLIDE, (XSLFSheet) slide);
        _link.setId(rp.getRelationship().getId());
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

    /**
     * Points this link, whose xml was copied from {@code src}, to the same target from this sheet.
     * The copied action and label are kept: a link without a relation (e.g. a media, macro or
     * relative slide jump action) needs nothing else, an external relation is recreated with the
     * same type and target, and a link to a slide gets a relation to that slide.
     */
    void copy(XSLFHyperlink src) {
        final String idSrc = src._link.getId();
        if (idSrc == null || idSrc.isEmpty()) {
            return;
        }

        // the copied id is a relation of the source sheet, not of this one
        _link.setId("");

        final PackageRelationship rel = src._sheet.getPackagePart().getRelationship(idSrc);
        if (rel != null && rel.getTargetMode() == TargetMode.EXTERNAL) {
            PackageRelationship copy = _sheet.getPackagePart().addExternalRelationship(
                rel.getTargetURI().toString(), rel.getRelationshipType());
            _link.setId(copy.getId());
            return;
        }

        // link to slide - absolute reference
        // this is kind of a hack, as we might link to pages not yet imported,
        // but the underlying implementation is based only on package part names,
        // so this actually works ...
        final POIXMLDocumentPart pp = (rel == null) ? null : src._sheet.getRelationById(idSrc);
        if (pp instanceof XSLFSlide) {
            RelationPart rp = _sheet.addRelation(null, XSLFRelation.SLIDE, pp);
            _link.setId(rp.getRelationship().getId());
            return;
        }

        // the target can't be resolved, so don't leave an action pointing nowhere
        LOG.atWarn().log("dropping the action of a copied hyperlink, as its relation {} can't be copied", idSrc);
        if (_link.isSetAction()) {
            _link.unsetAction();
        }
    }

    private void linkToRelativeSlide(String jump) {
        PackagePart thisPP = _sheet.getPackagePart();
        if (_link.isSetId() && !_link.getId().isEmpty()) {
            thisPP.removeRelationship(_link.getId());
        }
        _link.setId("");
        _link.setAction((jump.startsWith("ppaction") ? "" : "ppaction://hlinkshowjump?jump=") + jump);
    }
}
