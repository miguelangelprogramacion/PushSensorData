package world.we.deserve.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;
import java.util.List;

import world.we.deserve.DTO.SensorAccelerometer;

/**
 * Created by miguelangelprogramacion on 10/18/15.
 */
public class SensorAccelerometerSQLiteHelper extends SQLiteOpenHelper {

    // database version
    private static final int database_VERSION = 1;
    // database name
    private static final String database_NAME = "SensorDB";
    private static final String table_SENSORACCELEROMETER = "accelerometer";
    private static final String accelerometer_ID = "id";
    private static final String accelerometer_VALUE = "value";
    private static final String accelerometer_DATE = "date";

    private static final String[] COLUMNS = { accelerometer_ID, accelerometer_DATE, accelerometer_VALUE };

    public SensorAccelerometerSQLiteHelper(Context context) {
        super(context, database_NAME, null, database_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create book table
        String CREATE_ACCELEROMETER_TABLE = "CREATE  TABLE \"accelerometer\" (\"id\" INTEGER PRIMARY KEY  NOT NULL , \"value\" DOUBLE NOT NULL , \"date\" DATETIME)";
        db.execSQL(CREATE_ACCELEROMETER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
        // drop books table if already exists
        db.execSQL("DROP TABLE IF EXISTS "+table_SENSORACCELEROMETER);
        this.onCreate(db);
    }

    public void createAccelerometerValue(SensorAccelerometer sa) {
        // get reference of the BookDB database
        SQLiteDatabase db = this.getWritableDatabase();

        // make values to be inserted
        ContentValues values = new ContentValues();
        values.put(accelerometer_VALUE, sa.getValue());
        values.put(accelerometer_DATE, sa.getDatetime());

        // insert book
        db.insert(table_SENSORACCELEROMETER, null, values);

        // close database transaction
        db.close();
    }

    public List getAllValues() {
        List values = new LinkedList();

        // select book query
        String query = "SELECT  * FROM " + table_SENSORACCELEROMETER;

        // get reference of the BookDB database
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // parse all results
        SensorAccelerometer sa = null;
        if (cursor.moveToFirst()) {
            do {
                sa = new SensorAccelerometer();
                sa.setIdSensorAccelerometer(Integer.parseInt(cursor.getString(0)));
                sa.setValue(Double.parseDouble(cursor.getString(1)));
                sa.setDatetime(Long.parseLong(cursor.getString(2)));

                // Add book to books
                values.add(sa);
            } while (cursor.moveToNext());
        }
        return values;
    }
}
