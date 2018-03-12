package comicloader;

import com.jaunt.UserAgent;
import java.io.IOException;

public class ComicLoaderTests {
//    public static void testRun(String mangaName) {
//        try {
//            UserAgent userAgent = new UserAgent();
//            String targetURL = "http://www.mangapanda.com/actions/search/?q=" + mangaName.replaceAll("\\s", "+") + "&limit=100";
//            userAgent.visit(targetURL);
//            
//            String body = userAgent.doc.innerHTML().toString();
//            String []results = body.split("\\|");
//            String subPath = results[4];
//            
//            userAgent.visit(MANGAPANDA_SOURCE + subPath);
//            
//        } catch (Exception e) {
//            System.out.println("Error in testRun");            
//            System.out.println(e.toString());
//            
//            try {
//                int input = System.in.read();
//            } catch (IOException ex) {
//                System.out.println("Error reading confimation input");
//            }
//        }
//    }
}