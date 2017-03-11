package cn.edu.ldu;
import cn.edu.ldu.util.Message;
import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import javax.crypto.Cipher;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.SwingWorker;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * 功能：客户机聊天界面
 * 作者：董相志，版权所有2016--2018，upsunny2008@163.com
 */
public class FileSender extends SwingWorker<Integer,Object>{
    private File file; //文件
    private Message msg;
    private ClientUI parent; //父类
    private SSLSocket sslSocket; //传送文件的套接字
    private Document doc;
    //构造函数
    public FileSender(File file,Message msg,ClientUI parent) {
        this.file=file;
        this.msg=msg;
        this.parent=parent;  
        doc=parent.txtPane.getDocument();      
    }

    @Override
    protected Integer doInBackground() throws Exception {
        SimpleAttributeSet attr=new SimpleAttributeSet();
        //获取客户机证书库
        InputStream key =ClientUI.class.getResourceAsStream("/cn/edu/ldu/keystore/client.keystore");//私钥库
        InputStream tkey =ClientUI.class.getResourceAsStream("/cn/edu/ldu/keystore/tclient.keystore");//公钥库
        String CLIENT_KEY_STORE_PASSWORD = "123456"; //client.keystore密码
        String CLIENT_TRUST_KEY_STORE_PASSWORD = "123456";//tclient.keystore密码

        SSLContext ctx = SSLContext.getInstance("SSL"); //SSL上下文
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509"); //密钥管理
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");//密钥管理
        KeyStore ks = KeyStore.getInstance("JKS");//密钥库
        KeyStore tks = KeyStore.getInstance("JKS");//密钥库
        ks.load(key, CLIENT_KEY_STORE_PASSWORD.toCharArray());//加载密钥库
        tks.load(tkey, CLIENT_TRUST_KEY_STORE_PASSWORD.toCharArray());//加载密钥库
        kmf.init(ks, CLIENT_KEY_STORE_PASSWORD.toCharArray());//密钥库初始化
        tmf.init(tks);//密钥库初始化
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);//用密钥库初始化SSL上下文
        //连接服务器
        sslSocket = (SSLSocket) ctx.getSocketFactory().createSocket(msg.getToAddr(),msg.getToPort()); //安全连接远程主机 
        
        //获取套接字输出流
        DataOutputStream out=new DataOutputStream(
                             new BufferedOutputStream(
                             sslSocket.getOutputStream()));
        //得到加密后的文件
        File newFile = DoFile.encrypt(file, tks);
        //获取文件输入流
        DataInputStream in=new DataInputStream(
                           new BufferedInputStream(
                           new FileInputStream(newFile)));

        byte[] inBuff=new byte[8096]; //读入缓冲区
        byte[] outBuff=new byte[8096];//输出缓冲区
        int numRead=0; //单次读取的字节数
        int numFinished=0;//总完成字节数
        long fileLen=newFile.length();  //计算文件长度
        byte[] fileAll=new byte[(int)fileLen];
        in.mark((int)fileLen);
        in.read(fileAll);       
        in.reset();
         //进行数字签名
        //计算消息摘要 
        MessageDigest mdigest=MessageDigest.getInstance("SHA-1");//160位
        //MessageDigest mdigest=MessageDigest.getInstance("MD5");//128位
        mdigest.update(fileAll);//计算
        byte[] digest=mdigest.digest();//返回计算摘要结果
        //更新显示
        doc.insertString(doc.getLength(), "生成的消息摘要："+new String(digest,"UTF-8")+"\n", attr); 
        //用客户机私钥加密消息摘要，即进行数字签名
        PrivateKey privateKey=(PrivateKey)ks.getKey("client", "123456".toCharArray());
        Cipher cipher=Cipher.getInstance("RSA/ECB/PKCS1Padding"); //“算法/模式/填充”
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);//初始化工作模式
        byte[] signedDigest=cipher.doFinal(digest);//加密
        //更新显示
        doc.insertString(doc.getLength(), "生成的数字签名："+new String(signedDigest,"UTF-8")+"\n", attr);       
        //首先发送文件名称、文件长度和数字签名
        out.writeUTF(newFile.getName());
        out.writeLong(fileLen);
        //发送数字签名
        out.write(signedDigest);
        out.flush();
        
        //然后传送文件内容   
        while (numFinished<fileLen && (numRead=in.read(inBuff))!=-1) { //文件可读,读到输入缓冲区
            System.arraycopy(inBuff, 0, outBuff, 0, numRead);//输入缓冲区数据拷贝到输出缓冲区
            //发送输出缓冲区数据
            out.write(outBuff,0,numRead); 
            out.flush();
            numFinished+=numRead; //已完成字节数
        }//end while
  
        if (numFinished<fileLen) { //实际传送字节数低于文件长度
             doc.insertString(doc.getLength(),newFile.getName()+"文件传送失败!\n",attr);
        }else { //文件传送成功
            //接收服务器的反馈信息
            BufferedReader br=new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            String response=br.readLine();//读取返回串            
            if (response.equalsIgnoreCase("M_DONE")) { //服务器成功接收
                //设置消息窗口
                StyleConstants.setForeground(attr, Color.red);            
                doc.insertString(doc.getLength(), newFile.getName(), attr);
                StyleConstants.setForeground(attr, Color.black);
                doc.insertString(doc.getLength(),"  服务器成功接收！\n", attr);
            }else if (response.equalsIgnoreCase("M_LOST")){ //服务器接收失败
                //设置消息窗口
                StyleConstants.setForeground(attr, Color.red);            
                doc.insertString(doc.getLength(), newFile.getName(), attr);
                StyleConstants.setForeground(attr, Color.black);
                doc.insertString(doc.getLength(),"  服务器接收失败！\n", attr);
            }//end if
            br.close();
        }//end if
        //关闭文件流
        in.close(); 
        out.close();
        sslSocket.close(); //关闭套接字
        return 100;
    } //doInBackground  
}