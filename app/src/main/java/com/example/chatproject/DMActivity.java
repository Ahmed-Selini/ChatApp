package com.example.chatproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class DMActivity extends AppCompatActivity {

    RecyclerView dmRecycler;
    DMAdapter adapter;
    ArrayList<DM> dmList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dm);

        dmRecycler = findViewById(R.id.dmRecycler);

        // Static data
        dmList.add(new DM(R.drawable.ic_profile_boy, "Olimtoy", "Hey! Do you wanna see new robotics?"));
        dmList.add(new DM(R.drawable.ic_profile_boy, "Toshbolta", "Typing..."));
        dmList.add(new DM(R.drawable.ic_profile_boy, "O’tkuriy", "Go go go"));
        dmList.add(new DM(R.drawable.ic_profile_girl, "Lola Norova", "Back to office, a lot of work here!"));

        adapter = new DMAdapter(dmList);

        dmRecycler.setLayoutManager(new LinearLayoutManager(this));
        dmRecycler.setAdapter(adapter);
    }
}
