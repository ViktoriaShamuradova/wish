package com.example.wish.util;

import com.example.wish.exception.wish.ImageException;
import org.apache.http.entity.ContentType;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ImageUtil {



    public static void checkImage(MultipartFile file, String maxSize) throws HttpMediaTypeNotSupportedException, IOException {

        if (file.isEmpty()) throw new ImageException("image not sent");

        boolean isJpegPng = isJpegPng(file);
        if (!isJpegPng) {
            throw new HttpMediaTypeNotSupportedException("Invalid image format. Only " + MediaType.IMAGE_JPEG_VALUE + ", " + MediaType.IMAGE_PNG_VALUE + " are allowed.");
        }

        long maxSizeInBytes = parseSizeStringToBytes(maxSize);

        if (file.getSize() > maxSizeInBytes) {
            throw new FileSizeLimitExceededException("File size exceeds the allowed limit.", file.getSize(), maxSizeInBytes);
        }
        //     // Optionally, check image dimensions, if needed
        //            BufferedImage image = ImageIO.read(file.getInputStream());
        //            int width = image.getWidth();
        //            int height = image.getHeight();
        //            if (width > 1920 || height > 1080) {
        //                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image dimensions are too large.");
        //            }
    }

    public static boolean isJpegPng(MultipartFile file) throws IOException {
        return List.of(ContentType.IMAGE_JPEG.getMimeType(),
                ContentType.IMAGE_PNG.getMimeType()).contains(file.getContentType());
    }



    public static boolean isPng(MultipartFile file) throws IOException {
        // Check the magic number (file signature) to determine if it's a PNG image
        byte[] bytes = file.getBytes();
        return bytes.length >= 8 && bytes[0] == (byte) 0x89 &&
                bytes[1] == (byte) 0x50 && bytes[2] == (byte) 0x4E &&
                bytes[3] == (byte) 0x47 && bytes[4] == (byte) 0x0D &&
                bytes[5] == (byte) 0x0A && bytes[6] == (byte) 0x1A &&
                bytes[7] == (byte) 0x0A;
    }

    public static long parseSizeStringToBytes(String sizeString) {
        long bytes = 0;
        sizeString = sizeString.trim().toUpperCase();

        if (sizeString.endsWith("TB")) {
            bytes = Long.parseLong(sizeString.substring(0, sizeString.length() - 2).trim()) * 1024L * 1024L * 1024L * 1024L;
        } else if (sizeString.endsWith("GB")) {
            bytes = Long.parseLong(sizeString.substring(0, sizeString.length() - 2).trim()) * 1024L * 1024L * 1024L;
        } else if (sizeString.endsWith("MB")) {
            bytes = Long.parseLong(sizeString.substring(0, sizeString.length() - 2).trim()) * 1024L * 1024L;
        } else if (sizeString.endsWith("KB")) {
            bytes = Long.parseLong(sizeString.substring(0, sizeString.length() - 2).trim()) * 1024L;
        } else if (sizeString.endsWith("B")) {
            bytes = Long.parseLong(sizeString.substring(0, sizeString.length() - 1).trim());
        } else {
            throw new IllegalArgumentException("Invalid size format: " + sizeString);
        }

        return bytes;
    }

    public static byte[] compressImage(byte[] data) {

        Deflater deflater = new Deflater();
        deflater.setLevel(Deflater.BEST_COMPRESSION);
        deflater.setInput(data);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] tmp = new byte[4 * 1024];
        while (!deflater.finished()) {
            int size = deflater.deflate(tmp);
            outputStream.write(tmp, 0, size);
        }
        try {
            outputStream.close();
        } catch (Exception e) {
        }
        return outputStream.toByteArray();
    }

    public static byte[] decompressImage(byte[] data) {
        if (data != null) {
            Inflater inflater = new Inflater();
            inflater.setInput(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            byte[] tmp = new byte[4 * 1024];
            try {
                while (!inflater.finished()) {
                    int count = inflater.inflate(tmp);
                    outputStream.write(tmp, 0, count);
                }
                outputStream.close();
            } catch (Exception exception) {
            }
            return outputStream.toByteArray();
        } else {
            return null;
        }
    }
}
