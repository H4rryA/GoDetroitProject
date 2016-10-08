package com.isrhacks.godetroit;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TripActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, AdapterView.OnItemSelectedListener{

        String currentTime;
        TextView timeText;
        String tripTime;
        String spinner_choice;

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
            ((Spinner) findViewById(R.id.time_spinner)).setOnItemSelectedListener(this);
            timeText = (TextView) findViewById(R.id.time_text);

            Calendar c = Calendar.getInstance();
            Date d = c.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddhh:mmZ");
            currentTime = sdf.format(d);
            currentTime = currentTime.substring(0, 10) + "T" + currentTime.substring(11);
        }

        public void setTime(View view){
            //TODO Set time to current time.
            int hour = 12;
            int minute = 00;
            new TimePickerDialog(this, this, hour, minute, true).show();
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            this.timeText.setText( hourOfDay + ":" + minute);
        }

        public void launchRoutes(View v){
            Intent intent = new Intent(this, RoutesActivity.class);
            TextView fromView = (TextView) findViewById(R.id.from_input);
            TextView toView = (TextView) findViewById(R.id.to_input);
            String fromLocation = fromView.getText().toString();
            String toLocation = toView.getText().toString();
            if(fromLocation.equals("") || toLocation.equals("")){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("You have not filled all of the location fields.");
                builder.setTitle("Error");
                AlertDialog missingLocation = builder.create();
                missingLocation.show();
            }else {
                intent.putExtra("from", fromLocation);
                intent.putExtra("to", toLocation);
                intent.putExtra("time_constraint", spinner_choice);
                tripTime = currentTime;
                intent.putExtra("time", tripTime);
                startActivity(intent);
            }
        }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        spinner_choice = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
