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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@SuppressWarnings("nls")
public class Anonymizer {
    @NotNull
    private List<File> attachmentList = new ArrayList<File>();
    private TextRandomizer textRandomizer;
    private String documentDirectory;
    private boolean verbose;
    @NotNull
    private static Logger log = LogManager.getLogger(Anonymizer.class);
    @NotNull
    final public static String anonymizedSuffix = "_anonymized";
    @NotNull
    final private static Pattern patternForFindingAttachments = Pattern.compile("(src=\")(attachment:|workitemimg:(\\d+-))([^\"]*)(\")");
    private static Map<String, String> attachmentRandomizationMap = new HashMap<String, String>();

    protected Anonymizer(String documentDirectory, boolean verbose) {
        textRandomizer = new TextRandomizer();
        if (documentDirectory != null) {
            this.documentDirectory = documentDirectory;
        } else {
            throw new IllegalArgumentException("Directory with data was not defined.");
        }
        this.verbose = verbose;

    }

    private void run() throws IOException, ParserConfigurationException, SAXException, Exception {
        log.info("Polarion data anonymizer"); //$NON-NLS-1$
        if (!(new File(documentDirectory).exists())) {
            throw new IOException("Directory with Polarion data does not exists!");
        }

        String anonymizedDirectory = documentDirectory + anonymizedSuffix;
        if (new File(anonymizedDirectory).exists()) {
            throw new IOException("Directory \"" + anonymizedDirectory + "\" where anonymized data will be placed already exists, remove it please.");
        } else {
            if (!new File(anonymizedDirectory).mkdirs()) {
                throw new IOException("Directory \"" + anonymizedDirectory + "\" where anonymized data will be placed cannot be created.");
            }
        }

        log.info("Input directory: " + documentDirectory); //$NON-NLS-1$

        attachmentList.addAll(FileUtils.findNonXMLFiles(documentDirectory + "/attachments"));
        attachmentList.addAll(FileUtils.findNonXMLFiles(documentDirectory + "/workitems"));

        log.info("Attachment count: " + attachmentList.size()); //$NON-NLS-1$

        //randomization of XML files
        ArrayList<File> xmlList = FileUtils.findXMLFiles(documentDirectory);
        log.info("XML files count: " + xmlList.size()); //$NON-NLS-1$
        log.info("Working..."); //$NON-NLS-1$

        for (File xmlFile : xmlList) {
            if (verbose) {
                log.info("FILE: " + xmlFile.getAbsolutePath()); //$NON-NLS-1$
            }
            anonymizeAndMoveXML(xmlFile, documentDirectory);
        }

        //randomization of names of other attachments and their moving to new directory
        for (File attachmentFile : attachmentList) {
            FileUtils.renameAndCopyFile(attachmentFile.getParent(),
                    attachmentFile.getName(),
                    attachmentFile.getParent().substring(0, documentDirectory.length()) + anonymizedSuffix + "/" + attachmentFile.getParent().substring(documentDirectory.length() + 1), //$NON-NLS-1$
                    textRandomizer.fileNameRandomize(attachmentFile.getName()));
        }

        //randomization of attachments files
        FileUtils.randomizeFilesContent(anonymizedDirectory, verbose);

        log.info("--------------------------------"); //$NON-NLS-1$
        log.info("Anonymization of Polarion data finished."); //$NON-NLS-1$
        log.info("Output Directory: " + anonymizedDirectory); //$NON-NLS-1$

    }

    private void anonymizeAndMoveXML(@NotNull File inputFile, @NotNull String documentDirectory) throws Exception {
        File outputFile = new File(documentDirectory + anonymizedSuffix + "/" + inputFile.getParent().substring(documentDirectory.length()) + "/" + inputFile.getName()); //$NON-NLS-1$ //$NON-NLS-2$
        Document doc = FileUtils.createDocFromFile(inputFile);
        NodeList nList = doc.getElementsByTagName("field"); //$NON-NLS-1$
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (verbose) {
                log.info("Current Element: " + nNode.getNodeName()); //$NON-NLS-1$
            }
            Element eElement = (Element) nNode;
            if (verbose) {
                log.info("Current Element ID: " + eElement.getAttribute("id")); //$NON-NLS-1$ //$NON-NLS-2$
            }
            String textType = eElement.getAttribute("text-type"); //$NON-NLS-1$
            if (verbose) {
                log.info("Text type: " + textType); //$NON-NLS-1$
            }
            //randomization of "author", "title", "assignee" fields
            randomizeAuthorTitleAssigneeFields(eElement);
            //randomization of text in fields with text-type="text/html"
            randomizeHtmlFields(eElement);
            //randomization of other text-type fields
            randomizeOtherTextFields(eElement);
            //randomization of fields with type="text", "string", "integer", "float" or type="currency"
            randomizeTextStringIntegerFloatCurrencyFields(eElement);
            //randomization of fields with type="enum:@..."
            randomizeEnumFields(eElement);
        }
        //randomization of attachments in XMLs
        randomizeXmlAttachment(inputFile, documentDirectory, doc);
        //randomization of "text/html" item fields
        randomizeTextHtmlItems(doc);

        FileUtils.createFileFromDoc(outputFile, doc);
    }

    private void randomizeTextHtmlItems(@NotNull Document doc) {
        NodeList nList;
        nList = doc.getElementsByTagName("item"); //$NON-NLS-1$
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            Element eElement = (Element) nNode;
            if ("text/html".equals(eElement.getAttribute("text-type"))) {
                randomizeRichTextElement(eElement);
            }
        }
    }

    private void randomizeRichTextElement(@NotNull Element eElement) {
        randomizeTextElement(eElement, value -> textRandomizer.richTextRandomize(value));
    }

    private void randomizePlainTextElement(@NotNull Element eElement) {
        randomizeTextElement(eElement, value -> textRandomizer.plainTextRandomize(value));
    }

    private void randomizeTextElement(@NotNull Element eElement, @NotNull Function<String, String> textRandomization) {
        if (eElement.hasChildNodes()) {
            NodeList items = eElement.getElementsByTagName("item");
            if (items.getLength() > 0) {
                for (int i = 0; i < items.getLength(); i++) {
                    Node item = items.item(i);
                    String elementValue = item.getTextContent();
                    item.setTextContent(textRandomization.apply(elementValue));
                }
            } else {
                Node item = eElement.getChildNodes().item(0);
                String elementValue = item.getNodeValue();
                item.setNodeValue(textRandomization.apply(elementValue));
            }
        }
    }

    void randomizeXmlAttachment(@NotNull File inputFile, @NotNull String documentDirectory, @NotNull Document doc) throws Exception {
        NodeList nList;
        nList = doc.getElementsByTagName("field"); //$NON-NLS-1$
        randomizeNodesWithAttachment(inputFile, documentDirectory, nList);
        nList = doc.getElementsByTagName("item"); //$NON-NLS-1$
        randomizeNodesWithAttachment(inputFile, documentDirectory, nList);
    }

    private void randomizeNodesWithAttachment(File inputFile, String documentDirectory, NodeList nList) throws UnsupportedEncodingException, IOException {
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            Element eElement = (Element) nNode;
            if ("text/html".equals(eElement.getAttribute("text-type")) && (eElement.hasChildNodes())) {
                Node firstChildOfElement = eElement.getFirstChild();
                String field = firstChildOfElement.getNodeValue();
                Matcher m = patternForFindingAttachments.matcher(field);
                StringBuffer sb = new StringBuffer();
                while (m.find()) {
                    String attachmentName = m.group(4);
                    String decodedAttachmentName = FileUtils.decodeFileName(attachmentName);
                    String replaceWith;
                    if (attachmentRandomizationMap.containsKey(decodedAttachmentName)) {
                        replaceWith = attachmentRandomizationMap.get(decodedAttachmentName);
                    } else {
                        replaceWith = textRandomizer.fileNameRandomize(decodedAttachmentName);
                        attachmentRandomizationMap.put(decodedAttachmentName, replaceWith);
                    }
                    m.appendReplacement(sb, "$1$2" + Matcher.quoteReplacement(replaceWith) + "$5");
                    if ("attachment:".equals(m.group(2))) {
                        FileUtils.renameAndCopyAttachmentFile(documentDirectory, decodedAttachmentName, replaceWith);
                        attachmentList.remove(new File(documentDirectory + "/attachments/" + decodedAttachmentName)); //$NON-NLS-1$
                    } else {
                        String fileLocation = inputFile.getAbsolutePath().substring(documentDirectory.length(), inputFile.getAbsolutePath().length() - inputFile.getName().length() - 1);
                        String attachmentSequenceNumber = m.group(3);
                        FileUtils.renameAndCopyFile(documentDirectory + fileLocation, "attachment" + attachmentSequenceNumber + decodedAttachmentName, documentDirectory + anonymizedSuffix + fileLocation,
                                "attachment" + attachmentSequenceNumber + replaceWith);
                        attachmentList.remove(new File(documentDirectory + fileLocation + "/attachment" + attachmentSequenceNumber + decodedAttachmentName));
                    }
                }
                m.appendTail(sb);
                field = sb.toString();
                firstChildOfElement.setNodeValue(field);
            }
        }
    }

    void randomizeEnumFields(@NotNull Element eElement) {
        if (eElement.hasAttribute("type")) { //$NON-NLS-1$
            CharSequence cs1 = "enum:@"; //$NON-NLS-1$
            if (eElement.getAttribute("type").contains(cs1)) { //$NON-NLS-1$
                randomizePlainTextElement(eElement);
            }
        }
    }

    void randomizeTextStringIntegerFloatCurrencyFields(@NotNull Element eElement) {
        if (eElement.hasAttribute("type")) { //$NON-NLS-1$
            String type = eElement.getAttribute("type");
            if (type.equals("text") || //$NON-NLS-1$
                    type.equals("string") || //$NON-NLS-1$
                    type.equals("integer") || //$NON-NLS-1$
                    type.equals("float") || //$NON-NLS-1$
                    type.equals("currency")) //$NON-NLS-1$
            {
                if (verbose) {
                    log.info("Other text field found"); //$NON-NLS-1$
                }
                randomizePlainTextElement(eElement);
            }
        }
    }

    void randomizeOtherTextFields(@NotNull Element eElement) {
        if (eElement.hasAttribute("text-type")) { //$NON-NLS-1$
            if (!eElement.getAttribute("text-type").equals("") && !eElement.getAttribute("text-type").equals("text/html")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                if (verbose) {
                    log.info("Other text field found"); //$NON-NLS-1$
                }
                randomizePlainTextElement(eElement);
            }
        }
    }

    void randomizeHtmlFields(@NotNull Element eElement) {
        if (eElement.hasAttribute("text-type")) { //$NON-NLS-1$
            if (eElement.getAttribute("text-type").equals("text/html")) { //$NON-NLS-1$ //$NON-NLS-2$
                if (verbose) {
                    log.info("Rich text field found"); //$NON-NLS-1$
                }
                randomizeRichTextElement(eElement);
            }
        }
    }

    void randomizeAuthorTitleAssigneeFields(@NotNull Element eElement) {
        if (eElement.hasAttribute("id")) { //$NON-NLS-1$
            if (eElement.getAttribute("id").equals("author") //$NON-NLS-1$ //$NON-NLS-2$
                    || eElement.getAttribute("id").equals("title") //$NON-NLS-1$//$NON-NLS-2$
                    || eElement.getAttribute("id").equals("assignee")) { //$NON-NLS-1$ //$NON-NLS-2$
                randomizePlainTextElement(eElement);
            }
        }
    }

    public static void main(@NotNull String[] args) {
        boolean verbose = false;
        try {
            AnonymizerParams params = new AnonymizerParams(args);
            verbose = params.verbose;
            new Anonymizer(params.documentDirectory, verbose).run();
        } catch (Exception e) {
            log.error(e.getMessage(), verbose ? e : null);
            System.exit(1);
        }
    }

}
