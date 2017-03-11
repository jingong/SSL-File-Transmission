package cn.edu.ldu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class Main {

   

    

    /**
     * 文件file进行加密并保存目标文件destFile中
     *
     * @param file 要加密的文件 如c:/test/srcFile.txt
     * @param destFile 加密后存放的文件名 如c:/加密后文件.txt
     */
    public static void encrypt(String file, String destFile) throws Exception {
        
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
        
        PublicKey publicKey=(PublicKey)tks.getCertificate("server").getPublicKey();//客户机公钥
        Cipher cipher=Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.PUBLIC_KEY, publicKey);

        InputStream is = new FileInputStream(file);
        OutputStream out = new FileOutputStream(destFile);
        CipherInputStream cis = new CipherInputStream(is, cipher);
        byte[] buffer = new byte[1024];
        int r;
        while ((r = cis.read(buffer)) > 0) {
            out.write(buffer, 0, r);
        }
        cis.close();
        is.close();
        out.close();
    }

    /**
     * 文件采用DES算法解密文件
     *
     * @param file 已加密的文件 如c:/加密后文件.txt
     *         * @param destFile 解密后存放的文件名 如c:/ test/解密后文件.txt
     */
//    public void decrypt(String file, String dest) throws Exception {
//        Cipher cipher = Cipher.getInstance("DES");
//        cipher.init(Cipher.DECRYPT_MODE, this.key);
//        InputStream is = new FileInputStream(file);
//        OutputStream out = new FileOutputStream(dest);
//        CipherOutputStream cos = new CipherOutputStream(out, cipher);
//        byte[] buffer = new byte[1024];
//        int r;
//        while ((r = is.read(buffer)) >= 0) {
//            System.out.println();
//            cos.write(buffer, 0, r);
//        }
//        cos.close();
//        out.close();
//        is.close();
//    }

    public static void main(String[] args) {
        String str = "abcdefg";
        System.out.println("截取最后一个字符串生成的新字符串为: " + str.substring(1,str.length()));
//        try {
//            encrypt("e:/upload/r.txt", "e:/upload/r解密.txt"); //加密
//            //td.decrypt("e:/upload/r解密.txt", "e:/upload/r1.txt"); //解密   
//        } catch (Exception ex) {
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//        }
//            File file = new File("e:/upload/r.txt");
//        try {
//            DoFile.encrypt(file);
//            } catch (Exception ex) {
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        File file = new File("e:/upload/r.txt");
//        try {
//            System.out.println(file.getParent());
//        } catch (Exception ex) {
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        System.out.println(file.getName());
        
    }
}
