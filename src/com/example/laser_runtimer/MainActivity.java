package com.example.laser_runtimer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
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

    RaceState state = RaceState.Start;

    private LinearLayout vLayout;
    private Chronometer vChronoRun;
    private Chronometer vChronoShot;
    private int lap = 0;
    private int shotMaxTime = 50;
    private Button vButtonReset;
    private Button vButtonRace;
    private List<TextView> l = new ArrayList<TextView>();

    protected void switchChrono() {
        switch(state){
            case Start:
                reset();
                state = RaceState.Run;
                vButtonRace.setText("Start Shot");
                vChronoRun.start();
                break;
            case Run:
                state = RaceState.Shot;
                vButtonRace.setText("Start Run");
                vChronoShot.setBase(SystemClock.elapsedRealtime());
                vChronoShot.start();
                vChronoRun.stop();
                TextView vText = new TextView(vLayout.getContext());
                String label = "";
                if (lap == 0) {
                    label = "Inital lap " + vChronoRun.getText();
                } else {
                    label = "Lap " + lap + " " + vChronoRun.getText();
                }
                lap++;
                vText.setText(label);
                l.add(vText);
                vLayout.addView(vText);
                break;
            case Shot:
                state= RaceState.Run;
                vButtonRace.setText("Start Shot");
                vChronoShot.stop();
                vChronoRun.start();
                break;
        }

    }

    protected void reset() {
        vChronoRun.stop();
        vChronoRun.setBase(SystemClock.elapsedRealtime());
        vChronoShot.stop();
        vChronoShot.setBase(SystemClock.elapsedRealtime());
        state = RaceState.Start;
        lap = 0;
        shotMaxTime = 50;
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

        vLayout = (LinearLayout) findViewById(R.id.LinearLayout1);

        vChronoRun = (Chronometer) findViewById(R.id.chronometer1);

        vChronoShot = (Chronometer) findViewById(R.id.chronometer2);
        vChronoShot
                .setOnChronometerTickListener(new OnChronometerTickListener() {

                    @Override
                    public void onChronometerTick(Chronometer chronometer) {
                        if (shotMaxTime <= 0) {
                            shotMaxTime = 50;
                            switchChrono();
                        } else {
                            shotMaxTime--;
                        }

                    }
                });

        vButtonReset = (Button) findViewById(R.id.ButtonReset);
        vButtonReset.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                reset();
            }
        });

        vButtonRace= (Button) findViewById(R.id.ButtonRace);
        vButtonRace.setText("Start Run");
        vButtonRace.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                switchChrono();
            }
        });
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
