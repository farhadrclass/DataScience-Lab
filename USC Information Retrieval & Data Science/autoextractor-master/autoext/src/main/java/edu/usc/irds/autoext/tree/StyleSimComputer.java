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

import edu.usc.irds.autoext.base.SimilarityComputer;
import edu.usc.irds.autoext.utils.XPathEvaluator;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.Set;

/**
 * Computes CSS style Similarity between two DOM trees
 */
public class StyleSimComputer extends SimilarityComputer<TreeNode> implements Serializable {

    private static final long serialVersionUID = 6680072428272456472L;
    private static XPathEvaluator xPathUtil = new XPathEvaluator();

    /**
     * Computes the stylistic similarity
     * @param elem1 first element
     * @param elem2 second element
     * @returnt the style similarity
     */
    public double compute(Element elem1, Element elem2) {
        Set<String> setA = xPathUtil.findUniqueClassNames(elem1);
        Set<String> setB = xPathUtil.findUniqueClassNames(elem2);
        int modA = setA.size();
        int modB = setB.size();
        if (modA == 0 && modB == 0) {
            //Cant be determined by jaccards similarity;
            // however, by definition, they are very similar in empty style
            return 1.0;
        }
        int intersectSize = countIntersection(setA, setB);
        // the jaccards similarity
        return (double) intersectSize / (modA + modB - intersectSize);
    }

    /**
     * Computes the size of intersection of two sets
     * @param small first set. preferably smaller than the second argument
     * @param large second set;
     * @param <T> the type
     * @return size of intersection of sets
     */
    public <T> int countIntersection(Set<T> small, Set<T> large){
        //assuming first argument to be smaller than the later;
        //however double checking to be sure
        if (small.size() > large.size()) {
            //swap the references;
            Set<T> tmp = small;
            small = large;
            large = tmp;
        }
        int result = 0;
        for (T item : small) {
            if (large.contains(item)){
                //item found in both the sets
                result++;
            }
        }
        return result;
    }


    @Override
    public double compute(TreeNode obj1, TreeNode obj2) {
        //TODO: resolve the casts.. This could cause type cast errors
        return compute((Element) obj1.innerNode, (Element) obj2.innerNode);
    }
}
