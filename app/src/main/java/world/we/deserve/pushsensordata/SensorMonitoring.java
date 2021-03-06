package world.we.deserve.pushsensordata;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.hardware.SensorEventListener;

import java.util.Calendar;
import java.util.List;

import world.we.deserve.DAO.SensorAccelerometerSQLiteHelper;
import world.we.deserve.DTO.SensorAccelerometer;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SensorMonitoring extends AppCompatActivity implements SensorEventListener{


    private float lastX, lastY, lastZ;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float deltaXMax = 0;
    private float deltaYMax = 0;
    private float deltaZMax = 0;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;

    private float vibrateThreshold = 0;

    private TextView currentX, currentY, currentZ, maxX, maxY, maxZ;

    public Vibrator v;

    private SensorAccelerometerSQLiteHelper db;

    DateTime lastUpdate = new DateTime();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sensor_monitoring);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        initializeViews();

        // open the database of the application context
        db = new SensorAccelerometerSQLiteHelper(getApplicationContext());


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            vibrateThreshold = accelerometer.getMaximumRange() / 2;
        } else {
            // fai! we dont have an accelerometer!
        }

        //initialize vibration
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        Button buttonStore = (Button)findViewById(R.id.buttonStore);

        buttonStore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.i("buttonStore", "Click");
                SensorAccelerometer sa = new SensorAccelerometer();
                sa.setValue(1d);
                sa.setDatetime(1l);
                db.createAccelerometerValue(sa);
            }
        });

        Button buttonQueryAll = (Button)findViewById(R.id.buttonQueryAll);

        buttonQueryAll.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.i("buttonQueryAll", "Click");
                List values = db.getAllValues();
                for (Object objeto:values) {
                    Log.i("values", objeto.toString());
                }
            }
        });

    }

    ///////////////////////

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    ////////////////////////////////////////////////////////////////

    public void initializeViews() {
        currentX = (TextView) findViewById(R.id.currentX);
        currentY = (TextView) findViewById(R.id.currentY);
        currentZ = (TextView) findViewById(R.id.currentZ);

        maxX = (TextView) findViewById(R.id.maxX);
        maxY = (TextView) findViewById(R.id.maxY);
        maxZ = (TextView) findViewById(R.id.maxZ);
    }

    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // clean current values
        displayCleanValues();
        // display the current x,y,z accelerometer values
        displayCurrentValues();
        // display the max x,y,z accelerometer values
        displayMaxValues();

        // get the change of the x,y,z values of the accelerometer
        deltaX = Math.abs(lastX - event.values[0]);
        deltaY = Math.abs(lastY - event.values[1]);
        deltaZ = Math.abs(lastZ - event.values[2]);

        // if the change is below 2, it is just plain noise
        if (deltaX < 2)
            deltaX = 0;
        if (deltaY < 2)
            deltaY = 0;
        if (deltaZ < 2)
            deltaZ = 0;

        // set the last know values of x,y,z
        lastX = event.values[0];
        lastY = event.values[1];
        lastZ = event.values[2];

        // vibrate();

        boolean hayQueActualizar= false;

        DateTime now= new DateTime();

        /*if (lastUpdate==null)
            lastUpdate= now;

        Calendar calendar= Calendar.getInstance();
        calendar.setTime(lastUpdate);
        calendar.add(Calendar.SECOND, 1);

        Date aux= calendar.getTime();
 */
        /*if (lastUpdate.compareTo(aux)==-1) {
            new UpdateTask().execute();
            lastUpdate= now;
        }*/
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss");

        if (lastUpdate.plusSeconds(1).isBefore(now))
        {
            lastUpdate = now;
            new UpdateTask(lastX, lastY, lastZ).execute();
        }

        Log.i("Compare dates ", "Asíncrono"+dtf.print(now)+" "+dtf.print(lastUpdate));

    }

    // if the change in the accelerometer value is big enough, then vibrate!
    // our threshold is MaxValue/2
   /* public void vibrate() {
        if ((deltaX > vibrateThreshold) || (deltaY > vibrateThreshold) || (deltaZ > vibrateThreshold)) {
            v.vibrate(50);
        }
    }*/

    public void displayCleanValues() {
        currentX.setText("0.0");
        currentY.setText("0.0");
        currentZ.setText("0.0");
    }

    // display the current x,y,z accelerometer values
    public void displayCurrentValues() {
        currentX.setText(Float.toString(deltaX));
        currentY.setText(Float.toString(deltaY));
        currentZ.setText(Float.toString(deltaZ));
    }

    // display the max x,y,z accelerometer values
    public void displayMaxValues() {
        if (deltaX > deltaXMax) {
            deltaXMax = deltaX;
            maxX.setText(Float.toString(deltaXMax));
        }
        if (deltaY > deltaYMax) {
            deltaYMax = deltaY;
            maxY.setText(Float.toString(deltaYMax));
        }
        if (deltaZ > deltaZMax) {
            deltaZMax = deltaZ;
            maxZ.setText(Float.toString(deltaZMax));
        }
    }

    private class UpdateTask extends AsyncTask<URL, Integer, Long> {

       private float lastX, lastY, lastZ;

        public UpdateTask(float lastX, float lastY, float lastZ) {
            this.lastX = lastX;
            this.lastY = lastY;
            this.lastZ = lastZ;
        }

        protected Long doInBackground(URL... urls) {
            int count = urls.length;

            Log.i("Last Position", "Last position: "+lastX+" "+ lastY+" "+lastZ);


            return 0l;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {
            //showDialog("Downloaded " + result + " bytes");

        }
    }
}
