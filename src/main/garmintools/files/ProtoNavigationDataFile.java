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

import garmintools.adapters.garmin.GarminOutput;
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

  public void writeToGarmin(FileOutputStream outputStream) throws IOException {
    WritableByteChannel channel = Channels.newChannel(outputStream);
    Map<Integer, GarminOutput> sectionToOutput = sectionManager.getGarminOutputs();
    List<GarminOutput> outputs = new ArrayList<>();

    GarminOutput metadata = sectionManager.getMetadataSection().getSectionBytes(sectionManager);
    outputs.add(metadata);

    TableOfContentsSection tocSection = sectionManager.getTableOfContentsSection();
    // We need to know the size of the TOC section to insert the proper offsets into the TOC.
    int fileOffset = metadata.size() + tocSection.getSize();
    for (Map.Entry<Integer, GarminOutput> entry : sectionToOutput.entrySet()) {
      GarminOutput output = entry.getValue();
      tocSection.insert(
          entry.getKey(), output.getItemLength(), output.getItemQuantity(), fileOffset);
      fileOffset += output.size();
    }
    outputs.add(sectionManager.getTableOfContentsSection().getSectionBytes(sectionManager));
    outputs.addAll(sectionToOutput.values());

    for (GarminOutput output : outputs) {
      output.write(channel);
    }
  }
}
