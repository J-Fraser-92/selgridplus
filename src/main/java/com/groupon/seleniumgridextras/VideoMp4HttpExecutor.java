/**
 * Copyright (c) 2013, Groupon, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of GROUPON nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Created with IntelliJ IDEA. User: Dima Kovalenko (@dimacus) && Darko Marinov
 * Date: 5/10/13 Time: 4:06 PM
 */
package com.groupon.seleniumgridextras;

import com.groupon.seleniumgridextras.config.RuntimeConfig;
import com.groupon.seleniumgridextras.utilities.Utils;
import com.groupon.seleniumgridextras.utilities.threads.video.VideoRecordingThreadPool;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.io.File;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.List;

class VideoMp4HttpExecutor implements HttpHandler {

    private static Logger logger = Logger.getLogger(VideoMp4HttpExecutor.class);

    public void handle(HttpExchange t) throws IOException {
        Map params = (Map) t.getAttribute("parameters");

        String filename = params.get("filename").toString();

        File images_dir = RuntimeConfig.getConfig().getVideoRecording().getPrivateDir();

        File file = new File(images_dir, filename);
        
        logger.info(file.getAbsolutePath());

        Headers h = t.getResponseHeaders();

        h.add("Content-Type", "video/mp4");
        try {
            h.add("Hash-Checksum", Utils.sha1(file));
        } catch (NoSuchAlgorithmException ex) {
            logger.error(ex.getMessage());
        }

        boolean isFileReady = !VideoRecordingThreadPool.getRecordingFile().equals(filename);
        
        if (isFileReady) {
            h.add("File-Ready", "true");
        } else {
            h.add("File-Ready", "false");
        }

        t.sendResponseHeaders(200, file.length());

        OutputStream os = t.getResponseBody();

        Files.copy(file.toPath(), os);

        os.close();
    }

}
