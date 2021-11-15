package com.mzmm.wave_me.voice;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;


import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieFrameInfo;
import com.airbnb.lottie.value.SimpleLottieValueCallback;
import com.mzmm.wave_me.R;
import com.mzmm.wave_me.databinding.ActivityVoiceRecognitionBinding;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Objects;

import kotlinx.coroutines.Delay;

public class UserVoiceRecognition extends AppCompatActivity {

    ActivityVoiceRecognitionBinding binding;
    SpeechRecognizer speechRecognizer;
    Intent intentRecognizer;
    boolean isRecognize = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVoiceRecognitionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        intentRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        assert binding.button != null;
        assert binding.deleteText != null;

        binding.deleteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert binding.resultText != null;
                if (!TextUtils.isEmpty(binding.resultText.getText())) {
                    binding.deleteText.setSpeed(2);
                    binding.deleteText.playAnimation();
                    binding.resultText.setText("");
                    Log.d("MyLog", String.valueOf(binding.deleteText.getProgress()));
                    binding.deleteText.addAnimatorListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            binding.deleteText.setProgress(0);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });

                }
            }
        });

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recognize();
            }
        });
    }

    private void recognize() {
        if (isRecognize) {
            isRecognize = false;
            assert binding.lottie != null;
            binding.lottie.setVisibility(View.VISIBLE);
            binding.lottie.playAnimation();
            speechRecognizer.startListening(intentRecognizer);
        } else {
            isRecognize = true;
            assert binding.lottie != null;
            binding.lottie.setVisibility(View.INVISIBLE);
            binding.lottie.cancelAnimation();
            speechRecognizer.stopListening();
        }
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {
                onErr();
            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> arrayList = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (arrayList != null) {
                    binding.resultText.setText(binding.resultText.getText() + " " + arrayList.get(0));
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
    }

    public void back_to_main(View view) {
        if (isRecognize) speechRecognizer.stopListening();
        finish();
    }

    void onErr() {
        binding.errorLottie.setVisibility(View.VISIBLE);
        binding.errorLottie.setMinAndMaxProgress(0f, 1f);
        binding.errorLottie.setSpeed(1.5f);
        binding.errorLottie.playAnimation();
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        binding.errorLottie.setProgress(0);
        binding.errorLottie.setVisibility(View.INVISIBLE);
    }


}