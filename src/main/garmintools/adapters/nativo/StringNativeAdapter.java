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

package garmintools.adapters.nativo;

import garmintools.encoding.BitBuffer;
import garmintools.encoding.BitListOutputStream;
import garmintools.encoding.VariableLengthAsciiEncoding;
import garmintools.keys.VariableLengthEncodingForeignKey;
import garmintools.sections.DataLengthSection;
import garmintools.wrappers.StringAndKey;
import garmintools.wrappers.TableOfContentsEntry;

import java.nio.ByteBuffer;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class StringNativeAdapter implements NativeAdapter<List<String>> {
  public static class NativeOutputAndKeys extends NativeOutput {
    public final List<VariableLengthEncodingForeignKey> keys;
    public NativeOutputAndKeys(byte data[], List<VariableLengthEncodingForeignKey> keys) {
      super(data);
      this.keys = keys;
    }
  }

  @Override
  public List<String> read(DataLengthSection dataLengthSection, TableOfContentsEntry entry, ByteBuffer byteBuffer) {
    throw new UnsupportedOperationException("Must use readWithKeys");
  }

  public List<StringAndKey> readWithKeys(DataLengthSection dataLengthSection, TableOfContentsEntry entry, ByteBuffer byteBuffer) {
    VariableLengthAsciiEncoding encoding = new VariableLengthAsciiEncoding(byteBuffer);
    ImmutableList.Builder<StringAndKey> listBuilder = ImmutableList.builder();
    BitBuffer bitBuffer = encoding.getBitBuffer();
    while (encoding.hasRemaining()) {
      VariableLengthEncodingForeignKey key = bitBuffer.getKey();
      listBuilder.add(new StringAndKey(encoding.decode(), key));
    }
    return listBuilder.build();
  }

  @Override
  public NativeOutput write(List<String> strings) {
    BitListOutputStream bitListOutputStream = new BitListOutputStream();
    VariableLengthAsciiEncoding encoding = new VariableLengthAsciiEncoding(bitListOutputStream);
    ImmutableList.Builder<VariableLengthEncodingForeignKey> listBuilder = ImmutableList.builder();
    for (String string : strings) {
      listBuilder.add(bitListOutputStream.getKey());
      encoding.encodeExtended(string);
    }
    return new NativeOutputAndKeys(bitListOutputStream.toByteArray(), listBuilder.build());
  }
}
