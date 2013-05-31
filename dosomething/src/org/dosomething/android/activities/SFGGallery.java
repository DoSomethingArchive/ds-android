package org.dosomething.android.activities;

import java.util.ArrayList;
import java.util.List;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.SFGGalleryItem;
import org.dosomething.android.widget.CustomActionBar;
import org.dosomething.android.widget.ProgressBarImageLoadingListener;
import org.json.JSONArray;
import org.json.JSONObject;

import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.inject.Inject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * SFG = Share For Good
 * Main gallery page for Share for Good campaign types. Displays list of the 
 * sharable gallery items.
 */
public class SFGGallery extends AbstractActivity {
	
	@Inject private LayoutInflater inflater;
	@Inject private ImageLoader imageLoader;
	@Inject private UserContext userContext;
	
	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	@InjectView(R.id.list) private PullToRefreshListView pullToRefreshView;
	
	private Campaign campaign;
	private ListView list;

	@Override
	protected String getPageName() {
		return "SFGGallery";
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sfggallery);
		
		list = pullToRefreshView.getRefreshableView();
		pullToRefreshView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				fetchItems();
			}
		});
		
		campaign = (Campaign) getIntent().getExtras().get(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue());
		if (campaign != null) {
			actionBar.setTitle(campaign.getName());
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		fetchItems();
	}
	
	private void fetchItems() {
		if (campaign != null) {
			new SFGGalleryWebserviceTask(campaign.getSFGGalleryUrl()).execute();
		}
	}
	
	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign){
		Intent answer = new Intent(context, SFGGallery.class);
		answer.putExtra(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue(), campaign);
		return answer;
	}
	
	/**
	 * Task to retrieve list data
	 */
	private class SFGGalleryWebserviceTask extends AbstractWebserviceTask {
		private String url;
		private int page;
		private List<String> options;
		private boolean fetchSuccess;
		private List<SFGGalleryItem> galleryItems;
		
		public SFGGalleryWebserviceTask(String url) {
			super(userContext);
			
			this.url = url + "posts.json";
			this.fetchSuccess = false;
			this.galleryItems = new ArrayList<SFGGalleryItem>();
		}
		
		public SFGGalleryWebserviceTask(String url, int page) {
			super(userContext);
			
			this.url = url;
			this.page = page;
			this.fetchSuccess = false;
			this.galleryItems = new ArrayList<SFGGalleryItem>();
		}
		
		public SFGGalleryWebserviceTask(String url, int page, List<String> opts) {
			super(userContext);
			
			this.url = url;
			this.page = page;
			this.options = opts;
			this.fetchSuccess = false;
			this.galleryItems = new ArrayList<SFGGalleryItem>();
		}
		
		@Override
		protected void onPreExecute() {
			actionBar.setProgressBarVisibility(ProgressBar.VISIBLE);
		}

		@Override
		protected void onSuccess() {
			list.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> av, View v, int position, long id) {
					SFGGalleryItem galleryItem = (SFGGalleryItem)list.getAdapter().getItem(position);
					startActivity(SFGItem.getIntent(getApplicationContext(), galleryItem, campaign));
				}
			});
			
			SFGListAdapter adapter = new SFGListAdapter(galleryItems);
			list.setAdapter(adapter);
		}

		@Override
		protected void onFinish() {
			actionBar.setProgressBarVisibility(ProgressBar.GONE);
			pullToRefreshView.onRefreshComplete();
		}

		@Override
		protected void onError(Exception e) {
			new AlertDialog.Builder(SFGGallery.this)
				.setMessage("Unable to update")
				.setCancelable(false)
				.setPositiveButton(getString(R.string.ok_upper), null)
				.create()
				.show();
		}

		@Override
		protected void doWebOperation() throws Exception {
			WebserviceResponse response = this.doGet(this.url);
			if (response.getStatusCode() >= 400) {
				fetchSuccess = false;
			}
			else {
				fetchSuccess = true;
				JSONArray jsonItems = response.getBodyAsJSONArray();
				for (int i = 0; i < jsonItems.length(); i++) {
					JSONObject item = jsonItems.getJSONObject(i);
					galleryItems.add(new SFGGalleryItem(item));
				}
			}
		}
		
		private List<SFGGalleryItem> getPage(int page) throws Exception {
			List<SFGGalleryItem> items = new ArrayList<SFGGalleryItem>();
			
			return items;
		}
	}

	/**
	 * ListAdapter to handle SFG gallery functionality
	 */
	private class SFGListAdapter extends ArrayAdapter<SFGGalleryItem> {
		public SFGListAdapter(List<SFGGalleryItem> items) {
			super(SFGGallery.this, android.R.layout.simple_expandable_list_item_1, items);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = inflater.inflate(R.layout.sfggallery_row, null);
			}
			
			SFGGalleryItem item = getItem(position);
			
			ProgressBar progressBar = (ProgressBar)v.findViewById(R.id.progressBar);
			TextView bottomText = (TextView)v.findViewById(R.id.bottom_text);
			TextView name = (TextView)v.findViewById(R.id.name);
			ImageView image = (ImageView)v.findViewById(R.id.image);
			TextView shareCount = (TextView)v.findViewById(R.id.share_count);
			TextView shelter = (TextView)v.findViewById(R.id.shelter);
			TextView state = (TextView)v.findViewById(R.id.state);
			TextView topText = (TextView)v.findViewById(R.id.top_text);
			
			name.setText(item.getName());
			topText.setText(item.getTopText());
			bottomText.setText(item.getBottomText());
			String imageUrl = campaign.getSFGGalleryUrl() + item.getImageURL();
			imageLoader.displayImage(imageUrl, image, new ProgressBarImageLoadingListener(progressBar));
			
			shareCount.setText("Share Count: "+item.getShareCount());
			shelter.setText(item.getShelter());
			state.setText(item.getState());
			
			return v;
		}
	}
	
	
}
