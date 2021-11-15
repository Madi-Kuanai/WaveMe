package com.mzmm.wave_me.voice;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;

import com.mzmm.wave_me.databinding.ActivityTextToSpeechBinding;

import java.util.Locale;

public class TextToSpeechAct extends AppCompatActivity {

    ActivityTextToSpeechBinding binding;
    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTextToSpeechBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    int result = textToSpeech.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("MyPeriodicWorker1", result + " is not supported");
                    }

                }
                if (i == TextToSpeech.ERROR) Log.d("MyPeriodicWorker1", "Error");
            }
        });
        binding.toSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech.speak(String.valueOf(binding.textForSpeak.getText()), TextToSpeech.QUEUE_FLUSH, null);
                Log.d("MyPeriodicWorker1", "OnClick");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}