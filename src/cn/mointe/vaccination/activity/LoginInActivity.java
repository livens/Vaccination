package cn.mointe.vaccination.activity;

import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.WeiboAuth.AuthInfo;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.LogoutAPI;
import com.sina.weibo.sdk.widget.LoginButton;
import com.sina.weibo.sdk.widget.LoginoutButton;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import cn.mointe.vaccination.AccessTokenKeeper;
import cn.mointe.vaccination.Constants;
import cn.mointe.vaccination.R;
import cn.mointe.vaccination.TencentLoginActivity;

public class LoginInActivity extends Activity {

	private ImageButton mTencentLogin;
	private LoginButton mLoginBtnDefault;
	private TextView mTokenView;
	
	 private Button mCurrentClickedButton;

	/** 登录认证对应的listener */
	private AuthListener mLoginListener = new AuthListener();
	/** 登出操作对应的listener */
	private LogOutRequestListener mLogoutListener = new LogOutRequestListener();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_in);
		mTokenView = (TextView) findViewById(R.id.result);

		// 创建授权认证信息
		AuthInfo authInfo = new AuthInfo(this, Constants.APP_KEY,
				Constants.REDIRECT_URL, Constants.SCOPE);
		
		/**
		 * 登录按钮
		 */
		mLoginBtnDefault = (LoginButton)findViewById(R.id.sinalogin);
		mLoginBtnDefault.setWeiboAuthInfo(authInfo, mLoginListener);
		
		mTencentLogin = (ImageButton) findViewById(R.id.tencentlogin);
		mTencentLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(LoginInActivity.this,
						TencentLoginActivity.class));
			}

		});
	}
	
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (mCurrentClickedButton != null) {
            if (mCurrentClickedButton instanceof LoginButton) {
                ((LoginButton)mCurrentClickedButton).onActivityResult(requestCode, resultCode, data);
            } else if (mCurrentClickedButton instanceof LoginoutButton) {
                ((LoginoutButton)mCurrentClickedButton).onActivityResult(requestCode, resultCode, data);
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
			Toast.makeText(LoginInActivity.this, e.getMessage(),
					Toast.LENGTH_SHORT).show();

		}

		@Override
		public void onCancel() {
			Toast.makeText(LoginInActivity.this,
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
						AccessTokenKeeper.clear(LoginInActivity.this);
						mTokenView
								.setText(R.string.weibosdk_demo_logout_success);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			// TODO Auto-generated method stub

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login_in, menu);
		return true;
	}

}
