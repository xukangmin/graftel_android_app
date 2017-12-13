package com.graftel.www.graftel;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.graftel.www.graftel.mail.SendMail;
import com.graftel.www.graftel.QuoteBroadcastService;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Shorabh on 6/9/2016.
 */
public class QuoteFragment extends Fragment
{
    private final static String TAG = "QuoteBroadcastService";
    private static final String LOG_TAG = "AudioRecordTest";
    ImageButton t1,t2;
    EditText e1;
    TextView mTextField;
    static Button b1;
    RadioGroup radioGroup;
    private MediaRecorder mRecorder = null;
    private MediaPlayer   mPlayer = null;
    private static String mFileName = Environment.getExternalStorageDirectory().getAbsolutePath()+"/audiorecord.3gp";
    private static final int MODE_RECORD_START = 1;
    private static final int MODE_RECORD_STOP = 0;
    private static final int MODE_PLAY_START = 1;
    private static final int MODE_PLAY_STOP = 0;
    boolean audio = false;
    private int mRecordMode = MODE_RECORD_STOP;
    private int mPlayMode = MODE_PLAY_STOP;
    static String textMessage=null;
    SharedPreferences sp;
    SharedPreferences.Editor editor;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.quoterequest, container, false);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.spinner_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        e1 = (EditText) rootView.findViewById(R.id.editText);
        mTextField = (TextView) rootView.findViewById(R.id.textView);
        t1 = (ImageButton) rootView.findViewById(R.id.toggleButton);
        t1.setVisibility(View.GONE);
        t2 = (ImageButton) rootView.findViewById(R.id.play);
        b1 = (Button)rootView.findViewById(R.id.button);
        radioGroup = (RadioGroup) rootView.findViewById(R.id.radios);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.radioButton:
                        audio=false;
                        t1.setVisibility(View.GONE);
                        mTextField.setVisibility(View.GONE);
                        t2.setVisibility(View.GONE);
                        e1.setVisibility(View.VISIBLE);
                        break;
                    case R.id.radioButton2:
                        audio=true;
                        e1.setVisibility(View.GONE);
                        t1.setVisibility(View.VISIBLE);
                        voiceMessage();
                        break;
                }
            }
        });
        sp = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sp.edit();
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                long timestamp = sp.getLong("timestamp",0);
                if(timestamp==0 || ((System.currentTimeMillis()-timestamp)>=60000)) {
                    editor.putLong("timestamp", System.currentTimeMillis());
                    editor.commit();
                    if (!audio)
                        textMessage();
                    else {
                        sendQuote();
                    }
                }
                else
                    Toast.makeText(getActivity(),"Cannot Send a Quote within a minute.",Toast.LENGTH_SHORT).show();

            }
        });
        return rootView;
    }

    public void sendQuote()
    {
        final String subject = "[Graftel APP] Your quote request has been sent to Graftel";
        final String body = "\nThis a confirmation that your quote request has been successfully sent to Graftel, we will respond to you within 24 hours.\n" +
                "\nThank you for contacting Graftel LLC. Below is the original message.\n" +
                "\n---------------------------------------------------------------------------------------------------------------------------------\n" +
                "\nQuote request from "+ User.getContactPersonName ()+" ( "+User.getLoginEmail()+" )\n";

        new AsyncTask<String,Void,String>()
        {
            @Override
            protected String doInBackground(String... params) {
                if(ConnectivityReceiver.isConnected()) {
                    try {
                        SendMail sender = new SendMail("graftel", "calibrate1");
                        if (!audio) {
                            sender.sendMail(subject, body+"\n"+textMessage, User.getLoginEmail(),"");
                        }
                        else {
                            sender.sendMail(subject, body, User.getLoginEmail(),mFileName);
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {Toast.makeText(getActivity(), "Quote Sent! Thank you!", Toast.LENGTH_SHORT).show();}});
                    } catch (Exception e) {
                        Log.e("SendMail", e.getMessage(), e);
                    }
                }
                else
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {Toast.makeText(getActivity(), "No Internet Connection! Quote Not Sent!", Toast.LENGTH_SHORT).show();}});
                return null;
            }
        }.execute();
    }
    public void textMessage()
    {
        textMessage = e1.getText().toString();
        if(!textMessage.equals("")) {
            sendQuote();
        }else
            Toast.makeText(getActivity(), "Please, write a message!",Toast.LENGTH_SHORT).show();

    }
    public void voiceMessage()
    {
        final CountDownTimer t;
        t = new CountDownTimer(60000,1000) {

            public void onTick(long millisUntilFinished) {
                mTextField.setText("Seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                mTextField.setText("Audio Recorded!");

            }
        };

        t2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mPlayMode = mPlayMode == MODE_PLAY_START ? MODE_PLAY_STOP : MODE_PLAY_START;
                mPlayer = new MediaPlayer();
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                mPlayer.setDataSource(getContext().getApplicationContext(),Uri.parse(mFileName));
                switch (mPlayMode) {
                    case MODE_PLAY_START:
                        t2.setBackgroundResource(R.drawable.pause);

                            mPlayer.prepare();
                            mPlayer.start();
                            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    t2.setBackgroundResource(R.drawable.play);
                                    mPlayer.release();
                                    mPlayMode = MODE_PLAY_STOP;
                                }
                            });

                        break;
                    case MODE_PLAY_STOP:
                        t2.setBackgroundResource(R.drawable.play);
                        mPlayer.release();
                        break;
                }
                } catch (IOException e) {
                    //Log.e(LOG_TAG, "prepare() failed");
                }
            }
        });

        t1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int hasAudioPermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO);
                if (hasAudioPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[] {Manifest.permission.RECORD_AUDIO},1);
                    return;
                }
                mRecordMode = mRecordMode == MODE_RECORD_START ? MODE_RECORD_STOP : MODE_RECORD_START;
                switch (mRecordMode)
                {
                    case MODE_RECORD_START:
                        t1.setBackgroundResource(R.drawable.stop);
                        t2.setVisibility(View.GONE);
                        mTextField.setVisibility(View.VISIBLE);
                        b1.setFocusable(false);
                        b1.setClickable(false);
                        mRecorder = new MediaRecorder();
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        mRecorder.setOutputFile(mFileName);
                        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        mRecorder.setMaxDuration(60000);

                        try {
                            mRecorder.prepare();
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "prepare() failed");
                        }
                        mRecorder.start();
                        t.start();
                        break;
                    case MODE_RECORD_STOP:
                        t1.setBackgroundResource(R.drawable.record1);
                        b1.setFocusable(true);
                        b1.setClickable(true);
                        mRecorder.stop();
                        mRecorder.release();
                        t.cancel();
                        t2.setVisibility(View.VISIBLE);
                        break;
                }
                t.onFinish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(getActivity(), "Audio Permissions Denied", Toast.LENGTH_SHORT).show();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
