package com.example.djchen.morningassistant;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.security.Provider;

/**
 * Created by andrewstoycos on 12/2/16.
 */

public class RingtonePlayingService extends Service {


    MediaPlayer media_song;
    boolean music_is_playing;
    int startID;
    String state;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Get the extra values attached to the intents
        state = intent.getExtras().getString("extra");

        assert state != null;
        switch (state) {
            case "On":
                startId = 1;
                break;
            case "Off":
                startId = 0;
                break;
            default:
                startId = 0;
                break;
        }


        //This if statement turns on and off the MediaPlayer based on if the music is playing an the Alarm set button was pressed
        if(!this.music_is_playing && startId == 1){

            media_song = MediaPlayer.create(this, R.raw.super_ringtone);
            media_song.start();
            media_song.setLooping(true);

            this.music_is_playing = true;
            this.startID = 0;
            MainActivity.numSteps =0;

        }else if(this.music_is_playing && startId == 0 ){
            media_song.stop();
            media_song.reset();

            this.music_is_playing = false;
            this.startID = 0;

        }else if(!this.music_is_playing && startId == 0){

            this.music_is_playing = false;
            this.startID = 0;

        }else if(this.music_is_playing && startId == 1){
            this.music_is_playing = true;
            this.startID = 1;
        }else{

        }


        return START_NOT_STICKY;
    }



    @Override
    public void onDestroy() {

        Toast.makeText(this,"on Destroy call", Toast.LENGTH_SHORT).show();
    }


}
