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

  // 4 ASCII characters per 3 bytes.
  public static int getEncodedSize(int plainTextLength) {
    int whole = plainTextLength / 4 * 3;
    int partial = plainTextLength % 4;
    return whole + partial;
  }

  public static int getDecodedSize(int encodedLength) {
    return encodedLength * 4 / 3;
  }

  private final TransformFunctions transformFunctions;

  private SixBitAsciiEncoding(TransformFunctions transformFunctions) {
    this.transformFunctions = transformFunctions;
  }

  private static class EncodedInput {
    private byte[] data;
    private int maskIndex;
    private int dataIndex;

    EncodedInput(byte[] data) {
      this.data = data;
      this.dataIndex = data.length - 1;
      this.maskIndex = 0;
    }

    boolean hasNext() {
      return dataIndex >= 0;
    }

    // b0       b1       b2
    // -------- -------- 654321-- first character
    // -------- 4321---- ------65 second character
    // 21------ ----6543 -------- third character
    // --654321 -------- -------- fourth character
    //
    // e.g.
    // 0x64    |0xc1    |0x12    |0x04    | encoded bytes
    // (b2)    |(b0)    |(b1)    |(b2)    |
    // --------+--------+--------+--------+
    // 011001  |        |        |        | Y (0x25)
    //       00|        |        |        | unused (would be part of next letter)
    //         |11      |    0010|        | K (0x11) (11 from 0xc1 is low portion)
    //         |  000001|        |        | A (0x01)
    //         |        |0001    |      00| A (0x01) (0001 from 0x12 is low portion)
    //         |        |        |000001  | A (0x01)

    int next() {
      Preconditions.checkState(hasNext());
      switch(maskIndex++ % 4) {
      case 0:
        return (data[dataIndex--] >> 2) & 0x3f;
      case 1:
        int result = ((data[dataIndex + 1] << 4) & 0x30) | ((data[dataIndex] >> 4) & 0xf);
        dataIndex--;
        return result;
      case 2:
        return ((data[dataIndex + 1] << 2) & 0x3c) | ((data[dataIndex] >> 6) & 0x3);
      case 3:
        return data[dataIndex--] & 0x3f;
      default: throw new IllegalStateException();
      }
    }
  }

  public String decode(byte data[]) {
    String result = "";
    Function<Integer, Character> adjuster = transformFunctions.getDecodingTransformFunction();
    EncodedInput input = new EncodedInput(data);
    while (input.hasNext()) {
      result += adjuster.apply(input.next());
    }
    return result;
  }

  public byte[] encode(String text) {
    byte result[] = new byte[getEncodedSize(text.length())];
    int byteIndex = result.length - 1;

    Function<Character, Integer> adjuster = transformFunctions.getEncodingTransformFunction();
    for (int i = 0; i < text.length(); i += 4) {
      int adjusted[] = {
          adjuster.apply(text.charAt(i)),
          i + 1 >= text.length() ? 0 : adjuster.apply(text.charAt(i + 1)),
          i + 2 >= text.length() ? 0 : adjuster.apply(text.charAt(i + 2)),
          i + 3 >= text.length() ? 0 : adjuster.apply(text.charAt(i + 3))
      };
      result[byteIndex--] = (byte) (((adjusted[1] & 0x3F) >> 4) | ((adjusted[0] & 0x3F) << 2));
      if (byteIndex >= 0) {
        result[byteIndex--] = (byte) (((adjusted[2] & 0x3C) >> 2) | ((adjusted[1] & 0x0F) << 4));
        if (byteIndex >= 0) {
          result[byteIndex--] = (byte) (((adjusted[3] & 0x3F)) | ((adjusted[2] & 0x03) << 6));
        }
      }
    }
    return result;
  }
}