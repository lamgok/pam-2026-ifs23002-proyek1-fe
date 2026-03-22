package org.delcom.pam_2026_ifs23002_proyek1_fe.helper

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.delcom.pam_2026_ifs23002_proyek1_fe.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ToolsHelper {
    fun getUserImage(userId: String, t: String = "0"): String{
        return "${BuildConfig.BASE_URL}images/users/${userId}?t=${t}"
    }

    fun getEthnographyImage(ethnographyId: String, t: String = "0"): String{
        return "${BuildConfig.BASE_URL}images/ethnographies/${ethnographyId}?t=${t}"
    }

    fun String.toRequestBodyText(): RequestBody {
        return this.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    fun uriToMultipart(
        context: Context,
        uri: Uri,
        partName: String
    ): MultipartBody.Part {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
        
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.$extension")
        
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        val requestFile = RequestBody.create(mimeType.toMediaTypeOrNull(), file)

        return MultipartBody.Part.createFormData(
            partName,
            file.name,
            requestFile
        )
    }
}
