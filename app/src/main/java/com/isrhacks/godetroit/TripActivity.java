package com.isrhacks.godetroit;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

public class TripActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener{

        TextView timeText;
        int tripHour;
        int tripMin;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_trip);
            Spinner spinner = (Spinner) findViewById(R.id.time_spinner);
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.time_array, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            spinner.setAdapter(adapter);

            timeText = (TextView) findViewById(R.id.time_text);
        }

        public void setTime(View view){
            int hour = 12;
            int minute = 00;
            new TimePickerDialog(this, this, hour, minute, true).show();
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // TODO Auto-generated method stub
            this.timeText.setText( hourOfDay + ":" + minute);
        }

        public void launchRoutes(View v){
         /*   Intent intent = new Intent(this, RoutesActivity.class);
            intent.putExtra("hour", tripHour);
            intent.putExtra("min", tripMin);
            startActivity(intent);*/
        }

}
