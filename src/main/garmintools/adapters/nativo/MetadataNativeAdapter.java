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

import static garmintools.encoding.SixBitAsciiEncoding.SIMPLE_ENCODING;
import garmintools.Proto;
import garmintools.sections.DataLengthSection;
import garmintools.sections.Ids;
import garmintools.util.StringUtil;
import garmintools.wrappers.TableOfContentsEntry;

import java.nio.ByteBuffer;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.protobuf.ByteString;

public class MetadataNativeAdapter implements NativeAdapter<Proto.Metadata> {
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

  @Override
  public Proto.Metadata read(DataLengthSection dataLengthSection, TableOfContentsEntry entry, ByteBuffer byteBuffer) {
    Proto.Metadata.Builder builder = Proto.Metadata.newBuilder();
    int preambleLength = byteBuffer.get() & 0xff;
    byteBuffer.position(preambleLength + 1);

    byte unknown[] = new byte[18];
    byteBuffer.get(unknown);
    builder.setUnknownData1(ByteString.copyFrom(unknown));

    byte partNumberBytes[] = new byte[12];
    byteBuffer.get(partNumberBytes);
    builder.setPartNumber(SIMPLE_ENCODING.decode(partNumberBytes).trim());

    unknown = new byte[353];
    byteBuffer.get(unknown);
    builder.setUnknownData2(ByteString.copyFrom(unknown));

    Preconditions.checkState(!byteBuffer.hasRemaining());
    return builder.build();
  }

  @Override
  public NativeOutput write(Proto.Metadata data) {
    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    writePreamble(output);
    writeMetadata(data, output);
    return new NativeOutput(output.toByteArray());
  }

  private void writePreamble(ByteArrayDataOutput output) {
    output.write(DEFAULT_PREAMBLE_LENGTH);
    for (int i = 0; i < DEFAULT_PREAMBLE_LENGTH; ++i) {
      output.write(i);
    }
  }

  private void writeMetadata(Proto.Metadata data, ByteArrayDataOutput output) {
    output.write(data.getUnknownData1().toByteArray());
    output.write(SIMPLE_ENCODING.encode(StringUtil.pad(data.getPartNumber(), PART_NUMBER_LENGTH)));
    output.write(data.getUnknownData2().toByteArray());
  }
}
