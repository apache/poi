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

package org.apache.xmlbeans.impl.tool;

import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Schema;
import org.apache.xmlbeans.impl.xb.xsdschema.ImportDocument.Import;
import org.apache.xmlbeans.impl.xb.xsdschema.IncludeDocument.Include;

import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public abstract class SchemaImportResolver
{
    /**
     * Called when the ImportLoader wishes to resolve the
     * given import.  Should return a SchemaResource whose
     * "equals" relationship reveals when a SchemaResource is
     * duplicated and shouldn't be examined again.
     *
     * Returns null if the resource reference should be ignored.
     */
    public abstract SchemaResource lookupResource(String nsURI, String URL);

    /**
     * Called to notify that the expected namespace is different from the
     * actual namespace, or if no namespace is known, to report the
     * discovered namespace.
     */
    public abstract void reportActualNamespace(SchemaResource resource, String actualNamespace);

    /**
     * Used to supply a schema resource with an optional associated
     * expected-namespace-URI and original-location-URL.
     *
     * The equals (and hashCode) implementations of the SchemaResource
     * objects will be used to avoid examining the same resource twice;
     * these must be implemented according to the desired rules for
     * determining that two resources are the same.
     */
    public interface SchemaResource
    {
        /**
         * Returns a parsed schema object.
         */

        public Schema getSchema();

        public String getNamespace();
        public String getSchemaLocation();
    }

    protected final void resolveImports(SchemaResource[] resources)
    {
        LinkedList queueOfResources = new LinkedList(Arrays.asList(resources));
        LinkedList queueOfLocators = new LinkedList();
        Set seenResources = new HashSet();

        for (;;)
        {
            SchemaResource nextResource;

            // fetch next resource.
            if (!queueOfResources.isEmpty())
            {
                // either off the initial queue
                nextResource = (SchemaResource)queueOfResources.removeFirst();
            }
            else if (!queueOfLocators.isEmpty())
            {
                // or off the list of locators
                SchemaLocator locator = (SchemaLocator)queueOfLocators.removeFirst();
                nextResource = lookupResource(locator.namespace, locator.schemaLocation);
                if (nextResource == null)
                    continue;
            }
            else
            {
                // if no more, then terminate loop
                break;
            }

            // track and skip duplicates
            if (seenResources.contains(nextResource))
                continue;
            seenResources.add(nextResource);

            // get resource contents
            Schema schema = nextResource.getSchema();
            if (schema == null)
                continue;

            // check actual namespace
            String actualTargetNamespace = schema.getTargetNamespace();
            if (actualTargetNamespace == null)
                actualTargetNamespace = "";

            // report actual namespace
            String expectedTargetNamespace = nextResource.getNamespace();
            if (expectedTargetNamespace == null ||
                    !actualTargetNamespace.equals(expectedTargetNamespace))
            {
                reportActualNamespace(nextResource, actualTargetNamespace);
            }

            // now go through and record all the imports
            Import[] schemaImports = schema.getImportArray();
            for (int i = 0; i < schemaImports.length; i++)
            {
                queueOfLocators.add(new SchemaLocator(schemaImports[i].getNamespace() == null ? "" : schemaImports[i].getNamespace(), schemaImports[i].getSchemaLocation()));
            }
            
            // and record all the includes too
            Include[] schemaIncludes = schema.getIncludeArray();
            for (int i = 0; i < schemaIncludes.length; i++)
            {
                queueOfLocators.add(new SchemaLocator(null, schemaIncludes[i].getSchemaLocation()));
            }
        }
    }

    private static class SchemaLocator
    {
        public SchemaLocator(String namespace, String schemaLocation)
        {
            this.namespace = namespace;
            this.schemaLocation = schemaLocation;
        }

        public final String namespace;
        public final String schemaLocation;
    }
}
