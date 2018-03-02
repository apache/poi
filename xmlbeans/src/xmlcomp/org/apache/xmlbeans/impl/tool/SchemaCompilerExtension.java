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

import org.apache.xmlbeans.SchemaTypeSystem;

import java.util.Map;


/**
 * This interface is implemented by Schema Compiler Extensions.  By implementing this class
 *  you can then pass this class via the command line param <i>extension</i> to the SchemaCompiler class
 *  which will then in turn call this class back once the compilation is complete with the resulting
 *  SchemaTypeSystem.
 */
public interface SchemaCompilerExtension {
    /**
     * Implement this function to be called back by the XmlBeans Schema Compiler with
     *  the Schema Type System that has been created as a result of the compile.
     * @param schemaTypeSystem - The schema type systems that has been created by the compiler just prior to calling
     *   this extension class.
     * @param parms - name value pairs of options to this schema compiler extension
     */
    public void schemaCompilerExtension(SchemaTypeSystem schemaTypeSystem, Map parms);
    public String getExtensionName();
}
