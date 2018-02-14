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

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Created by tg on 12/29/15.
 */
public class ZSTEDComputerTest {

    @Test
    public void testMain() throws Exception {
        ClassLoader resLoader = getClass().getClassLoader();
        String file1 = resLoader.getResource("html/simple/1.html").getPath();
        String file2 = resLoader.getResource("html/simple/2.html").getPath();
        String file3 = resLoader.getResource("html/simple/3.html").getPath();
        double distance;
        //same file
        distance = ZSTEDComputer.computeDistance(new File(file1), new File(file1));
        assertEquals(0.0, distance, 0.00);

        //almost same
        distance = ZSTEDComputer.computeDistance(new File(file1), new File(file2));
        assertEquals(3.0, distance, 0.00);
        //if(true) return;
        //dissimilar
        distance = ZSTEDComputer.computeDistance(new File(file1), new File(file3));
        assertEquals(10.0, distance, 0.00);

    }
}