package com.demo.android;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @Headers("Content-Type:application/json")
    @POST("doc.html#/default/登陆事务/login")
    Observable<User> getTokenByPost(@Body RequestBody body);
}
