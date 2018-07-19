package com.aucma.machine;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.SerialPort;


public class StatusCapture implements Runnable {
    private SerialPort m_Port;
    private Timer m_Timer = new Timer();
    private Timer m_Timer1 = new Timer();
    //纸币状态
    private int m_NotesState = -1;
    //硬币状态
    private int m_CoinsState = -1;
    //纸币数
    private int m_NotesInit;
    //硬币数
    private int m_CoinsInit;
    //总金额
    private int m_Money;
    private int oldMoney = -1;
    //开关门
    private int m_Button=-1;
    private List<ISCInfo> m_Info = new ArrayList();
    private IMachineEventHandler m_EventHandler;
    private List<ErrorData> m_Error = new ArrayList();
    private boolean m_Exit;
    private InputStream inputStream;
    private OutputStream outputStream;
    private List<byte[]> m_Command = new ArrayList();
    private LinkedHashMap<String, SCState> m_State,mStateRoad;
    private int  m_CoinsRturn;





    //绑定信息handler
    public void SetEventHandler(IMachineEventHandler event) {
        this.m_EventHandler = event;
    }
    //开始串口开发
    public boolean Start(String portName) {
        try {
            this.m_Port = new SerialPort(new File(portName), 9600, 0);
            inputStream = this.m_Port.getInputStream();
            outputStream = this.m_Port.getOutputStream();
            this.m_State = new LinkedHashMap();
            this.mStateRoad = new LinkedHashMap();
            this.m_Timer.schedule(new StatusCapture.InfoTask(this), 100L, 100L);
            this.m_Timer1.schedule(new StatusCapture.HeartTask(this), 30000L, 30000L);
            this.m_Exit = false;
            Thread ex = new Thread(this);
            ex.start();
            this.m_NotesState = -1;
            this.m_CoinsState = -1;
            return true;
        }catch (Exception v){
            return false;
        }

    }
    //结束串口开发
    public void Close() {
        this.m_Timer.cancel();
        this.m_Timer1.cancel();
        this.m_Exit = true;
        if(this.m_Port != null) {
            this.m_Port.close();
            this.m_Port = null;
        }

    }


    //信息处理
    public void ExecCommand(byte[] buf) {
        int unitCode = MyFunc.getInt(buf[4]);
        switch(unitCode){
            //0x78签到响应
            case 120:
                if (m_Port != null) {
                    byte[]  bOutArray = {(byte) 0xEF, 0x55, (byte) 0xFE, 0x03, 0x78, 0x00, 0x00};
                    bOutArray[bOutArray.length - 1]=MyFunc.CalBlock(bOutArray);
                    send( bOutArray);
                    this.m_Info.add(new AucmaSign(MyFunc.getInt(buf[10]),MyFunc.getInt(buf[11]),MyFunc.ByteArrToHex(buf,8,10)));
                }
                break;
            //0x73料道配置响应
            case 115:
                if (m_Port != null) {
                    byte[] bOutArray = {(byte) 0xEF, 0x55, (byte) 0xFE, 0x03, 0x73, 0x00, 0x00};
                    bOutArray[bOutArray.length - 1] = MyFunc.CalBlock(bOutArray);
                    send(bOutArray);
                    if(MyFunc.getInt(buf[5])==0){
                        //当 Y5 为 0 时：Y9 为售货机类型
                    }else {
                        for (int i = 0;i<MyFunc.getBooleanArray(buf[6]).length;i++) {
                            m_State.put(String.valueOf(10+i),MyFunc.getBooleanArray(buf[6])[i] == 1? SCState.正常:SCState.无设备);
                        }
                        for (int i = 0;i<MyFunc.getBooleanArray(buf[7]).length;i++) {
                            m_State.put(String.valueOf(20+i),MyFunc.getBooleanArray(buf[7])[i] == 1? SCState.正常:SCState.无设备);
                        }
                        for (int i = 0;i<MyFunc.getBooleanArray(buf[8]).length;i++) {
                            m_State.put(String.valueOf(30+i),MyFunc.getBooleanArray(buf[8])[i] == 1? SCState.正常:SCState.无设备);
                        }
                        for (int i = 0;i<MyFunc.getBooleanArray(buf[9]).length;i++) {
                            m_State.put(String.valueOf(40+i),MyFunc.getBooleanArray(buf[9])[i] == 1? SCState.正常:SCState.无设备);
                        }
                        for (int i = 0;i<MyFunc.getBooleanArray(buf[10]).length;i++) {
                            m_State.put(String.valueOf(50+i),MyFunc.getBooleanArray(buf[10])[i] == 1? SCState.正常:SCState.无设备);
                        }
                        for (int i = 0;i<2;i++) {
                            m_State.put(String.valueOf(18+i),MyFunc.getBooleanArray(buf[16])[i] == 1? SCState.正常:SCState.无设备);
                        }
                        this.m_Info.add(new AucmaRoad(m_State));
                    }
                }
                break;
            //0x7D系统配置响应
            case 125:
                if (m_Port != null ) {
                    byte[]  bOutArray = {(byte) 0xEF, 0x55, (byte) 0xFE, 0x03, 0x7D, 0x00, 0x00};
                    bOutArray[bOutArray.length - 1]=MyFunc.CalBlock(bOutArray);
                    send( bOutArray);
                    Hashtable var15 = new Hashtable();
                    var15.put(50.0, MyFunc.getInt(buf[16]));
                    var15.put(100.0, MyFunc.getInt(buf[17]));
                    this.m_Info.add(new AucmaMachine(
                            var15,
                            MyFunc.getInt(buf[16]),
                            MyFunc.getInt(buf[17]),
                            MyFunc.getInt(buf[18]),
                            MyFunc.getBooleanArray(buf[19]),
                            MyFunc.getInt(buf[20]),
                            MyFunc.getBooleanArray(buf[21]),
                            MyFunc.getInt(buf[22]),
                            MyFunc.getInt(buf[23]),
                            MyFunc.getInt(buf[24]),
                            MyFunc.getInt(buf[25]),
                            MyFunc.getInt(buf[26]),
                            MyFunc.getInt(buf[27]),
                            MyFunc.getInt(buf[28]),
                            MyFunc.getInt(buf[29]),
                            MyFunc.getInt(buf[30]),
                            MyFunc.getInt(buf[31]),
                            MyFunc.getInt(buf[32]),
                            MyFunc.getInt(buf[33]),
                            MyFunc.getInt(buf[34]),
                            MyFunc.getInt(buf[35]),
                            MyFunc.getInt(buf[36]),
                            MyFunc.getBooleanArray(buf[37]),
                            MyFunc.getInt(buf[38]),
                            MyFunc.getInt(buf[39]),
                            MyFunc.getInt(buf[40]),
                            MyFunc.getInt(buf[41]),
                            MyFunc.getInt(buf[42]),
                            MyFunc.getInt(buf[43]),
                            MyFunc.getInt(buf[44]),
                            MyFunc.getBooleanArray(buf[45]),
                            MyFunc.getBooleanArray(buf[46])
                            ));
                }
                break;
            //0x79系统故障响应
            case 121:
                if (m_Port != null ) {
                    byte[]  bOutArray = {(byte) 0xEF, 0x55, (byte) 0xFE, 0x03, 0x79, 0x00, 0x00};
                    bOutArray[bOutArray.length - 1]=MyFunc.CalBlock(bOutArray);
                    send( bOutArray);
                    this.m_Info.add(new AucmaMachineE(
                            MyFunc.getBooleanArray(buf[5]),
                            MyFunc.getBooleanArray(buf[6]),
                            MyFunc.getBooleanArray(buf[7]),
                            MyFunc.getBooleanArray(buf[8]),
                            MyFunc.getBooleanArray(buf[9])
                    ));
                }
                break;
            //0x7A料道故障响应
            case 122:
                if (m_Port != null) {
                    byte[]  bOutArray = {(byte) 0xEF, 0x55, (byte) 0xFE, 0x03, 0x7A, 0x00, 0x00};
                    bOutArray[bOutArray.length - 1]=MyFunc.CalBlock(bOutArray);
                    send( bOutArray);
                    for (int i = 0;i<MyFunc.getBooleanArray(buf[6]).length;i++) {
                        mStateRoad.put(String.valueOf(10+i),MyFunc.getBooleanArray(buf[6])[i] == 0? SCState.正常:SCState.异常);
                    }
                    for (int i = 0;i<MyFunc.getBooleanArray(buf[7]).length;i++) {
                        mStateRoad.put(String.valueOf(20+i),MyFunc.getBooleanArray(buf[7])[i] == 0? SCState.正常:SCState.异常);
                    }
                    for (int i = 0;i<MyFunc.getBooleanArray(buf[8]).length;i++) {
                        mStateRoad.put(String.valueOf(30+i),MyFunc.getBooleanArray(buf[8])[i] == 0? SCState.正常:SCState.异常);
                    }
                    for (int i = 0;i<MyFunc.getBooleanArray(buf[9]).length;i++) {
                        mStateRoad.put(String.valueOf(40+i),MyFunc.getBooleanArray(buf[9])[i] == 0? SCState.正常:SCState.异常);
                    }
                    for (int i = 0;i<MyFunc.getBooleanArray(buf[10]).length;i++) {
                        mStateRoad.put(String.valueOf(50+i),MyFunc.getBooleanArray(buf[10])[i] == 0? SCState.正常:SCState.异常);
                    }
                    for (int i = 0;i<2;i++) {
                        //个别机子会发生这个问题记录
//                        mStateRoad.put(String.valueOf(18+i),MyFunc.getBooleanArray(buf[16])[i] == 0? SCState.正常:SCState.异常);
                    }
                    this.m_Info.add(new AucmaRoadE(mStateRoad));
                }
                break;
            //0x7B饮料有无货响应
            case 123:
                if (m_Port != null) {
                    byte[]  bOutArray = {(byte) 0xEF, 0x55, (byte) 0xFE, 0x03, 0x7B, 0x00, 0x00};
                    bOutArray[bOutArray.length - 1]=MyFunc.CalBlock(bOutArray);
                    send( bOutArray);
                    this.m_Info.add(new AucmaGoodIs(
                            MyFunc.getBooleanArray(buf[6]),
                            MyFunc.getBooleanArray(buf[7]),
                            MyFunc.getBooleanArray(buf[8]),
                            MyFunc.getBooleanArray(buf[9]),
                            MyFunc.getBooleanArray(buf[10]),
                            MyFunc.getBooleanArray(buf[11]),
                            MyFunc.getBooleanArray(buf[12]),
                            MyFunc.getBooleanArray(buf[13]),
                            MyFunc.getBooleanArray(buf[14])
                    ));
                }
                break;
            //0x76轮询指令
            case 118:
                if (m_Port != null) {
                    if(this.m_Command.size() > 0) {
                        send(this.m_Command.get(0));
                        this.m_Command.remove(0);
                    } else {
                        if(MyFunc.getInt(buf[3])==3) {
                            switch(MyFunc.getInt(buf[5])) {
                                case 0:
                                    this.m_Info.add(new AucmaSaleState("成功"));
                                break;
                                case 01:
                                    this.m_Info.add(new AucmaSaleState("忙"));
                                    break;
                                case 255:
                                    this.m_Info.add(new AucmaSaleState("失败"));
                                    break;
                                case 241:
                                    this.m_Info.add(new AucmaSaleState("纸币压仓失败"));
                                    break;
                                case 242:
                                    this.m_Info.add(new AucmaSaleState("钱不对"));
                                    break;
                                case 243:
                                    this.m_Info.add(new AucmaSaleState("不能卖、没货、没设料道、料道故障"));
                                    break;
                                case 244:
                                    this.m_Info.add(new AucmaSaleState("料道错误、开门、停止售卖"));
                                    break;
                                case 245:
                                    this.m_Info.add(new AucmaSaleState("交易序列号想同"));
                                    break;
                                case 246:
                                    this.m_Info.add(new AucmaSaleState("读卡器没准备好"));
                                    break;
                                case 247:
                                    this.m_Info.add(new AucmaSaleState("读卡器不能卖"));
                                    break;
                                case 248:
                                    this.m_Info.add(new AucmaSaleState("无法启动读卡器"));
                                    break;
                                case 249:
                                    this.m_Info.add(new AucmaSaleState("扣款失败"));
                                    break;
                                case 250:
                                    this.m_Info.add(new AucmaSaleState("超时出错"));
                                    break;
                                case 251:
                                    this.m_Info.add(new AucmaSaleState("取消"));
                                    break;
                                case 252:
                                    this.m_Info.add(new AucmaSaleState("支付方式不对"));
                                    break;
                                case 254:
                                    this.m_Info.add(new AucmaSaleState("校验错误"));
                                    break;
                            }
                        }else{
                            byte[] bOutArray = {(byte) 0xEF, 0x55, (byte) 0xFE, 0x06, 0x76, 0x00, 0x00, 0x00, 0x00, 0x00};
                            bOutArray[bOutArray.length - 1] = MyFunc.CalBlock(bOutArray);
                            send(bOutArray);
                            Log.i("小栗333",""+buf[19]);
                            int m_Button2 = buf[19]==04 ? 0:-1;
                            Log.i("小栗111",""+m_Button2);
                            //开门
                            if ( m_Button2  != m_Button){
                                this.m_Info.add(new AucmaButton(m_Button2));
                                m_Button =m_Button2;
                                Log.i("小栗111","发送："+m_Button2);
                            }
                            //将两个字节的byte 16进制数 转换成一个int型的10进制数 得到协议长度
                            m_Money = ((buf[13] & 0xff) << 8) | (buf[14] & 0xff);
                            if(oldMoney != -1 && oldMoney!=m_Money){
                                //纸硬币器
                                this.m_Info.add(new AucmaMoney(
                                        ((buf[13] & 0xff) << 8) | (buf[14] & 0xff),
                                        ((buf[15] & 0xff) << 8) | (buf[16] & 0xff),
                                        ((buf[17] & 0xff) << 8) | (buf[18] & 0xff)
                                ));

                            }
                            oldMoney = m_Money;
                            Log.i("小栗", "轮询指令");
                        }
                    }
                }
                break;
            //0x7C收到货指令
            case 124:
                if (m_Port != null) {
                    byte[]  bOutArray = {(byte) 0xEF, 0x55, (byte) 0xFE, 0x06, 0x7C, 0x00, 0x00,0x00 ,0x00,0x00};
                    byte s = MyFunc.CalBlock(bOutArray);
                    bOutArray[bOutArray.length - 1]=s;
                    send( bOutArray);
                    this.m_Info.add(new AucmaSaleMessage(
                            Integer.toHexString(MyFunc.getInt(buf[6])),
                            MyFunc.getInt(buf[7]),
                            MyFunc.getInt(buf[8]),
                            MyFunc.getInt(buf[9]),
                            MyFunc.getInt(buf[10]),
                            MyFunc.getInt(buf[11]),
                            MyFunc.getInt(buf[12]),
                            MyFunc.getInt(buf[23]),
                            MyFunc.getInt(buf[24]),
                            MyFunc.getInt(buf[25]),
                            MyFunc.getInt(buf[26]),
                            MyFunc.getInt(buf[27]),
                            MyFunc.getInt(buf[28]),
                            MyFunc.getInt(buf[29]),
                            MyFunc.getInt(buf[30]),
                            MyFunc.getInt(buf[35]),
                            MyFunc.getInt(buf[36]),
                            MyFunc.getInt(buf[37]),
                            MyFunc.getInt(buf[38]),
                            MyFunc.getInt(buf[39]),
                            MyFunc.getInt(buf[40]),
                            MyFunc.getInt(buf[41]),
                            Integer.toHexString(MyFunc.getInt(buf[42])),
                            Integer.toHexString(MyFunc.getInt(buf[43]))
                            )
                    );
                }
                break;
        }
    }

    //===================================执行类===============================
    //支付宝出货执行
    public boolean VMC_Sale(int Id,String key){
        byte[]  bOutArray = {(byte) 0xEF, 0x55, (byte) 0xFE, 0x17, 0x76, 0x05, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
        bOutArray[6]=MyFunc.getHeight(Id);
        bOutArray[7]=MyFunc.getLow(Id);
        bOutArray[9]=MyFunc.Tobyte(key);
        bOutArray[bOutArray.length - 1]=MyFunc.CalBlock(bOutArray);
        this.m_Command.add(bOutArray);
        return true;
    }
    //现金出货执行
    public boolean VMC_Sale2(int Id,String key){
        byte[]  bOutArray = {(byte) 0xEF, 0x55, (byte) 0xFE, 0x17, 0x76, 0x05, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
        bOutArray[6]=MyFunc.getHeight(Id);
        bOutArray[7]=MyFunc.getLow(Id);
        bOutArray[9]=MyFunc.Tobyte(key);
        bOutArray[bOutArray.length - 1]=MyFunc.CalBlock(bOutArray);
        this.m_Command.add(bOutArray);
        return true;
    }
    //售货机退币指令
    public Boolean  VMC_Time(int coins){
        byte[]  bOutArray = {(byte) 0xEF, 0x55, (byte) 0xFE, 0x05, 0x76, 0x09, 0x00, 0x00, 0x00};
        bOutArray[6]=MyFunc.getHeight(coins);
        bOutArray[7]=MyFunc.getLow(coins);
        bOutArray[bOutArray.length - 1]=MyFunc.CalBlock(bOutArray);
        this.m_Command.add(bOutArray);
        return  true;
    }

    //售货机设置料道价格
    public Boolean VMC_Price(String key , int price ){
        byte[]  bOutArray = {(byte) 0xEF, 0x55, (byte) 0xFE, 0x09, 0x76, 0x08, 0x01, 0x00, 0x00,0x00,0x00,0x00,0x00};
        bOutArray[7]=MyFunc.Tobyte(key);
        bOutArray[10]=MyFunc.getHeight(price);
        bOutArray[11]=MyFunc.getLow(price);
        bOutArray[bOutArray.length - 1]=MyFunc.CalBlock(bOutArray);
        this.m_Command.add(bOutArray);
        return  true;
    }

    //===================================查询类===============================
    //查询货道
    public boolean Road_See(){
        byte[]  bOutArray = {(byte) 0xEF, 0x55, (byte) 0xFE, 0x06,0x76 ,0x73,0x01,0x00,0x00,0x00};
        bOutArray[bOutArray.length - 1]=MyFunc.CalBlock(bOutArray);
        this.m_Command.add(bOutArray);
        return true;
    }
    //查询系统故障
    public boolean Machine_See(){
        byte[]  bOutArray = {(byte) 0xEF, 0x55, (byte) 0xFE, 0x06,0x76 ,0x79,0x00,0x00,0x00,0x00};
        bOutArray[bOutArray.length - 1]=MyFunc.CalBlock(bOutArray);
        this.m_Command.add(bOutArray);
        return true;
    }
    //查询料道故障
    public boolean RoadE_See(){
        byte[]  bOutArray = {(byte) 0xEF, 0x55, (byte) 0xFE, 0x06,0x76 ,0x7A,0x01,0x00,0x00,0x00};
        bOutArray[bOutArray.length - 1]=MyFunc.CalBlock(bOutArray);
        this.m_Command.add(bOutArray);
        return true;
    }
    //查询有无货
    public boolean GoodIs_See(){
        byte[]  bOutArray = {(byte) 0xEF, 0x55, (byte) 0xFE, 0x06,0x76 ,0x7B,0x00,0x00,0x00,0x00};
        bOutArray[bOutArray.length - 1]=MyFunc.CalBlock(bOutArray);
        this.m_Command.add(bOutArray);
        return true;
    }
    //查询系统信息
    public boolean MachineE_See(){
        byte[]  bOutArray = {(byte) 0xEF, 0x55, (byte) 0xFE, 0x06,0x76 ,0x7D,0x00,0x00,0x00,0x00};
        bOutArray[bOutArray.length - 1]=MyFunc.CalBlock(bOutArray);
        this.m_Command.add(bOutArray);
        return true;
    }
    //查询纸硬币器剩余币数 和 查询温度参数
    public boolean Coin_Qty() {
        byte[]  bOutArray = {(byte) 0xEF, 0x55, (byte) 0xFE, 0x06,0x76 ,0x7D,0x00,0x00,0x00,0x00};
        bOutArray[bOutArray.length - 1]=MyFunc.CalBlock(bOutArray);
        this.m_Command.add(bOutArray);
        return true;
    }




    //===================================附加类===============================
    //线程读取封包发送给onDataReceived
    @Override
    public void run() {
        // 定义一个包的最大长度
        int maxLength = 2048;
        byte[] buffer = new byte[maxLength];
        // 每次收到实际长度
        int available = 0;
        // 当前已经收到包的总长度
        int currentLength = 0;
        // 协议头长度4个字节（开始符3，长度1）
        int headerLength = 4;

        while (!this.m_Exit) {
            try {
                available = inputStream.available();
                if (available > 0) {
                    // 防止超出数组最大长度导致溢出
                    if (available > maxLength - currentLength) {
                        available = maxLength - currentLength;
                    }
                    //参数：b - 读入数据的缓冲区。off - 数组 b 中将写入数据的初始偏移量。len - 要读取的最大字节数。
                    int size = inputStream.read(buffer, currentLength, available);
                    //当前已经收到包 = 当前已经收到包+available
                    currentLength += available;
//                        Log.i("澳柯玛","size："+size+"----当前已经收到包:"+currentLength+"----available:"+available);
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            int cursor = 0;
            // 如果当前收到包大于头的长度，则解析当前包
            while (currentLength >= headerLength) {
                // 取到头部第一个字节
                if(buffer[cursor] != (byte)0xFE|| buffer[cursor + 1] != (byte)0x55 ||
                        buffer[cursor + 2] != (byte)0xEF) {
                    --currentLength;
                    ++cursor;
                    continue;
                }
                int contentLenght = buffer[cursor + 3] & 0xff;
                // 如果内容包的长度大于最大内容长度或者小于等于0，则说明这个包有问题，丢弃(丢掉开始码)
                if (contentLenght <= 0 || contentLenght > maxLength ) {
                    currentLength = 0;
                    break;
                }
                // 如果当前获取到长度小于整个包的长度，则跳出循环等待继续接收数据
                int factPackLen = contentLenght + 4;
                if (currentLength < contentLenght + 4) {
                    break;
                }

                // 一个完整包即产生
                onDataReceived(buffer, cursor, factPackLen);
                currentLength -= factPackLen;
                cursor += factPackLen;
            }
            // 残留字节移到缓冲区首
            if (currentLength > 0 && cursor > 0) {
                System.arraycopy(buffer, cursor, buffer, 0, currentLength);
            }

        }
    }
    //封包转新的包
    private void onDataReceived(final byte[] buffer, final int index, final int packlen){
        byte[] buf = new byte[packlen];
        System.arraycopy(buffer, index, buf, 0, packlen);
        String s =MyFunc.bytesToHexString(buf);
        Log.i("小栗","最终结果："+s);
        ExecCommand(buf);
    }
    //发送数据
    public void send(byte[] bOutArray){
        try
        {
            outputStream.write(bOutArray);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }
    //错误信息
    private class ErrorData {
        int ID;
        String Info;

        ErrorData(int id, String info) {
            this.ID = id;
            this.Info = info;
        }
    }


    //===================================计时器===============================
    //心跳包计时器
    private class HeartTask extends TimerTask {
        private StatusCapture m_Data;

        public HeartTask(StatusCapture data) {
            this.m_Data = data;
        }

        public void run() {
            StatusCapture.this.m_CoinsState = -1;
            StatusCapture.this.m_NotesState = -1;
            this.m_Data.m_Info.add(new SCHeartbeat());
        }
    }
    //信息计时器
    private class InfoTask extends TimerTask {
        private boolean m_Event = false;
        private StatusCapture m_Data;

        public InfoTask(StatusCapture data) {
            this.m_Data = data;
        }

        public void run() {
            if(!this.m_Event) {
                this.m_Event = true;
                try {
                    for(; StatusCapture.this.m_Info.size() > 0; StatusCapture.this.m_Info.remove(0)) {
                        if(this.m_Data.m_EventHandler != null) {
                            this.m_Data.m_EventHandler.InfoEventHandler(((ISCInfo)this.m_Data.m_Info.get(0)).packageCode, (ISCInfo)this.m_Data.m_Info.get(0));
                            Log.i("小栗111","hander发送："+((ISCInfo)this.m_Data.m_Info.get(0)).packageCode);
                        }
                    }

                    for(; StatusCapture.this.m_Error.size() > 0; StatusCapture.this.m_Error.remove(0)) {
                        if(this.m_Data.m_EventHandler != null) {
                            this.m_Data.m_EventHandler.ErrorEventHandler(((StatusCapture.ErrorData)this.m_Data.m_Error.get(0)).ID, ((StatusCapture.ErrorData)this.m_Data.m_Error.get(0)).Info);
                        }
                    }
                } catch (Exception var2) {

                }

                this.m_Event = false;
            }

        }
    }
}
