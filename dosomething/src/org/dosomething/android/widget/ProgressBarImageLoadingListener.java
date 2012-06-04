package org.dosomething.android.widget;

import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class ProgressBarImageLoadingListener implements ImageLoadingListener {
		
	private ProgressBar progressBar;
	
	public ProgressBarImageLoadingListener(ProgressBar progressBar) {
		this.progressBar = progressBar;
	}
	
	@Override
	public void onLoadingStarted() {
		progressBar.setVisibility(ProgressBar.VISIBLE);
	}
	@Override
	public void onLoadingFailed(FailReason failReason) {
	    progressBar.setVisibility(ProgressBar.GONE);
	}
	@Override
	public void onLoadingComplete() {
		progressBar.setVisibility(ProgressBar.GONE);
	}
	@Override
	public void onLoadingCancelled() {
	    // Do nothing
	}
}
