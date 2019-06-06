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

package org.apache.poi.hemf.record.emfplus;

import static org.apache.poi.hemf.record.emfplus.HemfPlusDraw.readRectF;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.poi.hemf.draw.HemfGraphics;
import org.apache.poi.hemf.record.emfplus.HemfPlusHeader.EmfPlusGraphicsVersion;
import org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObjectData;
import org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObjectType;
import org.apache.poi.hemf.record.emfplus.HemfPlusPath.EmfPlusPath;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;

public class HemfPlusRegion {
    public enum EmfPlusRegionNodeDataType {
        /**
         * Specifies a region node with child nodes. A Boolean AND operation SHOULD be applied to the left and right
         * child nodes specified by an EmfPlusRegionNodeChildNodes object
         */
        AND(0X00000001, EmfPlusRegionNode::new),
        /**
         * Specifies a region node with child nodes. A Boolean OR operation SHOULD be applied to the left and right
         * child nodes specified by an EmfPlusRegionNodeChildNodes object.
         */
        OR(0X00000002, EmfPlusRegionNode::new),
        /**
         * Specifies a region node with child nodes. A Boolean XOR operation SHOULD be applied to the left and right
         * child nodes specified by an EmfPlusRegionNodeChildNodes object.
         */
        XOR(0X00000003, EmfPlusRegionNode::new),
        /**
         * Specifies a region node with child nodes. A Boolean operation, defined as "the part of region 1 that is excluded
         * from region 2", SHOULD be applied to the left and right child nodes specified by an EmfPlusRegionNodeChildNodes object.
         */
        EXCLUDE(0X00000004, EmfPlusRegionNode::new),
        /**
         * Specifies a region node with child nodes. A Boolean operation, defined as "the part of region 2 that is excluded
         * from region 1", SHOULD be applied to the left and right child nodes specified by an EmfPlusRegionNodeChildNodes object.
         */
        COMPLEMENT(0X00000005, EmfPlusRegionNode::new),
        /**
         * Specifies a region node with no child nodes.
         * The RegionNodeData field SHOULD specify a boundary with an EmfPlusRectF object.
         */
        RECT(0X10000000, EmfPlusRegionRect::new),
        /**
         * Specifies a region node with no child nodes.
         * The RegionNodeData field SHOULD specify a boundary with an EmfPlusRegionNodePath object
         */
        PATH(0X10000001, EmfPlusRegionPath::new),
        /** Specifies a region node with no child nodes. The RegionNodeData field SHOULD NOT be present. */
        EMPTY(0X10000002, EmfPlusRegionEmpty::new),
        /** Specifies a region node with no child nodes, and its bounds are not defined. */
        INFINITE(0X10000003, EmfPlusRegionInfinite::new)
        ;

        public final int id;
        public final Supplier<EmfPlusRegionNodeData> constructor;

        EmfPlusRegionNodeDataType(int id, Supplier<EmfPlusRegionNodeData> constructor) {
            this.id = id;
            this.constructor = constructor;
        }

        public static EmfPlusRegionNodeDataType valueOf(int id) {
            for (EmfPlusRegionNodeDataType wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }
    }

    public static class EmfPlusRegion implements EmfPlusObjectData {

        private final EmfPlusGraphicsVersion graphicsVersion = new EmfPlusGraphicsVersion();
        private EmfPlusRegionNodeData regionNode;

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, EmfPlusObjectType objectType, int flags) throws IOException {
            long size = graphicsVersion.init(leis);

            // A 32-bit unsigned integer that specifies the number of child nodes in the RegionNode field.
            int nodeCount = leis.readInt();
            size += LittleEndianConsts.INT_SIZE;

            // An array of RegionNodeCount+1 EmfPlusRegionNode objects. Regions are specified as a binary tree of
            // region nodes, and each node MUST either be a terminal node or specify one or two child nodes.
            // RegionNode MUST contain at least one element.
            size += readNode(leis, d -> regionNode = d);

            return size;
        }

        @Override
        public void applyObject(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData) {

        }

        @Override
        public EmfPlusGraphicsVersion getGraphicsVersion() {
            return graphicsVersion;
        }
    }


    public interface EmfPlusRegionNodeData {
        long init(LittleEndianInputStream leis) throws IOException;
    }

    public static class EmfPlusRegionPath extends EmfPlusPath implements EmfPlusRegionNodeData {
        public long init(LittleEndianInputStream leis) throws IOException {
            int dataSize = leis.readInt();
            return super.init(leis, dataSize, EmfPlusObjectType.PATH, 0) + LittleEndianConsts.INT_SIZE;
        }
    }

    public static class EmfPlusRegionInfinite implements EmfPlusRegionNodeData {
        @Override
        public long init(LittleEndianInputStream leis) throws IOException {
            return 0;
        }
    }

    public static class EmfPlusRegionEmpty implements EmfPlusRegionNodeData {
        @Override
        public long init(LittleEndianInputStream leis) throws IOException {
            return 0;
        }
    }

    public static class EmfPlusRegionRect implements EmfPlusRegionNodeData {
        private final Rectangle2D rect = new Rectangle2D.Double();

        @Override
        public long init(LittleEndianInputStream leis) {
            return readRectF(leis, rect);
        }
    }

    public static class EmfPlusRegionNode implements EmfPlusRegionNodeData {
        private EmfPlusRegionNodeData left, right;

        @Override
        public long init(LittleEndianInputStream leis) throws IOException {
            long size = readNode(leis, n -> left = n);
            size += readNode(leis, n -> right = n);
            return size;
        }
    }


    private static long readNode(LittleEndianInputStream leis, Consumer<EmfPlusRegionNodeData> con) throws IOException {
        // A 32-bit unsigned integer that specifies the type of data in the RegionNodeData field.
        // This value MUST be defined in the RegionNodeDataType enumeration
        EmfPlusRegionNodeDataType type = EmfPlusRegionNodeDataType.valueOf(leis.readInt());
        assert(type != null);
        EmfPlusRegionNodeData nd = type.constructor.get();
        con.accept(nd);
        return LittleEndianConsts.INT_SIZE + nd.init(leis);
    }
}
