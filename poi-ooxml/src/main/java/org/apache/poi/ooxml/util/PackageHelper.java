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

package org.apache.poi.ooxml.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageProperties;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.openxml4j.opc.internal.InvalidZipException;
import org.apache.poi.util.IOUtils;

/**
 * Provides handy methods to work with OOXML packages
 */
public final class PackageHelper {

    /**
     * @param stream The InputStream to read from - which is closed when it is read
     * @return OPCPackage
     * @throws IOException If reading data from the stream fails
     * @throws POIXMLException If the stream is not a valid OPC package
     */
    public static OPCPackage open(InputStream stream) throws IOException {
        return open(stream, true);
    }

    /**
     * @param stream The InputStream to read from
     * @param closeStream whether to close the stream
     * @return OPCPackage
     * @throws IOException If reading data from the stream fails
     * @throws POIXMLException If the stream is not a valid OPC package
     * @since 5.2.0
     */
    public static OPCPackage open(InputStream stream, boolean closeStream) throws IOException {
        try {
            return OPCPackage.open(stream, closeStream);
        } catch (InvalidFormatException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof InvalidZipException ize) {
                throw ize;
            }
            throw new POIXMLException(e);
        } finally {
            if (closeStream) {
                stream.close();
            }
        }
    }

    /**
     * Recursively copy package parts to the destination package
     */
    private static void copy(OPCPackage pkg, PackagePart part, OPCPackage tgt, PackagePart part_tgt) throws OpenXML4JException, IOException {
        PackageRelationshipCollection rels = part.getRelationships();
        if(rels != null) for (PackageRelationship rel : rels) {
            PackagePart p;
            if(rel.getTargetMode() == TargetMode.EXTERNAL){
                part_tgt.addExternalRelationship(rel.getTargetURI().toString(), rel.getRelationshipType(), rel.getId());
                //external relations don't have associated package parts
                continue;
            }
            URI uri = rel.getTargetURI();

            if(uri.getRawFragment() != null) {
                part_tgt.addRelationship(uri, rel.getTargetMode(), rel.getRelationshipType(), rel.getId());
                continue;
            }
            PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
            p = pkg.getPart(relName);
            part_tgt.addRelationship(p.getPartName(), rel.getTargetMode(), rel.getRelationshipType(), rel.getId());

            PackagePart dest;
            if(!tgt.containPart(p.getPartName())){
                dest = tgt.createPart(p.getPartName(), p.getContentType());
                try (
                        InputStream in = p.getInputStream();
                        OutputStream out = dest.getOutputStream()
                ) {
                    IOUtils.copy(in, out);
                }
                copy(pkg, p, tgt, dest);
            }
        }
    }

    /**
     * Copy core package properties
     *
     * @param src source properties
     * @param tgt target properties
     */
    private static void copyProperties(PackageProperties src, PackageProperties tgt) {
        tgt.setCategoryProperty(src.getCategoryProperty());
        tgt.setContentStatusProperty(src.getContentStatusProperty());
        tgt.setContentTypeProperty(src.getContentTypeProperty());
        tgt.setCreatorProperty(src.getCreatorProperty());
        tgt.setDescriptionProperty(src.getDescriptionProperty());
        tgt.setIdentifierProperty(src.getIdentifierProperty());
        tgt.setKeywordsProperty(src.getKeywordsProperty());
        tgt.setLanguageProperty(src.getLanguageProperty());
        tgt.setRevisionProperty(src.getRevisionProperty());
        tgt.setSubjectProperty(src.getSubjectProperty());
        tgt.setTitleProperty(src.getTitleProperty());
        tgt.setVersionProperty(src.getVersionProperty());
    }
}
