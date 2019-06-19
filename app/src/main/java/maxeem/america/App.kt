package maxeem.america

import android.app.Application

class App : Application() {

    companion object {
        private lateinit var _instance : App
        val instance : App
            get() = _instance
    }

    init {
        _instance = this
    }

}
