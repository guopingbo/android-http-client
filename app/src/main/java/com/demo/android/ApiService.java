package com.demo.android;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    static String baseURL = "http://gd.topgamers.cn:8080/";
    @Headers("Content-Type:application/json")
    @POST("login/login")
    Observable<ApiResultObject<User>> getTokenByPost(@Body RequestBody body);
}
