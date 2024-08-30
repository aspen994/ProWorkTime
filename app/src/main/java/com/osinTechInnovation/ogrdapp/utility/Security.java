package com.osinTechInnovation.ogrdapp.utility;

import android.os.Build;
import android.text.TextUtils;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Security {
    public static final String TAG = "IABUtil/Security";
    public static final String KEY_FACTORY_ALGORITHM = "RSA";
    public static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

    public static boolean verifyPurchase(String base64PublicKey, String signedData,
                                         String signature) throws IOException{
        if(TextUtils.isEmpty(signedData) || TextUtils.isEmpty(base64PublicKey)){
            return false;
        }

        PublicKey key = generatePublicKey(base64PublicKey);
        return verify (key,signedData,signature);
    }

    public static PublicKey generatePublicKey(String encodedPublicKey) throws  IOException{
        try {
            byte[] decodeKey = new byte[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                decodeKey = Base64.getDecoder().decode(encodedPublicKey);
            }
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return keyFactory.generatePublic(new X509EncodedKeySpec(decodeKey));
        }
        catch (NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
        catch (InvalidKeySpecException e){
            String msg = "Ivalid key specifation: " + e;
            throw  new IOException(msg);
        }
    }

    public static boolean verify (PublicKey publicKey,String signedData,String signature){
        byte[] signatureBytes = new byte[0];
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                signatureBytes = Base64.getDecoder().decode(signature);
            }
        }catch(IllegalArgumentException e){
            return false;
        }
        try {
            Signature signatureAlgorithm = Signature.getInstance(SIGNATURE_ALGORITHM);
            signatureAlgorithm.initVerify(publicKey);
            signatureAlgorithm.update(signedData.getBytes());
            if(!signatureAlgorithm.verify(signatureBytes)){
                return false;
            }
            return true;
        }
        catch (NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        } catch (SignatureException e) {

        } catch (InvalidKeyException e) {

        }
        return false;

    }
}
