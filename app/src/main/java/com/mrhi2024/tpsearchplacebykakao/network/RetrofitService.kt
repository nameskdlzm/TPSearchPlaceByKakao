package com.mrhi2024.tpsearchplacebykakao.network

import com.mrhi2024.tpsearchplacebykakao.data.KakaoSearchPlaceResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface RetrofitService {

    // 카카오 로컬 검색 api 요청해주는 코드 만들어줘. 우선 응답 type :String
    @Headers("Authorization: KakaoAK 218ae28eb63a74438ec96a51e9568f9b")
    @GET("/v2/local/search/keyword.json")
    fun searchPlaceToString(@Query("query") query:String,@Query("x") longitude:String,@Query("y") letitude:String) : Call<String>

    // 카카오 로컬 검색 api 요청해주는 코드 만들어줘. 우선 응답 type :KakaoSearchPlaceResponse
    @Headers("Authorization: KakaoAK 218ae28eb63a74438ec96a51e9568f9b")
    @GET("/v2/local/search/keyword.json?sort=distance")
    fun searchPlace(@Query("query") query:String,@Query("x") longitude:String,@Query("y") letitude:String) : Call<KakaoSearchPlaceResponse>


}