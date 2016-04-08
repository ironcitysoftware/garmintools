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

import static garmintools.encoding.SixBitAsciiEncoding.COMPLEX_ENCODING;
import garmintools.encoding.SixBitAsciiEncoding;
import garmintools.sections.DataLengthSection;
import garmintools.util.SizeUtil;
import garmintools.util.StringUtil;
import garmintools.wrappers.TableOfContentsEntry;

import java.nio.ByteBuffer;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class NavigationFixGarminAdapter implements GarminAdapter<List<String>> {
  @Override
  public List<String> read(DataLengthSection dataLengthSection, TableOfContentsEntry entry, ByteBuffer byteBuffer) {
    ImmutableList.Builder<String> listBuilder = ImmutableList.builder();
    for (int index = 0; index < entry.itemQuantity; ++index) {
      byte item[] = new byte[entry.itemLength];
      byteBuffer.get(item);
      listBuilder.add(COMPLEX_ENCODING.decode(item).trim());
    }
    return listBuilder.build();
  }

  @Override
  public GarminOutput write(List<String> navigationFixes) {
    int longestString = SizeUtil.getLongestLength(navigationFixes);
    GarminOutput output = new GarminOutput(navigationFixes.size(),
        SixBitAsciiEncoding.getEncodedSize(longestString));
    for (String navigationFix : navigationFixes) {
      output.put(COMPLEX_ENCODING.encode(StringUtil.pad(navigationFix, longestString)));
    }
    return output;
  }
}
