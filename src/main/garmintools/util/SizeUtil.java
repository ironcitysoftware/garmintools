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

package garmintools.util;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

public class SizeUtil {
  public static int getLongestLength(Collection<String> strings) {
    int longestLength = 0;
    for (String string : strings) {
      if (string.length() > longestLength) {
        longestLength = string.length();
      }
    }
    return longestLength;
  }

  public static <T> int getLargest(List<T> list, final Function<T, Integer> sizeOf) {
    Ordering<T> sizeOrdering = new Ordering<T>() {
      @Override
      public int compare(T left, T right) {
        return Ints.compare(sizeOf.apply(left), sizeOf.apply(right));
      }
    };
    return sizeOf.apply(sizeOrdering.max(list));
  }
}
