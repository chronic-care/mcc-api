/*Copyright 2021 Cognitive Medical Systems*/
package com.cognitive.nih.niddk.mccapi.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;

import org.junit.jupiter.api.Test;


class FHIRHelperTest {

    @Test
    void dateTimeToString() throws IOException, GeneralSecurityException {
    	PrivateKeyReader p =  new PrivateKeyReader("/Users/seanmuir/mccall/oAuth2/privatekey.pem");
    	
    	System.out.println( p.getPrivateKey().toString());
    	
    }

    @Test
    void generateJWT() {
    	
////    	   String SECRET_KEY = "oeRaYY7Wo24sDqKSX3IM9ASGmdGPmkTd9jo1QTy4b7P9Ze5_9hKolVX8xNrQDcNRfVEdTZNOuOyqEGhXEbdJI-ZQ19k_o9MI0y3eZN2lp9jow55FfXMiINEdt1XR85VipRLSOkT6kSpzs2x-jbLDiz9iFVzkd81YKxMgPA7VfZeQUm4n-mOmnWMaVX30zGFU4L3oPBctYKkl4dYfqYWqRNfrgPJVi5DGFjywgxx0ASEiJHtV72paI3fDR2XwlSkyhhmY-ICjCRmsJN4fX1pdoL8a18-aQrvyu4j0Os6dVPYIoPvvY0SAZtWYKHfM15g7A3HD4cVREf9cUsprCRK93w";
//
//    	String SECRET_KEY = "MIIEpAIBAAKCAQEAsZ1ARmAaEJ/Xn0dJFHpvC3k5c4G1iLuTJ9CNnyZ22T5rrUzx"
//    			+ "uB55JppF1hQI9W0S396Ov06n5o2mJCNGOWgZCedGME/HuVPWWCWDMrKIptcJxQUN"
//    			+ "gJuUAowKctjYc2IluwGg2mG0EER3m3SZrSlxXMJYwfXXc3X+Pj7x94Wmq3VWPFhm"
//    			+ "jN7/zW31NAq0FwDRaJHn8Jwu2fOCGnnpOF63uDwVpq19O0CpEg498CyU0RTBuE4F"
//    			+ "wmDqlELZFPGRZ0eA0TMNoJ6DN4ci6hT9Pv2+n1rb3PfimI9RT6pwDxvpJjePWZx+"
//    			+ "rAABr7EmiY+zcmv/7pZzoRZNBJjKqzi+7NrERwIDAQABAoIBAESjDimkDa6K9pp4"
//    			+ "w8cEIVF/wTHf2DVEt3NVZsUv6hG9y4KnD2olbI/8Yix+hY/CXN7idKt9S+kOqDui"
//    			+ "3gplLffjxf35dqcpvNVHedHnqGNBCmd2smcWuDXbxFXpcov0S7xevrhltV3r94S9"
//    			+ "pH2EXGiKOh6KUH4wBNYtIPiUT3pGMiY6mkzx6Hi5q9IJRG6NPb6+1Mwgg0fl89rt"
//    			+ "8DHOybzDO82qfRQuO2y9JNqZmfaMmqLh1fBwnTX2a0SqnfTskMr3ATenBuwwZVsp"
//    			+ "Mdk5FzxXAUSHRxu8MvakgCb5NWGUEN1eDS/NrqdJEF3yhUUknCLX62dBCey6UVC+"
//    			+ "dUwq2zECgYEA7EZuXE6aqyHiNkuRkaXA7lJzLjsvyCRgr/zsYHajoIkIWE5ylRDJ"
//    			+ "Chbni8eTaEA8cbDgenH9X82gBGyxZd1IG/P3tbyifv+W1okMIbmVo/VLZfrSJFeR"
//    			+ "FkvG+VvUj9pvegSFMF7aU0lnGCLtq05j4+zX871oHFXqwU6tZGKGbyUCgYEAwHEl"
//    			+ "SfMZrhnYUeJWLPoY96/hRbKKGuTJWMm848suobtVDO1sjQDjnvN2kzCPGcNW+fkc"
//    			+ "JQ6J2wyg/6/aaiyNYGGCdnN6UpXKPEqRkhdMe+P/g4OR2AT/dOgJKMBTIaHjgzGk"
//    			+ "eKLLkn2+PkiFkzZuBwcZ1a0CyKa2TfxBixLUL/sCgYEAjhTkdJnIJLThhWj4Bq+4"
//    			+ "TxhBr0FkQGSvx+S+eFuflgARhpjWCpHncyvG3aWMHRrTvkJxGvDFPwE2ArMiEnF4"
//    			+ "1WZmRWFEDj7AhA4xv24McGYnixHDoJsP4mp7IqR4NHFUq/lf5nCDtaXkWv1WEre3"
//    			+ "Fe7tBeOWYcPAwbIATud4lAkCgYA200VLVx3W0vglA/WHJM1eK1Zjk+TymoGecQMG"
//    			+ "ZslnPEaTw0Rsu3cdvsDmWCw8q8EowEa7a/PsWyG3FnOEUiMI9V7Evkt4IcKXAKuO"
//    			+ "UmLw3Njbr+0p/OqGdOC/ImPMriFyK/9uevrCRl6fweuEMDqSd/xtOrgTUCOZ/iby"
//    			+ "JzAaZwKBgQDqzWyccl/CxR09MmTrHQ1kBi4XMbQKb94f6mqeYkBUT6JitqC1S4yl"
//    			+ "Tzd5gFzt/zkKmfRf3TKi3MpAxygeONTetYpa6BviCV5YUpq6OYa9gkKK4JEO3peD"
//    			+ "dZHu2NXNWjmh8DURov4kncHO8q6Wp+Mo/hxHw7i+Fz1DQaLQLPEA4w==";
//    	
//    	  //The JWT signature algorithm we will be using to sign the token
//        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS384;
//
////        String SECRET_KEY;
//		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
//        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
//
//    	
//    	// We need a signing key, so we'll create one just for this example. Usually
//    	// the key would be read from your application configuration instead.
////    	Key key = Keys.secretKeyFor(SignatureAlgorithm.RS384);
//
//    	String jws = Jwts.builder().claim("aaa", "yyy").signWith(signatureAlgorithm, signingKey).compact();
//    	System.out.println(jws);

    }
    
}
