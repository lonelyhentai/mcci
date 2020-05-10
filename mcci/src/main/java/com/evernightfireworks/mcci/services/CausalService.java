package com.evernightfireworks.mcci.services;

import ca.weblite.webview.WebView;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import joptsimple.internal.Strings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.stream.Collectors;

public class CausalService {
    private static final Logger logger = LogManager.getFormatterLogger(CausalService.class.getName());
    private static final int port = 12001;

    public static enum CausalPageType {
        GRAPH,
        DATA,
        ANALYSIS
    }

    private static String PYTHON_ENV_PREFIX =
            "./resourcepacks/libmcci" + System.getProperty("path.separator")
                    + "./resourcepacks/libmcci/Library/mingw-w64/bin" + System.getProperty("path.separator")
                    + "./resourcepacks/libmcci/Library/usr/bin" + System.getProperty("path.separator")
                    + "./resourcepacks/libmcci/Library/bin" + System.getProperty("path.separator")
                    + "./resourcepacks/libmcci/Scripts" + System.getProperty("path.separator")
                    + "./resourcepacks/libmcci/bin" + System.getProperty("path.separator");
    private static String PATH = PYTHON_ENV_PREFIX + System.getenv("Path");
    private static String LD_LIBRARY_PATH = PYTHON_ENV_PREFIX + System.getenv("LD_LIBRARY_PATH");
    private static String DYLD_LIBRARY_PATH = PYTHON_ENV_PREFIX + System.getenv("DYLD_LIBRARY_PATH");
    private static String PYTHONHOME = "resourcepacks/libmcci";

    public static void registerPythonLibrary() {
        try {
            setEnv("Path", PATH);
            setEnv("LD_LIBRARY_PATH", LD_LIBRARY_PATH);
            setEnv("DYLD_LIBRARY_PATH", DYLD_LIBRARY_PATH);
            setEnv("PYTHONHOME", PYTHONHOME);
        } catch (Exception e) {
            logger.error("failed to set env: ", e);
        }
    }

    public static void setPythonEnvironment(ProcessBuilder pb) {
        Map<String, String> env = pb.environment();
        env.put("Path", PATH);
        env.put("LD_LIBRARY_PATH", LD_LIBRARY_PATH);
        env.put("DYLD_LIBRARY_PATH", DYLD_LIBRARY_PATH);
        env.put("PYTHONHOME", PYTHONHOME);
    }

    public static void initJupyterServer() {
        try {
            ResourceSystemManager.ensureDir(ResourceSystemManager.getRuntimeResourceAbsPath("mcci"));
            ProcessBuilder pb = new ProcessBuilder()
                    .command("resourcepacks/libmcci/Scripts/jupyter",
                            "lab", "--port=" + port, "--no-browser",
                            "--NotebookApp.token=''",
                            "--notebook-dir='resourcepacks/mcci'",
                            "--ExecutePreprocessor.kernel_name='mcci'")
                    .inheritIO();
            setPythonEnvironment(pb);
            Process process = pb.start();
            Thread closeChildThread = new Thread(() -> {
                Deque<ProcessHandle> deque = new ArrayDeque<>();
                deque.addLast(process.toHandle());
                while (!deque.isEmpty()) {
                    ProcessHandle p = deque.pollFirst();
                    if (p.children().count() == 0) {
                        p.destroyForcibly();
                    } else {
                        p.children().forEach(deque::addLast);
                        deque.addLast(p);
                    }
                }
            });
            Runtime.getRuntime().addShutdownHook(closeChildThread);
        } catch (IOException e) {
            logger.error("failed to init causality engine", e);
        }
    }

    public static String getJupyterURL() {
        return getJupyterURL("/");
    }

    public static String getJupyterURL(String path) {
        try {
            return new URL("http", "127.0.0.1", port, path).toString();
        } catch (MalformedURLException e) {
            logger.error("unreachable code", e);
            return Strings.EMPTY;
        }
    }

    public static String getJupyterURL(String sessionName, String fileName) {
        return getJupyterURL("/edit/" + sessionName + "/" + fileName);
    }

    private static Path getSessionDir(String sessionName) {
        return ResourceSystemManager.getRuntimeResourceAbsPath("mcci/" + sessionName);
    }

    public static void deleteSession(String sessionName) throws IOException {
        Path absDir = getSessionDir(sessionName);
        if (!Files.exists(absDir)) {
            throw new FileNotFoundException(absDir.toString());
        } else if (!Files.isDirectory(absDir)) {
            throw new NotDirectoryException(absDir.toString());
        } else {
            FileUtils.deleteDirectory(new File(absDir.toString()));
        }
    }

    public static void createSession(String sessionName) throws IOException {
        Path absDir = getSessionDir(sessionName);
        if (Files.exists(absDir)) {
            throw new FileAlreadyExistsException(absDir.toString());
        }
        Files.createDirectory(absDir);
        Files.createFile(Path.of(absDir.toString(), "graph.dot"));
        Files.createFile(Path.of(absDir.toString(), "data.csv"));
        Files.createFile(Path.of(absDir.toString(), "analysis.ipynb"));
    }

    public static void changeSession(String sessionName) throws FileNotFoundException, NotDirectoryException {
        Path absDir = getSessionDir(sessionName);
        if (!Files.exists(absDir)) {
            throw new FileNotFoundException();
        } else if (!Files.isDirectory(absDir)) {
            throw new NotDirectoryException(absDir.toString());
        }
    }

    public static void viewPage(String sessionName, CausalPageType kind) throws IOException {
        String fileName;
        if (kind == CausalPageType.GRAPH) {
            fileName = "graph.dot";
        } else if (kind == CausalPageType.ANALYSIS) {
            fileName = "analysis.ipynb";
        } else {
            fileName = "data.csv";
        }
        Path absDir = getSessionDir(sessionName);
        if (!Files.exists(absDir) || !Files.isDirectory(absDir)) {
            throw new IOException("session '" + sessionName + "' state error");
        }
        String url = getJupyterURL(sessionName, fileName);
        new Thread(() -> {
            WebView webview = new WebView();
            webview.url(url);
            webview.size(1280, 720);
            webview.title(String.format("editing '%s' of session '%s'", fileName, sessionName));
            webview.resizable(true);
            webview.show();
        }).start();
    }

    public static void viewRoot() {
        String url = getJupyterURL();
        if (url.isEmpty()) {
            return;
        }
        new Thread(() -> {
            WebView webview = new WebView();
            webview.url(url);
            webview.size(1280, 720);
            webview.title("Causal Analysis");
            webview.resizable(true);
            webview.show();
        }).start();
    }

    public static void appendRecord(String sessionName, String record) throws IOException {
        Path path = ResourceSystemManager.getRuntimeResourceAbsPath("mcci/" + sessionName + "/data.csv");
        Files.write(path, (record + "\n").getBytes(), StandardOpenOption.APPEND);
    }

    public static Pair<String,String> getRecordHeader(String sessionName) throws IOException {
        try (InputStream is = ResourceSystemManager.getRuntimeResourceAsStream("mcci/" + sessionName + "/graph.dot")) {
            MutableGraph g = new Parser().read(is);
            String main = g.nodes().stream().filter(n -> {
                Object o = n.attrs().get("label");
                if(o==null){
                    return true;
                }
                return !"Unobserved Confounders".equals(o.toString());
            }).map(n -> n.name().toString()).collect(Collectors.joining(","));
            String unobservedConfounders = g.nodes().stream().filter(n -> {
                Object o = n.attrs().get("label");
                if(o==null){
                    return false;
                }
                return "Unobserved Confounders".equals(o.toString());
            }).map(n -> n.name().toString()).collect(Collectors.joining(","));
            return Pair.of(main, unobservedConfounders);
        }
    }

    private static void setEnv(String key, String value) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.put(key, value);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.put(key, value);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for (Class cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.put(key, value);
                }
            }
        }
    }
}
