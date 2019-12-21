package com.sport.video.airplay.ui;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.sport.video.airplay.Intents;
import com.sport.video.airplay.util.NetWorkBroadcastManager;
import com.sport.video.airplay.control.ClingPlayControl;
import com.sport.video.airplay.control.callback.ControlCallback;
import com.sport.video.airplay.control.callback.ControlReceiveCallback;
import com.sport.video.airplay.entity.ClingDevice;
import com.sport.video.airplay.entity.ClingDeviceList;
import com.sport.video.airplay.entity.DLANPlayState;
import com.sport.video.airplay.entity.IDevice;
import com.sport.video.airplay.entity.IResponse;
import com.sport.video.airplay.listener.BrowseRegistryListener;
import com.sport.video.airplay.listener.DeviceListChangedListener;
import com.sport.video.airplay.service.ClingUpnpService;
import com.sport.video.airplay.service.manager.ClingManager;
import com.sport.video.airplay.service.manager.DeviceManager;
import com.sport.video.airplay.util.Utils;
import com.sport.video.airplay.R;

import org.fourthline.cling.model.meta.Device;

import java.util.Collection;

public class AirPlayActivity extends AppCompatActivity implements
        SeekBar.OnSeekBarChangeListener,
        View.OnClickListener {

    private static final String TAG = AirPlayActivity.class.getSimpleName();
    /**
     * 连接设备状态: 播放状态
     */
    public static final int PLAY_ACTION = 0xa1;
    /**
     * 连接设备状态: 暂停状态
     */
    public static final int PAUSE_ACTION = 0xa2;
    /**
     * 连接设备状态: 停止状态
     */
    public static final int STOP_ACTION = 0xa3;
    /**
     * 连接设备状态: 转菊花状态
     */
    public static final int TRANSITIONING_ACTION = 0xa4;
    /**
     * 获取进度
     */
    public static final int GET_POSITION_INFO_ACTION = 0xa5;
    /**
     * 投放失败
     */
    public static final int ERROR_ACTION = 0xa5;

    private Context mContext;
    private Handler mHandler = new InnerHandler();

    private ListView mDeviceList;
    private ImageView img_refresh, img_close;
    private TextView tv_wifi;
//    private SeekBar mSeekProgress;
//    private SeekBar mSeekVolume;
//    private Switch mSwitchMute;

    private BroadcastReceiver mTransportStateBroadcastReceiver;
    private DevicesAdapter mDevicesAdapter;
    /**
     * 投屏控制器
     */
    private ClingPlayControl mClingPlayControl = new ClingPlayControl();
    private NetWorkBroadcastManager netWorkBroadcastManager;

    /**
     * 用于监听发现设备
     */
    private BrowseRegistryListener mBrowseRegistryListener = new BrowseRegistryListener();

    private ServiceConnection mUpnpServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e(TAG, "mUpnpServiceConnection onServiceConnected");

            ClingUpnpService.LocalBinder binder = (ClingUpnpService.LocalBinder) service;
            ClingUpnpService beyondUpnpService = binder.getService();

            ClingManager clingUpnpServiceManager = ClingManager.getInstance();
            clingUpnpServiceManager.setUpnpService(beyondUpnpService);
            clingUpnpServiceManager.setDeviceManager(new DeviceManager());

            clingUpnpServiceManager.getRegistry().addListener(mBrowseRegistryListener);
            //Search on service created.
            clingUpnpServiceManager.searchDevices();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "mUpnpServiceConnection onServiceDisconnected");

            ClingManager.getInstance().setUpnpService(null);
        }
    };

    public static void start(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Intent intent = new Intent(context, AirPlayActivity.class);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.airplay_activity_main_devices);
        mContext = this;
        path = getIntent().getStringExtra("url");
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(this, "投屏地址出错", Toast.LENGTH_SHORT).show();
            onBackPressed();
            return;
        }
        netWorkBroadcastManager = new NetWorkBroadcastManager(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initTextWifi();
                    }
                });

            }
        });
        initView();
        initListeners();
        bindServices();
        registerReceivers();
    }

    private void registerReceivers() {
        netWorkBroadcastManager.registerReceiver(this);
        //Register play status broadcast
        mTransportStateBroadcastReceiver = new TransportStateBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intents.ACTION_PLAYING);
        filter.addAction(Intents.ACTION_PAUSED_PLAYBACK);
        filter.addAction(Intents.ACTION_STOPPED);
        filter.addAction(Intents.ACTION_TRANSITIONING);
        registerReceiver(mTransportStateBroadcastReceiver, filter);
    }


    private void bindServices() {
        // Bind UPnP service
        Intent upnpServiceIntent = new Intent(AirPlayActivity.this, ClingUpnpService.class);
        bindService(upnpServiceIntent, mUpnpServiceConnection, Context.BIND_AUTO_CREATE);
        // Bind System service
        //        Intent systemServiceIntent = new Intent(AirPlayActivity.this, SystemService.class);
        //        bindService(systemServiceIntent, mSystemServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mHandler.removeCallbacksAndMessages(null);
            // Unbind UPnP service
            unbindService(mUpnpServiceConnection);
            // Unbind System service
            //        unbindService(mSystemServiceConnection);
            // UnRegister Receiver
            unregisterReceiver(mTransportStateBroadcastReceiver);
            netWorkBroadcastManager.unregisterReceiver(this);

            ClingManager.getInstance().destroy();
            ClingDeviceList.getInstance().destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ObjectAnimator animator;

    private void initView() {
        mDeviceList = (ListView) findViewById(R.id.lv_devices);
        img_refresh = findViewById(R.id.img_refresh);
        img_refresh.setOnClickListener(this);
        animator = Utils.getRotationAnimator(img_refresh, 1500, -1);
        img_close = findViewById(R.id.img_close);
        img_close.setOnClickListener(this);
        tv_wifi = findViewById(R.id.tv_wifi);
        initTextWifi();

//        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_refresh);
//        mTVSelected = (TextView) findViewById(R.id.tv_selected);
//        mSeekProgress = (SeekBar) findViewById(R.id.seekbar_progress);
//        mSeekVolume = (SeekBar) findViewById(R.id.seekbar_volume);
//        mSwitchMute = (Switch) findViewById(R.id.sw_mute);

        mDevicesAdapter = new DevicesAdapter(mContext);
        mDeviceList.setAdapter(mDevicesAdapter);

        /** 这里为了模拟 seek 效果(假设视频时间为 15s)，拖住 seekbar 同步视频时间，
         * 在实际中 使用的是片源的时间 */
//        mSeekProgress.setMax(15);

        // 最大音量就是 100，不要问我为什么
//        mSeekVolume.setMax(100);
    }

    private void initTextWifi() {
        if (tv_wifi == null || isFinishing()) {
            return;
        }
        if (Utils.isWifiConnected(this)) {
            tv_wifi.setText("当前Wi-Fi:" + Utils.wifiName(this));
            tv_wifi.setOnClickListener(null);
        } else {
            String text = "未连接Wi-Fi，请检查网络设置 >";
            tv_wifi.setText(Utils.foregroundColorSpan(text
                    , Color.parseColor("#0bcd6e"), 9, text.length()));
            tv_wifi.setOnClickListener(this);
        }
    }

    /**
     * 播放选中设备
     *
     * @param item
     */
    private boolean playDevice(ClingDevice item) {
        if (Utils.isNull(item)) {
            return false;
        }
        ClingManager.getInstance().setSelectedDevice(item);
        Device device = item.getDevice();
        if (Utils.isNull(device)) {
            return false;
        }
        Utils.saveAirPlayDevice(AirPlayActivity.this, getDeviceName(device));
        play();
        return true;
    }

    private void initListeners() {
        mDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 选择连接设备
                ClingDevice item = mDevicesAdapter.getItem(position);
                mDevicesAdapter.setSelectPosition(position);
                playDevice(item);
            }
        });

        // 设置发现设备监听
        mBrowseRegistryListener.setOnDeviceListChangedListener(new DeviceListChangedListener() {
            @Override
            public void onDeviceAdded(final IDevice device) {
                System.out.println("airplay onDeviceAdded");
                runOnUiThread(new Runnable() {
                    public void run() {
                        ClingDevice item = (ClingDevice) device;
                        mDevicesAdapter.add(item);
                        if (ClingManager.getInstance().getSelectedDevice() == null) {
                            String deviceName = getDeviceName(item.getDevice());
                            String name = Utils.readAirPlayDevice(AirPlayActivity.this);
                            if (!TextUtils.isEmpty(deviceName)
                                    && !TextUtils.isEmpty(name)
                                    && deviceName.equals(name)) {
                                mDevicesAdapter.setSelectPosition(mDevicesAdapter.getCount() - 1);
                                playDevice(item);
                            }
                        }
                    }
                });
            }

            @Override
            public void onDeviceRemoved(final IDevice device) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        mDevicesAdapter.remove((ClingDevice) device);
                    }
                });
            }
        });

//        // 静音开关
//        mSwitchMute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                mClingPlayControl.setMute(isChecked, new ControlCallback() {
//                    @Override
//                    public void success(IResponse response) {
//                        Log.e(TAG, "setMute success");
//                    }
//
//                    @Override
//                    public void fail(IResponse response) {
//                        Log.e(TAG, "setMute fail");
//                    }
//                });
//            }
//        });
//
//        mSeekProgress.setOnSeekBarChangeListener(this);
//        mSeekVolume.setOnSeekBarChangeListener(this);
    }

    private String getDeviceName(Device device) {
        if (device == null
                || device.getIdentity() == null
                || device.getIdentity().getUdn() == null) {
            return "";
        }

        String identifierString = device.getIdentity().getUdn().getIdentifierString();
        if (TextUtils.isEmpty(identifierString)) {
            return "";
        }

        return identifierString;
    }

    //第一次进来自动投屏
    private boolean isFirst = true;

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirst) {
            isFirst = false;
            int position = getSelectPosition();
            if (position >= 0
                    && position < mDevicesAdapter.getCount()) {
                ClingDevice item = mDevicesAdapter.getItem(position);
                playDevice(item);
            }
        }
    }

    /**
     * 选中播放的设备在搜索列表位置
     *
     * @return
     */
    private int getSelectPosition() {
        String deviceName = Utils.readAirPlayDevice(this);
        if (!TextUtils.isEmpty(deviceName)
                && mDevicesAdapter.getCount() > 0) {
            for (int i = 0; i < mDevicesAdapter.getCount(); i++) {
                ClingDevice item = mDevicesAdapter.getItem(i);
                Device device = item.getDevice();
                String name = getDeviceName(device);
                if (!TextUtils.isEmpty(name)
                        && name.equals(deviceName)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 刷新设备
     */
    private void refreshDeviceList() {
        Collection<ClingDevice> devices = ClingManager.getInstance().getDmrDevices();
        ClingDeviceList.getInstance().setClingDeviceList(devices);
        if (devices != null) {
            mDevicesAdapter.clear();
            mDevicesAdapter.addAll(devices);
            int position = getSelectPosition();
            mDevicesAdapter.setSelectPosition(position);
        }
        animator.setRepeatCount(0);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.img_refresh) {
            animator.setRepeatCount(-1);
            animator.start();
            refreshDeviceList();

        } else if (id == R.id.img_close) {
            onBackPressed();
        } else if (id == R.id.tv_wifi) {
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }
//       else if (id == R.id.bt_play) {
//            play();
//        } else if (id == R.id.bt_pause) {
//            pause();
//        } else if (id == R.id.bt_stop) {
//            stop();
//        }
    }

    /**
     * 停止
     */
    private void stop() {
        mClingPlayControl.stop(new ControlCallback() {
            @Override
            public void success(IResponse response) {
                Log.e(TAG, "stop success");
            }

            @Override
            public void fail(IResponse response) {
                Log.e(TAG, "stop fail");
            }
        });
    }

    /**
     * 暂停
     */
    private void pause() {
        mClingPlayControl.pause(new ControlCallback() {
            @Override
            public void success(IResponse response) {
                Log.e(TAG, "pause success");
            }

            @Override
            public void fail(IResponse response) {
                Log.e(TAG, "pause fail");
            }
        });
    }

    public void getPositionInfo() {
        mClingPlayControl.getPositionInfo(new ControlReceiveCallback() {
            @Override
            public void receive(IResponse response) {

            }

            @Override
            public void success(IResponse response) {

            }

            @Override
            public void fail(IResponse response) {

            }
        });
    }

    /**
     * 播放视频
     */
    private void play() {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        @DLANPlayState.DLANPlayStates int currentState = mClingPlayControl.getCurrentState();
        Utils.d("AirPlayActivity play currentState:" + currentState);
        /**
         * 通过判断状态 来决定 是继续播放 还是重新播放
         */

        if (currentState == DLANPlayState.STOP) {
            mClingPlayControl.playNew(path, new ControlCallback() {

                @Override
                public void success(IResponse response) {
                    Log.e(TAG, "playNew success");
                    //                    ClingUpnpServiceManager.getInstance().subscribeMediaRender();
                    //                    getPositionInfo();
                    // TODO: 17/7/21 play success
                    ClingManager.getInstance().registerAVTransport(mContext);
                    ClingManager.getInstance().registerRenderingControl(mContext);
                }

                @Override
                public void fail(IResponse response) {
                    Log.e(TAG, "playNew fail");
                    mHandler.sendEmptyMessage(ERROR_ACTION);
                }
            });
        } else {
            mClingPlayControl.play(new ControlCallback() {
                @Override
                public void success(IResponse response) {
                    Log.e(TAG, "play success");
                }

                @Override
                public void fail(IResponse response) {
                    Log.e(TAG, "play fail");
                    mHandler.sendEmptyMessage(ERROR_ACTION);
                }
            });
        }
    }

    /******************* start progress changed listener *************************/

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.e(TAG, "Start Seek");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.e(TAG, "Stop Seek");
        int id = seekBar.getId();
//        if (id == R.id.seekbar_progress) { // 进度
//
//            int currentProgress = seekBar.getProgress() * 1000; // 转为毫秒
//            mClingPlayControl.seek(currentProgress, new ControlCallback() {
//                @Override
//                public void success(IResponse response) {
//                    Log.e(TAG, "seek success");
//                }
//
//                @Override
//                public void fail(IResponse response) {
//                    Log.e(TAG, "seek fail");
//                }
//            });
//        } else if (id == R.id.seekbar_volume) {   // 音量
//
//            int currentVolume = seekBar.getProgress();
//            mClingPlayControl.setVolume(currentVolume, new ControlCallback() {
//                @Override
//                public void success(IResponse response) {
//                    Log.e(TAG, "volume success");
//                }
//
//                @Override
//                public void fail(IResponse response) {
//                    Log.e(TAG, "volume fail");
//                }
//            });
//        }
    }

    /******************* end progress changed listener *************************/

    private final class InnerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PLAY_ACTION:
                    Log.i(TAG, "Execute PLAY_ACTION");
                    Toast.makeText(mContext, "正在投放", Toast.LENGTH_SHORT).show();
                    mClingPlayControl.setCurrentState(DLANPlayState.PLAY);

                    break;
                case PAUSE_ACTION:
                    Log.i(TAG, "Execute PAUSE_ACTION");
                    mClingPlayControl.setCurrentState(DLANPlayState.PAUSE);

                    break;
                case STOP_ACTION:
                    Log.i(TAG, "Execute STOP_ACTION");
                    mClingPlayControl.setCurrentState(DLANPlayState.STOP);

                    break;
                case TRANSITIONING_ACTION:
                    Log.i(TAG, "Execute TRANSITIONING_ACTION");
                    Toast.makeText(mContext, "正在连接", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_ACTION:
                    Log.e(TAG, "Execute ERROR_ACTION");
                    Toast.makeText(mContext, "投放失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    /**
     * 接收状态改变信息
     */
    private class TransportStateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "Receive playback intent:" + action);
            if (Intents.ACTION_PLAYING.equals(action)) {
                mHandler.sendEmptyMessage(PLAY_ACTION);

            } else if (Intents.ACTION_PAUSED_PLAYBACK.equals(action)) {
                mHandler.sendEmptyMessage(PAUSE_ACTION);

            } else if (Intents.ACTION_STOPPED.equals(action)) {
                mHandler.sendEmptyMessage(STOP_ACTION);

            } else if (Intents.ACTION_TRANSITIONING.equals(action)) {
                mHandler.sendEmptyMessage(TRANSITIONING_ACTION);
            }
        }
    }
}