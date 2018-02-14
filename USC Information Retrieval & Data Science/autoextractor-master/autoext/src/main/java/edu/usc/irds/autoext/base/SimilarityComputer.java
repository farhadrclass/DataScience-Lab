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
package edu.usc.irds.autoext.base;

import edu.usc.irds.autoext.tree.StructureSimComputer;
import edu.usc.irds.autoext.tree.StyleSimComputer;
import edu.usc.irds.lang.BiFunction;


/**
 * Generic Similarity computer contract. Look into the implementations for specific details
 * @see StructureSimComputer
 * @see  StyleSimComputer
 * @author Thamme Gowda
 *
 */
public abstract class SimilarityComputer<T> implements BiFunction<T, T, Double> {

    /**
     * computes similarity between two objects. The similarity score is on [0.0, 1.0] scale inclusive.
     * The score of 1.0 indicates that argument {@code obj1} and {@code obj2} are extremely similar.
     * Similarity score of 0.0 indicates that both input objects are extremely dissimilar.
     * @param obj1 the first object
     * @param obj2  the second object
     * @return the similarity score [0.0, 1.0]
     */
    public abstract double compute(T obj1, T obj2);

    /**
     * Glues this contract with Functional programming
     * @param obj1  the first object
     * @param obj2 the second object
     * @return the similarity between first and second
     * @see #compute(Object, Object)
     */
    @Override
    public Double apply(T obj1, T obj2) {
        return this.compute(obj1, obj2);
    }
}
