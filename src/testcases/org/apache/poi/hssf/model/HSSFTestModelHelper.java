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

package org.apache.poi.hssf.model;

import org.apache.poi.hssf.usermodel.HSSFComment;
import org.apache.poi.hssf.usermodel.HSSFPolygon;
import org.apache.poi.hssf.usermodel.HSSFTextbox;

/**
 * @author Evgeniy Berlog
 * @date 25.06.12
 */
public class HSSFTestModelHelper {
    public static TextboxShape createTextboxShape(int shapeId, HSSFTextbox textbox){
        return new TextboxShape(textbox, shapeId);
    }

    public static CommentShape createCommentShape(int shapeId, HSSFComment comment){
        return new CommentShape(comment, shapeId);
    }

    public static PolygonShape createPolygonShape(int shapeId, HSSFPolygon polygon){
        return new PolygonShape(polygon, shapeId);
    }
}
