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
 * Created by tg on 1/4/16.
 */
public class Checks {

    /**
     * A custom {@link RuntimeException} to indicate that a check has failed
     */
    public static class CheckFailedException extends RuntimeException{
        /**
         * creates an exception
         * @param message message to describe why this exception was raised.
         */
        public CheckFailedException(String message) {
            super(message);
        }
    }

    /**
     * Checks boolean condition, on failure raises {@link CheckFailedException}
     * @param condition predicate
     * @param message error message to assist debug task when the condition fails
     */
    public static void check(boolean condition, String message){
        if (!condition) {
            throw new CheckFailedException(message);
        }
    }
}
