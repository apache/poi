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
import java.util.*;
import java.net.URI;

import org.apache.xmlbeans.XmlOptions;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.*;

/**
 * Represents an entry of a OOXML package.
 *
 * <p>
 * Each POIXMLDocumentPart keeps a reference to the underlying a {@link org.apache.poi.openxml4j.opc.PackagePart}.
 * </p>
 *
 * @author Yegor Kozlov
 */
public class POIXMLDocumentPart {
    private static POILogger logger = POILogFactory.getLogger(POIXMLDocumentPart.class);

    public static final XmlOptions DEFAULT_XML_OPTIONS;
    static {
        DEFAULT_XML_OPTIONS = new XmlOptions();
        DEFAULT_XML_OPTIONS.setSaveOuter();
        DEFAULT_XML_OPTIONS.setUseDefaultNamespace();
        DEFAULT_XML_OPTIONS.setSaveAggressiveNamespaces();
    }

    private PackagePart packagePart;
    private PackageRelationship packageRel;
    private POIXMLDocumentPart parent;
    private List<POIXMLDocumentPart> relations;

    /**
     * Construct POIXMLDocumentPart representing a "core document" package part.
     */
    public POIXMLDocumentPart(OPCPackage pkg) {
        PackageRelationship coreRel = pkg.getRelationshipsByType(
                PackageRelationshipTypes.CORE_DOCUMENT).getRelationship(0);

        this.relations = new LinkedList<POIXMLDocumentPart>();
        this.packagePart = pkg.getPart(coreRel);
        this.packageRel = coreRel;
    }

    /**
     * Creates new POIXMLDocumentPart   - called by client code to create new parts from scratch.
     *
     * @see #createRelationship(POIXMLRelation, POIXMLFactory, int, boolean)
     */
    public POIXMLDocumentPart(){
        this.relations = new LinkedList<POIXMLDocumentPart>();
    }

    /**
     * Creates an POIXMLDocumentPart representing the given package part and relationship.
     * Called by {@link #read(POIXMLFactory, java.util.Map)} when reading in an exisiting file.
     *
     * @param part - The package part that holds xml data represenring this sheet.
     * @param rel - the relationship of the given package part
     * @see #read(POIXMLFactory, java.util.Map) 
     */
    public POIXMLDocumentPart(PackagePart part, PackageRelationship rel){
        this.relations = new LinkedList<POIXMLDocumentPart>();
        this.packagePart = part;
        this.packageRel = rel;
    }
    
    /**
     * When you open something like a theme, call this to
     *  re-base the XML Document onto the core child of the
     *  current core document 
     */
    protected final void rebase(OPCPackage pkg) throws InvalidFormatException {
       PackageRelationshipCollection cores =
          packagePart.getRelationshipsByType(PackageRelationshipTypes.CORE_DOCUMENT);
       if(cores.size() != 1) {
          throw new IllegalStateException(
                "Tried to rebase using " + PackageRelationshipTypes.CORE_DOCUMENT +
                " but found " + cores.size() + " parts of the right type"
          );
       }
       packageRel = cores.getRelationship(0);
       packagePart = POIXMLDocument.getTargetPart(pkg, packageRel);
    }

    /**
     * Provides access to the underlying PackagePart
     *
     * @return the underlying PackagePart
     */
    public final PackagePart getPackagePart(){
        return packagePart;
    }

    /**
     * Provides access to the PackageRelationship that identifies this POIXMLDocumentPart
     *
     * @return the PackageRelationship that identifies this POIXMLDocumentPart
     */
    public final PackageRelationship getPackageRelationship(){
        return packageRel;
    }

    /**
     * Returns the list of child relations for this POIXMLDocumentPart
     *
     * @return child relations
     */
    public final List<POIXMLDocumentPart> getRelations(){
        return relations;
    }

    /**
     * Add a new child POIXMLDocumentPart
     *
     * @param part the child to add
     */
    protected final void addRelation(POIXMLDocumentPart part){
        relations.add(part);
    }

    /**
     * Remove the specified part in this package.
     */
    public final void removeRelation(POIXMLDocumentPart part){
        getPackagePart().removeRelationship(part.getPackageRelationship().getId());
        getPackagePart().getPackage().removePart(part.getPackagePart());
        relations.remove(part);
    }

    /**
     * Returns the parent POIXMLDocumentPart. All parts except root have not-null parent.
     *
     * @return the parent POIXMLDocumentPart or <code>null</code> for the root element.
     */
    public final POIXMLDocumentPart getParent(){
        return parent;
    }

    @Override
    public String toString(){
        return packagePart == null ? null : packagePart.toString();
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
     * Recursively fires {@link #commit()} for each package part
     *
     * @param alreadySaved    context set containing already visited nodes
     */
    protected final void onSave(Set<PackagePart> alreadySaved) throws IOException{
    	commit();
    	alreadySaved.add(this.getPackagePart());
    	for(POIXMLDocumentPart p : relations){
            if (!alreadySaved.contains(p.getPackagePart())) {
    			p.onSave(alreadySaved);
    		}
    	}
    }

    /**
     * Create a new child POIXMLDocumentPart
     *
     * @param descriptor the part descriptor
     * @param factory the factory that will create an instance of the requested relation
     * @return the created child POIXMLDocumentPart
     */
    public final POIXMLDocumentPart createRelationship(POIXMLRelation descriptor, POIXMLFactory factory){
        return createRelationship(descriptor, factory, -1, false);
    }

    public final POIXMLDocumentPart createRelationship(POIXMLRelation descriptor, POIXMLFactory factory, int idx){
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
    protected final POIXMLDocumentPart createRelationship(POIXMLRelation descriptor, POIXMLFactory factory, int idx, boolean noRelation){
        try {
            PackagePartName ppName = PackagingURIHelper.createPartName(descriptor.getFileName(idx));
            PackageRelationship rel = null;
            if(!noRelation) {
               rel = packagePart.addRelationship(ppName, TargetMode.INTERNAL, descriptor.getRelation());
            }
            PackagePart part = packagePart.getPackage().createPart(ppName, descriptor.getContentType());
            POIXMLDocumentPart doc = factory.newDocumentPart(descriptor);
            doc.packageRel = rel;
            doc.packagePart = part;
            doc.parent = this;
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
     * @param context   context map containing already visited noted keyed by targetURI
     */
    protected void read(POIXMLFactory factory, Map<PackagePart, POIXMLDocumentPart> context) throws OpenXML4JException {
    	PackageRelationshipCollection rels = packagePart.getRelationships();
    	for (PackageRelationship rel : rels) {
    		if(rel.getTargetMode() == TargetMode.INTERNAL){
                URI uri = rel.getTargetURI();

                PackagePart p;
                if(uri.getRawFragment() != null) {
                    /*
                     * For internal references (e.g. '#Sheet1!A1') the package part is null
                     */
                    p = null;
                } else {
                    PackagePartName relName = PackagingURIHelper.createPartName(uri);
                    p = packagePart.getPackage().getPart(relName);
                    if(p == null) {
                        logger.log(POILogger.ERROR, "Skipped invalid entry " + rel.getTargetURI());
                        continue;
                    }
                }

                if (!context.containsKey(p)) {
    				POIXMLDocumentPart childPart = factory.createDocumentPart(rel, p);
    				childPart.parent = this;
    				addRelation(childPart);
                    if(p != null){
                        context.put(p, childPart);
                        if(p.hasRelationships()) childPart.read(factory, context);
                    }
    			}
    			else {
    				addRelation(context.get(p));
    			}
    		}
    	}
    }

    /**
     * Fired when a new package part is created
     */
    protected void onDocumentCreate() throws IOException {

    }

    /**
     * Fired when a package part is read
     */
    protected void onDocumentRead() throws IOException{

    }
}
