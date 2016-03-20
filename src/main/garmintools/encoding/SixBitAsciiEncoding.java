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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

public class SixBitAsciiEncoding {
  private interface TransformFunctions {
    public Function<Integer, Character> getDecodingTransformFunction();
    public Function<Character, Integer> getEncodingTransformFunction();
  }

  private static final TransformFunctions SIMPLE_TRANSFORMS = new TransformFunctions() {
    @Override
    public Function<Integer, Character> getDecodingTransformFunction() {
      return new Function<Integer, Character>() {
        @Override
        public Character apply(Integer value) {
          return (char) (value + 0x20);
        }
      };
    }

    @Override
    public Function<Character, Integer> getEncodingTransformFunction() {
      return new Function<Character, Integer>() {
        @Override
        public Integer apply(Character value) {
          return value - 0x20;
        }
      };
    }
  };

  private static final TransformFunctions COMPLEX_TRANSFORMS = new TransformFunctions() {
    @Override
    public Function<Integer, Character> getDecodingTransformFunction() {
      return new Function<Integer, Character>() {
        @Override
        public Character apply(Integer value) {
          if (value == 0) {
            value = 0x20;   /* space */
          } else if (value >= 1 && value <= 0x1a) {
            value += 0x40;  /* A .. Z */
          } else if (value >= 0x20 && value <= 0x29) {
            value += 0x10;  /* 0 .. 9 */
          } else {
            throw new IllegalStateException("Unexpected value " + value);
          }
          return (char) value.intValue();
        }
      };
    }

    @Override
    public Function<Character, Integer> getEncodingTransformFunction() {
      return new Function<Character, Integer>() {
        @Override
        public Integer apply(Character value) {
          if (value == 0x20) {
            return 0;
          } else if (value >= 'A' && value <= 'Z') {
            return value - 0x40;
          } else if (value >= '0' && value <= '9') {
            return value - 0x10;
          } else {
            throw new IllegalStateException("Unexpected character " + value);
          }
        }
      };
    }
  };

  public static final SixBitAsciiEncoding SIMPLE_ENCODING = new SixBitAsciiEncoding(SIMPLE_TRANSFORMS);
  public static final SixBitAsciiEncoding COMPLEX_ENCODING = new SixBitAsciiEncoding(COMPLEX_TRANSFORMS);

  // 4 ASCII characters per 3 bytes.  Has to be a multiple of 3.
  public static int getEncodedSize(int plainTextLength) {
    int byteSize = (plainTextLength + 1) * 3 / 4;
    return Math.round((byteSize + 2) / 3) * 3;
  }

  public static int getDecodedSize(int encodedLength) {
    return encodedLength * 4 / 3;
  }

  private final TransformFunctions transformFunctions;

  private SixBitAsciiEncoding(TransformFunctions transformFunctions) {
    this.transformFunctions = transformFunctions;
  }

  public String decode(byte data[]) {
    Preconditions.checkState(data.length % 3 == 0);
    String result = "";

    // b0       b1       b2
    // -------- -------- 654321-- first character
    // -------- 4321---- ------65 second character
    // 21------ ----6543 -------- third character
    // --654321 -------- -------- fourth character

    Function<Integer, Character> adjuster = transformFunctions.getDecodingTransformFunction();
    for (int i = 0; i < data.length; i += 3) {
      String segment = ""
          + adjuster.apply((data[i + 2] >> 2) & 0x3f)
          + adjuster.apply(((data[i + 2] << 4) & 0x30) | ((data[i + 1] >> 4) & 0xf))
          + adjuster.apply(((data[i + 1] << 2) & 0x3c) | ((data[i] >> 6) & 0x3))
          + adjuster.apply(data[i] & 0x3f);
      result = segment + result;
    }
    return result;
  }

  public byte[] encode(String text) {
    byte result[] = new byte[getEncodedSize(text.length())];
    int byteIndex = 0;

    Function<Character, Integer> adjuster = transformFunctions.getEncodingTransformFunction();
    for (int i = text.length() - 1; i >= 0; i -= 4) {
      int encoded[] = {
          i - 3 < 0 ? 0 : adjuster.apply(text.charAt(i - 3)),
          i - 2 < 0 ? 0 : adjuster.apply(text.charAt(i - 2)),
          i - 1 < 0 ? 0 : adjuster.apply(text.charAt(i - 1)),
          adjuster.apply(text.charAt(i))
      };
      result[byteIndex++] = (byte) (((encoded[3] & 0x3F))      | ((encoded[2] & 0x03) << 6));
      result[byteIndex++] = (byte) (((encoded[2] & 0x3C) >> 2) | ((encoded[1] & 0x0F) << 4));
      result[byteIndex++] = (byte) (((encoded[1] & 0x3F) >> 4) | ((encoded[0] & 0x3F) << 2));
    }
    return result;
  }
}
