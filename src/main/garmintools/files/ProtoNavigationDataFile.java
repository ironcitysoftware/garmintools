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

import garmintools.adapters.nativo.NativeOutput;
import garmintools.sections.SectionManager;
import garmintools.sections.TableOfContentsSection;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProtoNavigationDataFile {
  private final SectionManager sectionManager;

  ProtoNavigationDataFile(SectionManager sectionManager) {
    this.sectionManager = sectionManager;
  }

  public void writeToNative(FileOutputStream outputStream) throws IOException {
    WritableByteChannel channel = Channels.newChannel(outputStream);
    Map<Integer, NativeOutput> sectionToOutput = sectionManager.getNativeBytes();
    List<NativeOutput> outputs = new ArrayList<>();

    NativeOutput metadata = sectionManager.getMetadataSection().getNativeBytes(sectionManager);
    outputs.add(metadata);

    TableOfContentsSection tocSection = sectionManager.getTableOfContentsSection();
    // We need to know the size of the TOC section to insert the proper offsets into the TOC.
    int fileOffset = metadata.size() + tocSection.getNativeSize();
    for (Map.Entry<Integer, NativeOutput> entry : sectionToOutput.entrySet()) {
      NativeOutput nativeOutput = entry.getValue();
      tocSection.insert(
          entry.getKey(), nativeOutput.getItemLength(), nativeOutput.getItemQuantity(), fileOffset);
      fileOffset += nativeOutput.size();
    }
    outputs.add(sectionManager.getTableOfContentsSection().getNativeBytes(sectionManager));
    outputs.addAll(sectionToOutput.values());

    for (NativeOutput output : outputs) {
      output.write(channel);
    }
  }
}
