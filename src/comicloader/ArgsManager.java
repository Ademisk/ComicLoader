package comicloader;

public class ArgsManager {
    
    final static int MANGA_NAME = 0;
    final static int ACTION = 1;
    final static int CHAPTERS = 2;
    final static int SITE_SOURCE = 3;    
    
    final public static String DO_LIST_CHAPTERS = "-c";
    final public static String DO_CHAPTER_DOWNLOAD_SPECIFIC = "-Specific";
    final public static String DO_CHAPTER_DOWNLOAD_RANGE = "-Range";
    final public static String DO_CHAPTER_DOWNLOAD_LAST = "-Last";
    final public static String DO_HELP = "-h";
    
    private String[] args;
    
    public ArgsManager(String inputArgs[]) {
        args = inputArgs;
    }
    
    public int getArgsLength() {
        return args.length;
    }
    
    public Boolean canDoListChapters() {
        return 3 <= args.length && DO_LIST_CHAPTERS.equals(args[ACTION]);
    }
    
    public Boolean canDoHelp() {
        return DO_HELP.equals(args[ACTION]);
    }
    
    public String getMangaName() {
        return args[MANGA_NAME];
    }
    
    public String getTypeOfDownload() {
        return args[ACTION];
    }
    
    public String getChapters() {
        return args[CHAPTERS];
    }
    
    public String getSource() {
        return args[SITE_SOURCE];
    }
}
