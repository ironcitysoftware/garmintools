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
import garmintools.adapters.garmin.GarminAdapter;
import garmintools.adapters.garmin.UnparsedSectionGarminAdapter;
import garmintools.adapters.proto.ProtoAdapter;
import garmintools.adapters.proto.UnparsedSectionProtoAdapter;

public class UnparsedSection extends Section<Proto.UnparsedSection> {
  UnparsedSection(int sectionNumber, Proto.UnparsedSection data,
      GarminAdapter<Proto.UnparsedSection> garminAdapter,
      ProtoAdapter<Proto.UnparsedSection> protoAdapter) {
    super(sectionNumber, data, garminAdapter, protoAdapter);
  }

  static class Factory extends SectionFactory<Proto.UnparsedSection> {
    Factory(int sectionNumber) {
      super(sectionNumber,
          new UnparsedSectionGarminAdapter(sectionNumber),
          new UnparsedSectionProtoAdapter(sectionNumber),
          UnparsedSection.class);
    }
  }
}
