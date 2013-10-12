package com.jseb.musictimer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MainActivity extends Activity {

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
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					v.setBackgroundResource(android.R.color.holo_blue_light);
					startTimer(hours.getValue(), minutes.getValue());
				} else if (event.getAction() == MotionEvent.ACTION_UP) v.setBackgroundResource(android.R.color.transparent);

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
		}

		return false;
	}

	public void showDatePicker() {
		View view = getLayoutInflater().inflate(R.layout.date_picker_dialog, null);
		final TimePicker picker = (TimePicker) view.findViewById(R.id.time_picker);
		final TextView end_time = (TextView) view.findViewById(R.id.end_time_dialog);
		picker.setIs24HourView(true);
		end_time.setText("ends now");

		picker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				Calendar cal = Calendar.getInstance();
				int curHour = cal.get(Calendar.HOUR_OF_DAY);

				cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
				cal.set(Calendar.MINUTE, minute);
				end_time.setText(String.format("ends at %02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)) + ((hourOfDay < curHour) ? " (tomorrow)" : ""));
			}
		});

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Custom end time: ");
		builder.setPositiveButton("start", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Calendar cal = Calendar.getInstance();
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

	public void startTimer(int hours, int minutes) {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

		int delay = (hours * 3600) + (minutes * 60) * 1000;

		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				((AudioManager) getSystemService(Context.AUDIO_SERVICE)).requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
			}
		}, delay);

		Toast.makeText(this, "music will stop in " + hours + " hour(s) and " + minutes + " minute(s)", Toast.LENGTH_LONG).show();
	}
}
