/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.hssf.model;

import junit.framework.TestCase;
import org.apache.poi.hssf.record.CommonObjectDataSubRecord;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFComment;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFTextbox;

/**
 *
 * @author Yegor Kozlov
 */
public final class TestShapes extends TestCase {

    /**
     * Test generator of ids for the CommonObjectDataSubRecord record.
     *
     * See Bug 51332
     */
    public void testShapeId(){

        HSSFClientAnchor anchor = new HSSFClientAnchor();
        AbstractShape shape;
        CommonObjectDataSubRecord cmo;

        shape = new TextboxShape(new HSSFTextbox(null, anchor), 1025);
        cmo = (CommonObjectDataSubRecord)shape.getObjRecord().getSubRecords().get(0);
        assertEquals(1, cmo.getObjectId());

        shape = new PictureShape(new HSSFPicture(null, anchor), 1026);
        cmo = (CommonObjectDataSubRecord)shape.getObjRecord().getSubRecords().get(0);
        assertEquals(2, cmo.getObjectId());

        shape = new CommentShape(new HSSFComment(null, anchor), 1027);
        cmo = (CommonObjectDataSubRecord)shape.getObjRecord().getSubRecords().get(0);
        assertEquals(1027, cmo.getObjectId());
    }
}
