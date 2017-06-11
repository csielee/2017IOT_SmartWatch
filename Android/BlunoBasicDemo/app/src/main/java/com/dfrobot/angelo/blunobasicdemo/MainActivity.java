package com.dfrobot.angelo.blunobasicdemo;

import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

public class MainActivity  extends BlunoLibrary {
	private Button buttonScan;
	private Button buttonSerialSend;
	private Button buttonSettime;
	private Button buttonCalHRV;
	private Button buttonClearsteps;
	private EditText serialSendText;
	private TextView serialReceivedText;
	private String command;
	//private int IBIdata_count;
	private Vector IBIdata = new Vector(256);


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        onCreateProcess();														//onCreate Process by BlunoLibrary


        serialBegin(115200);													//set the Uart Baudrate on BLE chip to 115200

        serialReceivedText=(TextView) findViewById(R.id.serialReveicedText);	//initial the EditText of the received data
        serialSendText=(EditText) findViewById(R.id.serialSendText);			//initial the EditText of the sending data

        buttonSerialSend = (Button) findViewById(R.id.buttonSerialSend);		//initial the button for sending the data
        buttonSerialSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				serialSend(serialSendText.getText().toString());				//send the data to the BLUNO
			}
		});

        buttonScan = (Button) findViewById(R.id.buttonScan);					//initial the button for scanning the BLE device
        buttonScan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				buttonScanOnClickProcess();										//Alert Dialog for selecting the BLE device
			}
		});

		buttonSettime = (Button) findViewById(R.id.buttonSettime);
		buttonSettime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				command = "Settime";
				// get curr time from cellphone
				Calendar c = Calendar.getInstance();
				SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
				String str = df.format(c.getTime());
				String[] strs = str.split(":");
				// send time to SmartWatch
				// ex : "h12;m12;s12;" is mean 12:12:12
				String sendstr = "h" + strs[0] + ";m" + strs[1] + ";s" + strs[2] + ";";
				serialSend(sendstr);
			}
		});

		buttonCalHRV = (Button) findViewById(R.id.buttonCalHRV);
		buttonCalHRV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				command = "CalHRV";
				// get IBI data from SmartWatch
				// "g;" can get all IBI data , if not enough will get "NO"
				IBIdata.clear();
				serialSend("g;");
			}
		});

		buttonClearsteps = (Button) findViewById(R.id.buttonClearsteps);
		buttonClearsteps.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				command = "Clearsteps";
				// send "c;" to clear steps
				serialSend("c;");
			}
		});

		buttonSettime.setEnabled(false);
		buttonCalHRV.setEnabled(false);
		buttonClearsteps.setEnabled(false);
	}

	protected void onResume(){
		super.onResume();
		System.out.println("BlUNOActivity onResume");
		onResumeProcess();														//onResume Process by BlunoLibrary
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
		super.onActivityResult(requestCode, resultCode, data);
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        onPauseProcess();														//onPause Process by BlunoLibrary
    }
	
	protected void onStop() {
		super.onStop();
		onStopProcess();														//onStop Process by BlunoLibrary
	}
    
	@Override
    protected void onDestroy() {
        super.onDestroy();	
        onDestroyProcess();														//onDestroy Process by BlunoLibrary
    }

	@Override
	public void onConectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called
		switch (theConnectionState) {											//Four connection state
		case isConnected:
			buttonScan.setText("Connected");
			buttonSettime.setEnabled(true);
			buttonCalHRV.setEnabled(true);
			buttonClearsteps.setEnabled(true);
			break;
		case isConnecting:
			buttonScan.setText("Connecting");
			break;
		case isToScan:
			buttonScan.setText("Scan");
			buttonSettime.setEnabled(false);
			buttonCalHRV.setEnabled(false);
			buttonClearsteps.setEnabled(false);
			break;
		case isScanning:
			buttonScan.setText("Scanning");
			break;
		case isDisconnecting:
			buttonScan.setText("isDisconnecting");
			break;
		default:
			break;
		}
	}

	@Override
	public void onSerialReceived(String theString) {							//Once connection data received, this function will be called
		// TODO Auto-generated method stub


		// handle receive data by diff command
		if (command == "Settime") {
			serialReceivedText.setText("Settime : "+theString);
		}
		if (command == "CalHRV") {
			serialReceivedText.setText("CalHRV : --" + theString);
			String str = theString.substring(0,theString.length()-1);
			if (str == "NO")
				serialReceivedText.setText("CalHRV : IBIdata is not enough\n");
			else {
				if (str == "OK") {
					if (IBIdata.size()!=0) {
						serialReceivedText.setText("CalHRV : start cal\n");
						command = "";
						// use SDNN to cal hrv
						int total = 0;
						int pow_total = 0;
						int tmp;
						for (int i = 0; i < IBIdata.size(); i++) {
							tmp = (int) IBIdata.get(i);
							total += tmp;
							pow_total += tmp * tmp;
						}
						double average = (double) total / (double) IBIdata.size();
						average = average * average;
						double hrv = Math.sqrt(((double) pow_total / (double) IBIdata.size()) - average);
						tmp = (int) (hrv * 100);
						String hrv_str = String.valueOf(tmp);
						// send HRV to SmartWatch
						serialSend("r" + hrv_str + ";");
						// use fft to cal lf/hf

						serialReceivedText.setText("CalHRV : end cal\n");
					} else {
						serialReceivedText.setText("CalHRV : OK but no data\n");
					}
				} else {
					// get IBIdata by theString
					IBIdata.add(Integer.valueOf(str));
				}
			}
		}
		if (command == "Clearsteps") {
			serialReceivedText.setText("Clearsteps : "+theString);
		}
		//The Serial data from the BLUNO may be sub-packaged, so using a buffer to hold the String is a good choice.
		//((ScrollView)serialReceivedText.getParent()).fullScroll(View.FOCUS_DOWN);
	}

}