package com.jseb.musictimer;

import android.content.Context;
import android.media.AudioManager;

/**
 * Created by tills13 on 10/12/13.
 */
public class GetAudioFocusTask implements Runnable {
	public MainActivity context;

	public GetAudioFocusTask(MainActivity context) {
		this.context = context;
	}

	public void run() {
		((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		context.stopTimer();
	}
}
