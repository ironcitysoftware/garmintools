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

import garmintools.adapters.garmin.GarminAdapter;
import garmintools.adapters.garmin.NavigationFixGarminAdapter;
import garmintools.adapters.openaip.LookupTableOpenAIPAdapter;
import garmintools.adapters.openaip.OpenAIPAdapter;
import garmintools.adapters.proto.LookupTableProtoAdapter;
import garmintools.adapters.proto.ProtoAdapter;
import garmintools.keys.IndexForeignKey;

import java.util.List;

public class NavigationFixSection extends LookupTableSection {
  NavigationFixSection(int sectionNumber, List<String> data,
      GarminAdapter<List<String>> garminAdapter,
      OpenAIPAdapter<List<String>> openAIPAdapter,
      ProtoAdapter<List<String>> protoAdapter) {
    super(sectionNumber, data, garminAdapter, openAIPAdapter, protoAdapter);
  }

  @Override
  public IndexForeignKey lookupOrInsert(String string) {
    if (!data.contains(string)) {
      data.add(string);
    }
    return new IndexForeignKey(data.indexOf(string));
  }

  @Override
  public String lookup(IndexForeignKey key) {
    return data.get(key.getIndex());
  }

  static class Factory extends SectionFactory<List<String>> {
    Factory() {
      super(Ids.NAVIGATION_FIX_SECTION,
          new NavigationFixGarminAdapter(),
          // TODO.  Remove temporary table when fixes are stored in instrument approach data.
          new LookupTableOpenAIPAdapter(Ids.NAVIGATION_FIX_SECTION),
          new LookupTableProtoAdapter(Ids.NAVIGATION_FIX_SECTION),
          NavigationFixSection.class);
    }
  }
}
