package com.jwkj.device.shake;

import com.hdl.udpsenderlib.UDPResult;
import com.jwkj.device.entity.LocalDevice;
import com.jwkj.device.utils.MUtils;

import java.nio.ByteBuffer;

/**
 * 搜索信息构造
 */
class ShakeData {
    /**
     * 指令类型
     */
    private int cmd;
    /**
     * 错误码
     */
    private int error_code;
    /**
     * 结构体大小
     */
    private int structSize;
    /**
     * 结构体版本信息--->需要根据此位来决定是否取指定的值
     */
    private int msgVersion;
    /**
     * 设备的id
     */
    private int id;
    /**
     * 设备的类型
     */
    private int type;
    /**
     * 设备标记是否有密码---1有，0没有
     */
    private int flag;
    /**
     * 设备子类型
     */
    private int subType;
    /**
     * 是否发现新id
     */
    private boolean isFoundNewId;
    /**
     * 新的设备id，isFoundNewId=true时才有值
     */
    private int contactNewId;
    /**
     * 本地p2p端口
     */
    private int localP2PPort;
    /**
     * 本地p2p的ip地址
     */
    private int localP2PRes;
    /**
     * 有些定制商只希望搜到自己的设备id
     */
    private static int[] iCustomer;

    /**
     * 结果转换
     *
     * @param result
     * @return
     */
    public static LocalDevice getDevice(UDPResult result) {
        if (result == null) {
            return null;
        }
        LocalDevice data = new LocalDevice();
        ByteBuffer buffer = ByteBuffer.allocate(result.getResultData().length);
        buffer.put(result.getResultData());
        if (buffer.getInt(0) != Cmd.CMD_RECEIVE_DEVICE) {//cmd!=2表示不是搜索回应，此处忽略
            return null;
        }
        //下面几个字段是所有固件都会有的，不用判断
        data.setIP(result.getIp());
        data.setId(String.valueOf(buffer.getInt(16)));
        data.setType(buffer.getInt(20));
        data.setFlag(buffer.getInt(24));
        //msgversion的第6位（从0开始的，其实是第7位）为1表示是新版固件-->需要按msgversion的标记（1可读，0不可读）来取值
        int msgVersion = buffer.getInt(12);
        if (MUtils.isCanGetValue(msgVersion, 6)) {
            //可以取客户ID
            if (MUtils.isCanGetValue(msgVersion, 0)) {
                int customId = buffer.getInt(68);
                if ((buffer.getInt(12) & 0x01) != 0) {
                    data.setCustomId(customId);
                } else {
                    data.setCustomId(0);
                }
            }
            //可以取子类型
            if (MUtils.isCanGetValue(msgVersion, 4)) {
                data.setSubType(buffer.getInt(80));
            }
            //可以取重号标记
            if (MUtils.isCanGetValue(msgVersion, 7)) {
                //重号处理字段
                data.setFoundNewId(buffer.getInt(88) == 1 ? true : false);
                if (data.isFoundNewId()) {
                    //获取新的设备id
                    data.setContactNewId(buffer.getInt(92));
                }
            }
            //可以取mac信息
            if (MUtils.isCanGetValue(msgVersion, 8)) {
                //MAC地址（返回的数据是按四位四位倒序的）
                byte[] b = buffer.array();
                byte[] macs1 = new byte[2];
                byte[] macs2 = new byte[4];
                System.arraycopy(b, 60, macs1, 0, 2);
                System.arraycopy(b, 64, macs2, 0, 4);
                macs1 = MUtils.bytesReverseOrder(macs1);
                macs2 = MUtils.bytesReverseOrder(macs2);
                byte[] macs = new byte[6];
                System.arraycopy(macs1, 0, macs, 0, 2);
                System.arraycopy(macs2, 0, macs, 2, 4);
                data.setMac(MUtils.getMacAddress(macs, ""));
            }
            //以后新增字段，需要在这里加判断是否可以取这个字段的值，返回true才能取这个值
        } else {
            //旧固件处理方式（固件没有标记为最新固件）
            data.setSubType(buffer.getInt(80));
            data.setFoundNewId(buffer.getInt(88) == 1 ? true : false);
            if (data.isFoundNewId()) {
                data.setContactNewId(buffer.getInt(92));//获取新的设备id
            }
            int anInt = buffer.getInt(84);
            short port = (short) anInt;
            short res = (short) (anInt >> 16);
            data.setLocalP2PPort(port);
            data.setLocalP2PRes(res);
            //客户ID
            int customId = buffer.getInt(68);
            if ((buffer.getInt(12) & 0x01) != 0) {
                data.setCustomId(customId);
            } else {
                data.setCustomId(0);
            }
            //MAC地址（返回的数据是按四位四位倒序的）
            byte[] b = buffer.array();
            byte[] macs1 = new byte[2];
            byte[] macs2 = new byte[4];
            System.arraycopy(b, 60, macs1, 0, 2);
            System.arraycopy(b, 64, macs2, 0, 4);
            macs1 = MUtils.bytesReverseOrder(macs1);
            macs2 = MUtils.bytesReverseOrder(macs2);
            byte[] macs = new byte[6];
            System.arraycopy(macs1, 0, macs, 0, 2);
            System.arraycopy(macs2, 0, macs, 2, 4);
            data.setMac(MUtils.getMacAddress(macs, ""));
        }
        //如果app设置了只允许指定的客户ID设备，局域网搜索时去掉其它设备
        boolean isCustomID = false;
        if (iCustomer != null) {
            for (int i = 0; i < iCustomer.length; i++) {
                int customerID = iCustomer[i];
                if (customerID != 0) {
                    if ((buffer.getInt(12) & 0x01) != 0 && buffer.getInt(17 * 4) == customerID) {
                        isCustomID = true;
                        break;
                    }
                } else {
                    isCustomID = true;
                    break;
                }
            }
            buffer.clear();
            if (!isCustomID) {
                return null;
            }
        }
        buffer.clear();
        return data;
    }


    /**
     * 局域网搜索设备的指令类
     */
    public static class Cmd {
        public static final int CMD_SHAKE_DEVICE = 1;//搜索的命令
        public static final int CMD_RECEIVE_DEVICE = 2;//接收设备的命令
        public static final int CMD_SHAKE_DEVICE_DEFAULT_PORT = 8899;//搜索设备的端口号
    }

    /**
     * 将ShakeData转换为字节数组输出
     *
     * @param data shakedata对象
     * @return 字节数组
     */
    public static byte[] getBytes(ShakeData data) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.putInt(data.getCmd());
        buffer.putInt(data.getError_code());
        buffer.putInt(data.getStructSize());
        buffer.putInt(data.getId());
        buffer.putInt(data.getType());
        buffer.putInt(data.getFlag());
        buffer.putInt(data.getSubType());
        byte[] array = buffer.array();
        buffer.clear();// 清空缓冲区
        return array;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public int getStructSize() {
        return structSize;
    }

    public void setStructSize(int structSize) {
        this.structSize = structSize;
    }

    public int getMsgVersion() {
        return msgVersion;
    }

    public void setMsgVersion(int msgVersion) {
        this.msgVersion = msgVersion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getSubType() {
        return subType;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }

    public boolean isFoundNewId() {
        return isFoundNewId;
    }

    public void setFoundNewId(boolean foundNewId) {
        isFoundNewId = foundNewId;
    }

    public int getContactNewId() {
        return contactNewId;
    }

    public void setContactNewId(int contactNewId) {
        this.contactNewId = contactNewId;
    }

    public int getLocalP2PPort() {
        return localP2PPort;
    }

    public void setLocalP2PPort(int localP2PPort) {
        this.localP2PPort = localP2PPort;
    }

    public int getLocalP2PRes() {
        return localP2PRes;
    }

    public void setLocalP2PRes(int localP2PRes) {
        this.localP2PRes = localP2PRes;
    }

    public int[] getiCustomer() {
        return iCustomer;
    }

    public void setiCustomer(int[] iCustomer) {
        this.iCustomer = iCustomer;
    }

    @Override
    public String toString() {
        return "ShakeData{" +
                "cmd=" + cmd +
                ", error_code=" + error_code +
                ", structSize=" + structSize +
                ", msgVersion=" + msgVersion +
                ", id=" + id +
                ", type=" + type +
                ", flag=" + flag +
                ", subType=" + subType +
                ", isFoundNewId=" + isFoundNewId +
                ", contactNewId=" + contactNewId +
                ", localP2PPort=" + localP2PPort +
                ", localP2PRes=" + localP2PRes +
                '}';
    }
}
