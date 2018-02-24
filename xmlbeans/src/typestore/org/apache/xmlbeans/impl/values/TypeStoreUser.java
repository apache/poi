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

package org.apache.xmlbeans.impl.values;

import javax.xml.namespace.QName;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaField;

/**
 * Represents the strongly-typed user of a typestore.
 * A typestore is responsible for all the lexical aspects of XML, and
 * a typestore user is responsible for all strongly-typed aspects.
 * Where there is interaction between the two models, it is represented
 * by the TypeStore and TypeStoreUser interfaces.
 */
public interface TypeStoreUser
{
    /**
     * Called to attach to the given textStore. When a TypeStoreUser is
     * attached, it is put into a completely invalidated state.
     */
    void attach_store(TypeStore store);

    /**
     * Returns the schema type of this user
     */
    SchemaType get_schema_type();

    /**
     * Returns the store to which this typestoreuser is attached, or
     * null if none.
     */
    TypeStore get_store();

    /**
     * A store will call back on invalidate_value when its text has
     * changed and it therefore knows that any cached type value is
     * invalid. It is the responsibilty of the type to call fetch_text
     * and reparse next time the user does a strongly-typed get.
     */
    void invalidate_value();

    /**
     * A store can call uses_invalidate_value to know if calls to
     * invalidate_value will be fruitful.  If uses_invalidate_value
     * returns false, invalidate_value need never be called.
     */
    boolean uses_invalidate_value();

    /**
     * A store will call back on build_text when it knows its own text
     * is invalid and needs to fill it in.  If forExternal is true, then
     * the value returned will be used to replenish the store's cache of
     * the value.  Otherwise, the value is being computed for purposes
     * other than validation, like persistence.
     *
     * Also, the only member on TypeStore which may be called while build_text
     * is on the stack is find_prefix_for_nsuri which must have the
     * forExternal state passed to it as it is passed here.
     */
    String build_text(NamespaceManager nsm);

    /**
     * A store will call back on build_nil after you've called invalidate_nil
     * and it needs to know what the nil value is.
     */
    boolean build_nil();

    /**
     * A store calls back on invalidate_nilvalue when the value of
     * the xsi:nil tag has changed.
     */
    void invalidate_nilvalue();

    /**
     * A store calls back on invalidate_element_order when a rearrangment
     * of sibling elements to the left of this element means that the
     * nillable value may no longer be valid.
     */
    void invalidate_element_order();

    /**
     * A store will call back on validate_now to force us to look at
     * the text if we're in an invalid state. This function is allowed
     * and expected to throw an exception if the text isn't valid for
     * our type.
     */
    void validate_now();

    /**
     * A store calls back on this call in order to force a disconnect.
     * After this is done, the object should be considered invalid.
     */
    void disconnect_store();

    /**
     * A typestore user can create a new TypeStoreUser instance for
     * a given element child name as long as you also pass the
     * qname contained by the xsi:type attribute, if any.
     *
     * Note that we will ignore the xsiType if it turns out to be invalid.
     */
    TypeStoreUser create_element_user(QName eltName, QName xsiType);

    /**
     * A typestore user can create a new TypeStoreUser instance for
     * a given attribute child, based on the attribute name.
     */
    TypeStoreUser create_attribute_user(QName attrName);

    /**
     * Return the SchemaType which a child element of this name and xsi:type
     * would be.
     */

    SchemaType get_element_type(QName eltName, QName xsiType);
    
    /**
     * Return the SchemaType which an attribute of this name would be.
     */

    SchemaType get_attribute_type(QName attrName);

    /**
     * Returns the default element text, if it's consistent. If it's
     * not consistent, returns null, and requires a visitor walk.
     */
    String get_default_element_text(QName eltName);

    /**
     * Returns the default attribute text for the attribute with
     * the given name.
     */
    String get_default_attribute_text(QName attrName);

    /**
     * Returns the elementflags, if they're consistent. If they're
     * not, returns -1, and requires a vistor walk.
     */
    int get_elementflags(QName eltName);

    /**
     * Returns the flags for an attribute.
     */
    int get_attributeflags(QName attrName);

    /**
     * Returns the schema field for an attribute
     */
    SchemaField get_attribute_field(QName attrName);

    /**
     * Returns false if child elements are insensitive to order;
     * if it returns true, you're required to call invalidate_element_order
     * on children to the right of any child order rearrangement.
     */
    boolean is_child_element_order_sensitive();

    /**
     * A typestore user can return the element sort order to use for
     * insertion operations if needed. Settable elements should
     * be stored in this order if possible.
     */
    QNameSet get_element_ending_delimiters(QName eltname);

    /**
     * A typestore user can return a visitor that is used to compute
     * default text and elementflags for an arbitrary element.
     */
    TypeStoreVisitor new_visitor();
}
