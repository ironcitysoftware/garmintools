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

package garmintools.adapters.nativo;

import garmintools.Proto;
import garmintools.keys.IndexForeignKey;
import garmintools.keys.NativeOffsetForeignKey;
import garmintools.keys.VariableLengthEncodingForeignKey;
import garmintools.sections.DataLengthSection;
import garmintools.wrappers.LandingFacility;
import garmintools.wrappers.TableOfContentsEntry;

import java.nio.ByteBuffer;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;

public class LandingFacilityNativeAdapter implements NativeAdapter<List<LandingFacility>> {
  public static final int LANDING_FACILITY_ENCODED_SIZE = 28;
  private static final int ELEVATION_ADJUSTMENT = 0x1388;
  private static final int TWO_TO_TWENTY_FOUR = 1 << 24;

  @Override
  public List<LandingFacility> read(DataLengthSection dataLengthSection, TableOfContentsEntry entry, ByteBuffer byteBuffer) {
    Preconditions.checkState(byteBuffer.remaining() % LANDING_FACILITY_ENCODED_SIZE == 0);
    ImmutableList.Builder<LandingFacility> listBuilder = ImmutableList.builder();
    int index = 0;
    while (byteBuffer.hasRemaining()) {
      // System.err.printf("Decoding landing facility at offset %08x\n",
      //    byteBuffer.position() + 0x00acf486); // 0x003d0b23);
      listBuilder.add(decodeLandingFacility(byteBuffer, index++));
    }
    return listBuilder.build();
  }

  private LandingFacility decodeLandingFacility(ByteBuffer byteBuffer, int index) {
    LandingFacility.Builder builder = LandingFacility.newBuilder();
    Proto.LandingFacility.Builder protoBuilder = Proto.LandingFacility.newBuilder();
    int unknown[] = new int[11];

    builder.withIdentifierPrefix(new IndexForeignKey(index));

    // bytes 0-3
    // ffff0000 -> longitude high
    // 0000ffff -> latitude high
    int longitudeBits = byteBuffer.getShort();
    longitudeBits <<= 9;
    int latitudeBits = byteBuffer.getShort();
    latitudeBits <<= 8;

    // bytes 4-7
    int mask = byteBuffer.getInt();
    // c0000000 -> unknown
    // 20000000 -> unknown
    // 1c000000 -> table 55 landing facility type index (Public, private, heliport...)
    // 03fe0000 -> table 1 icao region index
    // 0001ff00 -> longitude low
    // 000000ff -> latitude low

    latitudeBits |= (mask & 0xff);
    longitudeBits |= ((mask >> 8) & 0x1ff);
    protoBuilder.setLongitudeDegrees(((double) longitudeBits) / TWO_TO_TWENTY_FOUR * 180);
    protoBuilder.setLatitudeDegrees(((double) latitudeBits) / TWO_TO_TWENTY_FOUR * 180);
    builder.withIcaoRegion(new IndexForeignKey((mask >> 17) & 0x1ff));
    builder.withLandingFacilityType(new IndexForeignKey((mask >> 26) & 7));
    unknown[1] = (mask >> 29) & 1;
    //Preconditions.checkState(unknown[0] == 1); TODO USA ONLY
    unknown[0] = (mask >> 30) & 3;

    // bytes 8-9
    byte[] identifier = new byte[2];
    byteBuffer.get(identifier);
    builder.withIdentifier(identifier);

    // bytes 10-11
    // 8000 -> hasLandingFacilityDetail
    // 7fff -> elevation + 0x1388
    mask = byteBuffer.getShort();
    boolean hasLandingFacilityDetail = ((mask >> 15) & 1) > 0;
    protoBuilder.setElevationFeet((mask & 0x7fff) - ELEVATION_ADJUSTMENT);

    // bytes 12-15
    // c0000000 -> unknown
    // 20000000 -> unknown
    // 1f000000 -> unknown
    // 00800000 -> AvGas fuel available
    // 00400000 -> JetA fuel available
    // 00380000 -> section 2 landing facility name bit offset
    // 0007ffff -> section 2 landing facility name byte offset
    mask = byteBuffer.getInt();
    unknown[2] = (mask >> 30) & 3;
    unknown[3] = (mask >> 29) & 1;
    unknown[4] = (mask >> 24) & 0x1f;
    if (((mask >> 22) & 1) > 0) { protoBuilder.setJetAFuelAvailable(true); }
    if (((mask >> 23) & 1) > 0) { protoBuilder.setAvgasFuelAvailable(true); }
    int nameBitIndex = 7 - ((mask >> 19) & 7);
    int nameByteIndex = mask & 0x7ffff;
    builder.withName(new VariableLengthEncodingForeignKey(nameByteIndex, nameBitIndex));

    // byte 16-19
    // fe000000 -> section 9 details index high
    // 01000000 -> unknown
    // 00800000 -> is radar available
    // 00400000 -> unknown
    // 00380000 -> location bit offset
    // 0007ffff -> location byte offset
    mask = byteBuffer.getInt();
    int landingFacilityDetailsOffsetHigh = (mask >> 9) & 0x7f0000;
    unknown[5] = (mask >> 24) & 1;
    if (((mask >> 23) & 1) > 0) { protoBuilder.setRadarAvailable(true); }
    unknown[6] = (mask >> 22) & 1;
    int locationBitIndex = 7 - ((mask >> 19) & 7);
    int locationByteIndex = mask & 0x7ffff;
    builder.withLocation(new VariableLengthEncodingForeignKey(locationByteIndex, locationBitIndex));

    // byte 20-23
    // ffff0000 -> section 9 details index low (if details) or unknown (if no details)
    // 0000e000 -> airspace type index (eg B, C, TSRA)
    // 00001fff -> unknown
    mask = byteBuffer.getInt();
    if (hasLandingFacilityDetail) {
      int landingFacilityDetailsOffset = landingFacilityDetailsOffsetHigh | ((mask >> 16) & 0xffff);
      builder.withDetail(new NativeOffsetForeignKey(landingFacilityDetailsOffset));
    } else {
      unknown[7] = (mask >> 16) & 0xffff;  // Have seen 0x614, usually is 0x600.
    }
    builder.withAirspace(new IndexForeignKey((mask >> 13) & 7));
    unknown[8] = (mask & 0x1fff);

    // byte 24-27
    mask = byteBuffer.getInt();
    // ffffff00 -> unknown
    // 000000ff -> unknown
    unknown[9] = (mask >> 8) & 0xffffff;
    Preconditions.checkState(unknown[9] == 0x3ffff);
    unknown[10] = mask & 0xff;

    protoBuilder.addAllUnknown(Ints.asList(unknown));
    builder.withLandingFacility(protoBuilder.build());

    return builder.build();
  }

  @Override
  public NativeOutput write(List<LandingFacility> landingFacilities) {
    NativeOutput nativeOutput = new NativeOutput(landingFacilities.size(), LANDING_FACILITY_ENCODED_SIZE);
    for (LandingFacility landingFacility : landingFacilities) {
      encode(landingFacility, nativeOutput);
    }
    return nativeOutput;
  }

  private void encode(LandingFacility facility, NativeOutput nativeOutput) {
    int longitudeBits = (int) (facility.protoLandingFacility.getLongitudeDegrees() * TWO_TO_TWENTY_FOUR / 180);
    int latitudeBits = (int) (facility.protoLandingFacility.getLatitudeDegrees() * TWO_TO_TWENTY_FOUR / 180);
    nativeOutput.putShort((short) ((longitudeBits >> 9) & 0xffff));
    nativeOutput.putShort((short) ((latitudeBits >> 8) & 0xffff));

    int unknown[] = Ints.toArray(facility.protoLandingFacility.getUnknownList());

    int data = ((unknown[0] & 3) << 30)
        | ((unknown[1] & 1) << 29)
        | ((facility.landingFacilityType.getIndex() & 7) << 26)
        | ((facility.icaoRegion.getIndex() & 0x1ff) << 17)
        | ((longitudeBits & 0x1ff) << 8)
        | ((latitudeBits & 0xff));
    nativeOutput.putInt(data);
    nativeOutput.put(facility.identifier);

    data = ((facility.detail != null ? 1 : 0) << 15)
        | ((facility.protoLandingFacility.getElevationFeet() + ELEVATION_ADJUSTMENT) & 0x7fff);
    nativeOutput.putShort((short) data);

    data = ((unknown[2] & 3) << 30)
        | ((unknown[3] & 1) << 29)
        | ((unknown[4] & 0x1f) << 24)
        | ((facility.protoLandingFacility.getAvgasFuelAvailable() ? 1 : 0) << 23)
        | ((facility.protoLandingFacility.getJetAFuelAvailable() ? 1 : 0) << 22)
        | (((7 - facility.name.getBitIndex()) & 7) << 19)
        | ((facility.name.getByteIndex() & 0x7ffff));
    nativeOutput.putInt(data);

    int detailNativeOffset = facility.detail == null
        ? (unknown[7] & 0xffff)
        : facility.detail.getNativeOffset();
    data = ((detailNativeOffset & 0x7f0000) << 9)
        | ((unknown[5] & 1) << 24)
        | ((facility.protoLandingFacility.getRadarAvailable() ? 1 : 0) << 23)
        | ((unknown[6] & 1) << 22)
        | (((7 - facility.location.getBitIndex()) & 7) << 19)
        | ((facility.location.getByteIndex() & 0x7ffff));
    nativeOutput.putInt(data);

    data = ((detailNativeOffset & 0xffff) << 16)
        | ((facility.airspace.getIndex() & 7) << 13)
        | ((unknown[8] & 0x1fff));
    nativeOutput.putInt(data);

    data = ((unknown[9] & 0xffffff) << 8)
        | (unknown[10] & 0xff);
    nativeOutput.putInt(data);
  }
}
