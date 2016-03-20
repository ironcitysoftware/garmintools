/**
 *    Copyright 2016 Iron City Software LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package garmintools.encoding;

import garmintools.keys.VariableLengthEncodingForeignKey;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

/** Decorates {@link ByteArrayOutputStream} to write bits. */
public class BitListOutputStream extends ByteArrayOutputStream {
  private static final int NUM_BITS_PER_BYTE = 8;

  // The bit about to be written.  0 .. NUM_BITS_PER_BYTE (if about to write bit 0 of next byte).
  private int bitIndex = 0;

  public BitListOutputStream() {
    super();
  }

  public BitListOutputStream(int initialCapacity) {
    super(initialCapacity);
  }

  public synchronized void writeBits(List<Boolean> bitList) {
    for (int i = bitList.size() - 1; i >= 0; i--) {
      writeBit(bitList.get(i));
    }
  }

  public int getBitPosition() {
    return bitIndex == NUM_BITS_PER_BYTE ? 0 : bitIndex;
  }

  public int getBytePosition() {
    return bitIndex == NUM_BITS_PER_BYTE ? count : count - 1;
  }

  public VariableLengthEncodingForeignKey getKey() {
    return new VariableLengthEncodingForeignKey(getBytePosition(), getBitPosition());
  }

  private void writeBit(boolean bitValue) {
    // super.count is the number of valid bytes in the buffer.
    if (buf.length - count == 0) {
      buf = Arrays.copyOf(buf, Math.max(buf.length << 1, count + 1));
    }
    if (count == 0 || bitIndex == NUM_BITS_PER_BYTE) {
      buf[count++] = (byte) 0;
      bitIndex = 0;
    }
    int mask = 1 << (NUM_BITS_PER_BYTE - bitIndex - 1);
    if (bitValue) {
      buf[count - 1] |= mask;
    } else {
      buf[count - 1] &= ~mask;
    }
    bitIndex++;
  }
}
