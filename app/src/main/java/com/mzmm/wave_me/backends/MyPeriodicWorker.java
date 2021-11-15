package com.mzmm.wave_me.backends;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


public class MyPeriodicWorker extends Worker {
    private static final String TAG = "MyPeriodicWorker1";

    public MyPeriodicWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    @NonNull
    @Override
    public Result doWork() {

        return Result.success();
    }
}
