package cn.edu.ldu.util;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * 功能：消息类，定义会话消息结构，规定会话协议。
 * 客户机                      服务器
 * 登录时发送 M_LOGIN 消息   收到 M_LOGIN 消息，回送：M_ACK，向其他用户转发 M_LOGIN 消息
 * 发言时发送 M_MSG 消息     收到 M_MSG 消息，向所有用户转发 M_MSG 消息
 * 退出时发送 M_QUIT 消息    收到 M_QUIT 消息，向所有其他用户转发 M_QUIT 消息
 * 
 * 收到 M_LOGIN 消息，更新消息显示和用户列表
 * 收到 M_MSG 消息，更新消息显示
 * 收到 M_QUIT 消息，更新消息显示和用户列表
 * 
 * 作者：董相志，版权所有2016--2018，upsunny2008@163.com
 */
public class Message implements Serializable {
    private String userName=null; //用户名
    private String password=null; //密码
    private String type=null; //消息类型：M_LOGIN:用户登录消息；M_ACK:服务器对登录用户的确认消息；M_MSG:会话消息；M_QUIT:用户退出消息
    private String text=null; //消息体
    private InetAddress toAddr=null; //目标用户地址
    private int toPort; //目标用户端口

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public InetAddress getToAddr() {
        return toAddr;
    }

    public void setToAddr(InetAddress toAddr) {
        this.toAddr = toAddr;
    }

    public int getToPort() {
        return toPort;
    }

    public void setToPort(int toPort) {
        this.toPort = toPort;
    }

}
