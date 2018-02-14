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

/**
 * A tuple to store pair of values
 */
public class Tuple2<F, S> {
    public final F pos0;
    public final S pos1;

    public Tuple2(F pos0, S pos1) {
        this.pos0 = pos0;
        this.pos1 = pos1;
    }

    public F getPos0() {
        return pos0;
    }

    public S getPos1() {
        return pos1;
    }

    @Override
    public String toString() {
        return "(" + pos0 + ", " + pos1 + ")";
    }
}
