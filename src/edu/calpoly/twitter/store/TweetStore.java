package edu.calpoly.twitter.store;

import edu.calpoly.twitter.QueryTargetInfo;

import java.util.List;

/**
 * A place to hold tweets.
 * Ideally this would be some sort of database.
 * Use FakeTweetStore if you just need a quick store for testing.
 */
public interface TweetStore {

   /**
    * Get the last (most recent) twitter_id.
    *
    * @return The largest twitter_id, or "0" on error / no result.
    */
   public String getLastTweetId(QueryTargetInfo info);

   /**
   * Insert the tweets into the store.
   * The TweetStore now owns |data|.
   *
   * @param data The tweets to insert.
   *
   * @return The number of tweets actually inserted.
   *         There may be duplicates, so this number may be less than data.size().
   */
   public int insertTweets(QueryTargetInfo info, List<TweetData> data);
}
