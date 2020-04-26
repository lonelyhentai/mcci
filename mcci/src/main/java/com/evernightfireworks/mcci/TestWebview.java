package com.evernightfireworks.mcci;

import ca.weblite.webview.WebViewCLIClient;
import ca.weblite.webview.WebViewClient;

public class TestWebview {
    public static void main(String []args) {
         WebViewClient webview = (WebViewCLIClient) new WebViewCLIClient.Builder()
                            .url("https://baidu.com")
                            .title("Crafting policy")
                            .size(1600, 900)
                            .build();
         webview.ready();
    }
}
