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

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class FileUtils {
    @NotNull
    private static final Logger log = LogManager.getLogger(FileUtils.class);

    protected static @NotNull Document createDocFromFile(@NotNull File inputFile) throws ParserConfigurationException, SAXException, IOException {
        Document doc = null;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();
        return doc;
    }

    protected static void createFileFromDoc(@NotNull File outputFile, @NotNull Document doc) throws IOException, TransformerException {
        tryToCreateDirForFile(outputFile);
        if (!outputFile.createNewFile()) {
            log.warn("File \"" + outputFile + "\" already exists."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); //$NON-NLS-1$
        BufferedWriter bwr = Files.newBufferedWriter(outputFile.toPath(), Charset.forName("UTF-8")); //$NON-NLS-1$
        bwr.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"); //$NON-NLS-1$
        transformer.transform(new DOMSource(doc), new StreamResult(bwr));
    }

    protected static void renameAndCopyAttachmentFile(@NotNull String directory, @NotNull String originalName, @NotNull String newName) throws IOException {
        File originalFile = new File(directory + "/attachments/" + originalName); //$NON-NLS-1$
        File newFile = new File(directory + Anonymizer.anonymizedSuffix + "/attachments/" + newName); //$NON-NLS-1$
        tryToCreateDirForFile(newFile);
        Files.copy(originalFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    protected static void renameAndCopyFile(@NotNull String originalDirectory, @NotNull String originalName, @NotNull String newDirectory, @NotNull String newName) throws IOException {
        File originalFile = new File(originalDirectory + "/" + originalName); //$NON-NLS-1$
        File newFile = new File(newDirectory + "/" + newName); //$NON-NLS-1$
        tryToCreateDirForFile(newFile);
        if (originalFile.exists()) {
            try {
                Files.copy(originalFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new IOException("Failed to copy file \"" + originalFile + "\" to \"" + newFile + "\"", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        } else {
            log.warn("File \"" + originalFile + "\" was not found!"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private static void tryToCreateDirForFile(File inputFile) throws IOException {
        File parentFile = inputFile.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new IOException("Directory \"" + parentFile + "\" cannot be created."); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    protected static @NotNull ArrayList<File> findXMLFiles(@NotNull String path) throws IOException {
        final ArrayList<File> xmlList = new ArrayList<File>();
        Path root = Paths.get(path);
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (isXML(file)) {
                    xmlList.add(file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return xmlList;
    }

    protected static @NotNull ArrayList<File> findNonXMLFiles(@NotNull String path) throws IOException {
        final ArrayList<File> nonXMLFiles = new ArrayList<File>();
        Path root = Paths.get(path);
        if (Files.exists(root)) {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!isXML(file)) {
                        nonXMLFiles.add(file.toFile());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        return nonXMLFiles;
    }

    private static boolean isXML(@NotNull Path file) {
        String extension = getExtension(file);
        if (extension.equals("xml")) { //$NON-NLS-1$
            return true;
        }
        return false;
    }

    protected static void randomizeFilesContent(@NotNull String directory, final boolean verbose) throws IOException {
        final Random rnd = new Random();
        Path root = Paths.get(directory);
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String extension = getExtension(file);
                if (extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png") || extension.equals("bmp") || extension.equals("gif")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                    PictureRandomizer.randomizePictureContent(verbose, file.toFile());
                } else {
                    if (!extension.equals("xml")) { //$NON-NLS-1$
                        randomizeFileContent(verbose, rnd, file.toFile());
                    }
                }

                return FileVisitResult.CONTINUE;
            }
        });
    }

    static void randomizeFileContent(boolean verbose, @NotNull Random rnd, @NotNull File file) throws IOException {
        if (verbose) {
            log.info("OTHER FILE: " + file.getAbsolutePath()); //$NON-NLS-1$
        }
        long length = file.length();
        try (BufferedOutputStream buffOutStream = new BufferedOutputStream(new FileOutputStream(file))) {
            for (int i = 0; i < length; i++) {
                buffOutStream.write(rnd.nextInt());
            }
        }
    }

    protected static @NotNull String getExtension(@NotNull Path file) {
        String extension = ""; //$NON-NLS-1$
        String fileName = file.toString();
        int i = fileName.lastIndexOf("."); //$NON-NLS-1$
        if (i > -1) {
            extension = fileName.substring(i + 1).toLowerCase(Locale.ENGLISH);
        } else {
            extension = ""; //$NON-NLS-1$
        }
        return extension;
    }

    static @NotNull String decodeFileName(@NotNull String fileName) throws UnsupportedEncodingException {
        return URLDecoder.decode(fileName, "UTF-8"); //$NON-NLS-1$
    }

}
