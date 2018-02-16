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

/**
 * Represents a Schema annotation.
 */
public interface SchemaAnnotation extends SchemaComponent
{
    /**
     * Retrieves all application information items from this annotation
     */
    public XmlObject[] getApplicationInformation();

    /**
     * Retrieves all document information items from this annotation
     */
    public XmlObject[] getUserInformation();

    /**
     * Retrieves all attributes that are is a namespace other than
     * http://www.w3.org/2001/XMLSchema
     * from the annotation element and from the enclosing Schema component
     */
    public Attribute[] getAttributes();

    /**
     * Represents an attribute instance
     */
    public static interface Attribute
    {
        /**
         * Returns the name of the attribute
         */
        QName getName();

        /**
         * Returns the value of the attribute
         */
        String getValue();

        /**
         * In case the value of this attribute is a QName,
         * returns the URI to which the prefix in the value is bound
         */
        String getValueUri();
    }
}
