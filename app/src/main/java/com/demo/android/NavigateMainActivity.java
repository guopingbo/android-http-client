package com.demo.android;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.ComponentActivity;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import android.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class NavigateMainActivity extends ComponentActivity {
    public static final String TAG = NavigateMainActivity.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    public static WebView m_MainWebView;
    public static String m_CurrentNavigatingURL;
    public static CookieManager m_CookieManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigating_main);
        setWebView((WebView) findViewById(R.id.webView));

        //--------->following is test codes...
        LoginParam u = new LoginParam();
        u.user_name = "kkk";
        u.password = "pass";

        navigateURL(u, "https://www.baidu.com");
        //--------->above is test codes...
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void setWebView(@NonNull WebView webView) {
        if (webView == null) {
            Log.e(TAG, "setWebView paras error!");
            throw new IllegalArgumentException("setWebView paras error");
        } else {
            m_MainWebView = webView;
            m_CookieManager = CookieManager.getInstance();

            m_CookieManager.setAcceptCookie(true);
            m_CookieManager.setAcceptThirdPartyCookies(m_MainWebView, true);

            WebSettings settings = m_MainWebView.getSettings();
            settings.setJavaScriptEnabled(true);
        }
    }

    public static boolean navigateURL(@NonNull LoginParam loginParam, @NonNull String url) {
        if (loginParam == null || loginParam.user_name == null || url == null
                || loginParam.user_name.isEmpty() || url.isEmpty()) {
            Log.e(TAG, "navigateURL paras error!");
            throw new IllegalArgumentException("navigateURL paras error");
        }

        if (m_CurrentNavigatingURL == null) {
            m_CurrentNavigatingURL = url;
            byte[] sha_value = HmacSha256Util.hsha256(("NonArp15MonArp15" + loginParam.user_name).getBytes(StandardCharsets.UTF_8));
            loginParam.password = Base64.encodeToString(sha_value, Base64.DEFAULT);
            return execNavigate(loginParam);
        }

        return false;
    }

    private static boolean execNavigate(LoginParam loginParam) {
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ApiService.baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build();

            Gson gson = new Gson();
            String jsonStr = gson.toJson(loginParam);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonStr);

            ApiService service = retrofit.create(ApiService.class);
            Observable<ApiResultObject<User>> call = service.getTokenByPost(requestBody);

            call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ApiResultObject<User>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }
                    @Override
                    public void onNext(@NonNull ApiResultObject<User> apiResultObject) {

                        User user = apiResultObject.data;

                        if (Objects.equals(apiResultObject.code, "0") && user.token != null && !user.token.isEmpty()) {
                            // 使用user对象
                            m_CookieManager.setCookie(m_CurrentNavigatingURL, user.token);
                            Map<String, String> map = new HashMap<String, String>();
                            map.put("token", user.token);
                            m_MainWebView.loadUrl(m_CurrentNavigatingURL, map);
                            m_MainWebView.setWebViewClient(new WebViewClient() {
                                @Override
                                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                    //使用WebView加载显示url
                                    view.loadUrl(url);
                                    //返回true
                                    return true;
                                }
                            });

                            m_CurrentNavigatingURL = null;

                        } else {
                            Log.e(TAG, "apiResultObject.code:" + Objects.requireNonNull(apiResultObject.code)
                                                + ", apiResultObject.msg:" + Objects.requireNonNull(apiResultObject.msg)
                                                + ", apiResultObject.data:" + new Gson().toJson(apiResultObject.data)
                            );
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        //报错调试
                        //Log.e(TAG, "HTTP request failed!");
                        Log.e(TAG, Log.getStackTraceString(e));
                        Log.e(TAG, Objects.requireNonNull(e.getLocalizedMessage()));
                    }

                    @Override
                    public void onComplete() {
                        Log.i(TAG, "complete now!");
                    }
                });

        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            Log.e(TAG, Objects.requireNonNull(e.getLocalizedMessage()));
            return false;
        }

        return true;
    }
}
