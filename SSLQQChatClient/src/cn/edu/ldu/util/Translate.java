package cn.edu.ldu.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 功能：对象的序列化和反序列化
 * 作者：董相志，版权所有2016--2018，upsunny2008@163.com
 */
public class Translate {
    //对象反序列化
    public static Object ByteToObject(byte[] bytes) {
        Object obj=null;
        try {
            ByteArrayInputStream bi=new ByteArrayInputStream(bytes); //字节数组输入流
            ObjectInputStream oi=new ObjectInputStream(bi); //对象输入流
            obj=oi.readObject(); //转为对象
        }catch(IOException | ClassNotFoundException ex) { }
        return obj;
    }
    //对象序列化
    public static byte[] ObjectToByte(Object obj) {
       byte[] bytes=null;
        try {
            ByteArrayOutputStream bo=new ByteArrayOutputStream(); //字节数组输出流
            ObjectOutputStream oo=new ObjectOutputStream(bo); //对象输出流
            oo.writeObject(obj); //输出对象
            bytes=bo.toByteArray(); //对象序列化
        }catch(IOException ex) {}
        return bytes;
    }    
}
