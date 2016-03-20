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

import garmintools.sections.DataLengthSection;
import garmintools.wrappers.TableOfContents;
import garmintools.wrappers.TableOfContentsEntry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.primitives.Ints;

public class TableOfContentsNativeAdapter implements NativeAdapter<TableOfContents> {
  /** TODO: handle a different number of sections, item lengths, or location of the TOC. */
  public static final int TABLE_OF_CONTENTS_OFFSET = 0x200;
  private static final int MAX_ITEM_QUANTITY = 108;
  private static final int ITEM_LENGTH = 8;
  private static final TableOfContentsEntry SECTION_NOT_PRESENT_ENTRY = TableOfContentsEntry.newBuilder()
      .setFileOffset(0)
      .setItemLength(0)
      .setItemQuantity(0)
      .build();

  @Override
  public TableOfContents read(DataLengthSection unused,
      TableOfContentsEntry unusedToc, ByteBuffer unusedBuffer) {
    throw new UnsupportedOperationException("Use read(originalInputStream, inputFileLength)");
  }

  public int getNativeSize(int numEntries) {
    return numEntries * ITEM_LENGTH;
  }

  /**
   * Reading the native table of contents does not fit well into the NativeAdapter.read interface
   * for two reasons:
   * 1) the first entry in the TOC is for the TOC itself, so the length is not known apriori.
   * 2) it is useful to know the input file length to construct the TOC, and this is not available
   *    in the traditional NativeAdapter.read interface.
   */
  public TableOfContents read(InputStream originalInputStream, int inputFileLength)
      throws IOException {
    LittleEndianDataInputStream inputStream = new LittleEndianDataInputStream(originalInputStream);
    TableOfContentsEntry toc = readTableOfContentsEntry(inputStream);
    Preconditions.checkState(toc.itemQuantity <= MAX_ITEM_QUANTITY);
    Preconditions.checkState(toc.itemLength == ITEM_LENGTH);
    Map<Integer, TableOfContentsEntry> offsetToEntries = new HashMap<>();
    ImmutableMap.Builder<Integer, Integer> emptySectionItemLengths = ImmutableMap.builder();
    for (int sectionNumber = 0; sectionNumber < toc.itemQuantity - 1; ++sectionNumber) {
      TableOfContentsEntry entry = TableOfContentsEntry
          .newBuilder(readTableOfContentsEntry(inputStream))
          .setSectionNumber(sectionNumber)
          .build();
      if (entry.fileOffset == 0) {
        if (entry.itemLength > 0) {
          emptySectionItemLengths.put(sectionNumber, entry.itemLength);
        }
        continue;  // the section is not present in the file.
      }
      offsetToEntries.put(entry.fileOffset, entry);
    }
    List<Integer> orderedDeclaredOffsets = new ArrayList<Integer>(new TreeSet<Integer>(offsetToEntries.keySet()));
    orderedDeclaredOffsets.add(inputFileLength);
    ImmutableMap.Builder<Integer, TableOfContentsEntry> mapBuilder = ImmutableMap.builder();
    for (int i = 0; i < orderedDeclaredOffsets.size() - 1; ++i) {
      int currentOffset = orderedDeclaredOffsets.get(i);
      int nextOffset = orderedDeclaredOffsets.get(i + 1);
      TableOfContentsEntry entry = TableOfContentsEntry
          .newBuilder(offsetToEntries.get(currentOffset))
          .setActualLength(Ints.checkedCast(nextOffset - currentOffset))
          .build();
      mapBuilder.put(entry.sectionNumber, entry);
    }
    return new TableOfContents(toc.itemQuantity, mapBuilder.build(), emptySectionItemLengths.build());
  }

  private TableOfContentsEntry readTableOfContentsEntry(LittleEndianDataInputStream inputStream)
      throws IOException {
    TableOfContentsEntry.Builder builder = TableOfContentsEntry.newBuilder();
    int data = inputStream.readInt();
    // 00ffffff -> file offset
    // ff000000 -> high item length
    builder.setFileOffset(data & 0xffffff);
    int itemLength = (data >> 16) & 0xff00;

    data = inputStream.readInt();
    // 000000ff -> low item length
    // ffffff00 -> item quantity
    builder
        .setItemLength(itemLength | (data & 0xff))
        .setItemQuantity(data >> 8);
    return builder.build();
  }

  /**
   * The TOC entry itself and the other section entries compose the TOC.
   * The supplied data does not contain the TOC entry itself.
   */
  @Override
  public NativeOutput write(TableOfContents data) {
    NativeOutput nativeOutput = new NativeOutput(data.numSections, ITEM_LENGTH);
    TableOfContentsEntry toc = TableOfContentsEntry.newBuilder()
        .setFileOffset(TABLE_OF_CONTENTS_OFFSET)
        .setItemLength(ITEM_LENGTH)
        .setItemQuantity(data.numSections)
        .build();
    writeTableOfContentsEntry(toc, nativeOutput);

    // The TOC TOC counts as one, so only n - 1 actual sections are present in the TOC.
    for (int sectionNumber = 0; sectionNumber < data.numSections - 1; ++sectionNumber) {
      if (data.toc.containsKey(sectionNumber)) {
        writeTableOfContentsEntry(data.toc.get(sectionNumber), nativeOutput);
      } else {
        writeTableOfContentsEntry(
            getSectionNotPresentEntry(sectionNumber,
                data.emptySectionItemLengths.get(sectionNumber)),
            nativeOutput);
      }
    }

    return nativeOutput;
  }

  private TableOfContentsEntry getSectionNotPresentEntry(int sectionNumber, Integer itemLength) {
    TableOfContentsEntry.Builder builder = TableOfContentsEntry.newBuilder(SECTION_NOT_PRESENT_ENTRY);
    if (itemLength != null) {
      builder.setItemLength(itemLength);
    }
    return builder.build();
  }

  private void writeTableOfContentsEntry(TableOfContentsEntry entry, NativeOutput nativeOutput) {
    Preconditions.checkState(entry.fileOffset <= 0xffffff);
    Preconditions.checkState(entry.itemLength <= 0xffff);
    Preconditions.checkState(entry.itemQuantity <= 0xffffff);
    int data = ((entry.itemLength & 0xff00) << 16) | (entry.fileOffset & 0xffffff);
    nativeOutput.putInt(data);
    data = (entry.itemQuantity << 8) | (entry.itemLength & 0xff);
    nativeOutput.putInt(data);
  }
}
