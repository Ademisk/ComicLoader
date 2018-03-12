package comicloader;

import Utils.Utils;
import com.jaunt.Cookie;
import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.UserAgent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author Max
 */
public class Leftovers {
//    private static Boolean downloadAndZipFromMangaOnline(String mangaName, int start, int end, HashMap<Integer, Boolean> chMap, String destFolder) {
//        Boolean downloadSuccess = true;
//        Boolean zipSuccess = true;
//        String specialChars = "[<>\"\\/\\|?*.]";
//        Boolean chapterDownloaded = false;
//        
//        int rowIndex = 0;
//        String html = "";
//        try {
//            UserAgent userAgent = new UserAgent();
//            String targetURL = MANGA_ONLINE_SOURCE + "/search.html?keyword=" + mangaName.replace(" ", "+");
//            userAgent.visit(targetURL);
//            Elements results = userAgent.doc.findFirst("<div class=popular-body>").findEvery("<li>");
//            
//            String mangaUrl = "";
//            for (Element result : results) {
//                if (result.findFirst("<a>").findFirst("<img>").getAt("alt").trim().equals(mangaName)) {
//                    mangaUrl = result.findFirst("<a>").getAt("href");
//                    break;
//                }
//            }
//            
//            userAgent.visit(mangaUrl);
//            Elements rows = userAgent.doc.findEvery("<div class=list-chapter>").findEvery("<li>");
//            
//            //If 'last n' download mode, get the last chapter number and calculate the chapter range
//            if (Integer.MAX_VALUE == end) {
//                List<Element> rws = rows.toList();
//                String lastChNumStr = rws.get(0).findFirst("<a>").innerHTML().replaceAll("<span>.*</span>", "");
//                int lastChNum = Integer.parseInt(lastChNumStr.replaceAll("([Vv]ol[. ]*[\\d]{1,3}|[^\\d])*([\\d.]{1,5}).*\\s*", "$2"));
//                
//                start = lastChNum - (end - start) + 1;
//                end = lastChNum;
//            }
//            
//            //Manga list of chapters
//            for (Element row : rows) {
//                html = row.findFirst("<a>").innerHTML();
//                
//                //This may not be needed since the chNumber is specifically checked. Outsider chapters shouldn't get past the check
////                if (html.indexOf(mangaName) < 0)
////                    continue;
//                html = html.replaceAll("<span>.*</span>", "").trim();
//                String chNumberStr = html.replaceAll("([Vv]ol[. ]*[\\d]{1,3}|[^\\d])*([\\d.]{1,5}).*", "$2");
//                int chNumber = Integer.parseInt(chNumberStr.contains(".") ? chNumberStr.split("\\.")[0] : chNumberStr);
//                
//                if (chNumber < start) {
//                    break;
//                }
//                else if (start <= chNumber && chNumber <= end && !chMap.containsKey(chNumber) && zipSuccess) {
//                    chapterDownloaded = true;
//                    System.out.println("\nDownloading chapter " + chNumberStr);
//                    String baseUrl = row.findFirst("<a>").getAt("href");
//                    String chName = html.replaceAll("([Vv]ol[. ]*[\\d]{1,3}|[^\\d])*([\\d.]{1,5})[ :]*(.*)", "$3")
//                            .replaceAll("[" + SPECIAL_FOLDER_CHARACTERS + "]", "").trim();
//
//                    String destPath = destFolder + "\\" + mangaName + " " + chNumberStr;
//                    destPath = (destPath + (!chName.isEmpty() ? " - " + chName : "")).trim();
//                    new File(destPath).mkdir();
//
//                    //Go to the page with all of the images
//                    userAgent.visit(baseUrl);
//                    Element imgContainer = null;
//                    imgContainer = userAgent.doc.findFirst("<div class=list-img>");
//                        
//                    Elements pages = imgContainer.findEvery("<img>");
//                    int i = 0;
//                    for (Element page : pages) {
//                        String imgSrc = "";
//                        if (page.hasAttribute("onerror"))
//                            imgSrc = page.getAt("onerror");
//                        else
//                            imgSrc = page.getAt("src");
//                        if (imgSrc.indexOf("googleusercontent") > 0) {  //filter out google proxy and decode url
//                            imgSrc = imgSrc.replaceAll(".*url=(.*)", "$1");
//                            imgSrc = java.net.URLDecoder.decode(imgSrc);
//                        }
//                        WebService.downloadImage(imgSrc, destPath, i);
//                        i++;
//                    }
//
//                    if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
//                        destPath.replace("/", "\\");
//                    }
//
//                    //Downloads a random Poorly Drawn Lines comic for fun
//                    Utils.downloadRandomComic(destPath, i);
//
//                    //Compress into default format
//                    String cmpFile = destPath + "." + CBR;
//                    zipSuccess = StorageService.zipFolder(destPath, cmpFile);
//                }
//                
//                rowIndex++;
//            }
//        } catch (Exception e) {
//            downloadSuccess = false;
//            System.out.println("Error in downloadAndZipFromMyMangaOnline");
//            System.out.println(e);
//            
//            try {
//                int input = System.in.read();
//            } catch (IOException ex) {
//                System.out.println("Error reading confimation input");
//            }
//        }
//        
//        if (!chapterDownloaded) {
//            System.out.println("No chapters to download.");
//        }
//        
//        return downloadSuccess && zipSuccess;
//    }
//    
//    private static Boolean downloadAndZipFromKissManga(String mangaName, int start, int end, HashMap<Integer, Boolean> chMap, String destFolder) {
//        Boolean downloadSuccess = true;
//        Boolean zipSuccess = true;
//        String specialChars = "[<>\"\\/\\|?*.]";
//        Boolean chapterDownloaded = false;
//        
//        Cookie ck = null;
//        try {
//            ck = new Cookie("http://www.kissmanga.com", "cf_clearance=0b9cc50c58146bd764b4469a84d4c6f5d5be8ae9-1447568434-86400");
//        } catch (Exception e) {
//            System.out.println("error");
//        }
//        
//        int rowIndex = 0;
//        String html = "";
//        try {
//            UserAgent userAgent = new UserAgent();
//            userAgent.cookies.addCookie(ck);
//            String targetURL = "https://kissmanga.com/Manga/One-Piece";
//            userAgent.visit(targetURL);
//            
//            Elements rows = userAgent.doc.findFirst("<table class=listing>").findEvery("<tr>");
//            
//            //If 'last n' download mode, get the last chapter number and calculate the range
//            if (Integer.MAX_VALUE == end) {
//                int chCount = rows.size();
//                List<Element> rws = rows.toList();
//                int lastChNum = Integer.parseInt(rws.get(2).findFirst("<td>").innerHTML().replaceAll("\\s.+\\s+" + mangaName + " ([0-9]+).*</a>", "$1").trim());
//                
//                start = lastChNum - (end - start) + 1;
//                end = lastChNum;
//            }
//            
//            
//            int chCount = rows.size();
//            
//            if (Integer.MAX_VALUE == end) {
//                start = chCount - (end - start) + 1;
//                end = chCount;
//            }
//            
//            //Page with all chapters
//            for (Element row : rows) {
//                if (0 != rowIndex && 1 != rowIndex) {   //1st row is header tr, second row is a styling tr
//                    html = StringEscapeUtils.unescapeHtml3(row.findFirst("<td>").innerHTML());
//                    String num = html.replaceAll("\\s.+\\s+" + mangaName + " ([0-9]+).*</a>", "$1").trim();
//                    if (html.contains("Vol.")) {    //Skip all Volumes
//                        continue;
//                    }
//                    int chNumber = Integer.parseInt(num);
//
//                    if (start <= chNumber && chNumber <= end && !chMap.containsKey(chNumber) && zipSuccess) {
//                        chapterDownloaded = true;
//                        System.out.println("\nDownloading chapter " + chNumber);
//                        String baseUrl = row.findFirst("<td>").findFirst("<a>").getAt("href");
//                        String chName = " " + html.replaceAll("(?s)\\s.+\\s" + mangaName + " [0-9]+(.*)</a>", "$1").replaceAll(":", "-").replaceAll(specialChars, "").trim();
//
//                        String destPath = (destFolder + "\\" + mangaName + " " + chNumber + chName).trim();
//                        new File(destPath).mkdir();
//
//                        //Go to the page with all of the images
//                        userAgent.visit(baseUrl);
//                        Elements scripts = userAgent.doc.findEvery("<script type=\"text/javascript\">var lstImages");
//                        String script = scripts.getChildElements().get(0).innerHTML();
//                        
//                        Pattern p = Pattern.compile("https*://.+\\d+", Pattern.MULTILINE);
//                        Matcher m = p.matcher(script);
//                        StringBuffer sb = new StringBuffer(script.length());
//                        while (m.find()) {
//                            String text = m.group(0);
//                            sb.append(text + ";");
//                        }
//                        
//                        String[] imgSources = sb.toString().split(";");
//                        int i = 0;
//                        for (i = 0; i < imgSources.length; i++) {
//                            WebService.downloadImage(imgSources[i], destPath, i);
//                        }
//                        
//                        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
//                            destPath.replace("/", "\\");
//                        }
//                        
//                        Utils.downloadRandomComic(destPath, i);
//
//                        //Compress into default format
//                        String cmpFile = destPath + "." + CBR;
//                        zipSuccess = StorageService.zipFolder(destPath, cmpFile);
//                    }
//                }
//                
//                rowIndex++;
//            }
//        } catch (Exception e) {
//            downloadSuccess = false;
//            System.out.println("Error in downloadAndZipFromKissManga");
//            System.out.println(e);
//            
//            try {
//                int input = System.in.read();
//            } catch (IOException ex) {
//                System.out.println("Error reading confimation input");
//            }
//        }
//        
//        if (!chapterDownloaded) {
//            System.out.println("No chapters to download.");
//        }
//        
//        return downloadSuccess && zipSuccess;
//    }
//    
//    private static Boolean downloadAndZipFromMangaPanda(String mangaName, int start, int end, HashMap<Integer, Boolean> chMap, String destFolder) {
//        Boolean downloadSuccess = true;
//        Boolean zipSuccess = true;
//        String specialChars = "[<>\"\\/\\|?*.]";
//        
//        try {
//            UserAgent userAgent = new UserAgent();
//            String targetURL = MANGAPANDA_SOURCE + "/actions/search/?q=" + mangaName.replaceAll("\\s", "+") + "&limit=100";
//            userAgent.visit(targetURL);
//            
//            String body = userAgent.doc.innerHTML().toString();
//            String []results = body.split("\\|");
//            String subPath = results[4];
//            
//            userAgent.visit(MANGAPANDA_SOURCE + subPath);
//            
//            Elements rows = userAgent.doc.findFirst("<table id=listing>").findEvery("<tr class=>");
//            
//            //If 'last n' download mode, get the last chapter number and calculate the range
//            if (Integer.MAX_VALUE == end) {
//                int chCount = rows.size();
//                List<Element> rws = rows.toList();
//                int lastChNum = Integer.parseInt(rws.get(chCount - 1).findFirst("<td>").innerHTML().replaceAll("\\s.+\\s.+" + mangaName + " ([0-9]+)</a>.+", "$1").trim());
//                
//                start = lastChNum - (end - start) + 1;
//                end = lastChNum;
//            }
//            
//            //Page with all chapters
//            for (Element row : rows) {
//                String html = row.findFirst("<td>").innerHTML();
//                int chNumber = Integer.parseInt(html.replaceAll("\\s.+\\s.+" + mangaName + " ([0-9]+)</a>.+", "$1").trim());
//                
//                if (start <= chNumber && chNumber <= end && !chMap.containsKey(chNumber) && zipSuccess) {
//                    System.out.println("\nDownloading chapter " + chNumber);
//                    String baseUrl = row.findFirst("<td>").findFirst("<a>").getAt("href");
//                    String chName = html.replaceAll("\\s.+\\s.+(" + mangaName + ".+)</a>(.+)", "$1$2").replaceAll(":", "-").replaceAll(specialChars, "").trim();
//                    
//                    if (chName.endsWith("-"))
//                        chName = chName.substring(0, chName.length() - 2);
//
//                    String destPath = destFolder + "\\" + chName;
//                    new File(destPath).mkdir();
//
//                    userAgent.visit(baseUrl);
//
//                    //Find all Pages, and loop through to download each one
//                    Elements chapterPages = userAgent.doc.findFirst("<div id=selectpage>").findFirst("<select>").findEvery("<option>");
//                    int pageIndex = 0;
//                    for (Element pages : chapterPages) {
//                        System.out.print(".");
//                        pageIndex++;
//                        String pageUrl = MANGAPANDA_SOURCE + pages.getAt("value");
//                        userAgent.visit(pageUrl);
//
//                        String imgSrc = userAgent.doc.findFirst("<div id=imgholder>").findFirst("<a>").findFirst("<img>").getAt("src");
//                        WebService.downloadImage(imgSrc, destPath, pageIndex);
//                    }
//
//                    //Compress into default format
//                    String cmpFile = destPath + "." + CBR;
//                    zipSuccess = StorageService.zipFolder(destPath, cmpFile);
//                }
//            }
//        } catch (Exception e) {
//            downloadSuccess = false;
//            System.out.println("Error in downloadAndZipFromMangaPanda");
//            System.out.println(e);
//            
//            try {
//                int input = System.in.read();
//            } catch (IOException ex) {
//                System.out.println("Error reading confimation input");
//            }
//        }
//        
//        return downloadSuccess && zipSuccess;
//    }
}
