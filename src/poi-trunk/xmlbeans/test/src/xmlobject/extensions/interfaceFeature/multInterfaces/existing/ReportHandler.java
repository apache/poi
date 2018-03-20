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
package  xmlobject.extensions.interfaceFeature.multInterfaces.existing;


import interfaceFeature.xbean.multInterfaces.purchaseOrder.PurchaseOrderDocument;
import interfaceFeature.xbean.multInterfaces.purchaseOrder.Items.Item;


import java.math.BigDecimal;

import org.apache.xmlbeans.XmlObject;

public class ReportHandler {
    public static int getTotal(XmlObject xo) {

        int sum = 0;
        PurchaseOrderDocument poDoc = (PurchaseOrderDocument) xo;
        Item[] items = poDoc.getPurchaseOrder().getItems().getItemArray();

        for (int i = 0; i < items.length; i++)
            sum += items[i].getUSPrice().intValue();
        return sum;
    }

    public static int getMinPrice(XmlObject xo) {
        PurchaseOrderDocument poDoc = (PurchaseOrderDocument) xo;
              Item[] items = poDoc.getPurchaseOrder().getItems().getItemArray();

        int min = Integer.MAX_VALUE;
        for (int i = 0; i < items.length; i++) {
            int tmp = items[i].getUSPrice().intValue();

            min = min > tmp ? tmp : min;
        }
        return min;

    }

    public static XmlObject getCheapestItem(XmlObject xo) {
        PurchaseOrderDocument poDoc = (PurchaseOrderDocument) xo;
        Item[] items = poDoc.getPurchaseOrder().getItems().getItemArray();

        int min = Integer.MAX_VALUE;
        XmlObject item = null;


        for (int i = 0; i < items.length; i++) {
            int tmp = items[i].getUSPrice().intValue();
            if (min > tmp) {
                min = tmp;
                item = items[i];
            }
        }
        return item;

    }

    public static void setMinPrice(XmlObject xo, double min_price) {
        PurchaseOrderDocument poDoc = (PurchaseOrderDocument) xo;
              Item[] items = poDoc.getPurchaseOrder().getItems().getItemArray();

                BigDecimal newPrice=new BigDecimal(min_price);

        for (int i = 0; i < items.length; i++) {

            int tmp = items[i].getUSPrice().intValue();
            if (min_price > tmp)
                items[i].setUSPrice(newPrice);
        }

    }
}