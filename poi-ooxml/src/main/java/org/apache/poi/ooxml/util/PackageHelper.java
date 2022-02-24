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
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Removal;

/**
 * Provides handy methods to work with OOXML packages
 */
public final class PackageHelper {

    public static OPCPackage open(InputStream is) throws IOException {
        return open(is, false);
    }

    /**
     * @param stream The InputStream to read from
     * @param closeStream whether to close the stream (default is false)
     * @since POI 5.2.0
     * @return OPCPackage
     * @throws IOException If reading data from the stream fails
     */
    public static OPCPackage open(InputStream stream, boolean closeStream) throws IOException {
        try {
            return OPCPackage.open(stream);
        } catch (InvalidFormatException e){
            throw new POIXMLException(e);
        } finally {
            if (closeStream) {
                stream.close();
            }
        }
    }

    /**
     * Clone the specified package.
     *
     * @param   pkg   the package to clone
     * @param   file  the destination file
     * @return  the cloned package
     * @deprecated this method is not used internally and creates temp files that are not well handled
     */
    @Deprecated
    @Removal(version = "6.0.0")
    public static OPCPackage clone(OPCPackage pkg, File file) throws OpenXML4JException, IOException {

        String path = file.getAbsolutePath();

        try (OPCPackage dest = OPCPackage.create(path)) {
            PackageRelationshipCollection rels = pkg.getRelationships();
            for (PackageRelationship rel : rels) {
                PackagePart part = pkg.getPart(rel);
                PackagePart part_tgt;
                if (rel.getRelationshipType().equals(PackageRelationshipTypes.CORE_PROPERTIES)) {
                    copyProperties(pkg.getPackageProperties(), dest.getPackageProperties());
                    continue;
                }
                dest.addRelationship(part.getPartName(), rel.getTargetMode(), rel.getRelationshipType());
                part_tgt = dest.createPart(part.getPartName(), part.getContentType());

                try (
                        InputStream in = part.getInputStream();
                        OutputStream out = part_tgt.getOutputStream()
                ) {
                    IOUtils.copy(in, out);
                }

                if (part.hasRelationships()) {
                    copy(pkg, part, dest, part_tgt);
                }
            }
        }

        //the temp file will be deleted when JVM terminates
        new File(path).deleteOnExit();
        return OPCPackage.open(path);
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
