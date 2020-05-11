package br.activityApp.data.remote

import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*


interface GaitService {

    @Multipart
    @POST("upload")
    fun uploadData(@Part file: MultipartBody.Part): Single<ResponseBody>

    @GET("{userId}/data")
    fun getData(@Path("userId") userId: String)

}
