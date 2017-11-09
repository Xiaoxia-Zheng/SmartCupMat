package com.example.xiaoxiazheng.smartcupmat;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {
	private final static String TAG = MainActivity.class.getSimpleName();

	private RBLService mBluetoothLeService;
	private BluetoothAdapter mBluetoothAdapter;
	public static List<BluetoothDevice> mDevice = new ArrayList<BluetoothDevice>();
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

	Button scanAllBtn = null;
    Button submitData = null;
	TextView lastUuid = null;
	TextView temperatureTextView = null;
    EditText setTemprt;

	private static final int REQUEST_ENABLE_BT = 1;
	private static final long SCAN_PERIOD = 3000;
	public static final int REQUEST_CODE = 30;
	private String mDeviceAddress;
	private String mDeviceName;
	private boolean flag = true;
	private boolean connState = false;
    private int setTemValue;
    private String temprt;




    private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBluetoothLeService = ((com.example.xiaoxiazheng.smartcupmat.RBLService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (RBLService.ACTION_GATT_CONNECTED.equals(action)) {
				flag = true;
				connState = true;

				Toast.makeText(getApplicationContext(), "Connected",
						Toast.LENGTH_LONG).show();

				scanAllBtn.setText("D");
			}else if (RBLService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                getGattService(mBluetoothLeService.getSupportedGattService());
            } else if (RBLService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getByteArrayExtra(RBLService.EXTRA_DATA));
            }
		}
	};



    private void displayData(byte[] byteArray) {
        if (byteArray != null) {
            int digitalData;
            double voltage, temperature;

            digitalData =  byteArray[0] << 24 |
                    (byteArray[1] & 0xFF) << 16 |
                    (byteArray[2] & 0xFF) << 8 |
                    (byteArray[3] & 0xFF);

            voltage = (digitalData/1023.0) * 3.3;
            temperature = (voltage - 0.5) / 0.01;

            int tempdata = (int)(temperature);

            temprt =  Integer.toString(tempdata);

            temperatureTextView.setText(temprt + ' ' + '℃');

            if (setTemValue == tempdata){

                notificationStart();

            }
        }
    }

    private void getGattService(BluetoothGattService gattService) {
        if (gattService == null)
            return;

        BluetoothGattCharacteristic characteristicRx = gattService
                .getCharacteristic(RBLService.UUID_BLE_SHIELD_RX);
        mBluetoothLeService.setCharacteristicNotification(characteristicRx,
                true);
        mBluetoothLeService.readCharacteristic(characteristicRx);
    }


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);


		lastUuid = (TextView) findViewById(R.id.lastDevice);

		temperatureTextView = (TextView) findViewById(R.id.temData);
        setTemprt = (EditText) findViewById(R.id.setTemprt);

		scanAllBtn = (Button) findViewById(R.id.buttonScan);
        scanAllBtn.setOnClickListener(this);

        submitData = (Button) findViewById(R.id.submitValue);
        submitData.setOnClickListener(this);


		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, "Ble not supported", Toast.LENGTH_SHORT)
					.show();
			finish();
		}

		final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Ble not supported", Toast.LENGTH_SHORT)
					.show();
			finish();
			return;
		}

		Intent gattServiceIntent = new Intent(MainActivity.this, com.example.xiaoxiazheng.smartcupmat.RBLService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(RBLService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(RBLService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(RBLService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(RBLService.ACTION_DATA_AVAILABLE);

        return intentFilter;
	}

	private void scanLeDevice() {
		new Thread() {

			@Override
			public void run() {
				mBluetoothAdapter.startLeScan(mLeScanCallback);

				try {
					Thread.sleep(SCAN_PERIOD);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				mBluetoothAdapter.stopLeScan(mLeScanCallback);
			}
		}.start();
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi,
				byte[] scanRecord) {

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (device != null) {
						if (mDevice.indexOf(device) == -1)
							mDevice.add(device);
					}
				}
			});
		}
	};

	@Override
	protected void onStop() {
		super.onStop();

		flag = false;

		unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mServiceConnection != null)
			unbindService(mServiceConnection);

		System.exit(0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		} else if (requestCode == REQUEST_CODE
				&& resultCode == Device.RESULT_CODE) {
			mDeviceAddress = data.getStringExtra(Device.EXTRA_DEVICE_ADDRESS);
			mDeviceName = data.getStringExtra(Device.EXTRA_DEVICE_NAME);
			mBluetoothLeService.connect(mDeviceAddress);
		}

		super.onActivityResult(requestCode, resultCode, data);
	}


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonScan:
                if (connState == false) {
                    scanLeDevice();

                    try {
                        Thread.sleep(SCAN_PERIOD);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(getApplicationContext(),
                            Device.class);
                    startActivityForResult(intent, REQUEST_CODE);
                } else {
                    mBluetoothLeService.disconnect();
                    mBluetoothLeService.close();
                    scanAllBtn.setText("");
                    connState = false;
                }
                break;

            case R.id.submitValue:
                setTemValue = Integer.parseInt(setTemprt.getText().toString());
				Toast.makeText(getApplicationContext(), "Submitted",
						Toast.LENGTH_LONG).show();
				break;

            default:
                break;

        }

    }

    public void notificationStart(){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_cup)
                .setContentTitle("Notification")
                .setContentText("Time to tea!");

        Intent resultIntent = new Intent(this, MainActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        long[] pattern = {500,500,500,500,500,500,500,500,500};
        mBuilder.setVibrate(pattern);
        Uri alarmSound = RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);

        int mNotificationID = 001;

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(mNotificationID, mBuilder.build());
    }

}
