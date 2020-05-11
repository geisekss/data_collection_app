package br.activityApp.intro.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Toast
import br.activityApp.CalibrateActivity
import br.activityApp.R
import br.activityApp.data.local.FileItem

import br.activityApp.data.local.SyncHelper
import br.activityApp.data.remote.ApiConfiguration
import br.activityApp.data.remote.GaitService
import br.activityApp.data.remote.GaitServiceFactory
import br.activityApp.utils.FileHandler
import br.activityApp.utils.SoundPlayer
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody


class MainActivity : Activity() {

    private lateinit var syncHelper: SyncHelper
    private lateinit var filesAdapter: FilesAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SoundPlayer.getInstance().initialize(this)
        syncHelper = SyncHelper(this)


        initUiComponents()
    }

    private fun initUiComponents() {
        var btnSync = findViewById(R.id.btn_sync)

        btnSync.setOnClickListener {
            syncData()
        }
    }

    override fun onResume() {
        super.onResume()
        loadFilesList()
    }

    fun initCalibrateActivity(v: View) {
        val intent = Intent(this, CalibrateActivity::class.java)
        startActivity(intent)
    }


    private fun syncData() {
        val lastSync = syncHelper.getLastSyncTime()
        val unsyncedFiles = FileHandler.getUnsyncedFiles(lastSync)

        for (file in unsyncedFiles) {
            syncHelper.syncFile(file)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(object : SingleObserver<ResponseBody> {
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onSuccess(response: ResponseBody) {
                            Log.d("LOG", "success! " + response.toString())
                        }

                        override fun onError(e: Throwable) {
                            Log.d("LOG", e.toString())
                        }
                    })
            Log.d("LOG", ">>> unsinced: " + file.toString())
        }

        Toast.makeText(this, "Syncing...", Toast.LENGTH_SHORT).show()

//        syncHelper.updateLastSyncTime()
//        filesAdapter.setLastSync(System.currentTimeMillis())
    }

    private fun loadFilesList() {
        val lastSync = syncHelper.getLastSyncTime()
        val files = FileHandler.getAllFiles()

        val rvFiles = findViewById(R.id.rv_files) as RecyclerView
        rvFiles.layoutManager = LinearLayoutManager(this)

        filesAdapter = FilesAdapter(lastSync)
        filesAdapter.setFilesList(files)

        rvFiles.adapter = filesAdapter
    }
}
