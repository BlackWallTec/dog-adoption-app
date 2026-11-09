package com.app.android.data.dataSource.dataSourcesSolicitudes

import com.google.gson.annotations.SerializedName

import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
)

data class LoginResponseDto(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("expiresIn") val expiresIn: Long,
)

interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequestDto): LoginResponseDto
}