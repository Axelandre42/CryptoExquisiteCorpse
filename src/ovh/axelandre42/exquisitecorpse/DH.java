/* 
 * Copyright 2019 Alexandre Waeles
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to
 * do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ovh.axelandre42.exquisitecorpse;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Alexandre Waeles <www.axelandre42.ovh>
 *
 */
public class DH {
    
    private KeyPair pair;
    private KeyAgreement agreement;
    
    public PublicKey generatePublicKey() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
            gen.initialize(128);
            pair = gen.generateKeyPair();
            System.out.println(pair.getPublic().getAlgorithm());
            return pair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException("Exception during public key generation.", e);
        }
    }
    
    public SecretKey computeSharedKey(PublicKey key) {
        try {
            agreement = KeyAgreement.getInstance("ECDH");
            agreement.init(pair.getPrivate());
            
            agreement.doPhase(key, true);
            
            return new SecretKeySpec(agreement.generateSecret(), "AES");
        } catch (Exception e) {
            throw new RuntimeException("Exception during shared key compute.", e);
        }
    }
}
