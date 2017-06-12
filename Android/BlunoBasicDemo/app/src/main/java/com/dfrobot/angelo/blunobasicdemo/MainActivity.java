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
	private int IBIdata_length;
	private int[] IBIdata;
	private boolean IBIdataReady = false;
	private boolean currIBIdataReady = false;
	private boolean HRVLFHFReady = false;
	private double HRV = 0.0;
	private double LFHF = 0.0;
	//private Vector IBIdata = new Vector(256);


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
				// "g;" can get all IBI data, if not enough will get "NO", if get enough will get "OK"
				IBIdata_length = 0;
				//IBIdataReady = false;
				//HRVLFHFReady = false;
				buttonCalHRV.setText("Get IBIdata from SmartWatch");
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
		IBIdata = new int[530];
		IBIdata_length = 0;
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
			serialReceivedText.setText("");
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
			theString = theString.substring(0,theString.length()-2);
			serialReceivedText.setText("CalHRV : [" + theString +"]\n");
			if (theString.compareTo("NO") == 0) {
				serialReceivedText.append("CalHRV : IBIdata is not enough\n");
				buttonCalHRV.setText("Cal HRV and LF/HF");
			}
			else {
				if (theString.compareTo("OK") == 0) {
					buttonCalHRV.setText("Waiting for calculate HRV LF/HF");
					IBIdataReady = true;
					if (IBIdata_length!=0) {
						serialReceivedText.append("CalHRV : start cal\n");
						command = "";
						// use SDNN to cal hrv
						int total = 0;
						int pow_total = 0;
						int tmp;
						for (int i = 0; i < IBIdata_length; i++) {
							tmp = IBIdata[i];
							total += tmp;
							pow_total += tmp * tmp;
						}
						double average = (double) total / (double) IBIdata_length;
						average = average * average;
						HRV = Math.sqrt(((double) pow_total / (double) IBIdata_length) - average);

						// use fft with IBIdata
						// real number
						double[] yr = new double[IBIdata_length+1];
						// imaginary number
						double[] yi = new double[IBIdata_length+1];
						for (int i=0;i<IBIdata_length;i++) {
							yr[i] = 0.0;
							yi[i] = 0.0;
							for (int j=0;j<IBIdata_length;j++) {
								yr[i] += ((double)IBIdata[j])*Math.cos(2*Math.PI*i*j/((double)IBIdata_length));
								yi[i] += (-(double)IBIdata[j])*Math.sin(2*Math.PI*i*j/((double)IBIdata_length));
							}
						}
						// cal LF HF by yr yi
						double hzstep = (1.0) / IBIdata_length;
						//  0.04 hz < LF < 0.15 hz
						double LF = 0.0;
						// 0.15 hz < HF < 0.4 hz
						double HF = 0.0;
						double currhz = 0.0;
						for (int i=0;i<IBIdata_length/2;i++) {
							currhz = i*hzstep;
							if (0.04 <= currhz && currhz <= 0.15) {
								LF += Math.sqrt(yr[i]*yr[i] + yi[i]*yi[i]);
							}
							if (0.15 < currhz && currhz <= 0.4) {
								HF += Math.sqrt(yr[i]*yr[i] + yi[i]*yi[i]);
							}
							if (currhz > 0.4)
								break;
						}
						LFHF = LF / HF;

						// send HRV and LF/HF
						serialSend("r" + String.valueOf((int)(HRV*100)) + ";");
						serialSend("l" + String.valueOf((int)(LFHF*100)) + ";");
						serialReceivedText.append("CalHRV : end cal\nHRV : " + String.valueOf(HRV) + "\nLF/HF : " + String.valueOf(LFHF));
						HRVLFHFReady = true;
					} else {
						//serialReceivedText.setText("CalHRV : OK but no data\n");
						HRV = 0.0;
						LFHF = 0.0;
						HRVLFHFReady = true;
					}
					buttonCalHRV.setText("Cal HRV and LF/HF");
				} else {
					// get IBIdata by theString
					try {
						IBIdata[IBIdata_length] = Integer.parseInt(theString);
						IBIdata_length++;
						serialReceivedText.append("success!\n");
					} catch (NumberFormatException e) {
						IBIdata[IBIdata_length] = 0;
						IBIdata_length++;
						serialReceivedText.append("fail!\n");
					}

					//currIBIdataReady = true;
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