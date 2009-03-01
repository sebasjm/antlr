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

import org.antlr.tool.ErrorManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SourceMapping;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Generate source code from ANTLRv3 grammar specifications.
 *
 * @author <a href="mailto:dave@badgers-in-foil.co.uk">David Holroyd</a>
 * @version $Id $
 */
public abstract class AbstractAntlr3PluginMojo extends AbstractMojo
{
    /**
     * A set of patterns matching files from the sourceDirectory that
     * should be processed as grammars.
     *
     * @parameter
     */
    protected Set includes = new HashSet();

    /**
     * A set of exclude patterns.
     *
     * @parameter
     */
    protected Set excludes = new HashSet();

    /**
     * Enables ANTLR-specific network debugging. Requires a tool able to
     * talk this protocol e.g. ANTLRWorks.
     *
     * @parameter default-value="false"
     */
    protected boolean debug;

    /**
     * Generate a parser that logs rule entry/exit messages.
     *
     * @parameter default-value="false"
     */
    protected boolean trace;

    /**
     * Generate a parser that computes profiling information.
     *
     * @parameter default-value="false"
     */
    protected boolean profile;

    /**
     * Print out a report about the grammars processed
     * @parameter default-value="false"
     */
    protected boolean report;

    /**
     * Generate an NFA for each rule.
     * @parameter default-value="false"
     */
    protected boolean nfa;

    /**
     * Generate a DFA for each decision point.
     * @parameter default-value="false"
     */
    protected boolean dfa;

    /**
     * Print out the grammar without actions.
     * @parameter default-value="false"
     */
    protected boolean printGrammar;

    /**
     * Specify the output style for messages.
     * @parameter
     */
    protected String messageFormat;
    
    /**
     * @parameter
     */
    protected File antlrOutput;

    /**
     * The number of milliseconds ANTLR will wait for analysis of each
     * alternative in the grammar to complete before giving up.
     *
     * @parameter default-value="1000"
     */
    private int conversionTimeout;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    abstract File getSourceDirectory();
    abstract File getOutputDirectory();
    abstract File getLibDirectory();
    abstract void addSourceRoot( File outputDir );

    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException
    {

        ConfigurableTool tool = new ConfigurableTool();

        // setup error handling?
        ErrorManager.setErrorListener(new LoggingErrorListener(getLog()));

        try
        {
            configure(tool);
            if(ErrorManager.getNumErrors() > 0) {
              throw new MojoExecutionException("Unable to configure antlr properly.");
            }
            tool.process();
            if(ErrorManager.getNumErrors() > 0) {
              throw new MojoExecutionException("Antlr grammar processing failed with errors.");
            }
        }
        catch ( MojoExecutionException e)
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Antlr grammar processing failed.", e );
        }

        if ( project != null )
        {
            addSourceRoot( this.getOutputDirectory() );
        }
    }

    private void configure(ConfigurableTool tool)
            throws TokenStreamException, RecognitionException, IOException, InclusionScanException, MojoExecutionException {

        if(messageFormat != null) {
            ErrorManager.setFormat(messageFormat);
        }

        tool.setOutputDirectory(this.getOutputDirectory().getPath());

        tool.setBaseDirectory(this.getSourceDirectory().getPath());

        File libFile = getLibDirectory();
        if ( libFile != null )
        {
            if(!libFile.exists()) {
                libFile.mkdirs();
            }
            tool.setLibDirectory(libFile.getPath());
        }

        if(debug) {
            tool.setDebug();
        }

        if(trace) {
            tool.setTrace();
        }

        if(profile) {
            tool.setProfile();
        }

        if(report) {
            tool.setReport();
        }

        if(nfa) {
            tool.setNfa();
        }

        if(dfa) {
            tool.setDfa();
        }

        if(printGrammar) {
            tool.setPrintGrammar();
        }

        // If default of 0, then this will turn off timeouts. Better this
        // than ignoring a value of 0.
        //
        org.antlr.analysis.DFA.MAX_TIME_PER_DFA_CREATION = conversionTimeout;

        // find and add grammar files

        SourceMapping mapping = new SuffixMapping( "g", Collections.EMPTY_SET );
        Set includes = getIncludesPatterns();

        SourceInclusionScanner scan = new SimpleSourceInclusionScanner( includes, excludes );
        scan.addSourceMapping( mapping );
        Set<File> grammarFiles = scan.getIncludedSources( this.getSourceDirectory(), null );
        if ( grammarFiles.isEmpty() )
        {
            getLog().info( "No grammars to process" );
        }
        else
        {
            String sourceDirectory = this.getSourceDirectory().getPath();

            String grammarRelPath = null;
            for(File grammarFile: grammarFiles) {

                grammarRelPath = findRelativeSource(sourceDirectory, grammarFile.getPath());
                tool.addGrammarFile(grammarRelPath);
            }
            tool.sortGrammarFiles();
        }

    }

    public Set getIncludesPatterns()
    {
        if ( includes == null || includes.isEmpty() )
        {
            return Collections.singleton( "**/*.g" );
        }
        return includes;
    }

    private String findRelativeSource( String sourceDirectory, String grammarFileName )
    {
        if ( grammarFileName.startsWith( sourceDirectory ) )
        {
            grammarFileName = grammarFileName.substring(sourceDirectory.length() + 1);
        }
        return grammarFileName;
    }

}
