package cn.edu.ldu;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.net.ssl.SSLSocket;
import javax.swing.SwingWorker;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * 功能：服务器界面类
 * 作者：董相志，版权所有2016--2018，upsunny2008@163.com
 */
public class RecvFile extends SwingWorker<Integer,Object> {
    private final SSLSocket toClientSocket; //与客户机对话的套接字
    private Document doc; //消息文档
    private KeyStore tks;
    private KeyStore ks;
    public RecvFile(SSLSocket toClientSocket,Document doc,KeyStore tks,KeyStore ks) { //构造函数
        this.toClientSocket=toClientSocket;
        this.doc=doc;
        this.tks=tks;
        this.ks = ks;
    }
    @Override
    protected Integer doInBackground() throws Exception {
        SimpleAttributeSet attr=new SimpleAttributeSet(); //文档属性
        StyleConstants.setForeground(attr, Color.red);
        //获取套接字输入流
        DataInputStream in=new DataInputStream(
                           new BufferedInputStream(
                           toClientSocket.getInputStream()));
        //首先接收文件名、文件长度和数字签名
        String filename=in.readUTF(); //文件名
        int fileLen=(int)in.readLong(); //文件长度
        byte[] clientDigest=new byte[128]; //定义数字签名数组
        int digestLen=in.read(clientDigest);//读取数字签名
        //更新显示
        doc.insertString(doc.getLength(), "收到的数字签名："+new String(clientDigest,"UTF-8")+"\n", attr);
        byte[] fileAll=new byte[(int)fileLen];
        
        //创建文件输出流
        File file=new File("e:\\upload\\"+filename);
        BufferedOutputStream out=new BufferedOutputStream(
                           new FileOutputStream(file));
        byte[] inBuff=new byte[8096]; //读入缓冲区
        byte[] outBuff=new byte[8096];//输出缓冲区
        int numRead=0; //单次读取的字节数
        int numFinished=0;//总完成字节数
        while (numFinished<fileLen && (numRead=in.read(inBuff))!=-1) { //输入流可读
            System.arraycopy(inBuff, 0, outBuff, 0, numRead);//输入缓冲区数据拷贝到输出缓冲区
            out.write(outBuff,0,numRead);
            out.flush();
            System.arraycopy(outBuff, 0, fileAll, numFinished, numRead);
            numFinished+=numRead; //已完成字节数
        }//end while 到这里加密的文件已经接受完毕
        
        
        //对得到的加密文件进行解密
        DecodeFile.decrypt(file, ks);
        
        //给客户机发送一个完成信息
        PrintWriter pw=new PrintWriter(toClientSocket.getOutputStream(),true);
        if (numFinished<fileLen) {
            pw.println("M_LOST"); //回送失败信息
            //更新显示
            doc.insertString(doc.getLength(), filename+"  文件接收失败！\n", attr);
        }else {
            //重新计算消息摘要
            MessageDigest mdigest=MessageDigest.getInstance("SHA-1");//160位
            //MessageDigest mdigest=MessageDigest.getInstance("MD5");//128位
            mdigest.update(fileAll);
            byte[] serverDigest=mdigest.digest();
            //用客户机公钥解密消息摘要，验证数字签名
            PublicKey publicKey=(PublicKey)tks.getCertificate("client").getPublicKey();//客户机公钥
            Cipher cipher=Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] unsignedDigest=cipher.doFinal(clientDigest); //解密
            //更新显示
            doc.insertString(doc.getLength(), "解密的数字签名："+new String(unsignedDigest,"UTF-8")+"\n", attr);
            if (Arrays.equals(serverDigest,unsignedDigest)) {//验证数字签名
                pw.println("M_DONE"); //回送成功信息
                //更新显示
                doc.insertString(doc.getLength(), filename+"  文件接收成功！\n", attr);
            }else {
                pw.println("M_LOST"); //回送失败信息
                //更新显示
                doc.insertString(doc.getLength(), filename+"  文件接收失败！\n", attr);
            }//end if        
        }//end if
        //关闭流
        in.close();
        out.close();
        pw.close();
        toClientSocket.close();
        return 100;
    }//end doInBackground    
}
