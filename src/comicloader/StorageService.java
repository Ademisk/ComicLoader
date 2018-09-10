package comicloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.users.FullAccount;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import java.io.InputStream;

/**
 *
 * @author Max
 */
public class StorageService {
    final static String MAX_DROPBOX_API_KEY = "iappzlcay263tks";
    //final static String MAX_ACCESS_TOKEN = "vdW-CNED7A8AAAAAAABk-3cCn75BLQNnKwMmOnx6paolfgcpIgQftATJzogetp-L";
    //final static String CAITLIN_ACCESS_TOKEN = "PHuBlNxvi2cAAAAAAAAIe_Wgdwnl0qdpcqXOlPFQg9OR794k432tijqIId-Vi-t7";
    
    public static Boolean moveToDropbox(String fileType, String localFolder, String mangaName, String dropboxApiKey) {        
        Boolean moveSuccess = true;
        String []extensions = {fileType};
        //Find all files in default folder and upload to dropbox
        List<File> files = (List<File>)FileUtils.listFiles(new File(localFolder), extensions, true);
        
        DbxRequestConfig config = new DbxRequestConfig("mangaloader/1.0");
        DbxClientV2 client = new DbxClientV2(config, dropboxApiKey);
        for (File file : files) {
            String []fileParts = file.toString().split("\\\\");
            String chName = fileParts[fileParts.length - 1];
            String path = file.toPath().toString();
            try (InputStream in = new FileInputStream(path)) {
                FileMetadata metadata = client.files().uploadBuilder("/Manga/" + mangaName + "/" + chName)
                    .uploadAndFinish(in);
            }
            catch (Exception e) {
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
    
    public static Boolean zipFolder(String srcFolder, String format)
    {   
        String destFileName = srcFolder + "." + format;
        
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
    
    private static String generateZipEntry(String srcFolder, String file)
    {
       return file.substring(srcFolder.length() + 1, file.length());
    }
}
