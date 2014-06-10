package cn.mointe.vaccination.activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.mointe.vaccination.AccessTokenKeeper;
import cn.mointe.vaccination.Constants;
import cn.mointe.vaccination.R;
import cn.mointe.vaccination.Util;
import cn.mointe.vaccination.other.VaccinationPreferences;
import cn.mointe.vaccination.tools.Log;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth.AuthInfo;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.widget.LoginButton;
import com.sina.weibo.sdk.widget.LoginoutButton;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQAuth;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class LoginMainActivity extends Activity {

	private Tencent mTencent;
	private QQAuth mQQAuth;
	private UserInfo mInfo;

	private ImageButton login;
	private TextView loginInfo;
	private TextView nickname;
	private ImageView imageView;

	private LoginButton mLoginBtnDefault;
	private TextView mTokenView;
	private Button mCurrentClickedButton;

	/** 登录认证对应的listener */
	private AuthListener mLoginListener = new AuthListener();
	/** 登出操作对应的listener */
	private LogOutRequestListener mLogoutListener = new LogOutRequestListener();

	String url_qqlogin;
	private String SCOPE = "get_simple_userinfo,add_topic"; // 全部为 all
	private static final String APP_ID = "101080056";

	// private Button button2;

	private VaccinationPreferences mPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_test);

		mTokenView = (TextView) findViewById(R.id.result);

		// 创建授权认证信息
		AuthInfo authInfo = new AuthInfo(this, Constants.APP_KEY,
				Constants.REDIRECT_URL, Constants.SCOPE);

		/**
		 * 登录按钮
		 */
		mLoginBtnDefault = (LoginButton) findViewById(R.id.sina_login_main);
		mLoginBtnDefault.setWeiboAuthInfo(authInfo, mLoginListener);

		login = (ImageButton) this.findViewById(R.id.tencentlogin);
		loginInfo = (TextView) this.findViewById(R.id.textView1);
		nickname = (TextView) this.findViewById(R.id.textView2);
		// imageView = (ImageView) this.findViewById(R.id.imageView1);

		// button2 = (Button) this.findViewById(R.id.button2);

		mQQAuth = QQAuth.createInstance(APP_ID, this.getApplicationContext());
		mTencent = Tencent.createInstance(APP_ID, this.getApplicationContext());

		login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onClickLogin();
			}
		});

		/*
		 * button2.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { showUserInfo(); } });
		 */
	}

	private void showUserInfo() {
		IUiListener listener = new IUiListener() {
			@Override
			public void onCancel() {

			}

			@Override
			public void onComplete(Object arg0) {
				Log.i("MainActivity", "UserInfo:" + arg0);
				final JSONObject json = (JSONObject) arg0;
				Message msg = new Message();
				msg.obj = json;
				msg.what = 0;
				mHandler.sendMessage(msg);
				new Thread() {
					@Override
					public void run() {
						Bitmap bitmap = null;
						try {
							bitmap = getbitmap(json.getString("figureurl_qq_2"));
						} catch (JSONException e) {
							e.printStackTrace();
						}
						Message msg = new Message();
						msg.obj = bitmap;
						msg.what = 1;
						mHandler.sendMessage(msg);
					}
				}.start();
			}

			@Override
			public void onError(UiError arg0) {

			}

		};

		mInfo = new UserInfo(this, mQQAuth.getQQToken());
		mInfo.getUserInfo(listener);
	}

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				JSONObject response = (JSONObject) msg.obj;
				if (response.has("nickname")) {
					try {
						nickname.setText(response.getString("nickname"));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			} else if (msg.what == 1) {
				Bitmap bitmap = (Bitmap) msg.obj;
				imageView.setImageBitmap(bitmap);
			}
		}
	};

	/**
	 * 根据一个网络连接(String)获取bitmap图像
	 * 
	 * @param imageUri
	 * @return
	 * @throws MalformedURLException
	 */
	public static Bitmap getbitmap(String imageUri) {
		Log.v("MainActivity", "getbitmap:" + imageUri);
		// 显示网络上的图片
		Bitmap bitmap = null;
		try {
			URL myFileUrl = new URL(imageUri);
			HttpURLConnection conn = (HttpURLConnection) myFileUrl
					.openConnection();
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();

			Log.v("MainActivity", "image download finished." + imageUri);
		} catch (IOException e) {
			e.printStackTrace();
			Log.v("MainActivity", "getbitmap bmp fail---");
			return null;
		}
		return bitmap;
	}

	// 应用调用Andriod_SDK接口时，使能成功接收到回调
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mTencent.onActivityResult(requestCode, resultCode, data);

		if (mCurrentClickedButton != null) {
			if (mCurrentClickedButton instanceof LoginButton) {
				((LoginButton) mCurrentClickedButton).onActivityResult(
						requestCode, resultCode, data);
			} else if (mCurrentClickedButton instanceof LoginoutButton) {
				((LoginoutButton) mCurrentClickedButton).onActivityResult(
						requestCode, resultCode, data);
			}
		}
	}

	/**
	 * 
	 * @author Livens 登入按钮的监听器，接收授权结果
	 */
	private class AuthListener implements WeiboAuthListener {

		@Override
		public void onComplete(Bundle values) {
			Oauth2AccessToken accessToken = Oauth2AccessToken
					.parseAccessToken(values);
			if (accessToken != null && accessToken.isSessionValid()) {
				String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
						.format(new java.util.Date(accessToken.getExpiresTime()));
				String format = getString(R.string.weibosdk_demo_token_to_string_format_1);

				AccessTokenKeeper.writeAccessToken(getApplicationContext(),
						accessToken);
			}

		}

		@Override
		public void onWeiboException(WeiboException e) {
			Toast.makeText(LoginMainActivity.this, e.getMessage(),
					Toast.LENGTH_SHORT).show();

		}

		@Override
		public void onCancel() {
			Toast.makeText(LoginMainActivity.this,
					R.string.weibosdk_demo_toast_auth_canceled,
					Toast.LENGTH_SHORT).show();
		}

	}

	/**
	 * 登出按钮的监听器，接收登出处理信息，（API 请求结果的监听器）
	 */
	private class LogOutRequestListener implements RequestListener {

		@Override
		public void onComplete(String response) {
			if (!TextUtils.isEmpty(response)) {
				try {
					JSONObject obj = new JSONObject(response);
					String value = obj.getString("result");

					if ("true".equalsIgnoreCase(value)) {
						AccessTokenKeeper.clear(LoginMainActivity.this);
						mTokenView
								.setText(R.string.weibosdk_demo_logout_success);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		}

		@Override
		public void onWeiboException(WeiboException e) {
			mTokenView.setText(R.string.weibosdk_demo_logout_failed);
		}

	}

	@SuppressWarnings("unused")
	private class BaseUiListener implements IUiListener {

		@Override
		public void onComplete(Object response) {
			// TODO Auto-generated method stub
			Util.showResultDialog(LoginMainActivity.this, response.toString(),
					"登录成功");
			doComplete((JSONObject) response);

			Intent intent = new Intent(LoginMainActivity.this,
					MainActivity.class);
			startActivity(intent);
		}

		protected void doComplete(JSONObject values) {

		}

		@Override
		public void onError(UiError e) {
			// TODO Auto-generated method stub
			Util.toastMessage(LoginMainActivity.this, "onError:"
					+ e.errorDetail);
			Util.dismissDialog();
		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			Util.toastMessage(LoginMainActivity.this, "onCancel:");
			Util.dismissDialog();
		}

	}

	private void onClickLogin() {

		if (!mTencent.isSessionValid()) {
			IUiListener listener = new IUiListener() {

				// ** 授权失败的回调 *//*
				@Override
				public void onError(UiError arg0) {
					Toast.makeText(LoginMainActivity.this, "授权失败",
							Toast.LENGTH_SHORT).show();
				}

				// ** 授权成功的回调 *//*
				@Override
				public void onComplete(Object arg0) {
					Toast.makeText(LoginMainActivity.this, "登录成功",
							Toast.LENGTH_SHORT).show();
					// loginInfo.setText(arg0.toString());
					// Log.i("MainActivity", "登录信息:" + arg0.toString());

					Intent intent = new Intent(LoginMainActivity.this,
							MainActivity.class);
					startActivity(intent);

				}

				// ** 取消授权的回调 *//*
				@Override
				public void onCancel() {
					Toast.makeText(LoginMainActivity.this, "取消登录",
							Toast.LENGTH_SHORT).show();
				}

			};
			mTencent.login(this, SCOPE, listener);
		} else {
			mTencent.logout(this);
		}
	}

}
