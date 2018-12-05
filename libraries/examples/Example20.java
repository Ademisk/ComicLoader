import com.jaunt.*;
import com.jaunt.component.*;

public class Example20 {
  public static void main(String[] args){
    try{
      UserAgent userAgent = new UserAgent();   
      userAgent.visit("https://google.com");                 //visit google.com
      userAgent.doc.apply("seashells").submit();             //apply search term and submit form
		 
      Hyperlink nextPageLink = userAgent.doc.nextPageLink(); //extract url to next page of results
      nextPageLink.follow();                                 //visit next page (p 2).
      System.out.println("location: " + userAgent.getLocation());  //print current location (url)
    } 
    catch(JauntException e){
      System.err.println(e);
    } 
  }
}
