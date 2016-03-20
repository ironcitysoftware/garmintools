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
import garmintools.wrappers.LandingFacilityDetail;
import garmintools.wrappers.Runway;

public class LandingFacilityDetailNormalizer {
  private final RunwayNormalizer runwayNormalizer;
  private final CommunicationFrequencyNormalizer communicationFrequencyNormalizer;

  public LandingFacilityDetailNormalizer(SectionManager manager) {
    this.runwayNormalizer = new RunwayNormalizer(manager);
    this.communicationFrequencyNormalizer = new CommunicationFrequencyNormalizer(manager);
  }

  public LandingFacilityDetail normalize(Proto.LandingFacilityDetail proto) {
    LandingFacilityDetail.Builder builder = LandingFacilityDetail.newBuilder();
    Proto.LandingFacilityDetail.Builder protoBuilder = Proto.LandingFacilityDetail.newBuilder(proto);
    for (Proto.Runway runway : proto.getRunwayList()) {
      builder.withRunway(runwayNormalizer.normalize(runway));
    }
    protoBuilder.clearRunway();

    for (Proto.CommunicationFrequency frequency : proto.getCommunicationFrequencyList()) {
      builder.withCommunicationFrequency(communicationFrequencyNormalizer.normalize(frequency));
    }
    protoBuilder.clearCommunicationFrequency();
    builder.withLandingFacilityDetail(protoBuilder.build());
    return builder.build();
  }

  public Proto.LandingFacilityDetail denormalize(LandingFacilityDetail detail) {
    Proto.LandingFacilityDetail.Builder protoBuilder = Proto.LandingFacilityDetail.newBuilder(detail.protoLandingFacilityDetail);
    for (Runway runway : detail.runways) {
      protoBuilder.addRunway(runwayNormalizer.denormalize(runway));
    }
    for (CommunicationFrequency frequency : detail.communicationFrequencies) {
      protoBuilder.addCommunicationFrequency(communicationFrequencyNormalizer.denormalize(frequency));
    }
    return protoBuilder.build();
  }
}
