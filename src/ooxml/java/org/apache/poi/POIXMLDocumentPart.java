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
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.xmlbeans.XmlOptions;

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
    private static final POILogger logger = POILogFactory.getLogger(POIXMLDocumentPart.class);

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
    private Map<String,POIXMLDocumentPart> relations = new LinkedHashMap<String,POIXMLDocumentPart>();

    /**
     * Get the PackagePart that is the target of a relationship.
     *
     * @param rel The relationship
     * @param pkg The package to fetch from
     * @return The target part
     * @throws InvalidFormatException
     */
    protected static PackagePart getTargetPart(OPCPackage pkg, PackageRelationship rel)
    throws InvalidFormatException {
        PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
        PackagePart part = pkg.getPart(relName);
        if (part == null) {
            throw new IllegalArgumentException("No part found for relationship " + rel);
        }
        return part;
    }
    /**
     * Counter that provides the amount of incoming relations from other parts
     * to this part.
     */
    private int relationCounter = 0;

    int incrementRelationCounter() {
        relationCounter++;
        return relationCounter;
    }

    int decrementRelationCounter() {
        relationCounter--;
        return relationCounter;
    }

    int getRelationCounter() {
        return relationCounter;
    }

    /**
     * Construct POIXMLDocumentPart representing a "core document" package part.
     */
    public POIXMLDocumentPart(OPCPackage pkg) {
        PackageRelationship coreRel = pkg.getRelationshipsByType(PackageRelationshipTypes.CORE_DOCUMENT).getRelationship(0);

        this.packagePart = pkg.getPart(coreRel);
        this.packageRel = coreRel;
    }

    /**
     * Creates new POIXMLDocumentPart   - called by client code to create new parts from scratch.
     *
     * @see #createRelationship(POIXMLRelation, POIXMLFactory, int, boolean)
     */
    public POIXMLDocumentPart(){
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
        this.packagePart = part;
        this.packageRel = rel;
    }

    /**
     * Creates an POIXMLDocumentPart representing the given package part, relationship and parent
     * Called by {@link #read(POIXMLFactory, java.util.Map)} when reading in an exisiting file.
     *
     * @param parent - Parent part
     * @param part - The package part that holds xml data represenring this sheet.
     * @param rel - the relationship of the given package part
     * @see #read(POIXMLFactory, java.util.Map)
     */
    public POIXMLDocumentPart(POIXMLDocumentPart parent, PackagePart part, PackageRelationship rel){
        this.packagePart = part;
        this.packageRel = rel;
        this.parent = parent;
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
        return Collections.unmodifiableList(new ArrayList<POIXMLDocumentPart>(relations.values()));
    }

    /**
     * Returns the target {@link POIXMLDocumentPart}, where a
     * {@link PackageRelationship} is set from the {@link PackagePart} of this
     * {@link POIXMLDocumentPart} to the {@link PackagePart} of the target
     * {@link POIXMLDocumentPart} with a {@link PackageRelationship#getId()}
     * matching the given parameter value.
     * 
     * @param id
     *            The relation id to look for
     * @return the target part of the relation, or null, if none exists
     */
    public final POIXMLDocumentPart getRelationById(String id) {
        return relations.get(id);
    }

    /**
     * Returns the {@link PackageRelationship#getId()} of the
     * {@link PackageRelationship}, that sources from the {@link PackagePart} of
     * this {@link POIXMLDocumentPart} to the {@link PackagePart} of the given
     * parameter value.
     * 
     * @param part
     *            The {@link POIXMLDocumentPart} for which the according
     *            relation-id shall be found.
     * @return The value of the {@link PackageRelationship#getId()} or null, if
     *         parts are not related.
     */
    public final String getRelationId(POIXMLDocumentPart part) {
        Iterator<Entry<String, POIXMLDocumentPart>> iter = relations.entrySet().iterator();
        while (iter.hasNext())
        {
            Entry<String, POIXMLDocumentPart> entry = iter.next();
            if (entry.getValue() == part) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Add a new child POIXMLDocumentPart
     *
     * @param part the child to add
     */
    public final void addRelation(String id,POIXMLDocumentPart part){
        relations.put(id,part);
        part.incrementRelationCounter();
    }

    /**
     * Remove the relation to the specified part in this package and remove the
     * part, if it is no longer needed.
     */
    protected final void removeRelation(POIXMLDocumentPart part){
        removeRelation(part,true);
    }

    /**
     * Remove the relation to the specified part in this package and remove the
     * part, if it is no longer needed and flag is set to true.
     * 
     * @param part
     *            The related part, to which the relation shall be removed.
     * @param removeUnusedParts
     *            true, if the part shall be removed from the package if not
     *            needed any longer.
     */
    protected final boolean removeRelation(POIXMLDocumentPart part, boolean removeUnusedParts){
        String id = getRelationId(part);
        if (id == null) {
            // part is not related with this POIXMLDocumentPart
            return false;
        }
        /* decrement usage counter */
        part.decrementRelationCounter();
        /* remove packagepart relationship */
        getPackagePart().removeRelationship(id);
        /* remove POIXMLDocument from relations */
        relations.remove(id);

        if (removeUnusedParts) {
            /* if last relation to target part was removed, delete according target part */
            if (part.getRelationCounter() == 0) {
                try {
                    part.onDocumentRemove();
                } catch (IOException e) {
                    throw new POIXMLException(e);
                }
                getPackagePart().getPackage().removePart(part.getPackagePart());
            }
        }
        return true;
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
        for(POIXMLDocumentPart p : relations.values()){
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
            PackagePart part = packagePart.getPackage().createPart(ppName, descriptor.getContentType());
            if(!noRelation) {
                /* only add to relations, if according relationship is being created. */
                rel = packagePart.addRelationship(ppName, TargetMode.INTERNAL, descriptor.getRelation());
            }
            POIXMLDocumentPart doc = factory.newDocumentPart(descriptor);
            doc.packageRel = rel;
            doc.packagePart = part;
            doc.parent = this;
            if(!noRelation) {
                /* only add to relations, if according relationship is being created. */
                addRelation(rel.getId(),doc);
            }
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
                    POIXMLDocumentPart childPart = factory.createDocumentPart(this, rel, p);
                    childPart.parent = this;
                    addRelation(rel.getId(),childPart);
                    if(p != null){
                        context.put(p, childPart);
                        if(p.hasRelationships()) childPart.read(factory, context);
                    }
                }
                else {
                    addRelation(rel.getId(),context.get(p));
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
    protected void onDocumentRead() throws IOException {

    }

    /**
     * Get the PackagePart that is the target of a relationship.
     *
     * @param rel The relationship
     * @return The target part
     * @throws InvalidFormatException
     */
    protected PackagePart getTargetPart(PackageRelationship rel) throws InvalidFormatException {
        return getTargetPart(getPackagePart().getPackage(), rel);
    }

    /**
     * Fired when a package part is about to be removed from the package
     */
    protected void onDocumentRemove() throws IOException {

    }
}
