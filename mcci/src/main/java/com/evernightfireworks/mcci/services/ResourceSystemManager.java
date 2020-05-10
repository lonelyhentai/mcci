package com.evernightfireworks.mcci.services;

import com.evernightfireworks.mcci.CausalEngine;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.file.*;

public class ResourceSystemManager {
    public static InputStream getSourceResourceAsStream(String path) {
        return CausalEngine.class.getResourceAsStream("/assets/mcci/"+ path);
    }

    public static Path getRuntimeResourceAbsPath(String path) {
        return Paths.get(MinecraftClient.getInstance().getResourcePackDir().getAbsolutePath(), path);
    }

    public static InputStream getRuntimeResourceAsStream(String path) throws IOException {
        Path absPath = getRuntimeResourceAbsPath(path);
        return new FileInputStream(absPath.toFile());
    }

    public static void writeRuntimeResource(String path, String content) throws IOException {
        Path absPath = getRuntimeResourceAbsPath(path);
        File file = ensureAndWriteFile(absPath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        }
    }

    public static File ensureAndWriteFile(Path absPath) throws IOException {
        Path dir = absPath.getParent();
        if(!Files.exists(dir)) {
            Files.createDirectory(dir);
        } else if(!Files.isDirectory(dir)) {
            throw new IOException(String.format("parent of '%s' is not a directory", absPath.toString()));
        }
        Files.deleteIfExists(absPath);
        Files.createFile(absPath);
        return absPath.toFile();
    }

    public static void ensureDir(Path absDir) throws IOException {
        if(!Files.exists(absDir)) {
            Files.createDirectory(absDir);
        } else if(!Files.isDirectory(absDir)) {
            throw new IOException(String.format("'%s' exists but is not a directory", absDir.toString()));
        }
    }


}
