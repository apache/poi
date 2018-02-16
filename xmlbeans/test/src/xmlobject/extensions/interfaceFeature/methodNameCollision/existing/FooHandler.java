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

package xmlobject.extensions.interfaceFeature.methodNameCollision.existing;

import org.apache.xmlbeans.XmlObject;
import xmlobject.extensions.interfaceFeature.methodNameCollision.existing.IFoo;

public class FooHandler {
    public static String getName(XmlObject xo){
        return "Name0";
    }

    public static String getName2(XmlObject xo, int[][] ints){
        StringBuffer buf = new StringBuffer();
        buf.append("Name2: ");
        for (int i = 0; i < ints.length; i++)
        {
            buf.append("[");
            for (int j = 0; j < ints[i].length; j++)
            {
                buf.append(ints[i][j]);
                if (j != ints[i].length)
                    buf.append(", ");
            }
            buf.append("]");
            if (i != ints.length)
                buf.append(", ");
        }
        return buf.toString();
     }

     public static  String getName3(XmlObject xo, IFoo.Inner inner){
         return "Name3: " + inner.getValue();
     }
}
