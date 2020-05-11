package br.activityApp.data.local

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import br.activityApp.data.remote.ApiConfiguration
import br.activityApp.data.remote.GaitService
import br.activityApp.data.remote.GaitServiceFactory
import br.activityApp.utils.FileHandler
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody

class SyncHelper constructor(context: Context) {

    private lateinit var apiConfiguration: ApiConfiguration
    private lateinit var gaitService: GaitService

    companion object {
        private const val PREF_APP_PACKAGE_NAME = "br.activityapp"
        private const val PREF_KEY_LAST_SYNC = "last_sync"
    }

    private val prefs: SharedPreferences

    init {
        prefs = context.getSharedPreferences(PREF_APP_PACKAGE_NAME, Context.MODE_PRIVATE)
        apiConfiguration = GaitServiceFactory.makeApiConfiguration()
        gaitService = GaitServiceFactory.makeGaitService(apiConfiguration, true)
    }

    fun updateLastSyncTime() {
        prefs.edit().putLong(PREF_KEY_LAST_SYNC, System.currentTimeMillis()).apply()
    }

    fun getLastSyncTime(): Long {
        return prefs.getLong(PREF_KEY_LAST_SYNC, 0)
    }

    fun syncFile(file: FileItem): Single<ResponseBody> {
        val file = FileHandler.getFile(file?.filename)

        // create RequestBody instance from file
        val requestFile = RequestBody.create(
                MediaType.parse("text/*"),
                file
        )

        // MultipartBody.Part is used to send also the actual file name
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        return gaitService.uploadData(body)
    }
}