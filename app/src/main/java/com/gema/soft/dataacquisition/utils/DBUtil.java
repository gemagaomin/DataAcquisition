package com.gema.soft.dataacquisition.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DBUtil {
    private static DBUtil dbUtil;
    public SQLiteDatabase db;
    public static final String DB_NULL = "";
    private static final String title = "CREATE TABLE ";
    public static Map<String, String> tableNameMap;

    /**
     * 新添加表方法
     * @return
     */
    public boolean addNewTableDatabase(){
        boolean result=false;
        return result;
    }
    public boolean updateDatabase(String tableName, String createdSql, String insertSql){
        // TODO Auto-generated method stub
        boolean result=true;
        HaveData();
        createTable();
        db.beginTransaction();
        try{
            //改名数据库表
            db.execSQL("alter table "+tableName+" rename to "+tableName+2);
            //新建表单
            db.execSQL("create table "+tableName+" "+createdSql);

            //插入原有的数据
            db.execSQL("insert into "+tableName+ " "+insertSql+" from"+tableName+2);
            //如果增加了列属性，则使用双引号”” 来补充原来不存在的数据
            //删除临时表单
            db.execSQL("drop table "+tableName+2);
            result=true;
        }catch (Exception e){
            db.execSQL("drop table "+tableName);
            db.execSQL("alter table "+tableName+2+" rename to "+tableName);
            result=false;
        }finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        return result;
    }


    private Map<String, String> hadTableNameMap = new HashMap();

    private DBUtil() {
        if (db == null) {
            String path = DataUtil.getInstance().DB_PATH;
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            try{
                db = SQLiteDatabase.openOrCreateDatabase(path + "/pwcz.db", null);
                HaveData();
            }catch (Exception e){
               MyLog.d("SQLiteDatabase.openOrCreateDatabase 异常");
            }
        }
    }

    public static DBUtil getInstance() {
        if (dbUtil == null) {
            synchronized (DBUtil.class) {
                if (dbUtil == null) {
                    dbUtil = new DBUtil();
                    if(tableNameMap==null||tableNameMap.size()<=0){
                        tableNameMap = new HashMap<String, String>() {
                        };
                        tableNameMap.put(DataUtil.TableNameEnum.WORK.toString(), title + DataUtil.TableNameEnum.WORK.toString() + "(" +
                                "  `workid` varchar(60) ," +
                                "  `bluetoothconstatus` varchar(5) NOT NULL," +
                                "  `workstatus` varchar(5) NOT NULL," +
                                "  `starttime` varchar(50) ," +
                                "  `endtime` varchar(50) ," +
                                "  `traininfo` varchar(255) ," +
                                "  `fileName`  varchar(100) ," +
                                "  `bluetoothmas`  varchar(100)" +
                                ")");

                        tableNameMap.put(DataUtil.TableNameEnum.SUBMITFILE.toString(), title + DataUtil.TableNameEnum.SUBMITFILE.toString() +
                                "(" +
                                "'fileid' varchar(60)  PRIMARY KEY ," +
                                "'filename'  varchar(255) NOT NULL ," +
                                "'filepath'  varchar(255) NOT NULL ," +
                                "'filetime'  varchar(255) NOT NULL ," +
                                "'filestatus'  varchar(30) NOT NULL ," +
                                "'userid'  varchar(30) NOT NULL ," +
                                "'filetype'  varchar(30) NOT NULL ," +
                                "'workid'  varchar(30) NOT NULL ," +
                                "'filerank' varchar(30) NOT NULL"
                                + ")");
                        tableNameMap.put(DataUtil.TableNameEnum.TRAIN_TYPE.toString(), title + DataUtil.TableNameEnum.TRAIN_TYPE.toString() + "(" +
                                "traintypeid char(3) ," +
                                "traintypename varchar(16) NOT NULL )");
                        tableNameMap.put(DataUtil.TableNameEnum.PERSON.toString(), title + DataUtil.TableNameEnum.PERSON.toString() + "(" +
                                "`personid`  varchar(20) ," +
                                "`personname`  varchar(200)   ," +
                                "`band`  varchar(20)   ," +
                                "`depname`  varchar(20)   ," +
                                "`unitid`  varchar(20)   ," +
                                "`unitname`  varchar(20)"
                                + ")");
                    }
                }
            }
        }
        return dbUtil;
    }

    public boolean delete(String table, String whereClause, String[] whereArgs) {
        int i = db.delete(table, whereClause, whereArgs);
        if (i >= 0) {
            return true;
        }
        return false;
    }

    public void deleteAll(String table) {
        db.execSQL("delete from " + table);
    }

    public boolean insert(String table, ContentValues values) {
        long ret = db.insert(table, DBUtil.DB_NULL, values);
        if (ret != -1) {
            return true;
        }
        return false;
    }

    public boolean update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        int i = db.update(table, values, whereClause, whereArgs);
        if (i > 0) {
            return true;
        }
        return false;
    }

    public Cursor select(String sql, String[] selectionArgs) {
        return db.rawQuery(sql, selectionArgs);
    }

    public Cursor selectALL(String tableName) {
        return db.rawQuery("select * from " + tableName, null);
    }

    public void HaveData() {
        Cursor cursor;
        cursor = db.rawQuery("select name from sqlite_master where type='table' ", null);
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            hadTableNameMap.put(name, name);
        }
    }

    public void createTable() {
        if(hadTableNameMap==null||hadTableNameMap.size()<=0){
            HaveData();
        }
        for (Map.Entry<String, String> o : tableNameMap.entrySet()
        ) {
            if (TextUtils.isEmpty(hadTableNameMap.get(o.getKey()))) {
                db.execSQL(o.getValue());
            }
        }
    }

    public Cursor getUpdateFileList(){
        return dbUtil.select("select * from " + DataUtil.TableNameEnum.SUBMITFILE.toString() + " where filestatus=? group by workid,filerank,filetime ORDER BY filerank ,filetime ASC ", new String[]{FileUtil.FILE_STATUS_WAIT_UPLOADED});
    }


}
