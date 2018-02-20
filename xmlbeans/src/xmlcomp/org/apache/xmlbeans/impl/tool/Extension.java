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

import java.util.List;
import java.util.ArrayList;


 /**
  * An extension is a javabean that represents a SchemaCompilerExtension to be passed for callback into the
  *  XmlBeans Schema Compiler.
  */
public class Extension {
        private Class className;
        private List params = new ArrayList();

        public Class getClassName() {
            return className;
        }

        public void setClassName(Class className) {
            this.className = className;
        }
        public List getParams() {
            return params;
        }

        public Param createParam() {
            Param p = new Param();
            params.add(p);
            return p;
        }

        /**
         * A Param is just a name value pair applicable to the extension.
         */
        public class Param {
            private String name;
            private String value;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }
    }