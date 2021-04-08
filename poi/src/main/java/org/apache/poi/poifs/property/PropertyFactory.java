
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

import java.util.List;

import org.apache.poi.poifs.common.POIFSConstants;

/**
 * Factory for turning an array of RawDataBlock instances containing
 * Property data into an array of proper Property objects.
 *
 * The array produced may be sparse, in that any portion of data that
 * should correspond to a Property, but which does not map to a proper
 * Property (i.e., a DirectoryProperty, DocumentProperty, or
 * RootProperty) will get mapped to a null Property in the array.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

final class PropertyFactory {
    // no need for an accessible constructor
    private PropertyFactory()
    {
    }

    static void convertToProperties(byte[] data, List<Property> properties) {
       int property_count = data.length / POIFSConstants.PROPERTY_SIZE;
       int offset         = 0;

       for (int k = 0; k < property_count; k++) {
          switch (data[ offset + PropertyConstants.PROPERTY_TYPE_OFFSET ]) {
          case PropertyConstants.DIRECTORY_TYPE :
             properties.add(
                   new DirectoryProperty(properties.size(), data, offset)
             );
             break;

          case PropertyConstants.DOCUMENT_TYPE :
             properties.add(
                   new DocumentProperty(properties.size(), data, offset)
             );
             break;

          case PropertyConstants.ROOT_TYPE :
             properties.add(
                   new RootProperty(properties.size(), data, offset)
             );
             break;

          default :
             properties.add(null);
             break;
          }
          
          offset += POIFSConstants.PROPERTY_SIZE;
       }
    }
}
