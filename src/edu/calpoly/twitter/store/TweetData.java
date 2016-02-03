package edu.calpoly.twitter.store;

/**
  * A helper class to encapsulate all the data for a tweet returned from Twitter.
  */
public class TweetData {
   public String twitterId;
   public int published;
   public String content;
   public String source;
   public String lang;
   public String author;
   public String place;
   public String geo;

   public String fullPlace;
   public String fullGeo;

   /**
     * All the attributes returned by toString().
     */
   public final static String attributes =
      "(id, twitter_id, published, content, source, lang, author, place, geo)";

   /**
     * A String that you can use for a prepared statement that matches up with
     *  both attributes and the toString().
     */
   public final static String preparedStatement = "(NULL, ?, ?, ?, ?, ?, ?, ?, ?)";

   /**
     * Get all the data encapsulated by this class, SQL style.
     * String values are quoted.
     *
     * @return The data for this Tweet in a nice string.
     */
   public String toString() {
      return String.format("(NULL, '%s', %d, '%s', '%s', '%s', '%s', '%s', '%s')",
      twitterId, published, content, source, lang, author, place, geo);
   }
}
