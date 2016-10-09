package com.isrhacks.godetroit;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class CircleSettingsActivity extends AppCompatActivity {

    private String[] order = new String[]{"first", "second", "third", "fourth", "fifth"};
    private int count = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_settings);
        SharedPreferences preferences = getSharedPreferences(TripActivity.MY_PREFERENCES, MODE_PRIVATE);
        for(int i = 0; i < 5; i++){
            int contactId = getResources().getIdentifier(order[i] + "Contact", "id", getApplicationContext().getPackageName());
            int numberID = getResources().getIdentifier(order[i] + "Number", "id", getApplicationContext().getPackageName());
            String contact = preferences.getString("contact"+String.valueOf(i), "");
            String number = preferences.getString("number"+String.valueOf(i), "");
            if(!contact.equals("")) {
                TextView contactView = ((TextView) findViewById(contactId));
                contactView.setText(contact);
                contactView.setVisibility(View.VISIBLE);
                TextView numberView = ((TextView) findViewById(numberID));
                numberView.setText(number);
                numberView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void updatePreferences(){
        SharedPreferences.Editor editor = getSharedPreferences(TripActivity.MY_PREFERENCES, MODE_PRIVATE).edit();
        int i = count-1;
        int contactId = getResources().getIdentifier(order[i] + "Contact", "id", getApplicationContext().getPackageName());
        int numberID = getResources().getIdentifier(order[i] + "Number", "id", getApplicationContext().getPackageName());
        System.out.println(contactId);
        System.out.println(R.id.firstContact);
        TextView contact = (TextView) findViewById(contactId);
        TextView number = (TextView) findViewById(numberID);

        System.out.println("contact"+ String.valueOf(i) + contact.getText().toString());
        System.out.println("number"+ String.valueOf(i) + number.getText().toString());

        if (!contact.getText().toString().equals("")){
            editor.putString("contact" + String.valueOf(i), contact.getText().toString());
        }
        if (!number.getText().toString().equals("")) {
            editor.putString("number" + String.valueOf(i), number.getText().toString());
        }
        editor.apply();
    }

    public void addContact(View v){
        if(count < 5) {
            updatePreferences();
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
