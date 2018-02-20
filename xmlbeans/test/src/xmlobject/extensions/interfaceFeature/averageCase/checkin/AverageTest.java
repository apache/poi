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

package xmlobject.extensions.interfaceFeature.averageCase.checkin;

import interfaceFeature.xbean.averageCase.purchaseOrder.PurchaseOrderDocument ;
import interfaceFeature.xbean.averageCase.purchaseOrder.PurchaseOrderType;
import interfaceFeature.xbean.averageCase.purchaseOrder.Items;

import junit.framework.*;

import java.math.BigDecimal;



public class AverageTest extends TestCase{

        public  AverageTest(String s){
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
                it[i].setUSPrice(new BigDecimal(""+ 2 ));
         }
         Items items= Items.Factory.newInstance();
            items.setItemArray(it);
         po.setItems(items);
	// System.out.println("poDoc: " + poDoc);

        int i=poDoc.getTotal();
        //20 items @ $2
        assertEquals( 40, i );

    }

    public void testJiraXMLBEANS_206()
    {
        PurchaseOrderDocument poDoc ;

        poDoc= PurchaseOrderDocument.Factory.newInstance();
        PurchaseOrderType po = poDoc.addNewPurchaseOrder();

        System.out.println("Jira206 Test : = " + po.getName("String1"));
        System.out.println("Jira206 Test : = " + po.getName("String1", 3));
    }

}
