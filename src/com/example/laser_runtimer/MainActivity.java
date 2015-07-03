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
    private int shotMaxTime = 50;
    private Button vButtonStopReset;
    private Button vButtonRace;
    private List<TextView> l = new ArrayList<TextView>();

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
        vTextActivity = (TextView) findViewById(R.id.Activity);

        vLayout = (LinearLayout) findViewById(R.id.LinearLayout1);

        vChronoTotal = (Chronometer) findViewById(R.id.chronometer1);

        vChronoRunShot = (Chronometer) findViewById(R.id.chronometer2);
        vChronoRunShot
                .setOnChronometerTickListener(new OnChronometerTickListener() {

                    @Override
                    public void onChronometerTick(Chronometer chronometer) {
                        if (state == RaceState.Shot) {
                            if (shotMaxTime < 0) {
                                shotMaxTime = 50;
                                switchRaceState();
                                // Play alarm
                            } else {
                                shotMaxTime--;
                            }
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
