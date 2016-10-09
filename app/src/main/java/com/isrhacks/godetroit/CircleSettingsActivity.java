package com.isrhacks.godetroit;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class CircleSettingsActivity extends AppCompatActivity {

    private String[] order = new String[]{"first", "second", "third", "fourth", "fifth"};
    private int count = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_settings);

    }

    @Override
    public void onStop(){
        SharedPreferences.Editor editor = getSharedPreferences(TripActivity.MY_PREFERENCES, MODE_PRIVATE).edit();
        View view = getLayoutInflater().inflate(R.layout.activity_circle_settings, null, false);
        for (int i = 0; i < 5; i++) {
            int contactId = getResources().getIdentifier(order[i] + "Contact", "id", getApplicationContext().getPackageName());
            int numberID = getResources().getIdentifier(order[i] + "Number", "id", getApplicationContext().getPackageName());
            EditText contact = (EditText) view.findViewById(contactId);
            EditText number = (EditText) view.findViewById(numberID);
            if (!contact.getText().toString().equals("")){
                editor.putString("contact" + String.valueOf(i), contact.getText().toString());
            }
            if (!number.getText().toString().equals("")) {
                editor.putString("number" + String.valueOf(i), number.getText().toString());
            }
        }
        editor.apply();
        super.onStop();
    }

    public void addContact(View v){
        if(count < 5) {
            int contactID = getResources().getIdentifier(order[count] + "Contact", "id", getApplicationContext().getPackageName());
            int numberID = getResources().getIdentifier(order[count] + "Number", "id", getApplicationContext().getPackageName());
            EditText contact = (EditText) findViewById(contactID);
            EditText number = (EditText) findViewById(numberID);
            contact.setVisibility(View.VISIBLE);
            number.setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.add_contact).setVisibility(View.GONE);
        }
        count++;
    }
}
