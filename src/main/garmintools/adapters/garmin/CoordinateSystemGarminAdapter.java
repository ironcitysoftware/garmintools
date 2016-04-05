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

import static garmintools.encoding.SixBitAsciiEncoding.SIMPLE_ENCODING;
import garmintools.Proto;
import garmintools.encoding.SixBitAsciiEncoding;
import garmintools.sections.DataLengthSection;
import garmintools.util.StringUtil;
import garmintools.wrappers.TableOfContentsEntry;

import java.nio.ByteBuffer;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

public class CoordinateSystemGarminAdapter implements GarminAdapter<List<Proto.CoordinateSystem>> {
  // TODO: find DataLength constant
  private static final int ENCODED_COORDINATE_SYSTEM_NAME_WIDTH = 15;
  private static final int NUM_PARAMETERS = 7;

  @Override
  public List<Proto.CoordinateSystem> read(DataLengthSection dataLengthSection, TableOfContentsEntry entry, ByteBuffer byteBuffer) {
    Preconditions.checkArgument(entry.itemLength >= ENCODED_COORDINATE_SYSTEM_NAME_WIDTH);
    ImmutableList.Builder<Proto.CoordinateSystem> listBuilder = ImmutableList.builder();
    for (int index = 0; index < entry.itemQuantity; ++index) {
      byte item[] = new byte[entry.itemLength];
      byteBuffer.get(item);

      Proto.CoordinateSystem.Builder builder = Proto.CoordinateSystem.newBuilder();
      byte nameData[] = slice(item, 0, ENCODED_COORDINATE_SYSTEM_NAME_WIDTH);
      byte parameterData[] = slice(item, ENCODED_COORDINATE_SYSTEM_NAME_WIDTH, item.length);
      builder.setName(SIMPLE_ENCODING.decode(nameData).trim());

      int parameters[] = new int[parameterData.length];
      for (int i = 0; i < parameterData.length; ++i) {
        parameters[i] = parameterData[i] & 0xff;
      }
      builder.addAllParameters(Ints.asList(parameters));
      listBuilder.add(builder.build());
    }
    return listBuilder.build();
  }

  private byte[] slice(byte data[], int start, int end) {
    return Bytes.toArray(Bytes.asList(data).subList(start, end));
  }

  @Override
  public GarminOutput write(List<Proto.CoordinateSystem> coordinateSystems) {
    GarminOutput output = new GarminOutput(
        coordinateSystems.size(), ENCODED_COORDINATE_SYSTEM_NAME_WIDTH + NUM_PARAMETERS);
    for (Proto.CoordinateSystem coordinateSystem : coordinateSystems) {
      output.put(SIMPLE_ENCODING.encode(
          StringUtil.pad(coordinateSystem.getName(),
              SixBitAsciiEncoding.getDecodedSize(ENCODED_COORDINATE_SYSTEM_NAME_WIDTH))));
      for (int parameter : coordinateSystem.getParametersList()) {
        output.put((byte) parameter);
      }
    }
    return output;
  }
}
