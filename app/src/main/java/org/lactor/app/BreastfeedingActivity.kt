package org.lactor.app

import android.os.Bundle
import android.support.v4.view.LayoutInflaterCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.mikepenz.iconics.context.IconicsLayoutInflater2
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_breastfeeding.*
import java.util.concurrent.TimeUnit

class BreastfeedingActivity : AppCompatActivity() {

    private lateinit var timers: List<Timer>

    override fun onCreate(savedInstanceState: Bundle?) {
        LayoutInflaterCompat.setFactory2(layoutInflater, IconicsLayoutInflater2(delegate))
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
                    timer.button.animate().scaleX(1.1f).scaleY(1.1f).setInterpolator(AnticipateOvershootInterpolator()).setDuration(300L).start()
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
        }
    }

    private fun stopTimer(timer: Timer) {
        if (!timer.started) return
        timer.started = false
        breastfeedingWave.setAnimDuration(2000)
        breastfeedingWave.setAmplitudeRatio(20)
        breastfeedingWave.progressValue = 20
        timer.text.animate().scaleX(1f).scaleY(1f).setInterpolator(AnticipateOvershootInterpolator()).setDuration(300L).start()
        timer.button.animate().scaleX(1f).scaleY(1f).setInterpolator(AnticipateOvershootInterpolator()).setDuration(300L).start()
        timer.button.text = "Start"
        timer.subscription?.dispose()
        if (timer.lastStartTime > 0) timer.passedTime = timer.millis
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.breastfeeding, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> {
                MaterialDialog(this)
                        .title(text = "Reset?")
                        .message(text = "Are you sure you want to clear the timers and answers?")
                        .positiveButton(text = "Yes") {
                            timers.forEach {
                                stopTimer(it)
                                it.passedTime = 0L
                                it.lastStartTime = 0L
                                it.subscription?.dispose()
                                it.subscription = null
                                it.text.text = "00:00"
                            }
                            breastfeedingLatchingSpinner.setSelection(0)
                            breastfeedingBabyAlertnessSpinner.setSelection(0)
                            breastfeedingProblemsSpinner.setSelection(0)
                        }
                        .negativeButton(text = "No") {
                            it.dismiss()
                        }
                        .show()
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
    var started = false
    var subscription: Disposable? = null
    var passedTime = 0L
    var lastStartTime = 0L
    val millis get() = passedTime + (System.currentTimeMillis() - lastStartTime)

}