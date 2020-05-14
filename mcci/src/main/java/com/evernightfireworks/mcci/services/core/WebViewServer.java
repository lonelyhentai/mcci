package com.evernightfireworks.mcci.services.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;


import com.evernightfireworks.mcci.services.ResourceSystemManager;
import fi.iki.elonen.NanoHTTPD;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebViewServer extends NanoHTTPD {
    private final Logger logger = LogManager.getFormatterLogger(WebViewServer.class.getName());
    private final Map<String, String> mimes = mimeTypes();

    public WebViewServer(int port) throws IOException {
        super(port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
    }

    @Override
    public Response serve(IHTTPSession session) {
        logger.info(String.format("'%s', %s",session.getUri(), session.getMethod().toString().toUpperCase()));
        if(session.getMethod()==Method.GET) {
            Path path = ResourceSystemManager.getRuntimeResourceAbsPath(session.getUri());
            if(Files.exists(path)&&!Files.isDirectory(path)&&Files.isReadable(path)) {
                try {
                    InputStream s = ResourceSystemManager.getRuntimeResourceAsStream(session.getUri());
                    String ext = FilenameUtils.getExtension(path.toString());
                    String mime = mimes.getOrDefault(ext, "text/plain");
                    logger.info(String.format("'%s' found, mime '%s', length %d",session.getUri(), mime, s.available()));
                    return newFixedLengthResponse(
                            Response.Status.OK,
                            mime,
                            s, s.available());
                } catch (IOException e) {
                    logger.info(String.format("'%s' file not found", session.getUri()));
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, String.format("%s: %s", e.getClass().getName(), e.getLocalizedMessage()));
                }
            } else {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, Response.Status.NOT_FOUND.getDescription());
            }
        } else {
            logger.info(String.format("method %s, not allowed", session.getMethod().toString()));
            return newFixedLengthResponse(
                    Response.Status.METHOD_NOT_ALLOWED,
                    MIME_PLAINTEXT,
                    Response.Status.METHOD_NOT_ALLOWED.getDescription()
            );
        }
    }
}
