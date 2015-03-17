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

import android.view.View;
import android.view.animation.Interpolator;

import com.vector.widgets.ILoadingLayout;
import com.vector.widgets.PullToRefreshBase.Mode;
import com.vector.widgets.PullToRefreshBase.OnPullEventListener;
import com.vector.widgets.PullToRefreshBase.OnRefreshListener;
import com.vector.widgets.PullToRefreshBase.OnRefreshListener2;
import com.vector.widgets.PullToRefreshBase.State;

public interface IPullToRefresh<T extends View> {

	// Demos the Pull-to-Refresh functionality to the user.
	public boolean demo();

	public Mode getCurrentMode();

	// Returns whether the Touch Events are filtered or not.
	public boolean getFilterTouchEvents();

	// Returns a proxy object which allows you to call methods on all of the
	// LoadingLayouts (the Views which show when Pulling/Refreshing).
	public ILoadingLayout getLoadingLayoutProxy();

	// Returns a proxy object which allows you to call methods on the
	// LoadingLayouts (the Views which show when Pulling/Refreshing).
	public ILoadingLayout getLoadingLayoutProxy(boolean includeStart,
			boolean includeEnd);

	// Get the mode that this view has been set to.
	public Mode getMode();

	// Get the Wrapped Refreshable View. Anything returned here has already been
	// added to the content view.
	public T getRefreshableView();

	// Get whether the 'Refreshing' View should be automatically shown when
	// refreshing. Returns true by default.
	public boolean getShowViewWhileRefreshing();

	// The state that the View is currently in.
	public State getState();

	// Whether Pull-to-Refresh is enabled
	public boolean isPullToRefreshEnabled();

	// Gets whether Overscroll support is enabled.
	public boolean isPullToRefreshOverScrollEnabled();

	// Returns whether the Widget is currently in the Refreshing mState
	public boolean isRefreshing();

	// Returns whether the widget has enabled scrolling on the Refreshable View
	// while refreshing.
	public boolean isScrollingWhileRefreshingEnabled();

	// Mark the current Refresh as complete.
	public void onRefreshComplete();

	// Set the Touch Events to be filtered or not.
	public void setFilterTouchEvents(boolean filterEvents);

	// Set the mode of Pull-to-Refresh that this view will use
	public void setMode(Mode mode);

	// Set OnPullEventListener for the Widget
	public void setOnPullEventListener(OnPullEventListener<T> listener);

	// Set OnRefreshListener for the Widget
	public void setOnRefreshListener(OnRefreshListener<T> listener);

	// Set OnRefreshListener for the Widget
	public void setOnRefreshListener(OnRefreshListener2<T> listener);

	// Sets whether Overscroll support is enabled.
	public void setPullToRefreshOverScrollEnabled(boolean enabled);

	// Sets the Widget to be in the refresh state.
	public void setRefreshing();

	// Sets the Widget to be in the refresh state.
	public void setRefreshing(boolean doScroll);

	// Sets the Animation Interpolator that is used for animated scrolling.
	public void setScrollAnimationInterpolator(Interpolator interpolator);

	// By default the Widget disables scrolling on the Refreshable View while
	// refreshing.
	public void setScrollingWhileRefreshingEnabled(
			boolean scrollingWhileRefreshingEnabled);

	// A mutator to enable/disable whether the 'Refreshing' View should be
	// automatically shown when refreshing.
	public void setShowViewWhileRefreshing(boolean showView);
}