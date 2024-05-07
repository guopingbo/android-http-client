package com.demo.android;

import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.ComponentActivity;

import com.google.gson.Gson;

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
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class NavigateMainActivity extends ComponentActivity {
	public static WebView m_MainWebView;
	public static String m_CurrentNavigatingURL;
	public static CookieManager m_CookieManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setWebView((WebView) findViewById(R.id.webView));
	}
	public static void setWebView(WebView webView) {
		m_MainWebView = webView;
		m_CookieManager = CookieManager.getInstance();

		m_CookieManager.setAcceptCookie(true);
		m_CookieManager.setAcceptThirdPartyCookies(m_MainWebView, true);

		WebSettings settings = m_MainWebView.getSettings();
		settings.setJavaScriptEnabled(true);

		//--------->following is test codes...
		m_MainWebView.loadUrl("https://www.baidu.com");
		//系统默认会通过手机浏览器打开网页，为了能够直接通过WebView显示网页，则必须设置
		m_MainWebView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
			//使用WebView加载显示url
			view.loadUrl(url);
			//返回true
			return true;
			}
		});

		User u = new User();
		u.user_name = "";
		u.password = "";

		navigateURL(u, "https://www.baidu.com");
		//--------->above is test codes...
	}
	public static boolean navigateURL(User user, String url) {
		synchronized (NavigateMainActivity.class) {
			if (m_CurrentNavigatingURL == null) {
				m_CurrentNavigatingURL = url;
				return execNavigate(user);
			}

			return false;
		}
	}

	private static boolean execNavigate(User user) {
		try {
			Retrofit retrofit = new Retrofit.Builder()
					.baseUrl("http://gd.topgamers.cn:8080/")
					.addConverterFactory(GsonConverterFactory.create())
					.addCallAdapterFactory(RxJava3CallAdapterFactory.create())
					.build();

			Gson gson = new Gson();
			String jsonStr = gson.toJson(user);
			RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonStr);

			ApiService service = retrofit.create(ApiService.class);
			Observable<User> call = service.getTokenByPost(requestBody);

			call.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Observer<User>() {
					@Override
					public void onSubscribe(@NonNull Disposable d) {

					}
					@Override
					public void onNext(@NonNull User user) {
						if (user.token != null && !user.token.isEmpty()) {
							// 使用user对象
							m_CookieManager.setCookie(m_CurrentNavigatingURL, user.token);
							Map<String, String > map = new HashMap<String, String>();
							map.put("token", user.token);
							m_MainWebView.loadUrl(m_CurrentNavigatingURL, map);
							m_MainWebView.setWebViewClient(new WebViewClient(){
								@Override
								public boolean shouldOverrideUrlLoading(WebView view, String url) {
								//使用WebView加载显示url
								view.loadUrl(url);
								//返回true
								return true;
								}
							});

							synchronized (NavigateMainActivity.class) {
								m_CurrentNavigatingURL = null;
							}

						} else {
							Log.e("debug", "Token is empty! user_name:" + user.user_name);
						}
					}

					@Override
					public void onError(@NonNull Throwable e) {
						//报错调试
						Log.e("debug", Log.getStackTraceString(e));
						Log.e("debug", Objects.requireNonNull(e.getLocalizedMessage()));
					}

					@Override
					public void onComplete() {
						Log.i("info", "complete now!");
					}
				});
//			call.enqueue(new Callback<User>() {
//				@Override
//				public void onResponse(Call<User> request, Response<User> response) {
//					if (response.isSuccessful()) {
//						User user = response.body();
//						// 使用user对象
//					}
//				}
//				@Override
//				public void onFailure(Call<User> request, Throwable t) {
//					// 处理错误
//					Log.e("debug", Log.getStackTraceString(t));
//					Log.e("debug", Objects.requireNonNull(t.getLocalizedMessage()));
//				}
//			});
		} catch (Exception e) {
			Log.e("debug", Log.getStackTraceString(e));
			Log.e("debug", Objects.requireNonNull(e.getLocalizedMessage()));
			return false;
		}

		return true;
	}
	
}
