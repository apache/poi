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

package org.apache.poi.openxml4j.opc;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A part relationship.
 *
 * @author Julien Chable
 * @version 1.0
 */
public final class PackageRelationship {

	private static URI containerRelationshipPart;

	static {
		try {
			containerRelationshipPart = new URI("/_rels/.rels");
		} catch (URISyntaxException e) {
			// Do nothing
		}
	}

	/* XML markup */

	public static final String ID_ATTRIBUTE_NAME = "Id";

	public static final String RELATIONSHIPS_TAG_NAME = "Relationships";

	public static final String RELATIONSHIP_TAG_NAME = "Relationship";

	public static final String TARGET_ATTRIBUTE_NAME = "Target";

	public static final String TARGET_MODE_ATTRIBUTE_NAME = "TargetMode";

	public static final String TYPE_ATTRIBUTE_NAME = "Type";

	/* End XML markup */

	/**
	 * L'ID de la relation.
	 */
	private String id;

	/**
	 * Reference to the package.
	 */
	private OPCPackage container;

	/**
	 * Type de relation.
	 */
	private String relationshipType;

	/**
	 * Partie source de cette relation.
	 */
	private PackagePart source;

	/**
	 * Le mode de ciblage [Internal|External]
	 */
	private TargetMode targetMode;

	/**
	 * URI de la partie cible.
	 */
	private URI targetUri;

	/**
	 * Constructor.
	 *
	 * @param pkg
	 * @param sourcePart
	 * @param targetUri
	 * @param targetMode
	 * @param relationshipType
	 * @param id
	 */
	public PackageRelationship(OPCPackage pkg, PackagePart sourcePart,
			URI targetUri, TargetMode targetMode, String relationshipType,
			String id) {
		if (pkg == null)
			throw new IllegalArgumentException("pkg");
		if (targetUri == null)
			throw new IllegalArgumentException("targetUri");
		if (relationshipType == null)
			throw new IllegalArgumentException("relationshipType");
		if (id == null)
			throw new IllegalArgumentException("id");

		this.container = pkg;
		this.source = sourcePart;
		this.targetUri = targetUri;
		this.targetMode = targetMode;
		this.relationshipType = relationshipType;
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PackageRelationship)) {
			return false;
		}
		PackageRelationship rel = (PackageRelationship) obj;
		return (this.id.equals(rel.id)
				&& this.relationshipType.equals(rel.relationshipType)
				&& (rel.source != null ? rel.source.equals(this.source) : true)
				&& this.targetMode == rel.targetMode && this.targetUri
				.equals(rel.targetUri));
	}

	@Override
	public int hashCode() {
		return this.id.hashCode()
                + this.relationshipType.hashCode()
				+ (this.source == null ? 0 : this.source.hashCode())
                + this.targetMode.hashCode()
				+ this.targetUri.hashCode();
	}

	/* Getters */

	public static URI getContainerPartRelationship() {
		return containerRelationshipPart;
	}

	/**
	 * @return the container
	 */
	public OPCPackage getPackage() {
		return container;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the relationshipType
	 */
	public String getRelationshipType() {
		return relationshipType;
	}

	/**
	 * @return the source
	 */
	public PackagePart getSource() {
		return source;
	}

	/**
	 *
	 * @return URL of the source part of this relationship
	 */
	public URI getSourceURI() {
		if (source == null) {
			return PackagingURIHelper.PACKAGE_ROOT_URI;
		}
		return source._partName.getURI();
	}

	/**
	 * public URI getSourceUri(){ }
	 *
	 * @return the targetMode
	 */
	public TargetMode getTargetMode() {
		return targetMode;
	}

	/**
	 * @return the targetUri
	 */
	public URI getTargetURI() {
		// If it's an external target, we don't
		//  need to apply our normal validation rules
		if(targetMode == TargetMode.EXTERNAL) {
			return targetUri;
		}

		// Internal target
		// If it isn't absolute, resolve it relative
		//  to ourselves
		if (!targetUri.toASCIIString().startsWith("/")) {
			// So it's a relative part name, try to resolve it
			return PackagingURIHelper.resolvePartUri(getSourceURI(), targetUri);
		}
		return targetUri;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id == null ? "id=null" : "id=" + id);
		sb.append(container == null ? " - container=null" : " - container="
				+ container.toString());
		sb.append(relationshipType == null ? " - relationshipType=null"
				: " - relationshipType=" + relationshipType);
		sb.append(source == null ? " - source=null" : " - source="
				+ getSourceURI().toASCIIString());
		sb.append(targetUri == null ? " - target=null" : " - target="
				+ getTargetURI().toASCIIString());
		sb.append(targetMode == null ? ",targetMode=null" : ",targetMode="
				+ targetMode.toString());
		return sb.toString();
	}
}
