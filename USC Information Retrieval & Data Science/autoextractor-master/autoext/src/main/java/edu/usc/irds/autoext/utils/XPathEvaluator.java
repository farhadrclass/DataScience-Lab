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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An utility for evaluating XPath expressions on Documents
 * @author Thamme Gowda
 * @since Jan 16, 2016
 */
public class XPathEvaluator implements Serializable {

    public static final Logger LOG = LoggerFactory.getLogger(XPathEvaluator.class.getName());
    private static final String CLASS_VAL_XPATH = "//*[@class]/@class";
    private static final long serialVersionUID = -4886553689128529323L;

    private XPathFactory xPathFactory;
    private XPathExpression cssClassValExprsn;

    public XPathEvaluator() {
        xPathFactory = XPathFactory.newInstance();
        try {
            cssClassValExprsn = compile(CLASS_VAL_XPATH);
        } catch (XPathExpressionException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public XPathExpression compile(String expression) throws XPathExpressionException {
        return xPathFactory.newXPath().compile(expression);
    }

    /**
     * Evaluates the given xpath expression on input DOM Element
     * @param element Root element
     * @param expression Xpath expression
     * @return List of Nodes obtained by evaluating the nodes
     * @throws XPathExpressionException when the xpath expression is invalid
     */
    public NodeList eval(Element element, XPathExpression expression)
            throws XPathExpressionException {
        return (NodeList) expression.evaluate(element, XPathConstants.NODESET);
    }


    /**
     * Finds all unique class names from a DOM tree rooted at given element
     * @param element the root element of the DOM tree
     * @return  Set of class names
     */
    public Set<String> findUniqueClassNames(Element element){
        try {
            NodeList list = eval(element, cssClassValExprsn);
            Set<String> cssClasses = new HashSet<>();
            for (int i = 0; i < list.getLength(); i++) {
                Collections.addAll(cssClasses,
                        list.item(i).getTextContent().trim().split("\\s+"));
            }
            return cssClasses;
        } catch (XPathExpressionException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
