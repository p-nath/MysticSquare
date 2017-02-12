package com.example.artemis.mysticsquare;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button[][] buttons = new Button[4][4];
    private int moves;
    private TextView MovesCounter;
    Chronometer chronometer;
    private SharedPreferences shared_pref;

    //function which checks if the generated series is a solvable 15-puzzle
    public boolean IsSolvable(List<Integer> list) {
        int inverse_count = 0;
        for (int i = 0; i < 15; i++) {
            for (int j = i; j < 15; j++) {
                if (list.get(i)!=0 && list.get(j)!=0 &&list.get(i) > list.get(j)) {
                    inverse_count += 1;
                }
            }
        }
        return inverse_count % 2 == 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shared_pref = getPreferences(MODE_PRIVATE);
        //since there is 1 shared_pref file
        //no need to explicitly mention thee name

        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= 15; i++)
            list.add(i);
        Collections.shuffle(list);
        list.add(0);

        while (!IsSolvable(list)){
            Collections.shuffle(list);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

        int k = 0;
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++) {
                buttons[i][j] = new Button(this);
                buttons[i][j].setTag(i + " " + j);
                String val = shared_pref.getString(i + " " + j,list.get(k++).toString() );
                //either i+j or k if former not available
                buttons[i][j].setText(val);
                buttons[i][j].setLayoutParams(params);
                buttons[i][j].setOnClickListener(this); //main activity listens when clicked
            }
        if (buttons[3][3].getText().toString().equals("0"))  {
            buttons[3][3].setText("");
        }

        moves = shared_pref.getInt("moves", 0);

        LinearLayout page;
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);

        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < 4; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            rows.addView(row);
            for (int j = 0; j < 4; j++)
                row.addView(buttons[i][j]);
        }
        page.addView(rows);


        LinearLayout NewLine = new LinearLayout(this);
        NewLine.setOrientation(LinearLayout.HORIZONTAL);
        //MoveCounter
        MovesCounter = new TextView(this);
        MovesCounter.setLayoutParams(params);
        MovesCounter.setText("Moves: " + moves);
        NewLine.addView(MovesCounter);
        //Timer
        TextView Time = new TextView(this);
        Time.setLayoutParams(params);
        Time.setText("Timer:");
        NewLine.addView(Time);
        chronometer = new Chronometer(this);
        if (shared_pref.getLong("chrono", -1) != -1)
            chronometer.setBase(shared_pref.getLong("chrono", 0));
        chronometer.start();
        chronometer.setLayoutParams(params);
        NewLine.addView(chronometer);
        page.addView(NewLine);


        setContentView(page);
    }

    public int WinChecker() {
        int k = 1;
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++) {
                if (buttons[i][j].getText().toString().equals((k++) + "")) {
                    if (k == 16)  return 1;
                }
                else  return 0;
            }
        return 0;
    }

    @Override
    public void onClick(View v) {
        Button button = (Button)v;
        String[] s = button.getTag().toString().split(" ");
        int x = Integer.parseInt(s[0]);
        int y = Integer.parseInt(s[1]);

        int[] xx = {x - 1, x, x + 1, x};
        int[] yy = {y, y - 1, y, y + 1};

        for (int k = 0; k < 4; k++) {
            int i = xx[k];
            int j = yy[k];
            if (i >= 0 && i < 4 && j >= 0 && j < 4 && buttons[i][j].getText().equals("")) {
                buttons[i][j].setText(button.getText());
                button.setText("");
                moves += 1;
                MovesCounter.setText("Moves: " + moves);
                if (WinChecker() == 1) {
                    new AlertDialog.Builder(this)
                            .setTitle("Congratulations!!")
                            .setMessage("You win!")
                            .show();
                    chronometer.stop();
                    shared_pref.edit().clear().apply();
                }
                else {
                    Toast.makeText(this, "Continue", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Save game")
                .setMessage("Would you like to save the game?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() { //anonymous class
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = shared_pref.edit();
                        for (int i = 0; i < 4; i++)
                            for (int j = 0; j < 4; j++)
                                editor.putString(i + " " + j, buttons[i][j].getText().toString());
                        editor.putInt("moves", moves);
                        editor.putLong("chrono", chronometer.getBase());
                        editor.apply(); //save to shared pref
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() { //anonymous class
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        shared_pref.edit().clear().apply();
                        finish();
                    }
                })
                .show();

    }
}
