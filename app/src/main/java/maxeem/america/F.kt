//package maxeem.america
//
//import android.net.Uri
//import android.util.Log
//import android.widget.Toast
//import androidx.core.content.edit
//import com.tonyodev.fetch2.*
//import com.tonyodev.fetch2core.DownloadBlock
//import com.tonyodev.fetch2core.Downloader
//import com.tonyodev.fetch2core.FetchObserver
//import com.tonyodev.fetch2core.Reason
//import com.tonyodev.fetch2rx.RxFetch
//import io.reactivex.disposables.Disposable
//import java.io.File
//import java.net.URI
//
//object F { //Fetch
//
//    private object Info {
////        val j = JsonReader(App.instance().resources.openRawResource(R.raw.experiences))
//        const val pref = "F"
//        val url = "http://d2zlsagv0ouax1.cloudfront.net/assets/poshmarkets/api/market-men@3x-51b73e01b7ce82ca1ff3666955ed6607.png"
//        val file = "men.png"
////        val file = https://poshmark.com/api/meta/experiences"
////        val file = "experiences.json"
//    }
//    private val prefs = App.instance.getSharedPreferences(Info.pref, 0)
//
//    init {
//        if (BuildConfig.DEBUG)
//            Log.d("posh", "F.init() ${this}")
//        RxFetch.setDefaultRxInstanceConfiguration(FetchConfiguration.Builder(App.instance)
//            .enableRetryOnNetworkGain(true)
//            .setDownloadConcurrentLimit(3)
//            .setHttpDownloader(HttpUrlConnectionDownloader(Downloader.FileDownloaderType.PARALLEL))
//        .build())
//    }
//
////    fun update() {
////        if (BuildConfig.DEBUG) {
////            App.instance().fileList().forEach {s: String -> Log.d("posh", "fileList: $s - ${File(App.instance().filesDir, s).length()}") }
////        }
////        fetch(Info.url, File(App.instance().filesDir, Info.file))
////    }
//    private fun fetch(url : String, out: File) : Disposable {
//        val req = Request(url, Uri.fromFile(out))
//        return RxFetch.getDefaultRxInstance()
//                .attachFetchObserversForDownload(req.id, object: FetchObserver<Download> {
//                    override fun onChanged(download: Download, reason: Reason) {
//Log.d("posh", "onChanged: " + download + " - " + reason)
//                    }
//                })
//        .addListener(object : AbstractFetchListener() {
//            override fun onCompleted(download: Download) {
//                val f = File(URI.create(download.file))
//Log.d("posh", "onCompleted " + f + " + " + f.length())
//Log.d("posh", "onCompleted " + download.url + " - " + f + " - " + f.length() + " - " + f.name + "; " + Thread.currentThread())
//                if (f.length() > 1) {
//                    assert(Info.url == download.url)
//                    assert(Info.file == f.name)
//                    prefs.edit {
//                        putString(Info.url, Info.file)
//                    }
//Log.d("posh", "onCompleted success file: " + download.file + " - " + f.length())
//                }
//            }
//            override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
//Log.d("posh", "onStarted: " + download)
//            }
//            override fun onError(download: Download, error: Error, throwable: Throwable?) {
//Log.d("posh", "onError: " + download + "; " + error, throwable)
//            }
//            override fun onCancelled(download: Download) {
//Log.d("posh", "onCancelled: " + download)
//            }
//            override fun onWaitingNetwork(download: Download) {
//Log.d("posh", "onWaitingNetwork: " + download)
//            }
//            override fun onDeleted(download: Download) {
//Log.d("posh", "onDeleted: " + download)
//            }
//            override fun onRemoved(download: Download) {
//Log.d("posh", "onRemoved: " + download)
//            }
//            override fun onPaused(download: Download) {
//Log.d("posh", "onPaused: " + download)
//            }
//            override fun onAdded(download: Download) {
//Log.d("posh", "onAdded: " + download)
//            }
//            override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
//Log.d("posh", "onQueued: " + download)
//            }
//            override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) {
//Log.d("posh", "onDownloadBlockUpdated: " + download)
//            }
//            override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
//Log.d("posh", "onProgress: " + download)
//            }
//            override fun onResumed(download: Download) {
//Log.d("posh", "onResumed: " + download)
//            }
//        })
//        .enqueue(Request(url, Uri.fromFile(out)).apply {
//            priority = Priority.HIGH
//            networkType = NetworkType.ALL
////            addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG")
//        }).flowable
//        .subscribe({ request ->
//Log.d("posh", "subscribe onNext: " + request)
//App.instance.fileList().forEach {s: String? -> Log.d("posh", "fetch onNext: fileList: $s") }
//Toast.makeText(App.instance, "accept request " + request, Toast.LENGTH_LONG).show()
//        }, { throwable ->
//Log.d("posh", "subscribe onError: " + throwable)
//Toast.makeText(App.instance, "accept throwable: " + throwable, Toast.LENGTH_LONG).show()
//        })
//    }
//}