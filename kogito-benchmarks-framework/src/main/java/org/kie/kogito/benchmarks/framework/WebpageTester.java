/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.benchmarks.framework;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebpageTester {
    private static final Logger logger = LoggerFactory.getLogger(WebpageTester.class);

    /**
     * Patiently try to wait for a web page and examine it
     *
     * @param url address
     * @param timeoutS in seconds
     * @param stringToLookFor string must be present on the page
     */
    public static long testWeb(String url, long timeoutS, String stringToLookFor, boolean measureTime) throws InterruptedException, IOException {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("url must not be empty");
        }
        if (timeoutS < 0) {
            throw new IllegalArgumentException("timeoutS must be positive");
        }
        if (StringUtils.isBlank(stringToLookFor)) {
            throw new IllegalArgumentException("stringToLookFor must contain a non-empty string");
        }
        String webPage = "";

        long now = System.currentTimeMillis();
        final long startTime = now;
        boolean found = false;
        long foundTimestamp = -1L;
        while (now - startTime < 1000 * timeoutS) {
            URLConnection c = new URL(url).openConnection();
            c.setRequestProperty("Accept", "*/*");
            c.setConnectTimeout(500);
            try (Scanner scanner = new Scanner(c.getInputStream(), StandardCharsets.UTF_8.toString())) {
                scanner.useDelimiter("\\A");
                webPage = scanner.hasNext() ? scanner.next() : "";
            } catch (Exception e) {
                logger.debug("Waiting `" + stringToLookFor + "' to appear on " + url);
            }
            if (webPage.contains(stringToLookFor)) {
                found = true;
                if (measureTime) {
                    foundTimestamp = System.currentTimeMillis();
                }
                break;
            }
            if (!measureTime) {
                Thread.sleep(500);
            } else {
                Thread.sleep(0, 100000);
            }
            now = System.currentTimeMillis();
        }

        String failureMessage = "Timeout " + timeoutS + "s was reached. " +
                (StringUtils.isNotBlank(webPage) ? webPage + " must contain string: " : "Empty webpage does not contain string: ") +
                "`" + stringToLookFor + "'";
        if (!found) {
            logger.info(failureMessage);
        }
        assertTrue(found, failureMessage);
        return foundTimestamp - startTime;
    }
}
