/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xslf.usermodel;

import org.apache.poi.sl.usermodel.ShapeContainer;

/**
 * Common interface for shape containers, e.g. sheets or groups of shapes
 */
public interface XSLFShapeContainer extends ShapeContainer<XSLFShape> {

    /**
     * create a new shape with a predefined geometry and add it to this shape container
     */
    XSLFAutoShape createAutoShape();

    /**
     * create a new shape with a custom geometry
     */
    XSLFFreeformShape createFreeform();

    /**
     * create a text box
     */
    XSLFTextBox createTextBox();

    /**
     *
     * create a connector
     */
    XSLFConnectorShape createConnector();

    /**
     * create a group of shapes belonging to this container
     */
    XSLFGroupShape createGroup();

    /**
     * create a picture belonging to this container
     */
    XSLFPictureShape createPicture(int pictureIndex);

    /**
     * Removes all of the elements from this container (optional operation).
     * The container will be empty after this call returns.
     */
    void clear();
}
