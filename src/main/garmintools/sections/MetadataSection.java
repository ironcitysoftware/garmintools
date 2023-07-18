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
import garmintools.adapters.garmin.MetadataGarminAdapter;
import garmintools.adapters.openaip.MetadataOpenAIPAdapter;
import garmintools.adapters.openaip.OpenAIPAdapter;
import garmintools.adapters.garmin.GarminAdapter;
import garmintools.adapters.proto.MetadataProtoAdapter;
import garmintools.adapters.proto.ProtoAdapter;

public class MetadataSection extends Section<Proto.Metadata> {
  MetadataSection(int sectionNumber, Proto.Metadata data,
      GarminAdapter<Proto.Metadata> garminAdapter,
      OpenAIPAdapter<Proto.Metadata> openAIPAdapter,
      ProtoAdapter<Proto.Metadata> protoAdapter) {
    super(sectionNumber, data, garminAdapter, openAIPAdapter, protoAdapter);
  }

  static class Factory extends SectionFactory<Proto.Metadata> {
    Factory() {
      super(Ids.METADATA_SECTION,
          new MetadataGarminAdapter(),
          new MetadataOpenAIPAdapter(),
          new MetadataProtoAdapter(),
          MetadataSection.class);
    }
  }
}
