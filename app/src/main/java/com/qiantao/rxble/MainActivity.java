package com.qiantao.rxble;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.qiantao.rxble.ble.RxBle;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;

    @BindView(R.id.tv_receive)
    TextView sendTv;

    @BindView(R.id.et_send)
    EditText sendEt;

    private String mMsgSend;

    private static final String TAG = MainActivity.class.getSimpleName();

    private RxBle mRxBle;

    private StringBuffer mStringBuffer;

    BluetoothDevice mBleDevice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        verifyIfRequestPermission();
        mStringBuffer = new StringBuffer();
        mRxBle = RxBle.getInstance().setTargetDevice("test01");
        mRxBle.openBle(this);
        mRxBle.scanBleDevices(true);
        mRxBle.receiveData().subscribe(new Action1<String>() {
            @Override
            public void call(String receiveData) {

                System.out.println("receivedata:"+receiveData);
                mRxBle.sendData(receiveData, 0);

                sendTv.setText(mStringBuffer.append(receiveData));
            }
        });
        mRxBle.setScanListener(new RxBle.BleScanListener() {
            @Override
            public void onBleScan(BluetoothDevice bleDevice, int rssi, byte[] scanRecord) {
                // Get list of devices and other information
                System.out.println("name:"+bleDevice.getName()+";");
                if (bleDevice.getName().contains("test")){
                    System.out.println("conndd");
                    mRxBle.connectDevice(bleDevice);
                }
            }
        });

    }

    @OnClick(R.id.btn_send)
    public void sendMessage(View view) {
        if (!TextUtils.isEmpty(mMsgSend)) {
            Log.d(TAG, "sendMessage: " + mMsgSend);
            mRxBle.sendData(mMsgSend, 0);
        }
    }

    @OnTextChanged(R.id.et_send)
    public void onTextChanged(CharSequence text) {
        mMsgSend = text.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRxBle.closeBle();
    }
    private void verifyIfRequestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            Log.i(TAG, "onCreate: checkSelfPermission");
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onCreate: Android 6.0 动态申请权限");

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)) {
                    Log.i(TAG, "*********onCreate: shouldShowRequestPermissionRationale**********");
                    Toast.makeText(this, "只有允许访问位置才能搜索到蓝牙设备", Toast.LENGTH_SHORT).show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_ACCESS_COARSE_LOCATION);
                }
            } else {
//                showDialog(getResources().getString(R.string.scanning));
//                mBleService.scanLeDevice(true);

            }
        } else {
//            showDialog(getResources().getString(R.string.scanning));
//            mBleService.scanLeDevice(true);

        }
    }

}