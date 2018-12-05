import com.jaunt.*;
import com.jaunt.component.*;
import java.io.*;

public class Example3{
  public static void main(String[] args){
    try{
      UserAgent userAgent = new UserAgent();                       
                                                                    //open HTML from a String.
      userAgent.openContent("<html><body>WebPage <div>Hobbies:<p>beer<p>skiing</div> Copyright 2013</body></html>");
      Element body = userAgent.doc.findFirst("<body>");
      Element div = body.findFirst("<div>");
   
      System.out.println("body's child text: " + body.getChildText()); //join child text of body element
      System.out.println("-----------");
      System.out.println("all body's text: " + body.getTextContent()); //join all text within body element
      System.out.println("-----------");
      System.out.println("div's child text: " + div.getChildText());   //join child of div element
      System.out.println("-----------");
      System.out.println("all div's text: " + div.getTextContent());   //join all text within the div element 
    }
    catch(JauntException e){
      System.err.println(e);
    }
  }
}