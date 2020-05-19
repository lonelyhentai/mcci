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
    private static final String DOT_NAME = "graph.dot";
    private static final String DATA_NAME = "data.csv";
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
            writer.write(String.format(DOT_TEMPLATE, sessionName, ""));
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
        } else if(kind==CausalPageType.IDENTIFY) {
            return IDENTIFY_NAME;
        } else if(kind==CausalPageType.INSTRUMENT) {
            return INSTRUMENT_NAME;
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

    public static List<String> getRecordFormat(String sessionName) throws IOException {
        try (InputStream is = ResourceSystemManager.getRuntimeResourceAsStream("mcci/" + sessionName + "/" + DOT_NAME)) {
            MutableGraph g = new Parser().read(is);
            String source = "";
            String target = "";
            ArrayList<String> unobserveds = new ArrayList<>();
            ArrayList<String> observeds = new ArrayList<>();
            for(var n: g.nodes()) {
                Object o = n.attrs().get(GraphPropertyType.LATENT.toString().toLowerCase());
                if(o != null) {
                    unobserveds.add(n.name().toString());
                } else {
                    observeds.add(n.name().toString());
                }
                Object s = n.attrs().get(GraphPropertyType.EXPOSURE.toString().toLowerCase());
                if(s != null) {
                    source = n.name().toString();
                }
                Object t = n.attrs().get(GraphPropertyType.OUTCOME.toString().toLowerCase());
                if(t!=null){
                    target = n.name().toString();
                }
            }
            return Arrays.asList(source+","+target,String.join(",",observeds), String.join(",",unobserveds));
        }
    }

    public static void generateGraph(String sessionName) throws IOException, ParserException {
        String absDir = getSessionDir(sessionName).toString();
        Path pngPath = Path.of(absDir, IMAGE_NAME);
        try (InputStream is = ResourceSystemManager.getRuntimeResourceAsStream(getCommonURL(sessionName, CausalPageType.DOT))) {
            MutableGraph g = new Parser().read(is);
            Graphviz.fromGraph(g)
                    .basedir(new File(absDir)).render(Format.PNG).toFile(new File(pngPath.toString()));
        }
    }

    public static void setGraph(String sessionName, String graphContent) throws IOException {
        graphContent = graphContent.replaceAll(" ","").replaceAll("[,;]",";\n");
        String dotUrl = getCommonURL(sessionName, CausalPageType.DOT);
        String fileContent = String.format(DOT_TEMPLATE, sessionName, graphContent);
        MutableGraph g = new Parser().read(fileContent);
        ResourceSystemManager.writeRuntimeResource(dotUrl, g.toString());
    }

    public static void defineSession(String sessionName, String source, String target) throws IOException, NoSuchFieldException, ParserException {
        String dotUrl = getCommonURL(sessionName, CausalPageType.DOT);
        String s = "";
        try (InputStream is = ResourceSystemManager.getRuntimeResourceAsStream(dotUrl)) {
            MutableGraph g = new Parser().read(is);
            boolean hasExposure = false;
            boolean hasOutcome = false;
            for (var n : g.nodes()) {
                if (n.name().toString().equals(source)) {
                    n.attrs().add(GraphPropertyType.EXPOSURE.toString().toLowerCase(), "");
                    hasExposure = true;
                } else {
                    n.attrs().add(GraphPropertyType.EXPOSURE.toString().toLowerCase(), null);
                }
                if (n.name().toString().equals(target)) {
                    n.attrs().add(GraphPropertyType.OUTCOME.toString().toLowerCase(), "");
                    hasOutcome = true;
                } else {
                    n.attrs().add(GraphPropertyType.OUTCOME.toString().toLowerCase(), null);
                }
            }
            ;
            if (!hasExposure) {
                throw new NoSuchFileException(String.format("source node %s is exist", source));
            } else if (!hasOutcome) {
                throw new NoSuchFieldException(String.format("target node %s not exist", target));
            }
            s = g.toString();
        }
        ResourceSystemManager.writeRuntimeResource(dotUrl, s);
    }

    public static void setUnobserveds(String sessionName, String unobservedStr) throws IOException, NoSuchFieldException {
        Set<String> unobserveds = Arrays.stream(unobservedStr.split(",")).collect(Collectors.toSet());
        String dotUrl = getCommonURL(sessionName, CausalPageType.DOT);
        String newContext = "";
        try (InputStream is = ResourceSystemManager.getRuntimeResourceAsStream(dotUrl)) {
            MutableGraph g = new Parser().read(is);
            for (var n : g.nodes()) {
                String nodeName = n.name().toString();
                if (unobserveds.contains(nodeName)) {
                    n.attrs().add(GraphPropertyType.LATENT.toString().toLowerCase(), "");
                    unobserveds.remove(nodeName);
                } else {
                    n.attrs().add(GraphPropertyType.LATENT.toString().toLowerCase(), null);
                }
            }
            if (!unobserveds.isEmpty()) {
                throw new NoSuchFieldException(String.format("unobserved node %s not exist",
                        String.join(",", unobserveds)));
            }
            newContext = g.toString();
        }
        ResourceSystemManager.writeRuntimeResource(dotUrl, newContext);
    }

    public static String identify(String sessionName) throws IOException, URISyntaxException {
        String dotUrl = getCommonURL(sessionName, CausalPageType.DOT);
        try (InputStream is = ResourceSystemManager.getRuntimeResourceAsStream(dotUrl)) {
            String dot = new String(is.readAllBytes(), StandardCharsets.UTF_8).replaceAll("^\\s*digraph", "dag");
            NodeJS nodeJs = NodeJS.createNodeJS();
            V8Object module = nodeJs.require(ResourceSystemManager.getSourceResourceFile("js/causal_model.js"));
            V8 rt = nodeJs.getRuntime();
            rt.add("CausalModel", module);
            rt.executeVoidScript(
                    String.format("var model = new CausalModel(`%s`);\n",dot)
                    + "var identification = JSON.stringify(model.identify());\n"
            );
            String ident =  rt.getString("identification");
            module.release();
            nodeJs.release();
            ResourceSystemManager.writeRuntimeResource(getCommonURL(sessionName, CausalPageType.IDENTIFY), ident);
            return ident;
        }
    }

    public static String instrument(String sessionName) throws IOException, URISyntaxException {
        String dotUrl = getCommonURL(sessionName, CausalPageType.DOT);
        try (InputStream is = ResourceSystemManager.getRuntimeResourceAsStream(dotUrl)) {
            String dot = new String(is.readAllBytes(), StandardCharsets.UTF_8).replaceAll("^\\s*digraph", "dag");
            NodeJS nodeJs = NodeJS.createNodeJS();
            V8Object module = nodeJs.require(ResourceSystemManager.getSourceResourceFile("js/causal_model.js"));
            V8 rt = nodeJs.getRuntime();
            rt.add("CausalModel", module);
            rt.executeVoidScript(
                    String.format("var model = new CausalModel(`%s`);\n",dot)
                            + "var instruments = JSON.stringify(model.instruments());\n"
            );
            String inst = rt.getString("instruments");
            module.release();
            nodeJs.release();
            ResourceSystemManager.writeRuntimeResource(getCommonURL(sessionName, CausalPageType.INSTRUMENT), inst);
            return inst;
        }
    }
}
