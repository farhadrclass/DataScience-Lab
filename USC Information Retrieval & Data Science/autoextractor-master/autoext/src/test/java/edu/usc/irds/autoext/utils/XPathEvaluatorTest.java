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

import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpression;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by tg on 1/16/16.
 */
public class XPathEvaluatorTest {

    XPathEvaluator instance = new XPathEvaluator();
    Element docRoot ;
    {
        try {
            DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            docRoot = b.parse(getClass().getClassLoader()
                    .getResourceAsStream("html/simple/1.html")).getDocumentElement();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testEval() throws Exception {
        XPathExpression titleExpr = instance.compile("//title/text()");
        NodeList list = instance.eval(docRoot, titleExpr);
        assertEquals(1, list.getLength());
        assertEquals("This is my page 1", list.item(0).getTextContent());
    }

    @Test
    public void testFindUniqueClassNames() throws Exception {
        Set<String> names = instance.findUniqueClassNames(docRoot);
        assertEquals(6, names.size());
        assertTrue(names.contains("header"));
        assertTrue(names.contains("row"));
        assertTrue(names.contains("cell"));
        assertTrue(names.contains("col1"));
        assertTrue(names.contains("col2"));
        assertTrue(names.contains("table"));
    }
}