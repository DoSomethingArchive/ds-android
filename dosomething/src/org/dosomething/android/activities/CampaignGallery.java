package org.dosomething.android.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dosomething.android.R;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.GalleryItem;
import org.dosomething.android.transfer.GalleryItem.GalleryItemType;
import org.dosomething.android.transfer.WebForm;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;

import com.commonsware.cwac.endless.EndlessAdapter;
import com.google.inject.Inject;
import com.markupartist.android.widget.ActionBar;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CampaignGallery extends RoboActivity {
	
	private static final String TAG = "CampaignGallery";
	private static final String CAMPAIGN = "campaign";
	
	@Inject LayoutInflater inflater;
	@Inject private ImageLoader imageLoader;
	@Inject private UserContext userContext;
	
	@InjectView(R.id.actionbar) private ActionBar actionBar;
	@InjectView(R.id.gridview) private GridView gridview;
	
	private Context context;
	private int imagePixels;
	private Campaign campaign;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign_gallery);
        
        context = this;
        
        campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
        
        imagePixels = getResources().getDimensionPixelSize(R.dimen.gallery_item);
        
        actionBar.setHomeAction(Campaigns.getHomeAction(this));
        
        gridview.setOnItemClickListener(listener);
        
        //gridview.setAdapter(new MyGridAdapter(CampaignGallery.this, items));
        
        new MyFeedTask(campaign.getGallery().getFeed()).execute();
    }
	
	private OnItemClickListener listener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			GalleryItem item = (GalleryItem) gridview.getAdapter().getItem(position);
			switch (item.getType()) {
			case IMAGE:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.getUrl())));
				break;
			case VIDEO:
				Pattern pattern = Pattern.compile("<embed src=\"([^\"]+)\"");
				Matcher matcher = pattern.matcher(item.getUrl());
				if(matcher.find() && matcher.groupCount()==1) {
					String url = matcher.group(1);
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				} else {
					Log.e(TAG, "embed with src could not be found.");
				}
				break;
			default:
				throw new RuntimeException();
			}
		}
	};
	
	private class MyEndlessGridAdapter extends EndlessAdapter {
		
		private List<GalleryItem> items;
		private int page = 0;
		
		public MyEndlessGridAdapter(Context context, ListAdapter wrapped, int pendingResource) {
			super(context, wrapped, pendingResource);
			
		}

		@Override
		protected boolean cacheInBackground() throws Exception {
			
			//TODO need code here
			//items = getPa
			
			return false;
		}

		@Override
		protected void appendCachedData() {
			
			page++;
			
			MyGridAdapter adapter = (MyGridAdapter) getWrappedAdapter();
			
			for(GalleryItem item : items) {
				adapter.add(item);
			}
		}
		
	}
	
	private class MyGridAdapter extends ArrayAdapter<GalleryItem> {

		private MyGridAdapter(Context context, List<GalleryItem> items) {
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
			
			GalleryItem item = getItem(position);
			
			imageLoader.displayImage(item.getThumb(), answer);
			return answer;
		}
	}
	
	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign){
		Intent answer = new Intent(context, CampaignGallery.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}
	
	private class MyFeedTask extends AbstractWebserviceTask {
		
		private String feedUrl;
		private List<GalleryItem> loadedItems = new ArrayList<GalleryItem>();
		
		public MyFeedTask(String feedUrl) {
			super(userContext);
			this.feedUrl = feedUrl;
		}
		
		@Override
		protected void onPreExecute() {
			actionBar.setProgressBarVisibility(ProgressBar.VISIBLE);
		}
		
		@Override
		protected void onSuccess() {
			
			//TODO i need to put this somewhere
			//items.addAll(loadedItems);
		}

		@Override
		protected void onFinish() {
			actionBar.setProgressBarVisibility(ProgressBar.GONE);
		}

		@Override
		protected void onError() {
			new AlertDialog.Builder(CampaignGallery.this)
				.setMessage(getString(R.string.gallery_feed_failed))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.ok_upper), null)
				.create()
				.show();
		}

		@Override
		protected void doWebOperation() throws Exception {
			
			JSONObject obj = doGet(feedUrl).getBodyAsJSONObject();
			
			JSONArray imageItems = obj.optJSONArray("image_items");
			if(imageItems!=null) {
				for(int i=0; i<imageItems.length(); i++) {
					JSONObject imageItemWrapper = imageItems.getJSONObject(i);
					JSONObject imageItem = imageItemWrapper.getJSONObject("image_item");
					loadedItems.add(new GalleryItem(GalleryItemType.IMAGE, imageItem));
				}
			}
			
			JSONArray videoItems = obj.optJSONArray("video_items");
			if(videoItems!=null) {
				for(int i=0; i<videoItems.length(); i++) {
					JSONObject videoItemWrapper = videoItems.getJSONObject(i);
					JSONObject videoItem = videoItemWrapper.getJSONObject("video_item");
					loadedItems.add(new GalleryItem(GalleryItemType.VIDEO, videoItem));
				}
			}
		}
	}
}
