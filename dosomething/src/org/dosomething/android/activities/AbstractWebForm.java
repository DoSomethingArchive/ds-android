package org.dosomething.android.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64OutputStream;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.acra.util.Base64;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.dosomething.android.transfer.WebForm;
import org.dosomething.android.transfer.WebFormField;
import org.dosomething.android.transfer.WebFormSelectOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import roboguice.inject.InjectView;

public abstract class AbstractWebForm extends AbstractActionBarActivity {

    private static final int PICK_IMAGE_REQUEST = 0xFF0;
    private static final int PICK_SFG_IMAGE_REQUEST = 0xFF1;

    private static final String[] STATES = {"AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "DC", "FL", "GA", "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"};

    @Inject private LayoutInflater inflater;
    @Inject private UserContext userContext;
    @Inject @Named("ProximaNova-Bold")Typeface typefaceBold;
    @Inject @Named("ProximaNova-Reg")Typeface typefaceReg;

    @InjectView(R.id.required_instructions) private TextView lblRequiredInstructions;
    @InjectView(R.id.submit) private Button btnSubmit;

    protected List<WebFormFieldBinding> fields;

    private WebFormFieldBinding pendingImageResult;
    private boolean submitTaskInProgress;

    // Path to a preselected image to attach to this webform submission
    protected String mPreselectedImage;

    // For webforms that have a school search component. GSIDs of school results are cached here.
    private int[] schoolGSIDs;

    protected abstract int getContentViewResourceId();
    protected abstract WebForm getWebForm();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(getContentViewResourceId());

        // Enable ActionBar home button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        LinearLayout webform = (LinearLayout)findViewById(R.id.web_form);

        boolean anyRequired = false;
        fields = new ArrayList<WebFormFieldBinding>();
        for(WebFormField wff : getWebForm().getFields()) {
            WebFormFieldBinding binding = new WebFormFieldBinding(wff);
            fields.add(binding);
            webform.addView(binding.getView());

            anyRequired = anyRequired || wff.isRequired();
        }

        if(anyRequired) {
            lblRequiredInstructions.setVisibility(TextView.VISIBLE);
        }

        btnSubmit.setTypeface(typefaceBold, Typeface.BOLD);
        btnSubmit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                onSubmitClick();
            }
        });

        prePopulate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If home button is selected on ActionBar, then end the activity
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
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

    private void prePopulate() {
        for (WebFormFieldBinding binding : fields) {
            String name = binding.getWebFormField().getName();
            if (name == null) {
                continue;
            }

            if (name.equals("email") || name.equals("field_webform_email[und][0][email]")) {
                binding.setFormValue(Collections.singletonList(userContext.getEmail()));
            }
            else if (name.equals("name") || name.equals("user_name")) {
                binding.setFormValue(Collections.singletonList(userContext.getUserName()));
            }
            else if (name.equals("field_webform_mobile[und][0][value]") && userContext.getPhoneNumber() != null) {
                binding.setFormValue(Collections.singletonList(userContext.getPhoneNumber()));
            }
            else if (name.equals("first_name")) {
                binding.setFormValue(Collections.singletonList(userContext.getFirstName()));
            }
            else if (name.equals("last_name")) {
                binding.setFormValue(Collections.singletonList(userContext.getLastName()));
            }
            else if (name.equals("address_1") || name.equals("data[address1]")) {
                binding.setFormValue(Collections.singletonList(userContext.getAddr1()));
            }
            else if (name.equals("address_2") || name.equals("data[address2]")) {
                binding.setFormValue(Collections.singletonList(userContext.getAddr2()));
            }
            else if (name.equals("city") || name.equals("data[city]")) {
                binding.setFormValue(Collections.singletonList(userContext.getAddrCity()));
            }
            else if (name.equals("state") || name.equals("data[state]")) {
                binding.setFormValue(Collections.singletonList(userContext.getAddrState()));
            }
            else if (name.equals("zip") || name.equals("zip_code") || name.equals(("data[zip]"))) {
                binding.setFormValue(Collections.singletonList(userContext.getAddrZip()));
            }
            else if (name.startsWith("field_webform_pictures[und]") &&
                    mPreselectedImage != null &&
                    mPreselectedImage.length() > 0) {
                binding.setSelectedImage(mPreselectedImage);
            }
        }
    }

    private void onBeginImageActivity(WebFormFieldBinding binding, int requestType) {
        pendingImageResult = binding;
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestType);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if((requestCode==PICK_IMAGE_REQUEST || requestCode == PICK_SFG_IMAGE_REQUEST)
                && pendingImageResult!=null && data!=null) {

            Uri uri = data.getData();
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = managedQuery(uri, projection, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);

            if (requestCode == PICK_IMAGE_REQUEST) {
                pendingImageResult.setSelectedImage(path);
            }
            else if (requestCode == PICK_SFG_IMAGE_REQUEST) {
                pendingImageResult.setSelectedSFGImage(path);
            }
        }
    }

    private void onFileUploadSuccess(int fieldIndex, String fid, String path) {

        WebFormFieldBinding binding = fields.get(fieldIndex);
        binding.addUploadFid(fid);
        binding.setLastUploadedImage(path);

        nextFileUploadOrSubmit();
    }

    private void onSubmitClick() {

        if(validateRequired()) {
            nextFileUploadOrSubmit();
        }
    }

    protected boolean validateRequired() {
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
            if(binding.hasImagesToUpload()) {

                String imgPath = binding.getSelectedImage(binding.getLastUploadedImageIndex() + 1);
                new MyFileUpload(i, imgPath).execute();
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
        params.add(new BasicNameValuePair("uid", userContext.getUserUid()));

        for(WebFormFieldBinding binding : fields) {
            // Save applicable data to SharedPreferences
            String fieldName = binding.getWebFormField().getName();

            // Ignore this binding if there's not field name provided
            if (fieldName == null) {
                continue;
            }

            if (fieldName.equals("first_name")) {
                List<String> firstName = binding.getFormValue();
                userContext.setFirstName(firstName.get(0));
            }
            else if (fieldName.equals("last_name")) {
                List<String> lastName = binding.getFormValue();
                userContext.setLastName(lastName.get(0));
            }
            else if (fieldName.equals("address_1") || fieldName.equals("data[address1]")) {
                List<String> addr1 = binding.getFormValue();
                userContext.setAddr1(addr1.get(0));
            }
            else if (fieldName.equals("address_2") || fieldName.equals("data[address2]")) {
                List<String> addr2 = binding.getFormValue();
                userContext.setAddr2(addr2.get(0));
            }
            else if (fieldName.equals("city") || fieldName.equals("data[city]")) {
                List<String> city = binding.getFormValue();
                userContext.setAddrCity(city.get(0));
            }
            else if (fieldName.equals("state") || fieldName.equals("data[state]")) {
                List<String> state = binding.getFormValue();
                userContext.setAddrState(state.get(0));
            }
            else if (fieldName.equals("zip") || fieldName.equals("zip_code") || fieldName.equals("data[zip]")) {
                List<String> zip = binding.getFormValue();
                userContext.setAddrZip(zip.get(0));
            }

            int fidIndex = 0;

            for(String value : binding.getFormValue()) {
                // Date is special case that needs to be broken out into 3 fields
                if (binding.getLayoutResource() == R.layout.web_form_date_row) {
                    String[] dateValues = value.split("/");
                    if (dateValues.length == 3) {
                        String baseName = binding.getWebFormField().getName();
                        params.add(new BasicNameValuePair(baseName+"[month]",dateValues[0]));
                        params.add(new BasicNameValuePair(baseName+"[day]",dateValues[1]));
                        params.add(new BasicNameValuePair(baseName+"[year]",dateValues[2]));
                    }
                }
                else if (binding.getLayoutResource() == R.layout.web_form_image_row) {
                    String name = "field_webform_pictures[und]["+fidIndex+"][fid]";
                    params.add(new BasicNameValuePair(name, value));
                    fidIndex++;
                }
                else if (binding.getLayoutResource() == R.layout.web_form_select_multi_row) {
                    String baseName = binding.getWebFormField().getName();
                    params.add(new BasicNameValuePair(baseName+"[select]["+value+"]", value));
                }
                else {
                    params.add(new BasicNameValuePair(binding.getWebFormField().getName(), value));
                }
            }
        }

        // Use the custom url if provided, otherwise resort to the default
        String url = DSConstants.API_URL_WEBFORM;
        if (getWebForm().getPostUrl().length() > 0) {
            url = getWebForm().getPostUrl();
        }

        new MySubmitTask(url, params).execute();
    }

    protected void onSubmitSuccess() {

        setResult(RESULT_OK);
        finish();
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

    protected class WebFormFieldBinding {

        private View view;
        private int layoutResource;
        private WebFormField webFormField;
        private int lastUploadedImageIndex;
        private ArrayList<String> selectedImages;
        private ArrayList<String> uploadFids;
        private boolean editDialogOpen = false;


        public WebFormFieldBinding(WebFormField wff) {
            webFormField = wff;

            String type = webFormField.getType();
            if (type.equals("select")) {
                String selectType = webFormField.getSelectType();
                if (selectType != null && selectType.equals("multiple")) {
                    layoutResource = R.layout.web_form_select_multi_row;
                }
                else {
                    layoutResource = R.layout.web_form_select_single_row;
                }
            }
            else if (type.equals("number")) {
                layoutResource = R.layout.web_form_number_row;
            }
            else if (type.equals("phone") || type.equals("tel")) {
                layoutResource = R.layout.web_form_phone_row;
            }
            else if (type.equals("email")) {
                layoutResource = R.layout.web_form_email_row;
            }
            else if (type.equals("date")) {
                layoutResource = R.layout.web_form_date_row;
            }
            else if (type.equals("file")) {
                layoutResource = R.layout.web_form_image_row;
                lastUploadedImageIndex = -1;
                selectedImages = new ArrayList<String>();
                uploadFids = new ArrayList<String>();
            }
            else if (type.equals("sfg-image")) {
                layoutResource = R.layout.web_form_sfg_image_row;
                lastUploadedImageIndex = -1;
                selectedImages = new ArrayList<String>();
            }
            else if (type.equals("textarea")) {
                layoutResource = R.layout.web_form_textarea_row;
            }
            else if (type.equals("label")) {
                layoutResource = R.layout.web_form_label_row;
            }
            else if (type.equals("school_search")) {
                layoutResource = R.layout.web_form_school_search_row;
            }
            else {
                layoutResource = R.layout.web_form_textfield_row;
            }

            attatchView();
        }

        public void attatchView() {

            view = inflater.inflate(layoutResource, null);

            TextView label = (TextView)view.findViewById(R.id.label);
            String labelText = webFormField.getLabel();
            if (webFormField.isRequired()) {
                labelText += " *";
            }

            if (label != null) {
                label.setText(labelText);

                if (layoutResource == R.layout.web_form_label_row)
                    label.setTypeface(typefaceReg);
                else
                    label.setTypeface(typefaceBold);
            }

            switch(layoutResource) {
                case R.layout.web_form_select_single_row: {
                    List<String> options = new ArrayList<String>();
                    for(WebFormSelectOptions wfso : webFormField.getSelectOptions()) {
                        options.add(wfso.getLabel());
                    }
                    Spinner spinner = (Spinner)view.findViewById(R.id.field_select_single);
                    spinner.setAdapter(new ArrayAdapter<String>(AbstractWebForm.this, android.R.layout.simple_spinner_dropdown_item, options));
                    break;
                }
                case R.layout.web_form_select_multi_row: {
                    LinearLayout layout = (LinearLayout)view.findViewById(R.id.field_select_multi);
                    for(WebFormSelectOptions wfso : webFormField.getSelectOptions()) {
                        CheckBox checkbox = new CheckBox(AbstractWebForm.this);
                        checkbox.setTextColor(getResources().getColor(R.color.web_form_checkbox_label));
                        checkbox.setText(wfso.getLabel());
                        checkbox.setTag(wfso.getValue());
                        layout.addView(checkbox, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                    }
                    break;
                }
                case R.layout.web_form_date_row: {
                    EditText field = (EditText)view.findViewById(R.id.field_date);
                    field.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
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
                case R.layout.web_form_image_row: {
                    Button button = (Button)view.findViewById(R.id.button);
                    button.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            AbstractWebForm.this.onBeginImageActivity(WebFormFieldBinding.this, PICK_IMAGE_REQUEST);
                        }
                    });
                    updateImagePreview();
                    break;
                }
                case R.layout.web_form_school_search_row: {
                    Spinner spinner = (Spinner)view.findViewById(R.id.field_school_search_state);
                    spinner.setAdapter(new ArrayAdapter<String>(AbstractWebForm.this, android.R.layout.simple_spinner_dropdown_item, STATES));

                    TextView nameLabel = (TextView)view.findViewById(R.id.label_school_search_name);
                    TextView stateLabel = (TextView)view.findViewById(R.id.label_school_search_state);
                    TextView resultsLabel = (TextView)view.findViewById(R.id.label_school_search_results);
                    nameLabel.setTypeface(typefaceBold);
                    stateLabel.setTypeface(typefaceBold);
                    resultsLabel.setTypeface(typefaceBold);

                    Button button = (Button)view.findViewById(R.id.school_search);
                    button.setTypeface(typefaceBold);
                    button.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String name = ((TextView)findViewById(R.id.field_school_search_name)).getText().toString();
                            int stateSelection = ((Spinner)findViewById(R.id.field_school_search_state)).getSelectedItemPosition();
                            String state = STATES[stateSelection];

                            if (name == null || name.length() == 0) {
                                // Warn that the school name is required
                                new AlertDialog.Builder(AbstractWebForm.this)
                                        .setMessage(getString(R.string.school_search_name_required))
                                        .setCancelable(false)
                                        .setPositiveButton(getString(R.string.ok_upper), null)
                                        .create()
                                        .show();
                            }
                            else {
                                TextView labelView = (TextView)findViewById(R.id.label_school_search_results);
                                Spinner resultsView = (Spinner)findViewById(R.id.field_school_search_results);
                                new SchoolSearchTask(state, name, labelView, resultsView).execute();
                            }
                        }
                    });
                    break;
                }
                case R.layout.web_form_sfg_image_row: {
                    Button button = (Button)view.findViewById(R.id.button);
                    button.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            AbstractWebForm.this.onBeginImageActivity(WebFormFieldBinding.this, PICK_SFG_IMAGE_REQUEST);
                        }
                    });
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
                        field.setText(new SimpleDateFormat(DSConstants.DATE_FORMAT, Locale.US).format(date));
                        editDialogOpen = false;
                    }
                }, 1995, 0, 1).show();
                editDialogOpen = true;
            }
        }

        private void updateImagePreview() {
            // Clear all images
            LinearLayout content = (LinearLayout)view.findViewById(R.id.content);
            if (content != null) {
                content.removeAllViews();

                for (int i = 0; selectedImages != null && i < selectedImages.size(); i++) {
                    Context c = getApplicationContext();
                    ImageView imageView = new ImageView(c);
                    // Convert dp into pixels before setting height and width
                    int dpSize = 80;
                    float scale = getResources().getDisplayMetrics().density;	// screen's density scale
                    // +0.5 is to round up to nearest whole number when int conversion happens
                    int pixelSize = (int)(dpSize * scale + 0.5f);
                    imageView.setLayoutParams(new LayoutParams(pixelSize, pixelSize));
                    try {
                        // Set drawable
                        Bitmap bitmap = getCompressedBitmap(selectedImages.get(i), 80);
                        imageView.setImageBitmap(bitmap);

                        // Add new image to the view
                        content.addView(imageView);
                    }
                    catch (Exception e) {
                        //Log.v("IMAGE", "caught exception setting the image");
                    }
                }
            }
        }

        public void setSelectedSFGImage(String path) {
            // Only one image can be uploaded at a time for share-for-good campaigns
            selectedImages.clear();
            selectedImages.add(path);
            updateImagePreview();
        }

        public void setSelectedImage(String path) {
            selectedImages.add(path);
            updateImagePreview();
        }

        public String getSelectedImage(int i) {
            if (selectedImages != null && i >= 0 && i < selectedImages.size())
                return selectedImages.get(i);
            else
                return null;
        }

        public void setLastUploadedImage(String path) {
            for (int i = 0; selectedImages != null && i < selectedImages.size(); i++) {
                if (selectedImages.get(i) == path)
                    lastUploadedImageIndex = i;
            }
        }

        public int getLastUploadedImageIndex() {
            return lastUploadedImageIndex;
        }

        public boolean hasImagesToUpload() {
            if (selectedImages != null && lastUploadedImageIndex < selectedImages.size() - 1)
                return true;
            else
                return false;
        }

        public void addUploadFid(String fid) {
            uploadFids.add(fid);
        }

        public View getView() {
            return view;
        }

        public WebFormField getWebFormField() {
            return webFormField;
        }

        public int getLayoutResource() {
            return layoutResource;
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
                    if (values.size() > 0) {
                        if (selectedImages != null) {
                            selectedImages.clear();
                        }

                        for (int i = 0; i < values.size(); i++) {
                            selectedImages.add(values.get(i));
                        }

                        updateImagePreview();
                    }
                    break;
                }
                case R.layout.web_form_sfg_image_row: {
                    if (values.size() > 0) {
                        updateImagePreview();
                    }
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
                    if (uploadFids != null && uploadFids.size() > 0) {
                        for (int i = 0; i < uploadFids.size(); i++) {
                            answer.add(uploadFids.get(i));
                        }
                    }
                    else if (selectedImages != null && selectedImages.size() > 0) {
                        for (int i = 0; i < selectedImages.size(); i++) {
                            answer.add(selectedImages.get(i));
                        }
                    }
                    break;
                }
                case R.layout.web_form_sfg_image_row: {
                    if (selectedImages != null && selectedImages.size() > 0) {
                        answer.add(selectedImages.get(0));
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
                case R.layout.web_form_school_search_row: {
                    Spinner field = (Spinner)view.findViewById(R.id.field_school_search_results);
                    int selection = field.getSelectedItemPosition();
                    answer.add(Integer.toString(schoolGSIDs[selection]));
                    break;
                }
                case R.layout.web_form_label_row: {
                    // Nothing to save here, just ignore
                    break;
                }
                default: {
                    throw new RuntimeException();
                }
            }

            return answer;
        }
    }

    /**
     * Webservice task to upload a picture file.
     */
    private class MyFileUpload extends AbstractWebserviceTask {

        private int fieldIndex;
        private String path;

        public boolean uploadSuccess;
        private String fid;

        public MyFileUpload(int fieldIndex, String path){
            super(userContext);
            this.fieldIndex = fieldIndex;
            this.path = path;
        }

        @Override
        protected void onSuccess() {

            if(uploadSuccess) {
                AbstractWebForm.this.onFileUploadSuccess(fieldIndex, fid, path);
            } else {
                onError(null);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgressBarIndeterminateVisibility(Boolean.TRUE);
        }

        @Override
        protected void onFinish() {
            if (!submitTaskInProgress) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }
        }

        @Override
        protected void onError(Exception e) {
            new AlertDialog.Builder(AbstractWebForm.this)
                    .setMessage(getString(R.string.auth_failed))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok_upper), null)
                    .create()
                    .show();
        }

        @Override
        protected void doWebOperation() throws Exception {

            Bitmap bitmap = getCompressedBitmap(path, 600);

            ByteArrayOutputStream byteos = new ByteArrayOutputStream();
            Base64OutputStream baseos = new Base64OutputStream(byteos, Base64.DEFAULT);
            bitmap.compress(CompressFormat.JPEG, 50, baseos);


            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("file", byteos.toString("UTF-8")));
            params.add(new BasicNameValuePair("filename", new File(path).getName()));

            WebserviceResponse response = doPost(DSConstants.API_URL_FILE, params);

            if(response.getStatusCode()>=400 && response.getStatusCode()<500) {
                uploadSuccess = false;
            } else {
                JSONObject obj = response.getBodyAsJSONObject();
                fid = obj.getString("fid");
                uploadSuccess = true;
            }
        }

    }

    /**
     * Webservice task to submit the webform.
     */
    private class MySubmitTask extends AbstractWebserviceTask {

        private String url;
        private List<NameValuePair> params;

        private String validationMessage;
        private boolean submitSuccess = false;

        public MySubmitTask(String url, List<NameValuePair> params) {
            super(userContext);
            this.url = url;
            this.params = params;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgressBarIndeterminateVisibility(Boolean.TRUE);
            submitTaskInProgress = true;
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
            setProgressBarIndeterminateVisibility(Boolean.FALSE);
            submitTaskInProgress = false;
        }

        @Override
        protected void onError(Exception e) {
            new AlertDialog.Builder(AbstractWebForm.this)
                    .setMessage(getString(R.string.form_submit_failed))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok_upper), null)
                    .create()
                    .show();
        }

        @Override
        protected void doWebOperation() throws Exception {

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

    /**
     * Webservice task for getting schools and their info given a state and the school name.
     */
    private class SchoolSearchTask extends AbstractWebserviceTask {
        private TextView labelView;
        private Spinner resultsView;

        private String searchState;
        private String searchName;
        private int searchLimit = 10;

        private JSONArray jsonResults;

        /**
         * SchoolSearchTask constructor for finding matching schools based on provided info.
         *
         * @param state two letter abbreviation for the state to search in. Must be capitalized.
         * @param name name of the school to search for.
         * @param lView the label for search results.
         * @param sView the spinner to populate with search results.
         */
        public SchoolSearchTask(String state, String name, TextView lView, Spinner sView) {
            super(userContext);

            searchState = state;
            searchName = name;

            labelView = lView;
            resultsView = sView;

            if (labelView != null) {
                labelView.setVisibility(View.GONE);
            }

            if (resultsView != null) {
                resultsView.setVisibility(View.GONE);
            }
        }

        @Override
        protected void doWebOperation() throws Exception {
            String name = URLEncoder.encode(searchName.trim(), "utf-8");
            String state = URLEncoder.encode(searchState.trim(), "utf-8");
            String url = "http://lofischools.herokuapp.com/search?query="+name+"&state="+state+"&limit="+searchLimit;
            WebserviceResponse response = doGet(url);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 400) {
                JSONObject jsonResponse = response.getBodyAsJSONObject();
                jsonResults = jsonResponse.optJSONArray("results");
            }
        }

        @Override
        protected void onSuccess() {
        }

        @Override
        protected void onFinish() {
            labelView.setVisibility(View.VISIBLE);
            if (jsonResults != null && jsonResults.length() > 0) {
                // Update the TextView with the "results found" label
                labelView.setText(R.string.school_search_results_label);

                schoolGSIDs = new int[jsonResults.length()];
                String[] schools = new String[jsonResults.length()];
                for (int i = 0; i < jsonResults.length(); i++) {
                    try {
                        JSONObject result = jsonResults.getJSONObject(i);

                        // Populate the schools array to display in a spinner selector
                        schools[i] = result.getString("name");

                        // Cache the GSID for when the form is later submitted
                        schoolGSIDs[i] = result.getInt("gsid");
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // Update the dropdown spinner with the school results
                resultsView.setAdapter(new ArrayAdapter<String>(AbstractWebForm.this, android.R.layout.simple_spinner_dropdown_item, schools));
                resultsView.setVisibility(View.VISIBLE);
            }
            else {
                // If no results, update the text with the "no results" label
                labelView.setText(R.string.school_search_no_results_label);
            }
        }

        @Override
        protected void onError(Exception e) {
        }
    }

}
