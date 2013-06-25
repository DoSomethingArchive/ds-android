package org.dosomething.android.widget;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class ProgressBarImageLoadingListener implements ImageLoadingListener {
		
	private ProgressBar progressBar;
	
	public ProgressBarImageLoadingListener(ProgressBar progressBar) {
		this.progressBar = progressBar;
	}
	
	@Override
	public void onLoadingStarted(String imageUri, View view) {
		progressBar.setVisibility(ProgressBar.VISIBLE);
	}
	
	@Override
	public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
	    progressBar.setVisibility(ProgressBar.GONE);
	}
	
	@Override
	public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		progressBar.setVisibility(ProgressBar.GONE);
	}
	
	@Override
	public void onLoadingCancelled(String imageUri, View view) {
	    // Do nothing
	}

}
