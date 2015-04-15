// package org.jsoup;

import java.io.*;
import java.net.*;
import java.util.regex.*;
import java.sql.*;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.Jsoup.*;
import org.jsoup.helper.Validate;
import org.jsoup.Connection.*;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class Crawler {
	Connection connection;
	int urlID;
	public Properties props;
	static PrintWriter writer;

	Crawler() {
		
		urlID = 0;
	}

	public void readProperties() throws IOException {
      	props = new Properties();
      	FileInputStream in = new FileInputStream("database.properties");
      	props.load(in);
      	in.close();
	}

	public void openConnection() throws SQLException, IOException {
		String drivers = props.getProperty("jdbc.drivers");
      	if (drivers != null) System.setProperty("jdbc.drivers", drivers);

      	String url = props.getProperty("jdbc.url");
      	String username = props.getProperty("jdbc.username");
      	String password = props.getProperty("jdbc.password");

		connection = DriverManager.getConnection( url, username, password);
   	}
	
	public void closeConnection() throws SQLException, IOException {
		connection.close();
	}

	public void createDB() throws SQLException, IOException {
		openConnection();

        Statement stat = connection.createStatement();
		
		// Delete the table first if any
		try {
			stat.executeUpdate("DROP TABLE URLS");
		}
		catch (Exception e) {
		}
		
		try {
			stat.executeUpdate("DROP TABLE WORDS2");
		}
		catch (Exception e) {
		}
		// try {
			// stat.executeUpdate("DROP TABLE WORDS");
		// }
		// catch (Exception e) {
		// }
			
		// Create the table
        stat.executeUpdate("CREATE TABLE URLS (urlid INT, url VARCHAR(512), description VARCHAR(200), img VARCHAR(200))");
        stat.executeUpdate("CREATE TABLE WORDS2 (word VARCHAR(100), urlid INT)");
        // stat.executeUpdate("CREATE TABLE WORDS (word VARCHAR(100), urlid INT, primary key(word,urlid))");
	}

	public boolean urlInDB(String urlFound) throws SQLException, IOException {
	// public boolean urlInDB(String urlFound) {
        Statement stat = connection.createStatement();
		ResultSet result = stat.executeQuery( "SELECT * FROM urls WHERE url LIKE '"+urlFound+"'");

		if (result.next()) {
	       	// System.out.println("URL "+urlFound+" already in DB");
			result.close();
			return true;
		}
	       // System.out.println("URL "+urlFound+" not yet in DB");
		result.close();   
		return false;
	}

	public void insertURLInDB( String url, String description, String image) throws SQLException, IOException {
	// public void insertURLInDB( String url, String description, String image) {
        Statement stat = connection.createStatement();
		String query = "INSERT INTO urls VALUES ('"+urlID+"','"+url+"','"+description+"','"+image+"')";
		//System.out.println("Executing "+query);
		stat.executeUpdate( query );
		urlID++;
	}
	
	public String getDescription( StringBuilder input) {
		String patternString0 =  "<h1>([^<>]*)</h1>";
    	Pattern pattern0 = Pattern.compile(patternString0,Pattern.CASE_INSENSITIVE);
    	Matcher matcher0 = pattern0.matcher(input);
		
		String patternString1 =  "<h2>([^<>]*)</h2>";
    	Pattern pattern1 = Pattern.compile(patternString1,Pattern.CASE_INSENSITIVE);
    	Matcher matcher1 = pattern1.matcher(input);
		
		String patternString2 =  "<title>([^<>]*)</title>";
    	Pattern pattern2 = Pattern.compile(patternString2,Pattern.CASE_INSENSITIVE);
    	Matcher matcher2 = pattern2.matcher(input);
		
		String patternString3 =  "<p>([^<>]*)</p>";
    	Pattern pattern3 = Pattern.compile(patternString3,Pattern.CASE_INSENSITIVE);
    	Matcher matcher3 = pattern3.matcher(input);
		
		String des = null;
		if (matcher0.find()) {
    		des = matcher0.group(1);
						
 		} else if (matcher1.find()) {
    		des = matcher1.group(1);
						
 		} else if (matcher2.find()) {
    		des = matcher2.group(1);
						
 		} else if (matcher3.find()) {
    		des = matcher3.group(1);
						
 		}
				
		return des;
	}
	
	public void getWords( StringBuilder input) throws SQLException, IOException  {
		try {
			String patternString =  "<[^>]*>([^<>]*)</[^>]*>";
			// String patternString =  "<[A-Za-z0-9]+>(.*)</[A-Za-z0-9]+>";
    		Pattern pattern = Pattern.compile(patternString,Pattern.CASE_INSENSITIVE);
    		Matcher matcher = pattern.matcher(input);
			
			boolean added = false;
			String query = "INSERT INTO words2 VALUES ";
			
			while(matcher.find()) {
				int start = matcher.start();
    			int end = matcher.end();
    			String match = input.substring(start, end);
				String words = matcher.group(1);
				// System.out.println(match);
				// System.out.println(words);
				// String[] wrds = input.toString().split("[^A-Za-z0-9]+");
				String[] wrds = words.split("[^A-Za-z0-9]+");
				if (wrds.length < 1) continue;
								
				for (int i = 0; i < wrds.length; i++) {
								
					if (wrds[i].length() > 0) {
						// System.out.println("Word: " + wrds[i]);	
						if (added == true) query += ",";
						query += "('"+wrds[i]+"','"+urlID+"')";
						added = true;	
						// stat.addBatch(query);
							
					}
						
				}
				
				
			}
			query += ";";
			
			if (added == true) {
				// System.out.println(query);
				Statement stat = connection.createStatement();		
				// stat.executeBatch();
				stat.executeUpdate(query);
				stat.close();
			// } else {
				// stat.close();
			}		
			
		} catch (SQLException s) {
			
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public String getImg (StringBuilder input, String host) {
		String img = null;
		String patternString =  "<img[^<>]+src\\s*=\\s*\"[^\"]*\"[^>]*>";
    	Pattern pattern = Pattern.compile(patternString,Pattern.CASE_INSENSITIVE);
    	Matcher matcher = pattern.matcher(input);
		while (matcher.find()) {
    		int start = matcher.start();
    		int end = matcher.end();
    		String match = input.substring(start, end);
			// String urlFound = matcher.group(1);
			
			Document doc = Jsoup.parse(match, host);
			Element image = doc.select("img").first();
				
			img = image.attr("abs:src");
			if (!img.contains("logo") && !img.contains("brand")) break;
		}
		return img;
	}

	public void addPrimKey() throws SQLException, IOException {
		Statement stat = connection.createStatement();
		stat.execute("ALTER TABLE urls ADD PRIMARY KEY (urlid)");
		// stat.execute("ALTER TABLE words ADD primary key (word,urlid)");
		try {
			stat.executeUpdate("DROP TABLE WORDS");
		}
		catch (Exception e) {
		}
		stat.executeUpdate("CREATE TABLE WORDS (word VARCHAR(100), urlid INT)");
		stat.execute("insert into words select distinct * from WORDS2");
		stat.executeUpdate("DROP TABLE WORDS2");
	}
	

   	public void fetchURL(String urlScanned) {
		try {
			
			URL urlconn = new URL(urlScanned);
			
    			// open reader for URL
    		// InputStreamReader in = new InputStreamReader(urlconn.getInputStream());
    		InputStreamReader in = new InputStreamReader(urlconn.openStream());
				
    			// read contents into string builder
    		StringBuilder input = new StringBuilder();
    		int ch;
			while ((ch = in.read()) != -1) {
				// System.out.println(ch);
         		input.append((char) ch);
			}
			in.close();

			System.out.printf("%s, id=%d\n",urlScanned,urlID);
	
			String img = getImg(input, urlScanned);
			// System.out.println("Img: "+ img);	
			
			if (img == null) img = "";
			
			
			String description = getDescription(input);
			// System.out.println("Des: "+description);
			
			// if (description == null) return;
			
			getWords(input);
						
			StringBuilder des;
			if (description == null) {
				description = "";
				insertURLInDB(urlScanned,description,img);
			} else {
				des = new StringBuilder();	
				String[] wrds = description.split("[^A-Za-z0-9]+");
				int wlen = 0;	
				for (int i = 0; i < wrds.length; i++) {
					// System.out.println("Word: "+ wrds[i]);
					if (wlen >= 100) break;
					des.append(wrds[i]);
					des.append(" ");
					wlen += wrds[i].length();
					wlen += 1;
				}
				insertURLInDB(urlScanned,des.toString(),img);
			}
						
			String patternString =  "<a\\s+href\\s*=\\s*(\"[^\"]*\"|[^\\s>]*)\\s*>";
    		Pattern pattern = Pattern.compile(patternString,Pattern.CASE_INSENSITIVE);
    		Matcher matcher = pattern.matcher(input);
								
			while (matcher.find()) {
    			int start = matcher.start();
    			int end = matcher.end();
    			String match = input.substring(start, end);
				String rel = matcher.group(1);
				
				Document doc = Jsoup.parse(match, urlScanned);
				Element link = doc.select("a[href]").first();
				String absUrl = link.attr("abs:href");
				String relUrl = link.attr("href");
				// System.out.println(absUrl);
								
				// if (!absUrl.contains(".cs.purdue.edu") || !checkUrl(absUrl) || absUrl.contains("../")
				if (!absUrl.contains(".cs.purdue.edu") || absUrl.contains("dbseclab") || absUrl.contains("=")
					|| absUrl.contains("#") || absUrl.contains("&")) continue;
												
				URL urlFound = new URL(absUrl);
				
				HttpURLConnection conn = (HttpURLConnection)  urlFound.openConnection();
				// conn.setConnectTimeout(10 * 1000);
				conn.setRequestMethod("HEAD");
				conn.setInstanceFollowRedirects(false);
												
				conn.connect();
				int responseCode = conn.getResponseCode();
				
				if (responseCode > 399) {
					continue;
				} else if (responseCode > 299) {
					absUrl = conn.getHeaderField("Location");
					
				}
				
				String contentType = conn.getContentType();
				// System.out.println("Type: "+contentType);
				
				// if (absUrl.contains(".cs.purdue.edu") && !urlInDB(absUrl)) {
				if (contentType != null && contentType.contains("html") && !urlInDB(absUrl) ) {
					// System.out.println("BaseUrl: "+ urlScanned);
					
					// System.out.println("RelURLFound "+relUrl);
					// System.out.println("AbsURLFound "+absUrl);
					// System.out.println();
					writer.println("BaseUrl: "+ urlScanned);
					// writer.println("OrgRel: "+ rel);
					writer.println("FoundRel: "+ relUrl);
					writer.println("AbsURL: "+ absUrl);
					writer.println();
					// insertURLInDB(absUrl,description);
					fetchURL(absUrl);
					
				}
				// conn.disconnect();	
				
 			}

		} catch (SQLException s) {
			
		} catch (IOException io) {
			// System.out.println("IO error");
			// return;	
		} catch (Exception e) {
       		e.printStackTrace();
      	}
	}
	
	public static void main(String[] args) {
		Crawler crawler = new Crawler();

		try {
			writer = new PrintWriter("output.txt", "UTF-8");
			crawler.readProperties();
			String root = crawler.props.getProperty("crawler.root");
			crawler.createDB();
			crawler.fetchURL(root);
			crawler.addPrimKey();
			crawler.closeConnection();
			writer.close();
		}
		catch( Exception e) {
         	e.printStackTrace();
		}
		System.out.println("Crawl complete!");
    }
}

