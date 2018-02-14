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

import edu.usc.irds.autoext.tree.TreeNode;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

/**
 * This is a CLI utility for converting an HTML file into
 * bracket notation labelled tree structure
 *
 */
public class BracketTreeGen {

    @Option(name = "-in", usage = "Path to HTML file", forbids = {"-url"})
    private File htmlFile;

    @Option(name = "-url", usage = "URL of HTML doc", forbids = {"-in"})
    private URL htmlURL;

    @Option(name = "-out", usage = "Path to output file to store bracket notation tree")
    private File output;

    public static void main(String[] args) throws IOException, SAXException {
        //args = "-out sample.tree -url https://www.youtube.com/".split(" ");
        BracketTreeGen treeGen = new BracketTreeGen();
        CmdLineParser parser = new CmdLineParser(treeGen);
        try {
            parser.parseArgument(args);
            if (treeGen.htmlFile == null && treeGen.htmlURL == null){
               throw new CmdLineException("Either '-in' or '-url' is required");
            }
        } catch (CmdLineException e) {
            System.out.println(e.getLocalizedMessage());
            parser.printUsage(System.out);
            System.exit(-1);
        }
        Document doc;
        if (treeGen.htmlFile != null) {
            doc =  ParseUtils.parseFile(treeGen.htmlFile.getPath());

        } else {
             doc =  ParseUtils.parseURL(treeGen.htmlURL);
        }
        TreeNode node = new TreeNode(doc.getDocumentElement(), null);
        String bracketNotation = node.toBracketNotation();
        if (treeGen.output != null) {
            treeGen.output.getAbsoluteFile().getParentFile().mkdirs();
            Files.write(treeGen.output.toPath(), bracketNotation.getBytes("UTF-8"));
            System.out.println("Output stored in " + treeGen.output);
        } else {
            // dump to STDOUT
            System.out.println(bracketNotation);
        }
    }
}
