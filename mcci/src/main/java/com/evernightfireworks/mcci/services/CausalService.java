package com.evernightfireworks.mcci.services;

import com.google.common.collect.Lists;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import guru.nidi.graphviz.parse.ParserException;
import joptsimple.internal.Strings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CausalService {
    private static final Logger logger = LogManager.getFormatterLogger(CausalService.class.getName());
    private static final int port = 12001;

    public static enum CausalPageType {
        DOT,
        DATA,
        ANALYSIS,
        IMAGE,
    }

    private static final String PYTHON_ENV_PREFIX =
            Stream.of(
                    "libmcci",
                    "libmcci/Library/mingw-w64/bin",
                    "libmcci/Library/usr/bin",
                    "libmcci/Library/bin",
                    "libmcci/Scripts",
                    "libmcci/bin")
                    .map(p -> ResourceSystemManager.getRuntimeResourceAbsPath(p).toString())
                    .collect(Collectors.joining(System.getProperty("path.separator")));
    private static final String PYTHONHOME = ResourceSystemManager.getRuntimeResourceAbsPath("libmcci").toString();
    private static final String DOT_NAME = "graph.dot";
    private static final String DATA_NAME = "data.csv";
    private static final String SCRIPT_NAME = "analysis.ipynb";
    private static final String IMAGE_NAME = "graph.png";
    private static final String DOT_TEMPLATE = "digraph %s {\n}";
    private static final String SCRIPT_TEMPLATE = "{\n" +
            " \"cells\": [\n" +
            "  {\n" +
            "   \"cell_type\": \"code\",\n" +
            "   \"execution_count\": null,\n" +
            "   \"metadata\": {},\n" +
            "   \"outputs\": [],\n" +
            "   \"source\": []\n" +
            "  }\n" +
            " ],\n" +
            " \"metadata\": {\n" +
            "  \"kernelspec\": {\n" +
            "   \"display_name\": \"mcci\",\n" +
            "   \"language\": \"python\",\n" +
            "   \"name\": \"mcci\"\n" +
            "  },\n" +
            "  \"language_info\": {\n" +
            "   \"name\": \"\"\n" +
            "  }\n" +
            " },\n" +
            " \"nbformat\": 4,\n" +
            " \"nbformat_minor\": 4\n" +
            "}";

    public static void setPythonEnvironment(ProcessBuilder pb) {
        Map<String, String> env = pb.environment();
        env.put("Path", PYTHON_ENV_PREFIX + env.get("Path"));
        env.put("PYTHONHOME", PYTHONHOME);
    }

    public static void initJupyterServer() {
        try {
            ResourceSystemManager.ensureDir(ResourceSystemManager.getRuntimeResourceAbsPath("mcci"));
            ProcessBuilder pb = new ProcessBuilder();
            setPythonEnvironment(pb);
            pb.command("resourcepacks/libmcci/Scripts/jupyter",
                    "lab", "--port=" + port, "--no-browser",
                    "--NotebookApp.token=''",
                    "--notebook-dir='resourcepacks/mcci'",
                    "--ExecutePreprocessor.kernel_name='mcci'")
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT);
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

    public static String getJupyterURL(String sessionName, CausalPageType kind) {
        String prefix = parsePrefix(kind);
        return getJupyterURL("/" + prefix + "/" + sessionName + "/" + parseKind(kind));
    }

    public static String getCommonURL(String sessionName, CausalPageType kind) {
        return "/mcci/" + sessionName + "/" + parseKind(kind);
    }

    private static Path getSessionDir(String sessionName) {
        return ResourceSystemManager.getRuntimeResourceAbsPath("mcci/" + sessionName);
    }

    public static String listSessions() throws IOException {
        Path baseDir = ResourceSystemManager.getRuntimeResourceAbsPath("mcci");
        return Files.list(baseDir).filter(p -> Files.isDirectory(p)).map(p -> p.getFileName().toString())
                .filter(n -> !n.startsWith("."))
                .collect(Collectors.joining(","));
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
        Path dotPath = Path.of(absDir.toString(), DOT_NAME);
        Path scriptPath = Path.of(absDir.toString(), SCRIPT_NAME);
        Files.createFile(dotPath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dotPath.toFile()))) {
            writer.write(String.format(DOT_TEMPLATE, sessionName));
        }
        Files.createFile(Path.of(absDir.toString(), DATA_NAME));
        Files.createFile(scriptPath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(scriptPath.toFile()))) {
            writer.write(SCRIPT_TEMPLATE);
        }
    }

    public static void changeSession(String sessionName) throws FileNotFoundException, NotDirectoryException {
        Path absDir = getSessionDir(sessionName);
        if (!Files.exists(absDir)) {
            throw new FileNotFoundException();
        } else if (!Files.isDirectory(absDir)) {
            throw new NotDirectoryException(absDir.toString());
        }
    }

    public static String parseKind(CausalPageType kind) {
        if (kind == CausalPageType.DOT) {
            return DOT_NAME;
        } else if (kind == CausalPageType.ANALYSIS) {
            return SCRIPT_NAME;
        } else if (kind == CausalPageType.DATA) {
            return DATA_NAME;
        } else {
            return IMAGE_NAME;
        }
    }

    public static String parsePrefix(CausalPageType kind) {
        if (kind == CausalPageType.ANALYSIS) {
            return "notebooks";
        } else if (kind == CausalPageType.IMAGE) {
            return "view";
        } else {
            return "edit";
        }
    }

    public static void viewPage(String sessionName, CausalPageType kind) throws IOException {
        Path absDir = getSessionDir(sessionName);
        if (!Files.exists(absDir) || !Files.isDirectory(absDir)) {
            throw new IOException("session '" + sessionName + "' state error");
        }
        String url = getJupyterURL(sessionName, kind);
        WebViewService.view(url, String.format("editing '%s' of session '%s'", parseKind(kind), sessionName), true, 1280, 720);
    }

    public static void viewRoot() {
        String url = getJupyterURL();
        if (url.isEmpty()) {
            return;
        }
        WebViewService.view(url, "Causal Analysis", true, 1280, 720);
    }

    public static void appendRecord(String sessionName, String record) throws IOException {
        Path path = ResourceSystemManager.getRuntimeResourceAbsPath("mcci/" + sessionName + "/" + DATA_NAME);
        Files.write(path, (record + "\n").getBytes(), StandardOpenOption.APPEND);
    }

    public static Pair<String, String> getRecordHeader(String sessionName) throws IOException {
        try (InputStream is = ResourceSystemManager.getRuntimeResourceAsStream("mcci/" + sessionName + "/" + DOT_NAME)) {
            MutableGraph g = new Parser().read(is);
            String main = g.nodes().stream().filter(n -> {
                Object o = n.attrs().get("label");
                if (o == null) {
                    return true;
                }
                return !"Unobserved Confounders".equals(o.toString());
            }).map(n -> n.name().toString()).collect(Collectors.joining(","));
            String unobservedConfounders = g.nodes().stream().filter(n -> {
                Object o = n.attrs().get("label");
                if (o == null) {
                    return false;
                }
                return "Unobserved Confounders".equals(o.toString());
            }).map(n -> n.name().toString()).collect(Collectors.joining(","));
            return Pair.of(main, unobservedConfounders);
        }
    }

    public static void generateGraph(String sessionName) throws IOException, ParserException {
        String absDir = getSessionDir(sessionName).toString();
        Path pngPath = Path.of(absDir, IMAGE_NAME);
        try (InputStream is = ResourceSystemManager.getRuntimeResourceAsStream(getCommonURL(sessionName, CausalPageType.DOT))) {
            MutableGraph g = new Parser().read(is);
            g.nodes().stream().filter(n -> {
                Object o = n.attrs().get("label");
                if (o == null) {
                    return false;
                }
                return "Unobserved Confounders".equals(o.toString());
            }).forEach(n -> n.attrs().add("label", null));
            Graphviz.fromGraph(g)
                    .basedir(new File(absDir)).render(Format.PNG).toFile(new File(pngPath.toString()));
        }
    }
}
