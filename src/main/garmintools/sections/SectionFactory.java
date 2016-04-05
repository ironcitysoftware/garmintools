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
import garmintools.adapters.garmin.GarminAdapter;
import garmintools.adapters.proto.ProtoAdapter;
import garmintools.wrappers.TableOfContentsEntry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

abstract class SectionFactory<T> {
  final int sectionNumber;
  final GarminAdapter<T> garminAdapter;
  final ProtoAdapter<T> protoAdapter;
  final Class<?> sectionClass;

  SectionFactory(int sectionNumber, GarminAdapter<T> garminAdapter, ProtoAdapter<T> protoAdapter,
      Class<?> sectionClass) {
    this.sectionNumber = sectionNumber;
    this.garminAdapter = garminAdapter;
    this.protoAdapter = protoAdapter;
    this.sectionClass = sectionClass;
  }

  public int getSectionNumber() {
    return sectionNumber;
  }

  public Section<T> createFromGarmin(DataLengthSection dataLengthSection, TableOfContentsEntry entry, ByteBuffer byteBuffer) {
    return createSection(garminAdapter.read(dataLengthSection, entry, byteBuffer));
  }

  public Section<T> createFromProto(Proto.NavigationData proto) {
    return createSection(protoAdapter.read(proto));
  }

  @SuppressWarnings("unchecked")
  private Section<T> createSection(T data) {
    try {
      for (Constructor<?> constructor : sectionClass.getDeclaredConstructors()) {
        if (constructor.getParameterTypes().length == 4) {
          return (Section<T>) constructor.newInstance(sectionNumber, data, garminAdapter, protoAdapter);
        }
      }
      throw new IllegalStateException("Constructor not found for " + sectionClass.getName());
    } catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
  }
}
