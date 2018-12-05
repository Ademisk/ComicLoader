import com.jaunt.*;
import com.jaunt.component.*;

public class Example11{
  public static void main(String[] args){
    try{ 
      UserAgent userAgent = new UserAgent(); 
      userAgent.visit("http://jaunt-api.com/examples/hello.htm");
    	 
      Elements elements = userAgent.doc.findEvery("<div|span>");//find every element who's tagname is div or span.
      System.out.println("results1:\n" + elements.innerHTML()); //print the search results
    	 
      elements = userAgent.doc.findEvery("<p id=1|4>");         //find every p element who's id is 1 or 4
      System.out.println("results2:\n" + elements.innerHTML()); //print the search results
    	 
      elements = userAgent.doc.findEvery("< id=[2-6]>");        //find every element (any name) with id from 2-6
      System.out.println("results3:\n" + elements.innerHTML()); //print the search results
    	 
      elements = userAgent.doc.findEvery("<p>ho");        //find every p who's joined child text contains 'ho' (regex)
      System.out.println("results4:\n" + elements.innerHTML()); //print the search results
    	 
      elements = userAgent.doc.findEvery("<p|div>^ho");   //find every p or div who's child text starts with 'ho'
      System.out.println("results5:\n" + elements.innerHTML()); //print the search result
    	  
      elements = userAgent.doc.findEvery("<p>^(hi|ahoj)");//find every p who's child text starts with 'hi' or 'ahoy'
      System.out.println("results6:\n" + elements.innerHTML()); //print the search result
    }
    catch(ResponseException e){                          
      System.out.println(e);
    }
  }
}