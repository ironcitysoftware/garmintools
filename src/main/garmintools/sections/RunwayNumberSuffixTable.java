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

import garmintools.keys.IndexForeignKey;

public class RunwayNumberSuffixTable {
  private static final String[] RUNWAY_NUMBER_SUFFIXES = { "", "CENTER", "LEFT", "RIGHT", "TRUE" };

  public String lookup(IndexForeignKey key) {
    return RUNWAY_NUMBER_SUFFIXES[key.getIndex()];
  }

  public IndexForeignKey getKey(String string) {
    for (int index = 0; index < RUNWAY_NUMBER_SUFFIXES.length; ++index) {
      if (string.equals(RUNWAY_NUMBER_SUFFIXES[index])) {
        return new IndexForeignKey(index);
      }
    }
    throw new IllegalArgumentException("Not a known runway suffix: " + string);
  }
}
