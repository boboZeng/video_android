package com.sport.video.airplay.service.manager;


import android.content.Context;
import android.util.Log;

import com.sport.video.airplay.Config;
import com.sport.video.airplay.control.SubscriptionControl;
import com.sport.video.airplay.entity.ClingDevice;
import com.sport.video.airplay.entity.ClingDeviceList;
import com.sport.video.airplay.entity.IDevice;
import com.sport.video.airplay.util.Utils;

import java.util.Collection;

/**
 * 说明：
 * 作者：zhouzhan
 * 日期：17/7/21 16:33
 */

public class DeviceManager implements IDeviceManager {
    private static final String TAG = DeviceManager.class.getSimpleName();
    /**
     * 已选中的设备, 它也是 ClingDeviceList 中的一员
     */
    private ClingDevice mSelectedDevice;
    private SubscriptionControl mSubscriptionControl;

    public DeviceManager() {
        mSubscriptionControl = new SubscriptionControl();
    }

    @Override
    public IDevice getSelectedDevice() {
        return mSelectedDevice;
    }

    @Override
    public void setSelectedDevice(IDevice selectedDevice) {
//        if (selectedDevice != mSelectedDevice){
//            Intent intent = new Intent(Intents.ACTION_CHANGE_DEVICE);
//            sendBroadcast(intent);
//        }

        Log.i(TAG, "Change selected device.");
        mSelectedDevice = (ClingDevice) selectedDevice;

        // 重置选中状态
        Collection<ClingDevice> clingDeviceList = ClingDeviceList.getInstance().getClingDeviceList();
        if (Utils.isNotNull(clingDeviceList)){
            for (ClingDevice device : clingDeviceList){
                device.setSelected(false);
            }
        }
        // 设置选中状态
        mSelectedDevice.setSelected(true);
        // 清空状态
        Config.getInstance().setHasRelTimePosCallback(false);
    }

    @Override
    public void cleanSelectedDevice() {
        if (Utils.isNull(mSelectedDevice))
            return;

        mSelectedDevice.setSelected(false);
    }

    @Override
    public void registerAVTransport(Context context) {
        if (Utils.isNull(mSelectedDevice))
            return;

        mSubscriptionControl.registerAVTransport(mSelectedDevice, context);
    }

    @Override
    public void registerRenderingControl(Context context) {
        if (Utils.isNull(mSelectedDevice))
            return;

        mSubscriptionControl.registerRenderingControl(mSelectedDevice, context);
    }

    @Override
    public void destroy() {
        if (Utils.isNotNull(mSubscriptionControl)){
            mSubscriptionControl.destroy();
        }
    }
}
