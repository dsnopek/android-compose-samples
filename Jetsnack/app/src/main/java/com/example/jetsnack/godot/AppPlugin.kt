package com.example.jetsnack.godot

import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.UsedByGodot

class AppPlugin(godot: Godot) : GodotPlugin(godot) {

    override fun getPluginName() = "AppPlugin"

    /**
     * Returns the list of snack image filenames (e.g. "cupcake.jpg") for the
     * snacks in the user's cart. The Android side passes these as an Intent
     * extra when launching [GodotHostActivity]; we read them back here.
     */
    @UsedByGodot
    fun getSnackFilenames(): Array<String> =
        activity?.intent?.getStringArrayExtra(EXTRA_SNACK_FILENAMES) ?: emptyArray()

    /**
     * Called from Godot to leave the embedded view and return to the app.
     * Finishes the host activity; the host kills the (separate) Godot process in
     * its onDestroy, so the next launch starts fresh (Godot can't re-initialize
     * within a process that already ran it).
     */
    @UsedByGodot
    fun goBack() {
        activity?.moveTaskToBack(true)
    }

    companion object {
        const val EXTRA_SNACK_FILENAMES = "com.example.jetsnack.godot.SNACK_FILENAMES"
    }
}
