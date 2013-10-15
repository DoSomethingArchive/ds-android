package org.dosomething.android.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.inject.Inject;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.WebForm;

import java.io.File;
import java.util.HashMap;

import roboguice.inject.InjectView;

public class SFGSubmit extends AbstractWebForm {

	@Inject private UserContext userContext;

	@InjectView(R.id.submit) private Button btnSubmit;

	private Campaign campaign;
	private WebForm webForm;

	@Override
	public String getPageName() {
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
		campaign = (Campaign) getIntent().getExtras().get(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue());
		webForm = campaign.getReportBack();

		super.onCreate(savedInstanceState);

		// Overrides the OnClickListener set for this button in the parent class
		btnSubmit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onSubmitClick();
			}
		});
	}

	@Override
	protected void onSubmitSuccess() {
		// Track submission in analytics - Flurry Analytics event tracking
		HashMap<String, String> param = new HashMap<String, String>();
		param.put(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue(), campaign.getName());
		Analytics.logEvent("report-back-submit", param);
		
		// Google Analytics event tracking
		Analytics.logEvent("report-back", "sfg-submit", campaign.getName());
		
		startActivity(SFGGallery.getIntent(this, campaign, getString(R.string.campaign_sfg_submit_success)));
		finish();
	}

	private void onSubmitClick() {
		if (validateRequired()) {
			try {
				// Create MultipartEntity. POST submission will include both
				// file and text data.
				MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				for (int i = 0; i < fields.size(); i++) {
					WebFormFieldBinding binding = fields.get(i);
					if (binding.hasImagesToUpload()) {
						// SFG submissions will only have one image
						String imgPath = binding.getSelectedImage(0);
						File imgFile = new File(imgPath);

						FileBody fileBody = new FileBody(imgFile,getMimeType(imgPath));
						entity.addPart(binding.getWebFormField().getName(),fileBody);
					}
					else {
						entity.addPart(binding.getWebFormField().getName(), new StringBody(binding.getFormValue().get(0)));
					}
				}

				// API key and user uid need to also be part of content sent
				entity.addPart("key", new StringBody(DSConstants.PICS_API_KEY));
				entity.addPart("post[uid]",	new StringBody(userContext.getUserUid()));

				// Execute web task
				new SFGSubmitTask(this, entity).execute();
			} catch (Exception e) {
				showSubmitErrorDialog();
			}
		}
	}

	private String getMimeType(String filePath) {
		if (filePath != null && filePath.length() > 0) {
			int extIndex = filePath.lastIndexOf(".");
			if (extIndex > 0) {
				String ext = filePath.substring(extIndex + 1);
				if (ext.equalsIgnoreCase("png"))
					return "image/png";
				else if (ext.equalsIgnoreCase("jpeg")
						|| ext.equalsIgnoreCase("jpg"))
					return "image/jpeg";
				else if (ext.equalsIgnoreCase("gif"))
					return "image/gif";
			}
		}
		return null;
	}
	
	private void showSubmitErrorDialog() {
		new AlertDialog.Builder(SFGSubmit.this)
			.setMessage(getString(R.string.campaign_sfg_submit_error))
			.setPositiveButton(R.string.ok_upper, null)
			.setCancelable(true)
			.create()
			.show();
	}

	public static Intent getIntent(Context _context, Campaign _campaign, UserContext _userContext) {
		if (_userContext != null && _userContext.isLoggedIn() && _userContext.getUserUid() != null) {
			Intent answer = new Intent(_context, SFGSubmit.class);
			answer.putExtra(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue(), _campaign);
			return answer;
		}
		else {
			Intent answer = new Intent(_context, Login.class);
			return answer;
		}
	}

	/**
	 * Executes multipart POST web operation.
	 */
	private class SFGSubmitTask extends AbstractWebserviceTask {
		private Context taskContext;
		private MultipartEntity entityData;
		private boolean bSubmitSuccess;
		private ProgressDialog progressDialog;

		public SFGSubmitTask(Context _taskContext, MultipartEntity _entityData) {
			super(userContext);
			this.taskContext = _taskContext;
			this.entityData = _entityData;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			// Disable submit button and show progress indicator
			btnSubmit.setEnabled(false);
			progressDialog = new ProgressDialog(taskContext);
			progressDialog.setIndeterminate(true);
			progressDialog.setMessage(getString(R.string.campaign_sfg_submitting));
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected void onSuccess() {
			if (bSubmitSuccess) {
				onSubmitSuccess();
			}
			else {
				showSubmitErrorDialog();
			}
		}

		@Override
		protected void onFinish() {
			// Re-enable submit button and remove progress indicator
			btnSubmit.setEnabled(true);
			progressDialog.dismiss();
		}

		@Override
		protected void onError(Exception e) {
			showSubmitErrorDialog();
		}

		@Override
		protected void doWebOperation() throws Exception {
			String url = webForm.getPostUrl() + "?key=" + DSConstants.PICS_API_KEY;
			WebserviceResponse response = doPostMultipart(url, entityData);
			if (response.getStatusCode() >= 200 && response.getStatusCode() < 400) {
				bSubmitSuccess = true;
			}
			else {
				bSubmitSuccess = false;
			}
		}
	}
}
