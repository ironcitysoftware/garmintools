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
import garmintools.adapters.garmin.LandingFacilityGarminAdapter;
import garmintools.adapters.garmin.GarminAdapter;
import garmintools.adapters.garmin.GarminOutput;
import garmintools.adapters.proto.LandingFacilityProtoAdapter;
import garmintools.adapters.proto.ProtoAdapter;
import garmintools.normalize.LandingFacilityNormalizer;
import garmintools.wrappers.LandingFacility;

import java.util.ArrayList;
import java.util.List;

public class LandingFacilitySection extends Section<List<LandingFacility>> {
  LandingFacilitySection(int sectionNumber, List<LandingFacility> data,
      GarminAdapter<List<LandingFacility>> garminAdapter,
      ProtoAdapter<List<LandingFacility>> protoAdapter) {
    super(sectionNumber, data, garminAdapter, protoAdapter);
  }

  LandingFacilitySection(int sectionNumber,
      GarminAdapter<List<LandingFacility>> garminAdapter,
      ProtoAdapter<List<LandingFacility>> protoAdapter) {
    super(sectionNumber, new ArrayList<LandingFacility>(), garminAdapter, protoAdapter);
  }

  @Override
  public void mergeFromProto(SectionManager sectionManager, Proto.NavigationData proto) {
    LandingFacilityNormalizer normalizer = new LandingFacilityNormalizer(sectionManager);
    for (int index = 0; index < proto.getLandingFacilityCount(); ++index) {
      data.add(normalizer.normalize(proto.getLandingFacility(index), index));
    }
  }

  @Override
  public void mergeToProto(SectionManager sectionManager, Proto.NavigationData.Builder protoBuilder) {
    LandingFacilityNormalizer normalizer = new LandingFacilityNormalizer(sectionManager);
    List<LandingFacility> rewrittenLandingFacility = new ArrayList<>();
    for (LandingFacility landingFacility : data) {
      LandingFacility.Builder builder = LandingFacility.newBuilder(landingFacility);
      builder.withLandingFacility(normalizer.denormalize(landingFacility));
      rewrittenLandingFacility.add(builder.build());
    }
    protoAdapter.write(rewrittenLandingFacility, protoBuilder);
  }

  @Override
  public GarminOutput getSectionBytes(SectionManager sectionManager) {
    // We need to change the details index from an index FK to a offset FK.
    // We also need to change the name and location index from an index FK to a var len FK.
    // This is why string and details must be written first, so the FKs will be known.
    List<LandingFacility> rewrittenLandingFacility = new ArrayList<>();
    for (LandingFacility landingFacility : data) {
      LandingFacility.Builder builder = LandingFacility.newBuilder(landingFacility)
          .withDetail(landingFacility.detailIndex == null
              ? null
              : sectionManager.getLandingFacilityDetailSection().getOffsetForIndex(landingFacility.detailIndex))
          .withName(sectionManager.getStringSection().getKeyForIndex(landingFacility.nameIndex))
          .withLocation(sectionManager.getStringSection().getKeyForIndex(landingFacility.locationIndex));
      rewrittenLandingFacility.add(builder.build());
    }
    return garminAdapter.write(rewrittenLandingFacility);
  }

  static class Factory extends SectionFactory<List<LandingFacility>> {
    Factory() {
      super(Ids.LANDING_FACILITY_SECTION,
          new LandingFacilityGarminAdapter(),
          new LandingFacilityProtoAdapter(),
          LandingFacilitySection.class);
    }

    @Override
    public LandingFacilitySection createFromProto(Proto.NavigationData proto) {
      return new LandingFacilitySection(sectionNumber, garminAdapter, protoAdapter);
    }
  }
}
