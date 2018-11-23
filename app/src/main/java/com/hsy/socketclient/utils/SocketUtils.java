package com.hsy.socketclient.utils;

import android.util.Log;

import com.hsy.socketclient.msg.HelloOuterClass;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketUtils {

    private static final String TAG = "SocketUtils";
    private ExecutorService mExecutorService;
    private static final String HOST = "192.168.1.188";
    private static final int PORT = 3565;
    private Socket mSocket;
    private DataInputStream mInput;
    private DataOutputStream mOutputStream;
    private static SocketUtils mInstance;
    private TimerTask task;
    private Timer timer = new Timer();

    public static SocketUtils getInstance() {

        if (mInstance == null) {
            mInstance = new SocketUtils();
        }
        return mInstance;
    }

    public SocketUtils() {
        mExecutorService = Executors.newCachedThreadPool();
    }

    public void connect() {
        mExecutorService.execute(new ConnectService());
    }

    public void send(byte[] msg) {
        mExecutorService.execute(new SendService(msg));
    }

    public void disconnect() {
        try {
            if (mInput != null) {
                mInput.close();
            }
            if (mOutputStream != null) {
                mOutputStream.close();
            }
            if (mSocket != null) {
                mSocket.close();
            }
            mSocket = null;
            mOutputStream = null;
            mInput = null;
            Log.d(TAG, "socket_disconnect");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ConnectService implements Runnable {
        @Override
        public void run() {
            try {
                mSocket = new Socket(HOST, PORT);
//                mSocket = new Socket();
//                /*超时时间为2秒*/
//                mSocket.connect(new InetSocketAddress(HOST, PORT), 2000);
                /*连接成功的话  发送心跳包*/
                if (mSocket.isConnected()) {
                    Log.v(TAG, "socket 连接成功");
                    mOutputStream = new DataOutputStream(mSocket.getOutputStream());
                    mInput = new DataInputStream(mSocket.getInputStream());
                    /*发送心跳数据*/
                    //sendBeatData();
                    receiveMsg();
                } else {
                    Log.v(TAG, "socket 连接失敗");
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof SocketTimeoutException) {
                    Log.v(TAG, "连接超时，正在重连");
                    //   toastMsg("连接超时，正在重连");
                    //   releaseSocket();

                } else if (e instanceof NoRouteToHostException) {
                    Log.v(TAG, "该地址不存在，请检查");
                    //   toastMsg("该地址不存在，请检查");
                    //   stopSelf();

                } else if (e instanceof ConnectException) {
                    Log.v(TAG, "连接异常或被拒绝，请检查");
                    //   toastMsg("连接异常或被拒绝，请检查");
                    //   stopSelf();
                }
            }
        }
    }

    /*定时发送数据*/
    private void sendBeatData() {
        if (timer == null) {
            timer = new Timer();
        }

        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        /*这里的编码方式根据你的需求去改*/
//                        mOutputStream.writeUTF("\n\r");
//                        mOutputStream.flush();
                        Log.v(TAG, "发送心跳包");

                        HelloOuterClass.Hello.Builder helloBuilder = HelloOuterClass.Hello.newBuilder();
                        helloBuilder.setName("hsy_heart");
                        HelloOuterClass.Hello hello = helloBuilder.build();
                        //send(hello.toByteArray());

                        // byte[] msg = hello.toByteArray();
                        int msgId = 3;
                        byte[] data = new byte[4];
                        data[0] = (byte) (2 >>> 8 & 0xFF);
                        data[1] = (byte) (2 >>> 0 & 0xFF);
                        data[2] = (byte) ((msgId >>> 8) & 0xFF);
                        data[3] = (byte) ((msgId >>> 0) & 0xFF);
                        // System.arraycopy(msg, 0, data, 4, msg.length);
                        Log.e(TAG, "客户端发送");

                        if (mSocket != null && mSocket.isConnected() && mOutputStream != null && !mSocket.isClosed() && !mSocket.isOutputShutdown()) {
                            mOutputStream.write(data);
                            mOutputStream.flush();
                            Log.e(TAG, "客户端发送消息");
                        } else {
                            Log.e(TAG, "客户端没有连接到服务器");
                        }
                    } catch (Exception e) {
                        /*发送失败说明socket断开了或者出现了其他错误*/
                        Log.d(TAG, "连接断开，正在重连22222");
                        /*重连*/
                        releaseSocket();
                        e.printStackTrace();
                    }
                }
            };
        }

        timer.schedule(task, 0, 2000);
    }

    /*释放资源*/
    private void releaseSocket() {
//
//        if (task != null) {
//            task.cancel();
//            task = null;
//        }
//        if (timer != null) {
//            timer.purge();
//            timer.cancel();
//            timer = null;
//        }
//
//        if (mOutputStream != null) {
//            try {
//                mOutputStream.close();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            mOutputStream = null;
//        }
//
//        if (mSocket != null) {
//            try {
//                mSocket.close();
//            } catch (IOException e) {
//            }
//            mSocket = null;
//        }

        //mExecutorService.shutdownNow();
        connect();
//        if (connectThread != null) {
//            connectThread = null;
//        }
//
//        /*重新初始化socket*/
//        if (isReConnect) {
//            initSocket();
//        }

    }


    public static void main(String[] args) {

        //java int 有32bit, short 16位
        int num = 2;
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((num >> 24) & 0xff);//256=2^8
        bytes[1] = (byte) ((num >> 16) & 0xff);//256=2^8
        bytes[2] = (byte) ((num >> 8) & 0xff);//256=2^8
        bytes[3] = (byte) (num & 0xff);//256=2^8
        String hex = HexUtils.byteToHexString(bytes);
        byte test = (byte) 2;
        short a = 2;

//        // Little Endian
//        ret[0] = (byte) x;
//        ret[1] = (byte) (x >> 8);
//
//        // Big Endian
//        ret[0] = (byte) (x >> 8);
//        ret[1] = (byte) x;

        System.out.println("hex:" + hex);
    }

    //翻转byte数组
    public static byte[] reverseBytes(byte[] bytes) {
        byte tmp;
        int len = bytes.length;

        for (int i = 0; i < len / 2; i++) {
            tmp = bytes[len - 1 - i];
            bytes[len - 1 - i] = bytes[i];
            bytes[i] = tmp;
        }
        return bytes;
    }

    private class SendService implements Runnable {
        private byte[] msg;

        public SendService(byte[] msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            try {
                int msgId = 0;
                //大端排序
                byte[] data = new byte[4 + msg.length];

                //data[0] = (byte) ((msg.length + 2 >>> 8) & 0xFF);
                //data[1] = (byte) ((msg.length + 2 >>> 0) & 0xFF);
                data[0] = (byte) ((msg.length + 2 >> 8) & 0xFF);
                data[1] = (byte) ((msg.length + 2) & 0xFF);

                //data[2] = (byte) ((msgId >>> 8) & 0xFF);
                //data[3] = (byte) ((msgId >>> 0) & 0xFF);
                data[2] = (byte) ((msgId >> 8) & 0xFF);
                data[3] = (byte) (msgId & 0xFF);
                System.arraycopy(msg, 0, data, 4, msg.length);

                //小端排序
                byte[] littleByte = new byte[data.length];
                littleByte[0] = (byte) (msg.length + 2);
                littleByte[1] = (byte) (msg.length + 2 >> 8);
                littleByte[2] = (byte) msgId;
                littleByte[3] = (byte) (msgId >> 8);
                System.arraycopy(msg, 0, littleByte, 4, msg.length);
//                for (int i = 0; i < msg.length; i++) {
//                    temp[i + 4] = data[msg.length - i - 1];
//                }

                Log.e(TAG, "客户端发送");
                if (mSocket != null && mSocket.isConnected() && mOutputStream != null) {
                    mOutputStream.write(littleByte);
                    mOutputStream.flush();
                    Log.e(TAG, "客户端发送消息");
                } else {
                    Log.e(TAG, "客户端没有连接到服务器");
                }
            } catch (Exception e) {
                Log.e(TAG, "发送失败");
                e.printStackTrace();
            }
        }
    }

    //byte 数组与 int 的相互转换 ,小端
    public static int byteArrayToInt(byte[] b) {
        return b[0] & 0xFF | (b[1] & 0xFF) << 8;
    }


    private void receiveMsg() {
        try {
            while (true) {

                if (mSocket.isConnected()) {

                    //InputStreamReader inputStreamReader =new InputStreamReader(mSocket.getInputStream());

                    // BufferedReader br = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
//                    String readstr = br.readLine();

                    //先返回占位两个byte的消息长度
                    // 消息长度=消息id(长度2)+data的长度
                    //返回占位两个长度byte的消息id
                    //返回消息内容

                    byte[] lengthByte = new byte[2];
                    mInput.read(lengthByte);
                    int length = byteArrayToInt(lengthByte);
                    Log.v(TAG, "length:" + length);
                    byte[] msgIdByte = new byte[2];
                    mInput.read(msgIdByte);
                    int msgid = byteArrayToInt(lengthByte);
                    Log.v(TAG, "length:" + msgid);
                    byte[] dataByte = new byte[length - 2];
                    mInput.read(dataByte);
                    ByteArrayInputStream input3 = new ByteArrayInputStream(dataByte);
                    // 反序列化
                    HelloOuterClass.Hello hello = HelloOuterClass.Hello.parseFrom(input3);
                    Log.v(TAG, "hello name:" + hello.getName());


                    //涉及拆包 粘包
//                    byte[] temp = new byte[512];
//                    mInput.read(temp);
//                    String msgHex = HexUtils.byteToHexString(temp);
//
//                    byte[] msgIdByte = new byte[2];
//                    msgIdByte[0] = temp[0];
//                    msgIdByte[1] = temp[1];
//                    String msgid = HexUtils.byteToHexString(msgIdByte);
//                    Integer msgid2 = Integer.parseInt(msgid, 16);
//
//                    Log.v(TAG, "READ_BYTE:" + msgHex);
//                    Log.v(TAG, "msgid:" + msgid);
//                    Log.v(TAG, "msgid2:" + msgid2);
//                    byte[] msgIdByte = new byte[2];
//                    int read = mInput.readUnsignedByte();
//                    Log.v(TAG,"READ_BYTE:"+read);
//                    String msg = mInput.readUTF();//会导致阻塞
//                    Log.v(TAG, "MSG:" + msg);
//                    if (msg != null) {
//                        byte[] msgByte = msg.getBytes();
//                        byte[] data = new byte[msgByte.length - 2];
//                        System.arraycopy(msgByte, 2, data, 0, data.length);
//                        ByteArrayInputStream input3 = new ByteArrayInputStream(data);
//                        // 反序列化
//                        HelloOuterClass.Hello hello = HelloOuterClass.Hello.parseFrom(input3);
//
//                        byte[] msgIdByte = new byte[2];
//                        msgIdByte[0] = msgByte[0];
//                        msgIdByte[1] = msgByte[1];
//                        String msgid = HexUtils.byteToHexString(msgIdByte);
//
//                        Integer msgid2 = Integer.parseInt(msgid, 16);
//
//                        Log.d(TAG, "receiveMsg:" + hello.getName());
//                        Log.d(TAG, "receiveMsgID:" + msgid);
//                        Log.d(TAG, "receiveMsgID2:" + msgid2);
//                    }
                }
            }
        } catch (EOFException e) {
            Log.e(TAG, "服务器断开连接");
        } catch (SocketException e) {
            Log.e(TAG, "主动断开连接");
        } catch (IOException e) {
            Log.e(TAG, "receiveMsg: ");
            e.printStackTrace();
        }
    }
}
