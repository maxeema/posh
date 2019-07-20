package maxeem.america.posh

import android.app.Activity
import android.os.Bundle

class AL : Activity() {//Launch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG)
            U.debug(intent.dataString.takeUnless { it.isNullOrBlank() } ?: "nothing it is")
        intent.dataString?.takeIf { it.isNotBlank() && D.validate(it) }
            ?.also { P.go(this, D.actionById(it)!!) }
        finish()
    }

}