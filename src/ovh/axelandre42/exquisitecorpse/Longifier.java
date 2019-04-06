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

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Alexandre Waeles <www.axelandre42.ovh>
 *
 */
public class Longifier {
    private byte[] bytes;
    private int index;
    
    public Longifier(byte[] bytes) {
        this.bytes = bytes;
        this.index = 0;
    }
    
    public boolean hasNext() {
        int preoffset = bytes.length - (7 * index);
        return preoffset > 0 ? true : false;
    }
    
    public long nextLong() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        int preoffset = bytes.length - (7 * index);
        byte offset = (byte) (preoffset > 7 ? 7 : preoffset);
        buffer.put((byte) (7 - offset));
        byte[] b = new byte[7 - offset];
        Arrays.fill(b, (byte) 0);
        buffer.put(b);
        buffer.put(bytes, 7 * index, offset);
        buffer.flip();
        index++;
        return buffer.getLong();
    }
}
