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

package garmintools.wrappers;

import garmintools.Proto;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class LandingFacilityDetail {
  public final int sectionOffset;
  public final Proto.LandingFacilityDetail protoLandingFacilityDetail;
  public final List<Runway> runways;
  public final List<CommunicationFrequency> communicationFrequencies;

  private LandingFacilityDetail(Builder builder) {
    this.sectionOffset = builder.sectionOffset;
    this.protoLandingFacilityDetail = builder.protoLandingFacilityDetail;
    this.runways = ImmutableList.copyOf(builder.runways);
    this.communicationFrequencies = ImmutableList.copyOf(builder.communicationFrequencies);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private int sectionOffset;
    private Proto.LandingFacilityDetail protoLandingFacilityDetail;
    private List<Runway> runways = new ArrayList<>();
    private List<CommunicationFrequency> communicationFrequencies = new ArrayList<>();

    private Builder() { }
    public Builder withSectionOffset(int offset) { this.sectionOffset = offset; return this; }
    public Builder withLandingFacilityDetail(Proto.LandingFacilityDetail proto) {
      this.protoLandingFacilityDetail = proto; return this; }
    public Builder withRunway(Runway runway) { runways.add(runway); return this; }
    public Builder withCommunicationFrequency(CommunicationFrequency communicationFrequency) {
      communicationFrequencies.add(communicationFrequency); return this; }
    public LandingFacilityDetail build() { return new LandingFacilityDetail(this); }
  }
}