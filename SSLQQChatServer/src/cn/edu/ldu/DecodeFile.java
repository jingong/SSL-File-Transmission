package cn.edu.ldu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

public class DecodeFile {
        public static void decrypt(File file, KeyStore ks) throws Exception {
        PrivateKey privateKey=(PrivateKey)ks.getKey("server", "123456".toCharArray());
        Cipher cipher=Cipher.getInstance("RSA/ECB/PKCS1Padding"); //“算法/模式/填充”
        cipher.init(Cipher.PRIVATE_KEY, privateKey);//初始化工作模式
        InputStream is = new FileInputStream(file);
        OutputStream out = new FileOutputStream(file.getParent() + "/L" + file.getName());
        CipherOutputStream cos = new CipherOutputStream(out, cipher);
        byte[] buffer = new byte[1024];
        int r;
        while ((r = is.read(buffer)) >= 0) {
            System.out.println();
            cos.write(buffer, 0, r);
        }
        cos.close();
        out.close();
        is.close();
    }
}
