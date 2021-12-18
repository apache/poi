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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.exceptions.PartAlreadyExistsException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.xddf.usermodel.chart.XDDFChart;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRelation;

/**
 * Represents an entry of a OOXML package.
 * <p>
 * Each POIXMLDocumentPart keeps a reference to the underlying a {@link PackagePart}.
 * </p>
 */
public class POIXMLDocumentPart {
    private static final Logger LOG = LogManager.getLogger(POIXMLDocumentPart.class);

    private String coreDocumentRel = PackageRelationshipTypes.CORE_DOCUMENT;
    private PackagePart packagePart;
    private POIXMLDocumentPart parent;
    private final Map<String, RelationPart> relations = new LinkedHashMap<>();
    private boolean isCommitted = false;

    /**
     * to check whether embedded part is already committed
     *
     * @return return true if embedded part is committed
     * @since 4.1.2
     */
    public boolean isCommitted() {
        return isCommitted;
    }

    /**
     * setter method to set embedded part is committed
     *
     * @param isCommitted boolean value
     */
    public void setCommitted(boolean isCommitted) {
        this.isCommitted = isCommitted;
    }

    /**
     * The RelationPart is a cached relationship between the document, which contains the RelationPart,
     * and one of its referenced child document parts.
     * The child document parts may only belong to one parent, but it's often referenced by other
     * parents too, having varying {@link PackageRelationship#getId() relationship ids} pointing to it.
     */
    public static class RelationPart {
        private final PackageRelationship relationship;
        private final POIXMLDocumentPart documentPart;

        RelationPart(PackageRelationship relationship, POIXMLDocumentPart documentPart) {
            this.relationship = relationship;
            this.documentPart = documentPart;
        }

        /**
         * @return the cached relationship, which uniquely identifies this child document part within the parent
         */
        public PackageRelationship getRelationship() {
            return relationship;
        }

        /**
         * @param <T> the cast of the caller to a document sub class
         * @return the child document part
         */
        @SuppressWarnings("unchecked")
        public <T extends POIXMLDocumentPart> T getDocumentPart() {
            return (T) documentPart;
        }
    }

    /**
     * Counter that provides the amount of incoming relations from other parts
     * to this part.
     */
    private int relationCounter;

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
     *
     * @param pkg the OPCPackage containing this document
     */
    public POIXMLDocumentPart(OPCPackage pkg) {
        this(pkg, PackageRelationshipTypes.CORE_DOCUMENT);
    }

    /**
     * Construct POIXMLDocumentPart representing a custom "core document" package part.
     *
     * @param pkg             the OPCPackage containing this document
     * @param coreDocumentRel the relation type of this document
     */
    public POIXMLDocumentPart(OPCPackage pkg, String coreDocumentRel) {
        this(getPartFromOPCPackage(pkg, coreDocumentRel));
        this.coreDocumentRel = coreDocumentRel;
    }

    /**
     * Creates new POIXMLDocumentPart   - called by client code to create new parts from scratch.
     *
     * @see #createRelationship(POIXMLRelation, POIXMLFactory, int, boolean)
     */
    public POIXMLDocumentPart() {
    }

    /**
     * Creates an POIXMLDocumentPart representing the given package part and relationship.
     * Called by {@link #read(POIXMLFactory, Map)} when reading in an existing file.
     *
     * @param part - The package part that holds xml data representing this sheet.
     * @see #read(POIXMLFactory, Map)
     * @since POI 3.14-Beta1
     */
    public POIXMLDocumentPart(PackagePart part) {
        this(null, part);
    }

    /**
     * Creates an POIXMLDocumentPart representing the given package part, relationship and parent
     * Called by {@link #read(POIXMLFactory, Map)} when reading in an existing file.
     *
     * @param parent - Parent part
     * @param part   - The package part that holds xml data representing this sheet.
     * @see #read(POIXMLFactory, Map)
     * @since POI 3.14-Beta1
     */
    public POIXMLDocumentPart(POIXMLDocumentPart parent, PackagePart part) {
        this.packagePart = part;
        this.parent = parent;
    }

    /**
     * When you open something like a theme, call this to
     * re-base the XML Document onto the core child of the
     * current core document
     *
     * @param pkg the package to be rebased
     * @throws InvalidFormatException if there was an error in the core document relation
     * @throws IllegalStateException  if there are more than one core document relations
     */
    protected final void rebase(OPCPackage pkg) throws InvalidFormatException {
        // TODO: check why pkg parameter is not used ???
        PackageRelationshipCollection cores =
                packagePart.getRelationshipsByType(coreDocumentRel);
        if (cores.size() != 1) {
            throw new IllegalStateException(
                    "Tried to rebase using " + coreDocumentRel +
                            " but found " + cores.size() + " parts of the right type"
            );
        }
        packagePart = packagePart.getRelatedPart(cores.getRelationship(0));
    }

    /**
     * Provides access to the underlying PackagePart
     *
     * @return the underlying PackagePart
     */
    public final PackagePart getPackagePart() {
        return packagePart;
    }

    /**
     * Returns the list of child relations for this POIXMLDocumentPart
     *
     * @return child relations
     */
    public final List<POIXMLDocumentPart> getRelations() {
        List<POIXMLDocumentPart> l = new ArrayList<>();
        for (RelationPart rp : relations.values()) {
            l.add(rp.getDocumentPart());
        }
        return Collections.unmodifiableList(l);
    }

    /**
     * Returns the list of child relations for this POIXMLDocumentPart
     *
     * @return child relations
     */
    public final List<RelationPart> getRelationParts() {
        List<RelationPart> l = new ArrayList<>(relations.values());
        return Collections.unmodifiableList(l);
    }

    /**
     * Returns the target POIXMLDocumentPart, where a
     * {@link PackageRelationship} is set from the {@link PackagePart} of this
     * POIXMLDocumentPart to the {@link PackagePart} of the target
     * POIXMLDocumentPart with a {@link PackageRelationship#getId()}
     * matching the given parameter value.
     *
     * @param id The relation id to look for
     * @return the target part of the relation, or null, if none exists
     */
    public final POIXMLDocumentPart getRelationById(String id) {
        RelationPart rp = getRelationPartById(id);
        return (rp == null) ? null : rp.getDocumentPart();
    }

    /**
     * Returns the target {@link RelationPart}, where a
     * {@link PackageRelationship} is set from the {@link PackagePart} of this
     * POIXMLDocumentPart to the {@link PackagePart} of the target
     * POIXMLDocumentPart with a {@link PackageRelationship#getId()}
     * matching the given parameter value.
     *
     * @param id The relation id to look for
     * @return the target relation part, or null, if none exists
     * @since 4.0.0
     */
    public final RelationPart getRelationPartById(String id) {
        return relations.get(id);
    }

    /**
     * Returns the first {@link PackageRelationship#getId()} of the
     * {@link PackageRelationship}, that sources from the {@link PackagePart} of
     * this POIXMLDocumentPart to the {@link PackagePart} of the given
     * parameter value.
     * <p>
     * There can be multiple references to the given POIXMLDocumentPart
     * and only the first in the order of creation is returned.
     *
     * @param part The POIXMLDocumentPart for which the according
     *             relation-id shall be found.
     * @return The value of the {@link PackageRelationship#getId()} or null, if
     * parts are not related.
     */
    public final String getRelationId(POIXMLDocumentPart part) {
        for (RelationPart rp : relations.values()) {
            if (rp.getDocumentPart() == part) {
                return rp.getRelationship().getId();
            }
        }
        return null;
    }

    /**
     * Add a new child POIXMLDocumentPart
     *
     * @param relId            the preferred relation id, when null the next free relation id will be used
     * @param relationshipType the package relationship type
     * @param part             the child to add
     * @return the new RelationPart
     * @since 3.14-Beta1
     */
    public final RelationPart addRelation(String relId, POIXMLRelation relationshipType, POIXMLDocumentPart part) {
        PackageRelationship pr = this.packagePart.findExistingRelation(part.getPackagePart());
        if (pr == null) {
            PackagePartName ppn = part.getPackagePart().getPartName();
            String relType = relationshipType.getRelation();
            pr = packagePart.addRelationship(ppn, TargetMode.INTERNAL, relType, relId);
        }
        addRelation(pr, part);
        return new RelationPart(pr, part);
    }

    /**
     * Add a new child POIXMLDocumentPart
     *
     * @param pr   the relationship of the child
     * @param part the child to add
     */
    private void addRelation(PackageRelationship pr, POIXMLDocumentPart part) {
        relations.put(pr.getId(), new RelationPart(pr, part));
        part.incrementRelationCounter();

    }

    /**
     * Remove the relation to the specified part in this package and remove the
     * part, if it is no longer needed.
     * <p>
     * If there are multiple relationships to the same part, this will only
     * remove the first relationship in the order of creation. The removal
     * via the part id ({@link #removeRelation(String)} is preferred.
     *
     * @param part the part which relation is to be removed from this document
     */
    protected final void removeRelation(POIXMLDocumentPart part) {
        removeRelation(part, true);
    }

    /**
     * Remove the relation to the specified part in this package and remove the
     * part, if it is no longer needed and flag is set to true.
     * <p>
     * If there are multiple relationships to the same part, this will only
     * remove the first relationship in the order of creation. The removal
     * via the part id ({@link #removeRelation(String, boolean)} is preferred.
     *
     * @param part              The related part, to which the relation shall be removed.
     * @param removeUnusedParts true, if the part shall be removed from the package if not
     *                          needed any longer.
     * @return true, if the relation was removed
     */
    protected final boolean removeRelation(POIXMLDocumentPart part, boolean removeUnusedParts) {
        String id = getRelationId(part);
        return removeRelation(id, removeUnusedParts);
    }

    /**
     * Remove the relation to the specified part in this package and remove the
     * part, if it is no longer needed.
     * <p>
     * If there are multiple relationships to the same part, this will only
     * remove the first relationship in the order of creation. The removal
     * via the part id ({@link #removeRelation(POIXMLDocumentPart)} is preferred.
     *
     * @param partId the part id which relation is to be removed from this document
     * @since 4.0.0
     */
    protected final void removeRelation(String partId) {
        removeRelation(partId, true);
    }

    /**
     * Remove the relation to the specified part in this package and remove the
     * part, if it is no longer needed and flag is set to true.<p>
     *
     * @param partId            The related part id, to which the relation shall be removed.
     * @param removeUnusedParts true, if the part shall be removed from the package if not
     *                          needed any longer.
     * @return true, if the relation was removed
     * @since 4.0.0
     */
    private boolean removeRelation(String partId, boolean removeUnusedParts) {
        RelationPart rp = relations.get(partId);
        if (rp == null) {
            // part is not related with this POIXMLDocumentPart
            return false;
        }
        POIXMLDocumentPart part = rp.getDocumentPart();
        /* decrement usage counter */
        part.decrementRelationCounter();
        /* remove packagepart relationship */
        getPackagePart().removeRelationship(partId);
        /* remove POIXMLDocument from relations */
        relations.remove(partId);

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
     * @return the parent POIXMLDocumentPart or {@code null} for the root element.
     */
    public final POIXMLDocumentPart getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return packagePart == null ? "" : packagePart.toString();
    }

    /**
     * Save the content in the underlying package part.
     * Default implementation is empty meaning that the package part is left unmodified.
     * <p>
     * Sub-classes should override and add logic to marshal the "model" into Ooxml4J.
     * <p>
     * For example, the code saving a generic XML entry may look as follows:
     * <pre>
     * protected void commit() throws IOException {
     *   PackagePart part = getPackagePart();
     *   try (OutputStream out = part.getOutputStream()) {
     *     XmlObject bean = getXmlBean(); //the "model" which holds changes in memory
     *     bean.save(out, DEFAULT_XML_OPTIONS);
     *   }
     * }
     * </pre>
     *
     * @throws IOException a subclass may throw an IOException if the changes can't be committed
     */
    protected void commit() throws IOException {

    }

    /**
     * Save changes in the underlying OOXML package.
     * Recursively fires {@link #commit()} for each package part
     *
     * @param alreadySaved context set containing already visited nodes
     * @throws IOException a related part may throw an IOException if the changes can't be saved
     */
    protected final void onSave(Set<PackagePart> alreadySaved) throws IOException {
        //if part is already committed then return
        if (this.isCommitted) {
            return;
        }

        // this usually clears out previous content in the part...
        prepareForCommit();

        commit();
        alreadySaved.add(this.getPackagePart());
        for (RelationPart rp : relations.values()) {
            POIXMLDocumentPart p = rp.getDocumentPart();
            if (!alreadySaved.contains(p.getPackagePart())) {
                p.onSave(alreadySaved);
            }
        }
    }

    /**
     * Ensure that a memory based package part does not have lingering data from previous
     * commit() calls.
     * <p>
     * Note: This is overwritten for some objects, as *PictureData seem to store the actual content
     * in the part directly without keeping a copy like all others therefore we need to handle them differently.
     */
    protected void prepareForCommit() {
        PackagePart part = this.getPackagePart();
        if (part != null) {
            part.clear();
        }
    }

    /**
     * Create a new child POIXMLDocumentPart
     *
     * @param descriptor the part descriptor
     * @param factory    the factory that will create an instance of the requested relation
     * @return the created child POIXMLDocumentPart
     * @throws PartAlreadyExistsException If rule M1.12 is not verified : Packages shall not contain
     *                                    equivalent part names and package implementers shall neither
     *                                    create nor recognize packages with equivalent part names.
     */
    public final POIXMLDocumentPart createRelationship(POIXMLRelation descriptor, POIXMLFactory factory) {
        return createRelationship(descriptor, factory, -1, false).getDocumentPart();
    }

    /**
     * Create a new child POIXMLDocumentPart
     *
     * @param descriptor the part descriptor
     * @param factory    the factory that will create an instance of the requested relation
     * @param idx        part number
     * @return the created child POIXMLDocumentPart
     * @throws PartAlreadyExistsException If rule M1.12 is not verified : Packages shall not contain
     *                                    equivalent part names and package implementers shall neither
     *                                    create nor recognize packages with equivalent part names.
     */
    public final POIXMLDocumentPart createRelationship(POIXMLRelation descriptor, POIXMLFactory factory, int idx) {
        return createRelationship(descriptor, factory, idx, false).getDocumentPart();
    }

    /**
     * Identifies the next available part number for a part of the given type,
     * if possible, otherwise -1 if none are available.
     * The found (valid) index can then be safely given to
     * {@link #createRelationship(POIXMLRelation, POIXMLFactory, int)} or
     * {@link #createRelationship(POIXMLRelation, POIXMLFactory, int, boolean)}
     * without naming clashes.
     * If parts with other types are already claiming a name for this relationship
     * type (eg a {@link XSSFRelation#CHART} using the drawing part namespace
     * normally used by {@link XSSFRelation#DRAWINGS}), those will be considered
     * when finding the next spare number.
     *
     * @param descriptor The relationship type to find the part number for
     * @param minIdx     The minimum free index to assign, use -1 for any
     * @return The next free part number, or -1 if none available
     */
    @Internal
    public final int getNextPartNumber(POIXMLRelation descriptor, int minIdx) {
        OPCPackage pkg = packagePart.getPackage();

        try {
            String name = descriptor.getDefaultFileName();
            if (name.equals(descriptor.getFileName(9999))) {
                // Non-index based, check if default is free
                PackagePartName ppName = PackagingURIHelper.createPartName(name);
                if (pkg.containPart(ppName)) {
                    // Default name already taken, not index based, nothing free
                    return -1;
                } else {
                    // Default name free
                    return 0;
                }
            }

            // Default to searching from 1, unless they asked for 0+
            int idx = (minIdx < 0) ? 1 : minIdx;
            int maxIdx = minIdx + pkg.getParts().size();
            while (idx <= maxIdx) {
                name = descriptor.getFileName(idx);
                PackagePartName ppName = PackagingURIHelper.createPartName(name);
                if (!pkg.containPart(ppName)) {
                    return idx;
                }
                idx++;
            }
        } catch (InvalidFormatException e) {
            // Give a general wrapped exception for the problem
            throw new POIXMLException(e);
        }
        return -1;
    }

    /**
     * Create a new child POIXMLDocumentPart
     *
     * @param descriptor the part descriptor
     * @param factory    the factory that will create an instance of the requested relation
     * @param idx        part number
     * @param noRelation if true, then no relationship is added.
     * @return the created child POIXMLDocumentPart
     * @throws PartAlreadyExistsException If rule M1.12 is not verified : Packages shall not contain
     *                                    equivalent part names and package implementers shall neither
     *                                    create nor recognize packages with equivalent part names.
     */
    public final RelationPart createRelationship(POIXMLRelation descriptor, POIXMLFactory factory, int idx, boolean noRelation) {
        try {
            PackagePartName ppName = PackagingURIHelper.createPartName(descriptor.getFileName(idx));
            PackageRelationship rel = null;
            PackagePart part = packagePart.getPackage().createPart(ppName, descriptor.getContentType());
            if (!noRelation) {
                /* only add to relations, if according relationship is being created. */
                rel = packagePart.addRelationship(ppName, TargetMode.INTERNAL, descriptor.getRelation());
            }
            POIXMLDocumentPart doc = factory.newDocumentPart(descriptor);
            doc.packagePart = part;
            doc.parent = this;
            if (!noRelation) {
                /* only add to relations, if according relationship is being created. */
                addRelation(rel, doc);
            }

            return new RelationPart(rel, doc);
        } catch (PartAlreadyExistsException pae) {
            // Return the specific exception so the user knows
            //  that the name is already taken
            throw pae;
        } catch (Exception e) {
            // Give a general wrapped exception for the problem
            throw new POIXMLException(e);
        }
    }

    /**
     * Iterate through the underlying PackagePart and create child POIXMLFactory instances
     * using the specified factory
     *
     * @param factory the factory object that creates POIXMLFactory instances
     * @param context context map containing already visited noted keyed by targetURI
     * @throws OpenXML4JException thrown when a related part can't be read
     */
    protected void read(POIXMLFactory factory, Map<PackagePart, POIXMLDocumentPart> context) throws OpenXML4JException {
        PackagePart pp = getPackagePart();

        if (pp.getContentType().equals(XWPFRelation.GLOSSARY_DOCUMENT.getContentType())) {
            LOG.atWarn().log("POI does not currently support template.main+xml (glossary) parts.  " +
                    "Skipping this part for now.");
            return;
        }

        // add mapping a second time, in case of initial caller hasn't done so
        POIXMLDocumentPart otherChild = context.put(pp, this);
        if (otherChild != null && otherChild != this) {
            throw new POIXMLException("Unique PackagePart-POIXMLDocumentPart relation broken!");
        }

        if (!pp.hasRelationships()) return;

        PackageRelationshipCollection rels = packagePart.getRelationships();
        List<POIXMLDocumentPart> readLater = new ArrayList<>();

        // scan breadth-first, so parent-relations are hopefully the shallowest element
        for (PackageRelationship rel : rels) {
            if (rel.getTargetMode() == TargetMode.INTERNAL) {
                URI uri = rel.getTargetURI();

                // check for internal references (e.g. '#Sheet1!A1')
                PackagePartName relName;
                if (uri.getRawFragment() != null) {
                    relName = PackagingURIHelper.createPartName(uri.getPath());
                } else {
                    relName = PackagingURIHelper.createPartName(uri);
                }

                final PackagePart p = packagePart.getPackage().getPart(relName);
                if (p == null) {
                    LOG.atError().log("Skipped invalid entry {}", rel.getTargetURI());
                    continue;
                }

                POIXMLDocumentPart childPart = context.get(p);
                if (childPart == null) {
                    childPart = factory.createDocumentPart(this, p);
                    //here we are checking if part if embedded and excel then set it to chart class
                    //so that at the time to writing we can also write updated embedded part
                    if (this instanceof XDDFChart && childPart instanceof XSSFWorkbook) {
                        ((XDDFChart) this).setWorkbook((XSSFWorkbook) childPart);
                    }
                    childPart.parent = this;
                    // already add child to context, so other children can reference it
                    context.put(p, childPart);
                    readLater.add(childPart);
                }

                addRelation(rel, childPart);
            }
        }

        for (POIXMLDocumentPart childPart : readLater) {
            childPart.read(factory, context);
        }
    }

    /**
     * Get the PackagePart that is the target of a relationship from this Part.
     *
     * @param rel The relationship
     * @return The target part
     * @throws InvalidFormatException thrown if the related part has is erroneous
     */
    protected PackagePart getTargetPart(PackageRelationship rel) throws InvalidFormatException {
        return getPackagePart().getRelatedPart(rel);
    }


    /**
     * Fired when a new package part is created
     *
     * @throws IOException a subclass may throw an IOException on document creation
     */
    protected void onDocumentCreate() throws IOException {

    }

    /**
     * Fired when a package part is read
     *
     * @throws IOException a subclass may throw an IOException when a document is read
     */
    protected void onDocumentRead() throws IOException {

    }

    /**
     * Fired when a package part is about to be removed from the package
     *
     * @throws IOException a subclass may throw an IOException when a document is removed
     */
    protected void onDocumentRemove() throws IOException {

    }

    /**
     * Internal method, do not use!
     *
     * @deprecated This method only exists to allow access to protected {@link POIXMLDocumentPart#onDocumentRead()}
     * from {@link XWPFDocument} without reflection. It should be removed.
     *
     * @param part the part which is to be read
     * @throws IOException if the part can't be read
     */
    @Internal
    @Deprecated
    public static void _invokeOnDocumentRead(POIXMLDocumentPart part) throws IOException {
        part.onDocumentRead();
    }

    /**
     * Retrieves the core document part
     *
     * Since POI 4.1.2 - pkg is closed if this method throws an exception
     */
    private static PackagePart getPartFromOPCPackage(OPCPackage pkg, String coreDocumentRel) {
        try {
            PackageRelationship coreRel = pkg.getRelationshipsByType(coreDocumentRel).getRelationship(0);

            if (coreRel != null) {
                PackagePart pp = pkg.getPart(coreRel);
                if (pp == null) {
                    IOUtils.closeQuietly(pkg);
                    throw new POIXMLException("OOXML file structure broken/invalid - core document '" + coreRel.getTargetURI() + "' not found.");
                }
                return pp;
            }

            coreRel = pkg.getRelationshipsByType(PackageRelationshipTypes.STRICT_CORE_DOCUMENT).getRelationship(0);
            if (coreRel != null) {
                IOUtils.closeQuietly(pkg);
                throw new POIXMLException("Strict OOXML isn't currently supported, please see bug #57699");
            }

            IOUtils.closeQuietly(pkg);
            throw new POIXMLException("OOXML file structure broken/invalid - no core document found!");
        } catch (POIXMLException e) {
            throw e;
        } catch (RuntimeException e) {
            IOUtils.closeQuietly(pkg);
            throw new POIXMLException("OOXML file structure broken/invalid", e);
        }
    }
}
