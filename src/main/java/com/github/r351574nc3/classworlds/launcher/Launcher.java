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
package com.github.r351574nc3.classworlds.launcher;

import static com.github.r351574nc3.java.logging.FormattedLogger.*;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

import org.codehaus.plexus.classworlds.launcher.Configurator;
import org.codehaus.plexus.classworlds.launcher.ConfigurationException;

/**
 *
 * @author Leo Przybylski
 */
public class Launcher extends org.codehaus.plexus.classworlds.launcher.Launcher {
    public static final String MAVEN_HOME_KEY = "M2_HOME";
    
    protected static final String CLASSWORLDS_CONF = "classworlds.conf";  
    protected static final String UBERJAR_CONF_DIR = "WORLDS-INF/conf/";  
    protected ClassLoader systemClassLoader;  
    protected String mainClassName;  
    protected String mainRealmName;  
    protected ClassWorld world;
    protected Instrumentation inst;

    public Launcher() {
        this.systemClassLoader = Thread.currentThread().getContextClassLoader();
    }

    public void setSystemClassLoader(ClassLoader loader) {
        this.systemClassLoader = loader;
    }
    
    public ClassLoader getSystemClassLoader() {
        return this.systemClassLoader;
    }
    
    public void setAppMain(final String mainClassName,
                           final String mainRealmName) {
        this.mainClassName = mainClassName;  
        this.mainRealmName = mainRealmName;
    }
    
    public String getMainRealmName() {
        return this.mainRealmName;
    }
    
    public String getMainClassName() {
        return this.mainClassName;
    }
    
    public void setWorld(ClassWorld world) {
        this.world = world;
    }
    
    public ClassWorld getWorld() {
        return this.world;
    }
    
    /**
     * Configure from a file.
     *
     * @param is The config input stream.
     * @throws IOException             If an error occurs reading the config file.
     * @throws MalformedURLException   If the config file contains invalid URLs.
     * @throws ConfigurationException  If the config file is corrupt.
     * @throws org.codehaus.plexus.classworlds.realm.DuplicateRealmException If the config file defines two realms
     *                                 with the same id.
     * @throws org.codehaus.plexus.classworlds.realm.NoSuchRealmException    If the config file defines a main entry
     *                                 point in a non-existent realm.
     */
    public void configure( InputStream is )
        throws IOException, ConfigurationException, DuplicateRealmException, NoSuchRealmException {
        final Configurator configurator = new Configurator( this );
        
        configurator.configure( is );
    }
    
    protected static String getMavenHome() {
        return System.getenv(MAVEN_HOME_KEY);
    }

    /**
     * @param agentArgs
     * @param inst
     */
    public static void premain(final String agentArgs, final Instrumentation inst) {
        if (getMavenHome() == null) {
            // throw new MavenNotFoundError();
        }
        
        info("M2_HOME: %s", getMavenHome());
        System.setProperty("maven.home", getMavenHome());
        
        // inst.addTransformer(new DefaultClassFileTransformer());
        
        InputStream is = null;
        final String classworldsConf = System.getProperty(CLASSWORLDS_CONF);
        final Launcher launcher = new Launcher();
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
        launcher.setSystemClassLoader(cl);

        try {
            if (classworldsConf != null) {
                is = new FileInputStream(classworldsConf);
            }
            else {
                if ("true".equals( System.getProperty("classworlds.bootstrapped"))) {
                    is = cl.getResourceAsStream(UBERJAR_CONF_DIR + CLASSWORLDS_CONF);
                }
                else {
                    is = cl.getResourceAsStream(CLASSWORLDS_CONF);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
            
        
        if (is == null) {
            throw new RuntimeException( "classworlds configuration not specified nor found in the classpath" );
        }

        try {
            launcher.configure(is);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        info("Main Realm: %s", launcher.getMainRealmName());
        info("Main Class: %s", launcher.getMainClassName());
        
        try {
            launcher.launch(agentArgs, inst);
        }
        catch (InvocationTargetException e) {
            try {
                ClassRealm realm = launcher.getWorld().getRealm( launcher.getMainRealmName() );
                
                URL[] constituents = realm.getURLs();
                
                System.out.println( "---------------------------------------------------" );
                
                for (int i = 0; i < constituents.length; i++) {
                    System.out.println( "constituent[" + i + "]: " + constituents[i] );
                }
                
                System.out.println( "---------------------------------------------------" );
                
                // Decode ITE (if we can)
                Throwable t = e.getTargetException();
                
                if (t instanceof Exception) {
                    throw (Exception) t;
                }
                
                if ( t instanceof Error ) {
                    throw (Error) t;
                }
            }
            catch (Exception ee) {
                ee.printStackTrace();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        // new Thread(new MavenLoader(AgentMain.getMavenHome())).start();
    }

    public void launch(final String args, final Instrumentation inst) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchRealmException{
        final Method mainMethod = getMainMethod();
        
        final ClassLoader cl = getMainRealm();
        Thread.currentThread().setContextClassLoader(cl);

        final Object ret = mainMethod.invoke(getMainClass(), new Object[] {args, inst, getWorld()});
    }

    protected Method getMainMethod() throws ClassNotFoundException, NoSuchMethodException, NoSuchRealmException {
        final Class cworlds = getMainRealm().loadClass(ClassWorld.class.getName());
        final Method mainMethod = getMainClass().getMethod("premain", new Class[] { String.class, Instrumentation.class, cworlds });
        
        int modifiers = mainMethod.getModifiers();
        if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
            if (mainMethod.getReturnType() == Integer.TYPE || mainMethod.getReturnType() == Void.TYPE) {
                return mainMethod;
            }
        }
        throw new NoSuchMethodException("public static void main(String[] args, ClassWorld world)");
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
