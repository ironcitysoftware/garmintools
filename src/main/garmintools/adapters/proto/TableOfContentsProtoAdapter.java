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

package garmintools.adapters.proto;

import garmintools.Proto;
import garmintools.wrappers.TableOfContents;
import garmintools.wrappers.TableOfContentsEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * We store the "exception" TOC entries in order to preserve the original file.
 * "exception" entries are ones where the actual data takes more space than the TOC says it should.
 * TODO: there is probably a subtlety with the TOC semantics that is not understand.
 * Storing this should not be necessary.
 */
public class TableOfContentsProtoAdapter implements ProtoAdapter<TableOfContents> {
  @Override
  public TableOfContents read(Proto.NavigationData proto) {
    Map<Integer, TableOfContentsEntry> overrides = new HashMap<>();
    for (Proto.TableOfContentsOverride override : proto.getTableOfContents().getTableOfContentsOverrideList()) {
      TableOfContentsEntry entry = TableOfContentsEntry.newBuilder()
          .setSectionNumber(override.getSectionNumber())
          .setItemLength(override.getItemLength())
          .setItemQuantity(override.getItemQuantity())
          .build();
      overrides.put(override.getSectionNumber(), entry);
    }
    Map<Integer, Integer> emptySectionItemLength = new HashMap<>();
    for (Proto.TableOfContentsOverride override : proto.getTableOfContents().getEmptySectionItemLengthList()) {
      emptySectionItemLength.put(override.getSectionNumber(), override.getItemLength());
    }
    return new TableOfContents(proto.getTableOfContents().getNumSections(), overrides,
        emptySectionItemLength);
  }

  @Override
  public void write(TableOfContents data, Proto.NavigationData.Builder builder) {
    for (TableOfContentsEntry entry : data.toc.values()) {
      if (entry.actualLength != entry.itemLength * entry.itemQuantity) {
        builder.getTableOfContentsBuilder().addTableOfContentsOverrideBuilder()
            .setSectionNumber(entry.sectionNumber)
            .setItemLength(entry.itemLength)
            .setItemQuantity(entry.itemQuantity);
      }
    }
    for (Map.Entry<Integer, Integer> entry : data.emptySectionItemLengths.entrySet()) {
      builder.getTableOfContentsBuilder().addEmptySectionItemLengthBuilder()
            .setSectionNumber(entry.getKey())
            .setItemLength(entry.getValue());
    }
    builder.getTableOfContentsBuilder().setNumSections(data.numSections);
  }
}
