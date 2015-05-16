package Cloud.Tweets;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.amazonaws.services.sqs.AmazonSQS;
import com.google.gson.Gson;

/**
 * <p>This is a code example of Twitter4J Streaming API - sample method support.<br>
 * Usage: java twitter4j.examples.PrintSampleStream<br>
 * </p>
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class TweetGet {
	/**
	 * Main entry of this application.
	 *
	 * @param args
	 */

	static SimpleQueueServiceSample sqs=new SimpleQueueServiceSample() ;
	static  AmazonSQS amz;

	private final Connection connection;


	public TweetGet() throws SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
		}
		System.out.println("MySQL JDBC Driver Registered!");

		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/project1","root", "Garima@123");

		if (connection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
		}
	}

	public Connection getConnection(){
		return connection;
	}

	public void closeConnection(){
		if(connection != null){
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) throws TwitterException, SQLException {

		final TweetGet tweetGet = new TweetGet();
		amz = sqs.createSQSs();


		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey("0051bD7xTxDrRlcunih5HmziH")
		.setOAuthConsumerSecret("XGBitJq6Wh8dhkxdYeyRnF6hi1cyszNMuTJQmsfgiznq7WAmVU")
		.setOAuthAccessToken("549807950-lNSdnbkHEUWYZGA467ffXPX9olQ57ch0jngYnXYo")
		.setOAuthAccessTokenSecret("R0M4GE7qH9AxJp1BVHzRS16fhY6oQrmOzArR31iATWfc4");


		TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
		StatusListener listener = new StatusListener() {
			public void onStatus(Status status){
				Statement statement = null;
				Gson gson = new Gson();

				//amz.getQueueAttributes();


				try
				{

					if(status.getGeoLocation()!=null )
					{ 
						MyStatus myStatus = new MyStatus(status.getId(), status.getText());
						String insertSqlStatement = "INSERT INTO Tweets"
								+ "(TweetID,Text,Latitude,Longitude,HashTags) " + "VALUES"
								+ "('"+status.getId()+"','"+status.getText()+"','"+status.getGeoLocation().getLatitude()+"','"+status.getGeoLocation().getLongitude()+"','"+ ((status.getHashtagEntities().length>0)?status.getHashtagEntities()[0].getText():null)+"')";
						statement = tweetGet.getConnection().createStatement();
						System.out.println(insertSqlStatement);
						int x = statement.executeUpdate(insertSqlStatement);
						System.out.println("Record is inserted into Tweets table!");
						System.out.println(x);
						String statusJson = gson.toJson(myStatus); 
						sqs.addmessage(amz,statusJson);



					}
				}
				catch(SQLException e)
				{
					System.out.println(e.getMessage());
				}finally{
					if(statement != null){
						try {
							statement.close();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				//System.out.println("@" + status.getUser().getScreenName() +"-"+ status.getPlace().getCountry() + " - " + status.getText() + "-"+status.getId());
			}

			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				// System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
			}

			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				// System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
			}

			public void onScrubGeo(long userId, long upToStatusId) {
				// System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
			}

			public void onStallWarning(StallWarning warning) {
				// System.out.println("Got stall warning:" + warning);
			}

			public void onException(Exception ex) {
				ex.printStackTrace();
			}

		};
		twitterStream.addListener(listener);
		twitterStream.sample();


	}
}