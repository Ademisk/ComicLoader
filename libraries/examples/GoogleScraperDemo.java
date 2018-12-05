import com.jaunt.*;

//Jaunt demo: searches for 'butterflies' at Google and prints urls of search results 

public class GoogleScraperDemo{
  public static void main(String[] args) throws JauntException{    
    UserAgent userAgent = new UserAgent();        //create new userAgent (headless browser)
    userAgent.visit("http://google.com");         //visit google
    userAgent.doc.apply("butterflies").submit();  //apply 'butterflies' to search field, then submit form
    
    Elements links = userAgent.doc.findEvery("<h3>").findEvery("<a>");   //find search result links 
    for(Element link : links) System.out.println(link.getAt("href"));     //print results
  }
}