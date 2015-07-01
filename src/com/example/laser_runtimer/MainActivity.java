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
    private LinearLayout vLayout;
    private Chronometer vChronoRun;
    private Chronometer vChronoShot;
    private Boolean isChronoRunActive = false;
    private int lap = 0;
    private int shotMaxTime = 50;
    private Button vButtonReset;
    private List<TextView> l = new ArrayList<TextView>();

    protected void switchChrono() {
        if (!isChronoRunActive) {
            isChronoRunActive = true;
            vChronoRun.start();
            vChronoShot.stop();
        } else {
            isChronoRunActive = false;
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
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vChronoRun = (Chronometer) findViewById(R.id.chronometer1);
        vChronoShot = (Chronometer) findViewById(R.id.chronometer2);
        vLayout = (LinearLayout) findViewById(R.id.LinearLayout1);

        vChronoRun.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switchChrono();
            }
        });

        vChronoShot.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switchChrono();
            }
        });

        vChronoShot
                .setOnChronometerTickListener(new OnChronometerTickListener() {

                    @Override
                    public void onChronometerTick(Chronometer chronometer) {
                        // TODO Auto-generated method stub
                        if (shotMaxTime <= 0) {
                            shotMaxTime = 50;
                            switchChrono();
                        } else {
                            shotMaxTime--;
                        }

                    }
                });
    
        vButtonReset = (Button) findViewById(R.id.ButtonReset);
        vButtonReset.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                vChronoRun.stop();
                vChronoRun.setBase(SystemClock.elapsedRealtime());
                vChronoShot.stop();
                vChronoShot.setBase(SystemClock.elapsedRealtime());
                isChronoRunActive=false;
                lap = 0;
                shotMaxTime=50;
                // Clear laps
                for (Iterator<TextView> i = l.iterator(); i.hasNext();) {
                    TextView t = i.next();
                    ((ViewManager)t.getParent()).removeView(t);
                    
                }
                l.clear();
                
            }
        }
        );
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
