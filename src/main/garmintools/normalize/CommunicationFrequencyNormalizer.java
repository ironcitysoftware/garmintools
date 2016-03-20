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

package garmintools.normalize;

import garmintools.Proto;
import garmintools.sections.SectionManager;
import garmintools.wrappers.CommunicationFrequency;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

public class CommunicationFrequencyNormalizer {
  private static final BiMap<Proto.CommunicationFrequency.FrequencyType, String> PROTO_TO_STRING =
      ImmutableBiMap.of(
          Proto.CommunicationFrequency.FrequencyType.PILOT_CONTROLLED_LIGHTING, "PCL",
          Proto.CommunicationFrequency.FrequencyType.FLIGHT_SERVICE_STATION, "FSS",
          Proto.CommunicationFrequency.FrequencyType.PRE_TAXI, "PRE-TAXI",
          Proto.CommunicationFrequency.FrequencyType.AIRLIFT, "AIRLFT CMD");

  private final SectionManager manager;

  public CommunicationFrequencyNormalizer(SectionManager manager) {
    this.manager = manager;
  }

  public CommunicationFrequency normalize(Proto.CommunicationFrequency proto) {
    CommunicationFrequency.Builder builder = CommunicationFrequency.newBuilder();
    Proto.CommunicationFrequency.Builder protoBuilder =
        Proto.CommunicationFrequency.newBuilder(proto);

    String frequencyType;
    if (PROTO_TO_STRING.containsKey(proto.getFrequencyType())) {
      frequencyType = PROTO_TO_STRING.get(proto.getFrequencyType());
    } else {
      frequencyType = proto.getFrequencyType().name();
      frequencyType = frequencyType.replace('_', ' ');
    }
    builder.withFrequencyType(
        manager.getGenericAirportStringSection1().lookupOrInsert(frequencyType));
    protoBuilder.clearFrequencyType();

    builder.withCommunicationFrequency(protoBuilder.build());
    return builder.build();
  }


  public Proto.CommunicationFrequency denormalize(CommunicationFrequency frequency) {
    Proto.CommunicationFrequency.Builder protoBuilder =
        Proto.CommunicationFrequency.newBuilder(frequency.protoCommunicationFrequency);

    String frequencyType =
        manager.getGenericAirportStringSection1().lookup(frequency.frequencyType);
    if (PROTO_TO_STRING.inverse().containsKey(frequencyType)) {
      protoBuilder.setFrequencyType(PROTO_TO_STRING.inverse().get(frequencyType));
    } else {
      frequencyType = frequencyType.replace(' ', '_');
      protoBuilder.setFrequencyType(
          Proto.CommunicationFrequency.FrequencyType.valueOf(frequencyType));
    }
    return protoBuilder.build();
  }
}
