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

package org.apache.xmlbeans.impl.soap;

/**
 * A representation of a node (element) in a DOM representation of an XML document
 * that provides some tree manipulation methods.
 * This interface provides methods for getting the value of a node, for
 * getting and setting the parent of a node, and for removing a node.
 */
public interface Node extends org.w3c.dom.Node {

    /**
     * Returns the the value of the immediate child of this <code>Node</code>
     * object if a child exists and its value is text.
     * @return  a <code>String</code> with the text of the immediate child of
     *    this <code>Node</code> object if (1) there is a child and
     *    (2) the child is a <code>Text</code> object;
     *      <code>null</code> otherwise
     */
    public abstract String getValue();

    /**
     * Sets the parent of this <code>Node</code> object to the given
     * <code>SOAPElement</code> object.
     * @param parent the <code>SOAPElement</code> object to be set as
     *  the parent of this <code>Node</code> object
     * @throws SOAPException if there is a problem in setting the
     *                     parent to the given element
     * @see #getParentElement() getParentElement()
     */
    public abstract void setParentElement(SOAPElement parent)
        throws SOAPException;

    /**
     * Returns the parent element of this <code>Node</code> object.
     * This method can throw an <code>UnsupportedOperationException</code>
     * if the tree is not kept in memory.
     * @return  the <code>SOAPElement</code> object that is the parent of
     *    this <code>Node</code> object or <code>null</code> if this
     *    <code>Node</code> object is root
     * @throws java.lang.UnsupportedOperationException if the whole tree is not kept in memory
     * @see #setParentElement(javax.xml.soap.SOAPElement) setParentElement(javax.xml.soap.SOAPElement)
     */
    public abstract SOAPElement getParentElement();

    /**
     * Removes this <code>Node</code> object from the tree. Once
     * removed, this node can be garbage collected if there are no
     * application references to it.
     */
    public abstract void detachNode();

    /**
     * Notifies the implementation that this <code>Node</code>
     * object is no longer being used by the application and that the
     * implementation is free to reuse this object for nodes that may
     * be created later.
     * <P>
     * Calling the method <code>recycleNode</code> implies that the method
     * <code>detachNode</code> has been called previously.
     */
    public abstract void recycleNode();

    /**
     * If this is a Text node then this method will set its value, otherwise it
     * sets the value of the immediate (Text) child of this node. The value of
     * the immediate child of this node can be set only if, there is one child
     * node and that node is a Text node, or if there are no children in which
     * case a child Text node will be created.
     *
     * @param value the text to set
     * @throws IllegalStateException   if the node is not a Text  node and
     *              either has more than one child node or has a child node that
     *              is not a Text node
     */

    public abstract void setValue(String value);
}
