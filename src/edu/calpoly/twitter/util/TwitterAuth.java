package edu.calpoly.twitter.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.URLCodec;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

/**
 * This class is a utility class for the Twitter API v1.1 application based authentication.
 * To use the application bassed API, you will need a consumer/api key and secret.
 * See: https://dev.twitter.com/docs/auth/application-only-auth for details.
 *
 * This class provides a utility main to get a bearer token from a key and secret.
 */
public final class TwitterAuth {
   private static final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth2/token";

   // Static only.
   private TwitterAuth () {
      throw new UnsupportedOperationException();
   }

   public static void main(String[] args) {
      if (args.length != 2) {
         System.out.println(
               "USAGE: java edu.calpoly.twitter.util.TwitterAuth <api key> <api secret>");
         return;
      }

      String bearerToken = getBearerToken(args[0], args[1]);

      if (bearerToken == null) {
         System.out.println("There was an error getting the token. Check the logs.");
      } else {
         System.out.println("Bearer Token: " + bearerToken);
      }
   }

   /**
    * Use the api key and secret to generate the credential used to get the bearer token.
    */
   public static String generateTokenCredentials(String apiKey, String apiSecret) {
      try {
         URLCodec codec = new URLCodec();

         String encodedAPIKey = codec.encode(apiKey, "UTF-8");
         String encodedAPISecret = codec.encode(apiSecret, "UTF-8");

         String decodedCredentials = encodedAPIKey + ":" + encodedAPISecret;
         byte[] encodedCredentials = Base64.encodeBase64(decodedCredentials.getBytes());

         return new String(encodedCredentials);
      } catch (Exception ex) {
         Logger.logError("Unable to get the Twitter bearer token.", ex);
         return null;
      }
   }

   /**
    * Get the bearer token from Twitter.
    */
   public static String getBearerToken(String apiKey, String apiSecret) {
      HttpsURLConnection conn = null;
      String tokenCredentials = generateTokenCredentials(apiKey, apiSecret);

      try {
         URL url = new URL(REQUEST_TOKEN_URL);
         conn = (HttpsURLConnection)url.openConnection();
         conn.setDoOutput(true);
         conn.setDoInput(true);
         conn.setRequestMethod("POST");
         conn.setRequestProperty("Host", "api.twitter.com");
         conn.setRequestProperty("User-Agent", "BirdCatcher");
         conn.setRequestProperty("Authorization", "Basic " + tokenCredentials);
         conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
         conn.setRequestProperty("Content-Length", "29");
         conn.setUseCaches(false);

         writeRequest(conn, "grant_type=client_credentials");

         String response = readResponse(conn);
         JSONObject json = new JSONObject(response);

         if (!json.getString("token_type").equals("bearer")) {
            Logger.logError("Twitter did not give back a bearer token. Response: " + response);
            return null;
         }

         return json.getString("access_token");
      } catch (Exception ex) {
         Logger.logError("Unable to get Twitter bearer token.", ex);
         return null;
      } finally {
         if (conn != null) {
            conn.disconnect();
         }
      }
   }

   private static boolean writeRequest(HttpsURLConnection connection, String textBody) {
      try {
         BufferedWriter wr =
               new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

         wr.write(textBody);
         wr.flush();
         wr.close();

         return true;
      } catch (Exception ex) {
         Logger.logError("Error writing request.", ex);
         return false;
      }
   }

   private static String readResponse(HttpsURLConnection connection) {
      try {
         StringBuilder str = new StringBuilder();

         BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         String line = "";
         while ((line = br.readLine()) != null) {
            str.append(line + System.lineSeparator());
         }

         return str.toString();
      } catch (Exception ex) {
         Logger.logError("Error reading response.", ex);
         return new String();
      }
   }
}
