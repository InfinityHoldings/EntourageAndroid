package com.vector.widgets;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

public interface ILoadingLayout {

	// Set the Last Updated Text. This displayed under the main label when
	// pulling.
	public void setLastUpdatedLabel(CharSequence label);

	// Set the drawable used in the loading layout. This is the same as calling
	public void setLoadingDrawable(Drawable drawable);

	// Set text to show when the Widget is being pulled
	public void setPullLabel(CharSequence pullLabel);

	// Set text to show when the Widget is refreshing
	public void setRefreshingLabel(CharSequence refreshingLabel);

	// Set Text to show when the Widget is being pulled, and will refresh when
	// released.
	public void setReleaseLabel(CharSequence releaseLabel);

	// Set's the Sets the typeface and style in which the text should be
	// displayed.
	public void setTextTypeface(Typeface tf);
}
