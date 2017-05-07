package com.sundogsoftware.sparkstreaming


import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.streaming._
import org.apache.spark.streaming.api.java.JavaStreamingContext
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.StreamingContext._
import org.apache.log4j.Level
import Utilities._
//import scala.util.parsing.json._
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import twitter4j._
import twitter4j.TwitterObjectFactory
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat;
import java.util.Locale;
import org.elasticsearch.spark._
import org.elasticsearch.spark.rdd.EsSpark


/** Simple application to listen to a stream of Tweets and print them out */
object TweetsWithMapping {
 
  /** Our main function where the action happens */
  def main(args: Array[String]) {

    // Configure Twitter credentials using twitter.txt
    //setupTwitter()
    
    // Set up a Spark streaming context named "PrintTweets" that runs locally using
    // all CPU cores and one-second batches of data
    
    val ssc = new StreamingContext("local[*]", "PrintTweets", Seconds(1))
    //val jssc= new JavaStreamingContext("local[*]", "PrintTweets", Seconds(1))
    // Get rid of log spam (should be called after the context is set up)
    setupLogging()
    
    // time setup
    def TIME_FORMAT = "EEE MMM d HH:mm:ss Z yyyy"
    val currentTime= new Date()
    
    //print(currentTime)
    
    
//    val tweets1 = TwitterUtils.createStream(ssc, None)
    val index_name = "newindex"
//    val type_name = "iPad"
//    tweets1.map(status => {
//      val hashmap = new java.util.HashMap[String, Object]()
//      hashmap.put("user_name",status.getUser().getName())
//      hashmap.put("user_lang",status.getUser().getLang())
//      hashmap.put("text",status.getText())
//      (new JSONObject(hashmap).toString())}
//    ).foreachRDD(jsonRDD => {
//     EsSpark.saveJsonToEs(jsonRDD, index_name+"/tweet")
//  }
//)
     
    // Create a DStream from Twitter using our streaming context
    val tweets = ssc.socketTextStream("localhost", 9994)
    val status = tweets.map(status => TwitterObjectFactory.createStatus(status))
    //val status = tweets.map(status => JSON.parseFull(status))
    //val text = status.map(status => compact(render(JSON\"text")))
    //val text = status.map(status => status match{case Some(m: Map[String, Any]) => m.get("text").get.asInstanceOf[String]})
    
    val statuses = status.map(statuses => statuses.getText)
    
    val Pattern = """<[^>]*>""".r
    status.map(status => {
      val hashmap = new java.util.HashMap[String, Object]()
      hashmap.put("user_name",status.getUser().getName())
      hashmap.put("user_lang",status.getUser().getLang())
      hashmap.put("text",status.getText())
      val device = Pattern.replaceAllIn(status.getSource(), "")
      hashmap.put("device",device)
      hashmap.put("createAt",status.getCreatedAt().toInstant.toString)
      hashmap.put("hashtags",status.getHashtagEntities.map(_.getText))
      
      if(status.getGeoLocation != null){
        val location = Array(status.getGeoLocation).map(geo => { s"${geo.getLatitude},${geo.getLongitude}" })
        hashmap.put("location",location)}
      hashmap.put("sentiment", SentimentAnalysisUtils.detectSentiment(status.getText).toString)
      hashmap.put("place",status.getPlace.getName.toString())
      val followers= status.getUser.getFollowersCount.toString()
      val friends = status.getUser.getFriendsCount.toString()
      val statusCount = status.getUser.getStatusesCount.toString()
      val textSize = status.getText.length().toString()
      hashmap.put("followers",followers)
      hashmap.put("friends",friends)
      hashmap.put("statusCount",statusCount)
      hashmap.put("textSize",textSize)
      print("Text Size: "+textSize)
      
      (new JSONObject(hashmap).toString())}
    ).foreachRDD(jsonRDD => {
     EsSpark.saveJsonToEs(jsonRDD, index_name+"/newmapping")
  }
)
     val timeAnalysis = status.map(status => status.getCreatedAt)
    
    // Blow out each word into a new DStream
    //val tweetwords = text.flatMap(tweetText => tweetText.split(" "))
    val tweetwords = statuses.flatMap(tweetText => tweetText.split(" "))
    val userMentionsEntity = status.flatMap(userMentions => userMentions.getUserMentionEntities())
    val userMentions =  userMentionsEntity.map(userMentions => userMentions.getScreenName() )
    // Now eliminate anything that's not a hashtag
    val hashtags = tweetwords.filter(word => word.startsWith("#"))
    val wordCounts = tweetwords.countByValue()
    val hashtagsLower = hashtags.map(word => word.toLowerCase())
    
    // Map each hashtag to a key/value pair of (hashtag, 1) so we can count them up by adding up the values
    val hashtagKeyValues = hashtagsLower.map(hashtag => (hashtag, 1))
    
    val userMentionKeyValues = userMentions.map(usermention => (usermention, 1))
    
    // Now count them up over a 5 minute window sliding every one second
    val hashtagCounts = hashtagKeyValues.reduceByKeyAndWindow( (x,y) => x + y, (x,y) => x - y, Seconds(300), Seconds(60))
    val userMentionCounts = userMentionKeyValues.reduceByKeyAndWindow( (x,y) => x + y, (x,y) => x - y, Seconds(300), Seconds(60))
    
    // Sort the results by the count values
    val sortedResultsHash = hashtagCounts.transform(rdd => rdd.sortBy(x => x._2, false))
     val sortedResultsMention = userMentionCounts.transform(rdd => rdd.sortBy(x => x._2, false))
    // Print the top 10
    sortedResultsHash.print
    sortedResultsMention.print
    
    
    
    //print(status.getText())
    // Now extract the text of each status update into RDD's using map()
    //val statuses = tweets.map(status => status.getText())
    
    // Print out the first ten
    //statuses.print()
    
    // Kick it all off
    ssc.checkpoint("C:/checkpoint/")
    ssc.start()
    ssc.awaitTermination()
  }  
}