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

import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by tg on 1/5/16.
 */
public class ParseUtils {

    private static final DOMParser domParser = new DOMParser();

    public static Document parseFile(String path) throws IOException, SAXException {
        synchronized (domParser) {
            domParser.parse(new InputSource(new FileInputStream(path)));
            Document document = domParser.getDocument();
            domParser.reset();
            return document;
        }
    }

    public static Document parseURL(URL url) throws IOException, SAXException {
        try (InputStream stream = url.openStream()) {
            synchronized (domParser) {
                domParser.parse(new InputSource(stream));
                Document document = domParser.getDocument();
                domParser.reset();
                return document;
            }
        }
    }
}
