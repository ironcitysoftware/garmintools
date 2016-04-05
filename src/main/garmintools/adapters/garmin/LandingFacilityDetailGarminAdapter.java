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

package garmintools.adapters.garmin;

import garmintools.Proto;
import garmintools.Proto.UnknownLandingFacilityDetailSection;
import garmintools.encoding.BitListOutputStream;
import garmintools.encoding.LittleEndianByteArrayDataOutput;
import garmintools.encoding.VariableLengthAsciiEncoding;
import garmintools.keys.IndexForeignKey;
import garmintools.sections.DataLengthSection;
import garmintools.sections.DataLengthSection.DataLength;
import garmintools.wrappers.CommunicationFrequency;
import garmintools.wrappers.LandingFacilityDetail;
import garmintools.wrappers.Runway;
import garmintools.wrappers.TableOfContentsEntry;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Ints;
import com.google.protobuf.ByteString;

public class LandingFacilityDetailGarminAdapter implements GarminAdapter<List<LandingFacilityDetail>> {
  private static final int NUM_NARRATIVE_LEADING_BITS = 7;

  public static class GarminOutputAndIndexToOffset extends GarminOutput {
    public final Map<Integer, Integer> indexToOffset;
    public GarminOutputAndIndexToOffset(byte data[], Map<Integer, Integer> indexToOffset) {
      super(data);
      this.indexToOffset = indexToOffset;
    }
  }

  @Override
  public List<LandingFacilityDetail> read(DataLengthSection dataLengthSection, TableOfContentsEntry entry, ByteBuffer byteBuffer) {
    ImmutableList.Builder<LandingFacilityDetail> builder = ImmutableList.builder();
    while (byteBuffer.hasRemaining()) {
      builder.add(readLandingFacilityDetail(dataLengthSection, byteBuffer));
    }
    return builder.build();
  }

  private LandingFacilityDetail readLandingFacilityDetail(DataLengthSection dataLengthSection, ByteBuffer byteBuffer) {
    LandingFacilityDetail.Builder builder = LandingFacilityDetail.newBuilder();
    Proto.LandingFacilityDetail.Builder protoBuilder = Proto.LandingFacilityDetail.newBuilder();

    int offset = byteBuffer.position();
    // System.err.printf("Parsing detail offset %06d %06x (%06x)\n", offset, offset, 0x14ea0f + offset);
    builder.withSectionOffset(offset);

    Queue<Integer> sectionLengths = new LinkedList<>();
    BitSet sectionsPresent = BitSet.valueOf(new long[] { byteBuffer.getShort() });
    int numSectionsPresent = sectionsPresent.cardinality();
    for (int i = 0; i < numSectionsPresent; ++i) {
      int sectionLength = byteBuffer.getShort();
      sectionLengths.add(sectionLength);
    }
    for (int i = sectionsPresent.nextSetBit(0); i >= 0; i = sectionsPresent.nextSetBit(i + 1)) {
      int sectionLength = sectionLengths.remove();
      ByteBuffer sectionByteBuffer = byteBuffer.slice();
      sectionByteBuffer.order(ByteOrder.LITTLE_ENDIAN).limit(sectionLength);
      byteBuffer.position(byteBuffer.position() + sectionLength);
      switch (i) {
        // TODO: return list of built objects instead of passing builders?
        case 0:
          readRunwayInfo(dataLengthSection, sectionByteBuffer, builder); break;
        case 1:
          if (sectionLength > 0) {
            readCommunicationInfo(dataLengthSection, sectionByteBuffer, builder);
          } else {
            // 750 data has empty communication stanzas.
            // TODO: right now piggyback on unknown section but eventually have a canonical
            // way to express "empty" in the proto.
            protoBuilder.addUnknownSectionBuilder().setSectionNumber(i).setData(ByteString.EMPTY);
          }
          break;
        case 5:
          // readApproachInfo(dataLengthSection, sectionByteBuffer, builder); break;
        default:
          protoBuilder.addUnknownSectionBuilder()
              .setSectionNumber(i)
              .setData(ByteString.copyFrom(sectionByteBuffer));
      }
      // TODO: add after existing parsers consume block fully and all parsers are implemented.
      // Preconditions.checkState(!sectionByteBuffer.hasRemaining());
    }
    builder.withLandingFacilityDetail(protoBuilder.build());
    return builder.build();
  }

  private void readRunwayInfo(DataLengthSection dataLengthSection, ByteBuffer byteBuffer, LandingFacilityDetail.Builder builder) {
    while (byteBuffer.hasRemaining()) {
      Runway.Builder runwayBuilder = Runway.newBuilder();
      Proto.Runway.Builder protoRunwayBuilder = Proto.Runway.newBuilder();

      int unknown[] = new int[4];

      int data = byteBuffer.getInt();
      // e0000000 -> runway suffix index (' ', 'C', 'L', 'R', 'T')
      // 1f000000 -> runway number
      // 00800000 -> unknown[0]: more runways follow ?
      // 00700000 -> section 47 lighting index
      // 000f0000 -> section 48 runway surface index
      // 0000ffff -> runway length (ft)
      runwayBuilder.withRunwayNumberSuffix(new IndexForeignKey((data >> 29) & 7));
      protoRunwayBuilder.setRunwayNumber((data >> 24) & 0x1f);
      unknown[0] = (data >> 23) & 1;
      runwayBuilder.withSurface(new IndexForeignKey((data >> 16) & 0xf));
      runwayBuilder.withLighting(new IndexForeignKey((data >> 20) & 7));
      protoRunwayBuilder.setRunwayLengthFeet(data & 0xffff);

      data = byteBuffer.getShort() & 0xffff;
      data |= byteBuffer.get() << 16;
      // 00ffe000 -> runway width (ft)
      // 00001000 -> has additional info 1
      // 00000800 -> has additional info 2
      // 00000400 -> has additional info 3
      // 00000200 -> unknown[1]
      // 00000100 -> unknown[2]
      // 000000ff -> unknown[3]
      protoRunwayBuilder.setRunwayWidthFeet((data >> 13) & 0x7ff);
      boolean isAdditionalInfo1Present = ((data >> 12) & 1) > 0;
      boolean isAdditionalInfo2Present = ((data >> 11) & 1) > 0;
      boolean isAdditionalInfo3Present = ((data >> 10) & 1) > 0;
      unknown[1] = (data >> 9) & 1;
      unknown[2] = (data >> 8) & 1;
      unknown[3] = data & 0xff;

      protoRunwayBuilder.addAllUnknown(Ints.asList(unknown));

      if (isAdditionalInfo1Present) {
        byte b[] = new byte[4];
        byteBuffer.get(b);
        protoRunwayBuilder.setUnknownAdditionalInfo1(ByteString.copyFrom(b));
      }
      if (isAdditionalInfo2Present) {
        byte b[] = new byte[dataLengthSection.get(DataLength.RUNWAY_ADDITIONAL_INFO_2)];
        byteBuffer.get(b);
        protoRunwayBuilder.setUnknownAdditionalInfo2(ByteString.copyFrom(b));
      }
      if (isAdditionalInfo3Present) {
        protoRunwayBuilder.setUnknownAdditionalInfo3(ByteString.copyFrom(new byte[] { byteBuffer.get() }));
      }
      runwayBuilder.withRunway(protoRunwayBuilder.build());
      builder.withRunway(runwayBuilder.build());
    }
  }

  private void readCommunicationInfo(DataLengthSection dataLengthSection, ByteBuffer byteBuffer, LandingFacilityDetail.Builder builder) {
    while (byteBuffer.hasRemaining()) {
      CommunicationFrequency.Builder freqBuilder = CommunicationFrequency.newBuilder();
      Proto.CommunicationFrequency.Builder protoBuilder = Proto.CommunicationFrequency.newBuilder();
      int unknown[] = new int[8];

      int data = byteBuffer.getInt();
      // 00000fff -> frequency Ghz (N*5+0xd2f0)/500
      // 00003000 -> unknown[0]
      // 00004000 -> unknown[1]
      // 00008000 -> unknown[2]
      // 003f0000 -> table 79 frequency type index
      // 00400000 -> unknown[3]
      // 00800000 -> more data follows
      // 07000000 -> unknown[4]
      // 38000000 -> unknown[5]
      // 40000000 -> unknown[6]
      // 80000000 -> unknown[7]
      protoBuilder.setFrequencyGhz(decodeFrequency(data & 0xfff));
      freqBuilder.withFrequencyType(new IndexForeignKey((data >> 16) & 0x3f));
      unknown[0] = (data >> 12) & 3;
      unknown[1] = (data >> 14) & 1;
      unknown[2] = (data >> 15) & 1;
      unknown[3] = (data >> 22) & 1;
      boolean moreDataFollows = ((data >> 23) & 1) > 0;
      unknown[4] = (data >> 24) & 7;
      unknown[5] = (data >> 27) & 7;
      unknown[6] = (data >> 30) & 1;  // Rx only ?
      unknown[7] = (data >> 31) & 1;

      protoBuilder.addAllUnknown(Ints.asList(unknown));

      if (moreDataFollows) {
        byte additionalDataBitmap = byteBuffer.get();
        // 00000001 ->
        // 00000110 ->
        // 00001000 ->
        // 00100000 ->
        if ((additionalDataBitmap & 1) > 0) {
           byte b[] = new byte[dataLengthSection.get(DataLength.COMM_ADDITIONAL_INFO_1)];
           byteBuffer.get(b);
           protoBuilder.setUnknownAdditionalInfo1(ByteString.copyFrom(b));
        }
        if ((additionalDataBitmap & 2) > 0) {
          byte b[] = new byte[dataLengthSection.get(DataLength.COMM_ADDITIONAL_INFO_2)];
          byteBuffer.get(b);
          protoBuilder.setUnknownAdditionalInfo2(ByteString.copyFrom(b));
        }
        if ((additionalDataBitmap & 4) > 0) {
          byte b[] = new byte[dataLengthSection.get(DataLength.COMM_ADDITIONAL_INFO_2)];
          byteBuffer.get(b);
          protoBuilder.setUnknownAdditionalInfo3(ByteString.copyFrom(b));
        }
        if ((additionalDataBitmap & 8) > 0) {
          byte b[] = new byte[dataLengthSection.get(DataLength.COMM_ADDITIONAL_INFO_3)];
          // @624Ac0 per [68C6D8]
          byteBuffer.get(b);
          protoBuilder.setUnknownAdditionalInfo4(ByteString.copyFrom(b));
        }
        if ((additionalDataBitmap & 0x10) > 0) {
          byte length = byteBuffer.get();
          ByteBuffer narrativeBuffer = byteBuffer.slice().asReadOnlyBuffer();
          narrativeBuffer.limit(length);
          byteBuffer.position(byteBuffer.position() + length);
          VariableLengthAsciiEncoding decoder = new VariableLengthAsciiEncoding(narrativeBuffer);
          protoBuilder.setNarrative(decoder.decode(0, NUM_NARRATIVE_LEADING_BITS));
        }
        if ((additionalDataBitmap & 0x20) > 0) {
          protoBuilder.setUnknownAdditionalInfo5(true);
        }
        if ((additionalDataBitmap & 0x40) > 0) {
          protoBuilder.setUnknownAdditionalInfo6(true);
        }
        if ((additionalDataBitmap & 0x80) > 0) {
          protoBuilder.setUnknownAdditionalInfo7(true);
        }
      }

      freqBuilder.withCommunicationFrequency(protoBuilder.build());
      builder.withCommunicationFrequency(freqBuilder.build());
    }
  }

  private void readApproachInfo(DataLengthSection dataLengthSection, ByteBuffer byteBuffer, LandingFacilityDetail.Builder builder) {
    // appears ordered by the 'best' approach ?
    int data = byteBuffer.get();
    // c0
    // 3e section 65 index (approach type)
    // 01
  }

  @Override
  public GarminOutput write(List<LandingFacilityDetail> landingFacilityDetails) {
    ImmutableMap.Builder<Integer, Integer> indexToOffset = ImmutableMap.builder();
    ByteArrayDataOutput output = new LittleEndianByteArrayDataOutput(ByteStreams.newDataOutput());
    for (int index = 0; index < landingFacilityDetails.size(); ++index) {
      int offset = output.toByteArray().length;
      indexToOffset.put(index, offset);
      // System.err.printf("Encoding detail offset %06d %06x (%06x)\n", offset, offset, 0x14ea0f + offset);
      encode(landingFacilityDetails.get(index), output);
    }
    return new GarminOutputAndIndexToOffset(output.toByteArray(), indexToOffset.build());
  }

  private void encode(LandingFacilityDetail detail, ByteArrayDataOutput output) {
    BitSet sectionsPresent = new BitSet(16);
    List<ByteArrayDataOutput> sections = new ArrayList<>();
    if (!detail.runways.isEmpty()) {
      sectionsPresent.set(0);
      ByteArrayDataOutput sectionOutput = new LittleEndianByteArrayDataOutput(ByteStreams.newDataOutput());
      sections.add(sectionOutput);
      encodeRunwayInfo(detail.runways, sectionOutput);
    }
    if (!detail.communicationFrequencies.isEmpty()) {
      sectionsPresent.set(1);
      ByteArrayDataOutput sectionOutput = new LittleEndianByteArrayDataOutput(ByteStreams.newDataOutput());
      sections.add(sectionOutput);
      encodeCommunicationInfo(detail.communicationFrequencies, sectionOutput);
    }

    // Temporary; pass through unknown sections.
    for (UnknownLandingFacilityDetailSection unknownSection : detail.protoLandingFacilityDetail.getUnknownSectionList()) {
      sectionsPresent.set(unknownSection.getSectionNumber());
      ByteArrayDataOutput sectionOutput = ByteStreams.newDataOutput();
      sectionOutput.write(unknownSection.getData().toByteArray());
      sections.add(sectionOutput);
    }

    output.writeShort(sectionsPresent.length() == 0
        ? 0
        : (short) sectionsPresent.toLongArray()[0]);
    // write lengths.
    for (ByteArrayDataOutput section : sections) {
      int sectionLength = section.toByteArray().length;
      output.writeShort(sectionLength);
    }
    // write contents.
    for (ByteArrayDataOutput section : sections) {
      output.write(section.toByteArray());
    }
  }

  private void encodeRunwayInfo(List<Runway> runways, ByteArrayDataOutput output) {
    for (Runway runway : runways) {
      Proto.Runway proto = runway.protoRunway;

      int data = ((runway.runwayNumberSuffix.getIndex() & 7) << 29)
          | ((proto.getRunwayNumber() & 0x1f) << 24)
          | ((proto.getUnknown(0) & 1) << 23)
          | ((runway.lighting.getIndex() & 7) << 20)
          | ((runway.surface.getIndex() & 0xf) << 16)
          | ((proto.getRunwayLengthFeet() & 0xffff));
      output.writeInt(data);

      data = ((proto.getRunwayWidthFeet() & 0x7ff) << 13)
          | ((proto.hasUnknownAdditionalInfo1() ? 1 : 0) << 12)
          | ((proto.hasUnknownAdditionalInfo2() ? 1 : 0) << 11)
          | ((proto.hasUnknownAdditionalInfo3() ? 1 : 0) << 10)
          | ((proto.getUnknown(1) & 1) << 9)
          | ((proto.getUnknown(2) & 1) << 8)
          | ((proto.getUnknown(3) & 0xff));
      output.writeShort(data & 0xffff);
      output.write((data >> 16) & 0xff);

      if (proto.hasUnknownAdditionalInfo1()) {
        output.write(proto.getUnknownAdditionalInfo1().toByteArray());
      }
      if (proto.hasUnknownAdditionalInfo2()) {
        output.write(proto.getUnknownAdditionalInfo2().toByteArray());
      }
      if (proto.hasUnknownAdditionalInfo3()) {
        output.write(proto.getUnknownAdditionalInfo3().toByteArray());
      }
    }
  }

  private void encodeCommunicationInfo(List<CommunicationFrequency> freqs, ByteArrayDataOutput output) {
    for (CommunicationFrequency freq : freqs) {
      Proto.CommunicationFrequency proto = freq.protoCommunicationFrequency;

      int additionalDataBitmap = (proto.hasUnknownAdditionalInfo1() ? 1 : 0)
          | ((proto.hasUnknownAdditionalInfo2() ? 1 : 0) << 1)
          | ((proto.hasUnknownAdditionalInfo3() ? 1 : 0) << 2)
          | ((proto.hasUnknownAdditionalInfo4() ? 1 : 0) << 3)
          | ((proto.hasNarrative() ? 1 : 0) << 4)
          | ((proto.getUnknownAdditionalInfo5() ? 1 : 0) << 5)
          | ((proto.getUnknownAdditionalInfo6() ? 1 : 0) << 6)
          | ((proto.getUnknownAdditionalInfo7() ? 1 : 0) << 7);

      int data = ((encodeFrequency(proto.getFrequencyGhz()) & 0xfff)
          | ((proto.getUnknown(0) & 3) << 12)
          | ((proto.getUnknown(1) & 1) << 14)
          | ((proto.getUnknown(2) & 1) << 15)
          | ((freq.frequencyType.getIndex()) & 0x3f) << 16)
          | ((proto.getUnknown(3) & 1) << 22)
          | (((additionalDataBitmap > 0) ? 1 : 0) << 23)
          | ((proto.getUnknown(4) & 7) << 24)
          | ((proto.getUnknown(5) & 7) << 27)
          | ((proto.getUnknown(6) & 1) << 30)
          | ((proto.getUnknown(7) & 1) << 31);
      output.writeInt(data);

      if (additionalDataBitmap > 0) {
        output.write(additionalDataBitmap & 0xff);
        if (proto.hasUnknownAdditionalInfo1()) {
          output.write(proto.getUnknownAdditionalInfo1().toByteArray());
        }
        if (proto.hasUnknownAdditionalInfo2()) {
          output.write(proto.getUnknownAdditionalInfo2().toByteArray());
        }
        if (proto.hasUnknownAdditionalInfo3()) {
          output.write(proto.getUnknownAdditionalInfo3().toByteArray());
        }
        if (proto.hasUnknownAdditionalInfo4()) {
          output.write(proto.getUnknownAdditionalInfo4().toByteArray());
        }
        if (proto.hasNarrative()) {
          BitListOutputStream bitListOutputStream = new BitListOutputStream();
          for (int i = 0; i < NUM_NARRATIVE_LEADING_BITS; ++i) {
            bitListOutputStream.writeBits(ImmutableList.of(false));
          }
          VariableLengthAsciiEncoding encoding = new VariableLengthAsciiEncoding(bitListOutputStream);
          encoding.encodeExtended(proto.getNarrative());
          byte b[] = bitListOutputStream.toByteArray();
          output.write(b.length & 0xff);
          output.write(b);
        }
      }
    }
  }

  int decodeFrequency(int data) {
    boolean hasRemainder = data % 5 > 0;
    int frequency = (data * 5 + 0xd2f0) * 2;
    return hasRemainder ? frequency + 5 : frequency;
  }

  int encodeFrequency(int frequencyGhz) {
    int data = (frequencyGhz / 2) - 0xd2f0;
    return data / 5;
  }
}
