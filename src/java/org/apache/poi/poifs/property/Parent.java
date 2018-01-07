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

package org.apache.poi.poifs.property;

import java.util.Iterator;

import java.io.IOException;

/**
 * Behavior for parent (directory) properties
 */
public interface Parent extends Child, Iterable<Property> {

    /**
     * Get an iterator over the children of this Parent; all elements
     * are instances of Property.
     *
     * @return Iterator of children; may refer to an empty collection
     */

    public Iterator<Property> getChildren();

    /**
     * Add a new child to the collection of children
     *
     * @param property the new child to be added; must not be null
     *
     * @exception IOException if the Parent already has a child with
     *                        the same name
     */

    public void addChild(final Property property)
        throws IOException;

    /**
     * Set the previous Child
     *
     * @param child the new 'previous' child; may be null, which has
     *              the effect of saying there is no 'previous' child
     */

    public void setPreviousChild(final Child child);

    /**
     * Set the next Child
     *
     * @param child the new 'next' child; may be null, which has the
     *              effect of saying there is no 'next' child
     */

    public void setNextChild(final Child child);
}
