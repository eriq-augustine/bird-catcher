package edu.calpoly.twitter;

/**
  * For each differnt query to twitter, we need to keep ahold of some
  * metadata about the search for paging purposes.
  */
public class QueryTargetInfo {
   public String query;

   /**
     * The most recent tweet seen by the Gatherer.
     */
   public String lastSeenId;

   /**
     * The smallest id seen by a chain of queries to Twitter.
     * Note: Twitter give results from most recent to least recent.
     */
   public String smallestId;

   public QueryTargetInfo(String query) {
      this.query = query;
      this.lastSeenId = null;
      this.smallestId = null;
   }
}
