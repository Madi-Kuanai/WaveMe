package com.mzmm.wave_me;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.viewpager.widget.ViewPager;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.mzmm.wave_me.backends.MyPeriodicWorker;
import com.mzmm.wave_me.cards.Adapter;
import com.mzmm.wave_me.cards.Model;
import com.mzmm.wave_me.databinding.ActivityMainBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mzmm.wave_me.recognize.CameraActivity;
import com.mzmm.wave_me.sign_in_out.SignIn;
import com.mzmm.wave_me.voice.TextToSpeechAct;
import com.mzmm.wave_me.voice.UserVoiceRecognition;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {


    static {
        if (OpenCVLoader.initDebug()) {
            Log.d("MainActivity: ", "Opencv is loaded");
        } else {
            Log.d("MainActivity: ", "Opencv failed to load");
        }
    }

    ViewPager viewPager;
    Adapter adapter;
    List<Model> models;
    LottieAnimationView toggleButton;
    SharedPreferences sharedPreferences = null;
    ActivityMainBinding binding;
    boolean isNight;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_main);

        set_models();
        toggleButton = findViewById(R.id.toggle);
        sharedPreferences = getSharedPreferences("night", 0);
        isNight = sharedPreferences.getBoolean("night_mode", true);

        if (isNight) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            toggleButton.setProgress(0.5f);
        }

        toggleButton = findViewById(R.id.toggle);

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MyLog", String.valueOf(toggleButton.getProgress()));
                if (isNight) {
                    toggleButton.setMinAndMaxProgress(0.0f, 0.5f);
                    toggleButton.playAnimation();
                    try {
                        Thread.sleep(60);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("night_mode", false);
                    editor.apply();
                    isNight = false;


                } else {
                    toggleButton.setMinAndMaxProgress(0.5f, 1f);
                    toggleButton.playAnimation();
                    try {
                        Thread.sleep(60);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("night_mode", true);
                    editor.apply();
                    isNight = true;
                }
                set_models();
                Log.d("MyLog", String.valueOf(toggleButton.getProgress()));
            }
        });

        View bottomSheet = findViewById(R.id.sheet);

        BottomSheetBehavior.from(bottomSheet).setPeekHeight(90);
        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_HIDDEN);
        CoordinatorLayout swipe_sheet = findViewById(R.id.swipe_sheet);
        swipe_sheet.setVisibility(View.VISIBLE);
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(MyPeriodicWorker.class, 15, TimeUnit.MINUTES).build();

        WorkManager workManager = WorkManager.getInstance();
        workManager.enqueue(workRequest);
        workManager.getWorkInfoByIdLiveData(workRequest.getId()).observeForever(new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if (workInfo != null) {
                    Log.d("MyPeriodicWorker1", "!!!!" + workInfo.getState().toString());
                }
            }
        });

        auth = FirebaseAuth.getInstance();
    }

    private void set_models() {
        models = new ArrayList<>();

        models.add(new Model(R.drawable.test_banner, "Sign Language", "Sign languages are languages that use the visual-manual modality to convey meaning.", 0));
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            models.add(new Model(R.drawable.speech, "Voice to Text", "", 1));
        } else if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
            models.add(new Model(R.drawable.speech_white, "Voice to Text", "", 1));
        }
        models.add(new Model(R.drawable.speech, "Text To Speech", "", 2));

        adapter = new Adapter(models, this);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setPadding(50, 0, 50, 0);
    }


    public void card_clicked(View view) {
        switch (view.getId()) {
            case 0: {
                startActivity(new Intent(MainActivity.this, CameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            }
            case 1:
                startActivity(new Intent(this, UserVoiceRecognition.class));
                break;
            case 2:
                startActivity(new Intent(this, TextToSpeechAct.class));
            default:
                Log.d("MyLog", "Error");
        }

    }

    public void sign_out(View view) {
        Log.d("MyLog", "Clicked");
        auth.signOut();
        startActivity(new Intent(this, SignIn.class));
        finish();
    }
}