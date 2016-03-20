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

package garmintools.sections;

import garmintools.Proto;
import garmintools.Proto.NavigationData;
import garmintools.adapters.nativo.LandingFacilityIdentifierIndexNativeAdapter;
import garmintools.adapters.nativo.NativeAdapter;
import garmintools.adapters.proto.ProtoAdapter;
import garmintools.keys.IndexForeignKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBiMap;

/**
 * Map of section 6 indexes to the first byte of the landing facility identifier.
 * Since the first byte is part of a six-bit encoded number, six of the bits are for the
 * first letter and the remaining two bits are part of the second letter.  Thus there are
 * at most four entries for each initial landing facility identity letter.
 */
public class LandingFacilityIdentifierIndexSection extends Section <Map<Byte, Integer>> {
  private final SortedMap<Integer, Byte> firstOffsetToByte;

  LandingFacilityIdentifierIndexSection(int sectionNumber, Map<Byte, Integer> data,
      NativeAdapter<Map<Byte, Integer>> nativeAdapter,
      ProtoAdapter<Map<Byte, Integer>> protoAdapter) {
    super(sectionNumber, data, nativeAdapter, protoAdapter);
    Map<Integer, Byte> inverse = HashBiMap.create(data).inverse();
    List<Integer> sortedOffsets = new ArrayList<Integer>(inverse.keySet());
    Collections.sort(sortedOffsets);
    this.firstOffsetToByte = new TreeMap<>();
    for (int offset : sortedOffsets) {
      this.firstOffsetToByte.put(offset, inverse.get(offset));
    }
  }

  public Byte get(IndexForeignKey key) {
    int lastOffset = -1;
    int offset = key.getIndex();
    for (int mapOffset : firstOffsetToByte.keySet()) {
      if (mapOffset == offset) {
        return firstOffsetToByte.get(mapOffset);
      } else if (mapOffset > offset) {
        return firstOffsetToByte.get(lastOffset);
      }
      lastOffset = mapOffset;
    }
    return firstOffsetToByte.get(lastOffset);
  }

  public void insertIndexForByte(byte prefixByte, int index) {
    Preconditions.checkState(firstOffsetToByte.isEmpty());
    if (!data.containsKey(prefixByte)) {
      data.put(prefixByte, index);
    }
  }

  @Override
  public void mergeToProto(SectionManager sectionManager, Proto.NavigationData.Builder builder) {
    // no-op
  }

  static class Factory extends SectionFactory<Map<Byte, Integer>> {
    Factory() {
      super(Ids.LANDING_FACILITY_IDENTIFIER_INDEX_SECTION,
          new LandingFacilityIdentifierIndexNativeAdapter(),
          null,
          LandingFacilityIdentifierIndexSection.class);
    }

    @Override
    public LandingFacilityIdentifierIndexSection createFromProto(NavigationData proto) {
      return new LandingFacilityIdentifierIndexSection(sectionNumber, new HashMap<Byte, Integer>(),
          nativeAdapter, protoAdapter);
    }
  }
}
