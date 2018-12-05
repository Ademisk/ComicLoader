import com.jaunt.*;
import com.jaunt.component.*;

public class Example13{
  public static void main(String[] args){
    try{
      UserAgent userAgent = new UserAgent(); 
      userAgent.visit("http://jaunt-api.com/examples/login.htm");
      userAgent.doc.filloutField("Username:", "tom");          //fill the field labelled 'Username:' with "tom"
      userAgent.doc.filloutField("Password:", "secret");       //fill the field labelled 'Password:' with "secret"
      userAgent.doc.chooseCheckBox("Remember me", Label.RIGHT);//choose the component right-labelled 'Remember me'
      userAgent.doc.submit();                                  //submit the form
      System.out.println(userAgent.getLocation());             //print the current location (url)
    }
    catch(JauntException e){
      System.err.println(e);
    }
  }
}