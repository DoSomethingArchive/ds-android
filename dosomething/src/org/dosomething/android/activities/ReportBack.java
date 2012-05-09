package org.dosomething.android.activities;

import java.util.ArrayList;
import java.util.List;

import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.WebForm;
import org.dosomething.android.transfer.WebFormField;
import org.dosomething.android.transfer.WebFormSelectOptions;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.inject.Inject;
import com.markupartist.android.widget.ActionBar;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ReportBack extends RoboActivity {

private static final String CAMPAIGN = "campaign";
	
	@Inject private LayoutInflater inflater;
	@Inject private ImageLoader imageLoader;
	
	@InjectView(R.id.actionbar) private ActionBar actionBar;
	@InjectView(R.id.list) private ListView list;
	
	private List<WebFormFieldBinding> fields;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_back);
		
		actionBar.setHomeAction(Campaigns.getHomeAction(this));
        
        Campaign campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
        
        WebForm webForm = campaign.getReportBack();
        
        fields = new ArrayList<WebFormFieldBinding>();
        for(WebFormField wff : webForm.getFields()) {
			fields.add(new WebFormFieldBinding(wff));
        }
        
        View submitView = inflater.inflate(R.layout.web_form_submit_row, null);
        Button submitButton = (Button)submitView.findViewById(R.id.button);
        submitButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		onSubmit();
        	}
        });
        list.addFooterView(submitView);
        
        list.setAdapter(new MyAdapter(getApplicationContext(), fields));
    }
	
	private void onSubmit() {
		
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
			
			if(layoutResource==R.layout.web_form_select_row) {
				List<String> options = new ArrayList<String>();
				for(WebFormSelectOptions wfso : wff.getSelectOptions()) {
					options.add(wfso.getLabel());
				}
				Spinner spinner = (Spinner)view.findViewById(R.id.field);
				spinner.setAdapter(new ArrayAdapter<String>(ReportBack.this, android.R.layout.simple_spinner_item, options));
			}
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
				default: {
					EditText field = (EditText)view.findViewById(R.id.field);
					answer = field.getText().toString();
				}
			}
			
			return answer;
		}
	}
	
	
	private class MyAdapter extends ArrayAdapter<WebFormFieldBinding> {

		public MyAdapter(Context context, List<WebFormFieldBinding> bindings){
			super(context, android.R.layout.simple_list_item_1, bindings);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			return ((WebFormFieldBinding)getItem(position)).getView();
		}
	}
	
}
