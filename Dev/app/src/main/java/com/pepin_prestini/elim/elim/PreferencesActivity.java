package com.pepin_prestini.elim.elim;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        Toast.makeText(getApplicationContext(), "Vous êtes dans les préférences", Toast.LENGTH_LONG).show();
    }
}
