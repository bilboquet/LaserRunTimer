package com.example.laser_runtimer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    enum RaceState {
        Start, Shot, Run
    };

    enum BStopResetState {
        Stop, Reset
    };

    RaceState state = RaceState.Start;
    BStopResetState bState = BStopResetState.Reset;

    private TextView vTextActivity;
    private LinearLayout vLayout;
    private Chronometer vChronoTotal;
    private Chronometer vChronoRunShot;
    private int lap = 0;
    private static final int shotMaxTime = 50000; // 50s in millisecond
    private Button vButtonStopReset;
    private Button vButtonRace;
    private List<TextView> l = new ArrayList<TextView>();
    private SoundGenerator alarm = new SoundGenerator();

    protected void setButtonStopReset(BStopResetState state) {
        vButtonStopReset.setText(state.toString());
        bState = state;
    }

    protected void addTimeView(CharSequence text) {
        TextView vText = new TextView(vLayout.getContext());
        vText.setText(text);
        l.add(vText);
        vLayout.addView(vText);
    }

    protected void updateTimeView(CharSequence text) {
        TextView lastText = l.get(l.size() - 1);
        lastText.setText(lastText.getText() + " " + text);

    }

    protected void switchRaceState() {
        CharSequence time;
        switch (state) {
        case Start:
            reset();
            vButtonRace.setText("Start Shot");
            vTextActivity.setText("Running time");
            setButtonStopReset(BStopResetState.Stop);
            vChronoTotal.start();
            vChronoRunShot.start();
            state = RaceState.Run;
            break;

        case Run:
            time = getTimeRestart();

            if (lap == 0) {
                addTimeView("Inital lap " + time);
            } else {
                updateTimeView("Lap " + lap + " " + time);
            }
            lap++;

            vButtonRace.setText("Start Run");
            vTextActivity.setText("Shooting time");
            state = RaceState.Shot;
            break;

        case Shot:
            time = getTimeRestart();
            addTimeView("Shot serie " + lap + " " + time);

            vButtonRace.setText("Start Shot");
            vTextActivity.setText("Running time");
            state = RaceState.Run;
            break;
        }

    }

    private CharSequence getTimeRestart() {
        CharSequence time;
        time = vChronoRunShot.getText();
        vChronoRunShot.setBase(SystemClock.elapsedRealtime());
        vChronoRunShot.start();
        return time;
    }

    protected void reset() {
        vChronoTotal.stop();
        vChronoTotal.setBase(SystemClock.elapsedRealtime());
        vChronoRunShot.stop();
        vChronoRunShot.setBase(SystemClock.elapsedRealtime());
        state = RaceState.Start;
        bState = BStopResetState.Reset;
        vButtonRace.setText("Start Run");
        vButtonRace.setEnabled(true);
        lap = 0;
        // Clear laps
        for (Iterator<TextView> i = l.iterator(); i.hasNext();) {
            TextView t = i.next();
            ((ViewManager) t.getParent()).removeView(t);

        }
        l.clear();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vTextActivity = (TextView) findViewById(R.id.Activity);

        vLayout = (LinearLayout) findViewById(R.id.LinearLayout1);

        vChronoTotal = (Chronometer) findViewById(R.id.chronometer1);

        vChronoRunShot = (Chronometer) findViewById(R.id.chronometer2);
        vChronoRunShot
                .setOnChronometerTickListener(new OnChronometerTickListener() {

                    @Override
                    public void onChronometerTick(Chronometer chronometer) {
                        long elapsedTime = SystemClock.elapsedRealtime()
                                - chronometer.getBase();
                        if (state == RaceState.Shot
                                && elapsedTime >= shotMaxTime) {
                            chronometer.stop(); // to prevent new tick
                            alarm.play();
                            switchRaceState(); // let's run again
                        }
                    }
                });

        vButtonStopReset = (Button) findViewById(R.id.ButtonReset);
        vButtonStopReset.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                switch (bState) {
                case Reset:
                    reset();
                    break;

                case Stop:
                    vChronoRunShot.stop();
                    vChronoTotal.stop();
                    if (lap == 0) {
                        addTimeView("Inital lap " + vChronoRunShot.getText());
                    } else {
                        if (state == RaceState.Run) {
                            updateTimeView("Lap " + lap + " "
                                    + vChronoRunShot.getText());
                        } else {
                            addTimeView("Shot serie " + lap + " "
                                    + vChronoRunShot.getText());
                        }
                    }
                    addTimeView("Total time " + vChronoTotal.getText());
                    setButtonStopReset(BStopResetState.Reset);
                    vButtonRace.setEnabled(false);
                    break;
                }
            }
        });

        vButtonRace = (Button) findViewById(R.id.ButtonRace);
        vButtonRace.setText("Start Run");
        vButtonRace.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                switchRaceState();
            }
        });

        alarm.init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

class SoundGenerator {
    private final int duration = 3; // seconds
    private final int sampleRate = 8000;
    private final int numSamples = duration * sampleRate;
    private final double sample[] = new double[numSamples];
    private final double freqOfTone = 880; // hz

    private final byte generatedSnd[] = new byte[2 * numSamples];

    void init() {
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                genTone();
            }
        });
        thread.start();
    }

    void play() {
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                playSound();
            }
        });
        thread.start();
    }

    private void genTone() {
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            // sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
            sample[i] = Math.sin((2 * Math.PI - .001) * i
                    / (sampleRate / freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        int ramp = numSamples / 20;

        for (int i = 0; i < ramp; i++) {
            // scale to maximum amplitude
            final short val = (short) ((sample[i] * 32767) * i / ramp);
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        for (int i = ramp; i < numSamples - ramp; i++) {
            // scale to maximum amplitude
            final short val = (short) ((sample[i] * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        for (int i = numSamples - ramp; i < numSamples; i++) {
            // scale to maximum amplitude
            final short val = (short) ((sample[i] * 32767) * (numSamples - i) / ramp);
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
    }

    private void playSound() {
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();
    }

}
