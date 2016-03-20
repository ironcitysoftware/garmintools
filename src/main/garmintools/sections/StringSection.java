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
import garmintools.adapters.nativo.NativeOutput;
import garmintools.adapters.nativo.StringNativeAdapter;
import garmintools.adapters.nativo.StringNativeAdapter.NativeOutputAndKeys;
import garmintools.adapters.proto.ProtoAdapter;
import garmintools.adapters.proto.StringProtoAdapter;
import garmintools.keys.IndexForeignKey;
import garmintools.keys.VariableLengthEncodingForeignKey;
import garmintools.wrappers.StringAndKey;
import garmintools.wrappers.TableOfContentsEntry;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class StringSection extends Section<List<String>> {
  private Map<Integer, VariableLengthEncodingForeignKey> indexToWrittenKey;
  private List<VariableLengthEncodingForeignKey> readKeys;
  private final HashMap<String, IndexForeignKey> hashCodeCache = new HashMap<>();

  // read from proto
  public StringSection(int sectionNumber, List<String> data,
      NativeAdapter<List<String>> nativeAdapter,
      ProtoAdapter<List<String>> protoAdapter) {
    super(sectionNumber, data, nativeAdapter, protoAdapter);
  }

  // read from native
  public StringSection(int sectionNumber, List<String> data, List<VariableLengthEncodingForeignKey> keys, NativeAdapter<List<String>> nativeAdapter, ProtoAdapter<List<String>> protoAdapter) {
    super(sectionNumber, data, nativeAdapter, protoAdapter);
    readKeys = keys;
  }

  public String lookup(VariableLengthEncodingForeignKey key) {
    Preconditions.checkState(readKeys != null);
    return data.get(readKeys.indexOf(key));
  }

  public IndexForeignKey lookupOrInsert(String text) {
    // TODO: consider changing T to Map<String, IFK>
    if (hashCodeCache.size() != data.size()) {
      Preconditions.checkState(hashCodeCache.isEmpty() && !data.isEmpty());
      for (int index = 0; index < data.size(); ++index) {
        hashCodeCache.put(data.get(index), new IndexForeignKey(index));
      }
    }
    if (!hashCodeCache.containsKey(text)) {
      data.add(text);
      hashCodeCache.put(text, new IndexForeignKey(data.size() - 1));
    }
    return hashCodeCache.get(text);
  }

  // Resolves unsorted index to written VLE key.
  public VariableLengthEncodingForeignKey getKeyForIndex(IndexForeignKey key) {
    return indexToWrittenKey.get(key.getIndex());
  }

  @Override
  public NativeOutput getNativeBytes(SectionManager sectionManager) {
    List<String> sortedData = new ArrayList<>(data);
    Collections.sort(sortedData);  // Not sure if this is required, but native strings are sorted.
    NativeOutputAndKeys nativeOutput = (NativeOutputAndKeys) nativeAdapter.write(sortedData);

    ImmutableMap.Builder<Integer, VariableLengthEncodingForeignKey> mapBuilder = ImmutableMap.builder();
    for (int index = 0; index < nativeOutput.keys.size(); index++) {
      String string = data.get(index);
      int sortedIndex = sortedData.indexOf(string);
      mapBuilder.put(sortedIndex, nativeOutput.keys.get(index));
    }
    indexToWrittenKey = mapBuilder.build();
    return nativeOutput;
  }

  static class Factory extends SectionFactory<List<String>> {
    Factory() {
      super(Ids.STRING_SECTION,
          new StringNativeAdapter(),
          new StringProtoAdapter(),
          StringSection.class);
    }

    @Override
    public Section<List<String>> createFromNative(DataLengthSection dataLengthSection, TableOfContentsEntry entry, ByteBuffer byteBuffer) {
      List<StringAndKey> stringAndKey = ((StringNativeAdapter) nativeAdapter).readWithKeys(dataLengthSection, entry, byteBuffer);
      ImmutableList.Builder<String> strings = ImmutableList.builder();
      ImmutableList.Builder<VariableLengthEncodingForeignKey> keys = ImmutableList.builder();
      for (int index = 0; index < stringAndKey.size(); ++index) {
        strings.add(stringAndKey.get(index).string);
        keys.add(stringAndKey.get(index).key);
      }
      return new StringSection(
          sectionNumber,
          strings.build(),
          keys.build(),
          nativeAdapter,
          protoAdapter);
    }
  }
}
