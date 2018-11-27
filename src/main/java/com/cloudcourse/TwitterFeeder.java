package com.cloudcourse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.sql.*;


public class TwitterFeeder {
    private static Connection conn = null;
    public static void main(String[] args) throws TwitterException, IOException {
        createNewDB("TwitterFeeder");
        connectToDB("TwitterFeeder");

        // Create our configuration
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("mDARaDPpa3Iell6prBIb0PQHC")
                .setOAuthConsumerSecret("5inc1VdZvxIKIXi4NibfUoZjf2BMTX7bBJJ3BXLihal5UK4GGB")
                .setOAuthAccessToken("1059910371450986497-MA1F6omvuboXxuWu7LVK0QYANkZApe")
                .setOAuthAccessTokenSecret("pTR3qdYBdRcAzghnC5ZVBh6b380BNDTNCzW5U6lpBBN12");

        // Create our Twitter stream
        TwitterStreamFactory tf = new TwitterStreamFactory(cb.build());
        TwitterStream twitterStream = tf.getInstance();


        StatusListener listener = new StatusListener(){
            public void onStatus(Status status) {

                String link, title, content;
                Long tweetID;
                Timestamp timestamp;

                for (URLEntity url: status.getURLEntities()) {
                 link = url.getExpandedURL();
                 tweetID = status.getId();

                 try {
                     Document doc = Jsoup.connect(link).get();
                     content = doc.body().text();
                     title = doc.title();
                     timestamp = new Timestamp(System.currentTimeMillis());

                     InsertToTweeterFeeder(link, tweetID, title, content, timestamp);
                 }
                 catch (IOException ioe) {

                 }
                }
            }
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

            public void onScrubGeo(long l, long l1) {

            }

            public void onStallWarning(StallWarning stallWarning) {
            }

            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };

        twitterStream.addListener(listener);
        twitterStream.sample();
    }

    public static void InsertToTweeterFeeder(String link, Long tweedID, String title,
                                             String content, Timestamp timestamp) {
        String sql = "INSERT INTO TwitterFeeder(Link,TweetID,Title,Content,Timestamp) VALUES(?,?,?,?,?)";

        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, link);
            pstmt.setLong(2, tweedID);
            pstmt.setString(3, title);
            pstmt.setString(4, content);
            pstmt.setTimestamp(5, timestamp);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            //System.out.println("ERROR");
            e.printStackTrace();
        }
    }

    public static void connectToDB(String fileName) {
//        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:C:/sqlite/db/" + fileName + ".db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createNewDB(String fileName) {

        String url = "jdbc:sqlite:C:/sqlite/db/" + fileName;

        try {
            Connection conn = DriverManager.getConnection(url);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
