package org.dosomething.android.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.acra.util.Base64;
import org.dosomething.android.R;
import org.dosomething.android.context.SessionContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.dosomething.android.transfer.WebForm;
import org.dosomething.android.transfer.WebFormField;
import org.dosomething.android.transfer.WebFormSelectOptions;
import org.json.JSONObject;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.inject.Inject;
import com.markupartist.android.widget.ActionBar;

public abstract class AbstractWebForm extends RoboActivity {

	private static final String CAMPAIGN = "campaign";
	private static final int PICK_IMAGE_REQUEST = 0xFF0;
	private static final String DATE_FORMAT = "MM/dd/yyyy";
	
	@Inject private LayoutInflater inflater;
	@Inject private SessionContext sessionContext;
	
	@InjectView(R.id.actionbar) private ActionBar actionBar;
	@InjectView(R.id.list) private ListView list;
	
	private List<WebFormFieldBinding> fields;
	
	private WebFormFieldBinding pendingImageResult;
	
	protected abstract int getContentViewResourceId();
	protected abstract WebForm getWebForm();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getContentViewResourceId());
		
		actionBar.setHomeAction(Campaigns.getHomeAction(this));
        

        fields = new ArrayList<WebFormFieldBinding>();
        for(WebFormField wff : getWebForm().getFields()) {
        	WebFormFieldBinding binding = new WebFormFieldBinding(wff);
			fields.add(binding);
			
			if(savedInstanceState!=null) {
				String formValue = savedInstanceState.getString(wff.getName());
				if(formValue!=null) {
					binding.setFormValue(formValue);
				}
			}
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
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		for(WebFormFieldBinding binding : fields) {
			outState.putString(binding.getWebFormField().getName(), binding.getFormValue());
		}
	}
	
	private void onBeginImageActivity(WebFormFieldBinding binding) {
		pendingImageResult = binding;
		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, PICK_IMAGE_REQUEST);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==PICK_IMAGE_REQUEST && pendingImageResult!=null) {
			Uri uri = data.getData();
			String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = managedQuery(uri, projection, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            
			pendingImageResult.setSelectedImage(path);
		}
	}
	
	private void onFileUploadSuccess(int fieldIndex, String fid) {
		
		WebFormFieldBinding binding = fields.get(fieldIndex);
		binding.setUploadFid(fid);
		
		nextFileUploadOrSubmit();
	}
	
	private void onSubmitClick() {
		
		if(validateRequired()) {
			nextFileUploadOrSubmit();
		}
	}
	
	private boolean validateRequired() {
		boolean answer = true;
		for(WebFormFieldBinding binding : fields) {
			if(binding.getWebFormField().isRequired() && (binding.getFormValue()==null || binding.getFormValue().trim().length()==0)) {
				new AlertDialog.Builder(AbstractWebForm.this)
					.setMessage(getString(R.string.required_field, binding.getWebFormField().getLabel()))
					.setCancelable(false)
					.setPositiveButton(getString(R.string.ok_upper), null)
					.create()
					.show();
				answer = false;
				break;
			}
		}
		return answer;
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
		
		params.put("nid", getWebForm().getNodeId());
		
		for(WebFormFieldBinding binding : fields) {
			params.put(binding.getWebFormField().getName(), binding.getFormValue());
		}
		
		new MySubmitTask(params).execute();
	}
	
	protected void onSubmitSuccess() {
		
		setResult(RESULT_OK);
		finish();
	}
	
	private class WebFormFieldBinding {
		
		private View view;
		private int layoutResource;
		private WebFormField webFormField;
		private String selectedImage;
		private String uploadFid;
		private boolean editDialogOpen = false;
		
		
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
			} else if(type.equals("date")) { 
				layoutResource = R.layout.web_form_date_row;
			} else if(type.equals("file")) { 
				layoutResource = R.layout.web_form_image_row;
			} else {
				layoutResource = R.layout.web_form_text_row;
			}
			
			attatchView();
		}
		
		public void attatchView() {
			
			view = inflater.inflate(layoutResource, null);
			
			TextView label = (TextView)view.findViewById(R.id.label);
			label.setText(webFormField.getLabel());
			
			switch(layoutResource) {
				case R.layout.web_form_select_row : {
					List<String> options = new ArrayList<String>();
					for(WebFormSelectOptions wfso : webFormField.getSelectOptions()) {
						options.add(wfso.getLabel());
					}
					Spinner spinner = (Spinner)view.findViewById(R.id.field);
					spinner.setAdapter(new ArrayAdapter<String>(AbstractWebForm.this, android.R.layout.simple_spinner_item, options));
					break;
				}
				case R.layout.web_form_date_row : {
					EditText field = (EditText)view.findViewById(R.id.field);
//					field.setOnClickListener(new OnClickListener() {
//						public void onClick(View v) {
//							showBirthdayPicker();
//						}
//					});
					field.setOnFocusChangeListener(new OnFocusChangeListener() {
						public void onFocusChange(View v, boolean hasFocus) {
							if(hasFocus){
								showBirthdayPicker();
							}
						}
					});
					break;
				}
				case R.layout.web_form_image_row : {
					Button button = (Button)view.findViewById(R.id.field);
					button.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							AbstractWebForm.this.onBeginImageActivity(WebFormFieldBinding.this);
						}
					});
					updateImagePreview();
					break;
				}
			}
		}
		
		private void showBirthdayPicker(){
			if(!editDialogOpen){
		    	new DatePickerDialog(AbstractWebForm.this, new OnDateSetListener() {
					public void onDateSet(DatePicker datePickerView, int year, int monthOfYear, int dayOfMonth) {
						Date date  = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();
						EditText field = (EditText)view.findViewById(R.id.field);
						field.setText(new SimpleDateFormat(DATE_FORMAT).format(date));
						editDialogOpen = false;
					}
				}, 1995, 0, 1).show();
		    	editDialogOpen = true;
			}
	    }
		
		
		public void setSelectedImage(String path) {
			selectedImage = path;	
			updateImagePreview();
		}
		
		private void updateImagePreview() {
			if(selectedImage!=null) {
				ImageView image = (ImageView)view.findViewById(R.id.image);
				image.setImageURI(Uri.parse(new File(selectedImage).toString()));
			}
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
		
		public void setFormValue(String value) {
			
			switch(layoutResource) {
				case R.layout.web_form_select_row: {
					Spinner field = (Spinner)view.findViewById(R.id.field);
					int selectIndex = 0;
					List<WebFormSelectOptions> selectOptions = webFormField.getSelectOptions();
					for(int i=0; i<selectOptions.size(); i++) {
						WebFormSelectOptions wfso = selectOptions.get(i);
						if(wfso.getValue().equals(value)) {
							selectIndex = i;
							break;
						}
					}
					field.setSelection(selectIndex);
					break;
				}
				case R.layout.web_form_image_row: {
					selectedImage = value;
					updateImagePreview();
					break;
				}
				default: {
					EditText field = (EditText)view.findViewById(R.id.field);
					field.setText(value);
					break;
				}
			}
		}
		
		public String getFormValue() {
			
			String answer;
			switch(layoutResource) {
				case R.layout.web_form_select_row: {
					Spinner field = (Spinner)view.findViewById(R.id.field);
					answer = webFormField.getSelectOptions().get(field.getSelectedItemPosition()).getValue();
					break;
				}
				case R.layout.web_form_image_row: {
					if(uploadFid==null) {
						answer = selectedImage;
					} else {
						answer = uploadFid;
					}
					break;
				}
				default: {
					EditText field = (EditText)view.findViewById(R.id.field);
					answer = field.getText().toString();
					break;
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
				AbstractWebForm.this.onFileUploadSuccess(fieldIndex, fid);
			} else {
				onError();
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			actionBar.setProgressBarVisibility(ProgressBar.VISIBLE);
		}

		@Override
		protected void onFinish() {
			actionBar.setProgressBarVisibility(ProgressBar.GONE);
		}

		@Override
		protected void onError() {
			new AlertDialog.Builder(AbstractWebForm.this)
				.setMessage(getString(R.string.auth_failed))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.ok_upper), null)
				.create()
				.show();
		}

		@Override
		protected void doWebOperation() throws Exception {
			String url = "http://www.dosomething.org/?q=rest/file.json";
			
			Bitmap bitmap = getCompressedBitmap(path, 600);
			
			ByteArrayOutputStream byteos = new ByteArrayOutputStream();
			Base64OutputStream baseos = new Base64OutputStream(byteos, Base64.DEFAULT);
			bitmap.compress(CompressFormat.JPEG, 50, baseos);
			
			
			Map<String,String> params = new HashMap<String, String>();
			params.put("file", byteos.toString("UTF-8"));
			params.put("filename", new File(path).getName());
			
			WebserviceResponse response = doPost(url, params);
			
			if(response.getStatusCode()>=400 && response.getStatusCode()<500) {
				Log.e("asdf", "response="+response.getBodyAsString());
				uploadSuccess = false;
			} else {
				JSONObject obj = response.getBodyAsJSONObject();
				fid = obj.getString("fid");
				uploadSuccess = true;
			}
		}
		
		public Bitmap getCompressedBitmap(String path, int size) throws Exception {
            Bitmap b = null;

            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(path);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            int scale = 1;
            if (o.outHeight > size || o.outWidth > size) {
                    scale = (int)Math.pow(2, (int) Math.round(Math.log(size / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(path);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();

            return b;
		}
		
	}
	
	private class MySubmitTask extends AbstractWebserviceTask {
		
		private Map<String, String> params;
		
		private String validationMessage;
		private boolean submitSuccess = false;
		
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
			
			if(submitSuccess) {
				onSubmitSuccess();
			} else {
				new AlertDialog.Builder(AbstractWebForm.this)
					.setMessage(validationMessage)
					.setCancelable(false)
					.setPositiveButton(getString(R.string.ok_upper), null)
					.create()
					.show();
			}
		}

		@Override
		protected void onFinish() {
			actionBar.setProgressBarVisibility(ProgressBar.GONE);
		}

		@Override
		protected void onError() {
			new AlertDialog.Builder(AbstractWebForm.this)
				.setMessage(getString(R.string.form_submit_failed))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.ok_upper), null)
				.create()
				.show();
		}

		@Override
		protected void doWebOperation() throws Exception {
			
			String url = "http://www.dosomething.org/?q=rest/webform.json";
			
			WebserviceResponse response = doPost(url, params);
			
			if(response.getStatusCode()>=400 && response.getStatusCode()<500) {
				
				validationMessage = response.extractFormErrorsAsMessage();
				if(validationMessage==null) {
					validationMessage = getString(R.string.auth_failed);
				}
				submitSuccess = false;
			} else {
				
				submitSuccess = true;
			}
			
		}
	}
	
}
