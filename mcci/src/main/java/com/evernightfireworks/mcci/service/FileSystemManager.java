package com.evernightfireworks.mcci.service;

import com.evernightfireworks.mcci.CausalEngine;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSystemManager {
    public static InputStream getSourceResourceAsStream(String absolutePath) throws IOException {
        if (!absolutePath.startsWith("/")) {
            throw new IOException("'" + absolutePath + "' is not absolute path");
        }
        return CausalEngine.class.getResourceAsStream(absolutePath);
    }

    public static Path getRuntimeResourceAbsPath(String path) {
        return Paths.get(MinecraftClient.getInstance().getResourcePackDir().getAbsolutePath(), path);
    }

    @Environment(EnvType.CLIENT)
    public static InputStream getRuntimeResourceAsStream(String path) throws IOException {
        Path absPath = getRuntimeResourceAbsPath(path);
        return new FileInputStream(absPath.toFile());
    }

    @Environment(EnvType.CLIENT)
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
}
