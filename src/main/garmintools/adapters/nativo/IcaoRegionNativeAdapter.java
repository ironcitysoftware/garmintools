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

import static garmintools.encoding.SixBitAsciiEncoding.SIMPLE_ENCODING;
import garmintools.Proto;
import garmintools.encoding.SixBitAsciiEncoding;
import garmintools.sections.DataLengthSection;
import garmintools.util.SizeUtil;
import garmintools.util.StringUtil;
import garmintools.wrappers.TableOfContentsEntry;

import java.nio.ByteBuffer;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

public class IcaoRegionNativeAdapter implements NativeAdapter<List<Proto.IcaoRegion>> {
  @Override
  public List<Proto.IcaoRegion> read(DataLengthSection dataLengthSection, TableOfContentsEntry entry, ByteBuffer byteBuffer) {
    ImmutableList.Builder<Proto.IcaoRegion> listBuilder = ImmutableList.builder();
    for (int index = 0; index < entry.itemQuantity; ++index) {
      byte item[] = new byte[entry.itemLength];
      byteBuffer.get(item);
      String concatenatedString = SIMPLE_ENCODING.decode(item);
      listBuilder.add(Proto.IcaoRegion.newBuilder()
          .setLandingFacilityIdentifierPrefix(concatenatedString.substring(0, 2))
          .setRegion(concatenatedString.substring(2).trim())
          .build());
    }
    return listBuilder.build();
  }

  @Override
  public NativeOutput write(List<Proto.IcaoRegion> icaoRegions) {
    int longestString = SizeUtil.getLargest(icaoRegions, SIZEOF);
    NativeOutput nativeOutput = new NativeOutput(icaoRegions.size(),
        SixBitAsciiEncoding.getEncodedSize(longestString));
    for (Proto.IcaoRegion icaoRegion : icaoRegions) {
      String text = icaoRegion.getLandingFacilityIdentifierPrefix() + icaoRegion.getRegion();
      nativeOutput.put(SIMPLE_ENCODING.encode(StringUtil.pad(text, longestString)));
    }
    return nativeOutput;
  }

  private static Function<Proto.IcaoRegion, Integer> SIZEOF = new Function<Proto.IcaoRegion, Integer>() {
    @Override
    public Integer apply(Proto.IcaoRegion icaoRegion) {
      return icaoRegion.getRegion().length() + icaoRegion.getLandingFacilityIdentifierPrefix().length();
    }
  };
}
