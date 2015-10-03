import com.jaunt.*;

public class Example2{

  public static void main(String[] args){
    try{
      UserAgent userAgent = new UserAgent();         //create new userAgent (headless browser)
      System.out.println("SETTINGS:\n" + userAgent.settings);      //print the userAgent's default settings.
      userAgent.settings.autoSaveJSON = true;        //change settings to autosave last visited page.
   
      userAgent.sendGET("http://api.tumblr.com/v2/blog/puppygifs.tumblr.com/posts/");   //send request
      JNode title = userAgent.json.findFirst("title");
      System.out.println("title: " + title);	

      JNode meta = userAgent.json.findFirst("meta");
      System.out.println("meta:" + meta);      
    }
    catch(JauntException e){                         //if an HTTP/connection error occurs, handle JauntException.
      System.err.println(e);
    }
  }
}