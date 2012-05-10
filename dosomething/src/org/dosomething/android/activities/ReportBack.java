package org.dosomething.android.activities;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.acra.util.Base64;
import org.dosomething.android.R;
import org.dosomething.android.context.SessionContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.WebForm;
import org.dosomething.android.transfer.WebFormField;
import org.dosomething.android.transfer.WebFormSelectOptions;
import org.json.JSONObject;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.inject.Inject;
import com.markupartist.android.widget.ActionBar;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ReportBack extends RoboActivity {

	private static final String CAMPAIGN = "campaign";
	private static final int PICK_IMAGE_REQUEST = 0xFF0;
	
	@Inject private LayoutInflater inflater;
	@Inject private SessionContext sessionContext;
	
	@InjectView(R.id.actionbar) private ActionBar actionBar;
	@InjectView(R.id.list) private ListView list;
	
	private WebForm webForm;
	private List<WebFormFieldBinding> fields;
	
	private WebFormFieldBinding pendingImageResult;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_back);
		
		actionBar.setHomeAction(Campaigns.getHomeAction(this));
        
        Campaign campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
        
        webForm = campaign.getReportBack();
        
        fields = new ArrayList<WebFormFieldBinding>();
        for(WebFormField wff : webForm.getFields()) {
			fields.add(new WebFormFieldBinding(wff));
        }
        
        View submitView = inflater.inflate(R.layout.web_form_submit_row, null);
        Button submitButton = (Button)submitView.findViewById(R.id.button);
        submitButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		onSubmitClick();
        	}
        });
        list.addFooterView(submitView);
        
        list.setAdapter(new MyFormListAdapter(getApplicationContext(), fields));
    }
	
	private void onBeginImageActivity(WebFormFieldBinding binding) {
		pendingImageResult = binding;
		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, PICK_IMAGE_REQUEST);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==PICK_IMAGE_REQUEST && pendingImageResult!=null) {
			Uri targetUri = data.getData();
			pendingImageResult.setSelectedImage(targetUri.getPath());
		}
	}
	
	private void onFileUploadSuccess(int fieldIndex, String fid) {
		
		WebFormFieldBinding binding = fields.get(fieldIndex);
		binding.setUploadFid(fid);
		
		nextFileUploadOrSubmit();
	}
	
	private void onSubmitClick() {
		
		
		nextFileUploadOrSubmit();
	}
	
	private void nextFileUploadOrSubmit() {
		
		boolean didFileUpload = false;
		for(int i=0; i<fields.size(); i++) {
			WebFormFieldBinding binding = fields.get(i);
			if(binding.getSelectedImage()!=null && binding.getUploadFid()==null) {
				
				new MyFileUpload(i, binding.getSelectedImage()).execute();
				didFileUpload = true;
				break;
			}
		}
		
		if(!didFileUpload) {
			submitForm();
		}
	}
	
	private void submitForm() {
		
		Map<String, String> params = new HashMap<String, String>();
		
		params.put("nid", webForm.getNodeId());
		
		for(WebFormFieldBinding binding : fields) {
			params.put(binding.getWebFormField().getName(), binding.getValue());
		}
		
		new MySubmitTask(params).execute();
	}
	
	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign){
		Intent answer = new Intent(context, ReportBack.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}
	
	private class WebFormFieldBinding {
		
		private View view;
		private int layoutResource;
		private WebFormField webFormField;
		private String selectedImage;
		private String uploadFid;
		
		
		public WebFormFieldBinding(WebFormField wff) {
			webFormField = wff;
			
			String type = webFormField.getType();
			if(type.equals("select")) {
				layoutResource = R.layout.web_form_select_row;
			} else if(type.equals("number")) { 
				layoutResource = R.layout.web_form_number_row;
			} else if(type.equals("tel")) { 
				layoutResource = R.layout.web_form_tel_row;
			} else if(type.equals("email")) { 
				layoutResource = R.layout.web_form_email_row;
			} else if(type.equals("file")) { 
				layoutResource = R.layout.web_form_image_row;
			}	else {
				layoutResource = R.layout.web_form_text_row;
			}
			
			view = inflater.inflate(layoutResource, null);
			
			TextView label = (TextView)view.findViewById(R.id.label);
			label.setText(wff.getLabel());
			
			switch(layoutResource) {
				case R.layout.web_form_select_row : {
					List<String> options = new ArrayList<String>();
					for(WebFormSelectOptions wfso : wff.getSelectOptions()) {
						options.add(wfso.getLabel());
					}
					Spinner spinner = (Spinner)view.findViewById(R.id.field);
					spinner.setAdapter(new ArrayAdapter<String>(ReportBack.this, android.R.layout.simple_spinner_item, options));
				}
				case R.layout.web_form_image_row : {
					Button button = (Button)view.findViewById(R.id.field);
					button.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							ReportBack.this.onBeginImageActivity(WebFormFieldBinding.this);
						}
					});
				}
			}
		}
		
		public void setSelectedImage(String selectedImage) {
			this.selectedImage = selectedImage;
		}
		
		public String getUploadFid() {
			return uploadFid;
		}

		public void setUploadFid(String uploadFid) {
			this.uploadFid = uploadFid;
		}

		public String getSelectedImage() {
			return selectedImage;
		}

		public View getView() {
			return view;
		}

		public WebFormField getWebFormField() {
			return webFormField;
		}
		
		public String getValue() {
			
			String answer;
			switch(layoutResource) {
				case R.layout.web_form_select_row: {
					Spinner field = (Spinner)view.findViewById(R.id.field);
					answer = webFormField.getSelectOptions().get(field.getSelectedItemPosition()).getValue();
				}
				case R.layout.web_form_image_row: {
					answer = uploadFid;
				}
				default: {
					EditText field = (EditText)view.findViewById(R.id.field);
					answer = field.getText().toString();
				}
			}
			
			return answer;
		}
	}
	
	
	private class MyFormListAdapter extends ArrayAdapter<WebFormFieldBinding> {

		public MyFormListAdapter(Context context, List<WebFormFieldBinding> bindings){
			super(context, android.R.layout.simple_list_item_1, bindings);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			return ((WebFormFieldBinding)getItem(position)).getView();
		}
	}
	
	private class MyFileUpload extends AbstractWebserviceTask {
		
		private int fieldIndex;
		private String path;
		
		public boolean uploadSuccess;
		private String fid;
		
		public MyFileUpload(int fieldIndex, String path){
			super(sessionContext);
			this.fieldIndex = fieldIndex;
			this.path = path;
		}
		
		@Override
		protected void onSuccess() {
			
			if(uploadSuccess) {
				ReportBack.this.onFileUploadSuccess(fieldIndex, fid);
			} else {
				onError();
			}
		}

		@Override
		protected void onFinish() { /*ignore*/ }

		@Override
		protected void onError() {
			new AlertDialog.Builder(ReportBack.this)
				.setMessage("upload failded")
				.setCancelable(false)
				.setPositiveButton(getString(R.string.ok_upper), null)
				.create()
				.show();
		}

		@Override
		protected void doWebOperation() throws Exception {
			String url = "http://www.dosomething.org/?q=rest/file.json";
			
			Bitmap bitmap = BitmapFactory.decodeFile(path);
			bitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
			
			ByteArrayOutputStream byteos = new ByteArrayOutputStream();
			Base64OutputStream baseos = new Base64OutputStream(byteos, Base64.DEFAULT);
			bitmap.compress(CompressFormat.JPEG, 50, baseos);
			
			
			Map<String,String> params = new HashMap<String, String>();
			params.put("file", byteos.toString("UTF-8"));
			params.put("filename", path);
			
			WebserviceResponse response = doPost(url, params);
			
			if(response.getStatusCode()>=400 && response.getStatusCode()<500) {
				uploadSuccess = false;
			} else {
				JSONObject obj = response.getBodyAsJSONObject();
				fid = obj.getString("fid");
				uploadSuccess = true;
			}
		}
		
	}
	
	private class MySubmitTask extends AbstractWebserviceTask {
		
		private Map<String, String> params;
		
		public MySubmitTask(Map<String, String> params) {
			super(sessionContext);
			this.params = params;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			actionBar.setProgressBarVisibility(ProgressBar.VISIBLE);
		}

		@Override
		protected void onSuccess() {
			
		}

		@Override
		protected void onFinish() {
			actionBar.setProgressBarVisibility(ProgressBar.GONE);
		}

		@Override
		protected void onError() {
		
		}

		@Override
		protected void doWebOperation() throws Exception {
			
			String url = "http://www.dosomething.org/?q=rest/webform.json";
			
			WebserviceResponse response = doPost(url, params);
			
		}
	}
	
}
