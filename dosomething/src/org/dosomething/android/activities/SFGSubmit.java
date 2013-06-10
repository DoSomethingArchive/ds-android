package org.dosomething.android.activities;

import java.io.File;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.WebForm;
import org.dosomething.android.widget.CustomActionBar;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.inject.Inject;

public class SFGSubmit extends AbstractWebForm {
	
	@Inject private UserContext userContext;
	
	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	@InjectView(R.id.submit) private Button btnSubmit;

	private WebForm webForm;
	
	@Override
	protected String getPageName() {
		return "sfg-submit";
	}
	
	@Override
	protected int getContentViewResourceId() {
		return R.layout.report_back;
	}

	@Override
	protected WebForm getWebForm() {
		return webForm;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// webForm needs to be set before super.onCreate because super calls 
		// getWebForm() which shouldn't return null.
		Campaign campaign = (Campaign)getIntent().getExtras().get(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue());
		webForm = campaign.getReportBack();
		
		super.onCreate(savedInstanceState);
		
		actionBar.setTitle(campaign.getReportBack().getPageTitle());
		
		// Overrides the OnClickListener set for this button in the parent class
		btnSubmit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onSubmitClick();
			}
		});
	}
	
	@Override
	protected void onSubmitSuccess() {
		Campaign campaign = (Campaign) getIntent().getExtras().get(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue());
		
		String uid = new UserContext(this).getUserUid();
	}
	
	private void onSubmitClick() {
		if (validateRequired()) {
			// Upload the image as multi-part file
			// Upload and submit the other data stuffs too
			
			try {
				// Create MultipartEntity
				MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				
				for (int i = 0; i < fields.size(); i++) {
					WebFormFieldBinding binding = fields.get(i);
					if (binding.hasImagesToUpload()) {
						// SFG submissions will only have one image
						String imgPath = binding.getSelectedImage(0);
						File imgFile = new File(imgPath);
						Log.v("SUBMIT", "file field name: "+binding.getWebFormField().getName());
						entity.addPart(binding.getWebFormField().getName(), new FileBody(imgFile));
					}
					else {
						Log.v("SUBMIT", "field name: "+binding.getWebFormField().getName()+" / field value: "+binding.getFormValue().get(0));
						entity.addPart(binding.getWebFormField().getName(), new StringBody(binding.getFormValue().get(0)));
					}
				}
				
				// TODO: more generic handling of the API key
				entity.addPart("key", new StringBody("aea12e3fe5f83f0d574fdff0342aba91"));
				entity.addPart("post[uid]", new StringBody(userContext.getUserUid()));
				entity.addPart("post[adopted]", new StringBody("0"));
				entity.addPart("post[flagged]", new StringBody("0"));
				entity.addPart("post[promoted]", new StringBody("0"));
				entity.addPart("post[share_count]", new StringBody("0"));
				
				new SFGSubmitTask(entity).execute();
			}
			catch (Exception e) {
				Log.v("ERROR", e.getLocalizedMessage());
			}
		}
	}

	public static Intent getIntent(Context context, Campaign campaign) {
		Intent answer = new Intent(context, SFGSubmit.class);
		answer.putExtra(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue(), campaign);
		return answer;
	}
	
	private class SFGSubmitTask extends AbstractWebserviceTask {
		private MultipartEntity postData;
		
		public SFGSubmitTask(MultipartEntity entity) {
			super(userContext);
			this.postData = entity;
		}

		@Override
		protected void onSuccess() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void onFinish() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void onError(Exception e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void doWebOperation() throws Exception {
			// TODO Auto-generated method stub
			WebserviceResponse response = doPostMultipart(webForm.getPostUrl(), postData);
		}
	}
}
