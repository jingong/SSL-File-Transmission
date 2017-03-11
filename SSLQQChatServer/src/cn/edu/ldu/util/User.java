package cn.edu.ldu.util;

import java.net.DatagramPacket;

/**
 * 功能：定义用户对象，包含用户名和收到的数据报
 * 作者：董相志，版权所有2016--2018，upsunny2008@163.com
 */
public class User {
    private String userName=null; //用户名
    private DatagramPacket packet=null; //数据报

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public DatagramPacket getPacket() {
        return packet;
    }

    public void setPacket(DatagramPacket packet) {
        this.packet = packet;
    }   
}
