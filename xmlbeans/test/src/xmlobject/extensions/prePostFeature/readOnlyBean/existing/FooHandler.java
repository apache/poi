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
package xmlobject.extensions.prePostFeature.readOnlyBean.existing;



import prePostFeature.xbean.readOnlyBean.purchaseOrder.PurchaseOrderDocument;
import prePostFeature.xbean.readOnlyBean.purchaseOrder.Items.Item;

import javax.xml.namespace.QName;
import java.math.BigDecimal;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.PrePostExtension;

public class FooHandler
{

   
    public static boolean preSet(int opType, XmlObject xo, QName prop, boolean isAttr, int index)
       {
        if ( opType == PrePostExtension.OPERATION_SET )
           return false;
        return true;
       }

       public static void postSet(int opType, XmlObject xo, QName propertyName, boolean isAttr, int index)
       {
           if ( opType == PrePostExtension.OPERATION_SET )
          System.err.println("No setters are allowed for this bean:" +
                  " value unchanged"+propertyName);
       }





 public static void setPrice(XmlObject xo, int price)
    {

        Item it = (Item) xo;
        BigDecimal o=new BigDecimal(price+"");
        it.setUSPrice(o);

    }

	public static double getPrice(XmlObject xo){

        Item it = (Item) xo;
        return it.getUSPrice().doubleValue();
        }

    }
