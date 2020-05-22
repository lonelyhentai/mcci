package com.evernightfireworks.mcci.services;

import com.eclipsesource.v8.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import guru.nidi.graphviz.parse.ParserException;
import joptsimple.internal.Strings;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
        IDENTIFY,
        INSTRUMENT
    }

    public static enum GraphPropertyType {
        EXPOSURE,
        OUTCOME,
        LATENT,
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
    public static final String SESSION_DEFAULT = "default";
    private static final String DOT_NAME = "graph.dot";
    public static final String DATA_DEFAULT = "default";
    private static final String DATA_NAME_PREFIX = "data";
    private static final String DATA_NAME_EXT = ".csv";
    private static final String DATA_NAME = DATA_NAME_PREFIX + DATA_NAME_EXT;
    private static final String SCRIPT_NAME = "analysis.ipynb";
    private static final String IMAGE_NAME = "graph.png";
    private static final String IDENTIFY_NAME = "identify.json";
    private static final String INSTRUMENT_NAME = "instrument.json";
    private static final String DOT_TEMPLATE = "digraph %s { %s \n}";
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

    private static String parseKind(CausalPageType kind) {
        if (kind == CausalPageType.DOT) {
            return DOT_NAME;
        } else if (kind == CausalPageType.ANALYSIS) {
            return SCRIPT_NAME;
        } else if (kind == CausalPageType.DATA) {
            return DATA_NAME;
        } else if (kind == CausalPageType.IDENTIFY) {
            return IDENTIFY_NAME;
        } else if (kind == CausalPageType.INSTRUMENT) {
            return INSTRUMENT_NAME;
        } else {
            return IMAGE_NAME;
        }
    }

    private static String parseKind(CausalPageType kind, String extra) {
        String res = parseKind(kind);
        return res.replaceAll("(\\.[^\\.]+)$", String.format("%s$1", extra));
    }

    private static String parsePrefix(CausalPageType kind) {
        if (kind == CausalPageType.ANALYSIS) {
            return "notebooks";
        } else if (kind == CausalPageType.IMAGE) {
            return "view";
        } else {
            return "edit";
        }
    }

    private static Path getSessionAbsPath(String sessionName) {
        return ResourceSystemManager.getRuntimeResourceAbsPath("mcci/" + sessionName);
    }

    private static Path getCommonAbsPath(String sessionName, CausalPageType kind) {
        return ResourceSystemManager.getRuntimeResourceAbsPath(getCommonURL(sessionName, kind));
    }

    private static Path getCommonAbsPath(String sessionName, CausalPageType kind, String extra) {
        return ResourceSystemManager.getRuntimeResourceAbsPath(getCommonURL(sessionName, kind, extra));
    }

    private static InputStream getCommonAsStream(String sessionName, CausalPageType kind) throws IOException {
        return ResourceSystemManager.getRuntimeResourceAsStream(getCommonURL(sessionName, kind));
    }

    private static InputStream getCommonAsStream(String sessionName, CausalPageType kind, String extra) throws IOException {
        return ResourceSystemManager.getRuntimeResourceAsStream(getCommonURL(sessionName, kind, extra));
    }

    private static void writeCommon(String sessionName, CausalPageType kind, String content) throws IOException {
        ResourceSystemManager.ensureAndWriteFile(getCommonAbsPath(sessionName, kind));
        ResourceSystemManager.writeRuntimeResource(getCommonURL(sessionName, kind), content);
    }

    private static void writeCommon(String sessionName, CausalPageType kind, String extra, String content) throws IOException {
        ResourceSystemManager.ensureAndWriteFile(getCommonAbsPath(sessionName, kind));
        ResourceSystemManager.writeRuntimeResource(getCommonURL(sessionName, kind, extra), content);
    }


    private static void setPythonEnvironment(ProcessBuilder pb) {
        Map<String, String> env = pb.environment();
        env.put("Path", PYTHON_ENV_PREFIX + env.get("Path"));
        env.put("PYTHONHOME", PYTHONHOME);
    }

    private static String graphTypeDigraph2DAG(InputStream is) throws IOException {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8)
                .replaceAll("^\\s*digraph", "dag");
    }

    public static void initJupyterServer() {
        try {
            ResourceSystemManager.ensureDir(ResourceSystemManager.getRuntimeResourceAbsPath("mcci"));
            if(!Files.exists(getSessionAbsPath(SESSION_DEFAULT))) {
                sessionCreate(SESSION_DEFAULT);
            }
            ProcessBuilder pb = new ProcessBuilder();
            setPythonEnvironment(pb);
            pb.command("resourcepacks/libmcci/Scripts/jupyter",
                    "lab", "--port=" + port, "--no-browser",
                    "--NotebookApp.token=''",
                    "--notebook-dir='resourcepacks/mcci'"
            )
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

    public static String getJupyterURL(String sessionName, CausalPageType kind, String extra) {
        String prefix = parsePrefix(kind);
        return getJupyterURL("/" + prefix + "/" + sessionName + "/" + parseKind(kind, extra));
    }

    public static String getCommonURL(String sessionName, CausalPageType kind) {
        return "/mcci/" + sessionName + "/" + parseKind(kind);
    }

    public static String getCommonURL(String sessionName, CausalPageType kind, String extra) {
        return "/mcci/" + sessionName + "/" + parseKind(kind, extra);
    }

    /* session */

    public static String sessionList() throws IOException {
        Path baseDir = ResourceSystemManager.getRuntimeResourceAbsPath("mcci");
        return Files.list(baseDir).filter(p -> Files.isDirectory(p)).map(p -> p.getFileName().toString())
                .filter(n -> !n.startsWith("."))
                .collect(Collectors.joining(","));
    }

    public static void sessionDelete(String sessionName) throws IOException,  UnsupportedOperationException {
        if (SESSION_DEFAULT.equals(sessionName)) {
            throw new UnsupportedOperationException();
        }
        Path absDir = getSessionAbsPath(sessionName);
        if (!Files.exists(absDir)) {
            throw new FileNotFoundException(absDir.toString());
        } else if (!Files.isDirectory(absDir)) {
            throw new NotDirectoryException(absDir.toString());
        } else {
            FileUtils.deleteDirectory(new File(absDir.toString()));
        }
    }

    public static void sessionCreate(String sessionName) throws IOException {
        Path dir = getSessionAbsPath(sessionName);
        if (Files.exists(dir)) {
            throw new FileAlreadyExistsException(dir.toString());
        }
        Files.createDirectory(dir);
        writeCommon(sessionName, CausalPageType.DOT, String.format(DOT_TEMPLATE, sessionName, ""));
        writeCommon(sessionName, CausalPageType.ANALYSIS, SCRIPT_TEMPLATE);
        dataCreate(sessionName, DATA_DEFAULT);
    }

    public static void sessionChange(String sessionName) throws FileNotFoundException, NotDirectoryException {
        Path absDir = getSessionAbsPath(sessionName);
        if (!Files.exists(absDir)) {
            throw new FileNotFoundException();
        } else if (!Files.isDirectory(absDir)) {
            throw new NotDirectoryException(absDir.toString());
        }
    }

    public static void sessionDefine(String sessionName, String exposure, String outcome)
            throws IOException, NoSuchFieldException, ParserException {
        String s = "";
        try (InputStream is = getCommonAsStream(sessionName, CausalPageType.DOT)) {
            MutableGraph g = new Parser().read(is);
            boolean hasExposure = false;
            boolean hasOutcome = false;
            for (var n : g.nodes()) {
                if (n.name().toString().equals(exposure)) {
                    n.attrs().add(GraphPropertyType.EXPOSURE.toString().toLowerCase(), "");
                    hasExposure = true;
                } else {
                    n.attrs().add(GraphPropertyType.EXPOSURE.toString().toLowerCase(), null);
                }
                if (n.name().toString().equals(outcome)) {
                    n.attrs().add(GraphPropertyType.OUTCOME.toString().toLowerCase(), "");
                    hasOutcome = true;
                } else {
                    n.attrs().add(GraphPropertyType.OUTCOME.toString().toLowerCase(), null);
                }
            }
            ;
            if (!hasExposure) {
                throw new NoSuchFileException(String.format("exposure node %s is exist", exposure));
            } else if (!hasOutcome) {
                throw new NoSuchFieldException(String.format("outcome node %s not exist", outcome));
            }
            s = g.toString();
        }
        writeCommon(sessionName, CausalPageType.DOT, s);
    }

    /* view page */

    public static void viewPage(String sessionName, CausalPageType kind) throws IOException {
        String url = getJupyterURL(sessionName, kind);
        WebViewService.view(url, String.format("editing '%s' of session '%s'", parseKind(kind), sessionName),
                true, 1280, 720);
    }

    public static void viewPage(String sessionName, CausalPageType kind, String extra) throws IOException {
        String url = getJupyterURL(sessionName, kind, extra);
        WebViewService.view(url, String.format("editing '%s' of session '%s'", parseKind(kind, extra), sessionName), true, 1280, 720);
    }

    public static void viewRoot() {
        String url = getJupyterURL();
        if (url.isEmpty()) {
            return;
        }
        WebViewService.view(url, "Causal Analysis", true, 1280, 720);
    }

    /* data */

    public static void dataInsert(String sessionName, String tableName, String record) throws IOException {
        Path path = getCommonAbsPath(sessionName, CausalPageType.DATA, tableName);
        Files.write(path, (record + "\n").getBytes(), StandardOpenOption.APPEND);
    }

    public static void dataCreate(String sessionName, String tableName) throws IOException {
        Path p = getCommonAbsPath(sessionName, CausalPageType.DATA, tableName);
        if (Files.exists(p)) {
            throw new FileAlreadyExistsException(p.toString());
        }
        Files.createFile(p);
    }

    public static void dataChange(String sessionName, String tableName) throws IOException {
        Path p = getCommonAbsPath(sessionName, CausalPageType.DATA, tableName);
        if (!Files.exists(p) || Files.isDirectory(p)) {
            throw new FileNotFoundException();
        }
    }

    public static String dataList(String sessionName) throws IOException {
        Path absDir = getSessionAbsPath(sessionName);
        return Files.list(absDir).filter(p -> Files.isRegularFile(p)).map(p -> p.getFileName().toString())
                .filter(f -> f.startsWith(DATA_NAME_PREFIX) && f.endsWith(DATA_NAME_EXT))
                .map(f -> f.replaceAll(String.format("^%s(.*)%s$", DATA_NAME_PREFIX, DATA_NAME_EXT), "$1"))
                .collect(Collectors.joining(","));
    }

    public static void dataDelete(String sessionName, String tableName) throws IOException,  UnsupportedOperationException {
        if(DATA_DEFAULT.equals(tableName)) {
            throw new UnsupportedOperationException();
        }
        Path p = getCommonAbsPath(sessionName, CausalPageType.DATA, tableName);
        Files.delete(p);
    }

    /* graph */

    public static List<String> graphDescribe(String sessionName) throws IOException {
        try (InputStream is = getCommonAsStream(sessionName, CausalPageType.DOT)) {
            MutableGraph g = new Parser().read(is);
            String exposure = "";
            String outcome = "";
            ArrayList<String> latents = new ArrayList<>();
            ArrayList<String> observeds = new ArrayList<>();
            for (var n : g.nodes()) {
                Object o = n.attrs().get(GraphPropertyType.LATENT.toString().toLowerCase());
                if (o != null) {
                    latents.add(n.name().toString());
                } else {
                    observeds.add(n.name().toString());
                }
                Object s = n.attrs().get(GraphPropertyType.EXPOSURE.toString().toLowerCase());
                if (s != null) {
                    exposure = n.name().toString();
                }
                Object t = n.attrs().get(GraphPropertyType.OUTCOME.toString().toLowerCase());
                if (t != null) {
                    outcome = n.name().toString();
                }
            }
            return Arrays.asList(exposure + "," + outcome, String.join(",", observeds),
                    String.join(",", latents));
        }
    }

    public static void graphPlot(String sessionName) throws IOException, ParserException {
        Path absDir = getSessionAbsPath(sessionName);
        try (InputStream is = getCommonAsStream(sessionName, CausalPageType.DOT)) {
            MutableGraph g = new Parser().read(is);
            Graphviz.fromGraph(g)
                    .basedir(absDir.toFile()).render(Format.PNG)
                    .toFile(getCommonAbsPath(sessionName, CausalPageType.IMAGE).toFile());
        }
    }

    public static void graphContent(String sessionName, String graphContent) throws IOException {
        graphContent = graphContent.replaceAll(" ", "").replaceAll("[,;]", ";\n");
        String fileContent = String.format(DOT_TEMPLATE, sessionName, graphContent);
        MutableGraph g = new Parser().read(fileContent);
        writeCommon(sessionName, CausalPageType.DOT, g.toString());
    }

    public static void graphSetLatents(String sessionName, String latentStr) throws IOException, NoSuchFieldException {
        Set<String> latents = Arrays.stream(latentStr.split(",")).filter(f->!f.isEmpty()).collect(Collectors.toSet());
        String newContext = "";
        try (InputStream is = getCommonAsStream(sessionName, CausalPageType.DOT)) {
            MutableGraph g = new Parser().read(is);
            for (var n : g.nodes()) {
                String nodeName = n.name().toString();
                if (latents.contains(nodeName)) {
                    n.attrs().add(GraphPropertyType.LATENT.toString().toLowerCase(), "");
                    latents.remove(nodeName);
                } else {
                    n.attrs().add(GraphPropertyType.LATENT.toString().toLowerCase(), null);
                }
            }
            if (!latents.isEmpty()) {
                throw new NoSuchFieldException(String.format("latents node %s not exist",
                        String.join(",", latents)));
            }
            newContext = g.toString();
        }
        writeCommon(sessionName, CausalPageType.DOT, newContext);
    }

    /* identify */

    public static String identify(String sessionName, String paths) throws IOException, URISyntaxException {
        try (InputStream is = getCommonAsStream(sessionName, CausalPageType.DOT)) {
            String dot = graphTypeDigraph2DAG(is);
            NodeJS nodeJs = null;
            V8Object module = null;
            String ident;
            try {
                nodeJs = NodeJS.createNodeJS();
                module = nodeJs.require(ResourceSystemManager.getSourceResourceFile("js/causal_model.js"));
                V8 rt = nodeJs.getRuntime();
                rt.add("CausalModel", module);
                rt.executeVoidScript(
                        String.format("var model = new CausalModel(`%s`);\n", dot)
                                + String.format("var identification = JSON.stringify(model.identify('%s'));\n",
                                paths == null ? "" : paths)
                );
                ident = rt.getString("identification");
            } catch (Exception e) {
                if (module != null) {
                    module.release();
                }
                if (nodeJs != null) {
                    nodeJs.release();
                }
                throw e;
            }
            writeCommon(sessionName, CausalPageType.IDENTIFY, ident);
            return ident;
        }
    }


    /* instrument */
    public static String instrument(String sessionName) throws IOException, URISyntaxException {
        try (InputStream is = getCommonAsStream(sessionName, CausalPageType.DOT)) {
            String dot = graphTypeDigraph2DAG(is);
            NodeJS nodeJs = null;
            V8Object module = null;
            String inst;
            try {
                nodeJs = NodeJS.createNodeJS();
                module = nodeJs.require(ResourceSystemManager.getSourceResourceFile("js/causal_model.js"));
                V8 rt = nodeJs.getRuntime();
                rt.add("CausalModel", module);
                rt.executeVoidScript(
                        String.format("var model = new CausalModel(`%s`);\n", dot)
                                + "var instruments = JSON.stringify(model.instruments());\n"
                );
                inst = rt.getString("instruments");
            } catch (Exception e) {
                if (module != null) {
                    module.release();
                }
                if (nodeJs != null) {
                    nodeJs.release();
                }
                throw e;
            }
            writeCommon(sessionName, CausalPageType.INSTRUMENT, inst);
            return inst;
        }
    }
}
