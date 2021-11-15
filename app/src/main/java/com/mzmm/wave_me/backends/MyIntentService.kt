package com.mzmm.wave_me.backends

import android.app.IntentService
import android.app.Service
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 *
 *
 *
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
class MyIntentService : IntentService("MyIntentService") {
    override fun onHandleIntent(intent: Intent?) {
        intent?.apply {
            when (intent.action) {
                ACTION_SETUP_WORKER -> setupWorker()
            }
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        return Service.START_STICKY
    }

    private fun setupWorker() {
        val c: Calendar = Calendar.getInstance()
        val now: Long = c.timeInMillis
        c.add(Calendar.DATE, 1)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)

        val millisecondsUntilMidnight: Long = (c.timeInMillis - now) * 3600;
        Log.d("TIMEEEEEE", millisecondsUntilMidnight.toString())
    }

    companion object {

        const val ACTION_SETUP_WORKER = "ACTION_SETUP_WORKER"

        fun setupWorker(context: Context) {
            val intent = Intent(context, MyIntentService::class.java)
            intent.action = ACTION_SETUP_WORKER
            context.startService(intent)
        }
    }
}