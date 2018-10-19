package com.amaze.filemanager.utils;

import android.os.Environment;
import android.util.Log;

import com.amaze.filemanager.database.UtilsHandler;
import com.amaze.filemanager.database.models.OperationData;
import com.amaze.filemanager.utils.application.AppConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class VerifyPath {

    private static UtilsHandler utilsHandler ;
    private static String AMAZE_FILE_CENTER= Environment.getExternalStorageDirectory().getAbsolutePath() + "/AmazeFileCenter/";



    public static void init(){
       buildAmazeFolder();

//        if (!getBoolean(PREFERENCE_BUILD_AMAZE_FOLDER)) {
//            if(buildAmazeFolder()){
//                getPrefs().edit().putBoolean(PreferencesConstants.PREFERENCE_BUILD_AMAZE_FOLDER,  true).commit();
//            }
//        }
    }


    public static boolean buildAmazeFolder(){
        utilsHandler = AppConfig.getInstance().getUtilsHandler();
        File file=new File(AMAZE_FILE_CENTER);
        if (!file.exists()){
            file.mkdir();
        }

        if(file.exists() && file.isDirectory()){
            String[] AmazeDirs = new String[] {
                    AMAZE_FILE_CENTER+"Picture",
                    AMAZE_FILE_CENTER+"Download",
                    AMAZE_FILE_CENTER+"Audio",
                    AMAZE_FILE_CENTER+"Video",
                    AMAZE_FILE_CENTER+"Music"
            };

            String[] Libs = new String[] {
                    "Picture",
                    "Download",
                    "Audio",
                    "Video",
                    "Music"
            };

            String[][]  KeyWords=new String[][]{
                    {"camera","photo","pic","图片","相片","照片","dicm","screenshot","截屏","snap","image"},
                    {"down","下载"},
                    {"音频","录音","record","audio"},
                    {"movie","video","视频"},
                    {"音乐","music"}
            };
            for (int i=0;i<AmazeDirs.length;i++){

                String dir_child=AmazeDirs[i]+"/";
                int n=0;
                Log.i("show dir_child"+n,dir_child);

                File file_child=new File(dir_child);
                if (!file_child.exists()){
                    file_child.mkdir();
                }

                String pathgroup=findFolder2Layer(new File(Environment.getExternalStorageDirectory().getAbsolutePath()),KeyWords[i]);
                String[] path=pathgroup.split("\n");
                for(String pathsource:path){
                    try{
                        if( pathsource!=null && pathsource.length()>0
                                && !pathsource.contains(AMAZE_FILE_CENTER) && !pathsource.contains("/Android") && !pathsource.contains("/backup") ){
                            n++;

//                            String newFileName=dir_child+pathsource.replaceFirst(".*/","").replace("/","_")+n;
                            String name="";
                            if(pathsource.matches("^.*/$")){
                                  name=pathsource.replaceAll("/$","").replaceFirst(".*/","")+"/";
                            }else{
                                  name=pathsource.replaceFirst(".*/","");
                            }
       ;
                            String lib=Libs[i];
                         //   utilsHandler.saveToDatabase(new OperationData(UtilsHandler.Operation.LIB,name,pathsource,lib));
                            utilsHandler.updateLibPath(name,pathsource,lib);
                        }else{
                            Log.e("data","null error");
                        }
                    }catch (Exception e){Log.e("data","error");}

                    //     utilsHandler.addlibitem( name,pathsource,lib);
                }

            }
            return true;
        }
        return false;
    }

    // 扫描目标及其第一级子目录中，包含关键字的目录
    public static String findFolder2Layer(File Folder,String[] KeyWords){
        String result="";

        if( Folder.isDirectory()){
            File[] files = Folder.listFiles();
            for(File file:files){
                Boolean flag=true;
                if(file.isDirectory()){
                    String fileName=file.getName();
                    for(String KeyWord:KeyWords) {
                        if (fileName.toLowerCase().contains(KeyWord)) {
                            result = result + "\n" + file.getAbsolutePath();
                            flag=false;
                            break;
                        }
                    }

                    if(flag){
                        result= result + "\n" + findFolder(file,KeyWords);
                    }
                }

            }
        }
        result=result.replaceAll("\n+","\n");
        Log.w("2layer",result);
        return  result;
    }

    // 扫描目标目录中，包含关键字的目录
    public static  String findFolder(File Folder , String[] KeyWords) {
        String result="";
        if( Folder.isDirectory()){
            File[] files = Folder.listFiles();
            if(files==null) return result;
            for(File file:files){
                if(file.isDirectory()){
                    String fileName=file.getName();
                    for(String KeyWord:KeyWords) {
                        if (fileName.toLowerCase().contains(KeyWord)) {
                            result = result + "\n" + file.getAbsolutePath();
                            break;
                        }
                    }
                }

            }
        }
        //        return  result;
        return  result.replaceAll("\n+","\n");
    }


    public static boolean softLinkMode(String localPath, String softPath) {
        Process p;
        int status;
        try {
            long time = System.currentTimeMillis();
//			String cmd = "ln -s " + localPath + " "+ softPath; （文件名没有空格）
            String[] cmd ={ "ln","-s",localPath,softPath}; //（有空格时候，使用字符数组）
            p = Runtime.getRuntime().exec(cmd);
            releaseProcessStream(p);
            status = p.waitFor();
            if (status == 0) {
                return true;
            } else return false;
        } catch (Exception e) {
            return false;
        }
    }private static void releaseProcessStream(Process p) throws IOException {
        InputStream stderr = p.getErrorStream();
        InputStreamReader isr = new InputStreamReader(stderr);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        while ((line = br.readLine()) != null) System.out.println(line);
    }




}
