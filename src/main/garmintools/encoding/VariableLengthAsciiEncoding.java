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

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class VariableLengthAsciiEncoding {
  private final BitListOutputStream outputStream;  // for writing.
  private final BitBuffer bitBuffer;  // for reading.
  private boolean isReadMode;

  private static final class EncodedLength {
    final int encodedBitLength;
    final int minValue;
    final int maxValue;
    final byte encodedCharacters[];

    private EncodedLength(int encodedBitLength, int minValue, int maxValue, byte encodedCharacters[]) {
      this.encodedBitLength = encodedBitLength;
      this.minValue = minValue;
      this.maxValue = maxValue;
      this.encodedCharacters = encodedCharacters;
      Preconditions.checkArgument(encodedCharacters.length == (maxValue - minValue + 1),
          String.format("%d != %d", encodedCharacters.length, (maxValue - minValue + 1)));
    }
  }

  // Airport names and locations support a basic set of characters.
  private static final List<EncodedLength> ENCODED_LENGTHS = ImmutableList.of(
      new EncodedLength(3,   0x0,   0x0, new byte[] { 'A' }),
      new EncodedLength(4,   0x2,   0x7, new byte[] { 0x0, 'E', 'N', 'O', 'R', 'I' }),
      new EncodedLength(5,  0x10,  0x1d, new byte[] { 'L', ' ', 'S', 'T', 'U', 'M', 'C',
                                                      'D', 'H', 'B', 'G', 'K', 'P', 'Y'}),
      new EncodedLength(9, 0x1e0, 0x1f0, new byte[] { 'V', 'W', 'F', 'J', 'Z', 'X', 'Q',
                                                      '1', '2', '0', '4', '3', '6', '7',
                                                      '9', '8', '5' }));

  // Narrative text in communication frequencies supports additional characters.
  private static final List<EncodedLength> EXTENDED_ENCODED_LENGTHS = ImmutableList.<EncodedLength>builder()
      .addAll(ENCODED_LENGTHS.subList(0, 3))
      .add(new EncodedLength(9, 0x1e0, 0x1fb, new byte[] { 'V', 'W', 'F', 'J', 'Z', 'X', 'Q',
                                                           '1', '2', '0', '4', '3', '6', '7',
                                                           '9', '8', '5', '-', '/', ',', '.',
                                                           '&', '(', ')', '+', ';', '\'', ':' }))
      .build();

  // BitSet would be nice but it is unable to store a specific length of only false bits. */
  private static final Map<Byte, List<Boolean>> ENCODED_BITS = invertEncodedLengths(ENCODED_LENGTHS);
  private static final Map<Byte, List<Boolean>> EXTENDED_ENCODED_BITS = invertEncodedLengths(EXTENDED_ENCODED_LENGTHS);

  private static Map<Byte, List<Boolean>> invertEncodedLengths(List<EncodedLength> encodedLengths) {
    ImmutableMap.Builder<Byte, List<Boolean>> mapBuilder = ImmutableMap.builder();
    for (EncodedLength encodedLength : encodedLengths) {
      for (int i = 0; i < encodedLength.encodedCharacters.length; ++i) {
        mapBuilder.put(encodedLength.encodedCharacters[i],
            bitListOf(encodedLength.encodedBitLength, encodedLength.minValue + i));
      }
    }
    return mapBuilder.build();
  }

  private static List<Boolean> bitListOf(int length, int value) {
    ImmutableList.Builder<Boolean> listBuilder = ImmutableList.builder();
    for (int i = 0; i < length; i++) {
      listBuilder.add((value & (1 << i)) > 0);
    }
    return listBuilder.build();
  }

  /** Construct an encoding for reading. */
  public VariableLengthAsciiEncoding(ByteBuffer byteBuffer) {
    this.bitBuffer = new BitBuffer(byteBuffer);
    this.outputStream = null;
    this.isReadMode = true;
  }

  /** Construct an encoding for writing. */
  public VariableLengthAsciiEncoding(BitListOutputStream outputStream) {
    this.bitBuffer = null;
    this.outputStream = outputStream;
    this.isReadMode = false;
  }

  public BitBuffer getBitBuffer() {
    return bitBuffer;
  }

  public boolean hasRemaining() {
    /** At a minimum a 0x0 must be encoded, which takes 4 bits. */
    return bitBuffer.getNumRemainingBits() > 3;
  }

  public boolean isReadMode() {
    return isReadMode;
  }

  public void encode(String text) {
    for (byte b : text.getBytes()) {
      outputStream.writeBits(Preconditions.checkNotNull(ENCODED_BITS.get(b), "Not encodeable: " + b));
    }
    outputStream.writeBits(ENCODED_BITS.get((byte) 0));
  }

  public void encodeExtended(String text) {
    for (byte b : text.getBytes()) {
      outputStream.writeBits(Preconditions.checkNotNull(EXTENDED_ENCODED_BITS.get(b), "Not encodeable: " + b));
    }
    outputStream.writeBits(EXTENDED_ENCODED_BITS.get((byte) 0));
  }

  public String decode(int byteIndex, int bitIndex) {
    bitBuffer.setOffset(byteIndex, bitIndex);
    return decode();
  }

  public String decode() {
    String result = "";
    char c;
    while ((c = getNextCharacter()) != 0) {
      result += c;
    }
    return result;
  }

  private char getNextCharacter() {
    int currentBits = 0;
    int numCurrentBits = 0;
    int encodedLengthIndex = 0;
    while (encodedLengthIndex < EXTENDED_ENCODED_LENGTHS.size()) {
      EncodedLength encodedLength = EXTENDED_ENCODED_LENGTHS.get(encodedLengthIndex);
      int numBitsToFetch = encodedLength.encodedBitLength - numCurrentBits;
      currentBits <<= numBitsToFetch;
      currentBits |= bitBuffer.readNumBits(numBitsToFetch);
      numCurrentBits += numBitsToFetch;
      if (currentBits >= encodedLength.minValue
          && currentBits <= encodedLength.maxValue) {
        return (char) (encodedLength.encodedCharacters[currentBits - encodedLength.minValue] & 0xff);
      }
      encodedLengthIndex++;
    }
    throw new IllegalStateException(String.format("Failed to decode %x", currentBits));
  }
}
