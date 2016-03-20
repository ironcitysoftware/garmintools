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

import java.nio.ByteBuffer;

import com.google.common.base.Preconditions;

/** Decorates {@link ByteBuffer} to provide for reading individual bits. */
public class BitBuffer {
  private static final int NUM_BITS_PER_BYTE = 8;

  private final ByteBuffer byteBuffer;

  // The index of the next bit to be written or read.
  private int byteIndex;
  private int bitIndex;  // 0 .. NUM_BITS_PER_BYTE - 1

  public BitBuffer(ByteBuffer byteBuffer) {
    this.byteBuffer = byteBuffer;
  }

  public void setOffset(int byteIndex, int bitIndex) {
    Preconditions.checkArgument(byteBuffer.limit() > byteIndex);
    this.byteIndex = byteIndex;
    this.bitIndex = bitIndex;
  }

  public int readNumBits(int numRequestedBits) {
    Preconditions.checkArgument(numRequestedBits > 0);
    Preconditions.checkArgument(numRequestedBits < 32);  // <= but no eof ?
    Preconditions.checkArgument(numRequestedBits <= getNumRemainingBits());
    int result = 0;
    int numReturnedBits = 0;
    while (numReturnedBits < numRequestedBits) {
      result <<= 1;
      if (readNextBit()) {
        result |= 1;
      }
      numReturnedBits++;
    }
    return result;
  }

  public int getNumRemainingBits() {
    int totalBits = byteBuffer.limit() * NUM_BITS_PER_BYTE;
    int currentPosition = byteIndex * NUM_BITS_PER_BYTE + bitIndex;
    return totalBits - currentPosition;
  }

  public boolean hasRemaining() {
    return getNumRemainingBits() > 0;
  }

  public VariableLengthEncodingForeignKey getKey() {
    return new VariableLengthEncodingForeignKey(byteIndex, bitIndex);
  }

  private boolean readNextBit() {
    Preconditions.checkState(getNumRemainingBits() > 0);
    int mask = 1 << (NUM_BITS_PER_BYTE - bitIndex - 1);
    byteBuffer.position(byteIndex);
    int result = byteBuffer.get() & mask;
    if (bitIndex == NUM_BITS_PER_BYTE - 1) {
      bitIndex = 0;
      byteIndex++;
    } else {
      bitIndex++;
    }
    return result != 0;
  }
}
