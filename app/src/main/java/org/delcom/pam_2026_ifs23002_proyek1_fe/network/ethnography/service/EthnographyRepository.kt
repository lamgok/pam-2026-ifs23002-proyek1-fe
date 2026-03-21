package org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.service

import okhttp3.MultipartBody
import org.delcom.pam_2026_ifs23002_proyek1_fe.helper.SuspendHelper
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.data.ResponseMessage
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.data.*

class EthnographyRepository(
    private val apiService: EthnographyApiService
) : IEthnographyRepository {

    override suspend fun postRegister(request: RequestAuthRegister): ResponseMessage<ResponseAuthRegister?> =
        SuspendHelper.safeApiCall { apiService.postRegister(request) }

    override suspend fun postLogin(request: RequestAuthLogin): ResponseMessage<ResponseAuthLogin?> =
        SuspendHelper.safeApiCall { apiService.postLogin(request) }

    override suspend fun postLogout(request: RequestAuthLogout): ResponseMessage<String?> =
        SuspendHelper.safeApiCall { apiService.postLogout(request) }

    override suspend fun postRefreshToken(request: RequestAuthRefreshToken): ResponseMessage<ResponseAuthLogin?> =
        SuspendHelper.safeApiCall { apiService.postRefreshToken(request) }

    override suspend fun getUserMe(authToken: String): ResponseMessage<ResponseUser?> =
        SuspendHelper.safeApiCall { apiService.getUserMe("Bearer $authToken") }

    override suspend fun putUserMe(authToken: String, request: RequestUserChange): ResponseMessage<String?> =
        SuspendHelper.safeApiCall { apiService.putUserMe("Bearer $authToken", request) }

    override suspend fun putUserMePassword(authToken: String, request: RequestUserChangePassword): ResponseMessage<String?> =
        SuspendHelper.safeApiCall { apiService.putUserMePassword("Bearer $authToken", request) }

    override suspend fun putUserMePhoto(authToken: String, file: MultipartBody.Part): ResponseMessage<String?> =
        SuspendHelper.safeApiCall { apiService.putUserMePhoto("Bearer $authToken", file) }

    override suspend fun getEthnographies(authToken: String, search: String?): ResponseMessage<ResponseEthnographies?> =
        SuspendHelper.safeApiCall { apiService.getEthnographies("Bearer $authToken", search) }

    override suspend fun postEthnography(authToken: String, request: EthnographyRequest): ResponseMessage<ResponseEthnographyAdd?> =
        SuspendHelper.safeApiCall { apiService.postEthnography("Bearer $authToken", request) }

    override suspend fun getEthnographyById(authToken: String, id: String): ResponseMessage<ResponseEthnography?> =
        SuspendHelper.safeApiCall { apiService.getEthnographyById("Bearer $authToken", id) }

    override suspend fun putEthnography(authToken: String, id: String, request: EthnographyRequest): ResponseMessage<String?> =
        SuspendHelper.safeApiCall { apiService.putEthnography("Bearer $authToken", id, request) }

    override suspend fun putEthnographyImage(authToken: String, id: String, file: MultipartBody.Part): ResponseMessage<String?> =
        SuspendHelper.safeApiCall { apiService.putEthnographyImage("Bearer $authToken", id, file) }

    override suspend fun deleteEthnography(authToken: String, id: String): ResponseMessage<String?> =
        SuspendHelper.safeApiCall { apiService.deleteEthnography("Bearer $authToken", id) }
}
