package cn.sparrowmini.keycloak.tokenpocket;

import org.bouncycastle.util.Arrays;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class SignUtil {
    public static String ecRecover(String msg, String sig) {
        byte[] signatureBytes = Numeric.hexStringToByteArray(sig);
        Sign.SignatureData signatureData = sigFromByteArray(signatureBytes);

        try {
            BigInteger recoveredKey = Sign.signedPrefixedMessageToKey(msg.getBytes(), signatureData);
            String address = "0x" + Keys.getAddress(recoveredKey);
            System.out.println(address);
            return address.toLowerCase();
        } catch (Exception e) {
            System.out.println(String.format("SignatureException, msg:{}, sig:{}, nonce:{}: ", msg, sig, "nonce", e.getMessage()));
        }
        return null;
    }

    public static Sign.SignatureData sigFromByteArray(byte[] sig) {
        if (sig.length < 64 || sig.length > 65) return null;
        byte  v = sig[64];
        if (v < 27) v += 27;

        byte[] r = Arrays.copyOfRange(sig, 0, 32);
        byte[] s = Arrays.copyOfRange(sig, 32, 64);

        return new Sign.SignatureData(v, r, s);
    }
}
