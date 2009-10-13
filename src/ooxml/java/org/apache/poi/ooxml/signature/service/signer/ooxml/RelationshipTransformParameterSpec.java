
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


/*
 * Based on the eID Applet Project code.
 * Original Copyright (C) 2008-2009 FedICT.
 */

package org.apache.poi.ooxml.signature.service.signer.ooxml;

import java.util.LinkedList;
import java.util.List;

import javax.xml.crypto.dsig.spec.TransformParameterSpec;

/**
 * Relationship Transform parameter specification class.
 */
public class RelationshipTransformParameterSpec implements TransformParameterSpec {

    private final List<String> sourceIds;

    /**
     * Main constructor.
     */
    public RelationshipTransformParameterSpec() {
        this.sourceIds = new LinkedList<String>();
    }

    /**
     * Adds a relationship reference for the given source identifier.
     * 
     * @param sourceId
     */
    public void addRelationshipReference(String sourceId) {
        this.sourceIds.add(sourceId);
    }

    List<String> getSourceIds() {
        return this.sourceIds;
    }
}
