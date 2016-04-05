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
import garmintools.keys.IndexForeignKey;
import garmintools.keys.SectionOffsetForeignKey;
import garmintools.keys.VariableLengthEncodingForeignKey;

public class LandingFacility {
  public final Proto.LandingFacility protoLandingFacility;
  public final byte[] identifier;
  public final IndexForeignKey identifierPrefix;
  public final IndexForeignKey icaoRegion;
  public final IndexForeignKey landingFacilityType;
  public final VariableLengthEncodingForeignKey name;
  public final IndexForeignKey nameIndex;
  public final SectionOffsetForeignKey detail;
  public final IndexForeignKey detailIndex;
  public final VariableLengthEncodingForeignKey location;
  public final IndexForeignKey locationIndex;
  public final IndexForeignKey airspace;

  private LandingFacility(Builder builder) {
    this.protoLandingFacility = builder.protoLandingFacility;
    this.identifier = builder.identifier;
    this.identifierPrefix = builder.identifierPrefix;
    this.icaoRegion = builder.icaoRegion;
    this.landingFacilityType = builder.landingFacilityType;
    this.name = builder.name;
    this.nameIndex = builder.nameIndex;
    this.detail = builder.detail;
    this.detailIndex = builder.detailIndex;
    this.location = builder.location;
    this.locationIndex = builder.locationIndex;
    this.airspace = builder.airspace;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(LandingFacility landingFacility) {
    return new Builder(landingFacility);
  }

  public static class Builder {
    private Proto.LandingFacility protoLandingFacility;
    private IndexForeignKey identifierPrefix;
    private byte[] identifier;
    private IndexForeignKey icaoRegion;
    private IndexForeignKey landingFacilityType;
    private VariableLengthEncodingForeignKey name;
    private IndexForeignKey nameIndex;
    private SectionOffsetForeignKey detail;
    private IndexForeignKey detailIndex;
    private VariableLengthEncodingForeignKey location;
    private IndexForeignKey locationIndex;
    private IndexForeignKey airspace;

    private Builder() { }

    private Builder(LandingFacility landingFacility) {
      this.protoLandingFacility = landingFacility.protoLandingFacility;
      this.identifierPrefix = landingFacility.identifierPrefix;
      this.identifier = landingFacility.identifier;
      this.icaoRegion = landingFacility.icaoRegion;
      this.landingFacilityType = landingFacility.landingFacilityType;
      this.name = landingFacility.name;
      this.nameIndex = landingFacility.nameIndex;
      this.detail = landingFacility.detail;
      this.detailIndex = landingFacility.detailIndex;
      this.location = landingFacility.location;
      this.locationIndex = landingFacility.locationIndex;
      this.airspace = landingFacility.airspace;
    }

    public Builder withLandingFacility(Proto.LandingFacility proto) { this.protoLandingFacility = proto; return this; }
    public Builder withIdentifierPrefix(IndexForeignKey key) { this.identifierPrefix = key; return this; }
    public Builder withIdentifier(byte[] identifier) { this.identifier = identifier; return this; }
    public Builder withIcaoRegion(IndexForeignKey key) { this.icaoRegion = key; return this; }
    public Builder withLandingFacilityType(IndexForeignKey key) { this.landingFacilityType = key; return this; }
    public Builder withName(VariableLengthEncodingForeignKey key) { this.name = key; return this; }
    public Builder withNameIndex(IndexForeignKey key) { this.nameIndex = key; return this; }
    public Builder withDetail(SectionOffsetForeignKey key) { this.detail = key; return this; }
    public Builder withDetailIndex(IndexForeignKey key) { this.detailIndex = key; return this; }
    public Builder withLocation(VariableLengthEncodingForeignKey key) { this.location = key; return this; }
    public Builder withLocationIndex(IndexForeignKey key) { this.locationIndex = key; return this; }
    public Builder withAirspace(IndexForeignKey key) { this.airspace = key; return this; }
    public LandingFacility build() {
      return new LandingFacility(this);
    }
  }

  @Override
  public String toString() {
    return protoLandingFacility.toString();
  }
}