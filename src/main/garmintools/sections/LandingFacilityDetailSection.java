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
import garmintools.adapters.nativo.LandingFacilityDetailNativeAdapter;
import garmintools.adapters.nativo.LandingFacilityDetailNativeAdapter.NativeOutputAndIndexToOffset;
import garmintools.adapters.nativo.NativeAdapter;
import garmintools.adapters.nativo.NativeOutput;
import garmintools.adapters.proto.LandingFacilityDetailProtoAdapter;
import garmintools.adapters.proto.ProtoAdapter;
import garmintools.keys.IndexForeignKey;
import garmintools.keys.NativeOffsetForeignKey;
import garmintools.wrappers.LandingFacilityDetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class LandingFacilityDetailSection extends Section<List<LandingFacilityDetail>> {
  private final Map<Integer, Integer> nativeOffsetToIndex;  // offset to index of read native data
  private Map<Integer, Integer> indexToNativeOffset;  // index to offset of written native data

  LandingFacilityDetailSection(int sectionNumber, List<LandingFacilityDetail> data,
      NativeAdapter<List<LandingFacilityDetail>> nativeAdapter,
      ProtoAdapter<List<LandingFacilityDetail>> protoAdapter) {
    super(sectionNumber, data, nativeAdapter, protoAdapter);
    ImmutableMap.Builder<Integer, Integer> mapBuilder = ImmutableMap.builder();
    for (int index = 0; index < data.size(); ++index) {
      mapBuilder.put(data.get(index).nativeFileOffset, index);
    }
    this.nativeOffsetToIndex = mapBuilder.build();
    this.indexToNativeOffset = new HashMap<>();
  }

  public IndexForeignKey insert(LandingFacilityDetail landingFacilityDetail) {
    data.add(landingFacilityDetail);
    return new IndexForeignKey(data.size() - 1);
  }

  public NativeOffsetForeignKey getOffsetForIndex(IndexForeignKey key) {
    int nativeOffset = indexToNativeOffset.get(key.getIndex());
    return new NativeOffsetForeignKey(nativeOffset);
  }

  public LandingFacilityDetail lookup(NativeOffsetForeignKey key) {
    return data.get(nativeOffsetToIndex.get(key.getNativeOffset()));
  }

  @Override
  public void mergeToProto(SectionManager sectionManager, Proto.NavigationData.Builder builder) {
    // no-op
  }

  @Override
  public NativeOutput getNativeBytes(SectionManager sectionManager) {
    NativeOutputAndIndexToOffset nativeOutput = (NativeOutputAndIndexToOffset) nativeAdapter.write(data);
    indexToNativeOffset = ImmutableMap.copyOf(nativeOutput.indexToOffset);
    return nativeOutput;
  }

  static class Factory extends SectionFactory<List<LandingFacilityDetail>> {
    Factory() {
      super(Ids.LANDING_FACILITY_DETAIL_SECTION,
          new LandingFacilityDetailNativeAdapter(),
          new LandingFacilityDetailProtoAdapter(),
          LandingFacilityDetailSection.class);
    }

    @Override
    public LandingFacilityDetailSection createFromProto(NavigationData proto) {
      return new LandingFacilityDetailSection(sectionNumber, new ArrayList<LandingFacilityDetail>(),
          nativeAdapter, protoAdapter);
    }
  }
}
