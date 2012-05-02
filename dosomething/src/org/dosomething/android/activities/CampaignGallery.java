package org.dosomething.android.activities;

import java.util.ArrayList;
import java.util.List;

import org.dosomething.android.R;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.google.inject.Inject;
import com.markupartist.android.widget.ActionBar;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CampaignGallery extends RoboActivity {
	
	private static final String GALLERY = "gallery";
	
	@Inject LayoutInflater inflater;
	@Inject private ImageLoader imageLoader;
	
	@InjectView(R.id.actionbar) private ActionBar actionBar;
	@InjectView(R.id.gridview) private GridView gridview;
	
	private Context context;
	private int imagePixels;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign_gallery);
        
        context = this;
        
        imagePixels = getResources().getDimensionPixelSize(R.dimen.gallery_item);
        
        actionBar.setHomeAction(Campaigns.getHomeAction(this));
        
        List<GridItem> items = new ArrayList<CampaignGallery.GridItem>();
        
        for(int i = 1; i < 30; i++){
        	items.add(new GridItem("http://placekitten.com/300/300", "http://google.com"));
        }
        
        gridview.setAdapter(new MyGridAdapter(this, items));
        gridview.setOnItemClickListener(listener);
    }
	
	private OnItemClickListener listener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			GridItem item = (GridItem) gridview.getAdapter().getItem(position);
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.getTargetUrl())));
		}
	};
	
	private class GridItem {
		
		private String imageUrl;
		private String targetUrl;
		
		private GridItem(){}
		
		private GridItem(String imageUrl, String targetUrl) {
			super();
			this.imageUrl = imageUrl;
			this.targetUrl = targetUrl;
		}

		public String getImageUrl() {
			return imageUrl;
		}

		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}

		public String getTargetUrl() {
			return targetUrl;
		}

		public void setTargetUrl(String targetUrl) {
			this.targetUrl = targetUrl;
		}
	}
	
	private class MyGridAdapter extends ArrayAdapter<GridItem> {

		private MyGridAdapter(Context context, List<GridItem> items) {
			super(context, android.R.layout.simple_list_item_1, items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView answer;
			
			if(convertView != null){
				answer = (ImageView) convertView;
			}else{
				answer = new ImageView(context);
				answer.setLayoutParams(new GridView.LayoutParams(imagePixels, imagePixels));
				answer.setScaleType(ImageView.ScaleType.CENTER_CROP);
			}
			
			GridItem item = getItem(position);
			
			imageLoader.displayImage(item.getImageUrl(), answer);
			return answer;
		}
	
	}
	
	public Intent getIntent(Context context){
		Intent answer = new Intent(context, CampaignGallery.class);
		return answer;
	}
	
}
