package com.example.singin.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.singin.ParkingActivity
import com.example.singin.util.PrefUtil

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        PrefUtil.setTimerState(ParkingActivity.TimerState.Stopped,context)
        PrefUtil.setAlarmSetTime(0,context)
    }
}