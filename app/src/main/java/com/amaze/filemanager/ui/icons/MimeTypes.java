/*
 * Copyright (C) 2013 Simple Explorer
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 *
 * Copyright (C) 2014 Arpit Khurana <arpitkh96@gmail.com>
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.amaze.filemanager.ui.icons;

import android.util.Log;
import android.webkit.MimeTypeMap;

import com.amaze.filemanager.utils.files.CryptUtil;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;


import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

//import net.sf.jmimemagic.Magic;
//import net.sf.jmimemagic.MagicException;
//import net.sf.jmimemagic.MagicMatch;
//import net.sf.jmimemagic.MagicMatchNotFoundException;
//import net.sf.jmimemagic.MagicParseException;

//import eu.medsea.mimeutil.MimeUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.apache.commons.compress.utils.IOUtils.closeQuietly;
// import org.apache.tika.metadata.Metadata;
// import org.apache.tika.parser.AutoDetectParser;
// import org.apache.tika.parser.Parser;
// import org.apache.tika.sax.BodyContentHandler;


public final class MimeTypes {

    public static final String ALL_MIME_TYPES = "*/*";

    // construct a with an approximation of the capacity
    private static final HashMap<String, String> MIME_TYPES = new HashMap<>(1 + (int)(66 / 0.75));

    static {


		/*
         * ================= MIME TYPES ====================
		 */
        MIME_TYPES.put("asm", "text/x-asm");
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("js", "application/javascript");

        MIME_TYPES.put("def", "text/plain");
        MIME_TYPES.put("in", "text/plain");
        MIME_TYPES.put("rc", "text/plain");
        MIME_TYPES.put("list", "text/plain");
        MIME_TYPES.put("log", "text/plain");
        MIME_TYPES.put("pl", "text/plain");
        MIME_TYPES.put("prop", "text/plain");
        MIME_TYPES.put("properties", "text/plain");
        MIME_TYPES.put("rc", "text/plain");
        MIME_TYPES.put("ini", "text/plain");
        MIME_TYPES.put("md", "text/markdown");

        MIME_TYPES.put("epub", "application/epub+zip");
        MIME_TYPES.put("ibooks", "application/x-ibooks+zip");

        MIME_TYPES.put("ifb", "text/calendar");
        MIME_TYPES.put("eml", "message/rfc822");
        MIME_TYPES.put("msg", "application/vnd.ms-outlook");

        MIME_TYPES.put("ace", "application/x-ace-compressed");
        MIME_TYPES.put("bz", "application/x-bzip");
        MIME_TYPES.put("bz2", "application/x-bzip2");
        MIME_TYPES.put("cab", "application/vnd.ms-cab-compressed");
        MIME_TYPES.put("gz", "application/x-gzip");
        MIME_TYPES.put("lrf", "application/octet-stream");
        MIME_TYPES.put("jar", "application/java-archive");
        MIME_TYPES.put("xz", "application/x-xz");
        MIME_TYPES.put("Z", "application/x-compress");

        MIME_TYPES.put("bat", "application/x-msdownload");
        MIME_TYPES.put("ksh", "text/plain");
        MIME_TYPES.put("sh", "application/x-sh");

        MIME_TYPES.put("db", "application/octet-stream");
        MIME_TYPES.put("db3", "application/octet-stream");

        MIME_TYPES.put("otf", "application/x-font-otf");
        MIME_TYPES.put("ttf", "application/x-font-ttf");
        MIME_TYPES.put("psf", "application/x-font-linux-psf");

        MIME_TYPES.put("cgm", "image/cgm");
        MIME_TYPES.put("btif", "image/prs.btif");
        MIME_TYPES.put("dwg", "image/vnd.dwg");
        MIME_TYPES.put("dxf", "image/vnd.dxf");
        MIME_TYPES.put("fbs", "image/vnd.fastbidsheet");
        MIME_TYPES.put("fpx", "image/vnd.fpx");
        MIME_TYPES.put("fst", "image/vnd.fst");
        MIME_TYPES.put("mdi", "image/vnd.ms-mdi");
        MIME_TYPES.put("npx", "image/vnd.net-fpx");
        MIME_TYPES.put("xif", "image/vnd.xiff");
        MIME_TYPES.put("pct", "image/x-pict");
        MIME_TYPES.put("pic", "image/x-pict");
        MIME_TYPES.put("gif", "image/gif");


        MIME_TYPES.put("adp", "audio/adpcm");
        MIME_TYPES.put("au", "audio/basic");
        MIME_TYPES.put("snd", "audio/basic");
        MIME_TYPES.put("m2a", "audio/mpeg");
        MIME_TYPES.put("m3a", "audio/mpeg");
        MIME_TYPES.put("oga", "audio/ogg");
        MIME_TYPES.put("spx", "audio/ogg");
        MIME_TYPES.put("aac", "audio/x-aac");
        MIME_TYPES.put("mka", "audio/x-matroska");

        MIME_TYPES.put("jpgv", "video/jpeg");
        MIME_TYPES.put("jpgm", "video/jpm");
        MIME_TYPES.put("jpm", "video/jpm");
        MIME_TYPES.put("mj2", "video/mj2");
        MIME_TYPES.put("mjp2", "video/mj2");
        MIME_TYPES.put("mpa", "video/mpeg");
        MIME_TYPES.put("ogv", "video/ogg");
        MIME_TYPES.put("flv", "video/x-flv");
        MIME_TYPES.put("mkv", "video/x-matroska");

   // mp3|wma|wav|aac|ogg|m4a|flac|mp4|avi|mpg|mpeg|3gp|3gpp|mkv|flv|rmvb

        MIME_TYPES.put(CryptUtil.CRYPT_EXTENSION.replace(".", ""), "crypt/aze");
    }


    /**
     * Get Mime Type of a file
     * @param path the file of which mime type to get
     * @return Mime type in form of String
     */

//    public static String getMimeType2(String path){
//        File file=new File(path);
//        Magic parser = new Magic();
//        String type="";
//        try {
//            MagicMatch match = parser.getMagicMatch(file, false);
//            type=match.getMimeType();
//            System.out.println(match.getMimeType()) ;
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//    return type;
//    }
/*
    public static String getMimeType2(File file) throws MagicParseException, MagicMatchNotFoundException, MagicException {
        Magic parser = new Magic();
        MagicMatch match = parser.getMagicMatch(file, false);
        String type=match.getMimeType();
        Log.i("type2",""+type);
        return type;
    }


public static String getMimeType1(String path){
    File f = new File(path);
    MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
    Collection<?> mimeTypes = MimeUtil.getMimeTypes(f);
    System.out.println(mimeTypes);
 return "";
}

    public static String getMimeType3(String path){

        FileInputStream is = null;
        try {
            File f = new File("C:/Temp/mime/test.docx");
            is = new FileInputStream(f);

            ContentHandler contenthandler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            metadata.set(Metadata.RESOURCE_NAME_KEY, f.getName());
            Parser parser = new AutoDetectParser();
            // OOXMLParser parser = new OOXMLParser();
            parser.parse();
            parser.parse(is, contenthandler, metadata,this);
            System.out.println("Mime: " + metadata.get(Metadata.CONTENT_TYPE));
            System.out.println("Title: " + metadata.get(Metadata.TITLE));
            System.out.println("Author: " + metadata.get(Metadata.AUTHOR));
            System.out.println("content: " + contenthandler.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (is != null) is.close();
        }

        return "";
    }


    public static String getMimeType3(String path){
        Tika tika = new Tika();
        File file=new File(path);

        try {
            String text = tika.parseToString(file);
            System.out.print(text);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    public static String getMimeType3(File f){
        Tika tika = new Tika();
        try {
            String text = tika.parseToString(f);
            System.out.print(text);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getMimeType3(String f){

        try {
            InputStream input=new FileInputStream(new File(f));//可以写文件路径，pdf，word，html等
            BodyContentHandler textHandler=new BodyContentHandler();
            Metadata matadata=new Metadata();//Metadata对象保存了作者，标题等元数据
            org.apache.tika.parser.Parser parser=new  AutoDetectParser();//当调用parser，AutoDetectParser会自动估计文档MIME类型，此处输入pdf文件，因此可以使用PDFParser
            ParseContext context=new ParseContext();
            parser.parse(input, textHandler, matadata, context);//执行解析过程
            input.close();
            System.out.println("Title: "+matadata.get(Metadata.TITLE));
            System.out.println("Type: "+matadata.get(Metadata.TYPE));
            System.out.println("Body: "+textHandler.toString());//从textHandler打印正文
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

*/
public static void getMimeType4(String path){
    ContentInfoUtil util = new ContentInfoUtil();
 //   ContentInfo info = util.findMatch(path);
    try {
        ContentInfo info = util.findMatch(path);
        Log.i("type4",""+info);
    }catch (Exception e){
        e.printStackTrace();
    }
}
    public static void getMimeType4_(File file){
            if(file.exists()){
                Log.i("file","exists");
            }else {
                Log.i("file","not exists");
            }

            int readSize =10 * 1024;
            //fileReadSize;
            if (file.length() < readSize) {
                readSize = (int) file.length();
            }
            if (readSize == 0) {
                Log.w("type4_","empty");
               // return ContentInfo.EMPTY_INFO;
            }
            byte[] bytes = new byte[readSize];
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                fis.read(bytes);
                Log.i("file bytes",""+bytes);
            }catch (Exception e){} finally {
              closeQuietly(fis);
        }

        ContentInfoUtil util = new ContentInfoUtil();
        //   ContentInfo info = util.findMatch(path);
        try {
            ContentInfo info = util.findMatch(bytes);
            Log.i("type4",""+info);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void getMimeType4(File path){
        ContentInfoUtil util = new ContentInfoUtil();
        //   ContentInfo info = util.findMatch(path);
        try {
            ContentInfo info = util.findMatch(path);
            Log.i("type4",""+info);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
//    public static String getMimeType2(File file) {
//
//        Log.i("file",""+file.exists());
//        Magic parser = new Magic();
//        String type="";
//        try{
//            MagicMatch match = parser.getMagicMatch(file, false);
//             type=match.getMimeType().toString();
//            Log.i("type2",""+type);
//
//        }catch (Exception e)
//
//        {
//            e.printStackTrace();
//        }
//
//        return type;
//    }

    public static String getMimeType(String path, boolean isDirectory) {
        if (isDirectory) {
            return null;
        }

        String type = ALL_MIME_TYPES;
        final String extension = getExtension(path);

        // mapping extension to system mime types
        if (extension != null && !extension.isEmpty()) {
            final String extensionLowerCase = extension.toLowerCase(Locale
                    .getDefault());
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extensionLowerCase);
            if (type == null) {
                type = MIME_TYPES.get(extensionLowerCase);
            }
        }
        if(type == null) type = ALL_MIME_TYPES;
        return type;
    }

    public static boolean mimeTypeMatch(String mime, String input) {
        return Pattern.matches(mime.replace("*", ".*"), input);
    }


    /**
     * Helper method for {@link #getMimeType(String, boolean)}
     * to calculate the last '.' extension of files
     * @param path the path of file
     * @return extension extracted from name in lowercase
     */
    public static String getExtension(String path) {
        if(path.contains(".")) return path.substring(path.lastIndexOf(".") + 1).toLowerCase();
        else return "";
    }

}
