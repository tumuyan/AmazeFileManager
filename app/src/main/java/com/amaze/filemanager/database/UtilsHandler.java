package com.amaze.filemanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.amaze.filemanager.R;
import com.amaze.filemanager.database.models.OperationData;
import com.amaze.filemanager.filesystem.ssh.SshClientUtils;
import com.amaze.filemanager.utils.SmbUtil;
import com.amaze.filemanager.utils.application.AppConfig;
import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;
import com.googlecode.concurrenttrees.radix.node.concrete.voidvalue.VoidValue;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

/**
 * Created by Vishal on 29-05-2017.
 * Class handles database with tables having list of various utilities like
 * history, hidden files, list paths, grid paths, bookmarks, smb entry
 *
 * Try to use these functions from a background thread
 */

public class UtilsHandler extends SQLiteOpenHelper {

    private Context context;

    private static final String DATABASE_NAME = "utilities.db";
    private static final int DATABASE_VERSION = 2;  // increment only when making change in schema

    private static final String TABLE_HISTORY = "history";
    private static final String TABLE_HIDDEN = "hidden";
    private static final String TABLE_LIST = "list";
    private static final String TABLE_GRID = "grid";
    private static final String TABLE_BOOKMARKS = "bookmarks";
    private static final String TABLE_LIB = "lib";
    private static final String TABLE_LIBNAME = "lib_name";
    private static final String TABLE_SMB = "smb";
    private static final String TABLE_SFTP = "sftp";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_PATH = "path";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_HOST_PUBKEY = "pub_key";
    private static final String COLUMN_PRIVATE_KEY_NAME = "ssh_key_name";
    private static final String COLUMN_PRIVATE_KEY = "ssh_key";
    private static final String COLUMN_LIB = "lib";
    private static final String COLUMN_LIBNAME = "lib_name";

    private static final String querySftp = "CREATE TABLE IF NOT EXISTS " + TABLE_SFTP + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY,"
            + COLUMN_NAME + " TEXT,"
            + COLUMN_PATH + " TEXT,"
            + COLUMN_HOST_PUBKEY + " TEXT,"
            + COLUMN_PRIVATE_KEY_NAME + " TEXT,"
            + COLUMN_PRIVATE_KEY + " TEXT"
            + ")";

    public UtilsHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String queryHistory = "CREATE TABLE IF NOT EXISTS " + TABLE_HISTORY + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_PATH + " TEXT"
                + ")";
        String queryHidden = "CREATE TABLE IF NOT EXISTS " + TABLE_HIDDEN + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_PATH + " TEXT"
                + ")";
        String queryList = "CREATE TABLE IF NOT EXISTS " + TABLE_LIST + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_PATH + " TEXT"
                + ")";
        String queryGrid = "CREATE TABLE IF NOT EXISTS " + TABLE_GRID + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_PATH + " TEXT"
                + ")";
        String queryBookmarks = "CREATE TABLE IF NOT EXISTS " + TABLE_BOOKMARKS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_PATH + " TEXT"

                + ")";
        String queryLib="CREATE TABLE IF NOT EXISTS " + TABLE_LIB  + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_PATH + " TEXT,"
                + COLUMN_LIB + " TEXT"
                + ")";
/*      暂不启用libname表
        String queryLibName="CREATE TABLE IF NOT EXISTS " + TABLE_LIBNAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_LIB + " TEXT,"
                + COLUMN_LIBNAME + " TEXT"
                + ")";*/
        String querySmb = "CREATE TABLE IF NOT EXISTS " + TABLE_SMB + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_PATH + " TEXT"
                + ")";

        db.execSQL(queryHistory);
        db.execSQL(queryHidden);
        db.execSQL(queryList);
        db.execSQL(queryGrid);
        db.execSQL(queryBookmarks);
        db.execSQL(querySmb);
        db.execSQL(queryLib);
 //       db.execSQL(queryLibName);
        db.execSQL(querySftp);
        Log.w("UtilsHandler","oncreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch(oldVersion){
            case 1:
                db.execSQL(querySftp);
                break;
            default:
                break;
        }
    }

    public enum Operation {
        HISTORY,
        HIDDEN,
        LIST,
        GRID,
        BOOKMARKS,
        SMB,
        SFTP,
        LIB
    }

    public void saveToDatabase(OperationData operationData) {
        AppConfig.runInBackground(() -> {
            switch (operationData.type) {
                case HIDDEN:
                case HISTORY:
                case LIST:
                case GRID:
                    setPath(operationData.type, operationData.path);
                    break;
                case BOOKMARKS:
                case SMB:
                    setPath(operationData.type, operationData.name, operationData.path);
                    break;
                case SFTP:
                    addSsh(operationData.name, operationData.path, operationData.hostKey,
                            operationData.sshKeyName, operationData.sshKey);
                    break;
                case LIB:
                    setPath(operationData.type, operationData.name, operationData.path,operationData.lib);
                    break;
                default:
                    throw new IllegalStateException("Unidentified operation!");
            }
        });
    }

    public void removeFromDatabase(OperationData operationData) {
        AppConfig.runInBackground(() -> {
            switch (operationData.type) {
                case HIDDEN:
                case HISTORY:
                case LIST:
                case GRID:
                    removePath(operationData.type, operationData.path);
                    break;
                case BOOKMARKS:
                    removeBookmarksPath(operationData.name, operationData.path);
                    break;
                case LIB:
                    removelib(operationData.name,operationData.path,operationData.lib);
                case SMB:
                    removeSmbPath(operationData.name, operationData.path);
                    break;
                case SFTP:
                    removeSftpPath(operationData.name, operationData.path);
                    break;
                default:
                    throw new IllegalStateException("Unidentified operation!");
            }
        });
    }

    public void addCommonBookmarks() {
        String sd = Environment.getExternalStorageDirectory() + "/";

        String[] dirs = new String[] {
                sd + Environment.DIRECTORY_DCIM,
                sd + Environment.DIRECTORY_DOWNLOADS,
                sd + Environment.DIRECTORY_MOVIES,
                sd + Environment.DIRECTORY_MUSIC,
                sd + Environment.DIRECTORY_PICTURES
        };

        for (String dir : dirs) {
            saveToDatabase(new OperationData(Operation.BOOKMARKS, new File(dir).getName(), dir));
        }
    }


    public void addSsh(String name, String path, String hostKey, String sshKeyName, String sshKey) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PATH, path);
        values.put(COLUMN_HOST_PUBKEY, hostKey);
        if(sshKey != null && !"".equals(sshKey))
        {
            values.put(COLUMN_PRIVATE_KEY_NAME, sshKeyName);
            values.put(COLUMN_PRIVATE_KEY, sshKey);
        }

        database.insert(getTableForOperation(Operation.SFTP), null, values);
    }

    public void updateSsh(String connectionName, String oldConnectionName, String path,
                          String sshKeyName, String sshKey) {

        SQLiteDatabase database = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, connectionName);
        values.put(COLUMN_PATH, path);
        if(sshKeyName != null && sshKey != null) {
            values.put(COLUMN_PRIVATE_KEY_NAME, sshKeyName);
            values.put(COLUMN_PRIVATE_KEY, sshKey);
        }

        database.update(getTableForOperation(Operation.SFTP), values, String.format("%s=?", COLUMN_NAME),
                new String[]{oldConnectionName});
    }

    public LinkedList<String> getHistoryLinkedList() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(getTableForOperation(Operation.HISTORY), null,
                null, null, null, null, null);

        LinkedList<String> paths = new LinkedList<>();
        boolean hasNext = cursor.moveToFirst();
        while (hasNext) {
            paths.push(cursor.getString(cursor.getColumnIndex(COLUMN_PATH)));
            hasNext = cursor.moveToNext();
        }
        cursor.close();

        return paths;
    }

    public LinkedList<String> getLibraryLinkedList(String lib) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(getTableForOperation(Operation.LIB), null,
                COLUMN_LIB +" = ? ", new String[]{ lib}, null, null,null);

        LinkedList<String> paths = new LinkedList<>();
        boolean hasNext = cursor.moveToFirst();
        while (hasNext) {
            paths.push(cursor.getString(cursor.getColumnIndex(COLUMN_PATH)));
            hasNext = cursor.moveToNext();
        }
        cursor.close();

        return paths;
    }

    public ArrayList<String> getLibraryList(){
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(getTableForOperation(Operation.LIB), null,
                null, null, null, null, COLUMN_LIB);
        ArrayList<String> paths = new ArrayList<>();
        boolean hasNext = cursor.moveToFirst();
        while (hasNext) {
            paths.add(cursor.getString(cursor.getColumnIndex(COLUMN_PATH)));
            hasNext = cursor.moveToNext();
        }
        cursor.close();
        return paths;
    }

    public ArrayList<String> getLibraryRootList(){
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(getTableForOperation(Operation.LIB),
                new String[]{COLUMN_LIB,COLUMN_NAME},
                null, null, COLUMN_LIB, null,null);
        ArrayList<String> paths = new ArrayList<>();
        boolean hasNext = cursor.moveToFirst();
        while (hasNext) {
            paths.add(cursor.getString(cursor.getColumnIndex(COLUMN_LIB)));
            hasNext = cursor.moveToNext();
        }
        cursor.close();
        return paths;
    }

    public ArrayList<String[]> getLibraryChildList(String lib){
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(getTableForOperation(Operation.LIB),null,
//             COLUMN_NAME+" LIKE ? AND "+
//                     COLUMN_PATH+" LIKE ? AND " +

                COLUMN_LIB +" = ? ", new String[]{ lib}, null, null,null);
                //     COLUMN_LIB +" = ? ", new String[]{lib}, null, null,null);
        ArrayList<String[]> paths = new ArrayList<>();
        boolean hasNext = cursor.moveToFirst();
        while (hasNext) {
            String[] path=new String[2];
            path[0]=cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
            path[1]=cursor.getString(cursor.getColumnIndex(COLUMN_PATH));
            paths.add(path);
            hasNext = cursor.moveToNext();
        }
        cursor.close();
        return paths;
    }

    // 刷新文件库的空滤镜
    public void removeEmptyPath(){

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.beginTransaction();

        Cursor cursor = sqLiteDatabase.query(true,TABLE_LIB, new String[]{COLUMN_NAME,COLUMN_PATH,COLUMN_LIB},
                null,null,
                null, null, null,null);

        ArrayList<String[]> paths = new ArrayList<>();
        boolean hasNext = cursor.moveToFirst();
        while (hasNext) {
            String path = cursor.getString(cursor.getColumnIndex(COLUMN_PATH));
            File f = new File(path);
            if (!f.exists())
                sqLiteDatabase.delete(TABLE_LIB, COLUMN_PATH + "=?", new String[]{path});
            hasNext = cursor.moveToNext();
        }
        cursor.close();

        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();

    }

    // 刷新文件库的重复路径
    public void rebuildLibraryAllList(Boolean isExist){

        ArrayList<String[]> list=getLibraryAllList(isExist);
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.beginTransaction();

        sqLiteDatabase.execSQL("DROP TABLE "+TABLE_LIB);
        String queryLib="CREATE TABLE IF NOT EXISTS " + TABLE_LIB  + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_PATH + " TEXT,"
                + COLUMN_LIB + " TEXT"
                + ")";
        sqLiteDatabase.execSQL(queryLib);

        for (String[] path:list) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_NAME, path[0]);
            contentValues.put(COLUMN_PATH, path[1]);
            contentValues.put(COLUMN_LIB, path[2]);
            sqLiteDatabase.insert(TABLE_LIB,null,contentValues );
        }
        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();

    }
    // 获取文件库的全部内容
    public ArrayList<String[]> getLibraryAllList(Boolean isExist){
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        /**
         * 参数一：是否去重
         * 参数二：表名
         * 参数三：columns 表示查询的字段,new String[]{MODEL}表示查询该表当中的模式（也表示查询的结果）
         * 参数思：selection表示查询的条件，PHONE_NUMBER+" = ?" 表示根据手机号去查询模式
         * 参数五：selectionArgs 表示查询条件对应的值,new String[]{phoneNumber}表示查询条件对应的值
         * 参数六：String groupBy 分组
         * 参数七：String having
         * 参数八：orderBy 表示根据什么排序,
         * 参数九：limit  限制查询返回的行数，NULL表示无限制子句
         */

        Cursor cursor = sqLiteDatabase.query(true,TABLE_LIB, new String[]{COLUMN_NAME,COLUMN_PATH,COLUMN_LIB},
                null,null,
                null, null, null,null);

        ArrayList<String[]> paths = new ArrayList<>();
        boolean hasNext = cursor.moveToFirst();
        while (hasNext) {
            String[] path=new String[3];
            path[0]=cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
            path[1]=cursor.getString(cursor.getColumnIndex(COLUMN_PATH));
            path[2]=cursor.getString(cursor.getColumnIndex(COLUMN_LIB));
            hasNext = cursor.moveToNext();
            if(isExist){
                File f=new File(path[1]);
                if(!f.exists()){
                   continue;
                }
            }
            paths.add(path);

        }
        cursor.close();
        return paths;
    }

    public ConcurrentRadixTree<VoidValue> getHiddenFilesConcurrentRadixTree() {
        ConcurrentRadixTree<VoidValue> paths = new ConcurrentRadixTree<>(new DefaultCharArrayNodeFactory());

        Cursor cursor = getReadableDatabase().query(getTableForOperation(Operation.HIDDEN), null,
                null, null, null, null, null);
        boolean hasNext = cursor.moveToFirst();
        while (hasNext) {
            paths.put(cursor.getString(cursor.getColumnIndex(COLUMN_PATH)), VoidValue.SINGLETON);
            hasNext = cursor.moveToNext();
        }
        cursor.close();

        return paths;
    }

    public ArrayList<String> getListViewList() {
        return getPath(Operation.LIST);
    }

    public ArrayList<String> getGridViewList() {
        return getPath(Operation.GRID);
    }

    public ArrayList<String[]> getBookmarksList() {

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(getTableForOperation(Operation.BOOKMARKS), null,
                null, null, null, null, null);

        boolean hasNext = cursor.moveToFirst();
        ArrayList<String[]> row = new ArrayList<>();
        while (hasNext) {
            row.add(new String[] {
                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_PATH))
            });
            hasNext = cursor.moveToNext();
        }
        cursor.close();
        return row;
    }

    public ArrayList<String[]> getSmbList() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(getTableForOperation(Operation.SMB), null,
                null, null, null, null, null);
        boolean hasNext = cursor.moveToFirst();
        ArrayList<String[]> row = new ArrayList<>();
        while (hasNext) {
            try {
                row.add(new String[] {
                        cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                        SmbUtil.getSmbDecryptedPath(context, cursor.getString(cursor.getColumnIndex(COLUMN_PATH)))
                });
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();

                // failing to decrypt the path, removing entry from database
                Toast.makeText(context, context.getString(R.string.failed_smb_decrypt_path), Toast.LENGTH_LONG).show();
                removeSmbPath(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                        "");
                continue;
            }
            hasNext = cursor.moveToNext();
        }
        cursor.close();
        return row;
    }

    public List<String[]> getSftpList()
    {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(getTableForOperation(Operation.SFTP),
                new String[]{COLUMN_NAME,COLUMN_PATH},
                null, null, null, null, COLUMN_ID);

        boolean hasNext = cursor.moveToFirst();
        ArrayList<String[]> retval = new ArrayList<String[]>();
        while(hasNext)
        {
            String path = SshClientUtils.decryptSshPathAsNecessary(cursor.getString(cursor.getColumnIndex(COLUMN_PATH)));

            if(path == null) {
                Log.e("ERROR", "Error decrypting path: " + cursor.getString(cursor.getColumnIndex(COLUMN_PATH)));

                // failing to decrypt the path, removing entry from database
                Toast.makeText(context,
                        context.getString(R.string.failed_smb_decrypt_path),
                        Toast.LENGTH_LONG).show();
//                    removeSmbPath(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
//                            "");
                continue;
            } else {
                retval.add(new String[]{
                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                    path
                });
            }
            hasNext = cursor.moveToNext();
        }
        cursor.close();
        return retval;
    }

    public String getSshHostKey(String uri)
    {
        uri = SshClientUtils.encryptSshPathAsNecessary(uri);
        if(uri != null)
        {
            SQLiteDatabase sqLiteDatabase = getReadableDatabase();

            Cursor result = sqLiteDatabase.query(TABLE_SFTP, new String[]{COLUMN_HOST_PUBKEY},
                    COLUMN_PATH + " = ?", new String[]{uri},
                    null, null, null);
            if(result.moveToFirst())
            {
                String retval = result.getString(0);
                result.close();
                return retval;
            }
            else
            {
                result.close();
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    public String getSshAuthPrivateKeyName(String uri)
    {
        return getSshAuthPrivateKeyColumn(uri, COLUMN_PRIVATE_KEY_NAME);
    }

    public String getSshAuthPrivateKey(String uri)
    {
        return getSshAuthPrivateKeyColumn(uri, COLUMN_PRIVATE_KEY);
    }

    private String getSshAuthPrivateKeyColumn(String uri, String columnName) {
        //If connection is using key authentication, no need to decrypt the path at all
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor result = sqLiteDatabase.query(TABLE_SFTP, new String[]{columnName},
                COLUMN_PATH + " = ?", new String[]{uri},
                null, null, null);
        if(result.moveToFirst())
        {
            try {
                return result.getString(0);
            }
            finally {
                result.close();
            }
        }
        else {
            result.close();
            return null;
        }
    }

    private void removeBookmarksPath(String name, String path) {

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        sqLiteDatabase.delete(TABLE_BOOKMARKS, COLUMN_NAME + " = ? AND " + COLUMN_PATH + " = ?",
                new String[] {name, path});
    }

    private void removelib(String name,String path,String lib){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        sqLiteDatabase.delete(TABLE_LIB, COLUMN_NAME + " = ? AND " + COLUMN_PATH + " = ? AND "+COLUMN_LIB+" = ?",
                new String[] {name, path, lib});
    }

   public void remove_a_lib(String path,String lib){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        if(path.length()>0){
            sqLiteDatabase.delete(TABLE_LIB,   COLUMN_PATH + " = ? AND "+COLUMN_LIB+" = ?",
                    new String[] { path, lib});
        }else{
            sqLiteDatabase.delete(TABLE_LIB,  COLUMN_LIB+" = ?",
                    new String[] {lib});

        }


    }

   public void clearlib(){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

      //  sqLiteDatabase.delete(TABLE_LIB);
        sqLiteDatabase.execSQL("DROP TABLE "+TABLE_LIB);
        String queryLib="CREATE TABLE IF NOT EXISTS " + TABLE_LIB  + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_PATH + " TEXT,"
                + COLUMN_LIB + " TEXT"
                + ")";
        sqLiteDatabase.execSQL(queryLib);
/* 暂不启用libname表
       sqLiteDatabase.execSQL("DROP TABLE "+TABLE_LIBNAME);
        String queryLibName="CREATE TABLE IF NOT EXISTS " + TABLE_LIBNAME + " ("
               + COLUMN_ID + " INTEGER PRIMARY KEY,"
               + COLUMN_LIB + " TEXT,"
               + COLUMN_LIBNAME + " TEXT"
               + ")";
       sqLiteDatabase.execSQL(queryLibName);*/
    }
    public void clearlib(String lib){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.delete(TABLE_LIB, COLUMN_LIB+" = ?",
                new String[] {lib});

    //    sqLiteDatabase.delete(TABLE_LIBNAME, COLUMN_LIB +   " = ?",  new String[] {lib});


    }


    /**
     * Remove SMB entry
     * @param path the path we get from saved runtime variables is a decrypted, to remove entry,
     *             we must encrypt it's password fiend first first
     */
    private void removeSmbPath(String name, String path) {

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        try {
            if (path.equals("")) {
                // we don't have a path, remove the entry with this name
                throw new IOException();
            }

            sqLiteDatabase.delete(TABLE_SMB, COLUMN_NAME + " = ? AND " + COLUMN_PATH + " = ?",
                    new String[] {name, SmbUtil.getSmbEncryptedPath(context, path)});
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            // force remove entry, we end up deleting all entries with same name

            sqLiteDatabase.delete(TABLE_SMB, COLUMN_NAME + " = ?",
                    new String[] {name});
        }
    }

    private void removeSftpPath(String name, String path) {

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        try
        {
            if (path.equals("")) {
                // we don't have a path, remove the entry with this name
                throw new IOException();
            }

            sqLiteDatabase.delete(TABLE_SFTP, COLUMN_NAME + " = ? AND " + COLUMN_PATH + " = ?",
                    new String[] {name, SshClientUtils.encryptSshPathAsNecessary(path)});

        }
        catch (IOException e)
        {
            e.printStackTrace();
            // force remove entry, we end up deleting all entries with same name
            sqLiteDatabase.delete(TABLE_SFTP, COLUMN_NAME + " = ?",
                    new String[] {name});
        }
    }

    public void renameBookmark(String oldName, String oldPath, String newName, String newPath) {
        renamePath(Operation.BOOKMARKS, oldName, oldPath, newName, newPath);
    }

    public void renameSMB(String oldName, String oldPath, String newName, String newPath) {
        renamePath(Operation.SMB, oldName, oldPath, newName, newPath);
    }

    private void setPath(Operation operation, String path) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_PATH, path);

        sqLiteDatabase.insert(getTableForOperation(operation), null, contentValues);
    }

    private void setPath(Operation operation, String name, String path) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_PATH, path);

        sqLiteDatabase.insert(getTableForOperation(operation), null, contentValues);
    }

    // 普通的存储数据到lib表的方法
    private void setPath(Operation operation, String name, String path,String lib) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_PATH, path);
        contentValues.put(COLUMN_LIB, lib);;
        sqLiteDatabase.insert(getTableForOperation(operation), null, contentValues);
    }


    //新的存储数据到lib表的方法，防止插入重复数据，并且不需要使用operation类了。
    public void updateLibPath(String name, String path , String lib) {

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        sqLiteDatabase.delete(TABLE_LIB, COLUMN_NAME + " = ? AND " + COLUMN_PATH + " = ? AND "+COLUMN_LIB+" = ? ",
                new String[] {name, path, lib});
        sqLiteDatabase.delete(TABLE_LIB, COLUMN_NAME + " = ? AND " + COLUMN_PATH + " = ? AND "+COLUMN_LIB+" = ? ",
                new String[] {"", "", lib});

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_PATH, path);
        contentValues.put(COLUMN_LIB, lib);
        sqLiteDatabase.insert(TABLE_LIB,null,contentValues );

    }

//批量添加文件列表到库并自动去重去空
    public void updateLibPath(String[] paths , String lib) {

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
       sqLiteDatabase.beginTransaction();
        sqLiteDatabase.delete(TABLE_LIB, COLUMN_NAME + " = ? AND " + COLUMN_PATH + " = ? AND "+COLUMN_LIB+" = ? ",
                new String[] {"", "", lib});

        for (String path : paths) {
            String name = path.replaceFirst("/$", "").replaceAll(".*/", "");

            sqLiteDatabase.delete(TABLE_LIB, COLUMN_NAME + " = ? AND " + COLUMN_PATH + " = ? AND "+COLUMN_LIB+" = ? ",
                    new String[] {name, path, lib});

            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_NAME, name);
            contentValues.put(COLUMN_PATH, path);
            contentValues.put(COLUMN_LIB, lib);
            sqLiteDatabase.insert(TABLE_LIB,null,contentValues );
        }
        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();
    }

    //新的存储数据到lib表的方法，防止插入重复数据，并且不需要使用operation类了。
    //然而并不能用，因为主键是_ID
    public void updateLibPath0(String name, String path , String lib) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_PATH, path);
        contentValues.put(COLUMN_LIB, lib);
        sqLiteDatabase.replace(TABLE_LIB,null,contentValues );
    }

    private ArrayList<String> getPath(Operation operation) {

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(getTableForOperation(operation), null,
                null, null, null, null, null);

        ArrayList<String> paths = new ArrayList<>();

        switch (operation) {
            case LIST:
            case GRID:
                boolean hasNext = cursor.moveToFirst();
                while (hasNext) {
                    paths.add(cursor.getString(cursor.getColumnIndex(COLUMN_PATH)));
                    hasNext = cursor.moveToNext();
                }
                cursor.close();
                return paths;
            default:
                return null;
        }
    }

    private void removePath(Operation operation, String path) {

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        sqLiteDatabase.delete(getTableForOperation(operation), COLUMN_PATH + "=?",
                new String[] {path});
    }

    public void clearTable(Operation table) {
        getWritableDatabase().delete(getTableForOperation(table), null, null);
    }

    private void renamePath(Operation operation, String name, String path) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_PATH, path);

        sqLiteDatabase.update(getTableForOperation(operation), contentValues,
                COLUMN_PATH + "=?", new String[] {name});
    }

    private void renamePath(Operation operation, String oldName, String oldPath,
                               String newName, String newPath) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, newName);
        contentValues.put(COLUMN_PATH, newPath);

        sqLiteDatabase.update(getTableForOperation(operation), contentValues, COLUMN_NAME
                + "=? AND " + COLUMN_PATH + "=?", new String[] {oldName, oldPath});
    }

    /**
     * Return table string for corresponding {@link Operation}
     */
    private String getTableForOperation(Operation operation) {

        switch (operation) {
            case HISTORY:
                return TABLE_HISTORY;
            case HIDDEN:
                return TABLE_HIDDEN;
            case LIST:
                return TABLE_LIST;
            case GRID:
                return TABLE_GRID;
            case BOOKMARKS:
                return TABLE_BOOKMARKS;
            case LIB:
                return TABLE_LIB;
            case SMB:
                return TABLE_SMB;
            case SFTP:
                return TABLE_SFTP;
            default:
                return null;
        }
    }
}
