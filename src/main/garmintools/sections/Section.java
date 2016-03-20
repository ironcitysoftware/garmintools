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

import garmintools.Proto;
import garmintools.adapters.nativo.NativeAdapter;
import garmintools.adapters.nativo.NativeOutput;
import garmintools.adapters.proto.ProtoAdapter;

abstract class Section<T> {
  final int sectionNumber;
  final NativeAdapter<T> nativeAdapter;
  final ProtoAdapter<T> protoAdapter;
  final T data;

  Section(int sectionNumber, T data, NativeAdapter<T> nativeAdapter, ProtoAdapter<T> protoAdapter) {
    this.sectionNumber = sectionNumber;
    this.data = data;
    this.nativeAdapter = nativeAdapter;
    this.protoAdapter = protoAdapter;
  }

  /** This is the second pass to give constructed sections a chance at reading the proto. */
  public void mergeFromProto(SectionManager manager, Proto.NavigationData proto) {
    // default is no-op.
  }

  public void mergeToProto(SectionManager sectionManager, Proto.NavigationData.Builder builder) {
    protoAdapter.write(data, builder);
  }

  public NativeOutput getNativeBytes(SectionManager sectionManager) {
    return nativeAdapter.write(data);
  }

  @Override
  public String toString() {
    return data.toString();
  }
}
