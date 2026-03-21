package org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.service

import okhttp3.MultipartBody
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.data.ResponseMessage
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.data.*

interface IEthnographyRepository {

    // ----------------------------------
    // Auth
    // ----------------------------------

    suspend fun postRegister(
        request: RequestAuthRegister
    ): ResponseMessage<ResponseAuthRegister?>

    suspend fun postLogin(
        request: RequestAuthLogin
    ): ResponseMessage<ResponseAuthLogin?>

    suspend fun postLogout(
        request: RequestAuthLogout
    ): ResponseMessage<String?>

    suspend fun postRefreshToken(
        request: RequestAuthRefreshToken
    ): ResponseMessage<ResponseAuthLogin?>

    // ----------------------------------
    // Users
    // ----------------------------------

    suspend fun getUserMe(
        authToken: String
    ): ResponseMessage<ResponseUser?>

    suspend fun putUserMe(
        authToken: String,
        request: RequestUserChange
    ): ResponseMessage<String?>

    suspend fun putUserMePassword(
        authToken: String,
        request: RequestUserChangePassword
    ): ResponseMessage<String?>

    suspend fun putUserMePhoto(
        authToken: String,
        file: MultipartBody.Part
    ): ResponseMessage<String?>

    // ----------------------------------
    // Ethnographies
    // ----------------------------------

    suspend fun getEthnographies(
        authToken: String,
        search: String? = null
    ): ResponseMessage<ResponseEthnographies?>

    suspend fun postEthnography(
        authToken: String,
        request: EthnographyRequest
    ): ResponseMessage<ResponseEthnographyAdd?>

    suspend fun getEthnographyById(
        authToken: String,
        id: String
    ): ResponseMessage<ResponseEthnography?>

    suspend fun putEthnography(
        authToken: String,
        id: String,
        request: EthnographyRequest
    ): ResponseMessage<String?>

    suspend fun putEthnographyImage(
        authToken: String,
        id: String,
        file: MultipartBody.Part
    ): ResponseMessage<String?>

    suspend fun deleteEthnography(
        authToken: String,
        id: String
    ): ResponseMessage<String?>
}