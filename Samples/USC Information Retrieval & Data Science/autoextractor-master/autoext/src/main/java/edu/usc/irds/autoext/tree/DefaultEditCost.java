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
package edu.usc.irds.autoext.tree;

import edu.usc.irds.autoext.base.EditCost;

import java.io.Serializable;

/**
 * Default unit Costs for edit operations
 */
public class DefaultEditCost implements EditCost<TreeNode>, Serializable{

    private static final long serialVersionUID = -4846293473238639407L;
    private int insertCost = 1;
    private int removeCost = 1;
    private int replaceCost = 1;
    private int noEditCost = 0;
    private int maxEditCost = replaceCost;

    @Override
    public double getInsertCost(TreeNode node) {
        return insertCost;
    }

    @Override
    public double getRemoveCost(TreeNode node) {
        return removeCost;
    }

    @Override
    public double getReplaceCost(TreeNode node1, TreeNode node2) {
        return replaceCost;
    }

    @Override
    public double getNoEditCost() {
        return noEditCost;
    }

    @Override
    public double getMaxUnitCost() {
        return maxEditCost;
    }

    @Override
    public boolean isSymmetric() {
        return true;
    }
}
