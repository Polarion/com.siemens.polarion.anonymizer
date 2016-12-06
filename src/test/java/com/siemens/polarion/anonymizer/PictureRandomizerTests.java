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

import java.awt.image.BufferedImage;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class PictureRandomizerTests {

    @Test
    public void fillImageTestDimensions() {
        BufferedImage image1 = new BufferedImage(10, 20, BufferedImage.TYPE_INT_RGB);
        BufferedImage image2 = PictureRandomizer.fillImageContent(image1);

        assertEquals(image1.getHeight(), image2.getHeight());
        assertEquals(image1.getWidth(), image2.getWidth());
    }

    @Test
    public void fillImageTestContent() {
        BufferedImage image1 = new BufferedImage(10, 20, BufferedImage.TYPE_INT_RGB);
        BufferedImage image2 = new BufferedImage(10, 20, BufferedImage.TYPE_INT_RGB);
        image1 = PictureRandomizer.fillImageContent(image1);
        image2 = PictureRandomizer.fillImageContent(image2);
        assertFalse(bufferedImagesEqual(image1, image2));
    }

    boolean bufferedImagesEqual(@NotNull BufferedImage img1, @NotNull BufferedImage img2) {
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            return false;
        }
        for (int x = 0; x < img1.getWidth(); x++) {
            for (int y = 0; y < img1.getHeight(); y++) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }
}
