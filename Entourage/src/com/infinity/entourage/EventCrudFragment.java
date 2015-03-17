package com.infinity.entourage;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.infinity.asynctask.HttpClientJSONPOST;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class EventCrudFragment extends Fragment implements OnItemSelectedListener, TimePickerDialog.OnTimeSetListener,DatePickerDialog.OnDateSetListener {

	EditText eventName; 
	EditText streetAddress; 
	EditText suiteAddress; 
	EditText cityAddress; 
	EditText stateAddress; 
	
	Spinner typeSpinner; 
	JSONObject jsonRequestObj;
	int eventStartHour;
	String sessionId; 
	String username; 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		jsonRequestObj = new JSONObject();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.fragment_event_crud, container,false);

		SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs",getActivity().MODE_PRIVATE); 
		sessionId = prefs.getString("sessionid", ""); 
		username = prefs.getString("username", ""); 
		
		eventName = (EditText) v.findViewById(R.id.event_name); 
		stateAddress = (EditText) v.findViewById(R.id.state_address); 
		streetAddress = (EditText) v.findViewById(R.id.street_address); 
		cityAddress = (EditText) v.findViewById(R.id.city_address); 
		suiteAddress = (EditText)v.findViewById(R.id.suite_address); 
		
		typeSpinner = (Spinner) v.findViewById(R.id.type_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.event_types,android.R.layout.simple_spinner_item);
		typeSpinner.setAdapter(adapter);
		typeSpinner.setOnItemSelectedListener(this);

		Button timeButton = (Button) v.findViewById(R.id.selectTime);
		timeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				TimeFragment tf = new TimeFragment();
				tf.show(getFragmentManager(), "timePicker");

			}
		});

		Button dateButton = (Button) v.findViewById(R.id.selectDate);
		dateButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				DateFragment df = new DateFragment();
				df.show(getFragmentManager(), "datePicker");

			}
		});

		RadioGroup privacyGroup = (RadioGroup) v.findViewById(R.id.event_privacy_radiogroup);
		privacyGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

//				switch (checkedId) {
//				
//				case R.id.radioPublic:
//
//					try {
//						jsonRequestObj.put("eventPrivacy", "public");
//					} catch (JSONException e) {
//						e.printStackTrace();
//					}
//					
//					break;
//					
//				case R.id.radioPrivate:
//
//					try {
//						jsonRequestObj.put("eventPrivacy", "private");
//						Log.d("Event Privacy json", jsonRequestObj.toString());
//					} catch (JSONException e) {
//						e.printStackTrace();
//					}
//					// show invitation dialog/fragment prompt
//
//					break;
//				}
				
				
				if( checkedId == R.id.radioPublic){
					try {
						jsonRequestObj.put("eventPrivacy", "public");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}


				else if(checkedId ==  R.id.radioPrivate){
					try {
						jsonRequestObj.put("eventPrivacy", "private");
						Log.d("Event Privacy json", jsonRequestObj.toString());
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

					
					// show invitation dialog/fragment prompt

				}
			

		});
		
		RadioButton publicButton = (RadioButton) v.findViewById(R.id.radioPublic);
		RadioButton privateButton = (RadioButton) v.findViewById(R.id.radioPrivate);

		Button createButton = (Button) v.findViewById(R.id.create_button);
		createButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d("C Y C L E Tracker", "EventCrudFragment doInBackground()"); 
				Log.d("sessionid", sessionId); 
				//This click listener is where will will aggregate all values from text fields (not equipped with listeners)
				String eventNameStr = eventName.getText().toString(); 
				String streetAddressStr = streetAddress.getText().toString(); 
				String suiteAddressStr = suiteAddress.getText().toString(); 
				String cityAddressStr = cityAddress.getText().toString(); 
				String stateAddressStr = stateAddress.getText().toString(); 
				//String username = eventName.getText().toString(); 
				
				
				
				
				//implement session key asap. look through innotes/ articles 
				String eventHost = eventName.getText().toString(); 
				String keonw = "keon w";  
				
				try {
					jsonRequestObj.put("eventName", eventNameStr);
					//static username until validation story is implemented 
					jsonRequestObj.put("username", username); 
					jsonRequestObj.put("eventStreet", streetAddressStr); 
					jsonRequestObj.put("eventSuite", suiteAddressStr); 
					jsonRequestObj.put("eventCity", cityAddressStr);
					jsonRequestObj.put("eventState", stateAddressStr); 
					jsonRequestObj.put("sessionid", sessionId); 
					
					Log.wtf("where the fuck is the session id???", sessionId); 
					
				} catch (JSONException e) {
					e.printStackTrace();
					Log.d("EventCrudFragment: createButtonClicked, json:", jsonRequestObj.toString());
					
				}
				
				String url = "http://10.0.0.6:9000/rest/createEvent";
				new EventCrudAsyncTask().execute(url, jsonRequestObj.toString()); 
			}
		});
		return v;

	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	private class EventCrudAsyncTask extends AsyncTask<String, Void, String> {

		JSONObject jsonRequestObj;
		String jsonResponseStr;
		String url;

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			Log.d("C Y C L E Tracker", "EntourageAsyncTask doInBackground()"); 
			try {
				jsonRequestObj = new JSONObject(params[1]);
				HttpClientJSONPOST post = new HttpClientJSONPOST(params[0],
						jsonRequestObj);

				jsonResponseStr = post.executePOST();

				return jsonResponseStr;
			} catch (JSONException e) {
				e.printStackTrace();
				
			}
			// Check log for JSON response
			// Log.d();
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			Log.d("C Y C L E Tracker", "EntourageAsyncTask onPostExecute()"); 
			super.onPostExecute(result);
			if (result == null | result == "")
				return;
			// if (pDialog.isShowing())
			// pDialog.dismiss();
			try {
				// String success =
				// jobj.getJSONObject("User").getString("status");
				Gson gson = new Gson();

				JSONObject jsonResultObj = new JSONObject(result);
				String success = jsonResultObj.getJSONObject("EventResponse")
						.getString("success");
				String status = jsonResultObj.getJSONObject("EventResponse").getString("status"); 
				Log.d("Event Creation response: success", success);
				Log.d("Event Creation response: status", status);

				if (success.equals("0")) {
					Toast.makeText(getActivity(), "Event Creation Successful",
							Toast.LENGTH_LONG).show();

					// use the transaction manager to replace the
					// eventcrudfragment
					// Intent intent = new Intent(getActivity(),
					// EntourageMainActivity.class);
					// startActivity(intent);

				} else
					Toast.makeText(getActivity(), "Event creation failed",
							Toast.LENGTH_LONG).show();

			} catch (JSONException e) {
				e.printStackTrace();
				Log.d("EventCrudAsyncTask: onPostExecute() creating and executing Post, json:", jsonRequestObj.toString());
			}
		}
	}

	public static class TimeFragment extends DialogFragment {

		public interface onEventTimeSetListener {
			public void onEventTimeSet(Calendar cal);
		}

		onEventTimeSetListener mCallback;

		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Calendar now = Calendar.getInstance();
			int hour = now.get(Calendar.HOUR);
			int minute = now.get(Calendar.MINUTE);
			return new TimePickerDialog(getActivity(),
					(EventCrudFragment) getFragmentManager().findFragmentById(R.id.eventsContainer), hour, minute,DateFormat.is24HourFormat(getActivity()));

		}

	}

	public static class DateFragment extends DialogFragment {
//		onEventDateSetListener mCallback;
//
//		public interface onEventDateSetListener {
//			public void onEventDateSet(Calendar cal);
//		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Calendar now = Calendar.getInstance();
			int year = now.get(Calendar.YEAR);
			int day = now.get(Calendar.DAY_OF_MONTH);
			int month = now.get(Calendar.MONTH);

			return new DatePickerDialog(getActivity(),
					(EventCrudFragment) getFragmentManager().findFragmentById(R.id.eventsContainer), year, month, day);

		}

	}

	// Methods for spinner interface
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {
		// TODO Auto-generated method stub

		String eventType = parent.getItemAtPosition(position).toString();
		Log.d("EventType String", eventType);

		try {
			jsonRequestObj.put("eventType", eventType);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		// TODO Auto-generated method stub
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR, hourOfDay);
		cal.set(Calendar.MINUTE, minute);

		try {
			jsonRequestObj.put("eventHour", hourOfDay);
			jsonRequestObj.put("eventMinute", minute);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.d("Event Time json", jsonRequestObj.toString());

	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		// TODO Auto-generated method stub
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, monthOfYear);
		cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

		try {
			jsonRequestObj.put("eventYear", year);
			jsonRequestObj.put("eventMonth", monthOfYear);
			jsonRequestObj.put("eventDay", dayOfMonth);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.d("Event Time json", jsonRequestObj.toString());


	}

	public void onRadioButtonClicked(View v) {

		boolean checked = ((RadioButton) v).isChecked();

	
		
		if (v.getId() == R.id.radioPublic){
		
			if (checked) {

				try {
					jsonRequestObj.put("eventPrivacy", "public");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		if(v.getId() == R.id.radioPrivate){
			if (checked) {

				try {
					jsonRequestObj.put("eventPrivacy", "private");
					Log.d("Event Privacy json", jsonRequestObj.toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// show invitation dialog/fragment prompt

			}
		
		}

	}

}
