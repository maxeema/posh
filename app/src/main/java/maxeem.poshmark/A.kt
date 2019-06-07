package maxeem.poshmark

import android.app.Activity
import android.content.ActivityNotFoundException
import android.os.Bundle
import android.widget.Toast

class A : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            P.sell(this)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.install_first, Toast.LENGTH_LONG).show()
            try {
                P.install(this)
            } catch (e: ActivityNotFoundException) { } //maybe we're running on emulator without Play Store
        }
        finish()
    }

}