package com.example.singin

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import com.example.singin.util.PrefUtil
import com.example.singin.util.TimerExpiredReceiver
import kotlinx.android.synthetic.main.activity_parking.*
import java.util.*



class ParkingActivity : AppCompatActivity() {


    companion object{
        fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long): Long{
            val wakeUpTime = (nowSeconds + secondsRemaining) * 1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
            PrefUtil.setAlarmSetTime(nowSeconds, context)
            return wakeUpTime
        }

        fun removeAlarm(context: Context){
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context,0,intent,0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0,context)
        }

        val nowSeconds: Long
        get() = Calendar.getInstance().timeInMillis / 1000
    }

    enum class TimerState{
        Stopped, Running, Paused
    }

    private lateinit var timer: CountDownTimer
    private var timerLengthSecond = 0L
    private  var timerState = TimerState.Stopped

    private var secondsRemaining = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parking)

        b_start.setOnClickListener { v ->
            startTimer()
            timerState = TimerState.Running
            updateButtons()
        }

        b_stop.setOnClickListener { v ->
            timer.cancel()
            onTimerFinished()
        }
    }

    override fun onResume() {
        super.onResume()

        initTimer()

        removeAlarm(this)

        //hide notification
    }


    override fun onPause() {
        super.onPause()

        if (timerState == TimerState.Running){
            timer.cancel()
            val wakeUpTime = setAlarm(this, nowSeconds, secondsRemaining)
        }
        else if(timerState == TimerState.Paused){
        }

        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSecond,this)
        PrefUtil.setSecondsRemaining(secondsRemaining,this)
        PrefUtil.setTimerState(timerState,this)
    }


     private fun initTimer(){
         timerState = PrefUtil.getTimerState(this)
         if (timerState == TimerState.Stopped)
             setNewTimerLength()
         else
             setPreviousTimerLength()
         secondsRemaining = if (timerState == TimerState.Running || timerState == TimerState.Paused)
             PrefUtil.getSecondsRemaining(this)
         else
             timerLengthSecond

         val alarmSetTime = PrefUtil.getAlarmSetTime(this)
         if (alarmSetTime > 0)
             secondsRemaining -= nowSeconds - alarmSetTime

         if(secondsRemaining <= 0)
             onTimerFinished()
         else if (timerState == TimerState.Running)
             startTimer()

         updateButtons()
         updateCountdownUI()
    }

    private fun onTimerFinished(){
        timerState = TimerState.Stopped

        setNewTimerLength()

        t_countdown.progress = 0

        PrefUtil.setSecondsRemaining(timerLengthSecond,this)
        secondsRemaining = timerLengthSecond

        updateButtons()
        updateCountdownUI()
    }

    private fun startTimer(){
        timerState = TimerState.Running

        timer = object  : CountDownTimer(secondsRemaining * 1000, 1000){
            override fun onFinish() = onTimerFinished()

            override fun onTick(millisUntilFinished: Long){
                secondsRemaining = millisUntilFinished / 1000
                updateCountdownUI()
            }
        }.start()
    }

    private fun setNewTimerLength(){
        val lengthInMinutes = PrefUtil.getTimerLength(this)
        timerLengthSecond = (lengthInMinutes * 60L)
        t_countdown.max = timerLengthSecond.toInt()
    }

    private fun setPreviousTimerLength(){
        timerLengthSecond = PrefUtil.getPreviousTimerLengthSeconds(this)
        t_countdown.max = timerLengthSecond.toInt()
    }

    private fun updateCountdownUI(){
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        textView_countdown.text = "$minutesUntilFinished:${
            if (secondsStr.length == 2) secondsStr
        else "0" + secondsStr}"
        t_countdown.progress = (timerLengthSecond - secondsRemaining).toInt()
    }
    private fun updateButtons(){
        when (timerState){
            TimerState.Running ->{
                b_stop.isEnabled = true
                b_start.isEnabled = false
            }
            TimerState.Stopped ->{
                b_stop.isEnabled = false
                b_start.isEnabled = true
            }
        }
    }
}