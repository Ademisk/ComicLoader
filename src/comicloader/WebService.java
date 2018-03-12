package comicloader;

import com.jaunt.UserAgent;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import org.apache.commons.validator.routines.UrlValidator;

public class WebService {
    public static int getResponseCode(String urlString) throws MalformedURLException, ProtocolException {
//        int resp = -10000;
//        URL u = new URL(urlString); 
//        HttpURLConnection huc = (HttpURLConnection)u.openConnection();
//        huc.setRequestMethod("GET"); 
//        huc.connect(); 
//        resp = huc.getResponseCode();
//        return resp;
        return 200;
    }
    
    private static String sanitizeUrl(String url) {
        String cleanUrl = url.replaceAll("this.src='([^']*)'", "$1");
        return cleanUrl;
    }
    
    public static void downloadImage(String src, String destFolder, Integer index) {
        //Validate url
        String cleanUrl = sanitizeUrl(src);
        UrlValidator urlValidator = new UrlValidator();
        if (!urlValidator.isValid(cleanUrl)) {
            return;
        }
        
        //Ping the url to test existence of file
        HttpURLConnection connection = null;
        try{            
            URL myurl = new URL(cleanUrl);
            connection = (HttpURLConnection) myurl.openConnection(); 
            //Set request to header to reduce load as Subirkumarsao said.       
            connection.setRequestMethod("HEAD");         
            int code = connection.getResponseCode();        
            //System.out.println("" + code); 
        } catch (Exception e) {
            //No file at url, log and return
            System.out.println("Page " + index + ": " + cleanUrl + " can't be reached. Skipping");
            return;
        }
        
        try {
            //URL website = new URL(cleanUrl);
            //Path dest = Paths.get(destFolder + "\\" + index + ".jpg");
            String dest = destFolder + "\\" + index + ".jpg";
            File file = new File(dest);
            
            UserAgent userAgent = new UserAgent();
            userAgent.download(cleanUrl, file);
        } catch (Exception e) {

            System.out.println("Error in downloadImage");
            System.out.println(e.toString());

            try {
                int input = System.in.read();
            } catch (IOException ex) {
                System.out.println("Error reading confimation input");
            }
        }
    }
}
