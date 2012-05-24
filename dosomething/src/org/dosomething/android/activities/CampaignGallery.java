package org.dosomething.android.activities;

import java.util.ArrayList;
import java.util.List;

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

import com.google.inject.Inject;
import com.markupartist.android.widget.ActionBar;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CampaignGallery extends RoboActivity {
	
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
        
        new MyFeedTask().execute();
    }
	
	private OnItemClickListener listener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			GalleryItem item = (GalleryItem) gridview.getAdapter().getItem(position);
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.getUrl())));
		}
	};
	
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
		
		List<GalleryItem> items = new ArrayList<GalleryItem>();
		
		public MyFeedTask() {
			super(userContext);
		}

		@Override
		protected void onSuccess() {
			
			gridview.setAdapter(new MyGridAdapter(CampaignGallery.this, items));
		}

		@Override
		protected void onFinish() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void onError() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void doWebOperation() throws Exception {
			WebForm reportback = campaign.getReportBack();
			if(reportback==null) {
				return;
			}
			String reportbackId = reportback.getNodeId();
			
			String url = "http://www.dosomething.org/feed/campaign_photos/" + reportbackId;
			
			Log.d("adsf", url);

			JSONObject obj = doGet(url).getBodyAsJSONObject();
			
			JSONArray imageItems = obj.optJSONArray("image_items");
			if(imageItems!=null) {
				for(int i=0; i<imageItems.length(); i++) {
					JSONObject imageItemWrapper = imageItems.getJSONObject(i);
					JSONObject imageItem = imageItemWrapper.getJSONObject("image_item");
					items.add(new GalleryItem(GalleryItemType.IMAGE, imageItem));
				}
			}
			
			JSONArray videoItems = obj.optJSONArray("video_items");
			if(videoItems!=null) {
				for(int i=0; i<videoItems.length(); i++) {
					JSONObject videoItemWrapper = videoItems.getJSONObject(i);
					JSONObject videoItem = videoItemWrapper.getJSONObject("video_item");
					items.add(new GalleryItem(GalleryItemType.VIDEO, videoItem));
				}
			}
		}
	}
}
