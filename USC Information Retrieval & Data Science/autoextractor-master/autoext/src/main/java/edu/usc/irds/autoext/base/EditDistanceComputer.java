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

/**
 * Defines a contract for edit distance computer
 *
 * @author Thamme Gowda
 */
public interface EditDistanceComputer<T> {

    /**
     * Computes edit distance between two similar objects
     * @param object1 the first object
     * @param object2 the second object
     * @return the edit distance measure
     */
    double computeDistance(T object1, T object2);


    /**
     * Gets cost metric used for computing the edit distance
     * @return edit cost metric
     */
    EditCost<T> getCostMetric();

}
