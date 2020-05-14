package com.evernightfireworks.mcci.services;

import ca.weblite.webview.WebView;
import com.evernightfireworks.mcci.services.core.WebViewServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class WebViewService {
    private static final Logger logger = LogManager.getFormatterLogger(WebViewService.class.getName());
    private static final int PORT = 12000;

    public static void registerClient() {
        new Thread(()->{
            try {
                WebViewServer server = new WebViewServer(PORT);
                logger.info("webview service started");
                Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            } catch (IOException e) {
                logger.error("start web server error: " + e.getClass().getName() + ", " + e.getLocalizedMessage());
            }
        }).start();
    }

    public static String getResourceURI(String path) {
        return ("http://127.0.0.1:" + PORT + (path.startsWith("/")?"":"/")+path);
    }

    public static WebView viewResource(String uri, String title, boolean resize, int width, int height) {
        String url =  getResourceURI(uri);
        return view(url, title, resize, width, height);
    }

    public static WebView viewResource(String uri, String title, boolean resize) {
        String url =  getResourceURI(uri);
        return view(url, title, resize);
    }

    public static WebView view(String url, String title, boolean resize, int width, int height) {
        WebView webview = new WebView();
        webview.url(url);
        webview.title(title);
        webview.resizable(resize);
        webview.size(width, height);
        webview.show();
        return webview;
    }

    public static WebView view(String url, String title, boolean resize) {
        WebView webview = new WebView();
        webview.url(url);
        webview.title(title);
        webview.resizable(resize);
        webview.show();
        return webview;
    }
}
