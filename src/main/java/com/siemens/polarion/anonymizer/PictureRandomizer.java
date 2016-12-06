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

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class PictureRandomizer {
    private static final Random rnd = new Random();
    private static final int PIXEL_BLOCK_SIZE = 15;
    @NotNull
    private static final Logger log = LogManager.getLogger(PictureRandomizer.class);

    protected static void randomizePictureContent(boolean verbose, @NotNull File file) throws IOException {
        String suffix = FileUtils.getExtension(file.toPath());
        if (verbose) {
            log.info("PICTURE: " + file.getAbsolutePath()); //$NON-NLS-1$
        }
        BufferedImage image = null;
        image = ImageIO.read(new BufferedInputStream(new FileInputStream(file)));
        int width = image.getWidth();
        int height = image.getHeight();
        if (!suffix.equals("bmp")) { //$NON-NLS-1$
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }
        image = fillImageContent(image);
        ImageIO.write(image, suffix, file);
    }

    protected static @NotNull BufferedImage fillImageContent(@NotNull BufferedImage image) {
        for (int x = 0; x < image.getWidth(); x += PIXEL_BLOCK_SIZE) {
            for (int y = 0; y < image.getHeight(); y += PIXEL_BLOCK_SIZE) {
                int r = rnd.nextInt(256);
                int g = rnd.nextInt(256);
                int b = rnd.nextInt(256);
                int p;
                if (image.getType() == BufferedImage.TYPE_INT_ARGB) {
                    int a = 255;
                    p = (a << 24) | (r << 16) | (g << 8) | b;
                } else {
                    p = (r << 16) | (g << 8) | b;
                }
                for (int i = 0; i < PIXEL_BLOCK_SIZE && x + i < image.getWidth(); i++) {
                    for (int j = 0; j < PIXEL_BLOCK_SIZE && y + j < image.getHeight(); j++) {
                        image.setRGB(x + i, y + j, p);
                    }
                }
            }
        }
        return image;
    }
}
