package comicloader;

import java.util.Random;
import com.jaunt.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
        
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.validator.routines.UrlValidator;

public class ComicLoader {
    //Commands
    final static String DO_LIST_CHAPTERS = "-c";
    final static String DO_CHAPTER_DOWNLOAD_SPECIFIC = "-Specific";
    final static String DO_CHAPTER_DOWNLOAD_RANGE = "-Range";
    final static String DO_CHAPTER_DOWNLOAD_LAST = "-Last";
    final static String DO_HELP = "-h";
    
    //Default Storage
    final static String DEFAULT_FOLDER = "C:\\Users\\" + System.getProperty("user.name") + "\\Documents\\Manga\\";
    final static String DROPBOX_WINDOWS_FOLDER = "C:\\Users\\" + System.getProperty("user.name") + "\\Dropbox\\Manga\\";
    final static String DROPBOX_MAC_FOLDER = "Users\\" + System.getProperty("user.name") + "\\Dropbox\\Manga\\";
    
    //Compression formats
    final static String CBR = "cbr";
    
    //Manga Providers
    final static String KISS_MANGA = "kiss manga";          //Blocks mini browsers, validates by cookie (can't spoof)
    final static String MANGA_PANDA = "manga panda";
    final static String MANGA_TOWN = "manga town";          //For some reason dead
    final static String MY_MANGA_ONLINE = "manga online";   //Gone
    final static String MANGANELO = "manganelo";
    
    //Source sites
    final static String MANGATOWN_SOURCE = "temp";
    final static String MANGAPANDA_SOURCE = "http://www.mangapanda.com";
    final static String KISSMANGA_SOURCE = "http://kissmanga.com";
    final static String MANGA_ONLINE_SOURCE = "http://mangaonline.to";
    final static String MANGA_READER_SOURCE = "http://www.mangareader.net";
    final static String MANGANELO_SOURCE = "http://manganelo.com";
    //final static String MANGA_SOURCE = MANGANELO_SOURCE;
    
    final static String SPECIAL_FOLDER_CHARACTERS = "\\/:*?\"<>|";
    
    static String targetLocalFolder;
    static String targetDropboxFolder;
    
    //Parameters
    //args[0] - "Manga Name": "Bleach", "One Piece"
    //args[1] - Action to take. '-c' (chapter list) or '-[command]' (download chapters)
    //args[2] - Chapter numbers. Specific: '-specific n', Range: '-range 1-100', Last n: '-last n'
    //args[3] - "Source Site": "KISSMANGA", "MANGAPANDA", "MANGA ONLINE"
    public static void main(String[] args) {        
        mainController(args);
    }
    
    public static void testRun(String mangaName) {
        try {
            UserAgent userAgent = new UserAgent();
            String targetURL = "http://www.mangapanda.com/actions/search/?q=" + mangaName.replaceAll("\\s", "+") + "&limit=100";
            userAgent.visit(targetURL);
            
            String body = userAgent.doc.innerHTML().toString();
            String []results = body.split("\\|");
            String subPath = results[4];
            
            userAgent.visit(MANGAPANDA_SOURCE + subPath);
            
        } catch (Exception e) {
            System.out.println("Error in testRun");            
            System.out.println(e.toString());
            
            try {
                int input = System.in.read();
            } catch (IOException ex) {
                System.out.println("Error reading confimation input");
            }
        }
    }
    
    public static void mainController(String[] args) {
        System.out.println("Welcome!\n");
        
        String source = args[3];
        
        if (3 <= args.length && DO_LIST_CHAPTERS.equals(args[1])) {
            //List the latest n chapters
            if (args[2].matches("-\\d+")) {
                fetchChapterList(Integer.parseInt(args[2]));
            }
        } else if (3 <= args.length) { //&& DO_CHAPTER_DOWNLOAD.equals(args[1])) {
            //Download the chapters specified
            String mangaName = args[0];
            String targetLocalFolder;
            String systemOS = System.getProperty("os.name");
            
            if (!systemOS.toLowerCase().contains("windows")) {
                targetLocalFolder = ".\\Manga\\" + mangaName;
                targetDropboxFolder = DROPBOX_MAC_FOLDER + mangaName;
            } else {
                targetLocalFolder = DEFAULT_FOLDER + mangaName;
                targetDropboxFolder = DROPBOX_WINDOWS_FOLDER + mangaName;
            }
            Boolean downloadSuccess = false;
            Boolean moveSuccess = false;
            
            HashMap<Integer, Boolean> chMap = new HashMap<Integer, Boolean>();
            generateChapterList(targetLocalFolder, mangaName, chMap);
            
            int start = 1;
            int end = 1;

            if (DO_CHAPTER_DOWNLOAD_LAST.equals(args[1])) {                 //Get latest n chapters
                start = Integer.MAX_VALUE - Integer.parseInt(args[2].replaceAll("-", ""));
                end = Integer.MAX_VALUE;
            } else if (DO_CHAPTER_DOWNLOAD_RANGE.equals(args[1])) {      //Get capters n to m
                start = Integer.parseInt(args[2].replaceAll("-\\d+", ""));
                end = Integer.parseInt(args[2].replaceAll("\\d+-", ""));
            } else if (DO_CHAPTER_DOWNLOAD_SPECIFIC.equals(args[1])) {           //Get specific chapter
                start = Integer.parseInt(args[2]);
                end = Integer.parseInt(args[2]);
            }

            //Verify that the range is valid
            if (start <= end)
                downloadSuccess = downloadChapters(mangaName, start, end, chMap, source, targetLocalFolder);

            //Move to dropbox if successfully downloaded
            //Only moves the downloaded chapters because they had .cbr files created
            if (downloadSuccess)
                moveSuccess = moveToDropbox(CBR, targetLocalFolder, targetDropboxFolder);
        } else if (DO_HELP.equals(args[1])) {;
            System.out.println("Download a range of chapters: java -jar 'path/to/file.jar 'manga name' -d startChapterNum-endChapterNum 'manga source'");
            System.out.println("Download the latest x chapters: java -jar 'path/to/file.jar 'manga name' -d -xChapters 'manga source'");
            System.out.println("Download a specific chapter: java -jar 'path/to/file.jar 'manga name' -d chapterNum 'manga source'");
            System.out.println("Ex: \"java -jar path/to/file.js \"One Piece\" -d -20 \"kiss manga\"");
        } else {
            System.out.println("Incorrect Arguments");
        }
        
        System.out.println("Press any key to continue...");
        try {
            System.in.read();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
    
    public static void fetchChapterList(int count) {
        System.out.println("To Do: Implement chapter list");
    }
    
    //TO DO:
    //- Optimize here to pass a hashmap of paths to the download function, instead of having a function per site
    //- Split up download and zip
    public static Boolean downloadChapters(String mangaName, int start, int end, HashMap<Integer, Boolean> chMap, String source, String dest) {
        Boolean isSuccess = false;
        System.out.println("Beginning chapter download from " + source + "...");
        switch (source.toLowerCase()) {
            case KISS_MANGA:
                isSuccess = downloadAndZipFromKissManga(mangaName, start, end, chMap, dest);
                break;
            case MY_MANGA_ONLINE: 
                isSuccess = downloadAndZipFromMangaOnline(mangaName, start, end, chMap, dest);
                break;
            case MANGA_PANDA:
                isSuccess = downloadAndZipFromMangaPanda(mangaName, start, end, chMap, dest);
                break;
            case MANGA_TOWN:
                break;
            case MANGANELO:
                isSuccess = downloadAndZipFromManganelo(mangaName, start, end, chMap, dest);
                break;    
        }
        
        
//        if (MY_MANGA_ONLINE.toLowerCase().equals(source.toLowerCase())) {
//            isSuccess = downloadAndZipFromMangaOnline(mangaName, start, end, chMap, dest);
//        } else if (KISS_MANGA.toLowerCase().equals(source.toLowerCase())) {
//            isSuccess = downloadAndZipFromKissManga(mangaName, start, end, chMap, dest);
//        } else if (MANGA_PANDA.toLowerCase().equals(source.toLowerCase())) {
//            isSuccess = downloadAndZipFromMangaPanda(mangaName, start, end, chMap, dest);
////        } else if (MANGA_TOWN.toLowerCase().equals(source.toLowerCase())) {
////            isSuccess = downloadAndZipFromMangaTown(start, end, chMap, dest);
//        }
        
        return isSuccess;
    }
    
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
    
    private static void downloadImage(String src, String destFolder, Integer index) {
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
    
    private static Boolean moveToDropbox(String fileType, String localFolder, String dropboxFolder) {
        Boolean moveSuccess = true;
        String []extensions = {fileType};
        //Find all files in default folder and move to dropbox
        List<File> files = (List<File>)FileUtils.listFiles(new File(localFolder), extensions, true);
        
        //Create Manga folder if does not exist
        try {
            if (!Files.exists(Paths.get(dropboxFolder)))
                Files.createDirectories(Paths.get(dropboxFolder));
        } catch (IOException e) {
            System.out.println("Couldn't generate new manga folder: " + e.toString());
            
            try {
                int input = System.in.read();
            } catch (IOException ex) {
                System.out.println("Error reading confimation input");
            }
        }
       
        for (File file : files) {
            try {
                String []fileParts = file.toString().split("\\\\");
                String chName = fileParts[fileParts.length - 1];
                Path fp = file.toPath();
                Path fpdb = new File(dropboxFolder + "\\" + chName).toPath();
                Files.move(fp, fpdb, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                moveSuccess = false;
                System.out.println("Error in moveToDropbox");
                System.out.println(e.toString());
                
                try {
                    int input = System.in.read();
                } catch (IOException ex) {
                    System.out.println("Error reading confimation input");
                }
            }
        }
        
        return moveSuccess;
    }
    
    private static Boolean downloadAndZipFromMangaOnline(String mangaName, int start, int end, HashMap<Integer, Boolean> chMap, String destFolder) {
        Boolean downloadSuccess = true;
        Boolean zipSuccess = true;
        String specialChars = "[<>\"\\/\\|?*.]";
        Boolean chapterDownloaded = false;
        
        int rowIndex = 0;
        String html = "";
        try {
            UserAgent userAgent = new UserAgent();
            String targetURL = MANGA_ONLINE_SOURCE + "/search.html?keyword=" + mangaName.replace(" ", "+");
            userAgent.visit(targetURL);
            Elements results = userAgent.doc.findFirst("<div class=popular-body>").findEvery("<li>");
            
            String mangaUrl = "";
            for (Element result : results) {
                if (result.findFirst("<a>").findFirst("<img>").getAt("alt").trim().equals(mangaName)) {
                    mangaUrl = result.findFirst("<a>").getAt("href");
                    break;
                }
            }
            
            userAgent.visit(mangaUrl);
            Elements rows = userAgent.doc.findEvery("<div class=list-chapter>").findEvery("<li>");
            
            //If 'last n' download mode, get the last chapter number and calculate the chapter range
            if (Integer.MAX_VALUE == end) {
                List<Element> rws = rows.toList();
                String lastChNumStr = rws.get(0).findFirst("<a>").innerHTML().replaceAll("<span>.*</span>", "");
                int lastChNum = Integer.parseInt(lastChNumStr.replaceAll("([Vv]ol[. ]*[\\d]{1,3}|[^\\d])*([\\d.]{1,5}).*\\s*", "$2"));
                
                start = lastChNum - (end - start) + 1;
                end = lastChNum;
            }
            
            //Manga list of chapters
            for (Element row : rows) {
                html = row.findFirst("<a>").innerHTML();
                
                //This may not be needed since the chNumber is specifically checked. Outsider chapters shouldn't get past the check
//                if (html.indexOf(mangaName) < 0)
//                    continue;
                html = html.replaceAll("<span>.*</span>", "").trim();
                String chNumberStr = html.replaceAll("([Vv]ol[. ]*[\\d]{1,3}|[^\\d])*([\\d.]{1,5}).*", "$2");
                int chNumber = Integer.parseInt(chNumberStr.contains(".") ? chNumberStr.split("\\.")[0] : chNumberStr);
                
                if (chNumber < start) {
                    break;
                }
                else if (start <= chNumber && chNumber <= end && !chMap.containsKey(chNumber) && zipSuccess) {
                    chapterDownloaded = true;
                    System.out.println("\nDownloading chapter " + chNumberStr);
                    String baseUrl = row.findFirst("<a>").getAt("href");
                    String chName = html.replaceAll("([Vv]ol[. ]*[\\d]{1,3}|[^\\d])*([\\d.]{1,5})[ :]*(.*)", "$3")
                            .replaceAll("[" + SPECIAL_FOLDER_CHARACTERS + "]", "").trim();

                    String destPath = destFolder + "\\" + mangaName + " " + chNumberStr;
                    destPath = (destPath + (!chName.isEmpty() ? " - " + chName : "")).trim();
                    new File(destPath).mkdir();

                    //Go to the page with all of the images
                    userAgent.visit(baseUrl);
                    Element imgContainer = null;
                    imgContainer = userAgent.doc.findFirst("<div class=list-img>");
                        
                    Elements pages = imgContainer.findEvery("<img>");
                    int i = 0;
                    for (Element page : pages) {
                        String imgSrc = "";
                        if (page.hasAttribute("onerror"))
                            imgSrc = page.getAt("onerror");
                        else
                            imgSrc = page.getAt("src");
                        if (imgSrc.indexOf("googleusercontent") > 0) {  //filter out google proxy and decode url
                            imgSrc = imgSrc.replaceAll(".*url=(.*)", "$1");
                            imgSrc = java.net.URLDecoder.decode(imgSrc);
                        }
                        downloadImage(imgSrc, destPath, i);
                        i++;
                    }

                    if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
                        destPath.replace("/", "\\");
                    }

                    //Downloads a random Poorly Drawn Lines comic for fun
                    downloadRandomComic(destPath, i);

                    //Compress into default format
                    String cmpFile = destPath + "." + CBR;
                    zipSuccess = zipFolder(destPath, cmpFile);
                }
                
                rowIndex++;
            }
        } catch (Exception e) {
            downloadSuccess = false;
            System.out.println("Error in downloadAndZipFromMyMangaOnline");
            System.out.println(e);
            
            try {
                int input = System.in.read();
            } catch (IOException ex) {
                System.out.println("Error reading confimation input");
            }
        }
        
        if (!chapterDownloaded) {
            System.out.println("No chapters to download.");
        }
        
        return downloadSuccess && zipSuccess;
    }
    
    private static Boolean downloadAndZipFromKissManga(String mangaName, int start, int end, HashMap<Integer, Boolean> chMap, String destFolder) {
        Boolean downloadSuccess = true;
        Boolean zipSuccess = true;
        String specialChars = "[<>\"\\/\\|?*.]";
        Boolean chapterDownloaded = false;
        
        Cookie ck = null;
        try {
            ck = new Cookie("http://www.kissmanga.com", "cf_clearance=0b9cc50c58146bd764b4469a84d4c6f5d5be8ae9-1447568434-86400");
        } catch (Exception e) {
            System.out.println("error");
        }
        
        int rowIndex = 0;
        String html = "";
        try {
            UserAgent userAgent = new UserAgent();
            userAgent.cookies.addCookie(ck);
            String targetURL = "https://kissmanga.com/Manga/One-Piece";
            userAgent.visit(targetURL);
            
            Elements rows = userAgent.doc.findFirst("<table class=listing>").findEvery("<tr>");
            
            //If 'last n' download mode, get the last chapter number and calculate the range
            if (Integer.MAX_VALUE == end) {
                int chCount = rows.size();
                List<Element> rws = rows.toList();
                int lastChNum = Integer.parseInt(rws.get(2).findFirst("<td>").innerHTML().replaceAll("\\s.+\\s+" + mangaName + " ([0-9]+).*</a>", "$1").trim());
                
                start = lastChNum - (end - start) + 1;
                end = lastChNum;
            }
            
            
            int chCount = rows.size();
            
            if (Integer.MAX_VALUE == end) {
                start = chCount - (end - start) + 1;
                end = chCount;
            }
            
            //Page with all chapters
            for (Element row : rows) {
                if (0 != rowIndex && 1 != rowIndex) {   //1st row is header tr, second row is a styling tr
                    html = StringEscapeUtils.unescapeHtml3(row.findFirst("<td>").innerHTML());
                    String num = html.replaceAll("\\s.+\\s+" + mangaName + " ([0-9]+).*</a>", "$1").trim();
                    if (html.contains("Vol.")) {    //Skip all Volumes
                        continue;
                    }
                    int chNumber = Integer.parseInt(num);

                    if (start <= chNumber && chNumber <= end && !chMap.containsKey(chNumber) && zipSuccess) {
                        chapterDownloaded = true;
                        System.out.println("\nDownloading chapter " + chNumber);
                        String baseUrl = row.findFirst("<td>").findFirst("<a>").getAt("href");
                        String chName = " " + html.replaceAll("(?s)\\s.+\\s" + mangaName + " [0-9]+(.*)</a>", "$1").replaceAll(":", "-").replaceAll(specialChars, "").trim();

                        String destPath = (destFolder + "\\" + mangaName + " " + chNumber + chName).trim();
                        new File(destPath).mkdir();

                        //Go to the page with all of the images
                        userAgent.visit(baseUrl);
                        Elements scripts = userAgent.doc.findEvery("<script type=\"text/javascript\">var lstImages");
                        String script = scripts.getChildElements().get(0).innerHTML();
                        
                        Pattern p = Pattern.compile("https*://.+\\d+", Pattern.MULTILINE);
                        Matcher m = p.matcher(script);
                        StringBuffer sb = new StringBuffer(script.length());
                        while (m.find()) {
                            String text = m.group(0);
                            sb.append(text + ";");
                        }
                        
                        String[] imgSources = sb.toString().split(";");
                        int i = 0;
                        for (i = 0; i < imgSources.length; i++) {
                            downloadImage(imgSources[i], destPath, i);
                        }
                        
                        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
                            destPath.replace("/", "\\");
                        }
                        
                        downloadRandomComic(destPath, i);

                        //Compress into default format
                        String cmpFile = destPath + "." + CBR;
                        zipSuccess = zipFolder(destPath, cmpFile);
                    }
                }
                
                rowIndex++;
            }
        } catch (Exception e) {
            downloadSuccess = false;
            System.out.println("Error in downloadAndZipFromKissManga");
            System.out.println(e);
            
            try {
                int input = System.in.read();
            } catch (IOException ex) {
                System.out.println("Error reading confimation input");
            }
        }
        
        if (!chapterDownloaded) {
            System.out.println("No chapters to download.");
        }
        
        return downloadSuccess && zipSuccess;
    }
    
    private static Boolean downloadAndZipFromMangaPanda(String mangaName, int start, int end, HashMap<Integer, Boolean> chMap, String destFolder) {
        Boolean downloadSuccess = true;
        Boolean zipSuccess = true;
        String specialChars = "[<>\"\\/\\|?*.]";
        
        try {
            UserAgent userAgent = new UserAgent();
            String targetURL = MANGAPANDA_SOURCE + "/actions/search/?q=" + mangaName.replaceAll("\\s", "+") + "&limit=100";
            userAgent.visit(targetURL);
            
            String body = userAgent.doc.innerHTML().toString();
            String []results = body.split("\\|");
            String subPath = results[4];
            
            userAgent.visit(MANGAPANDA_SOURCE + subPath);
            
            Elements rows = userAgent.doc.findFirst("<table id=listing>").findEvery("<tr class=>");
            
            //If 'last n' download mode, get the last chapter number and calculate the range
            if (Integer.MAX_VALUE == end) {
                int chCount = rows.size();
                List<Element> rws = rows.toList();
                int lastChNum = Integer.parseInt(rws.get(chCount - 1).findFirst("<td>").innerHTML().replaceAll("\\s.+\\s.+" + mangaName + " ([0-9]+)</a>.+", "$1").trim());
                
                start = lastChNum - (end - start) + 1;
                end = lastChNum;
            }
            
            //Page with all chapters
            for (Element row : rows) {
                String html = row.findFirst("<td>").innerHTML();
                int chNumber = Integer.parseInt(html.replaceAll("\\s.+\\s.+" + mangaName + " ([0-9]+)</a>.+", "$1").trim());
                
                if (start <= chNumber && chNumber <= end && !chMap.containsKey(chNumber) && zipSuccess) {
                    System.out.println("\nDownloading chapter " + chNumber);
                    String baseUrl = row.findFirst("<td>").findFirst("<a>").getAt("href");
                    String chName = html.replaceAll("\\s.+\\s.+(" + mangaName + ".+)</a>(.+)", "$1$2").replaceAll(":", "-").replaceAll(specialChars, "").trim();
                    
                    if (chName.endsWith("-"))
                        chName = chName.substring(0, chName.length() - 2);

                    String destPath = destFolder + "\\" + chName;
                    new File(destPath).mkdir();

                    userAgent.visit(baseUrl);

                    //Find all Pages, and loop through to download each one
                    Elements chapterPages = userAgent.doc.findFirst("<div id=selectpage>").findFirst("<select>").findEvery("<option>");
                    int pageIndex = 0;
                    for (Element pages : chapterPages) {
                        System.out.print(".");
                        pageIndex++;
                        String pageUrl = MANGAPANDA_SOURCE + pages.getAt("value");
                        userAgent.visit(pageUrl);

                        String imgSrc = userAgent.doc.findFirst("<div id=imgholder>").findFirst("<a>").findFirst("<img>").getAt("src");
                        downloadImage(imgSrc, destPath, pageIndex);
                    }

                    //Compress into default format
                    String cmpFile = destPath + "." + CBR;
                    zipSuccess = zipFolder(destPath, cmpFile);
                }
            }
        } catch (Exception e) {
            downloadSuccess = false;
            System.out.println("Error in downloadAndZipFromMangaPanda");
            System.out.println(e);
            
            try {
                int input = System.in.read();
            } catch (IOException ex) {
                System.out.println("Error reading confimation input");
            }
        }
        
        return downloadSuccess && zipSuccess;
    }
    
    private static Boolean downloadAndZipFromManganelo(String mangaName, int start, int end, HashMap<Integer, Boolean> chMap, String destFolder) {
        Boolean downloadSuccess = true;
        Boolean zipSuccess = true;
        String specialChars = "[<>\"\\/\\|?*.]";
        
        int lChNum = 0;
        try {
            //1) Go to the search page
            UserAgent userAgent = new UserAgent();
            String targetURL = MANGANELO_SOURCE + "/search/" + mangaName.replaceAll("\\s", "_");
            userAgent.visit(targetURL);
            
            //2) Filter out target manga link and go to it
            String targetMangaUrl = userAgent.doc.findFirst("<div class=daily-update").findFirst("<a>").getAt("href");
            userAgent.visit(targetMangaUrl);
            
            //3) Figure out the chapters needed, and iterate through each to download
            Elements rows = userAgent.doc.findFirst("<div class=manga-info-chapter").findEvery("<a>");
            
            //If in 'last n' download mode, get the last chapter number and calculate the range
            if (Integer.MAX_VALUE == end) {
                int chCount = rows.size();
                List<Element> rws = rows.toList();
                int lastChNum = Integer.parseInt(rws.get(0).innerHTML().replaceAll("Vol.[0-9].+ ", "").replaceAll("[Cc]hapter ([0-9]+).*", "$1").trim());
                //lastChNum = Integer.parseInt(rws.get(chCount - 1).findFirst("<td>").innerHTML().replaceAll("\\s.+\\s.+" + mangaName + " ([0-9]+)</a>.+", "$1").trim());
                
                start = lastChNum - (end - start) + 1;
                end = lastChNum;
            }
            
            for (Element row : rows) {
                //3.1) Chapter number
                String num = row.innerHTML().replaceAll("Vol.[0-9]+ ", "").replaceAll("[Cc]hapter ([0-9]+).*", "$1").trim();
//                if (num == "Incident")
//                    System.out.print("hi");
                int chNumber = Integer.parseInt(num);
//                if (chNumber == 503)
//                    System.out.print("hi");
                
//                if (chNumber < start || chNumber > end)
//                    break;
                
                if (chNumber >= start && chNumber <= end && !chMap.containsKey(chNumber) && zipSuccess) {
                    System.out.println("\nDownloading chapter " + chNumber);
                    
//                    //3.2) Chapter name
//                    String chName = row.innerHTML().replaceAll("Vol.[0-9].+ ", "").replaceAll("[Cc]hapter " + chNumber, "").replaceAll(" : ", "").trim();
                    
                    //3.3) Chapter url
                    String baseUrl = row.getAt("href");
                    

                    String destPath = destFolder + "\\" + mangaName + " " + chNumber;
                    new File(destPath).mkdir();

                    userAgent.visit(baseUrl);

                    //Find all Pages, and loop through to download each one
                    Elements pages = userAgent.doc.findFirst("<div id=vungdoc>").findEvery("<img>");
                    int pageIndex = 0;
                    for (Element page : pages) {
                        System.out.print(".");
                        pageIndex++;
                        String imgSrc = page.getAt("src");
                        downloadImage(imgSrc, destPath, pageIndex);
                    }

                    //Compress into default format
                    String cmpFile = destPath + "." + CBR;
                    zipSuccess = zipFolder(destPath, cmpFile);
                }
                lChNum = chNumber;
            }
        } catch (Exception e) {
            downloadSuccess = false;
            System.out.println("Error in downloadAndZipFromMangaPanda, chapter " + lChNum);
            System.out.println(e);
            
            try {
                int input = System.in.read();
            } catch (IOException ex) {
                System.out.println("Error reading confimation input");
            }
        }
        
        return downloadSuccess && zipSuccess;
    }
    
    //============== OUTDATED!!! ==============
//    private static Boolean downloadAndZipFromMangaTown(int start, int end, HashMap<Integer, Boolean> chMap, String destFolder) {
//        Boolean isSuccess = true;
//        try {
//            UserAgent userAgent = new UserAgent();
//            userAgent.visit("http://www.mangatown.com/manga/one_piece/");
//
//            Element link = userAgent.doc.findFirst("<ul class=chapter_list?").findFirst("<li>");
//            
//            //Page with all chapters
//            String baseUrl = link.findFirst("<a>").getAt("href");
//            String chapter = link.findFirst("<a>").innerHTML().replaceAll("[A-Za-z\\s]*", "");
//            String vol = link.findFirst("<span>").innerHTML().replaceAll("[A-Za-z\\s]*", "");
//            
//            //First page of the chapter
//            userAgent.visit(baseUrl);
//            Elements chapterPages = userAgent.doc.findFirst("<div class=page_select>").findEvery("<select>").findEvery("<option>");
//            
//            //find the correct letter prefix by checking against the first page
//            int responseCode = -1;
//            char letter = 'a';
//            String imgBaseSrc = MANGATOWN_SOURCE + vol + "-" + chapter + ".0/compressed/";
//            for (int i = 0; i < 26 && 200 != responseCode; i++) {
//                letter = (char)(97 + i);
//                String imgSrc = imgBaseSrc + letter + "001.jpg";
//                responseCode = getResponseCode(imgSrc);
//            }
//
//            //Download all pages into destination folder
//            Integer pageIndex = 1;
//            imgBaseSrc += letter;
//            for (Element pages : chapterPages) {
//                String imgDownloadSrc = imgBaseSrc + String.format("%03d", pageIndex) + ".jpg";
//                downloadImage(imgDownloadSrc, destFolder, pageIndex);
//                
//                pageIndex++;
//           }
//        } catch (Exception e) {
//            isSuccess = false;
//            System.out.println("Error in downloadAndZipFromMangaTown");
//            System.out.println(e.toString());
//            
//            try {
//                int input = System.in.read();
//            } catch (IOException ex) {
//                System.out.println("Error reading confimation input");
//            }
//        }
//        
//        return isSuccess;
//    }
    
    public static Boolean zipFolder(String srcFolder, String destFileName)
    {   
        String folderParts[] = srcFolder.split("\\\\");
        String chName = folderParts[folderParts.length - 1];
        System.out.println("\nCompressing chapter " + chName + "...");
        
        Boolean zipSuccess = true;
        
        byte[] buffer = new byte[1024];
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(destFileName);
            zos = new ZipOutputStream(fos);
            FileInputStream in = null;

            ArrayList<String> fileList = new ArrayList<String>();
            generateFileList(srcFolder, fileList, new File(srcFolder));
            
            if (0 == fileList.size())
                zipSuccess = false;
            else {
                for (String file : fileList) {
                    ZipEntry ze = new ZipEntry(srcFolder + File.separator + file);
                    zos.putNextEntry(ze);
                    try {
                        in = new FileInputStream(srcFolder + File.separator + file);
                        int len;
                        while ((len = in.read(buffer)) > 0) {
                           zos.write(buffer, 0, len);
                        }
                    } finally {
                       in.close();
                    }
                }
                
                zos.closeEntry();
                System.out.println("Folder successfully compressed");
            }
        } catch (IOException e) {
            zipSuccess = false;
            System.out.println("Error in zipFolder");
            System.out.println(e.toString());
            
            try {
                int input = System.in.read();
            } catch (IOException ex) {
                System.out.println("Error reading confimation input");
            }
        }
        finally {
            try {
                zos.close();
            } catch (IOException e) {
                zipSuccess = false;
                e.printStackTrace();
            }
        }
        
        return zipSuccess;
    }

    public static void generateFileList(String srcFolder, ArrayList<String> fileList, File node)
    {
        if (node.isFile())
            fileList.add(generateZipEntry(srcFolder, node.toString()));
        
        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename : subNote)
               generateFileList(srcFolder, fileList, new File(node, filename));
        }
    }
    
    public static void generateChapterList(String srcFolder, String mangaName, HashMap<Integer, Boolean> dirMap)
    {
        try {
            if (!Files.exists(Paths.get(srcFolder)))
                Files.createDirectories(Paths.get(srcFolder));
        } catch (IOException e) {
            System.out.println("Couldn't generate new manga folder: " + e.toString());
            System.out.println(e.toString());
            
            try {
                int input = System.in.read();
            } catch (IOException ex) {
                System.out.println("Error reading confimation input");
            }
        }
            
        File dir = new File(srcFolder);
        
        String []chapters = dir.list();
        for (String chapter : chapters) {
            dirMap.put(Integer.parseInt(chapter.replaceAll(mangaName + " ([0-9]+).*", "$1")), true);
        }
    }

    private static String generateZipEntry(String srcFolder, String file)
    {
       return file.substring(srcFolder.length() + 1, file.length());
    }
    
    //==========================================================
    // Download Fun Stuff
    //==========================================================
    private static void downloadRandomComic(String destPath, int pageIndex) {
        UserAgent userAgent = new UserAgent();
        String targetURL = "http://poorlydrawnlines.com/archive/";
        try {
            userAgent.visit(targetURL);

            Element wrapper = userAgent.doc.findEvery("<div class=wrapper>");
            Element page = wrapper.findFirst("<div>");
//            Element ul = page.findFirst("<ul>");
//            Elements li = ul.findEvery("<li>");
            Elements href = page.findEvery("<a>");
            //Elements rows = userAgent.doc.findFirst("<div class=wrapper>").findFirst("<div>").findFirst("<ul>").findEvery("<li>").findEvery("<a>");
                    
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
                    downloadImage(imgSrc, destPath, pageIndex);
                }
                counter++;
            }
        } catch (Exception e) {
            System.out.println("Downloading Random Comic failed");
        }
    }
}
