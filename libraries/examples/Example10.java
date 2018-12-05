import com.jaunt.*;
import com.jaunt.component.*;
import java.util.List;

public class Example10{
  public static void main(String[] args){
    try{ 
      UserAgent userAgent = new UserAgent();
      userAgent.visit("http://jaunt-api.com/examples/food.htm");
    	   
      Element body = userAgent.doc.findFirst("<body>");                    //find body element
      Element element = body.getElement(2);                                //retrieve 3rd child element within the body.     
      System.out.println("result1: " + element);                           //print the element
    	    
      String text = body.getElement(3).getElement(0).getChildText();       //get text of 1st child of 4th child of body.
      System.out.println("result2: " + text);                              //print the text
    	    
      element = body.findFirst("<div class=meat>").getElement(1);          //retrieve 2nd child element of div
      System.out.println("result3: " + element.outerHTML());               //print the element and its content
    	    
      Elements elements = body.getEach("<div>");                           //get body's child divs
      System.out.println("result4 has " + elements.size() + " divs:\n");   //print the search results
      System.out.println(elements.innerHTML(2));   
    }
    catch(JauntException e){                          
      System.out.println(e);
    }    
  }
}