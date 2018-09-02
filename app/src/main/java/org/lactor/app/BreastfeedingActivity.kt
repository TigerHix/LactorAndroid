package org.lactor.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_breastfeeding.*
import java.util.concurrent.TimeUnit

class BreastfeedingActivity : AppCompatActivity() {

    lateinit var timers: List<Timer>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_breastfeeding)

        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_close)

        breastfeedingLatchingSpinner.adapter =
                ArrayAdapter.createFromResource(this, R.array.breastfeeding_latching, android.R.layout.simple_spinner_item)
                        .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        breastfeedingBabyAlertnessSpinner.adapter =
                ArrayAdapter.createFromResource(this, R.array.breastfeeding_baby_alertness, android.R.layout.simple_spinner_item)
                        .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        breastfeedingProblemsSpinner.adapter =
                ArrayAdapter.createFromResource(this, R.array.breastfeeding_problems, android.R.layout.simple_spinner_item)
                        .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        breastfeedingWave.setAmplitudeRatio(20)
        breastfeedingWave.progressValue = 20

        fun stopTimer(timer: Timer) {
            timer.started = false
            breastfeedingWave.setAnimDuration(2000)
            breastfeedingWave.setAmplitudeRatio(20)
            breastfeedingWave.progressValue = 20
            timer.text.animate().scaleX(1f / 1.2f).scaleY(1f / 1.2f).setInterpolator(AnticipateOvershootInterpolator()).setDuration(300L).start()
            timer.button.animate().scaleX(1f / 1.05f).scaleY(1f / 1.05f).setInterpolator(AnticipateOvershootInterpolator()).setDuration(300L).start()
            timer.button.text = "Start"
            timer.subscription?.dispose()
            if (timer.lastStartTime > 0) timer.passedTime = timer.millis
        }

        timers = listOf(Timer(timerLeft, titleLeft, startLeft), Timer(timerRight, titleRight, startRight))
        timers.forEach { timer ->
            timer.button.setOnClickListener {
                if (!timer.started) {
                    stopTimer(timers.first { it != timer })
                    timer.started = true
                    breastfeedingWave.setAnimDuration(1000)
                    breastfeedingWave.setAmplitudeRatio(60)
                    breastfeedingWave.progressValue = 85
                    timer.text.animate().scaleX(1.2f).scaleY(1.2f).setInterpolator(AnticipateOvershootInterpolator()).setDuration(300L).start()
                    timer.button.animate().scaleX(1.05f).scaleY(1.05f).setInterpolator(AnticipateOvershootInterpolator()).setDuration(300L).start()
                    timer.button.text = "Pause"
                    timer.lastStartTime = System.currentTimeMillis()
                    timer.subscription = Observable.interval(0, 100, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                timer.text.text = String.format("%02d:%02d",
                                        TimeUnit.MILLISECONDS.toMinutes(timer.millis),
                                        TimeUnit.MILLISECONDS.toSeconds(timer.millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timer.millis))
                                )
                            }
                } else {
                    stopTimer(timer)
                }

            }
            stopTimer(timer)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.breastfeeding, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> {
                return true
            }
            R.id.action_submit -> {
                return true
            }
        }
        return false
    }

}

data class Timer(val text: TextView, val title: TextView, val button: Button) {

    var initialized = false
    var started = false
    var subscription: Disposable? = null
    var passedTime = 0L
    var lastStartTime = 0L
    val millis get() = passedTime + (System.currentTimeMillis() - lastStartTime)

}