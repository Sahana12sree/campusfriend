package com.blackboxindia.bitcampusfriend.Fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.blackboxindia.bitcampusfriend.Network.Interfaces.onCompleteListener;
import com.blackboxindia.bitcampusfriend.Network.NetworkMethods;
import com.blackboxindia.bitcampusfriend.R;
import com.blackboxindia.bitcampusfriend.activities.MainActivity;
import com.blackboxindia.bitcampusfriend.adapters.NewAdImageAdapter;
import com.blackboxindia.bitcampusfriend.cameraIntentHelper.ImageUtils;
import com.blackboxindia.bitcampusfriend.dataModels.AdData;
import com.blackboxindia.bitcampusfriend.dataModels.AdTypes;
import com.blackboxindia.bitcampusfriend.dataModels.DateObject;
import com.blackboxindia.bitcampusfriend.dataModels.UserInfo;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Frag_newEvent extends Fragment {

    //region Variables

    private static String TAG = Frag_newEvent.class.getSimpleName()+" YOYO";
    private static Integer ADD_PHOTO_CODE = 154;

    EditText etTitle, etDescription;
    TextView etDate, etTime;
    Button btn_newImg;
    CardView btn_Create;
    RecyclerView recyclerView;
    NewAdImageAdapter adapter;
    View view;
    Context context;

    UserInfo userInfo;
    ImageUtils imageUtils;
    ArrayList<Uri> imgURIs;

    Calendar myCalendar;
    NetworkMethods networkMethods;

    int count = 0;
    private static final int NUMBER_OF_DUPLICATES = 1;
    //endregion

    //region Initial Setup

    @Override
    public void onResume() {
        ((MainActivity)context).toolbar.setTitle(MainActivity.TITLE_NewEvent);
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_newevent, container, false);

        initVariables();

        etTitle.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void afterTextChanged(Editable s) {

                for(int i = s.length(); i > 0; i--) {
                    if(s.subSequence(i-1, i).toString().equals("\n"))
                        s.replace(i-1, i, " ");
                }
            }
        });
        etTitle.requestFocus();

        setUpRecycler();

        populateDateTime();

        setUpListeners();

        initCamera();

        return view;
    }

    private void initVariables() {
        myCalendar = Calendar.getInstance();

        etTitle = view.findViewById(R.id.AdTitle);
        etDate = view.findViewById(R.id.newAd_etDate);
        etTime = view.findViewById(R.id.newAd_etTime);

        etDescription = view.findViewById(R.id.newAd_etDescription);

        btn_newImg = view.findViewById(R.id.newAd_btnAddImg);
        btn_Create = view.findViewById(R.id.newAd_btnCreate);

        context = view.getContext();
        imgURIs = new ArrayList<>();
    }

    private void setUpRecycler() {
        recyclerView = view.findViewById(R.id.ImageRecycler);
        adapter = new NewAdImageAdapter(context, new NewAdImageAdapter.onDeleteClickListener() {
            @Override
            public void onDelete(int position) {
                imgURIs.remove(position);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false));
    }

    @SuppressLint("SimpleDateFormat")
    private void populateDateTime(){
        Calendar currentDate= Calendar.getInstance();

        SimpleDateFormat format = new SimpleDateFormat("d", Locale.US);
        String date = format.format(new Date());
        if(date.endsWith("1") && !date.endsWith("11"))
            format = new SimpleDateFormat("EEEE, d'st' MMMM ''yy");
        else if(date.endsWith("2") && !date.endsWith("12"))
            format = new SimpleDateFormat("EEEE, d'nd' MMMM ''yy");
        else if(date.endsWith("3") && !date.endsWith("13"))
            format = new SimpleDateFormat("EEEE, d'rd' MMMM ''yy");
        else
            format = new SimpleDateFormat("EEEE, d'th' MMMM ''yy");

        etDate.setText(format.format(currentDate.getTime()));

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
        etTime.setText( sdf.format(currentDate.getTime()));
    }

    private void setUpListeners() {

        btn_Create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateForm())
                    prepareAndCreateAd();
            }
        });
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ((T)v).setError(null);
                Calendar currentDate= Calendar.getInstance();
                int
                    yy = currentDate.get(Calendar.YEAR),
                    mm = currentDate.get(Calendar.MONTH),
                    dd = currentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                myCalendar.set(Calendar.YEAR, year);
                                myCalendar.set(Calendar.MONTH, monthOfYear);
                                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

//                                String myFormat = "EEEE'th,'dd/MM/yy";
                                String myFormat = "EEEE, MMMM d ''yy";
                                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                                etDate.setText(sdf.format(myCalendar.getTime()));
                            }
                        }, yy, mm, dd);
                datePickerDialog.setCancelable(true);
                datePickerDialog.setTitle("Set Date:");
                datePickerDialog.show();

            }
        });

        etTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ((EditText)v).setError(null);
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                        myCalendar.set(Calendar.HOUR_OF_DAY,selectedHour);
                        myCalendar.set(Calendar.MINUTE,selectedMinute);

                        String myFormat = "hh:mm a";
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                        etTime.setText( sdf.format(myCalendar.getTime()));
                    }
                }, hour, minute, false);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });
    }
    //endregion

    private boolean validateForm(){
        boolean f = true;
//        if(etDate.getText().toString().trim().equals("")){
//            etDate.setError("Required!");
//            f = false;
//        }
//        if(etTime.getText().toString().trim().equals("")){
//            etTime.setError("Required!");
//            f = false;
//        }
        if(etDescription.getText().toString().trim().equals("")){
            etDescription.setError("Please give some details about the event");
            f = false;
        }
        if(etTitle.getText().toString().equals("") || etTitle.getText().toString().toLowerCase().contains("title")){
            etTitle.setError("Please give a suitable title");
            f = false;
        }
        return f;
    }

    private void prepareAndCreateAd() {
        userInfo = ((MainActivity)context).userInfo;
        if(userInfo!=null) {

            AdData event = new AdData();

            event.setCreatedBy(userInfo);
            event.setTitle(etTitle.getText().toString().trim());
            event.setPrice(null);
            event.setDescription(etDescription.getText().toString().trim());

            event.setNumberOfImages(imgURIs.size());
            event.setDateTime(new DateObject(myCalendar));

            event.setType(AdTypes.TYPE_EVENT);

            networkMethods = new NetworkMethods(context);

//            Bitmap major = adapter.getMajor();
//            if(major==null)
                //Log.e(TAG, "prepareAndCreateAd: major null");

            createAd(event);

        }
        else
        {
            Toast.makeText(context, "Not Logged in!", Toast.LENGTH_SHORT).show();
        }
    }

    void createAd(AdData event){
        networkMethods.createNewAd(userInfo, event, imgURIs, adapter.getMajor(), new onCompleteListener<AdData>() {
            @Override
            public void onSuccess(AdData event) {

                count++;
                if(count<NUMBER_OF_DUPLICATES){
                    Toast.makeText(context, "In Progress #"+count, Toast.LENGTH_SHORT).show();
                    if(event.getTitle().contains("#"))
                        event.setTitle(event.getTitle().replace("#"+(count-1),"#"+count));
                    else
                        event.setTitle(event.getTitle()+ " #1");

                    event.setDescription(event.getDescription()+" #"+count);
                    if(event.getPrice()!=null)
                        event.setPrice(event.getPrice()+count);
                    createAd(event);
                }else{
                    ((MainActivity)context).onBackPressed();
                    ((MainActivity)context).createSnackbar("Ad Created Successfully");
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "Error: "+ e.getMessage() , Toast.LENGTH_SHORT).show();
            }
        });
    }

    //region Camera Setup
    private void initCamera() {
        imageUtils = new ImageUtils(getActivity(), this, true, new ImageUtils.ImageAttachmentListener() {
            @Override
            public void image_attachment(int from, String filename, Bitmap file, Uri uri) {
                if(imgURIs.isEmpty())
                    view.findViewById(R.id.ImgRecyclerHint).setVisibility(View.GONE);
                imgURIs.add(uri);
                adapter.addImage(file);
            }
        });

        btn_newImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageUtils.imagepicker(ADD_PHOTO_CODE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        imageUtils.request_permission_result(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageUtils.onActivityResult(requestCode, resultCode, data);
    }
    //endregion

}
