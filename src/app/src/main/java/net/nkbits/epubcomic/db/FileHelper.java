package net.nkbits.epubcomic.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nakayama on 5/29/16.
 */
public class FileHelper {
    public static final String FILES_TABLE = "files";
    public static final String ID_COLUMN = "id";
    public static final String PATH_COLUMN = "path";
    public static final String READ_COLUMN = "read";
    public static final String ADDED_COLUMN = "added";
    public static final String LAST_READ = "last_read";
    public static final String FAVORITE_COLUMN = "favorite";
    public static final String TYPE_COLUMN = "type";

    public static final String FILES_TABLE_CREATE =
            "CREATE TABLE " + FILES_TABLE +" ( " +
                    ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PATH_COLUMN + " TEXT NOT NULL, " +
                    READ_COLUMN + " BOOLEAN NOT NULL DEFAULT 0, " +
                    ADDED_COLUMN + " DATE NOT NULL, " +
                    LAST_READ + " DATE NULL," +
                    TYPE_COLUMN + " BOOLEAN NOT NULL, " +
                    FAVORITE_COLUMN + " BOOLEAN NOT NULL DEFAULT 0)";

    private static final String INSERT_FILE =
            "INSERT INTO " +
                    FILES_TABLE + " (" +
                    PATH_COLUMN + ", " +
                    READ_COLUMN + ", " +
                    ADDED_COLUMN + ", " +
                    LAST_READ + ", " +
                    TYPE_COLUMN + ", " +
                    FAVORITE_COLUMN + ") values (?, 0, ?, NULL, ?, 0)";

    private static final String UPDATE_READ =
            "UPDATE " +
                    FILES_TABLE + " SET " +
                    READ_COLUMN + " = ? " +
                    "WHERE " +
                    ID_COLUMN + " = ?";

    private static final String UPDATE_LAST_READ =
            "UPDATE " +
                    FILES_TABLE + " SET " +
                    LAST_READ + " = ? " +
                    "WHERE " +
                    ID_COLUMN + " = ?";

    private SQLiteDatabase database;
    private SQLiteStatement insertQuery;
    private SQLiteStatement updateReadQuery;
    private SQLiteStatement updateLastReadQuery;

    public FileHelper(SQLiteDatabase database){
        this.database = database;
        insertQuery = database.compileStatement(INSERT_FILE);
        updateReadQuery = database.compileStatement(UPDATE_READ);
        updateLastReadQuery = database.compileStatement(UPDATE_LAST_READ);
    }

    public long insertFile(String path, boolean isBook) {
        insertQuery.bindString(1, path);
        insertQuery.bindLong(2, new Date().getTime());
        insertQuery.bindLong(3, isBook ? 0:1);
        return insertQuery.executeInsert();
    }

    public void updateLastRead(int id) {
        updateLastReadQuery.bindLong(1, new Date().getTime());
        updateLastReadQuery.bindLong(2, id);
        updateLastReadQuery.execute();
    }

    public boolean existsFile(String path) {
        final Cursor cursor = database.query(FILES_TABLE, new String[]{PATH_COLUMN}, PATH_COLUMN + " = ? ", new String[]{path}, null, null, null);
        final int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public void deleteFiles() {
        database.delete(FILES_TABLE, null, null);
    }

    public String selectMostRecentFile() {
        Cursor cursor = database.query(FILES_TABLE, new String[]{PATH_COLUMN},
                null, null, null, null, LAST_READ + " desc", "1");

        if (cursor.moveToFirst()) {
            String file = cursor.getString(0);
            cursor.close();
            return file;
        } else {
            cursor.close();
            return null;
        }
    }

    public List<String> getRecentFiles() {
        ArrayList<String> files = new ArrayList<String>();

        Cursor cursor = database.query(FILES_TABLE, new String[]{PATH_COLUMN},
                null, null, null, null, LAST_READ + " desc", "10");
        if (cursor.moveToFirst()) {
            do {
                String path = cursor.getString(0);
                files.add(path);
            } while (cursor.moveToNext());

        }

        cursor.close();
        return files;
    }
}
