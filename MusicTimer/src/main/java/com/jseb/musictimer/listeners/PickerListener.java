package com.jseb.musictimer.listeners;

import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by tills13 on 10/12/13.
 */
public class PickerListener implements TimePicker.OnTimeChangedListener {
	private TextView end_time;

	public PickerListener(TextView end_time) {
		this.end_time = end_time;
	}

	@Override
	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		Calendar cal = Calendar.getInstance();
		int curHour = cal.get(Calendar.HOUR_OF_DAY);
		int curMin = cal.get(Calendar.MINUTE);

		cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
		cal.set(Calendar.MINUTE, minute);
		if (curHour == cal.get(Calendar.HOUR_OF_DAY) && curMin == cal.get(Calendar.MINUTE)) end_time.setText("ends now");
		else end_time.setText(String.format("ends at %02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)) + (((hourOfDay < curHour) || (hourOfDay == curHour && minute < curMin)) ? " (tomorrow)" : ""));
	}
}
