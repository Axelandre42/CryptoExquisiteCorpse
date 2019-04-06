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
package ovh.axelandre42.exquisitecorpse.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexandre Waeles <www.axelandre42.ovh>
 *
 */
public class BiKeyMap<A, B, V> {
    private Map<A, V> values = new HashMap<>();
    private Map<B, A> keyMap = new HashMap<>();
    
    public void put(A keyA, B keyB, V value) {
        keyMap.put(keyB, keyA);
        values.put(keyA, value);
    }
    
    public V getFromA(A key) {
        return values.get(key);
    }
    
    public V getFromB(B key) {
        return values.get(keyMap.get(key));
    }
    
    public boolean containsKeyA(A key) {
        return values.containsKey(key);
    }
    
    public boolean containsKeyB(B key) {
        return keyMap.containsKey(key);
    }
    
    public int size() {
        return values.size();
    }
    
    public Collection<V> values() {
        return values.values();
    }
    
    public Collection<A> keysA() {
        return values.keySet();
    }
    
    public Collection<B> keysB() {
        return keyMap.keySet();
    }
}
