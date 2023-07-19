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
import garmintools.adapters.garmin.GarminAdapter;
import garmintools.adapters.garmin.GarminOutput;
import garmintools.adapters.garmin.LandingFacilityDetailGarminAdapter;
import garmintools.adapters.garmin.LandingFacilityDetailGarminAdapter.GarminOutputAndIndexToOffset;
import garmintools.adapters.openaip.LandingFacilityDetailOpenAIPAdapter;
import garmintools.adapters.openaip.OpenAIPAdapter;
import garmintools.adapters.proto.LandingFacilityDetailProtoAdapter;
import garmintools.adapters.proto.ProtoAdapter;
import garmintools.keys.IndexForeignKey;
import garmintools.keys.SectionOffsetForeignKey;
import garmintools.wrappers.LandingFacilityDetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class LandingFacilityDetailSection extends Section<List<LandingFacilityDetail>> {
  private final Map<Integer, Integer> sectionOffsetToIndex;  // offset to index of read data
  private Map<Integer, Integer> indexToSectionOffset;  // index to offset of written data

  LandingFacilityDetailSection(int sectionNumber, List<LandingFacilityDetail> data,
      GarminAdapter<List<LandingFacilityDetail>> garminAdapter,
      OpenAIPAdapter<List<LandingFacilityDetail>> openAIPAdapter,
      ProtoAdapter<List<LandingFacilityDetail>> protoAdapter) {
    super(sectionNumber, data, garminAdapter, openAIPAdapter, protoAdapter);
    ImmutableMap.Builder<Integer, Integer> mapBuilder = ImmutableMap.builder();
    for (int index = 0; index < data.size(); ++index) {
      mapBuilder.put(data.get(index).sectionOffset, index);
    }
    this.sectionOffsetToIndex = mapBuilder.build();
    this.indexToSectionOffset = new HashMap<>();
  }

  public IndexForeignKey insert(LandingFacilityDetail landingFacilityDetail) {
    data.add(landingFacilityDetail);
    return new IndexForeignKey(data.size() - 1);
  }

  public SectionOffsetForeignKey getOffsetForIndex(IndexForeignKey key) {
    int sectionOffset = indexToSectionOffset.get(key.getIndex());
    return new SectionOffsetForeignKey(sectionOffset);
  }

  public LandingFacilityDetail lookup(SectionOffsetForeignKey key) {
    return data.get(sectionOffsetToIndex.get(key.getSectionOffset()));
  }

  @Override
  public void mergeToProto(SectionManager sectionManager, Proto.NavigationData.Builder builder) {
    // no-op
  }

  @Override
  public GarminOutput getSectionBytes(SectionManager sectionManager) {
    GarminOutputAndIndexToOffset output = (GarminOutputAndIndexToOffset) garminAdapter.write(data);
    indexToSectionOffset = ImmutableMap.copyOf(output.indexToOffset);
    return output;
  }

  static class Factory extends SectionFactory<List<LandingFacilityDetail>> {
    Factory() {
      super(Ids.LANDING_FACILITY_DETAIL_SECTION,
          new LandingFacilityDetailGarminAdapter(),
          new LandingFacilityDetailOpenAIPAdapter(),
          new LandingFacilityDetailProtoAdapter(),
          LandingFacilityDetailSection.class);
    }

    @Override
    public LandingFacilityDetailSection createFromProto(NavigationData proto) {
      return new LandingFacilityDetailSection(sectionNumber, new ArrayList<LandingFacilityDetail>(),
          garminAdapter, openAIPAdapter, protoAdapter);
    }
  }
}
