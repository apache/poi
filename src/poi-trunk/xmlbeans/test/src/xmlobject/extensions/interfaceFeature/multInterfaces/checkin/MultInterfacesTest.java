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
package xmlobject.extensions.interfaceFeature.multInterfaces.checkin;

import interfaceFeature.xbean.multInterfaces.purchaseOrder.PurchaseOrderDocument;
import interfaceFeature.xbean.multInterfaces.purchaseOrder.PurchaseOrderType;
import interfaceFeature.xbean.multInterfaces.purchaseOrder.Items;
import interfaceFeature.xbean.multInterfaces.purchaseOrder.Items.Item;

import java.math.BigDecimal;

import org.apache.xmlbeans.XmlObject;


import junit.framework.*;

public class MultInterfacesTest extends TestCase {

    public MultInterfacesTest(String s) {
        super(s);
    }

    public void test() {


        PurchaseOrderDocument poDoc = null;

        poDoc = PurchaseOrderDocument.Factory.newInstance();
        PurchaseOrderType po = poDoc.addNewPurchaseOrder();
        int LEN = 20;

        Items.Item[] it = new Items.Item[LEN];
        for (int i = 0; i < LEN; i++) {
            it[i] = Items.Item.Factory.newInstance();
            it[i].setUSPrice(new BigDecimal("" + i));
        }
        Items items = Items.Factory.newInstance();
        items.setItemArray(it);
        po.setItems(items);

        StringBuffer sb = new StringBuffer();
        sb.append(
                "<pur:purchaseOrder xmlns:pur=\"http://xbean.interface_feature/multInterfaces/PurchaseOrder\">");

        sb.append("<pur:items>");

        StringBuffer sbContent = new StringBuffer();
        for (int i = 0; i < LEN; i++)
            sbContent.append("<pur:item><pur:USPrice>"+i+"</pur:USPrice></pur:item>");

        int pos = sb.length();
        sb.append("</pur:items></pur:purchaseOrder>");

        String sExpected = sb.subSequence(0, pos) +
                sbContent.toString() +
                sb.subSequence(pos, sb.length());
        assertEquals(sExpected, poDoc.xmlText());


        assertEquals(0, poDoc.getMinPrice());
        int price=10;

        poDoc.setMinPrice((double)price);

        sbContent = new StringBuffer();
        for (int i = 0; i < LEN; i++)
           if( i< price )
            sbContent.append("<pur:item><pur:USPrice>"+price+"</pur:USPrice></pur:item>");
            else
             sbContent.append("<pur:item><pur:USPrice>"+i+"</pur:USPrice></pur:item>");
        sExpected = sb.subSequence(0, pos) +
                sbContent.toString() +
                sb.subSequence(pos, sb.length());
        assertEquals(sExpected, poDoc.xmlText());

        assertEquals(price, poDoc.getMinPrice());

        int expTotal=(price-1)*price+(price+1+LEN) * (LEN-price) / 2;
        assertEquals(expTotal,poDoc.getTotal());

        XmlObject item = poDoc.getCheapestItem();

       Item expected=it[0];
        expected.setUSPrice(new BigDecimal(30d));
 //       assertEquals(expected, item );
    }

}
