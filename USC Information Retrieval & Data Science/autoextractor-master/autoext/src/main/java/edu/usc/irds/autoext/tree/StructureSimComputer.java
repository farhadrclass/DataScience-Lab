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

import edu.usc.irds.autoext.Config;
import edu.usc.irds.autoext.base.EditCost;
import edu.usc.irds.autoext.base.EditDistanceComputer;
import edu.usc.irds.autoext.base.SimilarityComputer;
import edu.usc.irds.autoext.utils.Checks;
import edu.usc.irds.autoext.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

/**
 *Computes the structural similarity between two DOM Trees
 *
 */
public class StructureSimComputer extends SimilarityComputer<TreeNode> implements Serializable{

    public static final Logger LOG = LoggerFactory.getLogger(StructureSimComputer.class);

    private static final long serialVersionUID = 5434501333215722663L;
    private final EditCost<TreeNode> costMetric;
    private EditDistanceComputer<TreeNode> distanceComputer;

    public StructureSimComputer(){
        String tedImpl = Config.getInstance().getTedImpl();
        LOG.info("TED = {}", tedImpl);
        this.distanceComputer = ReflectionUtils.instantiate(tedImpl);
        this.costMetric = distanceComputer.getCostMetric();
    }

    public StructureSimComputer(EditDistanceComputer<TreeNode> distanceComputer) {
        this(distanceComputer.getCostMetric());
        this.distanceComputer = distanceComputer;
    }

    public StructureSimComputer(EditCost<TreeNode> costMetric) {
        this.costMetric = costMetric;
    }

    /**
     * Computes similarity between the trees using edit distance measure
     * @param tree1 first tree
     * @param tree2 second tree
     * @return similarity measure
     */
    @Override
    public double compute(TreeNode tree1, TreeNode tree2){
        return computeSimilarity(distanceComputer.computeDistance(tree1, tree2),
                tree1.getSize(), tree2.getSize());
    }

    /**
     * Computes similarity between the trees using edit distance measure
     * @param distance first distance
     * @param size1 number of elements in first tree
     * @param size2 number of elements in second tree
     * @return similarity measure
     */
    public double computeSimilarity(double distance, int size1, int size2){
        //Wish I could speak java here instead of maths :-)
        return 1.0 - distance/(costMetric.getMaxUnitCost() * (size1 + size2));
    }
    /**
     * Computes similarity matrix
     * @param trees list of trees
     * @return similarity matrix
     */
    public double[][] compute(List<TreeNode> trees) {
        int n = trees.size();
        if (n < 2) {
            throw new IllegalArgumentException("At least two nodes should be given");
        }
        double matrix[][] = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = compute(trees.get(i), trees.get(j));
            }
        }
        return matrix;
    }

    /**
     * Computes similarity matrix from distance matrix
     * @param treeSizes the number/size of elements in each tree
     * @param distanceMatrix the distance matrix
     * @return similarity matrix
     */
    public double[][] compute(int[] treeSizes, double[][] distanceMatrix) {
        Checks.check(treeSizes.length == distanceMatrix.length, "The tree size must be same as the distance matrix's");
        Checks.check(distanceMatrix.length == distanceMatrix[0].length, "The matrix must have same rows and same columns");

        int n = treeSizes.length;
        double matrix[][] = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = computeSimilarity(distanceMatrix[i][j], treeSizes[i], treeSizes[j]);
            }
        }
        return matrix;
    }

}
