package com.example.djchen.morningassistant;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.util.Calendar;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewDebug;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.common.server.converter.StringToIntConverter;

public class MainActivity extends AppCompatActivity implements SensorEventListener, StepListener {

    AlarmManager alarm_manager;
    TimePicker alarm_timepicker;
    TextView update_text;
    Context contex;
    PendingIntent pending_intent;
    Intent tryIntent;

    //Make objects for step counter
    private StepDetector mStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    public static int numSteps;
    public int count = 0;
    public boolean date_change = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initializes step sensor objects
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mStepDetector = new StepDetector();
        mStepDetector.registerListener(this);

        setContentView(R.layout.activity_main);
        this.contex = this;


        //Initialize alarm manager
        alarm_manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        //Initialize timepicker
        alarm_timepicker = (TimePicker) findViewById(R.id.timepicker);

        //initialize the text update box
        update_text = (TextView) findViewById(R.id.update_text);

        // create an instance of a calender
        final Calendar calender = Calendar.getInstance();

        //Create Intent class
        final Intent my_intent = new Intent(this.contex, Alarm_Receiver.class);
        tryIntent = my_intent;

        //Initialize start Button
        final Button alarm_on = (Button) findViewById(R.id.alarm_on);

        //create an onclick listener to start alarm
        alarm_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //lets the sensor know the button has been pressed
                count = 1;

                //sets the calender object to the time chosen in the timePicker
                calender.set(Calendar.HOUR_OF_DAY, alarm_timepicker.getHour());
                calender.set(Calendar.MINUTE, alarm_timepicker.getMinute());

                //If alarm is set for the next day increment the calender date
                if (calender.getTimeInMillis() < System.currentTimeMillis()) {
                    calender.add(Calendar.DATE, 1);
                    date_change = true;

                }
                //Formats the times from military to standard time so they can be displayed in the texviews
                int hour = alarm_timepicker.getHour();
                int minute = alarm_timepicker.getMinute();
                // Convert int values to strings
                if (hour > 12) {
                    hour = hour - 12;

                }

                String hour_string = String.valueOf(hour);
                String minute_string = String.valueOf(minute);
                //method that changes the update text textbox
                if (minute < 10) {
                    minute_string = "0" + String.valueOf(minute);
                }


                set_alarm_text("Alarm set to: " + hour_string + ":" + minute_string);

                //Puts an "on" tag on the intent to let the Ringtone playing service know you want to play the alarm
                my_intent.putExtra("extra", "On");

                //create a pending intent
                pending_intent = PendingIntent.getBroadcast(MainActivity.this, 0, my_intent, PendingIntent.FLAG_UPDATE_CURRENT);

                // set alarm manager, will wake up and send the pending intent to the Alarm_Receiver
                alarm_manager.setExact(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), pending_intent);

                //If alarm was set for the next day, but never went off, reset the calender day
                if (date_change){
                    calender.add(Calendar.DATE,-1);
                    date_change = false;
                }
            }


        });

        //makes intent used by the sensor response
        tryIntent = my_intent;

    }
    private void set_alarm_text(String output) {
        update_text.setText(output);
    }


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    public void onResume() {
        super.onResume();
        numSteps = 0;
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void step(long timeNs) {
        //increments steps when step function is called baised on acceleramator and gyroscope sensors
        numSteps++;
        Log.e("STEP", Integer.toString(numSteps));
        //once ten steps have been taken and the alarm set button has been pressed
        if(numSteps >= 10 && count>0) {

            //Puts an "off" tag on the intent to let the Ringtone playing service know you want to stop the alarm
            tryIntent.putExtra("extra", "Off");

            //sets the textview
            set_alarm_text("Alarm off");

            //cancels the pending intent previously sent to alarm manager
            alarm_manager.cancel(pending_intent);

            //sends broadcase to RingTonePlaying service to stop the media player
            sendBroadcast(tryIntent);

            numSteps = 0;
            count=0;
        }
    }
}



