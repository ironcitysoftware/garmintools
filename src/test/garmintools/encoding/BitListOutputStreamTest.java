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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.google.common.primitives.Booleans;

public class BitListOutputStreamTest {
  @Test
  public void testWriteFirstBit() throws IOException {
    BitListOutputStream outputStream = new BitListOutputStream(0 /* initial capacity */);
    outputStream.writeBits(Booleans.asList(true));
    assertArrayEquals(new byte[] { (byte) 0x80 }, outputStream.toByteArray());
    assertEquals(1, outputStream.getBitPosition());
    assertEquals(0, outputStream.getBytePosition());
    outputStream.close();
  }

  @Test
  public void testWriteNineBits() throws IOException {
    BitListOutputStream outputStream = new BitListOutputStream(0 /* initial capacity */);
    outputStream.writeBits(Booleans.asList(true, true, true, true, true, true, true, true, true));
    assertArrayEquals(new byte[] { (byte) 0xff, (byte) 0x80 }, outputStream.toByteArray());
    assertEquals(1, outputStream.getBitPosition());
    assertEquals(1, outputStream.getBytePosition());
    outputStream.close();
  }

  @Test
  public void testWriteEightThenOneBit() throws IOException {
    BitListOutputStream outputStream = new BitListOutputStream(0 /* initial capacity */);
    outputStream.writeBits(Booleans.asList(true, true, true, true, true, true, true, true));
    assertArrayEquals(new byte[] { (byte) 0xff }, outputStream.toByteArray());
    assertEquals(0, outputStream.getBitPosition());
    assertEquals(1, outputStream.getBytePosition());

    outputStream.writeBits(Booleans.asList(true));
    assertArrayEquals(new byte[] { (byte) 0xff, (byte) 0x80 }, outputStream.toByteArray());
    assertEquals(1, outputStream.getBitPosition());
    assertEquals(1, outputStream.getBytePosition());
    outputStream.close();
  }
}
