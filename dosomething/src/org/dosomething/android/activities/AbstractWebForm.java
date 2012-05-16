package org.dosomething.android.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.acra.util.Base64;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.inject.Inject;
import com.markupartist.android.widget.ActionBar;

public abstract class AbstractWebForm extends RoboActivity {
	
	private static final String TAG = "AbstractWebForm";
	private static final String CAMPAIGN = "campaign";
	private static final int PICK_IMAGE_REQUEST = 0xFF0;
	private static final String DATE_FORMAT = "MM/dd/yyyy";
	
	@Inject private LayoutInflater inflater;
	@Inject private SessionContext sessionContext;
	
	@InjectView(R.id.actionbar) private ActionBar actionBar;
	
	private List<WebFormFieldBinding> fields;
	
	private WebFormFieldBinding pendingImageResult;
	
	protected abstract int getContentViewResourceId();
	protected abstract WebForm getWebForm();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getContentViewResourceId());
		
		actionBar.setHomeAction(Campaigns.getHomeAction(this));
		
		LinearLayout webform = (LinearLayout)findViewById(R.id.web_form);

        fields = new ArrayList<WebFormFieldBinding>();
        for(WebFormField wff : getWebForm().getFields()) {
        	WebFormFieldBinding binding = new WebFormFieldBinding(wff);
			fields.add(binding);
			webform.addView(binding.getView());
        }
        
        View submitView = inflater.inflate(R.layout.web_form_submit_row, null);
        Button submitButton = (Button)submitView.findViewById(R.id.button);
        submitButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		onSubmitClick();
        	}
        });
        webform.addView(submitView);
    }
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		if(savedInstanceState!=null) {
			for(WebFormFieldBinding binding : fields) {
				ArrayList<String> formValue = savedInstanceState.getStringArrayList(binding.getWebFormField().getName());
				if(formValue!=null) {
					binding.setFormValue(formValue);
				}
	        }
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		for(WebFormFieldBinding binding : fields) {
			outState.putStringArrayList(binding.getWebFormField().getName(), (ArrayList<String>)binding.getFormValue());
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
			if(binding.getWebFormField().isRequired() && (binding.getFormValue().isEmpty() || binding.getFormValue().get(0).trim().length()==0)) {
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
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		
		params.add(new BasicNameValuePair("nid", getWebForm().getNodeId()));
		
		for(WebFormFieldBinding binding : fields) {
			for(String value : binding.getFormValue()) {
				params.add(new BasicNameValuePair(binding.getWebFormField().getName(), value));
			}
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
				String selectType = webFormField.getSelectType();
				if(selectType!=null && selectType.equals("multiple")) {
					layoutResource = R.layout.web_form_select_multi_row;
				} else {
					layoutResource = R.layout.web_form_select_single_row;
				}
			} else if(type.equals("number")) { 
				layoutResource = R.layout.web_form_number_row;
			} else if(type.equals("phone") || type.equals("tel")) { 
				layoutResource = R.layout.web_form_phone_row;
			} else if(type.equals("email")) { 
				layoutResource = R.layout.web_form_email_row;
			} else if(type.equals("date")) { 
				layoutResource = R.layout.web_form_date_row;
			} else if(type.equals("file")) { 
				layoutResource = R.layout.web_form_image_row;
			} else if(type.equals("textarea")) {
				layoutResource = R.layout.web_form_textarea_row;
			} else {
				layoutResource = R.layout.web_form_textfield_row;
			}
			
			attatchView();
		}
		
		public void attatchView() {
			
			view = inflater.inflate(layoutResource, null);
			
			TextView label = (TextView)view.findViewById(R.id.label);
			label.setText(webFormField.getLabel());
			
			switch(layoutResource) {
				case R.layout.web_form_select_single_row : {
					List<String> options = new ArrayList<String>();
					for(WebFormSelectOptions wfso : webFormField.getSelectOptions()) {
						options.add(wfso.getLabel());
					}
					Spinner spinner = (Spinner)view.findViewById(R.id.field_select_single);
					spinner.setAdapter(new ArrayAdapter<String>(AbstractWebForm.this, android.R.layout.simple_spinner_item, options));
					break;
				}
				case R.layout.web_form_select_multi_row : {
					LinearLayout layout = (LinearLayout)view.findViewById(R.id.field_select_multi);
					for(WebFormSelectOptions wfso : webFormField.getSelectOptions()) {
						CheckBox checkbox = new CheckBox(AbstractWebForm.this);
						checkbox.setTextColor(R.color.web_form_checkbox_label);
						checkbox.setText(wfso.getLabel());
						checkbox.setTag(wfso.getValue());
						layout.addView(checkbox, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
					}
					break;
				}
				case R.layout.web_form_date_row : {
					EditText field = (EditText)view.findViewById(R.id.field_date);
					field.setOnClickListener(new OnClickListener() {
						public void onClick(View arg0) {
							showDatePicker();
						}
					});
					field.setOnFocusChangeListener(new OnFocusChangeListener() {
						public void onFocusChange(View v, boolean hasFocus) {
							if(hasFocus){
								showDatePicker();
							}
						}
					});
					break;
				}
				case R.layout.web_form_image_row : {
					Button button = (Button)view.findViewById(R.id.button);
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
		
		private void showDatePicker(){
			if(!editDialogOpen){
		    	new DatePickerDialog(AbstractWebForm.this, new OnDateSetListener() {
					public void onDateSet(DatePicker datePickerView, int year, int monthOfYear, int dayOfMonth) {
						Date date  = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();
						EditText field = (EditText)view.findViewById(R.id.field_date);
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
		
		public void setFormValue(List<String> values) {
			
			switch(layoutResource) {
				case R.layout.web_form_select_single_row: {
					Spinner field = (Spinner)view.findViewById(R.id.field_select_single);
					int selectIndex = 0;
					List<WebFormSelectOptions> selectOptions = webFormField.getSelectOptions();
					String value = values.get(0);
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
				case R.layout.web_form_select_multi_row: {
					LinearLayout layout = (LinearLayout)view.findViewById(R.id.field_select_multi);
					for(String value : values) {
						for(int i=0; i<layout.getChildCount(); i++) {
							CheckBox checkbox = (CheckBox)layout.getChildAt(i);
							if(value.equals(checkbox.getTag())) {
								checkbox.setChecked(true);
								break;
							}
						}
					}
					break;
				}
				case R.layout.web_form_image_row: {
					selectedImage = values.get(0);
					updateImagePreview();
					break;
				}
				case R.layout.web_form_date_row : {
					EditText field = (EditText)view.findViewById(R.id.field_date);
					field.setText(values.get(0));
					break;
				}
				case R.layout.web_form_email_row : {
					EditText field = (EditText)view.findViewById(R.id.field_email);
					field.setText(values.get(0));
					break;
				}
				case R.layout.web_form_number_row : {
					EditText field = (EditText)view.findViewById(R.id.field_number);
					field.setText(values.get(0));
					break;
				}
				case R.layout.web_form_phone_row : {
					EditText field = (EditText)view.findViewById(R.id.field_phone);
					field.setText(values.get(0));
					break;
				}
				case R.layout.web_form_textarea_row : {
					EditText field = (EditText)view.findViewById(R.id.field_textarea);
					field.setText(values.get(0));
					break;
				}
				case R.layout.web_form_textfield_row : {
					EditText field = (EditText)view.findViewById(R.id.field_textfield);
					field.setText(values.get(0));
					break;
				}
				default: {
					throw new RuntimeException();
				}
			}
		}
		
		public List<String> getFormValue() {
			
			List<String> answer = new ArrayList<String>(1);
			switch(layoutResource) {
				case R.layout.web_form_select_single_row: {
					Spinner field = (Spinner)view.findViewById(R.id.field_select_single);
					answer.add(webFormField.getSelectOptions().get(field.getSelectedItemPosition()).getValue());
					break;
				}
				case R.layout.web_form_select_multi_row: {
					LinearLayout layout = (LinearLayout)view.findViewById(R.id.field_select_multi);
					for(int i=0; i<layout.getChildCount(); i++) {
						CheckBox checkbox = (CheckBox)layout.getChildAt(i);
						if(checkbox.isChecked()) {
							answer.add((String)checkbox.getTag());
						}
					}
					break;
				}
				case R.layout.web_form_image_row: {
					if(uploadFid==null) {
						answer.add(selectedImage);
					} else {
						answer.add(uploadFid);
					}
					break;
				}
				case R.layout.web_form_date_row : {
					EditText field = (EditText)view.findViewById(R.id.field_date);
					answer.add(field.getText().toString());
					break;
				}
				case R.layout.web_form_email_row : {
					EditText field = (EditText)view.findViewById(R.id.field_email);
					answer.add(field.getText().toString());
					break;
				}
				case R.layout.web_form_number_row : {
					EditText field = (EditText)view.findViewById(R.id.field_number);
					answer.add(field.getText().toString());
					break;
				}
				case R.layout.web_form_phone_row : {
					EditText field = (EditText)view.findViewById(R.id.field_phone);
					answer.add(field.getText().toString());
					break;
				}
				case R.layout.web_form_textarea_row : {
					EditText field = (EditText)view.findViewById(R.id.field_textarea);
					answer.add(field.getText().toString());
					break;
				}
				case R.layout.web_form_textfield_row : {
					EditText field = (EditText)view.findViewById(R.id.field_textfield);
					answer.add(field.getText().toString());
					break;
				}
				default: {
					throw new RuntimeException();
				}
			}
			
			return answer;
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
			
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("file", byteos.toString("UTF-8")));
			params.add(new BasicNameValuePair("filename", new File(path).getName()));
			
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
		
		private List<NameValuePair> params;
		
		private String validationMessage;
		private boolean submitSuccess = false;
		
		public MySubmitTask(List<NameValuePair> params) {
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
