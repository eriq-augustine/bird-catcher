package edu.calpoly.twitter;

import edu.calpoly.twitter.store.TweetData;
import edu.calpoly.twitter.store.TweetStore;
import edu.calpoly.twitter.util.Logger;
import edu.calpoly.twitter.util.Props;
import edu.calpoly.twitter.util.TwitterAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;

/**
 * Get tweets from Twitter.
 * After getting tweets, it will put them in the specified store.
 * After constructed, just call update() when you want it to fetch tweets.
 */
public class JSONTwitterGatherer {
   /**
    * The base URL to use for the query to Twitter.
    * The only thing left is to add the actual query ('q') parameter.
    */
   private static final String URL_BASE = "https://api.twitter.com/1.1/search/tweets.json";

   /**
    * The number of allowed results per page that Twitter allows.
    */
   private static final int RPP = 100;

   /**
    * The maximum number of pages that Twitter will give.
    */
   private static final int MAX_PAGES = 15;

   /**
    * The number of seconds that are in an atomic frame.
    */
   private static final int SEC_PER_FRAME = 60;

   /**
    * The format that the date is given from Twitter.
    * Ex: Wed Apr 09 05:49:59 +0000 2014
    */
   private static final String TIME_FORMAT = "EEE MMM dd HH:mm:ss Z yyyy";

   /**
    * The info for each target of this Gatherer.
    */
   private List<QueryTargetInfo> queries;

   /**
    * The place to hold the tweets.
    */
   private TweetStore store;

   private final String kBearerToken;

   public JSONTwitterGatherer(TweetStore store, String queryString) {
      this(store, new ArrayList<String>(Arrays.asList(queryString)));
   }

   public JSONTwitterGatherer(TweetStore store, List<String> queryStrings) {
      kBearerToken = TwitterAuth.getBearerToken(Props.getString("TWITTER_API_KEY"),
                                                Props.getString("TWITTER_API_SECRET"));

      this.store = store;
      queries = new ArrayList<QueryTargetInfo>();

      for (String queryString : queryStrings) {
         queries.add(new QueryTargetInfo(queryString));
      }
   }

   /**
    * @inheritDoc
    */
   public void update() {
      for (QueryTargetInfo info : queries) {
         if (info.lastSeenId == null) {
            info.lastSeenId = store.getLastTweetId(info);
         }

         int count = gather(info);
         Logger.log(String.format("Gathered %d tweets for the query\"%s\".", count, info.query));
      }
   }

   /**
   * Get tweets from Twitter and insert them into the databse.
   *
   * @param info The query to ask Twitter for.
   *
   * @return The number of tweets actually inserted
   */
   private int gather(QueryTargetInfo info) {
      ArrayList<TweetData> res;
      int count = 0;
      int tempCount;
      int ndx;
      boolean flag = true;

      info.smallestId = null;

      do {
         flag = true;
         res = query(info);

         //If there was an error getting the tweets, then don't continue.
         if (res == null) {
            flag = false;
         } else {
            //If we did not get the maximum tweets, then don't continue
            if (res.size() < (MAX_PAGES * RPP)) {
               flag = false;
            }

            //Save the results in the db.
            tempCount = store.insertTweets(info, res);

            count += tempCount;
         }
      } while (flag);

      return count;
   }


   /**
   * Perform a query to Twitter.
   *
   * @param info The query to pass onto Twitter.
   *
   * @.pre sinceId should not be negative
   *
   * @return The data for each Tweet that was returned.
   */
   private ArrayList<TweetData> query(QueryTargetInfo info) {
      String url;
      ArrayList<TweetData> tweets = new ArrayList<TweetData>();
      InputStream is = null;

      // lastSeenId should have been set earlier.
      // However, if it is still null, just use "0".
      if (info.lastSeenId == null) {
         url = String.format("%s?q=%s&count=%d&result_type=recent&since_id=0",
          URL_BASE, info.query, RPP);
      } else if (info.smallestId == null) {
         url = String.format("%s?q=%s&count=%d&result_type=recent&since_id=%s",
          URL_BASE, info.query, RPP, info.lastSeenId);
      } else {
         url = String.format("%s?q=%s&count=%d&result_type=recent&since_id=%s&max_id=%s",
          URL_BASE, info.query, RPP, info.lastSeenId, info.smallestId);
      }

      try {
         do {
            URL searchURL = new URL(url);
            HttpsURLConnection searchConnection = (HttpsURLConnection)searchURL.openConnection();

            searchConnection.setRequestProperty("Host", "api.twitter.com");
            searchConnection.setRequestProperty("User-Agent", "BirdCatcher");
            searchConnection.setRequestProperty("Authorization", "Bearer " + kBearerToken);

            is = searchConnection.getInputStream();

            JSONTokener jsonTokener = new JSONTokener(is);

            JSONObject json = new JSONObject(jsonTokener);

            is.close();

            url = getNextLink(json, url, info);

            tweets.addAll(getTweets(json, info));

            Thread.sleep(1000);

            is = null;
         } while (url != null);
      } catch (Exception e) {
         Logger.logError("Error performing query", e);

         if (is != null) {
            try {
               java.io.BufferedReader in =
                new java.io.BufferedReader(new java.io.InputStreamReader(is));

               String response = "Response from Twitter:\n";
               String temp;

               while ((temp = in.readLine()) != null) {
                  response += (temp + "\n");
               }

               Logger.logDebug(response);

               response = null;
               temp = null;
            } catch (Exception ex) {
            }
         }

         return tweets;
      }

      return tweets;
   }

   /**
   * Get the next element from feed if it exists.
   *
   * @.pre json is the JSON returned bt the query
   *
   * @return The link to the next request to make if there is one.
   * If there is no next page, return a null.
   */
   private String getNextLink(JSONObject json, String oldUrl, QueryTargetInfo info) {
      String rtn = "";

      try {
         JSONObject searchMeta = json.getJSONObject("search_metadata");
         rtn = URL_BASE + searchMeta.getString("next_results");
      } catch (JSONException jsonEx) {
         return null;
      }

      return rtn;
   }

   /**
    * Extract TweetData from a JSONObject.
    *
    * @param json The JSONObject returned by the query.
    *
    * @return The tweets encapsulated in a TweetData.
    */
   private ArrayList<TweetData> getTweets(JSONObject json, QueryTargetInfo info) {
      ArrayList<TweetData> tweets = new ArrayList<TweetData>();

      JSONArray jsonArray = json.optJSONArray("statuses");


      if (jsonArray == null) {
         return tweets;
      }

      for (int i = 0; i < jsonArray.length(); i++) {
         try {
            TweetData tweet = new TweetData();
            JSONObject tweetJson = jsonArray.getJSONObject(i);

            SimpleDateFormat df = new SimpleDateFormat(TIME_FORMAT);

            tweet.twitterId = tweetJson.getString("id_str");
            tweet.published = (int)(df.parse(tweetJson.getString("created_at")).getTime() / 1000);
            tweet.content = tweetJson.getString("text");
            tweet.source = tweetJson.optString("source", "<No Source>");
            tweet.lang = tweetJson.optString("lang", "en");

            tweet.author = "Jon Doe";
            JSONObject user = tweetJson.optJSONObject("user");
            if (user != null) {
               tweet.author = user.optString("screen_name", "Jon Doe");
            }

            tweet.place = tweetJson.optString("place", "");
            tweet.geo = tweetJson.optString("geo", "");

            // Keep track of the smallest id.
            if (info.smallestId == null ||
                Long.valueOf(tweet.twitterId) < Long.valueOf(info.smallestId)) {
               info.smallestId = tweet.twitterId;
            }

            // Keep track of the last seen (max) id.
            if (info.lastSeenId == null ||
                Long.valueOf(tweet.twitterId) > Long.valueOf(info.lastSeenId)) {
               info.lastSeenId = tweet.twitterId;
            }

            tweets.add(tweet);
         } catch (Exception ex) {
            Logger.logError("Unable to parse tweet.", ex);
         }
      }

      return tweets;
   }
}
