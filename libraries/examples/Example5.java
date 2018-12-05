import com.jaunt.*;
import com.jaunt.component.*;
import java.io.*;

public class Example5{
  public static void main(String[] args){
    try{
      UserAgent userAgent = new UserAgent();
      userAgent.open(new File("images.htm"));  //open the HTML (or XML) from a file 
    
      Element div = userAgent.doc.findFirst("<div class=images>");    //find first div who's class matches 'images' 
      System.out.println("div's outerHTML():\n" + div.outerHTML());   
      System.out.println("-------------");    
      System.out.println("div's innerHTML():\n" + div.innerHTML());
      System.out.println("-------------");  
      System.out.println("div's outerXML(2):\n" + div.outerXML(2));   //2 extra spaces used per indent
      System.out.println("-------------");
      System.out.println("div's innerXML(2):\n" + div.innerXML(2));   //2 extra spaces used per indent
      System.out.println("-------------");  
      //make some changes
      div.innerHTML("<h1>Presto!</h1>");          //replace div's content with different elements.
      System.out.println("Altered document as HTML:\n" + userAgent.doc.innerHTML());  //print the altered document.
    }
    catch(JauntException e){
      System.err.println(e);
    }
    catch(IOException e){
      System.err.println(e);
    }
  }
}