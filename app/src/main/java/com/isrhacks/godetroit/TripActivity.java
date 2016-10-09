package com.isrhacks.godetroit;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TripActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, TimePickerDialog.OnTimeSetListener, AdapterView.OnItemSelectedListener{

        private final int RC_SIGN_IN = 9001;
        private final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1001;
        private final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1002;
        public static final String MY_PREFERENCES = "isrhacks";
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
//            tripTime = tripTime.substring(0, 10) + "T" + tripTime.substring(10);
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu){
            super.onCreateOptionsMenu(menu);
            System.out.println("Create Menu");
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.toolbar, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle item selection
            switch (item.getItemId()) {
                case R.id.action_settings:
                    return true;
                case R.id.circle_settings:
                    Intent intent = new Intent(this, CircleSettingsActivity.class);
                    startActivity(intent);
                    return true;
                default:
            return super.onOptionsItemSelected(item);
            }
        }
        public void setTime(View view){
            //changes made to accomodate no 'T'
            int hour = Integer.valueOf(tripTime.substring(10,12));
            int minute = Integer.valueOf(tripTime.substring(13,15));
            new TimePickerDialog(this, this, hour, minute, true).show();
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String hourString = String.valueOf(hourOfDay);
            String minuteString = String.valueOf(minute);
            System.out.println(hourString);
            hourString = String.format("%02d", hourOfDay);
            minuteString = String.format("%02d", minute);
            String time = hourString + ":" + minuteString;
            this.timeText.setText(time);
            tripTime = tripTime.substring(0,10)+time+tripTime.substring(15);
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
        /*OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if(!opr.isDone()){*/
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        /*}else{
            GoogleSignInAccount acct = opr.get().getSignInAccount();
            postUsername(this, acct.getEmail(), acct.getId());
        }*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length < 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.SEND_SMS},
                            MY_PERMISSIONS_REQUEST_SEND_SMS);
                    return;
                }
            }
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE:{
                if (grantResults.length < 0|| grantResults[0] == PackageManager.PERMISSION_DENIED){
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_PHONE_STATE},
                            MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                }
            }
        }
    }

    public void handleSignInResult(GoogleSignInResult result){
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            postUsername(this, acct.getEmail(), acct.getId());
        } else {
            System.out.println("Hello boy? " + result.getStatus().toString());
            System.out.println("Sign In Fail");
            logIn();
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

    public void postUsername(Context context, final String email, final String id){
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest sr = new StringRequest(Request.Method.POST,"http://godetroit-dev.us-east-1.elasticbeanstalk.com/users/register", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("Posted This is the response");
                JSONObject tokenobj;
                String token = "";
                try {
                    tokenobj = new JSONObject(response);
                    token = tokenobj.getString("token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println(token);
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE).edit();
                editor.putString("jwt", token);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            //    postUsernameResponse.requestEndedWithError(error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("uid", id);
                return params;
            }

/*
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;

            }*/
        };
    queue.add(sr);
    }

    public void notifyCircle(View v){
        //TODO Send current location.
        SharedPreferences preferences = getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
        SmsManager smsManager = SmsManager.getDefault();
        for(int i = 0; i < 5; i++){
            String next = preferences.getString("number"+String.valueOf(i), "0");
            if(!next.equals("0") && !next.equals(null)){
                System.out.println(next);
                smsManager.sendTextMessage(next, null, "Send Help!", null, null);
            }
        }
    }

}
