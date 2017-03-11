package cn.edu.ldu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.PublicKey;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

public class DoFile {
    public static File encrypt(File file,KeyStore tks) throws Exception {
        PublicKey publicKey=(PublicKey)tks.getCertificate("server").getPublicKey();//客户机公钥
        Cipher cipher=Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.PUBLIC_KEY, publicKey);
        InputStream is = new FileInputStream(file);
        File newFile = new File(file.getParent() + "/T" + file.getName());
        OutputStream out = new FileOutputStream(newFile);
        CipherInputStream cis = new CipherInputStream(is, cipher);
        byte[] buffer = new byte[1024];
        int r;
        while ((r = cis.read(buffer)) > 0) {
            out.write(buffer, 0, r);
        }
        cis.close();
        is.close();
        out.close();
        return newFile;
    }
}
