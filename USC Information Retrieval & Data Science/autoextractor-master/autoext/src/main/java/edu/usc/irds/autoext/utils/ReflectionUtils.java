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

import edu.usc.irds.autoext.base.EditDistanceComputer;
import edu.usc.irds.autoext.tree.TreeNode;

/**
 * Created by tg on 2/29/16.
 */
public class ReflectionUtils {

    /**
     * this method instantiates a class
     * @param clsName name of class
     * @return instance
     *
     */
    public static <T> T instantiate(String clsName) {
        try {
            Class<?> aClass = Class.forName(clsName, true, ReflectionUtils.class.getClassLoader());
            Object instance = aClass.newInstance();
            return (T) instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * this method instantiates an instance of edit distance computer
     * @param clsName name of class
     * @return
     */
    public static EditDistanceComputer<TreeNode> intantiateEDComputer(String clsName){
        return instantiate(clsName);
    }
}
