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

package org.apache.poi.xslf.model.geom;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * A guide formula in DrawingML.
 * This is a base class for adjust values, geometric guides and bilt-in guides
 *
 * @author Yegor Kozlov
 */
public abstract class Formula {

    String getName(){
        return null;
    }

    abstract double evaluate(Context ctx);

    static Map<String, Formula> builtInFormulas = new HashMap<String, Formula>();
    static {
        // 3 x 360 / 4 = 270
        builtInFormulas.put("3cd4",  new Formula(){
            @Override
            double evaluate(Context ctx){
                return 270 * 60000;
            }

         });

        // 3 x 360 / 8 = 135
        builtInFormulas.put("3cd8",  new Formula(){
            @Override
            double evaluate(Context ctx){
                return 135 * 60000;
            }

         });

        // 5 x 360 / 8 = 225
        builtInFormulas.put("5cd8",  new Formula(){
            @Override
            double evaluate(Context ctx){
                return 270 * 60000;
            }

         });

        // 7 x 360 / 8 = 315
        builtInFormulas.put("7cd8",  new Formula(){
            @Override
            double evaluate(Context ctx){
                return 270 * 60000;
            }

         });

        // bottom
        builtInFormulas.put("b",  new Formula(){
            @Override
            double evaluate(Context ctx){
                Rectangle2D anchor = ctx.getShapeAnchor();
                return anchor.getY() + anchor.getHeight();
            }

         });

        // 360 / 2 = 180
        builtInFormulas.put("cd2",  new Formula(){
            @Override
            double evaluate(Context ctx){
                return 180 * 60000;
            }

         });

        // 360 / 4 = 90
        builtInFormulas.put("cd4",  new Formula(){
            @Override
            double evaluate(Context ctx){
                return 90 * 60000;
            }

         });

        // 360 / 8 = 45
        builtInFormulas.put("cd8",  new Formula(){
            @Override
            double evaluate(Context ctx){
                return 45 * 60000;
            }

         });

        // horizontal center
        builtInFormulas.put("hc",  new Formula(){
            @Override
            double evaluate(Context ctx){
                Rectangle2D anchor = ctx.getShapeAnchor();
                return anchor.getX() + anchor.getWidth()/2;
            }

         });

        // height
        builtInFormulas.put("h",  new Formula(){
            @Override
            double evaluate(Context ctx){
                Rectangle2D anchor = ctx.getShapeAnchor();
                return anchor.getHeight();
            }

         });

        // height / 2
        builtInFormulas.put("hd2",  new Formula(){
            @Override
            double evaluate(Context ctx){
                Rectangle2D anchor = ctx.getShapeAnchor();
                return anchor.getHeight()/2;
            }

         });

        // height / 3
        builtInFormulas.put("hd3",  new Formula(){
            @Override
            double evaluate(Context ctx){
                Rectangle2D anchor = ctx.getShapeAnchor();
                return anchor.getHeight()/3;
            }

         });

        // height / 4
        builtInFormulas.put("hd4",  new Formula(){
            @Override
            double evaluate(Context ctx){
                Rectangle2D anchor = ctx.getShapeAnchor();
                return anchor.getHeight()/4;
            }

         });

        // height / 5
        builtInFormulas.put("hd5",  new Formula(){
            @Override
            double evaluate(Context ctx){
                Rectangle2D anchor = ctx.getShapeAnchor();
                return anchor.getHeight()/5;
            }

         });

        // height / 6
        builtInFormulas.put("hd6",  new Formula(){
            @Override
            double evaluate(Context ctx){
                Rectangle2D anchor = ctx.getShapeAnchor();
                return anchor.getHeight()/6;
            }

         });

        // height / 8
        builtInFormulas.put("hd8",  new Formula(){
            @Override
            double evaluate(Context ctx){
                Rectangle2D anchor = ctx.getShapeAnchor();
                return anchor.getHeight()/8;
            }

         });

        // left
        builtInFormulas.put("l",  new Formula(){
            @Override
            double evaluate(Context ctx){
                Rectangle2D anchor = ctx.getShapeAnchor();
                return anchor.getX();
            }

         });

        // long side
        builtInFormulas.put("ls",  new Formula(){
            @Override
            double evaluate(Context ctx){
                Rectangle2D anchor = ctx.getShapeAnchor();
                return Math.max(anchor.getWidth(), anchor.getHeight());
            }

         });

        // right
        builtInFormulas.put("r",  new Formula(){
            @Override
            double evaluate(Context ctx){
                Rectangle2D anchor = ctx.getShapeAnchor();
                return anchor.getX() + anchor.getWidth();
            }

         });

        // short side
        builtInFormulas.put("ss",  new Formula(){
            @Override
            double evaluate(Context ctx){
                Rectangle2D anchor = ctx.getShapeAnchor();
                return Math.min(anchor.getWidth(), anchor.getHeight());
            }

         });

        // short side / 2
        builtInFormulas.put("ssd2",  new Formula(){
            @Override
            double evaluate(Context ctx){
                Rectangle2D anchor = ctx.getShapeAnchor();
                double ss = Math.min(anchor.getWidth(), anchor.getHeight());
                return ss / 2;
            }
         });

        // short side / 4
        builtInFormulas.put("ssd4",  new Formula(){
            @Override
            double evaluate(Context ctx){
                Rectangle2D anchor = ctx.getShapeAnchor();
                double ss = Math.min(anchor.getWidth(), anchor.getHeight());
                return ss / 4;
            }
         });

        // short side / 6
        builtInFormulas.put("ssd6",  new Formula(){
            @Override
            double evaluate(Context ctx){
                Rectangle2D anchor = ctx.getShapeAnchor();
                double ss = Math.min(anchor.getWidth(), anchor.getHeight());
                return ss / 6;
            }
         });

        // short side / 8
        builtInFormulas.put("ssd8",  new Formula(){
            @Override
            double evaluate(Context ctx){
                Rectangle2D anchor = ctx.getShapeAnchor();
                double ss = Math.min(anchor.getWidth(), anchor.getHeight());
                return ss / 8;
            }
         });

        // short side / 16
        builtInFormulas.put("ssd16",  new Formula(){
            @Override
            double evaluate(Context ctx){
                Rectangle2D anchor = ctx.getShapeAnchor();
                double ss = Math.min(anchor.getWidth(), anchor.getHeight());
                return ss / 16;
            }
         });

        // short side / 32
        builtInFormulas.put("ssd32",  new Formula(){
            @Override
            double evaluate(Context ctx){
                Rectangle2D anchor = ctx.getShapeAnchor();
                double ss = Math.min(anchor.getWidth(), anchor.getHeight());
                return ss / 32;
            }
         });

        // top
        builtInFormulas.put("t",  new Formula(){
            @Override
            double evaluate(Context ctx){
                return ctx.getShapeAnchor().getY();
            }
         });

        // vertical center
        builtInFormulas.put("vc",  new Formula(){
            @Override
            double evaluate(Context ctx){
                Rectangle2D anchor = ctx.getShapeAnchor();
                return anchor.getY() + anchor.getHeight()/2;
            }
         });

        // width
        builtInFormulas.put("w",  new Formula(){
            @Override
            double evaluate(Context ctx){
                return ctx.getShapeAnchor().getWidth();
            }
         });

        // width / 2
        builtInFormulas.put("wd2",  new Formula(){
            @Override
            double evaluate(Context ctx){
                return ctx.getShapeAnchor().getWidth()/2;
            }
         });

        // width / 3
        builtInFormulas.put("wd3",  new Formula(){
            @Override
            double evaluate(Context ctx){
                return ctx.getShapeAnchor().getWidth()/3;
            }
         });

        // width / 4
        builtInFormulas.put("wd4",  new Formula(){
            @Override
            double evaluate(Context ctx){
                return ctx.getShapeAnchor().getWidth()/4;
            }
         });

        // width / 5
        builtInFormulas.put("wd5",  new Formula(){
            @Override
            double evaluate(Context ctx){
                return ctx.getShapeAnchor().getWidth()/5;
            }
         });

        // width / 6
        builtInFormulas.put("wd6",  new Formula(){
            @Override
            double evaluate(Context ctx){
                return ctx.getShapeAnchor().getWidth()/6;
            }
         });

        // width / 8
        builtInFormulas.put("wd8",  new Formula(){
            @Override
            double evaluate(Context ctx){
                return ctx.getShapeAnchor().getWidth()/8;
            }
         });

        // width / 10
        builtInFormulas.put("wd10",  new Formula(){
            @Override
            double evaluate(Context ctx){
                return ctx.getShapeAnchor().getWidth()/10;
            }
         });

        // width / 32
        builtInFormulas.put("wd32",  new Formula(){
            @Override
            double evaluate(Context ctx){
                return ctx.getShapeAnchor().getWidth()/32;
            }
         });
    }

}
