// ReactNativeBlueprintLabelModule.java

package com.blueprinter;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.IBinder;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.qs.helper.printer.BarcodeCreater;
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
    //Get Devices
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

    //Status
    //0：Not Connected
    //1：Listening
    //2：Connecting
    //3：Connected
    //4：Disconnected
    //5：Fail Connection
    //6：Connection Success
    //7：Scanning
    //8：End of scanning
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
    public void printParagraph(final ReadableArray printBuffer) {
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

    @ReactMethod
    public void printText(String text) {
        try {
            byte[] send = (text).getBytes("GBK");
            PrintService.pl.write(send);


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        PrintService.pl.printText("\n");
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

    public static void printAsOrder( String code, String fbaCode, String channel, String country, int count, int total) throws WriterException {
        byte[] btdata = null;
        try {
            btdata = code.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //设置纵向移动单位
        PrintService.pl.write(new byte[]{0x1d, 0x50, 0x10});
        //设置行间距
        PrintService.pl.write(new byte[]{0x1b, 0x32});
        //开启条码打印
        PrintService.pl.write(new byte[]{0x1d, 0x45, 0x43, 0x01});


        //Set the barcode height is 162
//					MainActivity.pl.write(new byte[]{0x1d,0x68,(byte) 0xa2});
        PrintService.pl.write(new byte[]{0x1d, 0x68, (byte) 0x82});


        //Set HRI character print location on bottom
        PrintService.pl.write(new byte[]{0x1d, 0x48, 0x00});


        PrintService.pl.write(new byte[]{0x1d, 0x77, 0x02});

        //Print the barcode use code128

        byte[] qrHead = new byte[]{0x1d, 0x6b, 0x49, (byte) btdata.length};
//					byte[] qrHead=new byte[]{0x1d,0x6b,0x44,(byte) btdata.length};

        byte[] barCodeData = new byte[qrHead.length + btdata.length];
        System.arraycopy(qrHead, 0, barCodeData, 0, qrHead.length);
        System.arraycopy(btdata, 0, barCodeData, qrHead.length, btdata.length);
        //居中指令
        PrintService.pl.write(new byte[]{0x1b, 0x61, 0x01});
        //打印条码
        PrintService.pl.write(barCodeData);
        PrintService.pl.printText("\n");
        //关闭条码打印
        PrintService.pl.write(new byte[]{0x1d, 0x45, 0x43, 0x00});
        //设置加粗
        PrintService.pl.write(new byte[]{0x1b, 0x45, 0x01});
        //打印条码内容
        PrintService.pl.printText(code);
        PrintService.pl.write(new byte[]{0x1b, 0x4a, 0x1a});
//        PrintService.pl.printText("\n");
        //判断是否fba订单
        if (fbaCode == null) {
            fbaCode = "非FBA订单";
        } else {
            fbaCode = "FBA单号：" + fbaCode;
        }
        //打印fba单号
        PrintService.pl.printText(fbaCode);
        PrintService.pl.write(new byte[]{0x1b, 0x4a, 0x0a});
//        PrintService.pl.printText("\n");
        printDivider();
//        PrintService.pl.printText("\n");
        //居左
        PrintService.pl.write(new byte[]{0x1b, 0x61, 0x00});
//        //设置左边距
//        PrintService.pl.write(new byte[]{0x1d, 0x4c, 0x2d, 0x00});
        //打印渠道
        channel = "\t渠道名称：" + channel;
        PrintService.pl.printText(channel);
//        PrintService.pl.printText("\n");
        PrintService.pl.write(new byte[]{0x1b, 0x4a, (byte) 0x2a});
        //打印目的地
        country = "\t目的国：" + country;
//        PrintService.pl.printText(country);
//        PrintService.pl.write(new byte[]{0x1b,0x4a,0x01});
//        PrintService.pl.printText("\b");
//        //居左
//        PrintService.pl.write(new byte[]{0x1b, 0x61, 0x02});
        //打印件数
        String countString = "件数：" + String.valueOf(count) + "/" + String.valueOf(total);
//        PrintService.pl.printText(countString);
        String bottom = country + "\t\t" + countString;
        //打印目的国和件数
        PrintService.pl.printText(bottom);
        PrintService.pl.printText("\n");
        //换页
        PrintService.pl.write(new byte[]{0x1d, 0x0c});
        //居左
        PrintService.pl.write(new byte[]{0x1b, 0x61, 0x00});

    }

    public static void printASBitMap( String code, String fbaCode, String channel, String country, int count, int total) {
        int width = 590;
        Bitmap asBitMap;
        TextPaint textPaint = new TextPaint();

        textPaint.setColor(Color.BLACK);

        textPaint.setTextSize(32);
//        textPaint.setFakeBoldText(true);

        TextPaint boldPaint = new TextPaint();
        boldPaint.setTextSize(40);
        boldPaint.setFakeBoldText(true);
        //生成条码
        Bitmap barCode = BarcodeCreater.creatBarcode(context, code, width, 130, false, 1);
        //居中指令
        PrintService.pl.write(new byte[]{0x1b, 0x61, 0x01});
        //打印条码
        PrintService.pl.write(draw2PxPoint(barCode));
        //换行
        PrintService.pl.write(new byte[]{0x1b, 0x0a});
//        //
//        PrintService.pl.write(new byte[]{0x1d, 0x48, 0x0a});
        //画as单号
        System.out.println("code:" + code);
        StaticLayout layout = new StaticLayout(code, boldPaint, width,
                Layout.Alignment.ALIGN_CENTER, 1.5f, 0.0f, true);
        Bitmap bitmap = Bitmap.createBitmap(layout.getWidth(),
                layout.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(10, 10);
        canvas.drawColor(Color.WHITE);
        layout.draw(canvas);
        //打印单号
        PrintService.pl.printImage(bitmap);
        //换行
        PrintService.pl.write(new byte[]{0x1b, 0x0a});
        //画fba单号
        if (fbaCode == null) {
            fbaCode = "非FBA订单";
        } else {
            fbaCode = "FBA单号：" + fbaCode;
        }
        ;
        System.out.println("fba:" + fbaCode);
        layout = new StaticLayout(fbaCode, textPaint, width, Layout.Alignment.ALIGN_CENTER, 1.5f, 0, true);
        bitmap = Bitmap.createBitmap(layout.getWidth(),
                layout.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.translate(10, 10);
        canvas.drawColor(Color.WHITE);
        layout.draw(canvas);
        //打印fba单号
        PrintService.pl.printImage(bitmap);
        //换行
        PrintService.pl.write(new byte[]{0x1b, 0x0a});
        //分割线
        String devider = "—————————————————————————";
        System.out.println(devider);
        layout = new StaticLayout(devider, boldPaint, width, Layout.Alignment.ALIGN_CENTER, 1.5f, 0, true);
        bitmap = Bitmap.createBitmap(layout.getWidth(),
                layout.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.translate(10, 10);
        canvas.drawColor(Color.WHITE);
        layout.draw(canvas);
        //打印分割线
        PrintService.pl.write(draw2PxPoint(bitmap));
        //换行
        PrintService.pl.write(new byte[]{0x1b, 0x0a});
        //渠道
        channel = "渠道名称：" + channel;
        System.out.println("channel：" + channel);
        layout = new StaticLayout(channel, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.5f, 0, true);
        bitmap = Bitmap.createBitmap(layout.getWidth(),
                layout.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.translate(10, 10);
        canvas.drawColor(Color.WHITE);
        layout.draw(canvas);
        //打印渠道
        PrintService.pl.printImage(bitmap);
        //换行
        PrintService.pl.write(new byte[]{0x1b, 0x0a});
        //国家
        country = "目的国：" + country;
        System.out.println("country:" + country);
        String countString = "件数：" + String.valueOf(count) + "/" + String.valueOf(total);
        System.out.println("count" + countString);
        layout = new StaticLayout(country, textPaint, width / 2, Layout.Alignment.ALIGN_NORMAL, 1.5f, 0, true);
        StaticLayout layout2 = new StaticLayout(countString, textPaint, width / 2, Layout.Alignment.ALIGN_OPPOSITE, 1.5f, 0, true);
        bitmap = Bitmap.createBitmap(layout.getWidth(),
                layout.getHeight(), Bitmap.Config.ARGB_8888);
        Bitmap bitmap2 = Bitmap.createBitmap(layout2.getWidth(),
                layout.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        Canvas canvas2 = new Canvas(bitmap2);
        canvas.translate(10, 10);
        canvas.drawColor(Color.WHITE);
        canvas2.drawColor(Color.WHITE);
        layout.draw(canvas);
        layout2.draw(canvas2);
        //合并图片
        asBitMap = mergeLeftRight(bitmap, bitmap2);
        //打印国家和件数
        PrintService.pl.printImage(asBitMap);
    }

//    //条形码加白边
//    public static Bitmap addEdge(Bitmap bitmap) {
//        Paint bgPaint = new Paint();
//        bgPaint.setAntiAlias(true);
//        int edge = (590 - bitmap.getWidth()) / 2;
//        Bitmap outBitmap = Bitmap.createBitmap(590, 130, bitmap.getConfig());
//        Canvas canvas = new Canvas(bitmap);
//        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
//        final RectF rectF = new RectF(rect);
//        bgPaint.setColor(Color.WHITE);
//        canvas.drawRect(rectF, bgPaint);
//        canvas.drawBitmap(bitmap, edge, 0, bgPaint);
//        return outBitmap;
//    }

    //打印分割线
    public static void printDivider() {
        int width = 450;
        int height = 3;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = 0xff000000;
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        PrintService.pl.write(draw2PxPoint(bitmap));
    }


    //图片上下合并
    public static Bitmap mergeUpDown(Bitmap bitmap1, Bitmap bitmap2) {
        Bitmap bitmap3 = Bitmap.createBitmap(bitmap1.getWidth(),
                bitmap1.getHeight() + bitmap2.getHeight(), bitmap1.getConfig());
        Canvas canvas = new Canvas(bitmap3);
        canvas.drawBitmap(bitmap1, new Matrix(), null);
        canvas.drawBitmap(bitmap2, 0, bitmap1.getHeight(), null);
        return bitmap3;
    }

    //  图片左右合并
    public static Bitmap mergeLeftRight(Bitmap bitmap1, Bitmap bitmap2) {
        Bitmap bitmap3 = Bitmap.createBitmap(bitmap1.getWidth() + bitmap2.getWidth(),
                bitmap1.getHeight(), bitmap1.getConfig());
        Canvas canvas = new Canvas(bitmap3);
        canvas.drawBitmap(bitmap1, new Matrix(), null);
        canvas.drawBitmap(bitmap2, bitmap1.getWidth(), 0, null);
        return bitmap3;
    }


    /**
     * 用于将给定的内容生成成一维码 注：目前生成内容为中文的话将直接报错，要修改底层jar包的内容
     *
     * @param content 将要生成一维码的内容
     * @return 返回生成好的一维码bitmap
     * @throws WriterException WriterException异常
     */
    public static Bitmap CreateOneDCode(String content) throws WriterException {
        //
        BitMatrix matrix = new MultiFormatWriter().encode(content,
                BarcodeFormat.CODE_128, 380, 100);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    //打印图片
    public static byte[] draw2PxPoint(Bitmap bmp) {
        // 用来存储转换后的 bitmap 数据。为什么要再加1000，这是为了应对当图片高度无法
        // 整除24时的情况。比如bitmap 分辨率为 240 * 250，占用 7500 byte，
        // 但是实际上要存储11行数据，每一行需要 24 * 240 / 8 =720byte 的空间。再加上一些指令存储的开销，
        // 所以多申请 1000byte 的空间是稳妥的，不然运行时会抛出数组访问越界的异常。
        int size = bmp.getWidth() * bmp.getHeight() / 8 + 2000;
        byte[] data = new byte[size];
        int k = 0;
        // 设置行距为0的指令
        data[k++] = 0x1B;
        data[k++] = 0x33;
        data[k++] = 0x00;
        // 逐行打印
        for (int j = 0; j < bmp.getHeight() / 24f; j++) {
            // 打印图片的指令
            data[k++] = 0x1B;
            data[k++] = 0x2A;
            data[k++] = 33;
            data[k++] = (byte) (bmp.getWidth() % 256); // nL
            data[k++] = (byte) (bmp.getWidth() / 256); // nH
            // 对于每一行，逐列打印
            for (int i = 0; i < bmp.getWidth(); i++) {
                // 每一列24个像素点，分为3个字节存储
                for (int m = 0; m < 3; m++) {
                    // 每个字节表示8个像素点，0表示白色，1表示黑色
                    for (int n = 0; n < 8; n++) {
                        byte b = px2Byte(i, j * 24 + m * 8 + n, bmp);
                        data[k] += data[k] + b;
                    }
                    k++;
                }
            }
            data[k++] = 10;// 换行
        }
        return data;
    }

    //图片灰度化
    public static byte px2Byte(int x, int y, Bitmap bit) {
        if (x < bit.getWidth() && y < bit.getHeight()) {
            byte b;
            int pixel = bit.getPixel(x, y);
            int red = (pixel & 0x00ff0000) >> 16; // 取高两位
            int green = (pixel & 0x0000ff00) >> 8; // 取中两位
            int blue = pixel & 0x000000ff; // 取低两位
            int gray = RGB2Gray(red, green, blue);
            if (gray < 128) {
                b = 1;
            } else {
                b = 0;
            }
            return b;
        }
        return 0;
    }

    //图片转黑白
    private static int RGB2Gray(int r, int g, int b) {
        int gray = (int) (0.29900 * r + 0.58700 * g + 0.11400 * b); // 灰度转化公式
        return gray;
    }


}
