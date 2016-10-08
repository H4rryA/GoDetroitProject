package com.isrhacks.godetroit;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TripActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, TimePickerDialog.OnTimeSetListener, AdapterView.OnItemSelectedListener{
        private final int RC_SIGN_IN = 9001;

        String tripTime;
        TextView timeText;
        String spinner_choice;
        GoogleApiClient mGoogleApiClient;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_trip);
            Spinner spinner = (Spinner) findViewById(R.id.time_spinner);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.time_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            ((Spinner) findViewById(R.id.time_spinner)).setOnItemSelectedListener(this);
            timeText = (TextView) findViewById(R.id.time_text);

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
            logIn();

            Calendar c = Calendar.getInstance();
            Date d = c.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddhh:mmZ", Locale.ENGLISH);
            tripTime = sdf.format(d);
            tripTime = tripTime.substring(0, 10) + "T" + tripTime.substring(10);
        }

        public void setTime(View view){
            int hour = Integer.valueOf(tripTime.substring(11,13));
            int minute = Integer.valueOf(tripTime.substring(14,16));
            new TimePickerDialog(this, this, hour, minute, true).show();
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String hourString = String.valueOf(hourOfDay);
            String minuteString = String.valueOf(minute);
            if(hourOfDay/10 == 0){
                hourString = String.format("%02d", hourOfDay);
            }
            if(minute/10 == 0){
                minuteString = String.format("%02d", minute);
            }
            String time = hourString + ":" + minuteString;
            this.timeText.setText(time);
            tripTime = tripTime.substring(0,11)+time+tripTime.substring(16);
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
                intent.putExtra("time", tripTime);
                startActivity(intent);
            }
        }

    private void logIn(){
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if(!opr.isDone()){
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    public void handleSignInResult(GoogleSignInResult result){
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            /*GoogleSignInAccount acct = */result.getSignInAccount();
        } else {
            System.out.println("Sign In Fail");
//            logIn();
        }
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        spinner_choice = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("Connection Failed");
    }
}
