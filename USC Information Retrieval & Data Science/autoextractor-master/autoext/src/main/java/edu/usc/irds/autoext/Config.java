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
package edu.usc.irds.autoext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The configuration framework.
 */
public class Config {

    public static final String CONFIG_FILE = "autoext.properties";
    public static final Properties DEF_PROPS = new Properties();
    public static final Config INSTANCE;

    static {
        try(InputStream stream = Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE)){
            DEF_PROPS.load(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        INSTANCE = new Config(DEF_PROPS);
    }

    public static Config getInstance(){
        return INSTANCE;
    }

    private String tedImpl;
    private double simWeight;

    public Config(){
        this(DEF_PROPS);
    }

    public Config(Properties props) {
        this.tedImpl = props.getProperty("ted.impl").trim();
        this.simWeight = Double.parseDouble(props.getProperty("sim.weight").trim());
    }

    public String getTedImpl() {
        return tedImpl;
    }

    public double getSimWeight() {
        return simWeight;
    }

}

