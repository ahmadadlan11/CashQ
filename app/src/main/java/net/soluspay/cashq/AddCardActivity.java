package net.soluspay.cashq;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialog;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment;
import com.gndi_sd.szzt.R;

import net.soluspay.cashq.model.Card;
import net.soluspay.cashq.model.EBSRequest;
import net.soluspay.cashq.utils.Globals;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddCardActivity extends AppCompatActivity {

    @BindView(R.id.pan)
    EditText pan;
    @BindView(R.id.card_name)
    EditText cardName;
    @BindView(R.id.save)
    Button save;
    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.exp_date)
    TextView expDate;

    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);
        ButterKnife.bind(this);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Add New Card");

    }

    public void addCard() {

        final ProgressDialog progressDialog;
        progressDialog = ProgressDialog.show(this, "Loading", "Please wait...", false, false);
        EBSRequest request = new EBSRequest();

        SharedPreferences sp = getSharedPreferences("credentials", Activity.MODE_PRIVATE);
        String token = sp.getString("token", null);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("pan", pan.getText().toString());
            jsonObject.put("name", cardName.getText().toString());
            jsonObject.put("exp_date", date);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(request.serverUrl() + Constants.ADD_CARD)
                .addJSONObjectBody(jsonObject)
                .setTag("test")
                .setPriority(Priority.MEDIUM)
                .addHeaders("Authorization", token)
                .build().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                // do anything with response

                progressDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(AddCardActivity.this);
                builder.setTitle("Successful")
                        .setMessage("Your card has been added successfully")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                                finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            }

            @Override
            public void onError(ANError error) {
                // handle error
                Log.i("Add Card", error.getErrorBody());
                progressDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(AddCardActivity.this);
                builder.setTitle("Failed")
                        .setMessage("Something went wrong")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                Log.i("MESSAGE", "Fail");


            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        onBackPressed();
        return true;
    }

    public void pickDate() {
        int yearSelected;
        int monthSelected;

        // Use the calendar for create ranges
        Calendar calendar = Calendar.getInstance();

        calendar.clear();
        calendar.set(2019, 8, 1); // Set minimum date to show in dialog
        long minDate = calendar.getTimeInMillis(); // Get milliseconds of the modified date

        calendar.clear();
        calendar.set(2030, 11, 31); // Set maximum date to show in dialog
        long maxDate = calendar.getTimeInMillis(); // Get milliseconds of the modified date

        yearSelected = calendar.get(Calendar.YEAR);
        monthSelected = calendar.get(Calendar.MONTH);

        String customTitle = "Pick a date";

        MonthYearPickerDialogFragment dialogFragment = MonthYearPickerDialogFragment
                .getInstance(monthSelected, yearSelected, minDate, maxDate, customTitle);

        dialogFragment.show(getSupportFragmentManager(), null);

        dialogFragment.setOnDateSetListener(new MonthYearPickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(int year, int monthOfYear) {
                // do something
                String yearString = Integer.toString(year);
                int month = monthOfYear + 1;
                expDate.setText(String.format("%02d / %s", month, year));
                date = String.format("%s%02d", yearString.substring(2), month);
                Log.i("Date", date);
            }
        });

    }


    @OnClick(R.id.save)
    public void onSaveClicked() {
        boolean error = false;
        if(pan.getText().toString().isEmpty())
        {
            error = true;
            pan.setError("Enter a card number");
        }
        if(!(pan.getText().toString().length() == 16 || pan.getText().toString().length() == 19))
        {
            error = true;
            pan.setError("Enter a valid card number");
        }
        if(cardName.getText().toString().isEmpty())
        {
            error = true;
            cardName.setError("Card name cannot be empty");
        }
        if(!error)
        {
            addCard();
        } else {
            //manage error case here
        }
    }

    @OnClick(R.id.exp_date)
    public void onViewClicked() {
        pickDate();
    }
}
