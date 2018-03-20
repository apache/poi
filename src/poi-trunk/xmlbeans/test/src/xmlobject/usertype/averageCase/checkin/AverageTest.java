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

package xmlobject.usertype.averageCase.checkin;

import java.math.BigDecimal;

import javax.xml.stream.XMLOutputFactory;

import junit.framework.TestCase;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;

import usertype.xbean.averageCase.purchaseOrder.Items;
import usertype.xbean.averageCase.purchaseOrder.PurchaseOrderDocument;
import usertype.xbean.averageCase.purchaseOrder.PurchaseOrderType;
import xmlobject.usertype.averageCase.existing.SKU;


public class AverageTest extends TestCase
{

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
            it[i].setPartNum(new SKU(i, "AB"));
        }
        Items items= Items.Factory.newInstance();
        items.setItemArray(it);
        po.setItems(items);
        //  System.out.println("poDoc: " + poDoc);

        for (int i=0; i< LEN; i++){
            assertEquals(i, it[i].getPartNum().getDigits());
            assertEquals("AB", it[i].getPartNum().getLetters());
        }


    }

    public void testBadInput() throws XmlException{

        StringBuffer sb = new StringBuffer();
        sb.append("<purchaseOrder xmlns=\"http://xbean.usertype/averageCase/PurchaseOrder\">");
        sb.append("<items><item partNum=\"000-AB\"><USPrice>2</USPrice></item>");
        sb.append("<item partNum=\"0013-AB\"><USPrice>2</USPrice></item>");
        sb.append("</items></purchaseOrder>");

        PurchaseOrderDocument poDocument = PurchaseOrderDocument.Factory.parse(sb.toString());

        PurchaseOrderType po = poDocument.getPurchaseOrder();

        Items.Item[] it = po.getItems().getItemArray();
        assertEquals(2, it.length);


        SKU sku = it[0].getPartNum();

        assertEquals(0, sku.getDigits());
        assertEquals("AB", sku.getLetters());

        try {

            sku = it[1].getPartNum();
            fail("Invalid SKU format should have failed");

        } catch (XmlValueOutOfRangeException e) {

            // test passed
        }


    }
}
