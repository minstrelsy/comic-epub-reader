/*******************************************************************************
 * Copyright 2009 Robot Media SL
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.nkbits.epubcomic.db;

import java.util.*;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class DBHelper {
    private Context mContext;
    private SQLiteDatabase mDB;

    private SQLiteStatement mInsertFile;
    private SQLiteStatement mUpdateRead;
    private SQLiteStatement mUpdateLastRead;

    private static final String INSERT_FILE =
            "INSERT INTO " +
                    DBOpenHelper.FILES_TABLE + " (" +
                    DBOpenHelper.PATH_COLUMN + ", " +
                    DBOpenHelper.READ_COLUMN + ", " +
                    DBOpenHelper.ADDED_COLUMN + ", " +
                    DBOpenHelper.LAST_READ + ", " +
                    DBOpenHelper.TYPE_COLUMN + ", " +
                    DBOpenHelper.FAVORITE_COLUMN + ") values (?, 0, ?, NULL, ?, 0)";

    private static final String UPDATE_READ =
            "UPDATE " +
                    DBOpenHelper.FILES_TABLE + " SET " +
                    DBOpenHelper.READ_COLUMN + " = ? " +
                    "WHERE " +
                    DBOpenHelper.ID_COLUMN + " = ?";

    private static final String UPDATE_LAST_READ =
            "UPDATE " +
                    DBOpenHelper.FILES_TABLE + " SET " +
                    DBOpenHelper.LAST_READ + " = ? " +
                    "WHERE " +
                    DBOpenHelper.ID_COLUMN + " = ?";

    public DBHelper(Context context) {
        mContext = context;
        final DBOpenHelper openHelper = new DBOpenHelper(mContext);
        mDB = openHelper.getWritableDatabase();
        mInsertFile = mDB.compileStatement(INSERT_FILE);
        mUpdateRead = mDB.compileStatement(UPDATE_READ);
        mUpdateLastRead = mDB.compileStatement(UPDATE_LAST_READ);
    }

    public long insertFile(String path, boolean isBook) {
        mInsertFile.bindString(1, path);
        mInsertFile.bindLong(2, new Date().getTime());
        mInsertFile.bindLong(3, isBook ? 0:1);
        return mInsertFile.executeInsert();
    }

    public void updateLastRead(int id) {
        mUpdateLastRead.bindLong(1, new Date().getTime());
        mUpdateLastRead.bindLong(2, id);
        mUpdateLastRead.execute();
    }

    public boolean existsFile(String path) {
        final Cursor cursor = mDB.query(DBOpenHelper.FILES_TABLE, new String[]{DBOpenHelper.PATH_COLUMN}, DBOpenHelper.PATH_COLUMN + " = ? ", new String[]{path}, null, null, null);
        final int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public void deleteFiles() {
        mDB.delete(DBOpenHelper.FILES_TABLE, null, null);
    }

    public String selectMostRecentFile() {
        Cursor cursor = mDB.query(DBOpenHelper.FILES_TABLE, new String[]{DBOpenHelper.PATH_COLUMN},
                null, null, null, null, DBOpenHelper.LAST_READ + " desc", "1");

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

        Cursor cursor = mDB.query(DBOpenHelper.FILES_TABLE, new String[]{DBOpenHelper.PATH_COLUMN},
                null, null, null, null, DBOpenHelper.LAST_READ + " desc", "10");
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
