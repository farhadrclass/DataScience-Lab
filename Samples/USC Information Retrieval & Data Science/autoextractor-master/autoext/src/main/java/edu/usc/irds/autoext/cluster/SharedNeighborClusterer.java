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
package edu.usc.irds.autoext.cluster;

import edu.usc.irds.autoext.tree.StructureSimComputer;
import edu.usc.irds.autoext.tree.TreeNode;
import edu.usc.irds.autoext.tree.ZSTEDComputer;
import edu.usc.irds.autoext.utils.Checks;
import edu.usc.irds.autoext.utils.ParseUtils;
import edu.usc.irds.autoext.utils.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * <h1>References : </h1>
 * <pre>
 * Jarvis, R.A.; Patrick, Edward A., "Clustering Using a Similarity Measure Based on Shared Near Neighbors,"
 * in Computers, IEEE Transactions on , vol.C-22, no.11, pp.1025-1034, Nov. 1973
 * </pre>
 *
 * @author  Thamme Gowda N
 *
 */
public class SharedNeighborClusterer {

    public static final Logger LOG = LoggerFactory.getLogger(SharedNeighborClusterer.class);
    public static Comparator<Tuple2<Double, Integer>> descendingComparator = new Comparator<Tuple2<Double, Integer>>() {
        @Override
        public int compare(Tuple2<Double, Integer> o1, Tuple2<Double, Integer> o2) {
            return Double.compare(o2.pos0, o1.pos0);
        }
    };

    /**
     * checks if the clusters needs to be merged into one
     * @param i cluster 1
     * @param j cluster 2
     * @param simThreshold minimum threshold to consider if the clusters are similar
     * @return true if clusters are similar; false otherwise
     */
    public static boolean areClustersSimilar(BitSet i, BitSet j, double simThreshold){
        return findOverlap(i, j) >= simThreshold;
    }

    /**
     * Finds the overlap between two bit sectors
     * @param first first bitset
     * @param second second bitset
     * @return overlap percent normalized to [0.0 1.0]
     */
    public static double findOverlap(BitSet first, BitSet second) {
        int a = first.cardinality();
        int b = second.cardinality();
        BitSet intersection = (BitSet) first.clone();
        intersection.and(second);
        int aIntersectB = intersection.cardinality();
        return (double) aIntersectB / (a + b - aIntersectB);
    }

    /**
     * Clusters documents
     * @param simMatrix similarity matrix, values in between [0.0 to 1.0] inclusive
     * @param labels labels for items in similarity matrix
     * @param simThreshold similarity threshold to treat that the items are similar, usually >= 0.8
     * @param k number of nearest neighbours to start with
     */
    public List<List<String>> cluster(double simMatrix[][], String[] labels,
                                      double simThreshold,
                                      int k){
        long statTime = System.currentTimeMillis();
        Checks.check(simMatrix.length == labels.length,
                "Couldn't match labels to similarity matrix ");

        //computing the table
        List<BitSet> table = new LinkedList<>();
        for (double[] simRow : simMatrix) {
            table.add(findNearestNeighbors(simRow, simThreshold, k));
        }

        int maxIterations = 100;
        LOG.debug("Starting to cluster {} elements, max iterations={}",
                labels.length, maxIterations);
        int iteration = 0;
        int numCollapses;
        do {
            numCollapses = 0;
            long st = System.currentTimeMillis();
            for (int i = 0; i < table.size(); i++) {
                for (int j = i + 1; j < table.size(); j++) {
                    if (areClustersSimilar(table.get(i), table.get(j), simThreshold)) {
                        // threshold or more neighbors in the intersection, collapse this cluster
                        numCollapses++;
                        // drop j
                        table.remove(j);
                        // replace j's index with i's index everywhere else
                        for (int l = j ; l < table.size(); l++) {
                            BitSet set = table.get(l);
                            if (set.get(j)) {
                                set.clear(j);
                                set.set(i);
                            }
                        }
                        //FIXME : possible bug with replacement strategy, test again
                    }
                }
            }
            LOG.debug("Iteration {} took {}ms", iteration, (System.currentTimeMillis() - st));
            LOG.debug("Iteration {} made {} collapses, num Clusters = {}", iteration, numCollapses, table.size());
            iteration++;
        } while (numCollapses > 0 && iteration < maxIterations);
        //if you found this code interesting, the credit goes to
        // authors of paper "Clustering Using a Similarity Measure Based on Shared Near Neighbors"

        List<List<String>> clusters = makeClusters(labels, table);
        LOG.info("Formed {} clusters from {} items, in {}ms time",
                clusters.size(), labels.length, System.currentTimeMillis() - statTime);
        return clusters;
    }

    /**
     * Constructs clusters of input items
     * @param labels item names or identifiers
     * @param table bitset table having cluster information
     * @return List of clusters
     */
    private List<List<String>> makeClusters(String[] labels, List<BitSet> table) {
        List<List<String>> result = new ArrayList<>();
        for (BitSet bs : table) {
            List<String> cluster = new ArrayList<>();
            for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
                // operate on index i here
                cluster.add(labels[i]);
                if (i == Integer.MAX_VALUE) {
                    break; // or (i+1) would overflow
                }
            }
            result.add(cluster);
        }
        return result;
    }

    /**
     * Finds nearest neighbors based on similarity measures
     * @param similarity similarity measure of an item with all possible nodes
     * @param simThreshold cut off similarity to make the computations faster.
     *                     Anything below this will be ignored
     * @param k number of neighbors to be picked at max
     * @return bit sequence representation of nearest neighbors
     */
    public BitSet findNearestNeighbors(double[] similarity, double simThreshold, int k){

        SortedSet<Tuple2<Double, Integer>> nearests = new TreeSet<>(descendingComparator);
        int n = similarity.length;
        // the given node itself will have 1.0 score which is the highest similarity,
        // so no need to add it at the zeroth position explicitly
        for (int i = 0; i < n; i++) {
            if (similarity[i] >= simThreshold) {
                nearests.add(new Tuple2<>(similarity[i], i));
            }
        }
        BitSet nearestNeighbors = new BitSet();
        int count = 0;
        for (Tuple2<Double, Integer> nearest : nearests) {
            nearestNeighbors.set(nearest.pos1);
            count++;
            if (count >= k){
                //pick nearest k
                break;
            }
        }
        return nearestNeighbors;
    }

    public static void main(String[] args) throws IOException, SAXException {
        if (args.length != 1) {
            System.err.println("Invalid Args!");
            System.err.println("Usage : <path to directory of html files>");
            System.exit(1);
            return;
        }
        String dir = args[0];
        String[] fileNames = new File(dir).list();
        File[] files = new File(dir).listFiles();

        StructureSimComputer computer = new StructureSimComputer(new ZSTEDComputer());
        List<TreeNode> nodes = new ArrayList<>();
        for (File file : files) {
            Document doc = ParseUtils.parseFile(file.getAbsolutePath());
            nodes.add(new TreeNode(doc, null));
        }
        System.out.println("Number of trees found :" + nodes.size());
        double[][] sims = computer.compute(nodes);
        SharedNeighborClusterer clusterer = new SharedNeighborClusterer();
        List<List<String>> list = clusterer.cluster(sims, fileNames, 0.75, 100);
        System.out.println(list);
    }
}
