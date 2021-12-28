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

package org.apache.poi.xdgf.usermodel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.schemas.office.visio.x2012.main.MasterType;
import com.microsoft.schemas.office.visio.x2012.main.MastersDocument;
import com.microsoft.schemas.office.visio.x2012.main.MastersType;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.Internal;
import org.apache.poi.xdgf.exceptions.XDGFException;
import org.apache.poi.xdgf.xml.XDGFXMLDocumentPart;
import org.apache.xmlbeans.XmlException;

/**
 * A collection of masters (typically stencils) in a Visio document
 */
public class XDGFMasters extends XDGFXMLDocumentPart {

    MastersType _mastersObject;

    // key: id of master
    protected Map<Long, XDGFMaster> _masters = new HashMap<>();

    /**
     * @since POI 3.14-Beta1
     */
    public XDGFMasters(PackagePart part) {
        super(part);
    }

    @Internal
    protected MastersType getXmlObject() {
        return _mastersObject;
    }

    @Override
    protected void onDocumentRead() {
        try {
            try (InputStream stream = getPackagePart().getInputStream()) {
                _mastersObject = MastersDocument.Factory.parse(stream).getMasters();
            } catch (XmlException | IOException e) {
                throw new POIXMLException(e);
            }

            Map<String, MasterType> masterSettings = new HashMap<>();
            for (MasterType master: _mastersObject.getMasterArray()) {
                masterSettings.put(master.getRel().getId(), master);
            }

            // create the masters
            for (RelationPart rp : getRelationParts()) {
                POIXMLDocumentPart part = rp.getDocumentPart();

                String relId = rp.getRelationship().getId();
                MasterType settings = masterSettings.get(relId);

                if (settings == null) {
                    throw new POIXMLException("Master relationship for " + relId + " not found");
                }

                if (!(part instanceof XDGFMasterContents)) {
                    throw new POIXMLException("Unexpected masters relationship for " + relId + ": " + part);
                }

                XDGFMasterContents contents = (XDGFMasterContents)part;
                contents.onDocumentRead();

                XDGFMaster master = new XDGFMaster(settings, contents, _document);
                _masters.put(master.getID(), master);
            }
        } catch (POIXMLException e) {
            throw XDGFException.wrap(this, e);
        }
    }

    public Collection<XDGFMaster> getMastersList() {
        return Collections.unmodifiableCollection(_masters.values());
    }

    public XDGFMaster getMasterById(long masterId) {
        return _masters.get(masterId);
    }
}
