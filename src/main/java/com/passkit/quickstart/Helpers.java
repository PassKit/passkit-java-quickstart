package com.passkit.quickstart;

import com.passkit.grpc.Image;
import com.passkit.grpc.ImagesGrpc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;


public class Helpers {

    private static ImagesGrpc.ImagesBlockingStub imageStub;

    public static String encodeFileToBase64(String path) throws IOException {
        try {
            File file = new File(path);
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            throw new IOException("could not read file: " + path, e);
        }
    }
}
