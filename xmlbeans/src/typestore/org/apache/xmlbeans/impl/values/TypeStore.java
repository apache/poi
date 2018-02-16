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

import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.QNameSet;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.impl.common.ValidatorListener;
import org.apache.xmlbeans.impl.common.XmlLocale;

public interface TypeStore extends NamespaceManager
{
    /**
     * Creates a new cursor positioned just before the part of the tree
     * where this TypeStore is located.
     */
    XmlCursor new_cursor();

    /**
     */
    void validate ( ValidatorListener vEventSink );

    /**
     * Get the SchemaTypeLoader associated with the store contianing this
     * TypeStore.
     */
    SchemaTypeLoader get_schematypeloader ( );

    /**
     * Change the type of this store (perhaps by applying xsi:type) and
     * return the new TypeStoreUser (or old one if the new type is not
     * different).
     */
    TypeStoreUser change_type ( SchemaType sType );

    TypeStoreUser substitute ( QName name, SchemaType sType );

    /**
     * Tells if this store is an attribute or not
     */
    boolean is_attribute ( );

    /**
     * Get the value of xsi:type.  Return null if none or this is an attribute
     * store.
     */
    QName get_xsi_type ( );

    /**
     * A user of a TypeStore calls invalidate_text when the underlying
     * value has changed and he wants the textstore to call him back with
     * a fetch_text (TypeStore/TypeStoreUsers work in pairs).
     */
    void invalidate_text();

    /**
     * A user of a TypeStore calls fetch_text when he knows his view
     * of the text is invalid and he wants to see what the actual text
     * value is.
     */
    // BUGBUG (ericvas) 12111
    String fetch_text(int whitespaceRule);

    public static int WS_UNSPECIFIED = 0;
    public static int WS_PRESERVE = 1;
    public static int WS_REPLACE = 2;
    public static int WS_COLLAPSE = 3;

    /**
     * A user of a TypeStore calls store_text when he wants the TypeStore
     * to remember the given text immediately. This typically happens when
     * the user has a noncanonical (but valid) string representation to save,
     * but doesn't have the storage in which to save it.
     */
    void store_text(String text);

    /**
     * Here the TypeStore is responsible for locating the default value.
     * This is done as follows
     * (1) go to the parent TypeStoreUser
     * (2) ask it to get_default_element_text(qname) (or _attribute_), and return it if not null.
     * (2) otherwise, grab a new TypeStoreUserVisitor via v = parentuser.new_visitor();
     * (3) call v.visit(name) on _every_ element qname up to and including this one in order
     * (4) return the result of v.get_default_text().
     */
    String compute_default_text();

    /**
     * Here the TypeStore is responsible for figuring if this value is
     * nillable and/or fixed. This is done by
     * (1) go to the parent TypeStoreUser
     * (2) ask it to get_elementflags(qname), and return it if not -1.
     * (2) otherwise, grab a new TypeStoreUserVisitor via v = parentuser.new_visitor();
     * (3) call v.visit(name) on _every_ element qname up to and including this one in order
     * (4) return the result of v.get_elementflags().
     */
    int compute_flags();


    /**
     * Tells if this store was created with this option which tells the strongly typed
     * objects to perform lexical and value validation after a setter is called.
     */
    boolean validate_on_set();

    /**
     * Here the typestore is resposible for finding the schema field for
     * this object. This is done by
     * (1) otherwise, grab a new TypeStoreUserVisitor via v = parentuser.new_visitor();
     * (2) call v.visit(name) on _every_ element qname up to and including this one in order
     * (3) return the result of v.get_schema_field().
     */
    SchemaField get_schema_field();

    public static final int NILLABLE = 1;
    public static final int HASDEFAULT = 2;
    public static final int FIXED = 4; // always set with HASDEFAULT

    /**
     * Called when the value has been nilled or unnilled, so the textstore
     * knows it needs to update the xsi:nil attribute.
     */
    void invalidate_nil();

    /**
     * The TypeStore is reponsible for discovering if this value is nil.
     * This is done by (1) going to the element and (2) finding the
     * xsi:nil attribute if present and (3) return true if the collapsed
     * textual value is either exactly the string "true" or "1".
     */
    boolean find_nil();

    /**
     * Returns the count of elements with the given name owned by this
     * textstore.
     */
    int count_elements(QName name);

    /**
     * Returns the count of elements that match of the names.
     */
    int count_elements(QNameSet names);

    /**
     * Returns the TypeStoreUser underneath the ith element with the given
     * name owned by this textstore, or null if none was found.
     *
     * Do not throw an IndexOutOfBoundsException if i is bad -
     * return null instead. The reason is to allow us to fail
     * and then follow with an add_element_etc if we choose to,
     * without randomly catching exceptions.
     */
// BUGBUG - this should be called find_element
// BUGBUG - this should be called find_element
// BUGBUG - this should be called find_element
// BUGBUG - this should be called find_element
// BUGBUG - this should be called find_element
    TypeStoreUser find_element_user(QName name, int i);

    /**
     * Like find_element_user but accepts a set of names to search for.
     */
    TypeStoreUser find_element_user(QNameSet names, int i);

    /**
     * Returns all the TypeStoreUsers corresponding to elements with the
     * given name owned by this typestore, or the empty array of
     * TypeStoreUsers if none was found.
     */
// BUGBUG - this should be called find_all_element
// BUGBUG - this should be called find_all_element
// BUGBUG - this should be called find_all_element
// BUGBUG - this should be called find_all_element
    void find_all_element_users(QName name, List fillMeUp);


    /**
     * Returns all TypeStoreUsers corresponding to elements with one
     * of the names is the QNameSet.
     */
    void find_all_element_users(QNameSet name, List fillMeUp);

    /**
     * Inserts a new element at the position that will make it
     * the ith element with the given name owned by this textstore,
     * and returns a TypeStoreUser for that element.
     *
     * Note that if there are no existing elements of the given
     * name, you may need to call back to discover the proper
     * ordering to use to insert the first one. Otherwise,
     * it should be inserted adjacent to existing elements with
     * the same name.
     *
     * Should throw an IndexOutOfBoundsException if i < 0
     * or if i > # of elts
     */

// BUGBUG - this should be called insert_element
// BUGBUG - this should be called insert_element
// BUGBUG - this should be called insert_element
    TypeStoreUser insert_element_user(QName name, int i);

    /**
     * Like the above method, except that it inserts an element named
     * name, after the ith member of set.
     */
    TypeStoreUser insert_element_user(QNameSet set, QName name, int i);

    /**
     * Adds a new element at the last position adjacent to existing
     * elements of the same name.
     *
     * Note that if there are no existing elements of the given
     * name, the same comment applies as with insert_element_user.
     */
// BUGBUG - this should be called add_element
// BUGBUG - this should be called add_element
// BUGBUG - this should be called add_element
// BUGBUG - this should be called add_element
    TypeStoreUser add_element_user(QName name);

    /**
     * Removes the ith element with the given name.
     *
     * Should throw an IndexOutOfBoundsException if i < 0
     * or if i > # of elts-1.
     */
    void remove_element(QName name, int i);

    /**
     * Removes the ith element that matches names.
     */
    void remove_element(QNameSet names, int i);


    /**
     * Returns the TypeStoreUser underneath the attribute with the given
     * name, or null if there is no such attribute.
     */
// BUGBUG - this should be called find_attribute
// BUGBUG - this should be called find_attribute
// BUGBUG - this should be called find_attribute
// BUGBUG - this should be called find_attribute
    TypeStoreUser find_attribute_user(QName name);

    /**
     * Adds an attribute with the given name and returns a TypeStoreUser
     * underneath it. Should throw an IndexOutOfBoundsException if there
     * is already an existing attribute with the given name.
     */
// BUGBUG - this should be called add_attribute
// BUGBUG - this should be called add_attribute
// BUGBUG - this should be called add_attribute
// BUGBUG - this should be called add_attribute
    TypeStoreUser add_attribute_user(QName name);

    /**
     * Removes the attribute with the given name.
     */
    void remove_attribute(QName name);

    /**
     * Copies the contents of the given TypeStore (including attributes,
     * elements, and mixed content), to the target type store.
     *
     * SPECIAL NOTE: The xsi:type attribute should not be removed from
     * the target or copied from the soruce, and the TypeStoreUser attached
     * to this TypeStore should not be disconnected.
     *
     * This is for implementing obj.set(foo).
     */
    TypeStoreUser copy_contents_from(TypeStore source);

    /**
     * Makes a copy of this store.
     * NOTE: Even if st is NO_TYPE, the store can be a document. This method will make an exact copy.
     */
    TypeStoreUser copy(SchemaTypeLoader schemaTypeLoader, SchemaType schemaType, XmlOptions options);

// BUGBUG - Need to use this in the future
//    /**
//     * Copies the contents of the given TypeStore (including attributes,
//     * elemets, mixed content), to the child element given by the given
//     * name and index. Any TypeStoreUser that might be currently attached
//     * to that element is disconnected.  The xsi:type attribute of the
//     * element should be set according to the given QName (or deleted if
//     * the xsitype argument is null)
//     */
//    void copy_to_element(
//        TypeStore source, QName xsitype, QName name, int i);

    /**
     * Copies the contents of the given array of XmlObject (including
     * attributes, elements, mixed content), over all the elements of the
     * given name under the current typestore.
     *
     * The lengths of the two arrays that are passed should be the same.
     *
     * If there are n current elements of the given name and m elements
     * in the source array, there are several cases for individual elements:
     *
     * 1. If i < n and i < m, then the contents of the ith source are copied
     *    underneath the ith element; the ith element is not moved, but its
     *    TypeStoreUser is disconnected.
     * 2. if i >= n and i < m, then first enough new elements are appended
     *    so that there is an element with the name i, then rule #1 is followed.
     * 3. if i >= m and i < n, then the element #i and all its contents
     *    are removed.
     */
    
    void array_setter ( XmlObject[] sources, QName elementName );

    /**
     * Visits all the elements immediately, using the given visitor.
     * A TypeStoreUser calls this when somebody has requested validation.
     */
    void visit_elements(TypeStoreVisitor visitor);

    XmlObject[] exec_query ( String queryExpr, XmlOptions options )
        throws XmlException;

    /**
     * Returns the monitor object, used for synchronizing access to the doc.
     * @deprecated
     */ 
    Object get_root_object();

    /**
     * Returns the locale object which is used to manage thread safty and the
     * gateway requirements for calls into the xml store
     */
    XmlLocale get_locale ( );
}
