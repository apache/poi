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
package xmlobject.extensions.interfaceFeature.readOnlyBean.existing;



import interfaceFeature.xbean.readOnlyBean.purchaseOrder.PurchaseOrderDocument;
import interfaceFeature.xbean.readOnlyBean.purchaseOrder.Items.Item;
import java.math.BigDecimal;

import org.apache.xmlbeans.XmlObject;

public class FooHandler
{
    public static void setPrice(XmlObject xo, int price)
    {

        PurchaseOrderDocument poDoc = (PurchaseOrderDocument) xo;
               Item[] items = poDoc.getPurchaseOrder().getItems().getItemArray();

        BigDecimal o=new BigDecimal(price+"");
        try{
         for (int i=0; i<items.length; i++)
		items[i].setUSPrice(o);
        }catch (Exception e){
            e.printStackTrace(System.err);
        }

    }

	public static void getPrice(XmlObject xo){

    }


   
}