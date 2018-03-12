package comicloader;

public class ComicLoader {
    //Parameters
    //args[0] - "Manga Name": "Bleach", "One Piece"
    //args[1] - Action to take. '-c' (chapter list) or '-[command]' (download chapters)
    //args[2] - Chapter numbers. Specific: '-specific n', Range: '-range 1-100', Last n: '-last n'
    //args[3] - "Source Site": "KISSMANGA", "MANGAPANDA", "MANGA ONLINE"
    public static void main(String[] args) {        
        ComicLoaderCtrl ctrl = new ComicLoaderCtrl();
        ctrl.run(args);
    }
}