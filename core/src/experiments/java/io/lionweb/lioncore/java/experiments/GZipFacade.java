package io.lionweb.lioncore.java.experiments;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

class GZipFacade {
  static byte[] compress(String str) {
    if (str == null || str.length() == 0) {
      throw new RuntimeException();
    }
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      GZIPOutputStream gzip = new GZIPOutputStream(out);
      gzip.write(str.getBytes());
      gzip.close();
      return out.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static String decompress(byte[] compressedData) {
    if (compressedData == null || compressedData.length == 0) {
      throw new RuntimeException();
    }

    StringBuilder outStr = new StringBuilder();
    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
        InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

      String line;
      while ((line = bufferedReader.readLine()) != null) {
        outStr.append(line);
      }
      return outStr.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
