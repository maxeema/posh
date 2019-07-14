package maxeem.america

import android.app.Application

val app = App.instance

class App : Application() {

    companion object {
        const val TAG = "posh"
        private var lazyInstance : (()->App)? = null
        val instance : App by lazy {
            lazyInstance!!.also { lazyInstance = null; }()
        }
    }

    init {
        lazyInstance = { this }
    }

}

