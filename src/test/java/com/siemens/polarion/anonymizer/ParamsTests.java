/*
 * Copyright 2016 Polarion AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siemens.polarion.anonymizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

public class ParamsTests {
    // @NotNull
    private static String testDirectory;

    @BeforeClass
    public static void setProperties() {
        if (System.getProperty("os.name").startsWith("Windows")) { //$NON-NLS-1$//$NON-NLS-2$
            testDirectory = "C:\\test"; //$NON-NLS-1$
        } else {
            testDirectory = "/data/test"; //$NON-NLS-1$
        }
    }

    @Test
    public void params1Test() throws Exception {
        String[] args = { testDirectory, "-v" }; //$NON-NLS-1$
        AnonymizerParams params = new AnonymizerParams(args);
        assertEquals(testDirectory, params.documentDirectory);
        assertTrue(params.verbose);
    }

    @Test
    public void params2Test() throws Exception {
        String[] args = { "-v", testDirectory }; //$NON-NLS-1$
        AnonymizerParams params = new AnonymizerParams(args);
        assertEquals(testDirectory, params.documentDirectory);
        assertTrue(params.verbose);
    }

    @Test
    public void params3Test() throws Exception {
        String[] args = { testDirectory };
        AnonymizerParams params = new AnonymizerParams(args);
        assertEquals(testDirectory, params.documentDirectory);
        assertFalse(params.verbose);
    }

    @Test
    public void nonAbsoluteDirectoryTest() throws Exception {
        String[] args = { "test", "-v" }; //$NON-NLS-1$ //$NON-NLS-2$
        AnonymizerParams params = new AnonymizerParams(args);
        String docDir = params.documentDirectory;
        assertEquals("test", docDir.substring(docDir.length() - 4, docDir.length())); //$NON-NLS-1$
        assertTrue(params.verbose);
    }

}
