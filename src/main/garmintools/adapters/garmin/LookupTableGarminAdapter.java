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

import garmintools.sections.DataLengthSection;
import garmintools.sections.Ids;
import garmintools.util.SizeUtil;
import garmintools.util.StringUtil;
import garmintools.wrappers.TableOfContentsEntry;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class LookupTableGarminAdapter implements GarminAdapter<List<String>> {
  private final int sectionNumber;

  // TODO: find correct DataLength for these
  private static final Map<Integer, Integer> DEFAULT_WIDTHS = ImmutableMap.of(
    Ids.RUNWAY_LIGHTING_SECTION, 10,
    Ids.GENERIC_AIRPORT_STRING_SECTION2, 15,
    Ids.GPS_APPROACH_TYPE_SECTION1, 15,
    Ids.GPS_APPROACH_TYPE_SECTION2, 7
  );

  public LookupTableGarminAdapter(int sectionNumber) {
    this.sectionNumber = sectionNumber;
  }

  @Override
  public List<String> read(DataLengthSection dataLengthSection, TableOfContentsEntry entry, ByteBuffer byteBuffer) {
    ImmutableList.Builder<String> listBuilder = ImmutableList.builder();
    for (int i = 0; i < entry.itemQuantity; ++i) {
      String string = "";
      for (int j = 0; j < entry.itemLength; ++j) {
        string += Character.toString((char) byteBuffer.get());
      }
      listBuilder.add(string.trim());
    }
    return listBuilder.build();
  }

  @Override
  public GarminOutput write(List<String> strings) {
    int width;
    if (DEFAULT_WIDTHS.containsKey(sectionNumber)) {
      width = DEFAULT_WIDTHS.get(sectionNumber);
    } else {
      width = SizeUtil.getLongestLength(strings);
    }
    GarminOutput output = new GarminOutput(strings.size(), width);
    for (String string : strings) {
      String paddedString = StringUtil.pad(string, width);
      for (byte b : paddedString.getBytes(Charsets.US_ASCII)) {
        output.put(b);
      }
    }
    return output;
  }
}
