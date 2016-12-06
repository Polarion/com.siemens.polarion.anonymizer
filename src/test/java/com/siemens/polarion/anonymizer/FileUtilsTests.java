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

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;

import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FileUtilsTests {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testGetExtension1() {
        Path file = Paths.get("test.JPG"); //$NON-NLS-1$
        String str = FileUtils.getExtension(file);
        assertEquals("jpg", str); //$NON-NLS-1$
    }

    @Test
    public void testGetExtension2() {
        Path file = Paths.get("C:/test"); //$NON-NLS-1$
        String str = FileUtils.getExtension(file);
        assertEquals("", str); //$NON-NLS-1$
    }

    @Test
    public void testFilesEquality() throws Exception {
        URL url = this.getClass().getResource("/comment.xml"); //$NON-NLS-1$
        File testFile = new File(url.getFile());
        final File tempFile = tempFolder.newFile("tempFile.txt"); //$NON-NLS-1$
        FileUtils.createFileFromDoc(tempFile, FileUtils.createDocFromFile(testFile));
        String origFileContent = new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.US_ASCII);
        String newFileContent = new String(Files.readAllBytes(tempFile.toPath()), StandardCharsets.US_ASCII);
        origFileContent = origFileContent.replace("\n", "").replace("\r", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        newFileContent = newFileContent.replace("\n", "").replace("\r", ""); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        assertEquals(origFileContent, newFileContent);
    }

    @Test
    public void fileNameDecoderTest() throws Exception {
        String input = "%2B%20"; //$NON-NLS-1$
        String output = FileUtils.decodeFileName(input);
        assertEquals("+ ", output); //$NON-NLS-1$
    }

    @Test
    public void randomizeFileContentTest() throws Exception {
        Random rnd = new Random();
        URL url = this.getClass().getResource("/file.a"); //$NON-NLS-1$
        File testFile = new File(url.getFile());
        byte[] file1sha1 = getSHA1(testFile);
        FileUtils.randomizeFileContent(false, rnd, testFile);
        byte[] file2sha1 = getSHA1(testFile);
        assertFalse(Arrays.equals(file1sha1, file2sha1));
    }

    private byte[] getSHA1(@NotNull File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA1"); //$NON-NLS-1$
        FileInputStream fis = new FileInputStream(file);
        byte[] dataBytes = new byte[1024];
        int nread = 0;

        while ((nread = fis.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, nread);
        }
        fis.close();
        byte[] mdbytes = md.digest();
        return mdbytes;
    }

}