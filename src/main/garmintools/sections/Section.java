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

import java.util.List;

import garmintools.Proto;
import garmintools.adapters.garmin.GarminAdapter;
import garmintools.adapters.garmin.GarminOutput;
import garmintools.adapters.openaip.OpenAIPAdapter;
import garmintools.adapters.proto.ProtoAdapter;
import garmintools.openaip.Airport;

abstract class Section<T> {
  final int sectionNumber;
  final GarminAdapter<T> garminAdapter;
  final OpenAIPAdapter<T> openAIPAdapter;
  final ProtoAdapter<T> protoAdapter;
  final T data;

  Section(int sectionNumber, T data, GarminAdapter<T> garminAdapter,
      OpenAIPAdapter<T> openAIPAdapter, ProtoAdapter<T> protoAdapter) {
    this.sectionNumber = sectionNumber;
    this.data = data;
    this.garminAdapter = garminAdapter;
    this.openAIPAdapter = openAIPAdapter;
    this.protoAdapter = protoAdapter;
  }

  /**
   * This is the second pass to give constructed sections a chance at reading the proto.
   */
  public void mergeFromOpenAIP(SectionManager sectionManager, List<Airport> airports) {
    // default is no-op.
  }

  /** This is the second pass to give constructed sections a chance at reading the proto. */
  public void mergeFromProto(SectionManager manager, Proto.NavigationData proto) {
    // default is no-op.
  }

  public void mergeToProto(SectionManager sectionManager, Proto.NavigationData.Builder builder) {
    protoAdapter.write(data, builder);
  }

  public GarminOutput getSectionBytes(SectionManager sectionManager) {
    return garminAdapter.write(data);
  }

  @Override
  public String toString() {
    return data.toString();
  }
}
