package com.github.yellowstonegames.glyph.rexpaint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

/**
 * Methods for compressing and decompressing byte arrays directly.
 * Imported from <a href="https://github.com/biscon/xpreader">biscon's xpreader project</a>.
 * <br>
 * Created by bison on 02-01-2016.
 */
public class CompressionUtils {

    public static byte[] compress(byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        deflater.finish();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer); // returns the generated code... index
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        return outputStream.toByteArray();
    }
    public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        return outputStream.toByteArray();
    }

    public static byte[] gzipDecodeByteArray(byte[] data) {
        GZIPInputStream gzipInputStream;
        try {
            gzipInputStream = new GZIPInputStream(
                    new ByteArrayInputStream(data));

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            byte[] buffer = new byte[1024];
            while (gzipInputStream.available() > 0) {
                int count = gzipInputStream.read(buffer, 0, 1024);
                if(count > 0) {
                    outputStream.write(buffer, 0, count);
                }
            }

            outputStream.close();
            gzipInputStream.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to gzip-decode byte array.");
        }
    }
}
