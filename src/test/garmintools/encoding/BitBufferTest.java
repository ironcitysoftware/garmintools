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

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Bytes;

public class BitBufferTest {
  private static final byte[] TEST_DATA =
      Bytes.toArray(ImmutableList.of(0xca, 0x0b, 0xa0, 0x91, 0xb2, 0x94, 0x17, 0x41));

  private final ByteBuffer byteBuffer;

  public BitBufferTest() {
    byteBuffer = ByteBuffer.allocate(TEST_DATA.length);
    for (byte b : TEST_DATA) {
      byteBuffer.put(b);
    }
    byteBuffer.flip();
  }

  @Test
  public void testReadBitsWithOffset() {
    BitBuffer bitBuffer = new BitBuffer(byteBuffer);
    bitBuffer.setOffset(0, 2);
    assertEquals(2, bitBuffer.readNumBits(4));
    assertEquals(58, bitBuffer.getNumRemainingBits());
  }

  @Test
  public void testReadNineBits() {
    BitBuffer bitBuffer = new BitBuffer(byteBuffer);
    bitBuffer.setOffset(0, 1);
    assertEquals(0x128, bitBuffer.readNumBits(9));
    assertEquals(54, bitBuffer.getNumRemainingBits());
  }
}
