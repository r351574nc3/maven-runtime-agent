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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import java.net.URL;
import java.net.URLClassLoader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Leo Przybylski
 */
public class AgentTest {
    protected static final String TEST_REDEFINE_BEFORE_BYTES = "TestRedefine1.bytes";
    protected static final String TEST_REDEFINE_AFTER_BYTES  = "TestRedefine2.bytes";

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testMavenLoad() {
    }

    protected byte[] getClassBytes(final URL url) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in =  null;
        byte[] retval = null;
        
        try {
            in = url.openStream();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            while (in.available() > 0) {
                out.write(in.read());
            }
            retval = out.toByteArray();
        }
        catch (Exception e) {
        }
        finally {
            try {
                in.close();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            
            try {
                out.close();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return retval;
    }

    @Test
    public void testDefineClass() {
        final URL urlBefore = getClass().getClassLoader().getResource(TEST_REDEFINE_BEFORE_BYTES);
        final URL urlAfter  = getClass().getClassLoader().getResource(TEST_REDEFINE_AFTER_BYTES);

        final byte[] beforeBytes = getClassBytes(urlBefore);
        final byte[] afterBytes  = getClassBytes(urlAfter);

        assertNotEquals(null, beforeBytes);
        assertNotEquals(null, afterBytes);
        /*
        final ClassLoader beforeClassLoader = new URLClassLoader(new URL[] { urlBefore }, getClass().getClassLoader());
        final ClassLoader afterClassLoader  = new URLClassLoader(new URL[] { urlAfter }, getClass().getClassLoader());
        */
        
        final ClassLoader beforeClassLoader = new ClassLoader(getClass().getClassLoader()) {
                public Class findClass(String name) {
                    return defineClass(name, beforeBytes, 0, beforeBytes.length);
                }
            };
        final ClassLoader afterClassLoader = new ClassLoader(getClass().getClassLoader()) {
                public Class findClass(String name) {
                    return defineClass(name, afterBytes, 0, afterBytes.length);
                }
            };



        try {
            beforeClassLoader.loadClass("TestRedefine1");
            final Class beforeClass = beforeClassLoader.loadClass("TestRedefine1");
            final String result = (String) beforeClass.getMethod("getString").invoke(null);
            assertEquals(result, "Test");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            afterClassLoader.loadClass("TestRedefine1");
            final Class afterClass = afterClassLoader.loadClass("TestRedefine1");            
            final String result = (String) afterClass.getMethod("getString").invoke(null);
            assertEquals(result, "Redefined");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
