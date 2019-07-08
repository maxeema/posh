package maxeem.america

import android.app.Application

val app = App.instance

class App : Application() {

    companion object {
        const val TAG = "posh"
        private lateinit var _instance : App
        val instance get() = _instance
    }

    init {
        _instance = this
    }

}
