// ReactNativeBlueprintLabelModule.java

package com.blueprinter;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.google.gson.Gson;
import com.qs.helper.printer.Device;
import com.qs.helper.printer.PrintService;
import com.qs.helper.printer.PrinterClass;
import com.qs.helper.printer.bt.BtService;

import com.facebook.react.bridge.Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ReactNativeBlueprintLabelModule extends ReactContextBaseJavaModule {

    private static Context context;
    private final ReactApplicationContext reactContext;


    public ReactNativeBlueprintLabelModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

    }

    @Override
    public String getName() {
        return "ReactNativeBlueprintLabel";
    }

    @ReactMethod
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    private static Handler mhandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if (readBuf[0] == 0x13) {
                        PrintService.isFUll = true;

                    } else if (readBuf[0] == 0x11) {
                        PrintService.isFUll = false;

                    } else if (readBuf[0] == 0x08) {

                    } else if (readBuf[0] == 0x01) {
                        //ShowMsg(getResources().getString(R.string.str_printer_state)+":"+getResources().getString(R.string.str_printer_printing));
                    } else if (readBuf[0] == 0x04) {

                    } else if (readBuf[0] == 0x02) {
//                        ShowMsg(getResourcesrces().getString(R.string.str_printer_state)+":"+getResources().getString(R.string.str_printer_lowpower));
                    } else {
                        if (readMessage.contains("800"))// 80mm paper
                        {
                            PrintService.imageWidth = 72;
//                            Toast.makeText(getApplicationContext(), "80mm",
//                                    Toast.LENGTH_SHORT).show();
                        } else if (readMessage.contains("580"))// 58mm paper
                        {
                            PrintService.imageWidth = 48;
//                            Toast.makeText(getApplicationContext(), "58mm",
//                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case PrinterClass.STATE_CONNECTED:
                            break;
                        case PrinterClass.STATE_CONNECTING:
//                            Toast.makeText(getApplicationContext(),
//                                    "STATE_CONNECTING", Toast.LENGTH_SHORT).show();
                            break;
                        case PrinterClass.STATE_LISTEN:
                        case PrinterClass.STATE_NONE:
                            break;
                        case PrinterClass.SUCCESS_CONNECT:
                            ////PrintService.pl.write(new byte[] { 0x1b, 0x2b });
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            PrintService.pl.write(new byte[]{0x1d, 0x67, 0x33});
//                            Toast.makeText(getApplicationContext(),
//                                    "SUCCESS_CONNECT", Toast.LENGTH_SHORT).show();
                            break;
                        case PrinterClass.FAILED_CONNECT:
//                            Toast.makeText(getApplicationContext(),
//                                    "FAILED_CONNECT", Toast.LENGTH_SHORT).show();

                            break;
                        case PrinterClass.LOSE_CONNECT:
//                            Toast.makeText(getApplicationContext(), "LOSE_CONNECT",
//                                    Toast.LENGTH_SHORT).show();
                    }
                    break;
                case MESSAGE_WRITE:

                    break;
            }
            super.handleMessage(msg);
        }
    };
    private static Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    break;
                case 1:
                    Device d = (Device) msg.obj;
                    if (d != null) {
//                        if (PrintSettingActivity.deviceList == null) {
//                            PrintSettingActivity.deviceList = new ArrayList<Device>();
//                        }
//                        if (!checkData(PrintSettingActivity.deviceList, d)) {
//                            PrintSettingActivity.deviceList.add(d);
//                        }
                    }
                    break;
                case 2:
                    break;
            }
        }
    };

    @ReactMethod
    public void init() {
        PrintService.pl = new BtService(reactContext, mhandler, handler);

    }

    public static void resetPrint() {
        //初始化打印机
        PrintService.pl.write(new byte[]{0x1b, 0x40});
    }

    public static void setBlackMark(int height, int width, int start, int voltage) {
        //设置黑标 高度
        PrintService.pl.write(twoToOne(new byte[]{0x1F, 0x1B, 0x1F, (byte) 0x81, 0x04, 0x05, 0x06}, toLH(height)));

        // 宽度
        PrintService.pl.write(twoToOne(new byte[]{0x1F, 0x1B, 0x1F, (byte) 0x82, 0x04, 0x05, 0x06}, toLH(width)));
        //起始
        PrintService.pl.write(twoToOne(new byte[]{0x1D, 0x54, 0x1D, 0x28, 0x46, 0x04, 0x00, 0x01, 0x00},
                toLH(start)));
        //  电压
        PrintService.pl.write(twoToOne(new byte[]{0x1B, 0x23, 0x23, 0X53, 0X42, 0X43, 0x56}, toLH(voltage))
        );

        System.out.println(PrintService.pl.write(new byte[]{0x1d, 0x67, 0x34}));
        System.out.println(PrintService.pl.write(new byte[]{0x1d, 0x67, 0x35}));
    }

    /**
     * 小端模式 将int转为低字节在前，高字节在后的byte数组
     *
     * @param n int
     * @return byte[]
     */
    public static byte[] toLH(int n) {
        byte[] b = new byte[2];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
//		b[2] = (byte) (n >> 16 & 0xff);
//		b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }

    /**
     * 将int转为高字节在前，低字节在后的byte数组
     *
     * @param n int
     * @return byte[]
     */
    public static byte[] toHH(int n) {
        byte[] b = new byte[2];
        b[0] = (byte) (n >> 8 & 0xff);
        b[1] = (byte) (n & 0xff);
        return b;
    }


    /**
     * 合并数组
     *
     * @param data1
     * @param data2
     * @return
     */
    public static byte[] twoToOne(byte[] data1, byte[] data2) {

        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;

    }

    private static List<Device> deviceList = new ArrayList<Device>();
    private List<String> btList = new ArrayList<>();
    WritableArray dataBt = new WritableNativeArray();

    //    private static ArrayAdapter<String> mNewDevicesArrayAdapter = null;
//扫描附近设备
    @ReactMethod
    public void getDevices(Promise promise) {
        WritableArray dataBt = new WritableNativeArray();
        if (deviceList != null) {
            deviceList.clear();
        }
        if (!PrintService.pl.IsOpen()) {
            PrintService.pl.open(context);
        }
//        mNewDevicesArrayAdapter.clear();
        PrintService.pl.scan();
        deviceList = PrintService.pl.getDeviceList();
        Gson gson = new Gson();
        String jsonString = gson.toJson(deviceList);
        Integer key = 0;

        for(Iterator<Device> it = deviceList.iterator(); it.hasNext();){
            Device btd = it.next();
            btList.add(btd.deviceAddress);

            dataBt.pushString(String.valueOf(btd.deviceAddress + "=" + btd.deviceName));
            key++;
        }
        promise.resolve(dataBt);
    }

    @ReactMethod
    public void connectDevice(String address) {
        PrintService.pl.connect(address);
    }

    //获取连接状态
    //0：未连接
    //1：监听中
    //2：连接中
    //3：已连接
    //4：丢失连接
    //5：连接失败
    //6：连接成功
    //7：扫描中
    //8：扫描结束
    @ReactMethod
    public int getStatus() {
        return PrintService.pl.getState();
    }

    @ReactMethod
    public static void disConnect() {
        PrintService.pl.disconnect();
    }

    @ReactMethod
    public void openBlackMark() {
        //设置黑标电压
//        PrintService.pl.write(new byte[]{0x1b, 0x23, 0x23, 0x52, 0x42, 0x43, 0x56, (byte) 0xf4, 0x01});
        //打开增强模式
//        PrintService.pl.write(new byte[]{0x1b,0x23,0x23,0x46,0x42,0x45,0x48,0x31});

        PrintService.pl.write(new byte[]{0x1F, 0x1B, 0x1F, (byte) 0x80,
                0x04, 0x05, 0x06, 0x44});
        System.out.println(PrintService.pl.write(new byte[]{0x1d, 0x67, 0x34}));
        System.out.println(PrintService.pl.write(new byte[]{0x1d, 0x67, 0x35}));

    }

    @ReactMethod
    public void closeBlackMark() {
        PrintService.pl.write(new byte[]{0x1F, 0x1B, 0x1F, (byte) 0x80,
                0x04, 0x05, 0x06, 0x66});
    }

    @ReactMethod
    public void printString(final ReadableArray printBuffer) {
        try {
            try{
                JSONArray aaa = convertArrayToJson(printBuffer);

                for (int i = 0; i < aaa.length(); i++) {

                    byte[] send = (aaa.getString(i) + '\n').getBytes("GBK");
                    PrintService.pl.write(send);
                }
            }catch (JSONException e){
                e.printStackTrace();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //PrintService.pl.printText("\n");
        PrintService.pl.write(new byte[]{0x1d, 0x0c});
    }



    private static WritableMap convertJsonToMap(JSONObject jsonObject) throws JSONException {
        WritableMap map = new WritableNativeMap();

        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                map.putMap(key, convertJsonToMap((JSONObject) value));
            } else if (value instanceof Boolean) {
                map.putBoolean(key, (Boolean) value);
            } else if (value instanceof Integer) {
                map.putInt(key, (Integer) value);
            } else if (value instanceof Double) {
                map.putDouble(key, (Double) value);
            } else if (value instanceof String) {
                map.putString(key, (String) value);
            } else {
                map.putString(key, value.toString());
            }
        }
        return map;
    }

    private static WritableArray convertJsonToArray(JSONArray jsonArray) throws JSONException {
        WritableArray array = new WritableNativeArray();

        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            if (value instanceof JSONObject) {
                array.pushMap(convertJsonToMap((JSONObject) value));
            } else if (value instanceof JSONArray) {
                array.pushArray(convertJsonToArray((JSONArray) value));
            } else if (value instanceof Boolean) {
                array.pushBoolean((Boolean) value);
            } else if (value instanceof Integer) {
                array.pushInt((Integer) value);
            } else if (value instanceof Double) {
                array.pushDouble((Double) value);
            } else if (value instanceof String) {
                array.pushString((String) value);
            } else {
                array.pushString(value.toString());
            }
        }
        return array;
    }

    private static JSONArray convertArrayToJson(ReadableArray readableArray) throws JSONException {
        JSONArray array = new JSONArray();
        for (int i = 0; i < readableArray.size(); i++) {
            switch (readableArray.getType(i)) {
                case Null:
                    break;
                case Boolean:
                    array.put(readableArray.getBoolean(i));
                    break;
                case Number:
                    array.put(readableArray.getDouble(i));
                    break;
                case String:
                    array.put(readableArray.getString(i));
                    break;
                case Map:
                    array.put(convertMapToJson(readableArray.getMap(i)));
                    break;
                case Array:
                    array.put(convertArrayToJson(readableArray.getArray(i)));
                    break;
            }
        }
        return array;
    }
    private static JSONObject convertMapToJson(ReadableMap readableMap) throws JSONException {
        JSONObject object = new JSONObject();
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            switch (readableMap.getType(key)) {
                case Null:
                    object.put(key, JSONObject.NULL);
                    break;
                case Boolean:
                    object.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    object.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    object.put(key, readableMap.getString(key));
                    break;
                case Map:
                    object.put(key, convertMapToJson(readableMap.getMap(key)));
                    break;
                case Array:
                    object.put(key, convertArrayToJson(readableMap.getArray(key)));
                    break;
            }
        }
        return object;
    }


}
