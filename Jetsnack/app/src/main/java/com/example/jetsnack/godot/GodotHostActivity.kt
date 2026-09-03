package com.example.jetsnack.godot

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Process
import org.godotengine.godot.Godot
import org.godotengine.godot.GodotHost

class GodotHostActivity : Activity(), GodotHost {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val godot = getGodot()
        if (godot.initEngine(this, emptyList(), setOf(AppPlugin(godot)))) {
            setContentView(godot.onInitRenderView(this))
        } else {
            finish()
        }
    }

    override fun onNewIntent(newIntent: Intent) {
        intent = newIntent
        super.onNewIntent(newIntent)
    }

    override fun onStart() {
        super.onStart()
        getGodot().onStart(this)
    }

    override fun onResume() {
        super.onResume()
        getGodot().onResume(this)
    }

    override fun onPause() {
        super.onPause()
        getGodot().onPause(this)
    }

    override fun onStop() {
        super.onStop()
        getGodot().onStop(this)
    }

    override fun getActivity() = this

    override fun getGodot(): Godot = Godot.getInstance(this)

    /**
     * Moves the current activity to the background.
     */
    fun moveToBackground() {
        runOnUiThread { moveTaskToBack(true) }
    }

    /**
     * Finishes this activity, returning to whatever launched it.
     */
    fun terminate() {
        runOnUiThread { finish() }
    }

    override fun onGodotForceQuit(instance: Godot) {
        terminate()
    }

    /**
     * Kill this (separate, ":godot") process whenever the activity is destroyed
     * via [terminate], the system back button, or the OS. Godot can't be
     * re-initialized in a process that already ran it, so the process must die to
     * guarantee a fresh start next launch. This runs in the ":godot" process only,
     * leaving the app's main process untouched.
     */
    override fun onDestroy() {
        getGodot().onDestroy(this)
        super.onDestroy()
        Process.killProcess(Process.myPid())
    }
}
