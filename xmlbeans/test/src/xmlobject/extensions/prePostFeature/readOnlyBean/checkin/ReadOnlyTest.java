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

package xmlobject.extensions.prePostFeature.readOnlyBean.checkin;

import prePostFeature.xbean.readOnlyBean.purchaseOrder.PurchaseOrderDocument ;
import prePostFeature.xbean.readOnlyBean.purchaseOrder.PurchaseOrderType;
import prePostFeature.xbean.readOnlyBean.purchaseOrder.Items;


import java.math.BigDecimal;
import junit.framework.*;

public class ReadOnlyTest extends TestCase{


      public  ReadOnlyTest(String s){
            super(s);
        }
    public void test(){

         PurchaseOrderDocument poDoc ;

         poDoc= PurchaseOrderDocument.Factory.newInstance();
         PurchaseOrderType po=poDoc.addNewPurchaseOrder();


         int LEN=20;

         Items.Item[] it= new Items.Item[LEN];
         for (int i=0; i< LEN; i++){
                it[i]=Items.Item.Factory.newInstance();
                it[i].setUSPrice(new BigDecimal(""+ 4 ));
         }
         Items items= Items.Factory.newInstance();
         items.setItemArray(it);
         po.setItems(items);


             String sExpected =
        "<pur:purchaseOrder xmlns:pur=\"http://xbean.prePost_feature/readOnlyBean/PurchaseOrder\"/>";


           it[0].setPrice(10);

        assertEquals( sExpected, poDoc.xmlText());



       assertTrue ( !poDoc.validate() );
    }

}
