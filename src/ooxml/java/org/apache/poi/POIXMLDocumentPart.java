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
package org.apache.poi;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.xmlbeans.XmlOptions;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.POILogFactory;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.*;

/**
 * Represents an entry of a OOXML package.
 *
 * <p>
 * Each POIXMLDocumentPart keeps a reference to the underlying a {@link org.openxml4j.opc.PackagePart}.
 * </p>
 *
 * @author Yegor Kozlov
 */
public class POIXMLDocumentPart {
    private static POILogger logger = POILogFactory.getLogger(POIXMLDocumentPart.class);

    public static XmlOptions DEFAULT_XML_OPTIONS;
    static {
        DEFAULT_XML_OPTIONS = new XmlOptions();
        DEFAULT_XML_OPTIONS.setSaveOuter();
        DEFAULT_XML_OPTIONS.setUseDefaultNamespace();
        DEFAULT_XML_OPTIONS.setSaveAggressiveNamespaces();
    }

    protected PackagePart packagePart;
    protected PackageRelationship packageRel;
    protected POIXMLDocumentPart parent;

    protected List<POIXMLDocumentPart> relations;

    public POIXMLDocumentPart(PackagePart part, PackageRelationship rel){
        relations = new LinkedList<POIXMLDocumentPart>();
        this.packagePart = part;
        this.packageRel = rel;
    }

    /**
     * Provides access to the underlying PackagePart
     *
     * @return the underlying PackagePart
     */
    public PackagePart getPackagePart(){
        return packagePart;
    }

    /**
     * Provides access to the PackageRelationship that identifies this POIXMLDocumentPart
     *
     * @return the PackageRelationship that identifies this POIXMLDocumentPart
     */
    public PackageRelationship getPackageRelationship(){
        return packageRel;
    }

    /**
     * Returns the list of child relations for this POIXMLDocumentPart
     *
     * @return child relations
     */
    public List<POIXMLDocumentPart> getRelations(){
        return relations;
    }

    /**
     * Add a new child POIXMLDocumentPart
     *
     * @param part the child to add
     */
    protected void addRelation(POIXMLDocumentPart part){
        relations.add(part);
    }

    /**
     * Returns the parent POIXMLDocumentPart. All parts except root have not-null parent.
     *
     * @return the parent POIXMLDocumentPart or <code>null</code> for the root element.
     */
    public POIXMLDocumentPart getParent(){
        return parent;
    }

    @Override
    public String toString(){
        return packagePart.toString();
    }

    /**
     * Save the content in the underlying package part.
     * Default implementation is empty meaning that the package part is left unmodified.
     *
     * Sub-classes should override and add logic to marshal the "model" into Ooxml4J.
     *
     * For example, the code saving a generic XML entry may look as follows:
     * <pre><code>
     * protected void commit() throws IOException {
     *   PackagePart part = getPackagePart();
     *   OutputStream out = part.getOutputStream();
     *   XmlObject bean = getXmlBean(); //the "model" which holds changes in memory
     *   bean.save(out, DEFAULT_XML_OPTIONS);
     *   out.close();
     * }
     *  </code></pre>
     *
     */
    protected void commit() throws IOException {

    }

    /**
     * Save changes in the underlying OOXML package.
     */
    protected void save() throws IOException{
        commit();
        for(POIXMLDocumentPart p : relations){
            p.save();
        }
    }

    /**
     * Create a new child POIXMLDocumentPart
     *
     * @param descriptor the part descriptor
     * @param factory the factory that will create an instance of the requested relation
     * @return the created child POIXMLDocumentPart
     */
    protected POIXMLDocumentPart createRelationship(POIXMLRelation descriptor, POIXMLFactory factory){
        return createRelationship(descriptor, factory, -1, false);
    }

    protected POIXMLDocumentPart createRelationship(POIXMLRelation descriptor, POIXMLFactory factory, int idx){
        return createRelationship(descriptor, factory, idx, false);
    }

    /**
     * Create a new child POIXMLDocumentPart
     *
     * @param descriptor the part descriptor
     * @param factory the factory that will create an instance of the requested relation
     * @param idx part number
     * @param noRelation if true, then no relationship is added.
     * @return the created child POIXMLDocumentPart
     */
    protected POIXMLDocumentPart createRelationship(POIXMLRelation descriptor, POIXMLFactory factory, int idx, boolean noRelation){
        try {

            PackagePartName ppName = PackagingURIHelper.createPartName(descriptor.getFileName(idx));
            PackageRelationship rel = null;
            if(!noRelation) rel = packagePart.addRelationship(ppName, TargetMode.INTERNAL, descriptor.getRelation());

            PackagePart part = packagePart.getPackage().createPart(ppName, descriptor.getContentType());
            POIXMLDocumentPart doc = factory.newDocumentPart(descriptor);
            doc.packageRel = rel;
            doc.packagePart = part;
            doc.parent = this;
            doc.onDocumentCreate();
            addRelation(doc);
            return doc;
        } catch (Exception e){
            throw new POIXMLException(e);
        }
    }

    /**
     * Iterate through the underlying PackagePart and create child POIXMLFactory instances
     * using the specified factory
     *
     * @param factory   the factory object that creates POIXMLFactory instances
     */
    protected void read(POIXMLFactory factory) throws OpenXML4JException {
        PackageRelationshipCollection rels = packagePart.getRelationships();
        for (PackageRelationship rel : rels) {
            if(rel.getTargetMode() == TargetMode.INTERNAL){
                PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
                PackagePart p = packagePart.getPackage().getPart(relName);
                if(p == null) {
                    logger.log(POILogger.ERROR, "Skipped invalid entry " + rel.getTargetURI());
                    continue;
                }
                POIXMLDocumentPart childPart = factory.createDocumentPart(rel, p);
                childPart.parent = this;
                childPart.onDocumentRead();
                addRelation(childPart);

                if(p.hasRelationships()) childPart.read(factory);
            }
        }
    }

    /**
     * Fired when a new package part is created
     */
    protected void onDocumentCreate(){

    }

    /**
     * Fired when a package part is read
     */
    protected void onDocumentRead(){

    }
}
