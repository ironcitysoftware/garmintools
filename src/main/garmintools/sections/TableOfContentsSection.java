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

package garmintools.sections;

import garmintools.adapters.nativo.NativeAdapter;
import garmintools.adapters.nativo.TableOfContentsNativeAdapter;
import garmintools.adapters.proto.ProtoAdapter;
import garmintools.adapters.proto.TableOfContentsProtoAdapter;
import garmintools.wrappers.TableOfContents;
import garmintools.wrappers.TableOfContentsEntry;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;

/**
 * The table of contents of a native NavData file maps well-known sections to subsets of the file.
 * For some sections, the declared length doesn't match the actual length.
 * We retain the correct lengths in order to read each section's portion of the InputStream fully.
 * If we move to a random-access model this would not be necessary.
 */
public class TableOfContentsSection extends Section<TableOfContents> {
  TableOfContentsSection(int sectionNumber, TableOfContents data,
      NativeAdapter<TableOfContents> nativeAdapter,
      ProtoAdapter<TableOfContents> protoAdapter) {
    super(sectionNumber, data, nativeAdapter, protoAdapter);
  }

  public Map<Integer, TableOfContentsEntry> getEntryMap() {
    return data.toc;
  }

  public int getNativeSize() {
    return ((TableOfContentsNativeAdapter) nativeAdapter).getNativeSize(data.numSections);
  }

  public void insert(int sectionNumber, int itemLength, int itemQuantity, int fileOffset) {
    // If there is already an entry, its length and quantity are canonical.
    if (data.toc.containsKey(sectionNumber)) {
      itemLength = data.toc.get(sectionNumber).itemLength;
      itemQuantity = data.toc.get(sectionNumber).itemQuantity;
    }
    TableOfContentsEntry entry = TableOfContentsEntry.newBuilder()
        .setItemLength(itemLength)
        .setItemQuantity(itemQuantity)
        .setSectionNumber(sectionNumber)
        .setFileOffset(fileOffset)
        .build();
    data.toc.put(sectionNumber, entry);
  }

  @Override
  public String toString() {
    List<String> entries = new ArrayList<>();
    /** ImmutableMap preserves insertion ordering, which is by ascending file offset. */
    for (TableOfContentsEntry entry : data.toc.values()) {
      entries.add(entry.toString());
    }
    return Joiner.on('\n').join(entries);
  }

  static class Factory extends SectionFactory<TableOfContents> {
    Factory() {
      super(Ids.TABLE_OF_CONTENTS_SECTION,
          new TableOfContentsNativeAdapter(),
          new TableOfContentsProtoAdapter(),
          TableOfContentsSection.class);
    }

    public TableOfContentsSection createFromNative(InputStream inputStream, int inputFileLength)
        throws IOException {
      TableOfContents data = ((TableOfContentsNativeAdapter) nativeAdapter).read(inputStream, inputFileLength);
      return new TableOfContentsSection(sectionNumber, data, nativeAdapter, protoAdapter);
    }
  }
}
