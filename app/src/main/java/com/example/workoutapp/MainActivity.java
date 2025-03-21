package com.example.workoutapp;

import android.annotation.SuppressLint;

import android.os.Bundle;


import androidx.fragment.app.Fragment;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.workoutapp.WorkoutFragment.WorkoutFragment;
import com.example.workoutapp.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
    public ActivityMainBinding bindingMain;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        bindingMain = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(bindingMain.getRoot());


        bindingMain.bottomNavView.setBackground(null);


        replaceFragment(new WorkoutFragment());

        setInitialActiveButton();

        bindingMain.bottomNavView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.Profile) {
                replaceFragment(new ProfileFragment());
            } else if(item.getItemId() == R.id.Workout){
                replaceFragment(new WorkoutFragment());
            } else if(item.getItemId() == R.id.Nutrition){
                replaceFragment(new NutritionFragment());
            }else if(item.getItemId() == R.id.People){
                replaceFragment(new PeopleFragment());
            }
            return true;
        });


    }



    private void setInitialActiveButton() {
        bindingMain.bottomNavView.getMenu().getItem(2).setChecked(true);
        replaceFragment(new WorkoutFragment());
    }

    public void replaceFragment(Fragment Fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, Fragment);
        fragmentTransaction.commit();


    }



}