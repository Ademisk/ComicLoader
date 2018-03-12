package Utils;

import comicloader.WebService;
import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.UserAgent;
import java.util.Random;

public class Utils {
    
    public static void downloadRandomComic(String destPath, int pageIndex) {
        UserAgent userAgent = new UserAgent();
        String targetURL = "http://poorlydrawnlines.com/archive/";
        try {
            userAgent.visit(targetURL);

            Element wrapper = userAgent.doc.findEvery("<div class=wrapper>");
            Element page = wrapper.findFirst("<div>");
            Elements href = page.findEvery("<a>");
                    
            int chCount = href.size();
            Random rand = new Random();
            int chNum = rand.nextInt(chCount + 1);

            int counter = 0;
            for (Element row : href) {
                if (chNum == counter) {
                    String url = row.getAt("href");
                    userAgent.visit(url);
                    Element img = userAgent.doc.findFirst("<div class=post>").findFirst("<img>");
                    
                    String imgSrc = img.getAt("src");
                    WebService.downloadImage(imgSrc, destPath, pageIndex);
                }
                counter++;
            }
        } catch (Exception e) {
            System.out.println("Downloading Random Comic failed");
        }
    }
    
    public static void printHelpMessage() {
        System.out.println("Download a range of chapters: java -jar 'path/to/file.jar 'manga name' -d startChapterNum-endChapterNum 'manga source'");
        System.out.println("Download the latest x chapters: java -jar 'path/to/file.jar 'manga name' -d -xChapters 'manga source'");
        System.out.println("Download a specific chapter: java -jar 'path/to/file.jar 'manga name' -d chapterNum 'manga source'");
        System.out.println("Ex: \"java -jar path/to/file.js \"One Piece\" -d -20 \"kiss manga\"");
    }
}
