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
import garmintools.Proto.UnparsedSection;
import garmintools.sections.DataLengthSection;
import garmintools.wrappers.TableOfContentsEntry;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;

public class UnparsedSectionNativeAdapter implements NativeAdapter<Proto.UnparsedSection> {
  private final int sectionNumber;

  public UnparsedSectionNativeAdapter(int sectionNumber) {
    this.sectionNumber = sectionNumber;
  }

  @Override
  public UnparsedSection read(DataLengthSection dataLengthSection, TableOfContentsEntry entry, ByteBuffer byteBuffer) {
    Proto.UnparsedSection.Builder builder = Proto.UnparsedSection.newBuilder();
    builder.setSectionNumber(sectionNumber);
    builder.setData(ByteString.copyFrom(byteBuffer));
    builder.setItemLength(entry.itemLength);
    builder.setItemQuantity(entry.itemQuantity);
    return builder.build();
  }

  @Override
  public NativeOutput write(UnparsedSection unparsedSection) {
    NativeOutput nativeOutput =
        new NativeOutput(unparsedSection.getItemQuantity(), unparsedSection.getItemLength(),
            unparsedSection.getData().size());
    nativeOutput.put(unparsedSection.getData().asReadOnlyByteBuffer());
    return nativeOutput;
  }
}
