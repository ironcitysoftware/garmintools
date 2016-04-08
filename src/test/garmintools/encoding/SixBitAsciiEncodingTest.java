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

import static garmintools.encoding.SixBitAsciiEncoding.COMPLEX_ENCODING;
import static garmintools.encoding.SixBitAsciiEncoding.SIMPLE_ENCODING;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Bytes;

public class SixBitAsciiEncodingTest {
  @Test
  public void testRemainderZero() {
    byte b[] = Bytes.toArray(ImmutableList.<Integer>of(
        0x6e, 0x48, 0xcf, 0xa9, 0x8b, 0x9e, 0x66, 0x18, 0xbe));
    assertEquals("OAAFGHNISTAN", SIMPLE_ENCODING.decode(b));  // sic
    assertArrayEquals(b, SIMPLE_ENCODING.encode("OAAFGHNISTAN"));  // sic
  }

  @Test
  public void testRemainderZeroB() {
    byte b[] = Bytes.toArray(ImmutableList.of(
        0x61, 0x2a, 0x86, 0x72, 0x08, 0xa4, 0x64, 0x1d, 0xce, 0x80,
        0x1b, 0xde, 0x32, 0x1a, 0xba));
    assertEquals("NAHRWAN SAUDI ARABIA", SIMPLE_ENCODING.decode(b));
    assertArrayEquals(b, SIMPLE_ENCODING.encode("NAHRWAN SAUDI ARABIA"));
  }

  @Test
  public void testRemainderTwo() {
    byte b[] = Bytes.toArray(ImmutableList.of(
        0x55, 0xd4, 0x40, 0x57, 0x04, 0x91, 0x8d, 0x05, 0x41));
    assertEquals("006-D0170-15", SIMPLE_ENCODING.decode(b));
    assertArrayEquals(b, SIMPLE_ENCODING.encode("006-D0170-15"));
  }

  @Test
  public void testAirportIdentifierAl05() {
    byte b[] = Bytes.toArray(ImmutableList.of(0x25, 0xc8, 0x4));
    assertEquals("AL05", COMPLEX_ENCODING.decode(b));
    assertArrayEquals(b, COMPLEX_ENCODING.encode("AL05"));
  }

  @Test
  public void testAirportIdentifierA05() {
    byte b[] = Bytes.toArray(ImmutableList.of(0x25, 0x18, 0x0));
    assertEquals(" A05", COMPLEX_ENCODING.decode(b));
    assertArrayEquals(b, COMPLEX_ENCODING.encode(" A05"));
  }

  @Test
  public void testAirportIdentifierA05B() {
    byte b[] = Bytes.toArray(ImmutableList.of(0x40, 0x9, 0x6));
    assertEquals("A05 ", COMPLEX_ENCODING.decode(b));
    assertArrayEquals(b, COMPLEX_ENCODING.encode("A05 "));
  }

  @Test
  public void testIcaoRegionAlbania() {
    byte b[] = Bytes.toArray(ImmutableList.of(0x41, 0xe2, 0x4, 0x2, 0x13, 0x4, 0xc, 0x0, 0x0));
    assertEquals("   LAALBANIA", COMPLEX_ENCODING.decode(b));
    assertArrayEquals(b, COMPLEX_ENCODING.encode("   LAALBANIA"));
  }

  @Test
  public void testFourBytes() {
    byte b[] = Bytes.toArray(ImmutableList.of(0x64, 0xc1, 0x12, 0x04));
    assertEquals("AAKAY", COMPLEX_ENCODING.decode(b));
    assertEquals(4, SixBitAsciiEncoding.getEncodedSize("AAKAY".length()));
    assertArrayEquals(b, COMPLEX_ENCODING.encode("AAKAY"));
  }
}
