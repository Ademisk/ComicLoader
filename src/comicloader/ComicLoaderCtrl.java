package comicloader;

import Utils.Utils;
import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.NotFound;
import com.jaunt.UserAgent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;


public class ComicLoaderCtrl {
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
    final static String MANGAPANDA_SOURCE = "http://www.mangapanda.com";
    final static String KISSMANGA_SOURCE = "http://kissmanga.com";
    final static String MANGA_ONLINE_SOURCE = "http://mangaonline.to";
    final static String MANGA_READER_SOURCE = "http://www.mangareader.net";
    final static String MANGANELO_SOURCE = "http://manganelo.com";
    //final static String MANGA_SOURCE = MANGANELO_SOURCE;
    
    final static String SPECIAL_FOLDER_CHARACTERS = "\\/:*?\"<>|";  
    
    private ArgsManager argMan;
    
    public void run(String args[]) {
        argMan = new ArgsManager(args);
        mainController();
        confirmPrompt("Press any key to continue...");
    }
    
    public void mainController() {
        System.out.println("Welcome!\n");
        
        String source = argMan.getSource();
        Boolean doListChapters = argMan.canDoListChapters();
        Boolean doHelp = argMan.canDoHelp();
        String chStr = argMan.getChapters();
        
        if (doListChapters && chStr.matches("-\\d+")) {
            //List the latest n chapters
            fetchChapterList(Integer.parseInt(chStr));
        } else if (doHelp) {
            Utils.printHelpMessage();
        } else if (3 <= argMan.getArgsLength()) {
            //Download the chapters specified
            String mangaName = argMan.getMangaName();
            String targetLocalFolder;
            String targetDropboxFolder;
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
            String typeOfDownload = argMan.getTypeOfDownload();
            
            switch (typeOfDownload) {
                case ArgsManager.DO_CHAPTER_DOWNLOAD_LAST:        //Get latest n chapters
                    start = Integer.MAX_VALUE - Integer.parseInt(chStr.replaceAll("-", ""));
                    end = Integer.MAX_VALUE;
                    break;
                case ArgsManager.DO_CHAPTER_DOWNLOAD_RANGE:       //Get capters n to m
                    start = Integer.parseInt(chStr.replaceAll("-\\d+", ""));
                    end = Integer.parseInt(chStr.replaceAll("\\d+-", ""));
                    break;
                case ArgsManager.DO_CHAPTER_DOWNLOAD_SPECIFIC:    //Get specific chapter
                    start = Integer.parseInt(chStr);
                    end = start;
                    break;
            }

            //Verify that the range is valid
            if (start <= end)
                downloadSuccess = downloadChapters(mangaName, start, end, chMap, source, targetLocalFolder);

            //Move to dropbox if successfully downloaded
            //Only moves the downloaded chapters because they had .cbr files created
            if (downloadSuccess)
                moveSuccess = StorageService.moveToDropbox(CBR, targetLocalFolder, targetDropboxFolder);
        } else {
            System.out.println("Incorrect Arguments");
        }
    }
    
    public Boolean downloadChapters(String mangaName, int start, int end, HashMap<Integer, Boolean> chMap, String source, String dest) {
        Boolean isSuccess = false;
        System.out.println("Beginning chapter download from " + source + "...");
        switch (source.toLowerCase()) {
            case MANGANELO:
                isSuccess = downloadAndZipFromManganelo(mangaName, start, end, chMap, dest);
                break;    
//            case KISS_MANGA:
//                isSuccess = downloadAndZipFromKissManga(mangaName, start, end, chMap, dest);
//                break;
//            case MY_MANGA_ONLINE: 
//                isSuccess = downloadAndZipFromMangaOnline(mangaName, start, end, chMap, dest);
//                break;
//            case MANGA_PANDA:
//                isSuccess = downloadAndZipFromMangaPanda(mangaName, start, end, chMap, dest);
//                break;
//            case MANGA_TOWN:
//                break;
        }
        
        return isSuccess;
    }
    
    private Boolean downloadAndZipFromManganelo(String mangaName, int start, int end, HashMap<Integer, Boolean> chMap, String destFolder) {
        Boolean downloadSuccess = true;
        Boolean zipSuccess = true;
        
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
            Elements chapters = userAgent.doc.findFirst("<div class=manga-info-chapter").findEvery("<a>");
            
            //If in 'last n' download mode, get the last chapter number and calculate the range
            if (Integer.MAX_VALUE == end) {
                List<Element> rws = chapters.toList();
                int lastChNum = Integer.parseInt(rws.get(0).innerHTML().replaceAll("Vol.[0-9].+ ", "").replaceAll("[Cc]hapter ([0-9]+).*", "$1").trim());
                
                start = lastChNum - (end - start) + 1;
                end = lastChNum;
            }
            
            
            for (Element chapter : chapters) {
                //3.1) Chapter number
                String chNumStr = chapter.innerHTML().replaceAll("Vol.[0-9]+ ", "").replaceAll("[Cc]hapter ([0-9]+).*", "$1").trim();
                int chNumber = Integer.parseInt(chNumStr);
                
                if (chNumber >= start && chNumber <= end && !chMap.containsKey(chNumber) && zipSuccess) {
                    System.out.println("\nDownloading chapter " + chNumber);
                    
//                    //3.2) Chapter name
//                    String chName = row.innerHTML().replaceAll("Vol.[0-9].+ ", "").replaceAll("[Cc]hapter " + chNumber, "").replaceAll(" : ", "").trim();
                    
                    //3.3) Chapter url
                    String chUrl = chapter.getAt("href");
                    userAgent.visit(chUrl);

                    //Find all Pages, and loop through to download each one
                    Elements pages = userAgent.doc.findFirst("<div id=vungdoc>").findEvery("<img>");
                    String destPath = destFolder + "\\" + mangaName + " " + chNumber;
                    Boolean isDownloadSuccessful = doChapterDownload(pages, destPath);

                    //Compress into default format
                    if (isDownloadSuccessful)
                        zipSuccess = StorageService.zipFolder(destPath, CBR);
                }
                lChNum = chNumber;
            }
        } catch (Exception e) {
            downloadSuccess = false;
            System.out.println("Error in downloadAndZipFromMangaPanda, chapter " + lChNum);
            System.out.println(e);
        }
        
        return downloadSuccess && zipSuccess;
    }
    
    private Boolean doChapterDownload(Elements pages, String destPath) {
        new File(destPath).mkdir();
        
        int pageIndex = 0;
        for (Element page : pages) {
            try {
                pageIndex++;
                System.out.print("[Page " + pageIndex + "]");
                String imgSrc = page.getAt("src");
                WebService.downloadImage(imgSrc, destPath, pageIndex);
            }
            catch (NotFound e) {
                System.out.println("Error in doChapterDownload, page " + pageIndex);
                System.out.println(e);
                
                return false;
            }
        }
        
        return true;
    }
    
    public void generateChapterList(String srcFolder, String mangaName, HashMap<Integer, Boolean> dirMap)
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
    
    public void fetchChapterList(int count) {
        System.out.println("To Do: Implement chapter list");
    }
    
    public void confirmPrompt(String str) {
        try {
            System.out.println(str);
            int input = System.in.read();
        } catch (IOException ex) {
            System.out.println("Error reading confimation input");
        }
    }
}
