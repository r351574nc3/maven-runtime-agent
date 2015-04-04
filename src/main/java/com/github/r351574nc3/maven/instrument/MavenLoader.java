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


import org.apache.maven.project.DefaultProjectBuilder;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;


/**
 * @author Leo Przybylski
 */
public class MavenLoader implements Runnable {

    protected String m2Home;
    protected MavenDependencyHandler dependencyHandler;
    protected Object mavenProject;
    protected Class mavenProjectClass;
    

    public MavenLoader() {
    }

    public MavenLoader(final String m2Home) {
        setMavenHome(m2Home);
        try {
            loadMaven();
            
            final ProjectBuilder projectBuilder = new DefaultProjectBuilder();

            for (final URL pomUrl : lookupPomFiles()) {
                info("URL: %s", pomUrl); 
                setMavenProject(projectBuilder.build(new File(pomUrl.getFile()), new DefaultProjectBuildingRequest()).getProject());
                    // mavenProjectClass.getMethod("setPomFile", new Class[] { File.class }).invoke(mavenProject, new File(pomUrl.getFile()));
            } 
        }
        catch (MalformedURLException mue) {
            mue.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        setDependencyHandler(new MavenDependencyHandler(mavenProject));
    }

    public void setMavenHome(final String m2Home) {
        this.m2Home = m2Home;
    }

    public String getMavenHome() {
        return this.m2Home;
    }

    public void setMavenProject(final Object mavenProject) {
        this.mavenProject = mavenProject;
    }

    public Object getMavenProject() {
        return this.mavenProject;
    }

    public void setMavenProjectClass(final Class mavenProjectClass) {
        this.mavenProjectClass = mavenProjectClass;
    }

    public Class getMavenProjectClass() {
        return this.mavenProjectClass;
    }

    public void setDependencyHandler(final MavenDependencyHandler dependencyHandler) {
        this.dependencyHandler = dependencyHandler;
    }

    public MavenDependencyHandler getDependencyHandler() {
        return this.dependencyHandler;
    }

    protected Class loadMaven() throws ClassNotFoundException {
        final File m2Home = new File(getMavenHome() + "/lib");

        final ClassLoader parentLoader = loadDependencies();

        if (m2Home.exists() && m2Home.isDirectory()) {
            try {
                final URL[] mavenJars = stream(m2Home.listFiles()).map(f -> toURL(f)).toArray(URL[]::new);
                final ClassLoader mavenClassLoader = new URLClassLoader(mavenJars, parentLoader);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    
    protected URL[] lookupPomFiles() throws IOException {
        final Enumeration<URL> urls = AgentMain.class.getClassLoader().getResources("pom.xml");
        return enumAsStream(urls).toArray(URL[]::new);
    }

    protected static <T> Stream<T> enumAsStream(final Enumeration<T> e) {
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(
                new Iterator<T>() {
                    public T next() {
                        return e.nextElement();
                    }
                    public boolean hasNext() {
                        return e.hasMoreElements();
                    }
                },
                Spliterator.ORDERED), false);
    }

    protected static URL toURL(final File file) {
        try {
            return file.toURI().toURL();
        }
        catch (MalformedURLException mue) {
            mue.printStackTrace();
        }
        return null;
    }

    protected ClassLoader loadDependencies() {
        final List<URL> urls = new ArrayList<URL>() {{
                try {
                    final File jar = new File(String.format("%s/%s/%s", System.getProperty("user.home"), ".m2/repository", "org/codehaus/plexus/plexus-classworlds/2.2.2/plexus-classworlds-2.2.2.jar"));
                    add(jar.toURI().toURL());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }};
        
        try {
            return new URLClassLoader(urls.stream().toArray(URL[]::new), AgentMain.class.getClassLoader());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void run() {
        // getDependencyHandler().handle();
    }
}
