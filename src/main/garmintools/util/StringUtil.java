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

import com.google.common.base.Preconditions;

public class StringUtil {
  public static String pad(String text, int length) {
    Preconditions.checkArgument(text.length() <= length,
        String.format("Text [%s] exceeds max length %d", text, length));
    for (int i = text.length(); i < length; ++i) {
      text += " ";
    }
    return text;
  }
}
