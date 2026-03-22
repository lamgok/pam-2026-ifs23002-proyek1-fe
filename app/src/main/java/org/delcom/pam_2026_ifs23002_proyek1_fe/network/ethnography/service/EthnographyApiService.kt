package org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.service

import okhttp3.MultipartBody
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.data.ResponseMessage
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.data.*
import retrofit2.http.*

interface EthnographyApiService {
    // ----------------------------------
    // Auth
    // ----------------------------------

    @POST("auth/register")
    suspend fun postRegister(
        @Body request: RequestAuthRegister
    ): ResponseMessage<ResponseAuthRegister?>

    @POST("auth/login")
    suspend fun postLogin(
        @Body request: RequestAuthLogin
    ): ResponseMessage<ResponseAuthLogin?>

    @POST("auth/logout")
    suspend fun postLogout(
        @Body request: RequestAuthLogout
    ): ResponseMessage<String?>

    @POST("auth/refresh-token")
    suspend fun postRefreshToken(
        @Body request: RequestAuthRefreshToken
    ): ResponseMessage<ResponseAuthLogin?>

    // ----------------------------------
    // Users
    // ----------------------------------

    @GET("users/me")
    suspend fun getUserMe(
        @Header("Authorization") authToken: String
    ): ResponseMessage<ResponseUser?>

    @PUT("users/me")
    suspend fun putUserMe(
        @Header("Authorization") authToken: String,
        @Body request: RequestUserChange
    ): ResponseMessage<String?>

    @PUT("users/me/password")
    suspend fun putUserMePassword(
        @Header("Authorization") authToken: String,
        @Body request: RequestUserChangePassword
    ): ResponseMessage<String?>

    @Multipart
    @PUT("users/me/photo")
    suspend fun putUserMePhoto(
        @Header("Authorization") authToken: String,
        @Part file: MultipartBody.Part
    ): ResponseMessage<String?>

    // ----------------------------------
    // Ethnographies
    // ----------------------------------

    @GET("ethnographies")
    suspend fun getEthnographies(
        @Header("Authorization") authToken: String,
        @Query("search") search: String? = null,
        @Query("region") region: String? = null,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): ResponseMessage<ResponseEthnographies?>

    @Headers("Content-Type: application/json")
    @POST("ethnographies")
    suspend fun postEthnography(
        @Header("Authorization") authToken: String,
        @Body request: EthnographyRequest
    ): ResponseMessage<ResponseEthnographyAdd?>

    @GET("ethnographies/{id}")
    suspend fun getEthnographyById(
        @Header("Authorization") authToken: String,
        @Path("id") id: String
    ): ResponseMessage<ResponseEthnography?>

    @Headers("Content-Type: application/json")
    @PUT("ethnographies/{id}")
    suspend fun putEthnography(
        @Header("Authorization") authToken: String,
        @Path("id") id: String,
        @Body request: EthnographyRequest
    ): ResponseMessage<String?>

    @Multipart
    @PUT("ethnographies/{id}/image")
    suspend fun putEthnographyImage(
        @Header("Authorization") authToken: String,
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): ResponseMessage<String?>

    @DELETE("ethnographies/{id}")
    suspend fun deleteEthnography(
        @Header("Authorization") authToken: String,
        @Path("id") id: String
    ): ResponseMessage<String?>
}
