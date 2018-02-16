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
package xmlobject.extensions.prePostFeature.ValueRestriction.existing;

import prePostFeature.xbean.valueRestriction.company.ConsultantType;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.PrePostExtension;


public class SetterHandler {

      public static boolean bReady=false;
      public static void setEmployeeAge(XmlObject xo, int age){
         ((ConsultantType)xo).setAge(age);
      }

    private static XmlObject origValue;
     public static boolean preSet(int opType, XmlObject xo, QName prop, boolean isAttr, int index)
          {
           origValue=xo.copy();
           return true;
          }

          public static void postSet(int opType, XmlObject xo, QName propertyName, boolean isAttr, int index)
          {
              if ( ! xo.validate() &&
                   ( opType == PrePostExtension.OPERATION_SET ) &&
                      bReady
              )
                  xo.set(origValue);
                  xo.validate();
          }


}
