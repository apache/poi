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

package xmlcursor.xpath.common;

import xmlcursor.common.BasicCursorTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Verifies XPath with locations
 * http://www.w3schools.com/xpath/xpath_location.asp
 */
public class XPathLocationTest extends BasicCursorTestCase {

    public XPathLocationTest(String sName) {
        super(sName);
    }

    public static Test suite() {
        return new TestSuite(XPathLocationTest.class);
    }

   //Axes and Node Tests
    /**
     * ancestor
     *      Contains all ancestors (parent, grandparent, etc.) of the current node
     *      Note: This axis will always include the root node, unless the current
     *      node is the root node
     */
    public void testLocationAncestor() throws Exception {
	//tested by zvon
    }

    /**
     * ancestor-or-self
     *      Contains the current node plus all its ancestors (parent, grandparent, etc.)
     */
    public void testLocationAncestorOrSelf() throws Exception {
	//tested by zvon???
    }

    /**
     * attribute
     *      Contains all attributes of the current node
     */
    public void testLocationAttribute() throws Exception {

    }
    /**
     * child
     *      Contains all children of the current node
     */
    public void testLocationChild() throws Exception {

    }
    /**
     * descendant
     *      Contains all descendants (children, grandchildren, etc.) of the current node
     *      Note: This axis never contains attribute or namespace nodes
     */
    public void testLocationDescendant() throws Exception {

    }
    /**
     * descendant-or-self
     *      Contains the current node plus all its descendants (children, grandchildren, etc.)
     */
    public void testLocationDescendantOrSelf() throws Exception {

    }
    /**
     * following
     *      Contains everything in the document after the closing tag of the current node
     */
    public void testLocationFollowing() throws Exception {

    }
    /**
     * following-sibling
     *      Contains all siblings after the current node
     *      Note: If the current node is an attribute node or namespace node,
     *      this axis will be empty
     */
    public void testLocationFollowingSibling() throws Exception {

    }
    /**
     * namespace
     *      Contains all namespace nodes of the current node
     */
    public void testLocationNamespace() throws Exception {

    }
    /**
     * parent
     *      Contains the parent of the current node
     */
    public void testLocationParent() throws Exception {

    }
    /**
     * preceding
     *      Contains everything in the document that is before the starting tag
     *      of the current node
     */
    public void testLocationPreceding() throws Exception {

    }
    /**
     * preceding-sibling
     *      Contains all siblings before the current node
     *      Note: If the current node is an attribute node or namespace node,
     *      this axis will be empty
     */
    public void testLocationPrecedingSibling() throws Exception {

    }
    /**
     * self
     *      Contains the current node
     */
    public void testLocationSelf() throws Exception {

    }


    ////Predicates
    ///**
    // *
    // */
    //public void testLocation() throws Exception {
    //
    //}
    //
    ////Location Path Abbreviated Syntax
    ///**
    // *
    // */
    //public void testLocation() throws Exception {
    //
    //}









}
