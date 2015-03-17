/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.vector.widgets;

import java.util.HashMap;

import com.vector.widgets.PullToRefreshBase.Mode;
import com.vector.widgets.PullToRefreshBase.State;

import android.content.Context;
import android.media.MediaPlayer;
import android.widget.GridView;


public class SoundPullEventListener<V extends GridView> implements PullToRefreshBase.OnPullEventListener<V> {

	private final Context mContext;
	private final HashMap<State, Integer> mSoundMap;

	private MediaPlayer mCurrentMediaPlayer;

	// Constructor
	public SoundPullEventListener(Context context) {
		mContext = context;
		mSoundMap = new HashMap<State, Integer>();
	}

	public final void onPullEvent(PullToRefreshBase<V> refreshView, State event, Mode direction) {
		Integer soundResIdObj = mSoundMap.get(event);
		if (null != soundResIdObj) {
			playSound(soundResIdObj.intValue());
		}
	}

	// Set the Sounds to be played when a Pull Event happens.
	public void addSoundEvent(State event, int resId) {
		mSoundMap.put(event, resId);
	}

	// Clears all of the previously set sounds and events.
	public void clearSounds() {
		mSoundMap.clear();
	}

	// Gets the current (or last) MediaPlayer instance.
	public MediaPlayer getCurrentMediaPlayer() {
		return mCurrentMediaPlayer;
	}

	private void playSound(int resId) {
		// Stop current player, if there's one playing
		if (null != mCurrentMediaPlayer) {
			mCurrentMediaPlayer.stop();
			mCurrentMediaPlayer.release();
		}

		mCurrentMediaPlayer = MediaPlayer.create(mContext, resId);
		if (null != mCurrentMediaPlayer) {
			mCurrentMediaPlayer.start();
		}
	}
}
