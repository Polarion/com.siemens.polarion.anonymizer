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

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

public class TextRandomizer {
    private static final Random rnd;
    private static final char[] letterChars = new char[52];
    private static final char[] numberChars = new char[10];
    @NotNull
    final private static Pattern patternForAltAttribute = Pattern.compile("(alt=\")([^\"]*)(\")"); //$NON-NLS-1$
    @NotNull
    final private static Pattern patternForAttachmentFilesPrefix = Pattern.compile("(attachment\\d+-)(.*)"); //$NON-NLS-1$

    static {
        rnd = new Random();
        for (char i = 'A'; i <= 'Z'; i++) {
            letterChars[i - 65] = i;
        }
        for (char i = 'a'; i <= 'z'; i++) {
            letterChars[i - 97 + 26] = i;
        }
        for (char i = '0'; i <= '9'; i++) {
            numberChars[i - 48] = i;
        }
    }

    protected @NotNull String plainTextRandomize(@NotNull String input) {
        StringBuilder output = new StringBuilder();
        input = deaccentString(input);
        for (int i = 0; i < input.length(); i++) {
            output.append(replaceChar(input.charAt(i), true));
        }
        return output.toString();
    }

    protected @NotNull String richTextRandomize(@NotNull String input) {
        boolean replace = true;
        boolean replaceNumbers = true;
        boolean htmlEntityFound = false;
        StringBuilder output = new StringBuilder();
        input = deaccentString(input);
        for (int i = 0; i < input.length(); i++) {
            //replace chars between > and <
            if (input.charAt(i) == '>') { //>
                replace = true;
            }
            if (input.charAt(i) == '<') { //<
                replace = false;
            }
            //do not replace chars between & and ;
            if (input.charAt(i) == '&') { //&
                replace = false;
                htmlEntityFound = true;
            }
            if ((input.charAt(i) == ';') && htmlEntityFound) { //;
                replace = true;
                htmlEntityFound = false;
            }
            //do not replace chars between &# and ;
            if ((input.charAt(i) == '&') && (input.charAt(i + 1) == '#')) { //&#
                replaceNumbers = false;
            }
            if (input.charAt(i) == ';') { //;
                replaceNumbers = true;
            }
            if (replace) {
                output.append(replaceChar(input.charAt(i), replaceNumbers));
            } else {
                output.append(input.charAt(i));
            }
        }
        return randomizeAltAttribute(output);
    }

    private @NotNull String randomizeAltAttribute(@NotNull StringBuilder input) {
        Matcher m = patternForAltAttribute.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String attributeValue = m.group(2);
            attributeValue = plainTextRandomize(attributeValue);
            m.appendReplacement(sb, "$1" + Matcher.quoteReplacement(attributeValue) + "$3"); //$NON-NLS-1$//$NON-NLS-2$
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private char replaceChar(char character, boolean replaceNumbers) {
        if (containsChar(letterChars, character)) {
            int randomNumber = rnd.nextInt(letterChars.length);
            return letterChars[randomNumber];
        } else if (replaceNumbers && containsChar(numberChars, character)) {
            int randomNumber = rnd.nextInt(numberChars.length);
            return numberChars[randomNumber];
        } else if (Character.isLetter(character)) {
            return 'W';
        } else {
            return character;
        }
    }

    private @NotNull String deaccentString(@NotNull String data) {
        data = Normalizer.normalize(data, Form.NFD);
        data = data.replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); //$NON-NLS-1$ //$NON-NLS-2$
        return data;
    }

    protected @NotNull String fileNameRandomize(@NotNull String input) {
        Matcher m = patternForAttachmentFilesPrefix.matcher(input);
        if (m.matches()) {
            return (m.group(1) + randomizeFileNameWithExtension(m.group(2)));
        } else {
            return randomizeFileNameWithExtension(input);
        }
    }

    protected @NotNull String randomizeFileNameWithExtension(@NotNull String input) {
        String output;
        int i = input.lastIndexOf("."); //$NON-NLS-1$
        if (i == -1) {
            output = plainTextRandomize(input);
        } else {
            output = plainTextRandomize(input.substring(0, i)) + input.substring(i);
        }
        return output;
    }

    protected boolean containsChar(char[] charArray, char testChar) {
        for (char element : charArray) {
            if (element == testChar) {
                return true;
            }
        }
        return false;
    }

}
