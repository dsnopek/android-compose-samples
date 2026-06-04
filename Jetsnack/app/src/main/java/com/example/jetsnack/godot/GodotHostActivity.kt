package com.example.jetsnack.godot

import android.os.Bundle
import android.os.Process
import androidx.fragment.app.FragmentActivity
import com.example.jetsnack.R
import org.godotengine.godot.Godot
import org.godotengine.godot.GodotFragment
import org.godotengine.godot.GodotHost
import org.godotengine.godot.plugin.GodotPlugin

/**
 * Hosts the [GodotFragment] and implements [GodotHost] so that runtime plugins
 * (such as [AppPlugin]) are registered with the embedded Godot instance. Without
 * [getHostPlugins], Godot can't find the "AppPlugin" singleton.
 */
class GodotHostActivity : FragmentActivity(), GodotHost {

    private var godotFragment: GodotFragment? = null

    private var appPlugin: AppPlugin? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_godot_host)
        val currentGodotFragment = supportFragmentManager.findFragmentById(R.id.godot_fragment_container)
        if (currentGodotFragment is GodotFragment) {
            godotFragment = currentGodotFragment
        } else {
            godotFragment = GodotFragment().also {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.godot_fragment_container, it)
                    .commitNowAllowingStateLoss()
            }
        }
    }

    private fun initAppPluginIfNeeded(godot: Godot) {
        if (appPlugin == null) {
            appPlugin = AppPlugin(godot)
        }
    }

    override fun getActivity() = this

    override fun getGodot() = godotFragment?.godot

    override fun getHostPlugins(godot: Godot): Set<GodotPlugin> {
        initAppPluginIfNeeded(godot)
        return setOf(appPlugin!!)
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
        super.onDestroy()
        Process.killProcess(Process.myPid())
    }
}
