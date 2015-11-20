import com.jaunt.*;

public class Example1{

  public static void main(String[] args){
    try{
      UserAgent userAgent = new UserAgent();         //create new userAgent (headless browser).
      userAgent.sendGET("http://api.tumblr.com/v2/blog/puppygifs.tumblr.com/posts/");   //send request
      System.out.println(userAgent.json);	         //print the retrieved JSON object
      System.out.println("Other response data:\n" + userAgent.response);   //response metadata, including headers
    }
    catch(JauntException e){         //if an HTTP/connection error occurs, handle JauntException.
      System.err.println(e);
    }
  }
}