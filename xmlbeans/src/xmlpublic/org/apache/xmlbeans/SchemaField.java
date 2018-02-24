/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans;

import javax.xml.namespace.QName;

import java.math.BigInteger;

/**
 * Represents an element or an attribute declaration.
 * 
 * @see SchemaType#getContainerField
 * @see SchemaLocalElement
 * @see SchemaLocalAttribute
 */ 
public interface SchemaField
{
    /**
     * Returns the form-unqualified-or-qualified name.
     */
    QName getName();

    /**
     * True if this use is an attribute
     */
    boolean isAttribute();

    /**
     * True if nillable; always false for attributes.
     */
    boolean isNillable();

    /**
     * Returns the type of this use.
     */
    SchemaType getType();

    /**
     * Returns the minOccurs value for this particle.
     * If it is not specified explicitly, this defaults to BigInteger.ONE.
     */
    BigInteger getMinOccurs();

    /**
     * Returns the maxOccurs value for this particle, or null if it
     * is unbounded.
     * If it is not specified explicitly, this defaults to BigInteger.ONE.
     */
    BigInteger getMaxOccurs();

    /**
     * The default value as plain text. See {@link #isDefault} and {@link #isFixed}.
     */
    String getDefaultText();
    
    /**
     * The default value as a strongly-typed value.  See {@link #isDefault} and {@link #isFixed}.
     */
    XmlAnySimpleType getDefaultValue();

    /**
     * True if a default is supplied. If {@link #isFixed}, then isDefault is always true.
     */
    boolean isDefault();

    /**
     * True if the value is fixed.
     */
    boolean isFixed();

    /**
     * Returns user-specific information.
     * @see SchemaBookmark
     */
    Object getUserData();
}
