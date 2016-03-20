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

import static garmintools.encoding.SixBitAsciiEncoding.COMPLEX_ENCODING;
import garmintools.Proto;
import garmintools.keys.IndexForeignKey;
import garmintools.sections.SectionManager;
import garmintools.util.StringUtil;
import garmintools.wrappers.LandingFacility;
import garmintools.wrappers.LandingFacilityDetail;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Bytes;

public class LandingFacilityNormalizer {
  private final SectionManager manager;
  private final LandingFacilityDetailNormalizer landingFacilityDetailNormalizer;

  public LandingFacilityNormalizer(SectionManager manager) {
    this.manager = manager;
    this.landingFacilityDetailNormalizer = new LandingFacilityDetailNormalizer(manager);
  }

  public LandingFacility normalize(Proto.LandingFacility proto, int index) {
    LandingFacility.Builder builder = LandingFacility.newBuilder();
    Proto.LandingFacility.Builder normalizedProtoBuilder = Proto.LandingFacility.newBuilder(proto);

    String airspaceText = proto.hasAirspace() ? proto.getAirspace().name() : "";
    builder.withAirspace(manager.getAirspaceTable().lookup(airspaceText));
    normalizedProtoBuilder.clearAirspace();

    String paddedIdentifier = StringUtil.pad(proto.getIdentifier(), 4);
    byte identifier[] = COMPLEX_ENCODING.encode(paddedIdentifier);
    Preconditions.checkState(identifier.length == 3);
    builder.withIdentifier(new byte[] { identifier[0], identifier[1] });
    manager.getLandingFacilityIdentifierIndexSection().insertIndexForByte(identifier[2], index);
    builder.withIdentifierPrefix(new IndexForeignKey(index));
    normalizedProtoBuilder.clearIdentifier();

    builder.withIcaoRegion(manager.getIcaoRegionSection().lookupByRegion(proto.getIcaoRegion()));
    normalizedProtoBuilder.clearIcaoRegion();

    builder.withLandingFacilityType(
        manager.getLandingFacilityTypeSection().lookupOrInsert(proto.getLandingFacilityType().name()));
    normalizedProtoBuilder.clearLandingFacilityType();

    builder.withNameIndex(manager.getStringSection().lookupOrInsert(proto.getName()));
    normalizedProtoBuilder.clearName();

    builder.withLocationIndex(
        manager.getStringSection().lookupOrInsert(proto.getCity() + proto.getState()));
    normalizedProtoBuilder.clearCity();
    normalizedProtoBuilder.clearState();

    if (proto.hasDetail()) {
      LandingFacilityDetail detail = landingFacilityDetailNormalizer.normalize(proto.getDetail());
      builder.withDetailIndex(manager.getLandingFacilityDetailSection().insert(detail));
      normalizedProtoBuilder.clearDetail();
    }

    builder.withLandingFacility(normalizedProtoBuilder.build());
    return builder.build();
  }

  public Proto.LandingFacility denormalize(LandingFacility facility) {
    Proto.LandingFacility.Builder protoBuilder = Proto.LandingFacility.newBuilder(facility.protoLandingFacility);
    List<Byte> identifierBytes = ImmutableList.of(facility.identifier[0], facility.identifier[1],
        manager.getLandingFacilityIdentifierIndexSection().get(facility.identifierPrefix));
    protoBuilder.setIdentifier(COMPLEX_ENCODING.decode(Bytes.toArray(identifierBytes)).trim());
    String airspaceText = manager.getAirspaceTable().lookup(facility.airspace);
    if (!airspaceText.isEmpty()) {
      protoBuilder.setAirspace(Proto.LandingFacility.Airspace.valueOf(airspaceText));
    }
    Proto.IcaoRegion icaoRegion = manager.getIcaoRegionSection().lookup(facility.icaoRegion);
    protoBuilder.setIcaoRegion(icaoRegion);
    protoBuilder.setLandingFacilityType(Proto.LandingFacility.LandingFacilityType.valueOf(
        manager.getLandingFacilityTypeSection().lookup(facility.landingFacilityType)));
    protoBuilder.setName(manager.getStringSection().lookup(facility.name));
    // TODO if there is a state, it is appended to the city.  Determine when to separate.
    // TODO this may be unknown[0]
    String cityAndMaybeState = manager.getStringSection().lookup(facility.location);
    String region = icaoRegion.getRegion();
    if (region.contains("USA") || region.contains("ALASKA") || region.contains("HAWAII")) {
      protoBuilder.setCity(cityAndMaybeState.substring(0, cityAndMaybeState.length() - 2));
      protoBuilder.setState(cityAndMaybeState.substring(cityAndMaybeState.length() - 2));
    } else {
      protoBuilder.setCity(cityAndMaybeState);
    }
    if (facility.detail != null) {
      LandingFacilityDetail detail = manager.getLandingFacilityDetailSection().lookup(facility.detail);
      protoBuilder.setDetail(landingFacilityDetailNormalizer.denormalize(detail));
    }
    return protoBuilder.build();
  }
}
