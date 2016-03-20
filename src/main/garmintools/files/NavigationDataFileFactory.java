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

package garmintools.files;

import garmintools.Proto.NavigationData;
import garmintools.adapters.nativo.MetadataNativeAdapter;
import garmintools.sections.Ids;
import garmintools.sections.SectionManager;
import garmintools.sections.TableOfContentsSection;
import garmintools.wrappers.TableOfContentsEntry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.CountingInputStream;
import com.google.common.primitives.Ints;

public class NavigationDataFileFactory {
  private final Logger logger = Logger.getLogger(getClass().getName());

  public NativeNavigationDataFile createFromNative(InputStream inputStream, long inputFileLength)
      throws IOException {
    CountingInputStream countingInputStream = new CountingInputStream(inputStream);
    SectionManager.NativeBuilder sectionManagerBuilder = new SectionManager.NativeBuilder();
    readSection(MetadataNativeAdapter.METADATA_TOC_ENTRY, countingInputStream, sectionManagerBuilder);
    sectionManagerBuilder.readTableOfContents(countingInputStream, Ints.checkedCast(inputFileLength));
    Collection<TableOfContentsEntry> tocEntries =
        ((TableOfContentsSection) sectionManagerBuilder.getSection(Ids.TABLE_OF_CONTENTS_SECTION))
            .getEntryMap().values();
    for (TableOfContentsEntry entry : tocEntries) {
      readSection(entry, countingInputStream, sectionManagerBuilder);
    }
    return new NativeNavigationDataFile(sectionManagerBuilder.build());
  }

  private void readSection(TableOfContentsEntry entry, CountingInputStream countingInputStream,
      SectionManager.NativeBuilder sectionManagerBuilder) throws IOException {
    Preconditions.checkState(countingInputStream.getCount() == entry.fileOffset);
    InputStream sectionInputStream = ByteStreams.limit(countingInputStream, entry.actualLength);
    ByteBuffer byteBuffer = ByteBuffer.wrap(ByteStreams.toByteArray(sectionInputStream))
        .order(ByteOrder.LITTLE_ENDIAN);
    logger.info(String.format("Reading section %d", entry.sectionNumber));
    sectionManagerBuilder.addSection(entry, byteBuffer);
    Preconditions.checkState(!byteBuffer.hasRemaining(),
        String.format("Trailing input (%d of %d bytes)", byteBuffer.remaining(), entry.actualLength));
  }

  public ProtoNavigationDataFile createFromProto(NavigationData proto) {
    SectionManager.ProtoBuilder sectionManagerBuilder = new SectionManager.ProtoBuilder(proto);
    return new ProtoNavigationDataFile(sectionManagerBuilder.build());
  }
}
