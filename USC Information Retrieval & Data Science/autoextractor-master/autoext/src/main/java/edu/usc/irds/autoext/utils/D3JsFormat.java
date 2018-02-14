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
package edu.usc.irds.autoext.utils;

import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities for transforming data to d3js format
 */
public class D3JsFormat {

    public static final String INDEX_KEY = "index";
    public static final String CHILDREN_KEY = "children";
    public static final String SIZE = "size";
    public static final String NAME_KEY = "name";
    public static final String CREATED_AT = "createdAt";

    /**
     *
     * @param name name for top level cluster
     * @param clusters cluster details
     * @param nameMap mapping indices back to labels
     * @param scaleFactor scale factor for magnifying the cluster size
     */
    public static String formatClusters(String name,
                                        Map<Integer, List<Integer>> clusters,
                                        Map<Integer, String> nameMap,
                                        final double scaleFactor){

        final Map<Integer, String> nameMapFinal = nameMap == null ?
                new HashMap<Integer, String>() : nameMap;

        Map<String, Object> result = new HashMap<>();
        result.put(NAME_KEY, name);
        result.put(INDEX_KEY, -1);
        result.put(SIZE, clusters.size() * scaleFactor);
        result.put(CREATED_AT, System.currentTimeMillis());

        List<Object> level1 = new ArrayList<>();
        result.put(CHILDREN_KEY, level1);
        for (Map.Entry<Integer, List<Integer>> entry : clusters.entrySet()) {
            Map<String, Object> child = new HashMap<>();
            level1.add(child);
            Integer key = entry.getKey();
            child.put(INDEX_KEY, key);
            child.put(NAME_KEY, nameMapFinal.containsKey(key) ? nameMapFinal.get(key): "" + key);
            child.put(SIZE, entry.getValue().size() * scaleFactor);
            List<Object> level2 = new ArrayList<>();
            child.put(CHILDREN_KEY, level2);

            for (final Integer item: entry.getValue()){
                Map<String, Object> node = new HashMap<>();
                node.put(INDEX_KEY, item);
                node.put(NAME_KEY, nameMapFinal.containsKey(item)? nameMapFinal.get(item) : ""+ item);
                node.put(SIZE, scaleFactor);
                level2.add(node);
            }
        }
        return new Gson().toJson(result);
    }

    /**
     * Stores the clusters data to a json file
     * @param file path to file
     * @param name name for the top level cluster
     * @param clusters cluster data
     * @param nameMap map of index to labels
     * @param scaleFactor factor to scale
     * @throws IOException when an error occurs while writing to file
     */
    public static void storeClusters(String file, String name,
                                     Map<Integer, List<Integer>> clusters,
                                     final Map<Integer, String> nameMap, float scaleFactor)
            throws IOException {
        String result = formatClusters(name, clusters, nameMap, scaleFactor);
        try(Writer writer = new FileWriter(file)){
            writer.write(result);
        }
    }

}
