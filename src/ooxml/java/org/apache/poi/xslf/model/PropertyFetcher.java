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

package org.apache.poi.xslf.model;

import org.apache.poi.util.Internal;
import org.apache.poi.xslf.usermodel.XSLFShape;

/**
 *  Used internally to navigate the PresentationML text style hierarchy and fetch properties
 *
 * @author Yegor Kozlov
*/
@Internal
public abstract class PropertyFetcher<T> {
    private T _value;

    /**
     *
     * @param shape the shape being examined
     * @return true if the desired property was fetched
     */
    public abstract boolean fetch(XSLFShape shape);

    public T getValue(){
        return _value;
    }

    public void setValue(T val){
        _value = val;
    }
}
