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

import java.nio.file.Paths;

import org.jetbrains.annotations.NotNull;

public class AnonymizerParams {
    public String documentDirectory;
    public boolean verbose;

    public AnonymizerParams(@NotNull String[] args) throws Exception {
        verbose = false;
        for (String arg : args) {
            if (arg.equals("-v")) { //$NON-NLS-1$
                if (args.length == 1) {
                    throw new Exception("Input directory was not specified!"); //$NON-NLS-1$
                }
                verbose = true;
            } else {
                documentDirectory = arg;
            }
        }

        if (args.length > 2 || args.length == 0) {
            throw new Exception("Wrong number of parameters."); //$NON-NLS-1$
        }

        if (documentDirectory == null) {
            throw new Exception("Input directory was not specified!"); //$NON-NLS-1$
        }
        if (documentDirectory.endsWith("\\") || documentDirectory.endsWith("/")) { //$NON-NLS-1$//$NON-NLS-2$
            documentDirectory = documentDirectory.substring(0, documentDirectory.length() - 1);
        }
        documentDirectory = Paths.get(documentDirectory).toAbsolutePath().toString();
    }

}