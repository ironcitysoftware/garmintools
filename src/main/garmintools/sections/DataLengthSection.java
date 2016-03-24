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

import garmintools.adapters.nativo.DataLengthNativeAdapter;
import garmintools.adapters.nativo.NativeAdapter;
import garmintools.adapters.proto.DataLengthProtoAdapter;
import garmintools.adapters.proto.ProtoAdapter;

import java.util.List;

public class DataLengthSection extends Section<List<Integer>> {
  DataLengthSection(int sectionNumber, List<Integer> data,
      NativeAdapter<List<Integer>> nativeAdapter,
      ProtoAdapter<List<Integer>> protoAdapter) {
    super(sectionNumber, data, nativeAdapter, protoAdapter);
  }

  public enum DataLength {
    UNKNOWN_0,  // size of lengths in details record (2)
    UNKNOWN_1,
    UNKNOWN_2,
    RUNWAY_ADDITIONAL_INFO_2,
    UNKNOWN_4,
    UNKNOWN_5,
    UNKNOWN_6,
    UNKNOWN_7,
    UNKNOWN_8,
    COMM_ADDITIONAL_INFO_1,
    COMM_ADDITIONAL_INFO_2,
    COMM_ADDITIONAL_INFO_3,
    UNKNOWN_12,
    UNKNOWN_13,
    UNKNOWN_14,
    UNKNOWN_15,
    UNKNOWN_16,
    UNKNOWN_17,
    UNKNOWN_18,
    UNKNOWN_19,
    UNKNOWN_20,
    UNKNOWN_21,
    UNKNOWN_22,
    UNKNOWN_23,
    UNKNOWN_24,
    UNKNOWN_25,
    UNKNOWN_26,
    UNKNOWN_27,
    UNKNOWN_28,
    UNKNOWN_29,
    UNKNOWN_30,
    UNKNOWN_31,
    UNKNOWN_32,
    UNKNOWN_33,
    UNKNOWN_34,
    UNKNOWN_35,
    UNKNOWN_36,
    UNKNOWN_37,
    UNKNOWN_38,
    UNKNOWN_39,
    UNKNOWN_40,
    UNKNOWN_41,
    UNKNOWN_42,
    UNKNOWN_43,
    UNKNOWN_44,
    UNKNOWN_45,
    UNKNOWN_46,
    UNKNOWN_47,
    UNKNOWN_48,
    UNKNOWN_49,
    UNKNOWN_50,
    UNKNOWN_51,
    UNKNOWN_52,
    UNKNOWN_53,
    UNKNOWN_54,
    UNKNOWN_55,
    UNKNOWN_56,
    UNKNOWN_57,
    UNKNOWN_58,
    UNKNOWN_59,
    UNKNOWN_60,
    UNKNOWN_61,
    UNKNOWN_62,
    UNKNOWN_63,
    UNKNOWN_64,
    UNKNOWN_65,
    UNKNOWN_66,
    UNKNOWN_67,
    UNKNOWN_68,
    UNKNOWN_69,
    UNKNOWN_70,
    UNKNOWN_71,
    UNKNOWN_72,
    UNKNOWN_73,
    UNKNOWN_74,
    UNKNOWN_75,
    UNKNOWN_76,
    UNKNOWN_77,
  }

  public int get(DataLength index) {
    return data.get(index.ordinal());
  }

  @Override
  public String toString() {
    String result = "00: ";
    for (int i = 0; i < data.size(); ++i) {
      result += String.format("%02d ", data.get(i));
      if (((i + 1) % 10) == 0) {
        result += String.format("\n%02d: ", i);
      }
    }
    return result;
  }

  static class Factory extends SectionFactory<List<Integer>> {
    Factory() {
      super(Ids.DATA_LENGTH_SECTION,
          new DataLengthNativeAdapter(),
          new DataLengthProtoAdapter(),
          DataLengthSection.class);
    }
  }
}
