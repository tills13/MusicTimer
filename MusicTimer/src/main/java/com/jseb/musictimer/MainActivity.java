package com.jseb.musictimer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jseb.musictimer.listeners.PickerListener;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
	private static ScheduledFuture mTask;
	public static boolean running;
	public static MenuItem mMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

		final NumberPicker hours = (NumberPicker) findViewById(R.id.hour_picker);
		final NumberPicker minutes = (NumberPicker) findViewById(R.id.minute_picker);
		final TextView end_time = (TextView) findViewById(R.id.end_time);

		hours.setMinValue(0);
		hours.setMaxValue(23);

		minutes.setMinValue(0);
		minutes.setMaxValue(59);

		findViewById(R.id.button_container).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) v.setBackgroundResource(R.color.ab_color);
				else if (event.getAction() == MotionEvent.ACTION_UP) {
					startTimer(hours.getValue(), minutes.getValue());
					v.setBackgroundResource(android.R.color.transparent);
				}

				return true;
			}
		});

		NumberPicker.OnValueChangeListener listener = new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				Calendar cal = Calendar.getInstance();
				int curDay = cal.get(Calendar.DAY_OF_WEEK);

				cal.add(Calendar.HOUR, hours.getValue());
				cal.add(Calendar.MINUTE, minutes.getValue());
				end_time.setText(String.format("ends at %02d:%02d %s" + ((curDay == cal.get(Calendar.DAY_OF_WEEK)) ? "" : " (tomorrow)"), cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), (cal.get(Calendar.AM_PM) == 0) ? "AM" : "PM"));
			}
		};

		hours.setOnValueChangedListener(listener);
		minutes.setOnValueChangedListener(listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		for (int i = 0; i < menu.size(); i++) {
			if (menu.getItem(i).getItemId() == (R.id.action_info)) {
				mMenuItem = menu.getItem(i);
				mMenuItem.setEnabled(this.running);
			}
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.simple_30mins:
				startTimer(0, 30);
				break;
			case R.id.simple_1hour:
				startTimer(1, 0);
				break;
			case R.id.simple_2hours:
				startTimer(2, 0);
				break;
			case R.id.simple_3hours:
				startTimer(3, 0);
				break;
			case R.id.action_date_picker:
				showDatePicker();
				break;
			case R.id.action_info:
				showTimerInfo();
				break;
		}

		return false;
	}

	public void showDatePicker() {
		View view = getLayoutInflater().inflate(R.layout.date_picker_dialog, null);

		final TimePicker picker = (TimePicker) view.findViewById(R.id.time_picker);
		final TextView end_time = (TextView) view.findViewById(R.id.end_time_dialog);
		final Calendar cal = Calendar.getInstance();

		picker.setIs24HourView(true);
		end_time.setText("ends now");

		picker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
		picker.setOnTimeChangedListener(new PickerListener(end_time));

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Custom end time: ");
		builder.setPositiveButton("start", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int hours, mins;
				int hour = picker.getCurrentHour();
				int minute = picker.getCurrentMinute();
				int curHour = cal.get(Calendar.HOUR_OF_DAY);
				int curMin = cal.get(Calendar.MINUTE);

				if (hour < curHour) hours =  (24 - curHour) + (hour);
				else hours = hour - curHour;

				if (minute < curMin) {
					hours--;
					mins = (60 - curMin) + (minute);
				} else mins = minute - curMin;

				startTimer(hours, mins);
			}
		});

		builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		builder.setView(view);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void showTimerInfo() {
		if (mTask.getDelay(TimeUnit.MILLISECONDS) < 0) {
			stopTimer();
			return;
		}

		View view = getLayoutInflater().inflate(R.layout.timer_info_dialog, null);
		final TextView timeLeft = ((TextView) view.findViewById(R.id.time_left));
		final CountDownTimer timer;

		final AlertDialog dialog = new AlertDialog.Builder(this).setPositiveButton("continue", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).setNegativeButton("cancel timer", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				stopTimer();
			}
		}).setView(view).create();

		timer = new CountDownTimer(mTask.getDelay(TimeUnit.MILLISECONDS), 1000) {
			@Override
			public void onTick(long seconds) {
				seconds = seconds / 1000;
				timeLeft.setText(String.format(getString(R.string.timer_info), (seconds / 3600), ((seconds % 3600) / 60), ((seconds % 3600) % 60)));
			}

			@Override
			public void onFinish() {
				dialog.dismiss();
			}
		}.start();

		dialog.show();
	}

	public void startTimer(final int hours, final int minutes) {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		final int delay = ((hours * 3600) + (minutes * 60)) * 1000;

		if (delay == 0) {
			Toast.makeText(this, "really?", Toast.LENGTH_LONG).show();
			return;
		}

		if (running) {
			new AlertDialog.Builder(this).setTitle("Timer already running...").setMessage("Do you wish to overwrite this timer?").setPositiveButton("yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					stopTimer();
					mTask = scheduler.schedule(new GetAudioFocusTask(MainActivity.this), delay, TimeUnit.MILLISECONDS);
					Toast.makeText(getApplicationContext(), "music will stop in " + hours + " hour(s) and " + minutes + " minute(s)", Toast.LENGTH_LONG).show();
					MainActivity.this.mMenuItem.setEnabled(true);
					running = true;
				}
			}).setNegativeButton("no", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			}).create().show();
		} else {
			mTask = scheduler.schedule(new com.jseb.musictimer.GetAudioFocusTask(this), delay, TimeUnit.MILLISECONDS);
			Toast.makeText(this, "music will stop in " + hours + " hour(s) and " + minutes + " minute(s)", Toast.LENGTH_LONG).show();
			this.mMenuItem.setEnabled(true);
			running = true;
		}
	}

	public static void stopTimer() {
		if (running) mTask.cancel(true);

		mMenuItem.setEnabled(false);
		running = false;
	}

	public void setRunning(boolean running) {
		this.running = running;

		if (this.running == false) {
			mTask.cancel(true);
			mTask = null;
			this.mMenuItem.setEnabled(false);
		}
	}
}
