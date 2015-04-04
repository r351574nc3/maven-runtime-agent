/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Leo Przybylski
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.r351574nc3.maven.instrument;

import java.io.File;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.codehaus.plexus.classworlds.ClassWorld;

/**
 *
 */
public class AgentRequest {
    protected String[] args;
    protected ClassWorld classWorld;
    protected String workingDirectory;
    protected File multiModuleProjectDirectory;
    protected boolean debug;
    protected boolean quiet;
    protected boolean showErrors = true;
    protected Properties userProperties = new Properties();
    protected Properties systemProperties = new Properties();
    protected MavenExecutionRequest request;

    AgentRequest(String[] args, ClassWorld classWorld) {
        this.args = args;
        this.classWorld = classWorld;
        this.request = new DefaultMavenExecutionRequest();
    }

    public String[] getArgs() {
        return args;
    }

    public CommandLine getCommandLine() {
        return commandLine;
    }

    public ClassWorld getClassWorld() {
        return classWorld;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public File getMultiModuleProjectDirectory() {
        return multiModuleProjectDirectory;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public boolean isShowErrors() {
        return showErrors;
    }

    public Properties getUserProperties() {
        return userProperties;
    }

    public Properties getSystemProperties() {
        return systemProperties;
    }

    public MavenExecutionRequest getRequest() {
        return request;
    }

    public void setUserProperties( Properties properties )  {
        this.userProperties.putAll( properties );      
    }
}