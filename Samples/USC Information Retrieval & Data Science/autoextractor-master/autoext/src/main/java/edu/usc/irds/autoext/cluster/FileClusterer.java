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
package edu.usc.irds.autoext.cluster;

import edu.usc.irds.autoext.tree.GrossSimComputer;
import edu.usc.irds.autoext.tree.StructureSimComputer;
import edu.usc.irds.autoext.tree.TreeNode;
import edu.usc.irds.autoext.tree.ZSTEDComputer;
import edu.usc.irds.autoext.utils.MatrixUtils;
import edu.usc.irds.autoext.utils.ParseUtils;
import edu.usc.irds.autoext.utils.Timer;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class offers commandline interface to make use of similarity and clustering
 * algorithms.
 *
 */
public class FileClusterer {

    public static final Logger LOG = LoggerFactory.getLogger(FileClusterer.class);
    public static final String IDS_FILE = "ids.txt";
    public static final String ED_DIST_FILE = "edit-distance.csv";
    public static final String TREE_SIM_FILE = "tree-sim.csv";
    public static final String GROSS_SIM_FILE = "gross-sim.csv";
    public static final String CLUSTER_FILE = "clusters.txt";
    public static final String REPORT_FILE = "report.txt";
    public static final char SEP = ',';

    @Option(name = "-list",
            required = true,
            usage = "path to a file containing paths to html files that requires clustering")
    private File listFile;

    @Option(name = "-workdir",
            required = true,
            usage = "Path to directory to create intermediate files and reports")
    private  File workDir;

    public void cluster() throws IOException {

        LOG.info("Create work directory ? {} ", workDir.mkdirs());
        File reportFile = new File(workDir, REPORT_FILE);
        try (PrintWriter report = new PrintWriter(
                new BufferedWriter(new FileWriter(reportFile)))) {
            Timer mainTimer = new Timer();
            Timer timer = new Timer();
            report.printf("Starting at : %d\n", timer.getStart());
            report.printf("Input specified : %s\n", listFile.getAbsolutePath());

            AtomicInteger skipCount = new AtomicInteger(0);
            List<TreeNode> trees = readTrees(skipCount);
            List<String> labels = new ArrayList<>();
            for (TreeNode tree : trees) {
                labels.add(tree.getExternalId());
            }
            report.printf("Parsed %d files and skipped %d files \n", trees.size(), skipCount.get());
            report.printf("Work Directory :%s\n", workDir.getAbsolutePath());
            report.printf("Time taken to parse : %dms\n", timer.reset());

            //Step1: write ids/paths to separate file
            File idsFile = new File(workDir, IDS_FILE);
            Files.write(idsFile.toPath(), labels, Charset.forName("UTF-8"));
            LOG.info("Wrote paths to {} ", idsFile.toPath());
            report.printf("Wrote %d ids to %s file in %dms\n", labels.size(), idsFile, timer.reset());

            //Step 2: Compute similarity and store to file
            GrossSimComputer<TreeNode> simComputer = GrossSimComputer.createWebSimilarityComputer();
            timer.reset();
            double[][] similarityMatrix = MatrixUtils.computeSymmetricMatrix(simComputer, trees);
            report.printf("Computed Gross similarity matrix in %dms\n", timer.reset());
            File similarityFile = new File(workDir, GROSS_SIM_FILE);
            writeToCSV(similarityMatrix, similarityFile);
            report.printf("Stored similarity matrix in %dms\n", timer.reset());

            //STEP 5: cluster
            SharedNeighborClusterer clusterer = new SharedNeighborClusterer();
            //TODO: make these configurable
            double similarityThreshold = 0.75;
            int k = 100;
            report.printf("Clustering:: SimilarityThreshold=%f," +
                    " no. of neighbors:%d\n", similarityThreshold, k);
            List<List<String>> clusters = clusterer.cluster(similarityMatrix,
                    labels.toArray(new String[labels.size()]), similarityThreshold, k);
            report.printf("Computed clusters in %dms\n", timer.reset());
            File clustersFile = new File(workDir, CLUSTER_FILE);
            writeClusters(clusters, clustersFile);
            report.printf("Wrote clusters in %dms\n", timer.reset());
            report.printf("Done! Total time = %dms\n", mainTimer.read());
        }
        LOG.info("Done.. Report stored in {} ", reportFile.getAbsolutePath());
    }

    /**
     * parses the files and builts trees
     * @param skipCounter the counter to be used to increment when some files are skipped
     * @return list of trees read
     * @throws IOException when an io error occures
     */
    private List<TreeNode> readTrees(AtomicInteger skipCounter) throws IOException {
        List<TreeNode> trees = new ArrayList<>();

        List<String> lines = Files.readAllLines(listFile.toPath(), Charset.forName("UTF-8"));
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            try {
                Document doc = ParseUtils.parseFile(line);
                TreeNode tree = new TreeNode(doc.getDocumentElement(), null);
                tree.setExternalId(line);
                trees.add(tree);
            } catch (IOException | SAXException e) {
                skipCounter.incrementAndGet();
                LOG.error("Skip : {}, reason:{}", line, e.getMessage());
            }
        }
        return trees;
    }

    /**
     * Writes clusters to a clusters file
     * @param clusters the clusters list
     * @param outputFile output file
     * @throws IOException when an io error occurs
     */
    public void writeClusters(List<List<String>> clusters, File outputFile ) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))){
            writer.write("##Total Clusters:" + clusters.size() + "\n");
            for (int i = 0; i < clusters.size(); i++) {
                writer.write("\n#Cluster:" + i + "\n");
                List<String> ids = clusters.get(i);
                for (String id : ids) {
                    writer.write(id);
                    writer.write("\n");
                }
            }
        }
    }

    /**
     * Writes given matrix to CSV file
     * @param matrix the matrix or table
     * @param csvFile the target csv file
     * @throws IOException when an IO error occurs
     */
    private void writeToCSV(double[][] matrix, File csvFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
            for (double[] row : matrix) {
                writer.write(String.valueOf(row[0]));
                for (int i = 1; i < row.length; i++) {
                    writer.append(SEP).append(String.valueOf(row[i]));
                }
                writer.write('\n');
            }
        }
    }

    public static void main(String[] args) throws IOException {
        //args = "-list in.list -workdir simple-work".split(" ");
        FileClusterer instance = new FileClusterer();
        CmdLineParser parser = new CmdLineParser(instance);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.out.println(e.getLocalizedMessage());
            parser.printUsage(System.out);
            System.exit(1);
        }
        instance.cluster();
    }
}
