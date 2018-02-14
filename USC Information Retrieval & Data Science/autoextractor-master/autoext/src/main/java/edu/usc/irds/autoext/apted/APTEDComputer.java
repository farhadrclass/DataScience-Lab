/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.usc.irds.autoext.apted;

import edu.usc.irds.autoext.base.EditCost;
import edu.usc.irds.autoext.base.EditDistanceComputer;
import edu.usc.irds.autoext.tree.TreeNode;
import edu.usc.irds.lang.Function;
import edu.usc.irds.ted.apted.APTED;
import edu.usc.irds.ted.apted.util.LblTree;

import java.io.Serializable;
import java.util.List;

/**
 *
 * This TED is based on AP-TED algorithm of Mateusz Pawlik and Nikolaus Augsten.
 * Refer to http://tree-edit-distance.dbresearch.uni-salzburg.at for more details
 *
 * @see APTED
 */
public class APTEDComputer
        implements EditDistanceComputer<TreeNode>, Serializable  {

    public static final float INSERT_COST = 1;
    public static final float DELETE_COST = 1;
    public static final float REPLACE_COST = 1;
    public static final float MAX_UNIT = Math.max(Math.max(INSERT_COST, DELETE_COST), REPLACE_COST);

    public static class APTEDMetric implements EditCost, Serializable{

        @Override
        public double getInsertCost(Object node) {
            return INSERT_COST;
        }

        @Override
        public double getRemoveCost(Object node) {
            return DELETE_COST;
        }

        @Override
        public double getReplaceCost(Object node1, Object node2) {
            return REPLACE_COST;
        }

        @Override
        public double getNoEditCost() {
            return 0;
        }

        @Override
        public double getMaxUnitCost() {
            return MAX_UNIT;
        }

        @Override
        public boolean isSymmetric() {
            return true;
        }
    }

    private APTEDMetric cost = new APTEDMetric();
    private StringToIntMapper idMapper = new StringToIntMapper();

    @Override
    public double computeDistance(TreeNode object1, TreeNode object2) {
        APTED ted = new APTED(DELETE_COST, INSERT_COST, REPLACE_COST);
        LblTree tree1 = transform(object1, idMapper);
        LblTree tree2 = transform(object2, idMapper);
        return ted.nonNormalizedTreeDist(tree1, tree2);
    }

    @Override
    public EditCost<TreeNode> getCostMetric() {
        return cost;
    }


    /**
     * Transforms TreeNode to LblNode
     * @param node TreeNode
     * @param idMapper mapper function that converts string id to integer id
     * @return an instance of LblTree
     */
    public static LblTree transform(TreeNode node, Function<String, Integer> idMapper){
        int treeID = idMapper != null ? idMapper.apply(node.getExternalId()) : -1;
        LblTree result = new LblTree(node.getNodeName(), treeID);
        List<TreeNode> children = node.getChildren();
        if (children != null) {
            for (TreeNode child : children) {
                result.add(transform(child, idMapper));
            }
        }
        return result;
    }


}
