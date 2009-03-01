package org.antlr.mojo.antlr3;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.antlr.Tool;
import org.antlr.tool.ErrorManager;
import org.antlr.tool.Grammar;
import org.antlr.tool.CompositeGrammar;
import org.antlr.tool.BuildDependencyGenerator;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.*;

import antlr.TokenStreamException;
import antlr.RecognitionException;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * An extension to the Antlr Tool class that allows programmatic configuration.  It also provides for setting a
 * "base directory" that relative file paths can be resolved from.  Also adds a method to sort a list of files such
 * that each file will come after any files that it is dependent on.  This is currently based exclusively on the
 * "tokenVocab" option.
 * User: jbunting
 * Date: Oct 30, 2008
 * Time: 10:57:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigurableTool extends Tool {

    protected String baseDirectory;
    private Map<String, ExtendedBuildDependencyGenerator> buildDependencyGenerators =
            new HashMap<String, ExtendedBuildDependencyGenerator>();

    public File getFileFor(String filename) {
        return new File(baseDirectory, filename);
    }

    public String getBaseDirectory() {
        return this.baseDirectory;
    }

    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public String getOutputDirectory() {
        return this.outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        if ( outputDirectory.endsWith("/") ||
             outputDirectory.endsWith("\\") )
        {
            outputDirectory =
                outputDirectory.substring(0,outputDirectory.length()-1);
        }
        File outDir = new File(outputDirectory);
        if( outDir.exists() && !outDir.isDirectory() ) {
            ErrorManager.error(ErrorManager.MSG_OUTPUT_DIR_IS_FILE,outputDirectory);
            libDirectory = ".";
        }
        this.outputDirectory = outputDirectory;
    }

    public void setLibDirectory(String libDirectory) {
        if ( libDirectory.endsWith("/") ||
             libDirectory.endsWith("\\") )
        {
            libDirectory =
                libDirectory.substring(0,libDirectory.length()-1);
        }
        File outDir = new File(libDirectory);
        if( !outDir.exists() ) {
            ErrorManager.error(ErrorManager.MSG_DIR_NOT_FOUND,libDirectory);
            libDirectory = ".";
        }
        this.libDirectory = libDirectory;
    }

    public void setNfa() {
        generate_NFA_dot=true;
    }

    public void setDfa() {
        generate_DFA_dot=true;
    }

    public void setDebug() {
        debug=true;
    }

    public void setTrace() {
        trace=true;
    }

    public void setReport() {
        report=true;
    }

    public void setProfile() {
        profile=true;
    }

    public void setPrintGrammar() {
        printGrammar = true;
    }

    public void setDepend() {
        depend=true;
    }

    public void addGrammarFile(String grammarFileName)
            throws TokenStreamException, RecognitionException, IOException {
        buildDependencyGenerators.put(grammarFileName, new ExtendedBuildDependencyGenerator(this, grammarFileName));
        grammarFileNames.add(grammarFileName);
    }

    @Override
    public Grammar getRootGrammar(String grammarFileName) throws IOException {
        //StringTemplate.setLintMode(true);
        // grammars mentioned on command line are either roots or single grammars.
        // create the necessary composite in case it's got delegates; even
        // single grammar needs it to get token types.
        CompositeGrammar composite = new CompositeGrammar();
        Grammar grammar = new Grammar(this,grammarFileName,composite);
        composite.setDelegationRoot(grammar);
        FileReader fr = null;
        fr = new FileReader(getFileFor(grammarFileName));
        BufferedReader br = new BufferedReader(fr);
        grammar.parseAndBuildAST(br);
        composite.watchNFAConversion = internalOption_watchNFAConversion;
        br.close();
        fr.close();
        return grammar;
    }

    /**
     * Dependency sorting is not as simple as providing a comparison function
     * and using the Collection sort() method. This is because a standard sort
     * will infer some orders and not compare every node with every other node.
     * This kind of sort will only work with 2 members.
     *
     * We need a topological sort here because in the case you have:
     *
     *   t1walker.g -> t1.g -> t1lexer.g
     *
     * It is quite likely that the walker will sort after t1.g but that
     * t1lexer will end up being sorter after t1wlaker, which is obviously
     * incorrect.
     *
     * Rather than work it all out again from first principles, I stole this
     * topological sort code from java2s.com.
     *
     * @see <a href="http://www.java2s.com/Code/Java/Collections-Data-Structure/Topologicalsorting.htm">Topoligical sort code</a>
     *
     */
    public void sortGrammarFiles() 
            
            throws MojoExecutionException
    {

        // Create our topological sorting object
        //
        GraphTS sorted = new GraphTS();

        // Node index tracking vars
        //
        int parent;         // Vertex we are determining dependencies for
        int dependency;     // Vertex we are asking if parent is dependent on


        // The array of topologically sorted grammar files
        //
        String sortedArray[];

        // Create a vertex for each file in the list
        //
        for (Object p : grammarFileNames) {
            sorted.addVertex((String) p);
        }

        // Next we need to determine, for each file in the collection,
        // which other files in the collection, it depends on. We use
        // an extension to the ANTLR tool stuff to determine this.
        //
        parent = 0;

        for (Object p : grammarFileNames) {

            // See if this file is dependent on any others
            //
            dependency = 0;

            for (Object d : grammarFileNames) {

                // Don't check a file against itself
                //
                if  (parent != dependency) {

                    // Is the parent dependent on the tentative dependency
                    //
                    if  (dependsWorker.dependsOn((String)p, (String)d)) {

                        // Yes it does, so we need to add an edge from the dependency to
                        // the parent
                        //
                        sorted.addEdge(dependency, parent);
                    }
                }

                // Next vertex in possible dependency chain
                //
                dependency++;
            }

            // Next vertex in potential dependent files
            //
            parent++;

        }

        // We now have a graph of all vertices and edges and can
        // sort it topologically...
        //
        try {

            // Sort
            //
            sortedArray = sorted.topo();

            // Rebuild the collection from the sorted order
            //
            grammarFileNames.clear();

            for (int j = 0; j < parent; j++) {
                grammarFileNames.add(sortedArray[j]);
            }
        }
        catch (Exception e) {

            // If we got an exception, then we leave the grammar list unsorted as
            // it usually means that there is a cyclic dependency that cannot be solved
            // and the user has created something that cannot be done. Such as:
            // t1.g depends on t2.g
            // t2.g depends on t3.g
            // t3.g depends on t1.g
            //
            throw new MojoExecutionException("Your grammar files contain circular dependencies that cannot be resolved. You must solve this issue before you can generate recognizers.");
        }
    }

    class Vertex {

        public String label;

        public Vertex(String lab) {
            label = lab;
        }
    }

    public class GraphTS {

        private final int MAX_VERTS = 200;
        private Vertex vertexList[]; // list of vertices
        private int matrix[][]; // adjacency matrix
        private int numVerts; // current number of vertices
        private String sortedArray[];

        public GraphTS() {
            vertexList = new Vertex[MAX_VERTS];
            matrix = new int[MAX_VERTS][MAX_VERTS];
            numVerts = 0;
            for (int i = 0; i < MAX_VERTS; i++) {
                for (int k = 0; k < MAX_VERTS; k++) {
                    matrix[i][k] = 0;
                }
            }
            sortedArray = new String[MAX_VERTS]; // sorted vert labels
        }

        public void addVertex(String lab) {
            vertexList[numVerts++] = new Vertex(lab);
        }

        public void addEdge(int start, int end) {
            matrix[start][end] = 1;
        }

        public void displayVertex(int v) {
            System.out.print(vertexList[v].label);
        }

        public String[] topo() // toplogical sort

                throws Exception
        {
            int orig_nVerts = numVerts;

            while (numVerts > 0) // while vertices remain,
            {
                // get a vertex with no successors, or -1
                int currentVertex = noSuccessors();
                if (currentVertex == -1) // must be a cycle
                {
                    throw new Exception("The provided grammar files have cyclic dependencies and cannot be ordered to build correctly");
                }
                // insert vertex label in sorted array (start at end)
                sortedArray[numVerts - 1] = vertexList[currentVertex].label;

                deleteVertex(currentVertex); // delete vertex
            }

            return sortedArray;


        }

        public int noSuccessors() // returns vert with no successors (or -1 if no such verts)
        {
            boolean isEdge; // edge from row to column in adjMat

            for (int row = 0; row < numVerts; row++) {
                isEdge = false; // check edges
                for (int col = 0; col < numVerts; col++) {
                    if (matrix[row][col] > 0) // if edge to another,
                    {
                        isEdge = true;
                        break; // this vertex has a successor try another
                    }
                }
                if (!isEdge) // if no edges, has no successors
                {
                    return row;
                }
            }
            return -1; // no
        }

        public void deleteVertex(int delVert) {
            if (delVert != numVerts - 1) // if not last vertex, delete from vertexList
            {
                for (int j = delVert; j < numVerts - 1; j++) {
                    vertexList[j] = vertexList[j + 1];
                }

                for (int row = delVert; row < numVerts - 1; row++) {
                    moveRowUp(row, numVerts);
                }

                for (int col = delVert; col < numVerts - 1; col++) {
                    moveColLeft(col, numVerts - 1);
                }
            }
            numVerts--; // one less vertex
        }

        private void moveRowUp(int row, int length) {
            for (int col = 0; col < length; col++) {
                matrix[row][col] = matrix[row + 1][col];
            }
        }

        private void moveColLeft(int col, int length) {
            for (int row = 0; row < length; row++) {
                matrix[row][col] = matrix[row][col + 1];
            }
        }
    }

    private final DependencyComparator dependsWorker = new DependencyComparator(this);

    public class DependencyComparator {

        private ConfigurableTool tool;

        public DependencyComparator(ConfigurableTool tool) {
            this.tool = tool;
        }

        protected boolean dependsOn(String o1, String o2) {


            ExtendedBuildDependencyGenerator dep1 = tool.buildDependencyGenerators.get(o1);
            ExtendedBuildDependencyGenerator dep2 = tool.buildDependencyGenerators.get(o2);

            List<File> dependencies = dep1.getUsefulDependenciesFileList();
            if(dependencies == null) {
                return false;
            }
            List<File> generatedFiles = dep2.getGeneratedFileList();

            Boolean does = !Collections.disjoint(dependencies, generatedFiles);
            
            return does;
        }
    }

    private class ExtendedBuildDependencyGenerator extends BuildDependencyGenerator {
        private ExtendedBuildDependencyGenerator(Tool tool, String grammarFileName)
                throws IOException, TokenStreamException, RecognitionException {
            super(tool, grammarFileName);
        }

        /** Return a list of File objects that name files ANTLR will read
         *  to process T.g; for now, this can only be .tokens files and only
         *  if they use the tokenVocab option.
         */
        protected List<File> getUsefulDependenciesFileList() {
            List<File> files = new ArrayList();

            // handle token vocabulary loads
            String vocabName = (String)grammar.getOption("tokenVocab");
            if ( vocabName != null ) {
                File parentFile = new File(grammar.getFileName()).getParentFile();
                if(parentFile != null) {
                    vocabName = parentFile.getPath() + File.separator + vocabName;
                }
                File vocabFile = tool.getImportedVocabFile(vocabName);
                files.add(vocabFile);
            }
            return files;
        }
    }
}
