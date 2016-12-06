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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class XmlTests {

    private static DocumentBuilder dBuilder;
    private Element element;
    private static Anonymizer anonymizer;

    @BeforeClass
    public static void initializeRandomization() throws Exception {
        anonymizer = new Anonymizer("C", false); //$NON-NLS-1$
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dBuilder = dbFactory.newDocumentBuilder();
    }

    @Test
    public void randomizeAuthorFieldTest() throws Exception {
        String xml = "<module-comment><field id=\"author\">AUTHOR</field></module-comment>"; //$NON-NLS-1$
        Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
        Node node = doc.getElementsByTagName("field").item(0); //$NON-NLS-1$
        element = (Element) node;
        anonymizer.randomizeAuthorTitleAssigneeFields(element);

        assertFalse("AUTHOR".equals(element.getChildNodes().item(0).getNodeValue())); //$NON-NLS-1$
    }

    @Test
    public void randomizeTitleFieldTest() throws Exception {
        String xml = "<module-comment><field id=\"title\">TITLE</field></module-comment>"; //$NON-NLS-1$
        Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
        Node node = doc.getElementsByTagName("field").item(0); //$NON-NLS-1$
        element = (Element) node;
        anonymizer.randomizeAuthorTitleAssigneeFields(element);

        assertFalse("TITLE".equals(element.getChildNodes().item(0).getNodeValue())); //$NON-NLS-1$
    }

    @Test
    public void randomizeAssigneeFieldTest() throws Exception {
        String xml = "<module-comment><field id=\"assignee\">ASSIGNEE</field></module-comment>"; //$NON-NLS-1$
        Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
        Node node = doc.getElementsByTagName("field").item(0); //$NON-NLS-1$
        element = (Element) node;
        anonymizer.randomizeAuthorTitleAssigneeFields(element);

        assertFalse("ASSIGNEE".equals(element.getChildNodes().item(0).getNodeValue())); //$NON-NLS-1$
    }

    @Test
    public void randomizeHtmlFieldTest() throws Exception {
        String xml = "<module><field id=\"homePageContent\" text-type=\"text/html\">&lt;h1 id=\"polarion_wiki macro name=module-workitem;params=id=2_AN-7846\"&gt;&lt;/h1&gt;&lt;p id=\"polarion_template_0\"&gt;TEST&lt;/p&gt;</field>  </module>"; //$NON-NLS-1$
        Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
        Node node = doc.getElementsByTagName("field").item(0); //$NON-NLS-1$
        element = (Element) node;
        anonymizer.randomizeHtmlFields(element);
        assertFalse((element.getChildNodes().item(0).getNodeValue()).contains("TEST")); //$NON-NLS-1$
    }

    @Test
    public void randomizeOtherTextFieldTest() throws Exception {
        String xml = "<module><field id=\"homePageContent\" text-type=\"qqqqq\">TestField</field></module>"; //$NON-NLS-1$
        Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
        Node node = doc.getElementsByTagName("field").item(0); //$NON-NLS-1$
        element = (Element) node;
        anonymizer.randomizeOtherTextFields(element);
        assertFalse("TestField".equals(element.getChildNodes().item(0).getNodeValue())); //$NON-NLS-1$
    }

    @Test
    public void randomizeTextFieldTest() throws Exception {
        String xml = "<module><field type=\"text\">TestField</field></module>"; //$NON-NLS-1$
        Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
        Node node = doc.getElementsByTagName("field").item(0); //$NON-NLS-1$
        element = (Element) node;
        anonymizer.randomizeTextStringIntegerFloatCurrencyFields(element);
        assertFalse("TestField".equals(element.getChildNodes().item(0).getNodeValue())); //$NON-NLS-1$
    }

    @Test
    public void randomizeStringFieldTest() throws Exception {
        String xml = "<module><field type=\"string\">TestField</field></module>"; //$NON-NLS-1$
        Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
        Node node = doc.getElementsByTagName("field").item(0); //$NON-NLS-1$
        element = (Element) node;
        anonymizer.randomizeTextStringIntegerFloatCurrencyFields(element);
        assertFalse("TestField".equals(element.getChildNodes().item(0).getNodeValue())); //$NON-NLS-1$
    }

    @Test
    public void randomizeIntegerFieldTest() throws Exception {
        String xml = "<module><field type=\"integer\">TestField</field></module>"; //$NON-NLS-1$
        Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
        Node node = doc.getElementsByTagName("field").item(0); //$NON-NLS-1$
        element = (Element) node;
        anonymizer.randomizeTextStringIntegerFloatCurrencyFields(element);
        assertFalse("TestField".equals(element.getChildNodes().item(0).getNodeValue())); //$NON-NLS-1$
    }

    @Test
    public void randomizeCurrencyFieldTest() throws Exception {
        String xml = "<module><field type=\"currency\">TestField</field></module>"; //$NON-NLS-1$
        Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
        Node node = doc.getElementsByTagName("field").item(0); //$NON-NLS-1$
        element = (Element) node;
        anonymizer.randomizeTextStringIntegerFloatCurrencyFields(element);
        assertFalse("TestField".equals(element.getChildNodes().item(0).getNodeValue())); //$NON-NLS-1$
    }

    @Test
    public void randomizeEnumFieldTest() throws Exception {
        String xml = "<module><field id=\"owner\" type=\"enum:@user\">TestField</field></module>"; //$NON-NLS-1$
        Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
        Node node = doc.getElementsByTagName("field").item(0); //$NON-NLS-1$
        element = (Element) node;
        anonymizer.randomizeEnumFields(element);
        assertFalse("TestField".equals(element.getChildNodes().item(0).getNodeValue())); //$NON-NLS-1$
    }

    @Test
    public void randomizeMultiFieldWithPlainText() throws Exception {
        String xml = "<module>" //$NON-NLS-1$
                + "<field id=\"assignee\">" //$NON-NLS-1$
                + "<list>" //$NON-NLS-1$
                + "<item>ASSIGNEE1</item>" //$NON-NLS-1$
                + "<item>ASSIGNEE2</item>" //$NON-NLS-1$
                + "<item>ASSIGNEE3</item>" //$NON-NLS-1$
                + "</list>" //$NON-NLS-1$
                + "</field>" //$NON-NLS-1$
                + "</module>"; //$NON-NLS-1$
        Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
        Node node = doc.getElementsByTagName("field").item(0); //$NON-NLS-1$
        element = (Element) node;
        anonymizer.randomizeAuthorTitleAssigneeFields(element);
        assertFalse(element.getTextContent().contains("ASSIGNEE")); //$NON-NLS-1$
    }

    @Test
    public void randomizeMultiFieldWithRichText() throws Exception {
        String xml = "<module>" //$NON-NLS-1$
                + "<field text-type=\"text/html\">" //$NON-NLS-1$
                + "<list>" //$NON-NLS-1$
                + "<item>&lt;h1 id=\"polarion_wiki macro name=module-workitem;params=id=2_AN-7846\"&gt;&lt;/h1&gt;&lt;p id=\"polarion_template_0\"&gt;TEST1&lt;/p&gt;</item>" //$NON-NLS-1$
                + "<item>&lt;h1 id=\"polarion_wiki macro name=module-workitem;params=id=2_AN-7846\"&gt;&lt;/h1&gt;&lt;p id=\"polarion_template_0\"&gt;TEST2&lt;/p&gt;</item>" //$NON-NLS-1$
                + "</list>" //$NON-NLS-1$
                + "</field>" //$NON-NLS-1$
                + "</module>"; //$NON-NLS-1$
        Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
        Node node = doc.getElementsByTagName("field").item(0); //$NON-NLS-1$
        element = (Element) node;
        anonymizer.randomizeHtmlFields(element);
        assertFalse(element.getTextContent().contains("TEST")); //$NON-NLS-1$
    }

    @Test
    public void randomizeAttachmentName() throws Exception {
        URL url = this.getClass().getResource("/attachment.xml"); //$NON-NLS-1$
        File testFile = new File(url.getFile());
        Document doc = dBuilder.parse(testFile);
        Node node = doc.getElementsByTagName("field").item(0); //$NON-NLS-1$
        element = (Element) node;
        String documentDirectory = testFile.getAbsolutePath().substring(0, testFile.getAbsolutePath().length() - testFile.getName().length() - 1);
        anonymizer.randomizeXmlAttachment(testFile, documentDirectory, doc);
        assertFalse(element.getChildNodes().item(0).getNodeValue().contains("TEST")); //$NON-NLS-1$
        assertTrue(element.getChildNodes().item(0).getNodeValue().contains("workitemimg:1")); //$NON-NLS-1$
    }

}
