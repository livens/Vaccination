package cn.mointe.vaccination.activity;

import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import cn.mointe.vaccination.R;
import cn.mointe.vaccination.other.VaccinationPreferences;
import cn.mointe.vaccination.service.VaccinationRemindService;
import cn.mointe.vaccination.tools.Constants;
import cn.mointe.vaccination.tools.Log;
import cn.mointe.vaccination.tools.PackageUtil;

/**
 * 设置界面
 * 
 */
public class SettingActivity extends Activity implements OnClickListener {

	private final static String TAG = "MainActivity";

	private ImageButton mSettingNotifyBtn;
	private boolean mIsNotifyOn;
	private VaccinationPreferences mPreference;
	private Button mVaccinationBeforeTime;
	private String[] mRemindTimeItems;

	private TextView mTitleText;
	private ImageButton mTitleLeftImgbtn;// title左边图标
	private ImageButton mTitleRightImgbtn;// title右边图标

	// 2014-05-27
	private ImageButton mWeekBeforeSwitch;
	private ImageButton mDayBeforeSwitch;
	private ImageButton mTodaySwitch;

	private Button mWeekBeforeTime;
	private Button mDayBeforeTime;
	private Button mTodayTime;

	private AlertDialog mTimeDialog;
	private String mSelectTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		mTitleText = (TextView) this.findViewById(R.id.title_text);
		mTitleLeftImgbtn = (ImageButton) this
				.findViewById(R.id.title_left_imgbtn);
		mTitleRightImgbtn = (ImageButton) this
				.findViewById(R.id.title_right_imgbtn);

		mTitleText.setText(R.string.action_settings);
		mTitleLeftImgbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SettingActivity.this.finish();
			}
		});
		mTitleRightImgbtn.setVisibility(View.GONE);

		mPreference = new VaccinationPreferences(this);
		mSettingNotifyBtn = (ImageButton) this
				.findViewById(R.id.setting_notify_turn);
		mVaccinationBeforeTime = (Button) this
				.findViewById(R.id.vaccination_before_time);

		mIsNotifyOn = mPreference.getNotify();
		Log.e(TAG, "notify==" + mIsNotifyOn);
		if (mIsNotifyOn) {
			mSettingNotifyBtn.setBackgroundResource(R.drawable.btn_on);
		} else {
			mSettingNotifyBtn.setBackgroundResource(R.drawable.btn_off);
		}

		mRemindTimeItems = getResources().getStringArray(R.array.remind_time);

		mSettingNotifyBtn.setOnClickListener(this);
		mVaccinationBeforeTime.setOnClickListener(this);

		switch (mPreference.getRemindTime()) {
		case 1:
			mVaccinationBeforeTime.setText(mRemindTimeItems[0]);
			break;
		case 2:
			mVaccinationBeforeTime.setText(mRemindTimeItems[1]);
			break;
		case 3:
			mVaccinationBeforeTime.setText(mRemindTimeItems[2]);
			break;
		default:
			break;
		}

		// 2014-05-27
		mWeekBeforeSwitch = (ImageButton) this
				.findViewById(R.id.setting_week_before_switch);
		mDayBeforeSwitch = (ImageButton) this
				.findViewById(R.id.setting_day_before_switch);
		mTodaySwitch = (ImageButton) this
				.findViewById(R.id.setting_today_switch);

		mWeekBeforeTime = (Button) this
				.findViewById(R.id.setting_week_before_time);
		mDayBeforeTime = (Button) this
				.findViewById(R.id.setting_day_before_time);
		mTodayTime = (Button) this.findViewById(R.id.setting_today_time);

		if (mPreference.getWeekBeforeIsRemind()) {
			mWeekBeforeSwitch.setBackgroundResource(R.drawable.btn_on);
		} else {
			mWeekBeforeSwitch.setBackgroundResource(R.drawable.btn_off);
		}
		if (mPreference.getDayBeforeIsRemind()) {
			mDayBeforeSwitch.setBackgroundResource(R.drawable.btn_on);
		} else {
			mDayBeforeSwitch.setBackgroundResource(R.drawable.btn_off);
		}
		if (mPreference.getTodayIsRemind()) {
			mTodaySwitch.setBackgroundResource(R.drawable.btn_on);
		} else {
			mTodaySwitch.setBackgroundResource(R.drawable.btn_off);
		}

		mWeekBeforeTime.setText(mPreference.getWeekBeforeRemindTime());
		mDayBeforeTime.setText(mPreference.getDayBeforeRemindTime());
		mTodayTime.setText(mPreference.getTodayRemindTime());

		mWeekBeforeSwitch.setOnClickListener(this);
		mDayBeforeSwitch.setOnClickListener(this);
		mTodaySwitch.setOnClickListener(this);

		mWeekBeforeTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showSettingTimeDialog(mWeekBeforeTime);
			}
		});
		mDayBeforeTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showSettingTimeDialog(mDayBeforeTime);
			}
		});
		mTodayTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showSettingTimeDialog(mTodayTime);
			}
		});
	}

	/**
	 * 2014-05-27
	 */
	private void showSettingTimeDialog(final Button button) {
		if (mTimeDialog != null && mTimeDialog.isShowing()) {
			return;
		}
		final String oldTime = button.getText().toString();
		String[] arr = oldTime.split(":");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.app_icon);
		builder.setTitle("设置时间");
		View view = LayoutInflater.from(this).inflate(R.layout.time_picker,
				null);
		TimePicker timePicker = (TimePicker) view.findViewById(R.id.timePicker);
		timePicker.setIs24HourView(true);
		timePicker.setCurrentHour(Integer.valueOf(arr[0]));
		timePicker.setCurrentMinute(Integer.valueOf(arr[1]));
		timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {

			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				// mSelectTime = hourOfDay + ":" + minute;
				StringBuilder stringBuilder = new StringBuilder();
				if (hourOfDay < 10) {
					stringBuilder.append("0").append(hourOfDay).append(":");
				} else {
					stringBuilder.append(hourOfDay).append(":");
				}
				if (minute < 10) {
					stringBuilder.append("0").append(minute);
				} else {
					stringBuilder.append(minute);
				}
				mSelectTime = stringBuilder.toString();
			}
		});
		builder.setView(view);
		builder.setPositiveButton(R.string.confirm,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mSelectTime == null) {
							button.setText(oldTime);
						} else {
							button.setText(mSelectTime);
							if (button.getId() == R.id.setting_week_before_time) {
								mPreference
										.setWeekBeforeRemindTime(mSelectTime);
							} else if (button.getId() == R.id.setting_day_before_time) {
								mPreference.setDayBeforeRemindTime(mSelectTime);
							} else if (button.getId() == R.id.setting_today_time) {
								mPreference.setTodayRemindTime(mSelectTime);
							}
							mSelectTime = null;
						}
						// XXX 时间存到哪 ，是否需要存服务器
						// XXX 目前存到SharedPreferences
					}
				});
		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mSelectTime = null;
					}
				});
		mTimeDialog = builder.create();
		mTimeDialog.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("SplashScreen"); // 统计页面
		MobclickAgent.onResume(this); // 统计时长
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("SplashScreen");
		MobclickAgent.onPause(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setting_notify_turn:// 是否提醒
			if (mIsNotifyOn) {
				mSettingNotifyBtn.setBackgroundResource(R.drawable.btn_off);
				mPreference.setNotify(false);
				mIsNotifyOn = false;
				Intent remindService = new Intent(this,
						VaccinationRemindService.class);
				if (PackageUtil.isServiceRunning(getApplicationContext(),
						Constants.REMIND_SERVICE)) {
					stopService(remindService);
				}
			} else {
				mSettingNotifyBtn.setBackgroundResource(R.drawable.btn_on);
				mPreference.setNotify(true);
				mIsNotifyOn = true;
				Intent remindService = new Intent(this,
						VaccinationRemindService.class);
				if (!PackageUtil.isServiceRunning(getApplicationContext(),
						Constants.REMIND_SERVICE)) {
					startService(remindService);
				}
			}
			break;
		case R.id.vaccination_before_time:// 提前几天提醒
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.choose_remind_time);
			builder.setItems(mRemindTimeItems,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case 0:// 接种前1天
								mPreference.setRemindTime(1);
								mVaccinationBeforeTime
										.setText(mRemindTimeItems[0]);
								break;
							case 1:// 接种前2天
								mPreference.setRemindTime(2);
								mVaccinationBeforeTime
										.setText(mRemindTimeItems[1]);
								break;
							case 2:// 接种前3天
								mPreference.setRemindTime(3);
								mVaccinationBeforeTime
										.setText(mRemindTimeItems[2]);
								break;
							default:
								break;
							}
							// 如果服务启动正在运行，重新启动
							if (PackageUtil.isServiceRunning(
									getApplicationContext(),
									Constants.REMIND_SERVICE)) {
								stopService(new Intent(getApplicationContext(),
										VaccinationRemindService.class));
							}
							startService(new Intent(getApplicationContext(),
									VaccinationRemindService.class));
						}
					});
			builder.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.create();
			builder.show();
			break;
		case R.id.setting_week_before_switch: // 接种前一周 提醒开关
			if (mPreference.getWeekBeforeIsRemind()) {
				mWeekBeforeSwitch.setBackgroundResource(R.drawable.btn_off);
				mPreference.setWeekBeforeIsRemind(false);
				// TODO 取消提醒
			} else {
				mWeekBeforeSwitch.setBackgroundResource(R.drawable.btn_on);
				mPreference.setWeekBeforeIsRemind(true);
				// TODO 开启提醒
			}
			break;
		case R.id.setting_day_before_switch:// 接种前一天 提醒开关
			if (mPreference.getDayBeforeIsRemind()) {
				mDayBeforeSwitch.setBackgroundResource(R.drawable.btn_off);
				mPreference.setDayBeforeIsRemind(false);
				// TODO 取消提醒
			} else {
				mDayBeforeSwitch.setBackgroundResource(R.drawable.btn_on);
				mPreference.setDayBeforeIsRemind(true);
				// TODO 开启提醒
			}
			break;
		case R.id.setting_today_switch: // 接种当天 提醒开关
			if (mPreference.getTodayIsRemind()) {
				mTodaySwitch.setBackgroundResource(R.drawable.btn_off);
				mPreference.setTodayIsRemind(false);
				// TODO 取消提醒
			} else {
				mTodaySwitch.setBackgroundResource(R.drawable.btn_on);
				mPreference.setTodayIsRemind(true);
				// TODO 开启提醒
			}
			break;
		default:
			break;
		}
	}

}
