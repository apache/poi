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

import java.math.BigInteger;

import javax.xml.namespace.QName;

/**
 * Represents a Schema particle definition.
 * <p>
 * The content model of a complex type is a tree of particles.  Each
 * particle is either an {@link #ALL}, {@link #CHOICE}, {@link #SEQUENCE},
 * {@link #ELEMENT}, or {@link #WILDCARD}.
 * All, choice and sequence particles are groups that can have child
 * particles; elements and wildcards are always leaves of the particle tree.
 * <p>
 * The tree of particles available on a schema type is minimized, that
 * is, it already has removed "pointless" particles such as empty
 * sequences, nonrepeating sequences with only one item, and so on.
 * (<a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#cos-particle-restrict">Pointless particles</a>
 * are defined precisely in the XML Schema specification.)
 * 
 * @see SchemaType#getContentModel
 */
public interface SchemaParticle
{
    /**
     * Returns the particle type ({@link #ALL}, {@link #CHOICE},
     * {@link #SEQUENCE}, {@link #ELEMENT}, or {@link #WILDCARD}). 
     */ 
    int getParticleType();
    
    /**
     * An <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#declare-contentModel">xs:all</a> group.
     * See {@link #getParticleType}.
     */ 
    static final int ALL = 1;
    /**
     * A <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#declare-contentModel">xs:choice</a> group.
     * See {@link #getParticleType}.
     */ 
    static final int CHOICE = 2;
    /**
     * A <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#declare-contentModel">xs:sequence</a> group.
     * See {@link #getParticleType}.
     */ 
    static final int SEQUENCE = 3;
    /**
     * An <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#declare-element">xs:element</a> particle.
     * This code means the particle can be coerced to {@link SchemaLocalElement}.
     * See {@link #getParticleType}.
     */ 
    static final int ELEMENT = 4;
    /**
     * An <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#declare-openness">xs:any</a> particle,
     * also known as an element wildcard.
     * See {@link #getParticleType}.
     */ 
    static final int WILDCARD = 5;

    /**
     * Returns the minOccurs value for this particle.
     * If it's not specified explicitly, this returns BigInteger.ONE.
     */
    BigInteger getMinOccurs();

    /**
     * Returns the maxOccurs value for this particle, or null if it
     * is unbounded.
     * If it's not specified explicitly, this returns BigInteger.ONE.
     */
    BigInteger getMaxOccurs();

    /**
     * Returns the minOccurs value, pegged to a 32-bit int for
     * convenience of a validating state machine that doesn't count
     * higher than MAX_INT anyway.
     */
    public int getIntMinOccurs();

    /**
     * Returns the maxOccurs value, pegged to a 32-bit int for
     * convenience of a validating state machine that doesn't count
     * higher than MAX_INT anyway. Unbounded is given as MAX_INT.
     */
    public int getIntMaxOccurs();


    /**
     * One if minOccurs == maxOccurs == 1.
     */
    boolean isSingleton();

    /**
     * Applies to sequence, choice, and all particles only: returns an array
     * of all the particle children in order.
     */
    SchemaParticle[] getParticleChildren();

    /**
     * Another way to access the particle children.
     */
    SchemaParticle getParticleChild(int i);

    /**
     * The number of children.
     */
    int countOfParticleChild();

    /**
     * True if this particle can start with the given element
     * (taking into account the structure of all child particles
     * of course).
     */
    boolean canStartWithElement(QName name);

    /**
     * Returns the QNameSet of element names that can be
     * accepted at the beginning of this particle.
     */
    QNameSet acceptedStartNames();

    /**
     * True if this particle can be skipped (taking into account
     * both the minOcurs as well as the structure of all the
     * child particles)
     */
    boolean isSkippable();

    /**
     * For wildcards, returns a QNameSet representing the wildcard.
     */
    QNameSet getWildcardSet();

    /**
     * For wildcards, returns the processing code ({@link #STRICT}, {@link #LAX}, {@link #SKIP}).
     */
    int getWildcardProcess();

    /** <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#Wildcard_details">Strict wildcard</a> processing. See {@link #getWildcardProcess} */
    static final int STRICT = 1;
    /** <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#Wildcard_details">Lax wildcard</a> processing. See {@link #getWildcardProcess} */
    static final int LAX = 2;
    /** <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#Wildcard_details">Skip wildcard</a> processing. See {@link #getWildcardProcess} */
    static final int SKIP = 3;

    /**
     * For elements only: the QName for the element use.
     * May be unqualified version of referenced element's name.
     */
    QName getName();
    
    /**
     * For elements only: returns the type of the element.
     */
    SchemaType getType();

    /**
     * For elements only: true if nillable.
     */
    boolean isNillable();

    /**
     * For elements only: returns the default (or fixed) text value
     */
    String getDefaultText();
    
    /**
     * For elements only: returns the default (or fixed) strongly-typed value
     */
    XmlAnySimpleType getDefaultValue();

    /**
     * For elements only: True if has default. If isFixed, then isDefault is always true.
     */
    boolean isDefault();

    /**
     * For elements only: true if is fixed value.
     */
    boolean isFixed();
    
}
