package edu.calpoly.twitter.store;

import edu.calpoly.twitter.QueryTargetInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This store doesn't actually persist any data.
 * It just holds it all in a list.
 */
public class FakeTweetStore implements TweetStore {
   /**
    * All the deduplicated tweets we have seen.
    */
   private List<TweetData> tweets;

   /**
    * All the ids we have seen so we can dedup.
    */
   private Set<String> ids;

   private long maxId;

   public FakeTweetStore() {
      tweets = new ArrayList<TweetData>();
      ids = new HashSet<String>();
      maxId = 0;
   }

   /**
    * @inheritDoc
    */
   public String getLastTweetId(QueryTargetInfo info) {
      return "" + maxId;
   }

   /**
    * @inheritDoc
    */
   public int insertTweets(QueryTargetInfo info, List<TweetData> data) {
      int count = 0;

      for (TweetData tweet : data) {
         if (!ids.contains(tweet.twitterId)) {
            ids.add(tweet.twitterId);
            tweets.add(tweet);

            if (maxId < Long.valueOf(tweet.twitterId)) {
               maxId = Long.valueOf(tweet.twitterId);
            }

            count++;
         }
      }

      return count;
   }

   public List<TweetData> getTweets() {
      return tweets;
   }

   public int size() {
      return tweets.size();
   }
}
