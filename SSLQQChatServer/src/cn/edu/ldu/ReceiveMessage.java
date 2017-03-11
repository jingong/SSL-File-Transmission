package cn.edu.ldu;

import cn.edu.ldu.util.Message;
import cn.edu.ldu.util.Translate;
import cn.edu.ldu.util.User;
import java.awt.Color;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * 功能：服务器消息接收和处理线程
 * 作者：董相志，版权所有2016--2018，upsunny2008@163.com
 */
public class ReceiveMessage extends Thread {
    private DatagramSocket serverSocket; //服务器套接字
    private DatagramPacket packet;  //数据报
    private List<User> userList=new ArrayList<User>(); //用户列表
    private byte[] data=new byte[8096]; //8K字节数组
    private ServerUI parentUI; //消息窗口
    private Document doc; //消息文档
    private SimpleAttributeSet attr=new SimpleAttributeSet(); //文档属性   
    //构造函数
    public ReceiveMessage(DatagramSocket socket,ServerUI parent) {
        serverSocket=socket;
        parentUI=parent;
        //设置消息窗口
        StyleConstants.setForeground(attr, Color.black);
        doc=parentUI.txtPane.getDocument(); //消息窗口文档
    }
    @Override
    public void run() {  
        while (true) { //循环处理收到的各种消息
            try {
            packet=new DatagramPacket(data,data.length);//构建接收数据包
            serverSocket.receive(packet);//接收客户机数据
            //收到的消息反序列化转为消息对象
            Message msg=(Message)Translate.ByteToObject(packet.getData());
            String userName=msg.getUserName();//当前消息来自哪个用户？
            if (msg.getType().equalsIgnoreCase("M_LOGIN")) { //是M_LOGIN消息
                //新用户加入用户列表
                User user=new User();
                user.setUserName(userName);//用户名
                user.setPacket(packet);//保存收到的数据包
                userList.add(user);
                //更新服务器聊天室大厅
                doc.insertString(doc.getLength(), userName+" 登录！\n", attr);
                //向所有其他在线用户发送新用户M_LOGIN消息，向新用户发送整个用户列表
                for (int i=0;i<userList.size();i++) { //遍历整个用户列表                                       
                    //向其他在线用户发送M_LOGIN消息
                    if (!userName.equalsIgnoreCase(userList.get(i).getUserName())){
                         DatagramPacket oldPacket=userList.get(i).getPacket(); //用户登录时创建的数据报
                        DatagramPacket newPacket=new DatagramPacket(data,data.length,oldPacket.getAddress(),oldPacket.getPort());//构建向其他用户发送的数据包
                        serverSocket.send(newPacket); //发送
                    }//end if
                    //向当前用户回送M_ACK消息，将第i个用户加入当前用户的用户列表
                    Message other=new Message();
                    other.setUserName(userList.get(i).getUserName());
                    other.setType("M_ACK");
                    byte[] buffer=Translate.ObjectToByte(other);
                    DatagramPacket newPacket=new DatagramPacket(buffer,buffer.length,packet.getAddress(),packet.getPort());
                    serverSocket.send(newPacket);
                }//end for
            }else if (msg.getType().equalsIgnoreCase("M_MSG")) { //是M_MSG消息
                //更新显示
                doc.insertString(doc.getLength(), userName+" 说："+msg.getText()+"\n", attr);
                //转发消息
                for (int i=0;i<userList.size();i++) { //遍历用户
                    DatagramPacket oldPacket=userList.get(i).getPacket();
                    DatagramPacket newPacket=new DatagramPacket(data,data.length,oldPacket.getAddress(),oldPacket.getPort());//构建发送数据包
                    serverSocket.send(newPacket); //发送
                }
            }else if (msg.getType().equalsIgnoreCase("M_QUIT")) { //是M_QUIT消息
                //更新显示
                doc.insertString(doc.getLength(), userName+" 下线！\n", attr);
                //删除用户
                for(int i=0;i<userList.size();i++) {
                    if (userList.get(i).getUserName().equals(userName)) {
                        userList.remove(i);
                        break;
                    }
                }//end for
                //向其他用户转发下线消息
                for (int i=0;i<userList.size();i++) {
                    DatagramPacket oldPacket=userList.get(i).getPacket();
                    DatagramPacket newPacket=new DatagramPacket(data,data.length,oldPacket.getAddress(),oldPacket.getPort());
                    serverSocket.send(newPacket);
                }//end for 
            }//end if
            } catch (Exception ex) {
            }// end try
        }//end while
    }//end run
}//end class
