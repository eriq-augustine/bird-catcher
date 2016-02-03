package edu.calpoly.twitter;

import edu.calpoly.twitter.store.FakeTweetStore;
import edu.calpoly.twitter.store.TweetData;
import edu.calpoly.twitter.store.TweetStore;
import edu.calpoly.twitter.util.Logger;
import edu.calpoly.twitter.util.Props;

/**
 * A simple sample driver to get some tweets.
 */
public class GatherTweets {
   public static void main(String[] args) {
      Props.readFile("config/base.properties");
      Props.readFile("config/secrets.properties");

      Logger.init();

      FakeTweetStore store = new FakeTweetStore();

      JSONTwitterGatherer gatherer = new JSONTwitterGatherer(store, "calpoly");

      gatherer.update();

      System.out.println("Collected " + store.size() + " tweets:");
      for (TweetData tweet : store.getTweets()) {
         System.out.println("   " + tweet);
      }

      Logger.tearDown();
   }
}
