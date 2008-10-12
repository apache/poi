/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.xssf.usermodel;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPageOrder;

/**
 * Specifies printed page order.
 *
 * @author Gisella Bronzetti
 */
public enum PageOrder {

    /**
     * Order pages vertically first, then move horizontally.
     */
    DOWN_THEN_OVER(STPageOrder.DOWN_THEN_OVER),
    /**
     * Order pages horizontally first, then move vertically
     */
    OVER_THEN_DOWN(STPageOrder.OVER_THEN_DOWN);


    private STPageOrder.Enum order;


    PageOrder(STPageOrder.Enum order) {
        this.order = order;
    }

    /**
     * Returns value of pages order
     *
     * @return String value of pages order
     */
    public STPageOrder.Enum getValue() {
        return order;
    }


    public static PageOrder valueOf(STPageOrder.Enum pageOrder) {
        switch (pageOrder.intValue()) {
            case STPageOrder.INT_DOWN_THEN_OVER:
                return DOWN_THEN_OVER;
            case STPageOrder.INT_OVER_THEN_DOWN:
                return OVER_THEN_DOWN;
        }
        throw new RuntimeException("PageOrder value [" + pageOrder + "] not supported");
    }
}
