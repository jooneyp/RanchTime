package com.goodwebple.jooney.ranchtime;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by j8n3y on 15. 1. 3..
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DBHelper";

    private static final int DATABASE_VERSION = 2;
    private static final String DB_NAME = "data.db";
    private static File DB_FILE;
    private static File DOWN_FILE;
    private static final String DICTIONARY_TABLE_NAME = "wordlist";
    private static final String DICTIONARY_TABLE_CREATE =
            "CREATE TABLE wordlist (" +
                    "idx INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "word TEXT, " +
                    "mean TEXT, " +
                    "is_correct_memorize_mode BOOLEAN, " +
                    "is_appeared_memorize_mode BOOLEAN, " +
                    "got_wrong_memorize_mode_cnt INTEGER, " +
                    "is_correct_challenge_mode BOOLEAN, " +
                    "is_appeared_challenge_mode BOOLEAN, " +
                    "got_wrong_challenge_mode_cnt INTEGER" +
                    ");";

    private SQLiteDatabase mDataBase;
    private final Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 2);
        Log.i("DBNAME", DB_NAME);
        DOWN_FILE = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + "/" + DB_NAME).getAbsoluteFile();
        Log.i("DOWNFILE", DOWN_FILE.getPath());
        DB_FILE = context.getDatabasePath(DB_NAME).getAbsoluteFile();
        Log.i("DBFILE", DB_FILE.getPath());
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        if(isExternalStorageWritable()) {
//            db.execSQL(DICTIONARY_TABLE_CREATE);
//            try {
//                createDataBase();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            Log.i(TAG, "Create Table Finished");
//        } else {
//            Log.i(TAG, "External Storage Write Denied");
//        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onUpgrade, onCreate");
        onCreate(db);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void createDataBase() throws IOException {
        boolean mDataBaseExist = checkDataBase();
        if(!mDataBaseExist) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDataBase();
                Log.e(TAG, "createDataBase() : database created");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_FILE + DB_NAME);
        Log.i("dbFile", dbFile + "   " + dbFile.exists());
        return dbFile.exists();
    }

    private void copyDataBase() throws IOException {
        InputStream mInput = new FileInputStream(DOWN_FILE);
        OutputStream mOutput = new FileOutputStream(DB_FILE);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0) {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }


    public boolean openDataBase() throws SQLException {
        mDataBase = SQLiteDatabase.openDatabase(DB_FILE.getPath(), null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return mDataBase != null;
    }

    @Override
    public synchronized void close() {
        if(mDataBase != null)
            mDataBase.close();
        super.close();
    }

    /* USAGE
    DatabaseHelper mDbHelper = new DatabaseHelper(urContext);
    mDbHelper.createDatabase();
    mDbHelper.open();

    Cursor testdata = mDbHelper.getTestData();

    mDbHelper.close();
     */

    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {
                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(Exception ex){

            Log.d("printing exception", ex.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }
}
