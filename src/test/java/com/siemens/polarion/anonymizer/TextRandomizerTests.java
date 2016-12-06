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

import org.junit.Test;

public class TextRandomizerTests {

    @Test
    public void testPlainTextRandomize() {
        TextRandomizer textRandomizer = new TextRandomizer();
        String str1 = "test"; //$NON-NLS-1$
        String str2 = textRandomizer.plainTextRandomize(str1);
        assertFalse(str1.equals(str2));
    }

    @Test
    public void testFileNameRandomize() {
        TextRandomizer textRandomizer = new TextRandomizer();
        String str1 = ".jpg"; //$NON-NLS-1$
        String str2 = textRandomizer.fileNameRandomize(str1);
        assertEquals(str1, str2);
    }

    @Test
    public void testFileNameRandomize2() {
        TextRandomizer textRandomizer = new TextRandomizer();
        String str1 = "test"; //$NON-NLS-1$
        String str2 = textRandomizer.fileNameRandomize(str1);
        assertFalse(str1.equals(str2));
    }

    @Test
    public void testFileNameRandomize3() {
        TextRandomizer textRandomizer = new TextRandomizer();
        String str1 = "attachment1-README"; //$NON-NLS-1$
        String str2 = textRandomizer.fileNameRandomize(str1);
        assertFalse(str2.contains("README")); //$NON-NLS-1$
    }

    @Test
    public void testFileNameRandomize4() {
        TextRandomizer textRandomizer = new TextRandomizer();
        String str1 = "attachment12-README.QQ.txt"; //$NON-NLS-1$
        String str2 = textRandomizer.fileNameRandomize(str1);
        assertFalse(str2.contains("README.QQ")); //$NON-NLS-1$
    }

    @Test
    public void testRichTextRandomizer() {
        TextRandomizer textRandomizer = new TextRandomizer();
        String str1 = "<![CDATA[Lorem ipsum dolor <span id=\"polarion_wiki macro name=workitem;params=Lorem ipsum dolor/AAA-111|display=long\"></span> ......<br/> <img src=\"workitemimg:1-image.png\" style=\"width: 401px;height: 556px;\"/>]]>"; //$NON-NLS-1$
        String str2 = textRandomizer.richTextRandomize(str1);
        assertEquals(str1, str2);
    }

    @Test
    public void testRichTextRandomizer2() {
        TextRandomizer textRandomizer = new TextRandomizer();
        String str1 = ",./;'-<abc"; //$NON-NLS-1$
        String str2 = textRandomizer.richTextRandomize(str1);
        assertEquals(str1, str2);
    }

    @Test
    public void testRichTextRandomizer3() {
        TextRandomizer textRandomizer = new TextRandomizer();
        String str1 = ">abc<"; //$NON-NLS-1$
        String str2 = textRandomizer.richTextRandomize(str1);
        assertFalse(str1.equals(str2));
    }

    @Test
    public void testContainsChar() {
        TextRandomizer textRandomizer = new TextRandomizer();
        char[] charArray = { 'a', 'b', 'c' };
        char testChar = 'a';
        assertTrue(textRandomizer.containsChar(charArray, testChar));
    }

    @Test
    public void testAltAttributeInRichText() {
        TextRandomizer textRandomizer = new TextRandomizer();
        String str1 = "<![CDATA[Lorem ipsum dolor <span  <img src=\"workitemimg:1-image.png\" alt=\"CUSTOMER_CONTENT\" alt=\"workitemimg:2-1812b-CUSTOMER_CONTENT.png\" />]]>"; //$NON-NLS-1$
        String str2 = textRandomizer.richTextRandomize(str1);
        assertFalse(str2.contains("CUSTOMER_CONTENT")); //$NON-NLS-1$
    }

}
