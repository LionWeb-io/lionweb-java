package io.lionweb.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class NetworkUtils {

  private NetworkUtils() {
    // Prevent instantiation
  }

  public static String getStringFromUrl(URL url) throws IOException {
    return inputStreamToString(urlToInputStream(url, null));
  }

  private static String inputStreamToString(InputStream inputStream) throws IOException {
    try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[1024];
      int length;
      while ((length = inputStream.read(buffer)) != -1) {
        result.write(buffer, 0, length);
      }

      return result.toString();
    }
  }

  private static InputStream urlToInputStream(URL url, Map<String, String> args) {
    HttpURLConnection con = null;
    InputStream inputStream = null;
    try {
      con = (HttpURLConnection) url.openConnection();
      con.setConnectTimeout(15000);
      con.setReadTimeout(15000);
      if (args != null) {
        for (Map.Entry<String, String> e : args.entrySet()) {
          con.setRequestProperty(e.getKey(), e.getValue());
        }
      }
      con.connect();
      int responseCode = con.getResponseCode();
      /* By default the connection will follow redirects. The following
       * block is only entered if the implementation of HttpURLConnection
       * does not perform the redirect. The exact behavior depends to
       * the actual implementation (e.g. sun.net).
       * !!! Attention: This block allows the connection to
       * switch protocols (e.g. HTTP to HTTPS), which is <b>not</b>
       * default behavior. See: https://stackoverflow.com/questions/1884230
       * for more info!!!
       */
      if (responseCode < 400 && responseCode > 299) {
        String redirectUrl = con.getHeaderField("Location");
        try {
          URL newUrl = new URL(redirectUrl);
          return urlToInputStream(newUrl, args);
        } catch (MalformedURLException e) {
          URL newUrl = new URL(url.getProtocol() + "://" + url.getHost() + redirectUrl);
          return urlToInputStream(newUrl, args);
        }
      }
      /*!!!!!*/

      inputStream = con.getInputStream();
      return inputStream;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
