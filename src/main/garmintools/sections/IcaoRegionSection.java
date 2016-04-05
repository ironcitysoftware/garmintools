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
import garmintools.adapters.garmin.IcaoRegionGarminAdapter;
import garmintools.adapters.garmin.GarminAdapter;
import garmintools.adapters.proto.IcaoRegionProtoAdapter;
import garmintools.adapters.proto.ProtoAdapter;
import garmintools.keys.IndexForeignKey;

import java.util.List;

import com.google.common.base.Preconditions;

public class IcaoRegionSection extends Section<List<Proto.IcaoRegion>> {
  IcaoRegionSection(int sectionNumber, List<Proto.IcaoRegion> data,
      GarminAdapter<List<Proto.IcaoRegion>> garminAdapter,
      ProtoAdapter<List<Proto.IcaoRegion>> protoAdapter) {
    super(sectionNumber, data, garminAdapter, protoAdapter);
  }

  public IndexForeignKey lookupByRegion(Proto.IcaoRegion region) {
    int index = data.indexOf(region);
    Preconditions.checkState(index != -1);
    return new IndexForeignKey(index);
  }

  public Proto.IcaoRegion lookup(IndexForeignKey key) {
    return data.get(key.getIndex());
  }

  static class Factory extends SectionFactory<List<Proto.IcaoRegion>> {
    Factory() {
      super(Ids.ICAO_REGION_SECTION,
          new IcaoRegionGarminAdapter(),
          new IcaoRegionProtoAdapter(),
          IcaoRegionSection.class);
    }
  }
}
