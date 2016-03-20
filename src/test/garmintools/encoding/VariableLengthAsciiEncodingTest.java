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
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Booleans;
import com.google.common.primitives.Bytes;

public class VariableLengthAsciiEncodingTest {
  private static final byte[] TEST_DATA = Bytes.toArray(ImmutableList.of(
      0xca, 0x0b, 0xa0, 0x91, 0xb2, 0x94, 0x17, 0x41,
      0x23, 0xe2, 0x73, 0x85, 0xca, 0x0b, 0xa0, 0x91,
      0x74, 0x9c));

  @Test
  public void testDecode() {
    VariableLengthAsciiEncoding vls = new VariableLengthAsciiEncoding(ByteBuffer.wrap(TEST_DATA));
    assertEquals("LOGAN CO", vls.decode(0, 6));
    assertEquals("LOGAN FIELD", vls.decode(5, 5));
  }

  @Test
  public void testDecodeTime() {
    VariableLengthAsciiEncoding vls = new VariableLengthAsciiEncoding(
        ByteBuffer.wrap(Bytes.toArray(ImmutableList.of(
            0x01, 0x55, 0x4f, 0x8c, 0xa8, 0x9f, 0xb8, 0xfa, 0x7d, 0x9f, 0xbf, 0x4f, 0xa7,
            0xe3, 0xe9, 0xf4, 0xfe, 0xfd, 0x3e, 0x92))));
    assertEquals("MON-SUN: 06:00-00:00", vls.decode(0, 7));
  }

  @Test
  public void testDecodeStrings() {
    VariableLengthAsciiEncoding vls = new VariableLengthAsciiEncoding(
        ByteBuffer.wrap(Bytes.toArray(ImmutableList.of(
            0x89, 0x7d, 0xaf, 0xf7, 0xf8, 0xfa, 0xfd, 0x3e, 0x9f, 0x41, 0x7d, 0xaf, 0xf7, 0xf8))));
    assertEquals(" ", vls.decode());
    assertEquals("(D)-3002", vls.decode());
  }

  @Test
  public void testEncode() {
    BitListOutputStream outputStream = new BitListOutputStream();
    outputStream.writeBits(Booleans.asList(false, false, false, false, false, false));  // TEST_DATA begins at bit 7
    VariableLengthAsciiEncoding vls = new VariableLengthAsciiEncoding(outputStream);
    vls.encode("LOGAN CO");
    assertArrayEquals(Bytes.toArray(ImmutableList.of(
        0x02, 0x0b, 0xa0, 0x91, 0xb2, 0x90)), outputStream.toByteArray());
    vls.encode("LOGAN FIELD");
    assertArrayEquals(Bytes.toArray(ImmutableList.of(
        0x02, 0x0b, 0xa0, 0x91, 0xb2, 0x94, 0x17, 0x41,
        0x23, 0xe2, 0x73, 0x85, 0xc8)), outputStream.toByteArray());
  }

  @Test
  public void testEncodeFailure() {
    BitListOutputStream outputStream = new BitListOutputStream();
    VariableLengthAsciiEncoding vls = new VariableLengthAsciiEncoding(outputStream);
    try {
      vls.encode("a");
      fail();
    } catch (NullPointerException e) {
      // expected
    }
    try {
      vls.encode(":");
      fail();
    } catch (NullPointerException e) {
      // expected
    }
    vls.encodeExtended(":");
  }
}
