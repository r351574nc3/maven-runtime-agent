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

import static com.github.r351574nc3.java.logging.FormattedLogger.*;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

/**
 * Main agent class. The agent creates a background thread that scans the classpath for changes to jar files
 *
 * @author Leo Przybylski
 */
public class AgentMain {

    public static final String MAVEN_HOME_KEY = "M2_HOME";

    public AgentMain() {
    }

    protected static String getMavenHome() {
        return System.getenv(MAVEN_HOME_KEY);
    }

    
    /**
     * @param agentArgs
     * @param inst
     */
    public static void premain(final String agentArgs, final Instrumentation inst) {
        if (AgentMain.getMavenHome() == null) {
            throw new MavenNotFoundError();
        }

        info("M2_HOME: %s", AgentMain.getMavenHome());
        inst.addTransformer(new DefaultClassFileTransformer());

        
        new Thread(new MavenLoader(AgentMain.getMavenHome())).start();
    }

    /**
     * @param agentArgs
     * @param inst
     */
    public static void agentmain(final String agentArgs, final Instrumentation inst) {
        unregisterAllTransformers();
        premain(agentArgs, inst);
    }

    public static void unregisterAllTransformers() {
    }
}
