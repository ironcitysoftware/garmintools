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

import garmintools.sections.DataLengthSection;
import garmintools.wrappers.TableOfContentsEntry;

import java.nio.ByteBuffer;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class LandingFacilityIdentifierIndexNativeAdapter implements NativeAdapter<Map<Byte, Integer>> {
  /** Prefixes with this index are not present in the landing facilities section. */
  private static final int NOT_PRESENT_INDEX_MARKER = 0x3ffff;
  private static final int NUM_ENTRIES = 164;

  @Override
  public Map<Byte, Integer> read(DataLengthSection dataLengthSection, TableOfContentsEntry entry, ByteBuffer byteBuffer) {
    Preconditions.checkState(entry.itemQuantity == NUM_ENTRIES);
    ImmutableMap.Builder<Byte, Integer> mapBuilder = ImmutableMap.builder();
    Preconditions.checkState(entry.itemLength == 3);
    for (int i = 0; i < entry.itemQuantity; ++i) {
      int offset = byteBuffer.getShort() & 0xffff;
      offset |= (byteBuffer.get() << 16);
      byte landingFacilityIdentifierPrefixByte = (byte) (i + 4);
      if (landingFacilityIdentifierPrefixByte <= NUM_ENTRIES && offset != NOT_PRESENT_INDEX_MARKER) {
        mapBuilder.put(landingFacilityIdentifierPrefixByte, offset);
      }
    }
    return mapBuilder.build();
  }

  @Override
  public NativeOutput write(Map<Byte, Integer> prefixByteToIndex) {
    NativeOutput nativeOutput = new NativeOutput(NUM_ENTRIES, 3);
    for (int i = 4; i < NUM_ENTRIES + 4; ++i) {
      byte prefixByte = (byte) i;
      if (prefixByteToIndex.containsKey(prefixByte)) {
        int offset = prefixByteToIndex.get(prefixByte);
        nativeOutput.putShort((short) (offset & 0xffff));
        nativeOutput.put((byte) ((offset >> 16) & 0xff));
      } else {
        nativeOutput.putShort((short) 0xffff);
        nativeOutput.put((byte) 0x3);
      }
    }
    return nativeOutput;
  }
}
