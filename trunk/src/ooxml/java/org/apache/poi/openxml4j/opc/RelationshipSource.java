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

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;

public interface RelationshipSource {

	/**
	 * Add a relationship to a part (except relationships part).
	 *
	 * @param targetPartName
	 *            Name of the target part. This one must be relative to the
	 *            source root directory of the part.
	 * @param targetMode
	 *            Mode [Internal|External].
	 * @param relationshipType
	 *            Type of relationship.
	 * @return The newly created and added relationship
	 */
	public abstract PackageRelationship addRelationship(
			PackagePartName targetPartName, TargetMode targetMode,
			String relationshipType);

	/**
	 * Add a relationship to a part (except relationships part).
	 * <p>
	 * Check rule M1.25: The Relationships part shall not have relationships to
	 * any other part. Package implementers shall enforce this requirement upon
	 * the attempt to create such a relationship and shall treat any such
	 * relationship as invalid.
	 * </p>
	 * @param targetPartName
	 *            Name of the target part. This one must be relative to the
	 *            source root directory of the part.
	 * @param targetMode
	 *            Mode [Internal|External].
	 * @param relationshipType
	 *            Type of relationship.
	 * @param id
	 *            Relationship unique id.
	 * @return The newly created and added relationship
	 *
	 * @throws InvalidFormatException
	 *             If the URI point to a relationship part URI.
	 */
	public abstract PackageRelationship addRelationship(
			PackagePartName targetPartName, TargetMode targetMode,
			String relationshipType, String id);

	/**
	 * Adds an external relationship to a part
	 *  (except relationships part).
	 *
	 * The targets of external relationships are not
	 *  subject to the same validity checks that internal
	 *  ones are, as the contents is potentially
	 *  any file, URL or similar.
	 *
	 * @param target External target of the relationship
	 * @param relationshipType Type of relationship.
	 * @return The newly created and added relationship
	 * @see org.apache.poi.openxml4j.opc.RelationshipSource#addExternalRelationship(java.lang.String, java.lang.String)
	 */
	public PackageRelationship addExternalRelationship(String target, String relationshipType);

	/**
	 * Adds an external relationship to a part
	 *  (except relationships part).
	 *
	 * The targets of external relationships are not
	 *  subject to the same validity checks that internal
	 *  ones are, as the contents is potentially
	 *  any file, URL or similar.
	 *
	 * @param target External target of the relationship
	 * @param relationshipType Type of relationship.
	 * @param id Relationship unique id.
	 * @return The newly created and added relationship
	 * @see org.apache.poi.openxml4j.opc.RelationshipSource#addExternalRelationship(java.lang.String, java.lang.String)
	 */
	public PackageRelationship addExternalRelationship(String target, String relationshipType, String id);

	/**
	 * Delete all the relationships attached to this.
	 */
	public abstract void clearRelationships();

	/**
	 * Delete the relationship specified by its id.
	 *
	 * @param id
	 *            The ID identified the part to delete.
	 */
	public abstract void removeRelationship(String id);

	/**
	 * Retrieve all the relationships attached to this.
	 *
	 * @return This part's relationships.
	 * @throws OpenXML4JException
	 */
	public abstract PackageRelationshipCollection getRelationships()
			throws InvalidFormatException, OpenXML4JException;

	/**
	 * Retrieves a package relationship from its id.
	 *
	 * @param id
	 *            ID of the package relationship to retrieve.
	 * @return The package relationship
	 */
	public abstract PackageRelationship getRelationship(String id);

	/**
	 * Retrieve all relationships attached to this part which have the specified
	 * type.
	 *
	 * @param relationshipType
	 *            Relationship type filter.
	 * @return All relationships from this part that have the specified type.
	 * @throws InvalidFormatException
	 *             If an error occurs while parsing the part.
	 * @throws InvalidOperationException
	 *             If the package is open in write only mode.
	 */
	public abstract PackageRelationshipCollection getRelationshipsByType(
			String relationshipType) throws InvalidFormatException,
			IllegalArgumentException, OpenXML4JException;

	/**
	 * Knows if the part have any relationships.
	 *
	 * @return <b>true</b> if the part have at least one relationship else
	 *         <b>false</b>.
	 */
	public abstract boolean hasRelationships();

	/**
	 * Checks if the specified relationship is part of this package part.
	 *
	 * @param rel
	 *            The relationship to check.
	 * @return <b>true</b> if the specified relationship exists in this part,
	 *         else returns <b>false</b>
	 */
	public abstract boolean isRelationshipExists(PackageRelationship rel);

}
