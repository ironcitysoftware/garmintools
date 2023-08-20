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

import static garmintools.encoding.SixBitAsciiEncoding.SIMPLE_ENCODING;
import garmintools.Proto;
import garmintools.encoding.LittleEndianByteArrayDataOutput;
import garmintools.encoding.SixBitAsciiEncoding;
import garmintools.sections.DataLengthSection;
import garmintools.sections.Ids;
import garmintools.util.StringUtil;
import garmintools.wrappers.TableOfContentsEntry;

import java.nio.ByteBuffer;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class MetadataGarminAdapter implements GarminAdapter<Proto.Metadata> {
  /** Metadata does not have a TOC entry, so use a pseudo one for consistency. */
  public static final TableOfContentsEntry METADATA_TOC_ENTRY = TableOfContentsEntry.newBuilder()
      .setSectionNumber(Ids.METADATA_SECTION)
      .setFileOffset(0)
      .setItemLength(1)
      .setItemQuantity(512)
      .setActualLength(512)
      .build();
  private static final int DEFAULT_PREAMBLE_LENGTH = 128;
  private static final int PART_NUMBER_LENGTH = 16;
  private static final int COVERAGE_REGION_LENGTH = 30;
  private static final int COVERAGE_REGION_PAD_LENGTH = 25;
  private static final int COPYRIGHT_LINE_LENGTH = 25;
  private static final int INTERMEDIATE_ZERO_BYTE_LENGTH = 65;
  private static final int TRAILING_ZERO_BYTE_LENGTH = 206;

  @Override
  public Proto.Metadata read(DataLengthSection dataLengthSection, TableOfContentsEntry entry, ByteBuffer byteBuffer) {
    Proto.Metadata.Builder builder = Proto.Metadata.newBuilder();
    int preambleLength = byteBuffer.get() & 0xff;
    byteBuffer.position(preambleLength + 1);

    builder.setCycleNumber(byteBuffer.getShort());
    builder.setEffectiveDate(readDate(byteBuffer));
    builder.setExpiresDate(readDate(byteBuffer));
    builder.setAeronauticalDataSnapshotDate(readDate(byteBuffer));

    builder.setUnknownData1(byteBuffer.get());
    builder.setUnknownData2(byteBuffer.getShort());
    builder.setUnknownData3(byteBuffer.get());

    byte partNumberBytes[] = new byte[SixBitAsciiEncoding.getEncodedSize(PART_NUMBER_LENGTH)];
    byteBuffer.get(partNumberBytes);
    builder.setPartNumber(SIMPLE_ENCODING.decode(partNumberBytes).trim());

    byte coverageRegionBytes[] = new byte[COVERAGE_REGION_LENGTH];
    byteBuffer.get(coverageRegionBytes);
    builder.setCoverageRegion(new String(coverageRegionBytes, Charsets.US_ASCII).trim());

    byte copyrightLineBytes[] = new byte[COPYRIGHT_LINE_LENGTH];
    byteBuffer.get(copyrightLineBytes);
    builder.setCopyrightLine1(new String(copyrightLineBytes, Charsets.US_ASCII).trim());
    byteBuffer.get(copyrightLineBytes);
    builder.setCopyrightLine2(new String(copyrightLineBytes, Charsets.US_ASCII).trim());

    builder.setUnknownData4(byteBuffer.get());

    for (int i = 0; i < INTERMEDIATE_ZERO_BYTE_LENGTH; ++i) {
      Preconditions.checkState(byteBuffer.get() == 0);
    }

    builder.setUnknownData5(byteBuffer.get());

    while(byteBuffer.hasRemaining()) {
      Preconditions.checkState(byteBuffer.get() == 0);
    }

    return builder.build();
  }

  @Override
  public GarminOutput write(Proto.Metadata data) {
    ByteArrayDataOutput output = new LittleEndianByteArrayDataOutput(ByteStreams.newDataOutput());
    writePreamble(output);
    writeMetadata(data, output);
    return new GarminOutput(output.toByteArray());
  }

  private void writePreamble(ByteArrayDataOutput output) {
    output.write(DEFAULT_PREAMBLE_LENGTH);
    for (int i = 0; i < DEFAULT_PREAMBLE_LENGTH; ++i) {
      output.write(i);
    }
  }

  private void writeMetadata(Proto.Metadata data, ByteArrayDataOutput output) {
    output.writeShort(data.getCycleNumber());
    writeDate(output, data.getEffectiveDate());
    writeDate(output, data.getExpiresDate());
    writeDate(output, data.getAeronauticalDataSnapshotDate());
    output.write(data.getUnknownData1());
    output.writeShort(data.getUnknownData2());
    output.write(data.getUnknownData3());
    output.write(SIMPLE_ENCODING.encode(StringUtil.pad(data.getPartNumber(), PART_NUMBER_LENGTH)));
    writeStringAndPadWithSpace(output,
        StringUtil.pad(data.getCoverageRegion(), COVERAGE_REGION_PAD_LENGTH), COVERAGE_REGION_LENGTH);
    writeStringAndPadWithSpace(output, data.getCopyrightLine1(), COPYRIGHT_LINE_LENGTH);
    writeStringAndPadWithSpace(output, data.getCopyrightLine2(), COPYRIGHT_LINE_LENGTH);
    output.write(data.getUnknownData4());
    for (int i = 0; i < INTERMEDIATE_ZERO_BYTE_LENGTH; ++i) {
      output.write(0);
    }
    output.write(data.getUnknownData5());
    for (int i = 0; i < TRAILING_ZERO_BYTE_LENGTH; ++i) {
      output.write(0);
    }
  }

  private void writeStringAndPadWithSpace(ByteArrayDataOutput output, String string, int paddedLength) {
    byte b[] = string.getBytes(Charsets.US_ASCII);
    output.write(b);
    output.write(0);
    Preconditions.checkArgument(b.length + 1 <= paddedLength);
    for (int i = b.length + 1; i < paddedLength; ++i) {
      output.write(32);
    }
  }

  private void writeDate(ByteArrayDataOutput output, Proto.Date date) {
    output.write(date.getMonth());
    output.write(date.getDay());
    output.writeShort(date.getYear());
  }

  private Proto.Date readDate(ByteBuffer byteBuffer) {
    return Proto.Date.newBuilder()
        .setMonth(byteBuffer.get())
        .setDay(byteBuffer.get())
        .setYear(byteBuffer.getShort())
        .build();
  }
}
