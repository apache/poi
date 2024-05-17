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
package org.apache.poi.ooxml;

import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;

import java.net.URI;

/**
 * Defines a reference relationship. A reference relationship can be internal or external.
 *
 * @since POI 5.3.0
 */
public abstract class ReferenceRelationship {
    private POIXMLDocumentPart container;
    private final String relationshipType;
    private final boolean external;
    private final String id;
    private final URI uri;

    protected ReferenceRelationship(POIXMLDocumentPart container, PackageRelationship packageRelationship) {
        if (packageRelationship == null) {
            throw new IllegalArgumentException("packageRelationship");
        }

        this.container = container;
        this.relationshipType = packageRelationship.getRelationshipType();
        this.uri = packageRelationship.getTargetURI();
        this.external = packageRelationship.getTargetMode() == TargetMode.EXTERNAL;
        this.id = packageRelationship.getId();
    }

    protected ReferenceRelationship(POIXMLDocumentPart container, URI targetUri, boolean isExternal, String relationshipType, String id) {
        if (targetUri == null) {
            throw new NullPointerException("targetUri cannot be null");
        }

        this.container = container;
        this.relationshipType = relationshipType;
        this.uri = targetUri;
        this.id = id;
        this.external = isExternal;
    }

    public POIXMLDocumentPart getContainer() {
        return container;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public boolean isExternal() {
        return external;
    }

    public String getId() {
        return id;
    }

    public URI getUri() {
        return uri;
    }
}
